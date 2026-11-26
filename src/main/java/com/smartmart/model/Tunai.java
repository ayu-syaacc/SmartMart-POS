
package com.smartmart.model;

import java.time.LocalDateTime;

public class Tunai extends Pembayaran {

    private double uangDiterima;
    private double uangKembalian;

    public Tunai() {
        super();
        setMetodePembayaran("TUNAI");
    }

    public Tunai(int idTransaksi, double uangDiterima) {
        super(idTransaksi, uangDiterima, "TUNAI");
        this.uangDiterima = uangDiterima;
        this.uangKembalian = 0;
    }

    @Override
    public boolean prosesPembayaran(double totalTagihan) {

        if (totalTagihan <= 0 || uangDiterima < totalTagihan) {
            setStatusPembayaran("PENDING");
            uangKembalian = 0;
            return false;
        }

        uangKembalian = uangDiterima - totalTagihan;

        setJumlahBayar(uangDiterima);
        setStatusPembayaran("PAID");
        setTanggalPembayaran(LocalDateTime.now());

        return true;
    }

    public double getUangDiterima() {
        return uangDiterima;
    }

    public void setUangDiterima(double uangDiterima) {
        this.uangDiterima = uangDiterima;
        setJumlahBayar(uangDiterima);
    }

    public double getUangKembalian() {
        return uangKembalian;
    }
}
