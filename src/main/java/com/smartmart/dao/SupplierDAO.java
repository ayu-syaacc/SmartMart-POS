package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.Supplier;

public class SupplierDAO {

    public List<Supplier> ambilSemuaAktif() throws SQLException {

        String sql = """
                SELECT id_supplier, kode_supplier, nama,
                       alamat, telepon, aktif
                FROM supplier
                WHERE aktif = 1
                ORDER BY nama
                """;

        List<Supplier> hasil = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hasil.add(petakan(rs));
            }
        }

        return hasil;
    }

    public List<Supplier> ambilSemua() throws SQLException {

        String sql = """
                SELECT id_supplier, kode_supplier, nama,
                       alamat, telepon, aktif
                FROM supplier
                ORDER BY nama
                """;

        List<Supplier> hasil = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hasil.add(petakan(rs));
            }
        }

        return hasil;
    }

    public Supplier cariById(int idSupplier) throws SQLException {

        String sql = """
                SELECT id_supplier, kode_supplier, nama,
                       alamat, telepon, aktif
                FROM supplier
                WHERE id_supplier = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSupplier);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public Supplier cariKode(String kodeSupplier) throws SQLException {

        String sql = """
                SELECT id_supplier, kode_supplier, nama,
                       alamat, telepon, aktif
                FROM supplier
                WHERE kode_supplier = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kodeSupplier);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakan(rs) : null;
            }
        }
    }

    public int tambah(Supplier supplier) throws SQLException {

        String sql = """
                INSERT INTO supplier
                    (kode_supplier, nama, alamat, telepon, aktif)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, supplier.getKodeSupplier());
            ps.setString(2, supplier.getNama());
            ps.setString(3, supplier.getAlamat());
            ps.setString(4, supplier.getTelepon());
            ps.setInt(5, supplier.isAktif() ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    supplier.setIdSupplier(id);
                    return id;
                }
            }
        }

        return 0;
    }

    private Supplier petakan(ResultSet rs) throws SQLException {

        return new Supplier(
                rs.getInt("id_supplier"),
                rs.getString("kode_supplier"),
                rs.getString("nama"),
                rs.getString("alamat"),
                rs.getString("telepon"),
                rs.getInt("aktif") == 1
        );
    }
}