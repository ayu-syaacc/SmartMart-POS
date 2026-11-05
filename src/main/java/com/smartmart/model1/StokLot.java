
package com.smartmart.model1;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StokLot {

    private int id;
    private int produkId;
    private String kodeLot;
    private LocalDate tanggalMasuk;
    private LocalDate tanggalKedaluwarsa;
    private int jumlah;

    public StokLot(int id, int produkId, String kodeLot,
                   LocalDate tanggalMasuk,
                   LocalDate tanggalKedaluwarsa,
                   int jumlah) {
        this.id = id;
        this.produkId = produkId;
        this.kodeLot = kodeLot;
        this.tanggalMasuk = tanggalMasuk;
        this.tanggalKedaluwarsa = tanggalKedaluwarsa;
        this.jumlah = jumlah;
    }

    public int getId() {
        return id;
    }

    public int getProdukId() {
        return produkId;
    }

    public String getKodeLot() {
        return kodeLot;
    }

    public LocalDate getTanggalMasuk() {
        return tanggalMasuk;
    }

    public LocalDate getTanggalKedaluwarsa() {
        return tanggalKedaluwarsa;
    }

    public int getJumlah() {
        return jumlah;
    }

    public int sisaHari() {
        return (int) ChronoUnit.DAYS.between(
            LocalDate.now(), tanggalKedaluwarsa
        );
    }

    public String statusKedaluwarsa() {
        if (sisaHari() < 0) {
            return "KEDALUWARSA";
        } else if (sisaHari() == 0) {
            return "KEDALUWARSA HARI INI";
        } else if (sisaHari() <= 7) {
            return "SEGERA KEDALUWARSA";
        } else {
            return "AMAN";
        }
    }

    public boolean bolehDijual() {
        return jumlah > 0 &&
               !tanggalKedaluwarsa.isBefore(LocalDate.now());
    }

    public void kurangi(int jumlahKeluar) {
        if (jumlahKeluar <= 0) {
            throw new IllegalArgumentException(
                "Jumlah pengurangan harus lebih dari 0"
            );
        }

        if (!bolehDijual()) {
            throw new IllegalStateException(
                "Stok lot tidak boleh dijual"
            );
        }

        if (jumlahKeluar > jumlah) {
            throw new IllegalArgumentException(
                "Stok tidak mencukupi"
            );
        }

        jumlah -= jumlahKeluar;
    }

    public void tambah(int jumlahMasuk) {
        if (jumlahMasuk <= 0) {
            throw new IllegalArgumentException(
                "Jumlah penambahan harus lebih dari 0"
            );
        }

        jumlah += jumlahMasuk;
    }
}
