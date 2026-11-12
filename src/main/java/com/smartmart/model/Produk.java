
package com.smartmart.model;

/**
 * Model data produk SmartMart.
 * Struktur atribut disesuaikan dengan tabel produk pada schema.sql.
 */
public class Produk {

    private String kodeBarang;
    private String nama;
    private String satuan;
    private long harga;
    private int stokMinimum;
    private boolean dapatDiretur;

    // Kompatibilitas sementara untuk SmartCart versi lama.
private int legacyId;

    // Stok merupakan hasil perhitungan dari tabel stok_lot.
    private double stok;

    /**
 * Konstruktor kompatibilitas untuk pengujian SmartCart versi lama.
 * Kategori belum digunakan dalam skema produk yang baru.
 */
@Deprecated
public Produk(
        int id,
        String kode,
        String nama,
        String kategori,
        long harga,
        boolean perKg,
        int stokMinimum,
        boolean aktif
) {
    this(
        kode,
        nama,
        harga,
        perKg ? "kg" : "pcs",
        stokMinimum,
        true
    );

    this.legacyId = id;
}

    public Produk() {
    }

    public Produk(
            String kodeBarang,
            String nama,
            long harga,
            String satuan,
            int stokMinimum,
            boolean dapatDiretur
    ) {
        this.kodeBarang = kodeBarang;
        this.nama = nama;
        this.harga = harga;
        this.satuan = satuan;
        this.stokMinimum = stokMinimum;
        this.dapatDiretur = dapatDiretur;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public long getHarga() {
        return harga;
    }

    public void setHarga(long harga) {
        this.harga = harga;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public int getStokMinimum() {
        return stokMinimum;
    }

    public void setStokMinimum(int stokMinimum) {
        this.stokMinimum = stokMinimum;
    }

    public boolean isDapatDiretur() {
        return dapatDiretur;
    }

    public void setDapatDiretur(boolean dapatDiretur) {
        this.dapatDiretur = dapatDiretur;
    }

    public double getStok() {
        return stok;
    }

    public void setStok(double stok) {
        this.stok = stok;
    }

    /**
     * Kompatibilitas sementara dengan SmartCart lama.
     * Produk dengan satuan kg atau liter dapat dijual pecahan.
     */
    public boolean isPerKg() {
        return satuan != null
                && (satuan.equalsIgnoreCase("kg")
                || satuan.equalsIgnoreCase("liter"));
    }

    /**
     * Kompatibilitas sementara.
     * Jangan digunakan sebagai identitas produk baru.
     */
    @Deprecated
public int getId() {
    return legacyId;
}

    @Override
    public String toString() {
        return nama + " (" + kodeBarang + ")";
    }
}
