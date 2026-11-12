
package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
                    return Optional.of(mapPegawai(result));
                }
            }
        }

        return Optional.empty();
    }

    // Menampilkan seluruh pegawai
    public List<Pegawai> findAll() throws SQLException {

        List<Pegawai> daftarPegawai = new ArrayList<>();

        String sql = """
                SELECT id_pegawai, nama, username,
                       password_hash, salt, peran, aktif
                FROM pegawai
                ORDER BY id_pegawai
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                daftarPegawai.add(mapPegawai(result));
            }
        }

        return daftarPegawai;
    }

    // Menambahkan pegawai baru
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

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    pegawai.setIdPegawai(keys.getInt(1));
                }
            }
        }
    }

    // Memperbarui data pegawai
    public boolean update(Pegawai pegawai) throws SQLException {

        String sql = """
                UPDATE pegawai
                SET nama = ?, username = ?, peran = ?
                WHERE id_pegawai = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pegawai.getNama());
            statement.setString(2, pegawai.getUsername());
            statement.setString(3, pegawai.getPeran());
            statement.setInt(4, pegawai.getIdPegawai());

            return statement.executeUpdate() > 0;
        }
    }

    // Mengaktifkan atau menonaktifkan akun pegawai
    public boolean updateStatus(int idPegawai, boolean aktif)
            throws SQLException {

        String sql = """
                UPDATE pegawai
                SET aktif = ?
                WHERE id_pegawai = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, aktif ? 1 : 0);
            statement.setInt(2, idPegawai);

            return statement.executeUpdate() > 0;
        }
    }

    // Mengubah hasil query menjadi objek Pegawai
    private Pegawai mapPegawai(ResultSet result) throws SQLException {

        return new Pegawai(
                result.getInt("id_pegawai"),
                result.getString("nama"),
                result.getString("username"),
                result.getString("password_hash"),
                result.getString("salt"),
                result.getString("peran"),
                result.getInt("aktif") == 1
        );
    }
}
