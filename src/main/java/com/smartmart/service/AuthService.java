
package com.smartmart.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.Optional;

import com.smartmart.dao.PegawaiDAO;
import com.smartmart.model.Pegawai;
import com.smartmart.util.PasswordUtil;

public class AuthService {

    private final PegawaiDAO pegawaiDAO;

    public AuthService() {
        this.pegawaiDAO = new PegawaiDAO();
    }

    public Optional<Pegawai> login(String username, String password)
            throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        // Memeriksa input kosong
        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        // Mencari pegawai berdasarkan username
        Optional<Pegawai> result =
                pegawaiDAO.findByUsername(username.trim());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Pegawai pegawai = result.get();

        // Memastikan akun masih aktif
        if (!pegawai.isAktif()) {
            return Optional.empty();
        }

        // Memeriksa password menggunakan hash dan salt
        boolean passwordValid = PasswordUtil.verifyPassword(
                password,
                pegawai.getSalt(),
                pegawai.getPasswordHash()
        );

        if (!passwordValid) {
            return Optional.empty();
        }

        // Login berhasil
        return Optional.of(pegawai);
    }
}
