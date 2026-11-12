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
 * ASUMSI SKEMA (cocokkan dengan schema.sql milik Epi):
 *   produk(id INTEGER PK, kode TEXT UNIQUE, nama TEXT, kategori TEXT, harga INTEGER,
 *          per_kg INTEGER, stok_minimum INTEGER, aktif INTEGER)
 * ASUMSI: DatabaseConnection.getConnection() mengembalikan java.sql.Connection.
 *
 * cariKode, cariNama, dan ambilAktif hanya mengembalikan produk aktif (untuk kasir).
 * ambilSemua, tambah, ubah, dan nonaktifkan dipakai Salsa (Product Management).
 */
public class ProdukDAO {

    private static final String KOLOM =
            "id, kode, nama, kategori, harga, per_kg, stok_minimum, aktif";

    public Produk cariKode(String kode) throws SQLException {
        String sql = "SELECT " + KOLOM + " FROM produk WHERE kode = ? AND aktif = 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public List<Produk> cariNama(String kataKunci) throws SQLException {
        String sql = "SELECT " + KOLOM + " FROM produk WHERE aktif = 1 "
                + "AND nama LIKE ? ORDER BY nama";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + kataKunci.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /** Produk aktif untuk grid kasir, boleh difilter kategori (null atau "Semua" = semua). */
    public List<Produk> ambilAktif(String kategori) throws SQLException {
        boolean semua = kategori == null || kategori.equalsIgnoreCase("Semua");
        String sql = "SELECT " + KOLOM + " FROM produk WHERE aktif = 1"
                + (semua ? "" : " AND kategori = ?") + " ORDER BY nama";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (!semua) {
                ps.setString(1, kategori);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /** Semua produk termasuk yang nonaktif (untuk Product Management). */
    public List<Produk> ambilSemua() throws SQLException {
        String sql = "SELECT " + KOLOM + " FROM produk ORDER BY nama";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return petakanSemua(rs);
        }
    }

    /** Menambah produk, mengembalikan id baru. */
    public int tambah(Produk p) throws SQLException {
        String sql = "INSERT INTO produk (kode, nama, kategori, harga, per_kg, stok_minimum, aktif) "
                + "VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getKode());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getKategori());
            ps.setLong(4, p.getHarga());
            ps.setInt(5, p.isPerKg() ? 1 : 0);
            ps.setInt(6, p.getStokMinimum());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void ubah(Produk p) throws SQLException {
        String sql = "UPDATE produk SET kode = ?, nama = ?, kategori = ?, harga = ?, "
                + "per_kg = ?, stok_minimum = ? WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getKode());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getKategori());
            ps.setLong(4, p.getHarga());
            ps.setInt(5, p.isPerKg() ? 1 : 0);
            ps.setInt(6, p.getStokMinimum());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    /** Nonaktifkan (bukan hapus) agar riwayat transaksi lama tetap utuh. */
    public void nonaktifkan(int id) throws SQLException {
        String sql = "UPDATE produk SET aktif = 0 WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
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
                rs.getInt("id"),
                rs.getString("kode"),
                rs.getString("nama"),
                rs.getString("kategori"),
                rs.getLong("harga"),
                rs.getInt("per_kg") == 1,
                rs.getInt("stok_minimum"),
                rs.getInt("aktif") == 1);
    }
}
