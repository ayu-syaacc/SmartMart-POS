
package com.smartmart.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize()
            throws SQLException, IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        try (Connection connection = DatabaseConnection.getConnection()) {

            executeScript(connection, "/db/schema.sql");
            executeScript(connection, "/db/seed.sql");

        }

        // Membuat akun awal setelah tabel tersedia
        PegawaiSeeder.seed();

        System.out.println("Database berhasil diinisialisasi!");
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
