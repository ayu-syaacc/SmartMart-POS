package com.smartmart.dao;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.StokLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StokLogDAO {

    /**
     * Menyimpan catatan perubahan stok.
     */
    public int catat(StokLog log) throws SQLException {

        String sql =
                "INSERT INTO stok_log " +
                "(waktu, kode_barang, id_lot, jenis, jumlah, " +
                "id_pegawai, alasan) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, log.getWaktu());
            ps.setString(2, log.getKodeBarang());

            if (log.getIdLot() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, log.getIdLot());
            }

            ps.setString(4, log.getJenis());
            ps.setDouble(5, log.getJumlah());
            ps.setInt(6, log.getIdPegawai());
            ps.setString(7, log.getAlasan());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    log.setIdLog(id);
                    return id;
                }
            }
        }

        return -1;
    }

    /**
     * Mengambil seluruh catatan stok.
     */
    public List<StokLog> ambilSemua() throws SQLException {

        String sql =
                "SELECT id_log, waktu, kode_barang, id_lot, " +
                "jenis, jumlah, id_pegawai, alasan " +
                "FROM stok_log " +
                "ORDER BY id_log DESC";

        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return petakanSemua(rs);
        }
    }

    /**
     * Mengambil log berdasarkan jenis perubahan stok.
     * Digunakan oleh LossGuard untuk fitur filter jenis.
     */
    public List<StokLog> ambilBerdasarkanJenis(String jenis)
            throws SQLException {

        String sql =
                "SELECT id_log, waktu, kode_barang, id_lot, " +
                "jenis, jumlah, id_pegawai, alasan " +
                "FROM stok_log " +
                "WHERE jenis = ? " +
                "ORDER BY id_log DESC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, jenis);

            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    /**
     * Mengambil log berdasarkan kode barang.
     */
    public List<StokLog> ambilBerdasarkanBarang(String kodeBarang)
            throws SQLException {

        String sql =
                "SELECT id_log, waktu, kode_barang, id_lot, " +
                "jenis, jumlah, id_pegawai, alasan " +
                "FROM stok_log " +
                "WHERE kode_barang = ? " +
                "ORDER BY id_log DESC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, kodeBarang);

            try (ResultSet rs = ps.executeQuery()) {
                return petakanSemua(rs);
            }
        }
    }

    private List<StokLog> petakanSemua(ResultSet rs)
            throws SQLException {

        List<StokLog> hasil = new ArrayList<>();

        while (rs.next()) {
            hasil.add(petakan(rs));
        }

        return hasil;
    }

    private StokLog petakan(ResultSet rs)
            throws SQLException {

        int idLot = rs.getInt("id_lot");
        Integer idLotValue = rs.wasNull() ? null : idLot;

        return new StokLog(
                rs.getInt("id_log"),
                rs.getString("waktu"),
                rs.getString("kode_barang"),
                idLotValue,
                rs.getString("jenis"),
                rs.getDouble("jumlah"),
                rs.getInt("id_pegawai"),
                rs.getString("alasan")
        );
    }
}