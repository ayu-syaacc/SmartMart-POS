package com.smartmart.model;

import java.time.LocalDate;

public class DetailPenerimaan {

    private int idDetail;
    private int idPenerimaan;
    private String kodeBarang;
    private int jumlah;
    private long hargaBeli;
    private LocalDate tglKedaluwarsa;

    public DetailPenerimaan() {
    }

    public DetailPenerimaan(String kodeBarang,
                            int jumlah,
                            long hargaBeli,
                            LocalDate tglKedaluwarsa) {
        this.kodeBarang = kodeBarang;
        this.jumlah = jumlah;
        this.hargaBeli = hargaBeli;
        this.tglKedaluwarsa = tglKedaluwarsa;
    }

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public int getIdPenerimaan() {
        return idPenerimaan;
    }

    public void setIdPenerimaan(int idPenerimaan) {
        this.idPenerimaan = idPenerimaan;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public long getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(long hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public LocalDate getTglKedaluwarsa() {
        return tglKedaluwarsa;
    }

    public void setTglKedaluwarsa(LocalDate tglKedaluwarsa) {
        this.tglKedaluwarsa = tglKedaluwarsa;
    }
}