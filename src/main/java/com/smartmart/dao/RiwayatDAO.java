package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RiwayatDAO {

    public static class Baris {
        public String id;
        public LocalDateTime waktu;
        public String pelanggan;
        public int total;
        public String metode;
        public String status;
        public int poinDiperoleh;
        public boolean adaMember;
        public String kasir = "-";
    }

    public static class Item {
        public String nama;
        public String satuan;
        public double jumlah;
        public int hargaAwal;
        public int subtotal;
        public String sumberDiskon;
    }

    public static final String PERIODE_SEMUA = "SEMUA";
    public static final String PERIODE_HARI_INI = "HARI_INI";
    public static final String PERIODE_MINGGU_INI = "MINGGU_INI";

    static final String SQL_CARI =
            "SELECT t.id_transaksi, t.waktu, COALESCE(m.nama, 'Umum') AS pelanggan, t.total, "
            + "       COALESCE(p.metode, '-') AS metode, t.status, COALESCE(t.poin_diperoleh, 0) AS poin, "
            + "       t.id_member, COALESCE(pg.nama, '-') AS kasir "
            + "FROM transaksi t "
            + "LEFT JOIN member m ON m.id_member = t.id_member "
            + "LEFT JOIN pembayaran p ON p.id_transaksi = t.id_transaksi "
            + "LEFT JOIN pegawai pg ON pg.id_pegawai = t.id_pegawai "
            + "WHERE t.status <> 'BERJALAN' "
            + "  AND (? = '' OR t.status = ?) "
            + "  AND date(t.waktu) >= date(?) "
            + "  AND (? = '' OR t.id_transaksi LIKE ? OR t.waktu LIKE ? OR m.nama LIKE ? "
            + "       OR EXISTS (SELECT 1 FROM detail_transaksi d JOIN produk pr ON pr.kode_barang = d.kode_barang "
            + "                  WHERE d.id_transaksi = t.id_transaksi AND pr.nama LIKE ?)) "
            + "ORDER BY t.waktu DESC LIMIT 200";

    static final String SQL_SATU =
            "SELECT t.id_transaksi, t.waktu, COALESCE(m.nama, 'Umum') AS pelanggan, t.total, "
            + "       COALESCE(p.metode, '-') AS metode, t.status, COALESCE(t.poin_diperoleh, 0) AS poin, "
            + "       t.id_member, COALESCE(pg.nama, '-') AS kasir "
            + "FROM transaksi t "
            + "LEFT JOIN member m ON m.id_member = t.id_member "
            + "LEFT JOIN pembayaran p ON p.id_transaksi = t.id_transaksi "
            + "LEFT JOIN pegawai pg ON pg.id_pegawai = t.id_pegawai "
            + "WHERE t.id_transaksi = ?";

    static final String SQL_ITEM =
            "SELECT pr.nama, pr.satuan, d.jumlah, d.harga_awal, d.subtotal, d.sumber_diskon "
            + "FROM detail_transaksi d JOIN produk pr ON pr.kode_barang = d.kode_barang "
            + "WHERE d.id_transaksi = ? ORDER BY d.id_detail_trx";

    public List<Baris> cari(Connection c, String kata, String status, String periode) throws SQLException {
        String k = kata == null ? "" : kata.trim();
        String like = "%" + k + "%";
        LocalDate dari = LocalDate.of(1970, 1, 1);
        if (PERIODE_HARI_INI.equals(periode)) dari = LocalDate.now();
        if (PERIODE_MINGGU_INI.equals(periode)) dari = LocalDate.now().minusDays(6);

        List<Baris> hasil = new ArrayList<Baris>();
        PreparedStatement ps = c.prepareStatement(SQL_CARI);
        try {
            String st = status == null ? "" : status;
            ps.setString(1, st);
            ps.setString(2, st);
            ps.setString(3, dari.toString());
            ps.setString(4, k);
            ps.setString(5, like);
            ps.setString(6, like);
            ps.setString(7, like);
            ps.setString(8, like);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    Baris b = new Baris();
                    b.id = rs.getString("id_transaksi");
                    b.waktu = WaktuUtil.parse(rs.getString("waktu"));
                    b.pelanggan = rs.getString("pelanggan");
                    b.total = rs.getInt("total");
                    b.metode = rs.getString("metode");
                    b.status = rs.getString("status");
                    b.poinDiperoleh = rs.getInt("poin");
                    rs.getLong("id_member");
                    b.adaMember = !rs.wasNull();
                    b.kasir = rs.getString("kasir");
                    hasil.add(b);
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return hasil;
    }

    public Baris ambilSatu(Connection c, String idTransaksi) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_SATU);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                if (!rs.next()) return null;
                Baris b = new Baris();
                b.id = rs.getString("id_transaksi");
                b.waktu = WaktuUtil.parse(rs.getString("waktu"));
                b.pelanggan = rs.getString("pelanggan");
                b.total = rs.getInt("total");
                b.metode = rs.getString("metode");
                b.status = rs.getString("status");
                b.poinDiperoleh = rs.getInt("poin");
                rs.getLong("id_member");
                b.adaMember = !rs.wasNull();
                b.kasir = rs.getString("kasir");
                return b;
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    public List<Item> ambilItem(Connection c, String idTransaksi) throws SQLException {
        List<Item> hasil = new ArrayList<Item>();
        PreparedStatement ps = c.prepareStatement(SQL_ITEM);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    Item i = new Item();
                    i.nama = rs.getString("nama");
                    i.satuan = rs.getString("satuan");
                    i.jumlah = rs.getDouble("jumlah");
                    i.hargaAwal = rs.getInt("harga_awal");
                    i.subtotal = rs.getInt("subtotal");
                    i.sumberDiskon = rs.getString("sumber_diskon");
                    hasil.add(i);
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return hasil;
    }
}
