
package com.smartmart.model;

import java.time.LocalDateTime;

public abstract class Pembayaran {

    private int idPembayaran;
    private int idTransaksi;
    private double jumlahBayar;
    private String metodePembayaran;
    private String statusPembayaran;
    private LocalDateTime tanggalPembayaran;

    public Pembayaran() {
    }

    public Pembayaran(int idTransaksi,
                      double jumlahBayar,
                      String metodePembayaran) {

        this.idTransaksi = idTransaksi;
        this.jumlahBayar = jumlahBayar;
        this.metodePembayaran = metodePembayaran;
        this.statusPembayaran = "PENDING";
    }

    // Method abstrak untuk proses pembayaran
    public abstract boolean prosesPembayaran(double totalTagihan);

    public int getIdPembayaran() {
        return idPembayaran;
    }

    public void setIdPembayaran(int idPembayaran) {
        this.idPembayaran = idPembayaran;
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(int idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public double getJumlahBayar() {
        return jumlahBayar;
    }

    public void setJumlahBayar(double jumlahBayar) {
        this.jumlahBayar = jumlahBayar;
    }

    public String getMetodePembayaran() {
        return metodePembayaran;
    }

    public void setMetodePembayaran(String metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }

    public String getStatusPembayaran() {
        return statusPembayaran;
    }

    public void setStatusPembayaran(String statusPembayaran) {
        this.statusPembayaran = statusPembayaran;
    }

    public LocalDateTime getTanggalPembayaran() {
        return tanggalPembayaran;
    }

    public void setTanggalPembayaran(LocalDateTime tanggalPembayaran) {
        this.tanggalPembayaran = tanggalPembayaran;
    }
}
