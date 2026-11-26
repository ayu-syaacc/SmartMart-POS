
package com.smartmart.model;

import java.time.LocalDateTime;

public class Debit extends Pembayaran {

    private String nomorKartu;
    private String namaPemegangKartu;
    private boolean terkonfirmasi;

    public Debit() {
        super();
        setMetodePembayaran("DEBIT");
    }

    public Debit(int idTransaksi,
                 double jumlahBayar,
                 String nomorKartu,
                 String namaPemegangKartu) {

        super(idTransaksi, jumlahBayar, "DEBIT");

        this.nomorKartu = nomorKartu;
        this.namaPemegangKartu = namaPemegangKartu;
        this.terkonfirmasi = false;
    }

    @Override
    public boolean prosesPembayaran(double totalTagihan) {

        if (totalTagihan <= 0
                || getJumlahBayar() != totalTagihan) {
            setStatusPembayaran("PENDING");
            return false;
        }

        if (nomorKartu == null
                || !nomorKartu.matches("\\d{16}")) {
            setStatusPembayaran("PENDING");
            return false;
        }

        if (namaPemegangKartu == null
                || namaPemegangKartu.trim().isEmpty()) {
            setStatusPembayaran("PENDING");
            return false;
        }

        if (!terkonfirmasi) {
            setStatusPembayaran("PENDING");
            return false;
        }

        setStatusPembayaran("PAID");
        setTanggalPembayaran(LocalDateTime.now());

        return true;
    }

    public String getNomorKartu() {
        return nomorKartu;
    }

    public void setNomorKartu(String nomorKartu) {
        this.nomorKartu = nomorKartu;
    }

    public String getNamaPemegangKartu() {
        return namaPemegangKartu;
    }

    public void setNamaPemegangKartu(
            String namaPemegangKartu) {
        this.namaPemegangKartu = namaPemegangKartu;
    }

    public boolean isTerkonfirmasi() {
        return terkonfirmasi;
    }

    public void setTerkonfirmasi(boolean terkonfirmasi) {
        this.terkonfirmasi = terkonfirmasi;
    }
}
