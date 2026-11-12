
package com.smartmart.model;

public class Pegawai {

    private int idPegawai;
    private String nama;
    private String username;
    private String passwordHash;
    private String salt;
    private String peran;
    private boolean aktif;

    // Constructor untuk mengambil data pegawai dari database
    public Pegawai(int idPegawai, String nama, String username,
                   String passwordHash, String salt,
                   String peran, boolean aktif) {

        this.idPegawai = idPegawai;
        this.nama = nama;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.peran = peran;
        this.aktif = aktif;
    }

    // Constructor untuk membuat pegawai baru
    public Pegawai(String nama, String username,
                   String passwordHash, String salt,
                   String peran) {

        this.nama = nama;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.peran = peran;
        this.aktif = true;
    }

    // Getter dan Setter
    public int getIdPegawai() {
        return idPegawai;
    }

    public void setIdPegawai(int idPegawai) {
        this.idPegawai = idPegawai;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPeran() {
        return peran;
    }

    public void setPeran(String peran) {
        this.peran = peran;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }
}
