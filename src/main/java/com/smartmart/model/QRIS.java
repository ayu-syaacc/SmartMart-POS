
package com.smartmart.model;

import java.time.LocalDateTime;

public class QRIS extends Pembayaran {

    private String nomorReferensi;
    private boolean terkonfirmasi;

    public QRIS() {
        super();
        setMetodePembayaran("QRIS");
    }

    public QRIS(int idTransaksi, double jumlahBayar) {
        super(idTransaksi, jumlahBayar, "QRIS");
        this.terkonfirmasi = false;
    }

    @Override
    public boolean prosesPembayaran(double totalTagihan) {

        if (totalTagihan <= 0
                || getJumlahBayar() != totalTagihan) {
            setStatusPembayaran("PENDING");
            return false;
        }

        if (nomorReferensi == null
                || nomorReferensi.trim().isEmpty()) {
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

    public String getNomorReferensi() {
        return nomorReferensi;
    }

    public void setNomorReferensi(String nomorReferensi) {
        this.nomorReferensi = nomorReferensi;
    }

    public boolean isTerkonfirmasi() {
        return terkonfirmasi;
    }

    public void setTerkonfirmasi(boolean terkonfirmasi) {
        this.terkonfirmasi = terkonfirmasi;
    }
}
