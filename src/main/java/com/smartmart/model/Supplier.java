package com.smartmart.model;

public class Supplier {

    private int idSupplier;
    private String kodeSupplier;
    private String nama;
    private String alamat;
    private String telepon;
    private boolean aktif;

    public Supplier() {
    }

    public Supplier(int idSupplier, String kodeSupplier, String nama,
                    String alamat, String telepon, boolean aktif) {
        this.idSupplier = idSupplier;
        this.kodeSupplier = kodeSupplier;
        this.nama = nama;
        this.alamat = alamat;
        this.telepon = telepon;
        this.aktif = aktif;
    }

    public Supplier(String kodeSupplier, String nama,
                    String alamat, String telepon, boolean aktif) {
        this.kodeSupplier = kodeSupplier;
        this.nama = nama;
        this.alamat = alamat;
        this.telepon = telepon;
        this.aktif = aktif;
    }

    public int getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(int idSupplier) {
        this.idSupplier = idSupplier;
    }

    public String getKodeSupplier() {
        return kodeSupplier;
    }

    public void setKodeSupplier(String kodeSupplier) {
        this.kodeSupplier = kodeSupplier;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getTelepon() {
        return telepon;
    }

    public void setTelepon(String telepon) {
        this.telepon = telepon;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

    @Override
    public String toString() {
        return kodeSupplier + " - " + nama;
    }
}