package com.smartmart.model;

/**
 * Satu baris keranjang (branch: feature/smartcart-core).
 * Satu produk bisa punya beberapa baris bila diambil dari lot berbeda
 * (misalnya sebagian dari lot hampir kedaluwarsa yang kena FreshSave).
 *
 * BENTUK INI PERLU DISEPAKATI DENGAN CIKA (DetailTransaksi dibuat dari ItemKeranjang).
 */
public class ItemKeranjang {

    public enum JenisDiskon { TIDAK_ADA, MEMBER, FRESHSAVE }

    private final Produk produk;
    private final Integer lotId;          // null bila produk belum memakai lot
    private final int persenFreshSave;    // diskon FreshSave milik lot ini (0 bila tidak ada)
    private double jumlah;                // unit, atau kg untuk produk perKg
    private int persenMember;             // diatur oleh SmartCart

    public ItemKeranjang(Produk produk, Integer lotId, int persenFreshSave, double jumlah) {
        this.produk = produk;
        this.lotId = lotId;
        this.persenFreshSave = persenFreshSave;
        this.jumlah = jumlah;
    }

    public Produk getProduk() { return produk; }
    public Integer getLotId() { return lotId; }
    public int getPersenFreshSave() { return persenFreshSave; }
    public double getJumlah() { return jumlah; }

    void tambahJumlah(double tambahan) { this.jumlah += tambahan; }
    void setPersenMember(int persenMember) { this.persenMember = persenMember; }

    /** Harga sebelum diskon. */
    public long getHargaKotor() {
        return Math.round(produk.getHarga() * jumlah);
    }

    /** Hanya diskon terbesar yang dipakai (member atau FreshSave, tidak ditumpuk). */
    public JenisDiskon getJenisDiskon() {
        if (persenFreshSave <= 0 && persenMember <= 0) {
            return JenisDiskon.TIDAK_ADA;
        }
        return persenFreshSave > persenMember ? JenisDiskon.FRESHSAVE : JenisDiskon.MEMBER;
    }

    public int getPersenDiskon() {
        return Math.max(persenFreshSave, persenMember);
    }

    public long getDiskon() {
        return Math.round(getHargaKotor() * getPersenDiskon() / 100.0);
    }

    public long getTotal() {
        return getHargaKotor() - getDiskon();
    }
}
