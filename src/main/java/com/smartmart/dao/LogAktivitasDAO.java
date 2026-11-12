
package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.LogAktivitas;

public class LogAktivitasDAO {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Menyimpan aktivitas pengguna
    public void insert(
            int idPegawai,
            String aksi,
            String keterangan
    ) throws SQLException {

        String sql = """
                INSERT INTO log_aktivitas
                (waktu, id_pegawai, aksi, keterangan)
                VALUES (?, ?, ?, ?)
                """;

        String waktu = LocalDateTime.now().format(FORMATTER);

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, waktu);
            statement.setInt(2, idPegawai);
            statement.setString(3, aksi);
            statement.setString(4, keterangan);

            statement.executeUpdate();
        }
    }

    // Mengambil seluruh riwayat aktivitas
    public List<LogAktivitas> findAll() throws SQLException {

        List<LogAktivitas> daftarAktivitas =
                new ArrayList<>();

        String sql = """
                SELECT
                    l.id_aktivitas,
                    l.waktu,
                    l.id_pegawai,
                    p.nama AS nama_pegawai,
                    l.aksi,
                    l.keterangan
                FROM log_aktivitas l
                INNER JOIN pegawai p
                    ON l.id_pegawai = p.id_pegawai
                ORDER BY l.id_aktivitas DESC
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                Statement statement =
                        connection.createStatement();

                ResultSet result = statement.executeQuery(sql)
        ) {

            while (result.next()) {

                LogAktivitas aktivitas = new LogAktivitas(
                        result.getInt("id_aktivitas"),
                        result.getString("waktu"),
                        result.getInt("id_pegawai"),
                        result.getString("nama_pegawai"),
                        result.getString("aksi"),
                        result.getString("keterangan")
                );

                daftarAktivitas.add(aktivitas);
            }
        }

        return daftarAktivitas;
    }
}
