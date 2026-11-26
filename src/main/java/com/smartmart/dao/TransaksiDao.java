
package com.smartmart.dao;

import com.smartmart.model.Transaksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TransaksiDao {

    private final Connection connection;

    public TransaksiDao(Connection connection) {
        this.connection = connection;
    }

    // Menyimpan transaksi
    public boolean simpan(Transaksi transaksi) {

        String sql = "INSERT INTO transaksi "
                + "(nomor_transaksi, tanggal_transaksi, "
                + "id_pegawai, id_member, subtotal, "
                + "diskon, total, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, transaksi.getNomorTransaksi());
            ps.setTimestamp(2,
                    Timestamp.valueOf(transaksi.getTanggalTransaksi()));
            ps.setInt(3, transaksi.getIdPegawai());

            if (transaksi.getIdMember() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, transaksi.getIdMember());
            }

            ps.setDouble(5, transaksi.getSubtotal());
            ps.setDouble(6, transaksi.getDiskon());
            ps.setDouble(7, transaksi.getTotal());
            ps.setString(8, transaksi.getStatus());

            int hasil = ps.executeUpdate();

            if (hasil > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        transaksi.setIdTransaksi(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan transaksi: "
                    + e.getMessage());
        }

        return false;
    }

    // Mencari transaksi berdasarkan ID
    public Transaksi cariById(int id) {

        String sql = "SELECT * FROM transaksi "
                + "WHERE id_transaksi = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ambilData(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Gagal mencari transaksi: "
                    + e.getMessage());
        }

        return null;
    }

    // Mengambil seluruh transaksi
    public List<Transaksi> ambilSemua() {

        List<Transaksi> daftar = new ArrayList<>();

        String sql = "SELECT * FROM transaksi "
                + "ORDER BY tanggal_transaksi DESC";

        try (PreparedStatement ps =
                connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftar.add(ambilData(rs));
            }

        } catch (SQLException e) {
            System.out.println("Gagal mengambil transaksi: "
                    + e.getMessage());
        }

        return daftar;
    }

    // Memperbarui status transaksi
    public boolean ubahStatus(int id, String status) {

        String sql = "UPDATE transaksi "
                + "SET status = ? WHERE id_transaksi = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Gagal mengubah status: "
                    + e.getMessage());
        }

        return false;
    }

    // Mengambil data dari database
    private Transaksi ambilData(ResultSet rs)
            throws SQLException {

        Transaksi transaksi = new Transaksi();

        transaksi.setIdTransaksi(
                rs.getInt("id_transaksi"));

        transaksi.setNomorTransaksi(
                rs.getString("nomor_transaksi"));

        Timestamp waktu =
                rs.getTimestamp("tanggal_transaksi");

        if (waktu != null) {
            transaksi.setTanggalTransaksi(
                    waktu.toLocalDateTime());
        }

        transaksi.setIdPegawai(
                rs.getInt("id_pegawai"));

        int idMember = rs.getInt("id_member");

        if (rs.wasNull()) {
            transaksi.setIdMember(null);
        } else {
            transaksi.setIdMember(idMember);
        }

        transaksi.setSubtotal(
                rs.getDouble("subtotal"));

        transaksi.setDiskon(
                rs.getDouble("diskon"));

        transaksi.setTotal(
                rs.getDouble("total"));

        transaksi.setStatus(
                rs.getString("status"));

        return transaksi;
    }
}
