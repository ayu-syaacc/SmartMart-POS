
package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.Produk;

/**
 * DAO untuk mengelola data produk SmartMart.
 * Stok produk dihitung dari tabel stok_lot.
 */
public class ProdukDAO {

    private static final String QUERY_DASAR = """
            SELECT
                p.kode_barang,
                p.nama,
                p.harga,
                p.satuan,
                p.stok_minimum,
                p.dapat_diretur,
                COALESCE(SUM(s.jumlah_sisa), 0) AS stok
            FROM produk p
            LEFT JOIN stok_lot s
                ON p.kode_barang = s.kode_barang
            """;

    /**
     * Mengambil seluruh produk beserta total stoknya.
     */
    public List<Produk> ambilSemua() throws SQLException {
        String sql = QUERY_DASAR
                + """
                  GROUP BY p.kode_barang
                  ORDER BY p.nama
                  """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return petakanSemua(rs);
        }
    }

    /**
     * Mencari produk berdasarkan kode barang.
     */
    public Produk cariKode(String kodeBarang) throws SQLException {
        String sql = QUERY_DASAR
                + """
                  WHERE p.kode_barang = ?
                  GROUP BY p.kode_barang
                  """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kodeBarang.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    /**
     * Mencari produk berdasarkan nama.
     */
    public List<Produk> cariNama(String kataKunci) throws SQLException {
        String sql = QUERY_DASAR
                + """
                  WHERE p.nama LIKE ?
                  GROUP BY p.kode_barang
                  ORDER BY p.nama
                  """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + kataKunci.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /**
     * Menambahkan produk baru.
     * Stok awal tidak dimasukkan di sini karena stok dikelola melalui stok_lot.
     */
    public void tambah(Produk produk) throws SQLException {
        String sql = """
                INSERT INTO produk (
                    kode_barang,
                    nama,
                    harga,
                    satuan,
                    stok_minimum,
                    dapat_diretur
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, produk.getKodeBarang());
            ps.setString(2, produk.getNama());
            ps.setLong(3, produk.getHarga());
            ps.setString(4, produk.getSatuan());
            ps.setInt(5, produk.getStokMinimum());
            ps.setInt(6, produk.isDapatDiretur() ? 1 : 0);

            ps.executeUpdate();
        }
    }

    /**
     * Mengubah data produk.
     * Kode lama digunakan sebagai identitas pencarian.
     */
    public void ubah(String kodeLama, Produk produk) throws SQLException {
        String sql = """
                UPDATE produk
                SET kode_barang = ?,
                    nama = ?,
                    harga = ?,
                    satuan = ?,
                    stok_minimum = ?,
                    dapat_diretur = ?
                WHERE kode_barang = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, produk.getKodeBarang());
            ps.setString(2, produk.getNama());
            ps.setLong(3, produk.getHarga());
            ps.setString(4, produk.getSatuan());
            ps.setInt(5, produk.getStokMinimum());
            ps.setInt(6, produk.isDapatDiretur() ? 1 : 0);
            ps.setString(7, kodeLama);

            ps.executeUpdate();
        }
    }

    /**
     * Menghapus produk.
     * Jika produk sudah direferensikan oleh data lain, database akan menolak
     * penghapusan ketika foreign key aktif.
     */
    public void hapus(String kodeBarang) throws SQLException {
        String sql = "DELETE FROM produk WHERE kode_barang = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);
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
        Produk produk = new Produk(
                rs.getString("kode_barang"),
                rs.getString("nama"),
                rs.getLong("harga"),
                rs.getString("satuan"),
                rs.getInt("stok_minimum"),
                rs.getInt("dapat_diretur") == 1
        );

        produk.setStok(rs.getDouble("stok"));

        return produk;
    }
}
