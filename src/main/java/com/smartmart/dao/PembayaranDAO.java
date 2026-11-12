
package com.smartmart.dao;

import com.smartmart.model.Pembayaran;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class PembayaranDAO {

    private final Connection connection;

    public PembayaranDAO(Connection connection) {
        this.connection = connection;
    }

    // Menyimpan data pembayaran
    public boolean simpan(Pembayaran pembayaran) {

        String sql = "INSERT INTO pembayaran "
                + "(id_transaksi, metode_pembayaran, "
                + "jumlah_bayar, status_pembayaran, "
                + "tanggal_pembayaran, nomor_referensi) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, pembayaran.getIdTransaksi());
            ps.setString(2, pembayaran.getMetodePembayaran());
            ps.setDouble(3, pembayaran.getJumlahBayar());
            ps.setString(4, pembayaran.getStatusPembayaran());

            if (pembayaran.getTanggalPembayaran() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(
                        pembayaran.getTanggalPembayaran()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }

            // Nomor referensi khusus QRIS
            if (pembayaran instanceof com.smartmart.model.QRIS) {
                com.smartmart.model.QRIS qris =
                        (com.smartmart.model.QRIS) pembayaran;

                ps.setString(6, qris.getNomorReferensi());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            int hasil = ps.executeUpdate();

            if (hasil > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        pembayaran.setIdPembayaran(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Gagal menyimpan pembayaran: "
                    + e.getMessage());
        }

        return false;
    }

    // Mencari pembayaran berdasarkan ID transaksi
    public PembayaranInfo cariByTransaksi(int idTransaksi) {

        String sql = "SELECT * FROM pembayaran "
                + "WHERE id_transaksi = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, idTransaksi);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new PembayaranInfo(
                            rs.getInt("id_pembayaran"),
                            rs.getInt("id_transaksi"),
                            rs.getString("metode_pembayaran"),
                            rs.getDouble("jumlah_bayar"),
                            rs.getString("status_pembayaran"),
                            rs.getTimestamp("tanggal_pembayaran")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Gagal mencari pembayaran: "
                    + e.getMessage());
        }

        return null;
    }

    // Menampung hasil pencarian pembayaran
    public static class PembayaranInfo {

        private int idPembayaran;
        private int idTransaksi;
        private String metode;
        private double jumlahBayar;
        private String status;
        private Timestamp tanggal;

        public PembayaranInfo(int idPembayaran,
                              int idTransaksi,
                              String metode,
                              double jumlahBayar,
                              String status,
                              Timestamp tanggal) {

            this.idPembayaran = idPembayaran;
            this.idTransaksi = idTransaksi;
            this.metode = metode;
            this.jumlahBayar = jumlahBayar;
            this.status = status;
            this.tanggal = tanggal;
        }

        public int getIdPembayaran() {
            return idPembayaran;
        }

        public int getIdTransaksi() {
            return idTransaksi;
        }

        public String getMetode() {
            return metode;
        }

        public double getJumlahBayar() {
            return jumlahBayar;
        }

        public String getStatus() {
            return status;
        }

        public Timestamp getTanggal() {
            return tanggal;
        }
    }
}
