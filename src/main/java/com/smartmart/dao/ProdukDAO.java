package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.Produk;

/**
 * Akses data tabel produk (branch: feature/product-model).
 *
 * Mengikuti schema.sql milik Epi:
 *   produk(kode_barang TEXT PK, nama, harga INTEGER, satuan, stok_minimum INTEGER,
 *          dapat_diretur INTEGER, kategori TEXT, aktif INTEGER)
 *
 * Untuk kasir: cariKode, cariNama, dan ambilAktif hanya mengembalikan produk AKTIF.
 * Untuk Product Management (Salsa) dan riwayat/retur: ambilSemua dan ambilByKode
 * mengembalikan semua produk termasuk yang nonaktif.
 * kode_barang adalah kunci utama yang dirujuk tabel lain, jadi ubah() tidak mengganti kode.
 */
public class ProdukDAO {

    private static final String KOLOM =
            "kode_barang, nama, kategori, harga, satuan, stok_minimum, dapat_diretur, aktif";

    static final String SQL_CARI_KODE = "SELECT " + KOLOM + " FROM produk WHERE kode_barang = ? AND aktif = 1";
    static final String SQL_BY_KODE = "SELECT " + KOLOM + " FROM produk WHERE kode_barang = ?";
    static final String SQL_CARI_NAMA = "SELECT " + KOLOM + " FROM produk WHERE aktif = 1 AND nama LIKE ? ORDER BY nama";
    static final String SQL_AKTIF = "SELECT " + KOLOM + " FROM produk WHERE aktif = 1 ORDER BY nama";
    static final String SQL_AKTIF_KATEGORI = "SELECT " + KOLOM + " FROM produk WHERE aktif = 1 AND kategori = ? ORDER BY nama";
    static final String SQL_SEMUA = "SELECT " + KOLOM + " FROM produk ORDER BY nama";
    static final String SQL_TAMBAH = "INSERT INTO produk (kode_barang, nama, kategori, harga, satuan, stok_minimum, dapat_diretur, aktif) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, 1)";
    static final String SQL_UBAH = "UPDATE produk SET nama = ?, kategori = ?, harga = ?, satuan = ?, stok_minimum = ?, "
            + "dapat_diretur = ? WHERE kode_barang = ?";
    static final String SQL_SET_AKTIF = "UPDATE produk SET aktif = ? WHERE kode_barang = ?";

    /** Scan barcode di kasir: cari persis berdasarkan kode, hanya produk aktif; null bila tidak ada. */
    public Produk cariKode(String kodeBarang) throws SQLException {
        return cariSatu(SQL_CARI_KODE, kodeBarang);
    }

    /** Cari berdasarkan kode, aktif maupun nonaktif (riwayat, retur, Product Management). */
    public Produk ambilByKode(String kodeBarang) throws SQLException {
        return cariSatu(SQL_BY_KODE, kodeBarang);
    }

    /** Cari berdasarkan sebagian nama, hanya produk aktif. */
    public List<Produk> cariNama(String kataKunci) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_CARI_NAMA)) {
            ps.setString(1, "%" + kataKunci.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /** Produk aktif untuk grid kasir, boleh difilter kategori (null atau "Semua" = semua kategori). */
    public List<Produk> ambilAktif(String kategori) throws SQLException {
        boolean semua = kategori == null || kategori.equalsIgnoreCase("Semua");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(semua ? SQL_AKTIF : SQL_AKTIF_KATEGORI)) {
            if (!semua) {
                ps.setString(1, kategori);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /** Semua produk termasuk yang nonaktif, urut nama (Product Management). */
    public List<Produk> ambilSemua() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SQL_SEMUA)) {
            return petakanSemua(rs);
        }
    }

    /** Menambah produk (otomatis aktif). Melempar SQLException bila kode_barang sudah dipakai. */
    public void tambah(Produk p) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_TAMBAH)) {
            ps.setString(1, p.getKodeBarang());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getKategori());
            ps.setLong(4, p.getHarga());
            ps.setString(5, p.getSatuan());
            ps.setInt(6, p.getStokMinimum());
            ps.setInt(7, p.isDapatDiretur() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    /** Mengubah data produk berdasarkan kode_barang (kode dan status aktif tidak diubah). false bila kode tidak ada. */
    public boolean ubah(Produk p) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_UBAH)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getKategori());
            ps.setLong(3, p.getHarga());
            ps.setString(4, p.getSatuan());
            ps.setInt(5, p.getStokMinimum());
            ps.setInt(6, p.isDapatDiretur() ? 1 : 0);
            ps.setString(7, p.getKodeBarang());
            return ps.executeUpdate() > 0;
        }
    }

    /** Nonaktifkan (bukan hapus) agar riwayat transaksi lama tetap utuh. false bila kode tidak ada. */
    public boolean nonaktifkan(String kodeBarang) throws SQLException {
        return setAktif(kodeBarang, false);
    }

    /** Mengaktifkan kembali produk yang pernah dinonaktifkan. false bila kode tidak ada. */
    public boolean aktifkan(String kodeBarang) throws SQLException {
        return setAktif(kodeBarang, true);
    }

    private boolean setAktif(String kodeBarang, boolean aktif) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL_SET_AKTIF)) {
            ps.setInt(1, aktif ? 1 : 0);
            ps.setString(2, kodeBarang);
            return ps.executeUpdate() > 0;
        }
    }

    private Produk cariSatu(String sql, String kodeBarang) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kodeBarang.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    private List<Produk> petakanSemua(ResultSet rs) throws SQLException {
        List<Produk> hasil = new ArrayList<>();
        while (rs.next()) {
            hasil.add(petakan(rs));
        }
        return hasil;
    }

    private Produk petakan(ResultSet rs) throws SQLException {
        return new Produk(
                rs.getString("kode_barang"),
                rs.getString("nama"),
                rs.getString("kategori"),
                rs.getLong("harga"),
                rs.getString("satuan"),
                rs.getInt("stok_minimum"),
                rs.getInt("dapat_diretur") == 1,
                rs.getInt("aktif") == 1);
    }


    /**
     * Menghitung total stok aktual berdasarkan seluruh lot produk.
     * Mengembalikan 0 jika produk belum memiliki stok lot.
     */
    public int hitungStok(String kodeBarang) throws SQLException {
        String sql = "SELECT COALESCE(SUM(jumlah_sisa), 0) "
                   + "FROM stok_lot WHERE kode_barang = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

}
