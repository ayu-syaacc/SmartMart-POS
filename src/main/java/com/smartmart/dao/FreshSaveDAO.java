package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.FreshSave;

public class FreshSaveDAO {

    public List<FreshSave> ambilSemua() throws SQLException {

        String sql = """
                SELECT id_aturan, batas_hari, persen_diskon
                FROM aturan_freshsave
                ORDER BY batas_hari ASC
                """;

        List<FreshSave> hasil = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hasil.add(petakan(rs));
            }
        }

        return hasil;
    }

    public FreshSave cariById(int idAturan) throws SQLException {

        String sql = """
                SELECT id_aturan, batas_hari, persen_diskon
                FROM aturan_freshsave
                WHERE id_aturan = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAturan);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return petakan(rs);
                }
            }
        }

        return null;
    }

    public int tambah(FreshSave freshSave) throws SQLException {

        String sql = """
                INSERT INTO aturan_freshsave
                    (batas_hari, persen_diskon)
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, freshSave.getBatasHari());
            ps.setInt(2, freshSave.getPersenDiskon());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    freshSave.setIdAturan(id);
                    return id;
                }
            }
        }

        return 0;
    }

    public void ubah(FreshSave freshSave) throws SQLException {

        String sql = """
                UPDATE aturan_freshsave
                SET batas_hari = ?,
                    persen_diskon = ?
                WHERE id_aturan = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, freshSave.getBatasHari());
            ps.setInt(2, freshSave.getPersenDiskon());
            ps.setInt(3, freshSave.getIdAturan());

            ps.executeUpdate();
        }
    }

    public void hapus(int idAturan) throws SQLException {

        String sql = """
                DELETE FROM aturan_freshsave
                WHERE id_aturan = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAturan);
            ps.executeUpdate();
        }
    }

    private FreshSave petakan(ResultSet rs) throws SQLException {

        return new FreshSave(
                rs.getInt("id_aturan"),
                rs.getInt("batas_hari"),
                rs.getInt("persen_diskon")
        );
    }
}