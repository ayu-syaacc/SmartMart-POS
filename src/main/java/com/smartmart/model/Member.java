
package com.smartmart.model;

import java.time.LocalDate;

public class Member {

    private int idMember;
    private String kodeMember;
    private String namaMember;
    private String nomorTelepon;
    private String alamat;
    private int poin;
    private LocalDate tanggalDaftar;
    private String status;

    public Member() {
    }

    public Member(String kodeMember,
                  String namaMember,
                  String nomorTelepon,
                  String alamat,
                  int poin,
                  LocalDate tanggalDaftar,
                  String status) {

        this.kodeMember = kodeMember;
        this.namaMember = namaMember;
        this.nomorTelepon = nomorTelepon;
        this.alamat = alamat;
        this.poin = poin;
        this.tanggalDaftar = tanggalDaftar;
        this.status = status;
    }

    public int getIdMember() {
        return idMember;
    }

    public void setIdMember(int idMember) {
        this.idMember = idMember;
    }

    public String getKodeMember() {
        return kodeMember;
    }

    public void setKodeMember(String kodeMember) {
        this.kodeMember = kodeMember;
    }

    public String getNamaMember() {
        return namaMember;
    }

    public void setNamaMember(String namaMember) {
        this.namaMember = namaMember;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public int getPoin() {
        return poin;
    }

    public void setPoin(int poin) {
        this.poin = poin;
    }

    public LocalDate getTanggalDaftar() {
        return tanggalDaftar;
    }

    public void setTanggalDaftar(LocalDate tanggalDaftar) {
        this.tanggalDaftar = tanggalDaftar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Menambahkan poin member
    public void tambahPoin(int jumlah) {
        if (jumlah > 0) {
            this.poin += jumlah;
        }
    }

    // Mengurangi poin member
    public boolean kurangiPoin(int jumlah) {
        if (jumlah > 0 && this.poin >= jumlah) {
            this.poin -= jumlah;
            return true;
        }

        return false;
    }

    // Menghitung nilai rupiah dari poin
    public double hitungNilaiPoin() {
        return (poin / 100) * 5000.0;
    }

}
