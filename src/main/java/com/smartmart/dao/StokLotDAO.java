package com.smartmart.dao;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.StokLot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StokLotDAO {

    /**
     * Mengambil lot terdekat kedaluwarsanya untuk suatu barang.
     * Prinsip yang digunakan adalah FEFO:
     * First Expired, First Out.
     */
    public StokLot ambilLotTerdekat(String kodeBarang) throws SQLException {

        String sql =
                "SELECT id_lot, kode_barang, id_detail, " +
                "tgl_kedaluwarsa, jumlah_sisa " +
                "FROM stok_lot " +
                "WHERE kode_barang = ? " +
                "AND jumlah_sisa > 0 " +
                "ORDER BY " +
                "CASE WHEN tgl_kedaluwarsa IS NULL THEN 1 ELSE 0 END, " +
                "date(tgl_kedaluwarsa) ASC, " +
                "id_lot ASC " +
                "LIMIT 1";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    /**
     * Mengambil seluruh lot untuk satu barang.
     */
    public List<StokLot> ambilBerdasarkanBarang(String kodeBarang)
            throws SQLException {

        String sql =
                "SELECT id_lot, kode_barang, id_detail, " +
                "tgl_kedaluwarsa, jumlah_sisa " +
                "FROM stok_lot " +
                "WHERE kode_barang = ? " +
                "ORDER BY " +
                "CASE WHEN tgl_kedaluwarsa IS NULL THEN 1 ELSE 0 END, " +
                "date(tgl_kedaluwarsa) ASC, " +
                "id_lot ASC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);

            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /**
     * Mengambil lot berdasarkan ID.
     */
    public StokLot ambilById(int idLot) throws SQLException {

        String sql =
                "SELECT id_lot, kode_barang, id_detail, " +
                "tgl_kedaluwarsa, jumlah_sisa " +
                "FROM stok_lot " +
                "WHERE id_lot = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, idLot);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    /**
     * Menambahkan lot baru dan mengembalikan ID lot.
     */
    public int tambah(StokLot lot) throws SQLException {

        String sql =
                "INSERT INTO stok_lot " +
                "(kode_barang, id_detail, tgl_kedaluwarsa, jumlah_sisa) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, lot.getKodeBarang());

            if (lot.getIdDetail() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, lot.getIdDetail());
            }

            if (lot.getTglKedaluwarsa() == null) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(3, lot.getTglKedaluwarsa().toString());
            }

            ps.setInt(4, lot.getJumlahSisa());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    lot.setIdLot(id);
                    return id;
                }
            }
        }

        return -1;
    }

    /**
     * Mengubah jumlah sisa stok lot.
     */
    public void ubahJumlah(int idLot, int jumlahSisa)
            throws SQLException {

        if (jumlahSisa < 0) {
            throw new IllegalArgumentException(
                    "Jumlah sisa tidak boleh negatif."
            );
        }

        String sql =
                "UPDATE stok_lot " +
                "SET jumlah_sisa = ? " +
                "WHERE id_lot = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, jumlahSisa);
            ps.setInt(2, idLot);

            ps.executeUpdate();
        }
    }

    /**
     * Mengurangi stok lot secara langsung di database.
     * Pengurangan gagal jika stok tidak mencukupi.
     */
    public boolean kurangi(int idLot, int jumlah)
            throws SQLException {

        if (jumlah <= 0) {
            throw new IllegalArgumentException(
                    "Jumlah pengurangan harus lebih dari 0."
            );
        }

        String sql =
                "UPDATE stok_lot " +
                "SET jumlah_sisa = jumlah_sisa - ? " +
                "WHERE id_lot = ? " +
                "AND jumlah_sisa >= ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, jumlah);
            ps.setInt(2, idLot);
            ps.setInt(3, jumlah);

            return ps.executeUpdate() > 0;
        }
    }

    private List<StokLot> petakanSemua(ResultSet rs)
            throws SQLException {

        List<StokLot> hasil = new ArrayList<>();

        while (rs.next()) {
            hasil.add(petakan(rs));
        }

        return hasil;
    }

    private StokLot petakan(ResultSet rs)
            throws SQLException {

        String tanggal = rs.getString("tgl_kedaluwarsa");

        LocalDate tglKedaluwarsa = null;

        if (tanggal != null && !tanggal.isBlank()) {
            tglKedaluwarsa = LocalDate.parse(tanggal);
        }

        int idDetail = rs.getInt("id_detail");

        Integer idDetailValue =
                rs.wasNull() ? null : idDetail;

        return new StokLot(
                rs.getInt("id_lot"),
                rs.getString("kode_barang"),
                idDetailValue,
                tglKedaluwarsa,
                rs.getInt("jumlah_sisa")
        );
    }
}