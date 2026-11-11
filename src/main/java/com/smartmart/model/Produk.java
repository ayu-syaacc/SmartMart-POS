package com.smartmart.model;

/**
 * Data produk (branch: feature/product-model).
 * Harga disimpan dalam Rupiah bulat (long). Untuk produk perKg, harga adalah harga per kg.
 */
public class Produk {
    private int id;
    private String kode;        // kode barcode
    private String nama;
    private String kategori;    // Minuman, Makanan, Segar, Susu, Lainnya
    private long harga;
    private boolean perKg;
    private int stokMinimum;
    private boolean aktif = true;

    public Produk() {
    }

    public Produk(int id, String kode, String nama, String kategori, long harga,
                  boolean perKg, int stokMinimum, boolean aktif) {
        this.id = id;
        this.kode = kode;
        this.nama = nama;
        this.kategori = kategori;
        this.harga = harga;
        this.perKg = perKg;
        this.stokMinimum = stokMinimum;
        this.aktif = aktif;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public long getHarga() { return harga; }
    public void setHarga(long harga) { this.harga = harga; }

    public boolean isPerKg() { return perKg; }
    public void setPerKg(boolean perKg) { this.perKg = perKg; }

    public int getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(int stokMinimum) { this.stokMinimum = stokMinimum; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    @Override
    public String toString() {
        return nama + " (" + kode + ")";
    }
}
