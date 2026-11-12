package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.smartmart.db.DatabaseConnection;

/**
 * Pembacaan data (hanya baca) untuk layar Laporan Penjualan admin.
 * Semua nilai uang dikembalikan sudah berformat rupiah (mis. "Rp1.250.000").
 */
public class LaporanDAO {

    public enum Periode { HARI_INI, TUJUH_HARI, BULAN_INI }

    public static class BarisProduk {
        public String nama;
        public String jumlah;
    }

    public static class BarisKasir {
        public String nama;
        public int transaksi;
        public String total;
    }

    public static class Laporan {
        public String pendapatanBersih = "Rp0";
        public int jumlahTransaksi;
        public String rataRata = "Rp0";
        public String totalKerugian = "Rp0";
        public String selisihPenerimaan = "Rp0";
        public String kerugianKedaluwarsa = "Rp0";
        public String kerugianRusak = "Rp0";
        public String kerugianHilang = "Rp0";
        public List<BarisProduk> terlaris = new ArrayList<>();
        public List<BarisProduk> terendah = new ArrayList<>();
        public List<BarisKasir> kasir = new ArrayList<>();
    }

    private static final String STATUS_OK = "UPPER(t.status) IN ('SELESAI', 'PAID')";

    static final String SQL_OMZET =
            "SELECT COUNT(*) AS jumlah, COALESCE(SUM(t.total), 0) AS omzet FROM transaksi t "
            + "WHERE " + STATUS_OK + " AND substr(t.waktu, 1, 10) BETWEEN ? AND ?";

    static final String SQL_REFUND =
            "SELECT COALESCE(SUM(total_refund), 0) AS refund FROM retur "
            + "WHERE substr(waktu, 1, 10) BETWEEN ? AND ?";

    static final String SQL_KERUGIAN =
            "SELECT UPPER(l.jenis) AS jenis, COALESCE(SUM(ABS(l.jumlah) * p.harga), 0) AS rugi "
            + "FROM stok_log l JOIN produk p ON p.kode_barang = l.kode_barang "
            + "WHERE UPPER(l.jenis) IN ('RUSAK', 'KEDALUWARSA', 'HILANG') "
            + "  AND substr(l.waktu, 1, 10) BETWEEN ? AND ? "
            + "GROUP BY UPPER(l.jenis)";

    static final String SQL_SELISIH =
            "SELECT COALESCE(SUM((d.jumlah_dipesan - d.jumlah_diterima) * p.harga), 0) AS selisih "
            + "FROM detail_penerimaan d "
            + "JOIN penerimaan_barang b ON b.id_penerimaan = d.id_penerimaan "
            + "JOIN produk p ON p.kode_barang = d.kode_barang "
            + "WHERE substr(b.tanggal, 1, 10) BETWEEN ? AND ?";

    static final String SQL_PRODUK =
            "SELECT p.nama, SUM(d.jumlah) AS qty, p.satuan FROM detail_transaksi d "
            + "JOIN transaksi t ON t.id_transaksi = d.id_transaksi "
            + "JOIN produk p ON p.kode_barang = d.kode_barang "
            + "WHERE " + STATUS_OK + " AND substr(t.waktu, 1, 10) BETWEEN ? AND ? "
            + "GROUP BY p.kode_barang, p.nama, p.satuan "
            + "ORDER BY qty %s, p.nama LIMIT 5";

    static final String SQL_KASIR =
            "SELECT g.nama, COUNT(*) AS jumlah, COALESCE(SUM(t.total), 0) AS total "
            + "FROM transaksi t JOIN pegawai g ON g.id_pegawai = t.id_pegawai "
            + "WHERE " + STATUS_OK + " AND substr(t.waktu, 1, 10) BETWEEN ? AND ? "
            + "GROUP BY g.id_pegawai, g.nama ORDER BY total DESC";

    public Laporan ambil(Periode periode) throws SQLException {
        LocalDate sampai = LocalDate.now();
        LocalDate dari;
        switch (periode) {
            case TUJUH_HARI: dari = sampai.minusDays(6); break;
            case BULAN_INI:  dari = sampai.withDayOfMonth(1); break;
            default:         dari = sampai; break;
        }
        try (Connection c = DatabaseConnection.getConnection()) {
            return ambil(c, dari, sampai);
        }
    }

    Laporan ambil(Connection c, LocalDate dari, LocalDate sampai) throws SQLException {
        String a = dari.toString();
        String b = sampai.toString();
        Laporan lap = new Laporan();

        long omzet = 0;
        try (PreparedStatement ps = c.prepareStatement(SQL_OMZET)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    lap.jumlahTransaksi = rs.getInt("jumlah");
                    omzet = rs.getLong("omzet");
                }
            }
        }

        long refund = 0;
        try (PreparedStatement ps = c.prepareStatement(SQL_REFUND)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) refund = rs.getLong("refund");
            }
        }

        long pendapatan = omzet - refund;
        lap.pendapatanBersih = rupiah(pendapatan);
        lap.rataRata = rupiah(lap.jumlahTransaksi == 0 ? 0 : Math.round((double) pendapatan / lap.jumlahTransaksi));

        long rugiKedaluwarsa = 0, rugiRusak = 0, rugiHilang = 0;
        try (PreparedStatement ps = c.prepareStatement(SQL_KERUGIAN)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long v = Math.round(rs.getDouble("rugi"));
                    switch (rs.getString("jenis")) {
                        case "KEDALUWARSA": rugiKedaluwarsa = v; break;
                        case "RUSAK":       rugiRusak = v; break;
                        case "HILANG":      rugiHilang = v; break;
                        default: break;
                    }
                }
            }
        }
        lap.kerugianKedaluwarsa = rupiah(rugiKedaluwarsa);
        lap.kerugianRusak = rupiah(rugiRusak);
        lap.kerugianHilang = rupiah(rugiHilang);
        lap.totalKerugian = rupiah(rugiKedaluwarsa + rugiRusak + rugiHilang);

        try (PreparedStatement ps = c.prepareStatement(SQL_SELISIH)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) lap.selisihPenerimaan = rupiah(Math.round(rs.getDouble("selisih")));
            }
        }

        lap.terlaris = produk(c, String.format(SQL_PRODUK, "DESC"), a, b);
        lap.terendah = produk(c, String.format(SQL_PRODUK, "ASC"), a, b);

        try (PreparedStatement ps = c.prepareStatement(SQL_KASIR)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BarisKasir k = new BarisKasir();
                    k.nama = rs.getString("nama");
                    k.transaksi = rs.getInt("jumlah");
                    k.total = rupiah(rs.getLong("total"));
                    lap.kasir.add(k);
                }
            }
        }
        return lap;
    }

    private List<BarisProduk> produk(Connection c, String sql, String a, String b) throws SQLException {
        List<BarisProduk> hasil = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BarisProduk p = new BarisProduk();
                    p.nama = rs.getString("nama");
                    double qty = rs.getDouble("qty");
                    String jml = (qty == Math.rint(qty)) ? String.valueOf((long) qty) : String.valueOf(qty);
                    p.jumlah = jml + " " + rs.getString("satuan");
                    hasil.add(p);
                }
            }
        }
        return hasil;
    }

    static String rupiah(long nilai) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
        return (nilai < 0 ? "-Rp" : "Rp") + nf.format(Math.abs(nilai));
    }
}
