
package com.smartmart.model;

import java.time.LocalDateTime;

public class Transaksi {

    private int idTransaksi;
    private String nomorTransaksi;
    private LocalDateTime tanggalTransaksi;
    private int idPegawai;
    private Integer idMember;
    private double subtotal;
    private double diskon;
    private double total;
    private String status;

    public Transaksi() {
    }

    public Transaksi(String nomorTransaksi,
                     LocalDateTime tanggalTransaksi,
                     int idPegawai,
                     Integer idMember,
                     double subtotal,
                     double diskon,
                     double total,
                     String status) {

        this.nomorTransaksi = nomorTransaksi;
        this.tanggalTransaksi = tanggalTransaksi;
        this.idPegawai = idPegawai;
        this.idMember = idMember;
        this.subtotal = subtotal;
        this.diskon = diskon;
        this.total = total;
        this.status = status;
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(int idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public String getNomorTransaksi() {
        return nomorTransaksi;
    }

    public void setNomorTransaksi(String nomorTransaksi) {
        this.nomorTransaksi = nomorTransaksi;
    }

    public LocalDateTime getTanggalTransaksi() {
        return tanggalTransaksi;
    }

    public void setTanggalTransaksi(LocalDateTime tanggalTransaksi) {
        this.tanggalTransaksi = tanggalTransaksi;
    }

    public int getIdPegawai() {
        return idPegawai;
    }

    public void setIdPegawai(int idPegawai) {
        this.idPegawai = idPegawai;
    }

    public Integer getIdMember() {
        return idMember;
    }

    public void setIdMember(Integer idMember) {
        this.idMember = idMember;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiskon() {
        return diskon;
    }

    public void setDiskon(double diskon) {
        this.diskon = diskon;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
