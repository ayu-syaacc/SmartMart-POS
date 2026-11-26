
package com.smartmart.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize()
            throws SQLException, IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        try (Connection connection = DatabaseConnection.getConnection()) {

            executeScript(connection, "/db/schema.sql");
            migrasiProduk(connection);
            executeScript(connection, "/db/seed.sql");

        }

        // Membuat akun awal setelah tabel tersedia
        PegawaiSeeder.seed();

        System.out.println("Database berhasil diinisialisasi!");
    }

    // CREATE TABLE IF NOT EXISTS tidak menambah kolom ke tabel yang sudah ada,
    // jadi database lama (smartmart.db) perlu ditambah kolom kategori dan aktif.
    private static void migrasiProduk(Connection connection) throws SQLException {

        if (!kolomAda(connection, "produk", "kategori")) {
            jalankan(connection,
                    "ALTER TABLE produk ADD COLUMN kategori TEXT NOT NULL DEFAULT 'Lainnya'");
        }

        if (!kolomAda(connection, "produk", "aktif")) {
            jalankan(connection,
                    "ALTER TABLE produk ADD COLUMN aktif INTEGER NOT NULL DEFAULT 1 CHECK (aktif IN (0, 1))");
        }
    }

    private static boolean kolomAda(
            Connection connection,
            String tabel,
            String kolom
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tabel + ")")) {

            while (rs.next()) {
                if (kolom.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void jalankan(Connection connection, String sql) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void executeScript(
            Connection connection,
            String filePath
    ) throws IOException, SQLException {

        try (InputStream input = DatabaseInitializer.class
                .getResourceAsStream(filePath)) {

            if (input == null) {
                throw new IOException(
                        "File tidak ditemukan: " + filePath
                );
            }

            String sql = new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String[] statements = sql.split(";");

            try (Statement statement = connection.createStatement()) {

                for (String query : statements) {

                    if (!query.trim().isEmpty()) {
                        statement.execute(query);
                    }
                }
            }
        }
    }
}
