package com.smartmart.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StokLot {

    private int idLot;
    private String kodeBarang;
    private Integer idDetail;
    private LocalDate tglKedaluwarsa;
    private int jumlahSisa;

    public StokLot() {
    }

    public StokLot(int idLot, String kodeBarang, Integer idDetail,
                   LocalDate tglKedaluwarsa, int jumlahSisa) {
        this.idLot = idLot;
        this.kodeBarang = kodeBarang;
        this.idDetail = idDetail;
        this.tglKedaluwarsa = tglKedaluwarsa;
        this.jumlahSisa = jumlahSisa;
    }

    public StokLot(String kodeBarang, Integer idDetail,
                   LocalDate tglKedaluwarsa, int jumlahSisa) {
        this(0, kodeBarang, idDetail, tglKedaluwarsa, jumlahSisa);
    }

    /**
     * Menghitung jumlah hari dari hari ini sampai tanggal kedaluwarsa.
     * Nilai negatif berarti sudah kedaluwarsa.
     * Jika tidak memiliki tanggal kedaluwarsa, dikembalikan Integer.MAX_VALUE.
     */
    public long sisaHari() {
        if (tglKedaluwarsa == null) {
            return Integer.MAX_VALUE;
        }

        return ChronoUnit.DAYS.between(
                LocalDate.now(),
                tglKedaluwarsa
        );
    }

    /**
     * Mengembalikan status kedaluwarsa lot.
     */
    public String statusKedaluwarsa() {
        if (tglKedaluwarsa == null) {
            return "TIDAK ADA TANGGAL";
        }

        long hari = sisaHari();

        if (hari < 0) {
            return "KEDALUWARSA";
        }

        if (hari == 0) {
            return "KEDALUWARSA HARI INI";
        }

        return "AMAN";
    }

    /**
     * Menentukan apakah lot masih boleh dijual.
     * Lot harus memiliki stok dan belum melewati tanggal kedaluwarsa.
     */
    public boolean bolehDijual() {
        return jumlahSisa > 0
                && (tglKedaluwarsa == null || sisaHari() >= 0);
    }

    /**
     * Mengurangi jumlah stok lot.
     */
    public void kurangi(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException(
                    "Jumlah yang dikurangi harus lebih dari 0."
            );
        }

        if (jumlah > jumlahSisa) {
            throw new IllegalArgumentException(
                    "Jumlah pengurangan melebihi stok lot."
            );
        }

        jumlahSisa -= jumlah;
    }

    /**
     * Menambah jumlah stok lot.
     */
    public void tambah(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException(
                    "Jumlah yang ditambahkan harus lebih dari 0."
            );
        }

        jumlahSisa += jumlah;
    }

    public int getIdLot() {
        return idLot;
    }

    public void setIdLot(int idLot) {
        this.idLot = idLot;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public Integer getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(Integer idDetail) {
        this.idDetail = idDetail;
    }

    public LocalDate getTglKedaluwarsa() {
        return tglKedaluwarsa;
    }

    public void setTglKedaluwarsa(LocalDate tglKedaluwarsa) {
        this.tglKedaluwarsa = tglKedaluwarsa;
    }

    public int getJumlahSisa() {
        return jumlahSisa;
    }

    public void setJumlahSisa(int jumlahSisa) {
        if (jumlahSisa < 0) {
            throw new IllegalArgumentException(
                    "Jumlah sisa tidak boleh negatif."
            );
        }

        this.jumlahSisa = jumlahSisa;
    }

    @Override
    public String toString() {
        return "StokLot{" +
                "idLot=" + idLot +
                ", kodeBarang='" + kodeBarang + '\'' +
                ", idDetail=" + idDetail +
                ", tglKedaluwarsa=" + tglKedaluwarsa +
                ", jumlahSisa=" + jumlahSisa +
                '}';
    }
}