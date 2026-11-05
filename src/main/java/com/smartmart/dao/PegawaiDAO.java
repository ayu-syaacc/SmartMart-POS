
package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.Pegawai;

public class PegawaiDAO {

    // Mencari pegawai berdasarkan username
    public Optional<Pegawai> findByUsername(String username)
            throws SQLException {

        String sql = """
                SELECT id_pegawai, nama, username,
                       password_hash, salt, peran, aktif
                FROM pegawai
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    Pegawai pegawai = new Pegawai(
                            result.getInt("id_pegawai"),
                            result.getString("nama"),
                            result.getString("username"),
                            result.getString("password_hash"),
                            result.getString("salt"),
                            result.getString("peran"),
                            result.getInt("aktif") == 1
                    );

                    return Optional.of(pegawai);
                }
            }
        }

        return Optional.empty();
    }

    // Menambahkan pegawai baru ke database
    public void insert(Pegawai pegawai) throws SQLException {

        String sql = """
                INSERT INTO pegawai
                (nama, username, password_hash, salt, peran, aktif)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, pegawai.getNama());
            statement.setString(2, pegawai.getUsername());
            statement.setString(3, pegawai.getPasswordHash());
            statement.setString(4, pegawai.getSalt());
            statement.setString(5, pegawai.getPeran());
            statement.setInt(6, pegawai.isAktif() ? 1 : 0);

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pegawai.setIdPegawai(generatedKeys.getInt(1));
                }
            }
        }
    }
}
