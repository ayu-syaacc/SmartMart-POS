package com.smartmart.model;

import java.time.LocalDate;

public class DetailRetur {

    public static final String KONDISI_LAYAK = "LAYAK";
    public static final String KONDISI_RUSAK = "RUSAK";

    private long idDetailTrx;
    private String kodeBarang;
    private String namaBarang;
    private String satuan = "PCS";
    private boolean dapatDiretur = true;
    private Long idLot;
    private LocalDate tglKedaluwarsaLot;
    private double jumlahDibeli;
    private double sudahDiretur;
    private int subtotalBaris;
    private String sumberDiskon = "TIDAK_ADA";

    private double jumlahRetur;
    private String kondisi = KONDISI_LAYAK;

    public DetailRetur() { }

    public DetailRetur(long idDetailTrx, String kodeBarang, String namaBarang, String satuan,
                       boolean dapatDiretur, Long idLot, LocalDate tglKedaluwarsaLot,
                       double jumlahDibeli, double sudahDiretur, int subtotalBaris, String sumberDiskon) {
        this.idDetailTrx = idDetailTrx;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.satuan = satuan;
        this.dapatDiretur = dapatDiretur;
        this.idLot = idLot;
        this.tglKedaluwarsaLot = tglKedaluwarsaLot;
        this.jumlahDibeli = jumlahDibeli;
        this.sudahDiretur = sudahDiretur;
        this.subtotalBaris = subtotalBaris;
        this.sumberDiskon = sumberDiskon == null ? "TIDAK_ADA" : sumberDiskon;
    }

    public double sisaBisaDiretur() {
        return Math.max(0, jumlahDibeli - sudahDiretur);
    }

    public double hargaSatuanEfektif() {
        return jumlahDibeli <= 0 ? 0 : subtotalBaris / jumlahDibeli;
    }

    public int hitungRefund() {
        if (jumlahRetur <= 0) return 0;
        return (int) Math.round(hargaSatuanEfektif() * jumlahRetur);
    }

    public String alasanTolakBarang() {
        if (!dapatDiretur) {
            return namaBarang + ": barang tidak dapat diretur (sayur, buah, yoghurt, roti, dan produk cepat basi).";
        }
        if ("FRESHSAVE".equalsIgnoreCase(sumberDiskon)) {
            return namaBarang + ": dibeli dengan diskon FreshSave, barang FreshSave tidak dapat diretur.";
        }
        return null;
    }

    public String alasanTolakJumlah() {
        if (jumlahRetur > sisaBisaDiretur() + 1e-9) {
            return namaBarang + ": jumlah melebihi pembelian. Dibeli " + angka(jumlahDibeli)
                    + ", sudah diretur " + angka(sudahDiretur) + ", sisa maks. " + angka(sisaBisaDiretur()) + ".";
        }
        return null;
    }

    public boolean layakMasukStok(LocalDate hariIni) {
        if (!KONDISI_LAYAK.equals(kondisi)) return false;
        if (idLot == null) return false;
        return tglKedaluwarsaLot == null || tglKedaluwarsaLot.isAfter(hariIni);
    }

    public static String angka(double d) {
        return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d).replace('.', ',');
    }

    public long getIdDetailTrx() { return idDetailTrx; }
    public void setIdDetailTrx(long v) { idDetailTrx = v; }
    public String getKodeBarang() { return kodeBarang; }
    public void setKodeBarang(String v) { kodeBarang = v; }
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String v) { namaBarang = v; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String v) { satuan = v; }
    public boolean isDapatDiretur() { return dapatDiretur; }
    public void setDapatDiretur(boolean v) { dapatDiretur = v; }
    public Long getIdLot() { return idLot; }
    public void setIdLot(Long v) { idLot = v; }
    public LocalDate getTglKedaluwarsaLot() { return tglKedaluwarsaLot; }
    public void setTglKedaluwarsaLot(LocalDate v) { tglKedaluwarsaLot = v; }
    public double getJumlahDibeli() { return jumlahDibeli; }
    public void setJumlahDibeli(double v) { jumlahDibeli = v; }
    public double getSudahDiretur() { return sudahDiretur; }
    public void setSudahDiretur(double v) { sudahDiretur = v; }
    public int getSubtotalBaris() { return subtotalBaris; }
    public void setSubtotalBaris(int v) { subtotalBaris = v; }
    public String getSumberDiskon() { return sumberDiskon; }
    public void setSumberDiskon(String v) { sumberDiskon = v; }
    public double getJumlahRetur() { return jumlahRetur; }
    public void setJumlahRetur(double v) { jumlahRetur = v; }
    public String getKondisi() { return kondisi; }
    public void setKondisi(String v) { kondisi = v; }
}
