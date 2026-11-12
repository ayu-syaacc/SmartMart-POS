
package com.smartmart.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.smartmart.dao.PegawaiDAO;
import com.smartmart.model.Pegawai;
import com.smartmart.util.PasswordUtil;

public class PegawaiService {

    private final PegawaiDAO pegawaiDAO;

    public PegawaiService() {
        this.pegawaiDAO = new PegawaiDAO();
    }

    // Mengambil seluruh data pegawai
    public List<Pegawai> getAllPegawai() throws SQLException {
        return pegawaiDAO.findAll();
    }

    // Menambahkan pegawai baru
    public void tambahPegawai(
            String nama,
            String username,
            String password,
            String peran
    ) throws SQLException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        validasiIdentitas(nama, username, peran);
        validasiPassword(password);

        String usernameBersih = username.trim();

        if (pegawaiDAO.findByUsername(usernameBersih).isPresent()) {
            throw new IllegalArgumentException(
                    "Username sudah digunakan."
            );
        }

        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);

        Pegawai pegawai = new Pegawai(
                nama.trim(),
                usernameBersih,
                passwordHash,
                salt,
                peran.toUpperCase()
        );

        pegawaiDAO.insert(pegawai);
    }

    // Memperbarui data pegawai
    public void ubahPegawai(
            int idPegawai,
            String nama,
            String username,
            String peran
    ) throws SQLException {

        if (idPegawai <= 0) {
            throw new IllegalArgumentException(
                    "ID pegawai tidak valid."
            );
        }

        validasiIdentitas(nama, username, peran);

        String usernameBersih = username.trim();

        Optional<Pegawai> pegawaiLama =
                pegawaiDAO.findByUsername(usernameBersih);

        if (pegawaiLama.isPresent()
                && pegawaiLama.get().getIdPegawai() != idPegawai) {

            throw new IllegalArgumentException(
                    "Username sudah digunakan pegawai lain."
            );
        }

        Pegawai pegawai = new Pegawai(
                idPegawai,
                nama.trim(),
                usernameBersih,
                "",
                "",
                peran.toUpperCase(),
                true
        );

        boolean berhasil = pegawaiDAO.update(pegawai);

        if (!berhasil) {
            throw new IllegalArgumentException(
                    "Data pegawai tidak ditemukan."
            );
        }
    }

    // Mengubah status akun pegawai
    public void ubahStatus(int idPegawai, boolean aktif)
            throws SQLException {

        if (idPegawai <= 0) {
            throw new IllegalArgumentException(
                    "ID pegawai tidak valid."
            );
        }

        boolean berhasil = pegawaiDAO.updateStatus(idPegawai, aktif);

        if (!berhasil) {
            throw new IllegalArgumentException(
                    "Data pegawai tidak ditemukan."
            );
        }
    }

    // Validasi nama, username, dan peran
    private void validasiIdentitas(
            String nama,
            String username,
            String peran
    ) {

        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException(
                    "Nama pegawai wajib diisi."
            );
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username wajib diisi."
            );
        }

        if (!"ADMIN".equalsIgnoreCase(peran)
                && !"KASIR".equalsIgnoreCase(peran)) {
            throw new IllegalArgumentException(
                    "Role harus ADMIN atau KASIR."
            );
        }
    }

    // Validasi password
    private void validasiPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password wajib diisi."
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password minimal 8 karakter."
            );
        }
    }
}
