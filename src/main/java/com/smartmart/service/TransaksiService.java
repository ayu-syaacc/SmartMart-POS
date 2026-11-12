
package com.smartmart.service;

import com.smartmart.dao.TransaksiDao;
import com.smartmart.model.Transaksi;

public class TransaksiService {

    private final TransaksiDao transaksiDAO;

    public TransaksiService(TransaksiDao transaksiDAO) {
        this.transaksiDAO = transaksiDAO;
    }

    public boolean simpanTransaksiSetelahDibayar(
            Transaksi transaksi) {

        if (transaksi == null) {
            System.out.println("Data transaksi tidak tersedia.");
            return false;
        }

        // Memeriksa status pembayaran
        if (!"PAID".equalsIgnoreCase(transaksi.getStatus())) {
            System.out.println(
                    "Transaksi belum dibayar. Data tidak disimpan.");
            return false;
        }

        // Memeriksa total pembayaran
        if (transaksi.getTotal() <= 0) {
            System.out.println("Total transaksi tidak valid.");
            return false;
        }

        // Menyimpan transaksi ke database
        boolean berhasil = transaksiDAO.simpan(transaksi);

        if (berhasil) {
            System.out.println(
                    "Transaksi berhasil disimpan.");
        } else {
            System.out.println(
                    "Transaksi gagal disimpan.");
        }

        return berhasil;
    }

    // Mencari transaksi berdasarkan ID
    public Transaksi cariTransaksi(int idTransaksi) {

        if (idTransaksi <= 0) {
            return null;
        }

        return transaksiDAO.cariById(idTransaksi);
    }

    // Mengambil seluruh data transaksi
    public java.util.List<Transaksi> ambilSemuaTransaksi() {

        return transaksiDAO.ambilSemua();
    }

}
