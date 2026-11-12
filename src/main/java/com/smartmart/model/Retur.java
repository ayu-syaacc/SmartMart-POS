package com.smartmart.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Retur {

    public static final int BATAS_HARI = 3;
    public static final int RUPIAH_PER_POIN = 10000;
    public static final String STATUS_SELESAI = "SELESAI";

    private String idTransaksi;
    private LocalDateTime waktuTransaksi;
    private String statusTransaksi;
    private Long idMember;
    private String namaPelanggan = "Umum";
    private String metodeBayar = "Tunai";
    private int totalTransaksi;
    private String namaKasirAsal;

    private long idRetur;
    private int idKasir;
    private Integer idAdmin;
    private LocalDateTime waktu;
    private String alasan;
    private boolean belumDigunakan = true;
    private boolean kemasanUtuh = true;
    private final List<DetailRetur> detail = new ArrayList<DetailRetur>();

    public Retur() { }

    public Retur(String idTransaksi) { this.idTransaksi = idTransaksi; }

    public long hariKe(LocalDate hariIni) {
        if (waktuTransaksi == null) return 0;
        return ChronoUnit.DAYS.between(waktuTransaksi.toLocalDate(), hariIni);
    }

    public List<DetailRetur> barangDipilih() {
        List<DetailRetur> r = new ArrayList<DetailRetur>();
        for (DetailRetur d : detail) {
            if (d.getJumlahRetur() > 0) r.add(d);
        }
        return r;
    }

    public List<String> validasi(LocalDate hariIni) {
        List<String> pesan = validasiTransaksi(hariIni);
        if (!pesan.isEmpty()) return pesan;

        List<DetailRetur> dipilih = barangDipilih();
        if (dipilih.isEmpty()) {
            pesan.add("Pilih minimal satu barang yang diretur.");
        }
        for (DetailRetur d : dipilih) {
            String tolak = d.alasanTolakBarang();
            if (tolak == null) tolak = d.alasanTolakJumlah();
            if (tolak != null) pesan.add(tolak);
        }
        if (!belumDigunakan || !kemasanUtuh) {
            pesan.add("Barang harus belum digunakan dan kemasan utuh (BR-34).");
        }
        if (alasan == null || alasan.trim().isEmpty()) {
            pesan.add("Alasan retur wajib dipilih.");
        }
        return pesan;
    }

    public List<String> validasiTransaksi(LocalDate hariIni) {
        List<String> pesan = new ArrayList<String>();
        if (idTransaksi == null || idTransaksi.trim().isEmpty() || waktuTransaksi == null) {
            pesan.add("Masukkan ID transaksi asli untuk melanjutkan.");
            return pesan;
        }
        if ("DIBATALKAN".equals(statusTransaksi)) {
            pesan.add("Transaksi sudah di-void, tidak dapat diretur.");
            return pesan;
        }
        if (!STATUS_SELESAI.equals(statusTransaksi)) {
            pesan.add("Transaksi belum selesai dibayar, tidak dapat diretur.");
            return pesan;
        }
        if (hariKe(hariIni) > BATAS_HARI) {
            pesan.add("Lewat " + BATAS_HARI + " hari: pembelian " + tanggal(waktuTransaksi.toLocalDate())
                    + ", batas retur " + BATAS_HARI + " hari sudah lewat.");
        }
        return pesan;
    }

    public int hitungRefund() {
        int total = 0;
        for (DetailRetur d : detail) total += d.hitungRefund();
        return total;
    }

    public int hitungPoinDikurangi() {
        return idMember == null ? 0 : hitungRefund() / RUPIAH_PER_POIN;
    }

    private static final String[] BULAN = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

    public static String tanggal(LocalDate d) {
        return String.format("%02d %s %d", d.getDayOfMonth(), BULAN[d.getMonthValue() - 1], d.getYear());
    }

    public String getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(String v) { idTransaksi = v; }
    public LocalDateTime getWaktuTransaksi() { return waktuTransaksi; }
    public void setWaktuTransaksi(LocalDateTime v) { waktuTransaksi = v; }
    public String getStatusTransaksi() { return statusTransaksi; }
    public void setStatusTransaksi(String v) { statusTransaksi = v; }
    public Long getIdMember() { return idMember; }
    public void setIdMember(Long v) { idMember = v; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String v) { namaPelanggan = v; }
    public String getMetodeBayar() { return metodeBayar; }
    public void setMetodeBayar(String v) { metodeBayar = v; }
    public int getTotalTransaksi() { return totalTransaksi; }
    public void setTotalTransaksi(int v) { totalTransaksi = v; }
    public String getNamaKasirAsal() { return namaKasirAsal; }
    public void setNamaKasirAsal(String v) { namaKasirAsal = v; }
    public long getIdRetur() { return idRetur; }
    public void setIdRetur(long v) { idRetur = v; }
    public int getIdKasir() { return idKasir; }
    public void setIdKasir(int v) { idKasir = v; }
    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer v) { idAdmin = v; }
    public LocalDateTime getWaktu() { return waktu; }
    public void setWaktu(LocalDateTime v) { waktu = v; }
    public String getAlasan() { return alasan; }
    public void setAlasan(String v) { alasan = v; }
    public boolean isBelumDigunakan() { return belumDigunakan; }
    public void setBelumDigunakan(boolean v) { belumDigunakan = v; }
    public boolean isKemasanUtuh() { return kemasanUtuh; }
    public void setKemasanUtuh(boolean v) { kemasanUtuh = v; }
    public List<DetailRetur> getDetail() { return detail; }
}
