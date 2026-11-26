
package com.smartmart.db;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import com.smartmart.dao.PegawaiDAO;
import com.smartmart.model.Pegawai;
import com.smartmart.util.PasswordUtil;

public class PegawaiSeeder {

    private static final PegawaiDAO pegawaiDAO = new PegawaiDAO();

    public static void seed()
            throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        seedAdmin();
        seedKasir();
    }

    private static void seedAdmin()
            throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        if (pegawaiDAO.findByUsername("admin").isEmpty()) {

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(
                    "Admin123!", salt
            );

            Pegawai admin = new Pegawai(
                    "Administrator",
                    "admin",
                    hash,
                    salt,
                    "ADMIN"
            );

            pegawaiDAO.insert(admin);

            System.out.println("Akun ADMIN berhasil dibuat.");
        }
    }

    private static void seedKasir()
            throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        if (pegawaiDAO.findByUsername("kasir").isEmpty()) {

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(
                    "Kasir123!", salt
            );

            Pegawai kasir = new Pegawai(
                    "Kasir Demo",
                    "kasir",
                    hash,
                    salt,
                    "KASIR"
            );

            pegawaiDAO.insert(kasir);

            System.out.println("Akun KASIR berhasil dibuat.");
        }
    }
}
