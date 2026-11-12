package com.smartmart.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PenerimaanBarang {

    private int idPenerimaan;
    private String noPenerimaan;
    private LocalDate tglTerima;
    private Integer idSupplier;
    private int idPegawai;
    private long total;

    private List<DetailPenerimaan> detail = new ArrayList<>();

    public PenerimaanBarang() {
    }

    public PenerimaanBarang(String noPenerimaan,
                            LocalDate tglTerima,
                            Integer idSupplier,
                            int idPegawai) {
        this.noPenerimaan = noPenerimaan;
        this.tglTerima = tglTerima;
        this.idSupplier = idSupplier;
        this.idPegawai = idPegawai;
    }

    public int getIdPenerimaan() {
        return idPenerimaan;
    }

    public void setIdPenerimaan(int idPenerimaan) {
        this.idPenerimaan = idPenerimaan;
    }

    public String getNoPenerimaan() {
        return noPenerimaan;
    }

    public void setNoPenerimaan(String noPenerimaan) {
        this.noPenerimaan = noPenerimaan;
    }

    public LocalDate getTglTerima() {
        return tglTerima;
    }

    public void setTglTerima(LocalDate tglTerima) {
        this.tglTerima = tglTerima;
    }

    public Integer getIdSupplier() {
        return idSupplier;
    }

    public void setIdSupplier(Integer idSupplier) {
        this.idSupplier = idSupplier;
    }

    public int getIdPegawai() {
        return idPegawai;
    }

    public void setIdPegawai(int idPegawai) {
        this.idPegawai = idPegawai;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<DetailPenerimaan> getDetail() {
        return detail;
    }

    public void setDetail(List<DetailPenerimaan> detail) {
        this.detail = detail;
    }

    public void tambahDetail(DetailPenerimaan item) {
        detail.add(item);
    }

    public void hitungTotal() {
        total = 0;

        for (DetailPenerimaan item : detail) {
            total += item.getJumlah() * item.getHargaBeli();
        }
    }
}