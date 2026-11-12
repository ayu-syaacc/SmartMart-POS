package com.smartmart.model;

/**
 * Data produk, mengikuti tabel produk di schema.sql (Epi):
 * produk(kode_barang PK, nama, harga, satuan, stok_minimum, dapat_diretur, kategori, aktif).
 *
 * Harga dalam Rupiah bulat (long); untuk produk per kg, harga adalah harga per kg.
 * Produk dijual per kg bila satuan = "KG" (selain itu dianggap per unit, mis. "PCS").
 * Produk nonaktif tidak muncul di layar kasir, tetapi datanya tetap ada untuk riwayat transaksi.
 */
public class Produk {
    public static final String SATUAN_KG = "KG";

    /** Kategori yang dipakai tab di layar kasir dan form Product Management. */
    public static final String[] KATEGORI = {"Minuman", "Makanan", "Segar", "Susu", "Lainnya"};
    public static final String KATEGORI_DEFAULT = "Lainnya";

    private String kodeBarang;   // kode barcode, juga kunci utama
    private String nama;
    private String kategori = KATEGORI_DEFAULT;
    private long harga;
    private String satuan;       // PCS, KG, dst.
    private int stokMinimum;
    private boolean dapatDiretur = true;
    private boolean aktif = true;

    public Produk() {
    }

    public Produk(String kodeBarang, String nama, String kategori, long harga, String satuan,
                  int stokMinimum, boolean dapatDiretur, boolean aktif) {
        this.kodeBarang = kodeBarang;
        this.nama = nama;
        setKategori(kategori);
        this.harga = harga;
        this.satuan = satuan;
        this.stokMinimum = stokMinimum;
        this.dapatDiretur = dapatDiretur;
        this.aktif = aktif;
    }

    public String getKodeBarang() { return kodeBarang; }
    public void setKodeBarang(String kodeBarang) { this.kodeBarang = kodeBarang; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getKategori() { return kategori; }
    /** Kategori kosong atau null menjadi "Lainnya". */
    public void setKategori(String kategori) {
        this.kategori = (kategori == null || kategori.trim().isEmpty())
                ? KATEGORI_DEFAULT : kategori.trim();
    }

    public long getHarga() { return harga; }
    public void setHarga(long harga) { this.harga = harga; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public int getStokMinimum() { return stokMinimum; }
    public void setStokMinimum(int stokMinimum) { this.stokMinimum = stokMinimum; }

    public boolean isDapatDiretur() { return dapatDiretur; }
    public void setDapatDiretur(boolean dapatDiretur) { this.dapatDiretur = dapatDiretur; }

    public boolean isAktif() { return aktif; }
    public void setAktif(boolean aktif) { this.aktif = aktif; }

    /** Dijual per kg (berat diinput kasir) bila satuannya KG. */
    public boolean isPerKg() { return SATUAN_KG.equalsIgnoreCase(satuan); }

    @Override
    public String toString() {
        return nama + " (" + kodeBarang + ")";
    }
}
