
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

    /**
     * Memvalidasi login pegawai berdasarkan username dan password.
     */
    public Optional<Pegawai> login(String username, String password)
            throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        // Memeriksa input kosong.
        if (username == null || username.isBlank()
                || password == null || password.isEmpty()) {
            return Optional.empty();
        }

        // Mencari pegawai berdasarkan username.
        Optional<Pegawai> result =
                pegawaiDAO.findByUsername(username.trim());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Pegawai pegawai = result.get();

        // Memastikan akun masih aktif.
        if (!pegawai.isAktif()) {
            return Optional.empty();
        }

        // Memeriksa password menggunakan hash dan salt.
        boolean passwordValid = PasswordUtil.verifyPassword(
                password,
                pegawai.getSalt(),
                pegawai.getPasswordHash()
        );

        if (!passwordValid) {
            return Optional.empty();
        }

        // Login berhasil.
        return Optional.of(pegawai);
    }

    /**
     * Memvalidasi otorisasi khusus Admin.
     * Hanya pegawai aktif dengan role ADMIN
     * dan password yang benar yang dapat memperoleh otorisasi.
     */
    public Optional<Pegawai> authorizeAdmin(
            String username,
            String password)
            throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        // Memvalidasi username dan password terlebih dahulu.
        Optional<Pegawai> result = login(username, password);

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Pegawai pegawai = result.get();

        // Memastikan pegawai memiliki role ADMIN.
        if (!"ADMIN".equalsIgnoreCase(pegawai.getPeran())) {
            return Optional.empty();
        }

        // Otorisasi Admin berhasil.
        return Optional.of(pegawai);
    }
}
