package com.smartmart.service;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.ItemKeranjang;
import com.smartmart.model.SmartCart;
import com.smartmart.model.StrukData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.smartmart.util.FormatRupiah;

/**
 * Menyimpan transaksi kasir setelah pembayaran berstatus PAID.
 * Semua langkah (transaksi, detail, pembayaran, stok lot, stok log, poin member,
 * log aktivitas) dijalankan dalam satu transaksi database: gagal satu, batal semua.
 */
public class PenjualanService {

    public static final int PERSEN_MEMBER = 10;
    public static final int POIN_PER_TUKAR = 100;
    public static final long NILAI_TUKAR = 5000;
    public static final long BELANJA_PER_POIN = 10000;

    private static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static class MemberInfo {
        public int id;
        public String noMember;
        public String nama;
        public int poin;
    }

    /** Hasil pemeriksaan pembayaran di dialog. */
    public static class Pembayaran {
        public String metode;       // Tunai, QRIS, Debit
        public long dibayar;
        public String referensi;
        public MemberInfo member;
        public int poinDitukar;
    }

    /** Mencari member berdasarkan nomor member; null bila tidak ada. */
    public MemberInfo cariMember(String noMember) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id_member, no_member, nama, poin FROM member WHERE UPPER(no_member) = UPPER(?)")) {
            ps.setString(1, noMember.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                MemberInfo m = new MemberInfo();
                m.id = rs.getInt("id_member");
                m.noMember = rs.getString("no_member");
                m.nama = rs.getString("nama");
                m.poin = rs.getInt("poin");
                return m;
            }
        }
    }

    /** Poin maksimum yang boleh ditukar: kelipatan 100, tidak melebihi poin member dan total belanja. */
    public static int poinMaksimum(MemberInfo member, long totalSetelahDiskon) {
        if (member == null) {
            return 0;
        }
        long dariTotal = (totalSetelahDiskon / NILAI_TUKAR) * POIN_PER_TUKAR;
        long dariPoin = (member.poin / POIN_PER_TUKAR) * POIN_PER_TUKAR;
        return (int) Math.min(dariTotal, dariPoin);
    }

    public StrukData bayar(int idKasir, String namaKasir, SmartCart cart, Pembayaran p) throws SQLException {
        if (cart.isKosong()) {
            throw new IllegalStateException("Keranjang kosong.");
        }
        long diskonPoin = (p.poinDitukar / POIN_PER_TUKAR) * NILAI_TUKAR;
        if (p.member == null && p.poinDitukar > 0) {
            throw new IllegalStateException("Poin hanya bisa ditukar oleh member.");
        }
        if (p.member != null && (p.poinDitukar % POIN_PER_TUKAR != 0 || p.poinDitukar > p.member.poin)) {
            throw new IllegalStateException("Jumlah poin yang ditukar tidak valid.");
        }
        long total = cart.getTotal() - diskonPoin;
        if (total <= 0) {
            throw new IllegalStateException("Total transaksi tidak valid.");
        }
        long dibayar = "Tunai".equals(p.metode) ? p.dibayar : total;
        if (dibayar < total) {
            throw new IllegalStateException("Uang yang diterima kurang dari total.");
        }
        long kembalian = dibayar - total;
        int poinDiperoleh = p.member == null ? 0 : (int) (total / BELANJA_PER_POIN);

        LocalDateTime waktu = LocalDateTime.now();
        String waktuTeks = waktu.format(FORMAT_WAKTU);

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int idTransaksi;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transaksi (waktu, id_pegawai, id_member, subtotal, diskon, poin_dipakai, "
                        + "poin_diperoleh, diskon_poin, total, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SELESAI')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, waktuTeks);
                    ps.setInt(2, idKasir);
                    if (p.member == null) {
                        ps.setNull(3, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(3, p.member.id);
                    }
                    ps.setLong(4, cart.getSubtotal());
                    ps.setLong(5, cart.getTotalHemat());
                    ps.setInt(6, p.poinDitukar);
                    ps.setInt(7, poinDiperoleh);
                    ps.setLong(8, diskonPoin);
                    ps.setLong(9, total);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new SQLException("ID transaksi tidak terbentuk.");
                        }
                        idTransaksi = rs.getInt(1);
                    }
                }

                StrukData struk = new StrukData();
                DecimalFormat jml = new DecimalFormat("0.###",
                        DecimalFormatSymbols.getInstance(Locale.forLanguageTag("id-ID")));

                for (ItemKeranjang i : cart.getItems()) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO detail_transaksi (id_transaksi, kode_barang, id_lot, jumlah, harga_awal, "
                            + "diskon, sumber_diskon, subtotal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, idTransaksi);
                        ps.setString(2, i.getProduk().getKodeBarang());
                        if (i.getLotId() == null) {
                            ps.setNull(3, java.sql.Types.INTEGER);
                        } else {
                            ps.setInt(3, i.getLotId());
                        }
                        ps.setDouble(4, i.getJumlah());
                        ps.setLong(5, i.getProduk().getHarga());
                        ps.setLong(6, i.getDiskon());
                        if (i.getJenisDiskon() == ItemKeranjang.JenisDiskon.TIDAK_ADA) {
                            ps.setNull(7, java.sql.Types.VARCHAR);
                        } else {
                            ps.setString(7, i.getJenisDiskon().name());
                        }
                        ps.setLong(8, i.getTotal());
                        ps.executeUpdate();
                    }

                    if (i.getLotId() != null) {
                        try (PreparedStatement ps = c.prepareStatement(
                                "UPDATE stok_lot SET jumlah_sisa = jumlah_sisa - ? "
                                + "WHERE id_lot = ? AND jumlah_sisa >= ?")) {
                            ps.setDouble(1, i.getJumlah());
                            ps.setInt(2, i.getLotId());
                            ps.setDouble(3, i.getJumlah());
                            if (ps.executeUpdate() == 0) {
                                throw new IllegalStateException("Stok " + i.getProduk().getNama()
                                        + " sudah berubah dan tidak cukup. Muat ulang keranjang.");
                            }
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO stok_log (waktu, kode_barang, id_lot, jenis, jumlah, id_pegawai, alasan) "
                            + "VALUES (?, ?, ?, 'PENJUALAN', ?, ?, ?)")) {
                        ps.setString(1, waktuTeks);
                        ps.setString(2, i.getProduk().getKodeBarang());
                        if (i.getLotId() == null) {
                            ps.setNull(3, java.sql.Types.INTEGER);
                        } else {
                            ps.setInt(3, i.getLotId());
                        }
                        ps.setDouble(4, -i.getJumlah());
                        ps.setInt(5, idKasir);
                        ps.setString(6, "Transaksi #" + idTransaksi);
                        ps.executeUpdate();
                    }

                    StrukData.Baris b = new StrukData.Baris();
                    b.nama = i.getProduk().getNama();
                    String harga = FormatRupiah.angka(i.getProduk().getHarga());
                    b.rincian = i.getProduk().isPerKg()
                            ? jml.format(i.getJumlah()) + " kg x " + harga
                            : jml.format(i.getJumlah()) + " x " + harga;
                    b.jumlahKotor = i.getHargaKotor();
                    if (i.getJenisDiskon() == ItemKeranjang.JenisDiskon.FRESHSAVE) {
                        b.ketDiskon = "FreshSave " + i.getPersenDiskon() + "% -" + FormatRupiah.angka(i.getDiskon());
                    } else if (i.getJenisDiskon() == ItemKeranjang.JenisDiskon.MEMBER) {
                        b.ketDiskon = "Diskon member -" + FormatRupiah.angka(i.getDiskon());
                    }
                    struk.items.add(b);
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO pembayaran (id_transaksi, metode, nominal, kembalian, status) "
                        + "VALUES (?, ?, ?, ?, 'PAID')")) {
                    ps.setInt(1, idTransaksi);
                    ps.setString(2, p.metode);
                    ps.setLong(3, dibayar);
                    ps.setLong(4, kembalian);
                    ps.executeUpdate();
                }

                int sisaPoin = 0;
                if (p.member != null) {
                    sisaPoin = p.member.poin - p.poinDitukar + poinDiperoleh;
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE member SET poin = ? WHERE id_member = ?")) {
                        ps.setInt(1, sisaPoin);
                        ps.setInt(2, p.member.id);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO log_aktivitas (waktu, id_pegawai, aksi, keterangan) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, waktuTeks);
                    ps.setInt(2, idKasir);
                    ps.setString(3, "TRANSAKSI");
                    ps.setString(4, "Transaksi " + StrukData.buatNomor(idTransaksi, waktu)
                            + " senilai " + FormatRupiah.format(total) + " (" + p.metode + ")");
                    ps.executeUpdate();
                }

                c.commit();

                struk.idTransaksi = idTransaksi;
                struk.nomor = StrukData.buatNomor(idTransaksi, waktu);
                struk.waktu = waktu;
                struk.kasir = namaKasir;
                if (p.member != null) {
                    struk.namaMember = p.member.nama;
                    struk.noMember = p.member.noMember;
                }
                struk.subtotal = cart.getSubtotal();
                struk.totalDiskon = cart.getTotalHemat();
                struk.poinDitukar = p.poinDitukar;
                struk.diskonPoin = diskonPoin;
                struk.total = total;
                struk.metode = p.metode;
                struk.dibayar = dibayar;
                struk.kembalian = kembalian;
                struk.poinDiperoleh = poinDiperoleh;
                struk.sisaPoin = sisaPoin;
                return struk;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Membangun ulang struk transaksi lama dari database (untuk tombol Lihat Struk di Riwayat). */
    public StrukData ambilStruk(int idTransaksi) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            StrukData s = new StrukData();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT t.id_transaksi, t.waktu, t.subtotal, t.diskon, t.poin_dipakai, t.poin_diperoleh, "
                    + "t.diskon_poin, t.total, COALESCE(pg.nama, '-') AS kasir, m.nama AS mnama, m.no_member, "
                    + "COALESCE(m.poin, 0) AS mpoin, COALESCE(p.metode, 'Tunai') AS metode, "
                    + "COALESCE(p.nominal, t.total) AS nominal, COALESCE(p.kembalian, 0) AS kembalian "
                    + "FROM transaksi t "
                    + "LEFT JOIN pegawai pg ON pg.id_pegawai = t.id_pegawai "
                    + "LEFT JOIN member m ON m.id_member = t.id_member "
                    + "LEFT JOIN pembayaran p ON p.id_transaksi = t.id_transaksi "
                    + "WHERE t.id_transaksi = ?")) {
                ps.setInt(1, idTransaksi);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    s.idTransaksi = rs.getInt("id_transaksi");
                    s.waktu = LocalDateTime.parse(rs.getString("waktu").trim().replace(' ', 'T').substring(0, 19));
                    s.nomor = StrukData.buatNomor(s.idTransaksi, s.waktu);
                    s.kasir = rs.getString("kasir");
                    s.namaMember = rs.getString("mnama");
                    s.noMember = rs.getString("no_member");
                    s.subtotal = rs.getLong("subtotal");
                    s.totalDiskon = rs.getLong("diskon");
                    s.poinDitukar = rs.getInt("poin_dipakai");
                    s.diskonPoin = rs.getLong("diskon_poin");
                    s.total = rs.getLong("total");
                    s.metode = rs.getString("metode");
                    s.dibayar = rs.getLong("nominal");
                    s.kembalian = rs.getLong("kembalian");
                    s.poinDiperoleh = rs.getInt("poin_diperoleh");
                    s.sisaPoin = rs.getInt("mpoin");
                }
            }
            DecimalFormat jml = new DecimalFormat("0.###",
                    DecimalFormatSymbols.getInstance(Locale.forLanguageTag("id-ID")));
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT pr.nama, pr.satuan, d.jumlah, d.harga_awal, d.diskon, d.sumber_diskon "
                    + "FROM detail_transaksi d JOIN produk pr ON pr.kode_barang = d.kode_barang "
                    + "WHERE d.id_transaksi = ? ORDER BY d.id_detail_trx")) {
                ps.setInt(1, idTransaksi);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        StrukData.Baris b = new StrukData.Baris();
                        double jumlah = rs.getDouble("jumlah");
                        long harga = rs.getLong("harga_awal");
                        long diskon = rs.getLong("diskon");
                        String sumber = rs.getString("sumber_diskon");
                        b.nama = rs.getString("nama");
                        b.jumlahKotor = Math.round(harga * jumlah);
                        b.rincian = "KG".equalsIgnoreCase(rs.getString("satuan"))
                                ? jml.format(jumlah) + " kg x " + FormatRupiah.angka(harga)
                                : jml.format(jumlah) + " x " + FormatRupiah.angka(harga);
                        if (diskon > 0 && "FRESHSAVE".equals(sumber)) {
                            long persen = b.jumlahKotor == 0 ? 0 : Math.round(diskon * 100.0 / b.jumlahKotor);
                            b.ketDiskon = "FreshSave " + persen + "% -" + FormatRupiah.angka(diskon);
                        } else if (diskon > 0) {
                            b.ketDiskon = "Diskon member -" + FormatRupiah.angka(diskon);
                        }
                        s.items.add(b);
                    }
                }
            }
            return s;
        }
    }
}
