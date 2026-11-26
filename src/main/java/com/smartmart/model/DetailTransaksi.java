
package com.smartmart.model;

public class DetailTransaksi {

    private int idDetail;
    private int idTransaksi;
    private int idProduk;
    private int idLot;
    private String namaProduk;
    private double harga;
    private double jumlah;
    private double diskon;
    private double subtotal;

    public DetailTransaksi() {
    }

    public DetailTransaksi(int idTransaksi,
                           int idProduk,
                           int idLot,
                           String namaProduk,
                           double harga,
                           double jumlah,
                           double diskon) {

        this.idTransaksi = idTransaksi;
        this.idProduk = idProduk;
        this.idLot = idLot;
        this.namaProduk = namaProduk;
        this.harga = harga;
        this.jumlah = jumlah;
        this.diskon = diskon;
        this.subtotal = (harga * jumlah) - diskon;
    }

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(int idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public int getIdProduk() {
        return idProduk;
    }

    public void setIdProduk(int idProduk) {
        this.idProduk = idProduk;
    }

    public int getIdLot() {
        return idLot;
    }

    public void setIdLot(int idLot) {
        this.idLot = idLot;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public double getJumlah() {
        return jumlah;
    }

    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }

    public double getDiskon() {
        return diskon;
    }

    public void setDiskon(double diskon) {
        this.diskon = diskon;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}
