
package com.smartmart.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class untuk menyimpan detail barang yang dikembalikan dalam proses retur.
 * Satu retur bisa memiliki beberapa barang dengan jumlah dan kondisi yang berbeda.
 *
 * Data transaksi asli juga disimpan sementara di class ini agar proses pengecekan
 * dan perhitungan refund bisa dilakukan tanpa mengambil data berulang kali.
 */
public class DetailRetur {

    // Kondisi barang setelah diperiksa oleh kasir.
    public enum Kondisi { LAYAK_JUAL, RUSAK }

    private static final double EPS = 1e-9;

    // Data detail retur yang disimpan ke database.
    private int id;
    private int idRetur;
    private int idDetailTrx;
    private double jumlah;
    private Kondisi kondisi;

    // Data barang dari transaksi asli untuk keperluan pengecekan retur.
    private int idLot;
    private String kodeBarang;
    private String namaProduk;
    private double jumlahDibeli;
    private double jumlahSudahDiretur;
    private long subtotalBaris;
    private String sumberDiskon;
    private boolean dapatDiretur;
    private LocalDate tglKedaluwarsaLot;

    public DetailRetur() {
    }

    public DetailRetur(int idDetailTrx, int idLot, String kodeBarang, String namaProduk,
                       double jumlahDibeli, double jumlahSudahDiretur, long subtotalBaris,
                       String sumberDiskon, boolean dapatDiretur, LocalDate tglKedaluwarsaLot) {
        this.idDetailTrx = idDetailTrx;
        this.idLot = idLot;
        this.kodeBarang = kodeBarang;
        this.namaProduk = namaProduk;
        this.jumlahDibeli = jumlahDibeli;
        this.jumlahSudahDiretur = jumlahSudahDiretur;
        this.subtotalBaris = subtotalBaris;
        this.sumberDiskon = sumberDiskon;
        this.dapatDiretur = dapatDiretur;
        this.tglKedaluwarsaLot = tglKedaluwarsaLot;
    }

    // Mengisi jumlah dan kondisi barang yang dipilih untuk diretur.
    public DetailRetur pilih(double jumlah, Kondisi kondisi) {
        this.jumlah = jumlah;
        this.kondisi = kondisi;
        return this;
    }

    // Menghitung sisa barang yang masih bisa dikembalikan.
    public double getJumlahBisaDiretur() {
        return Math.max(0, jumlahDibeli - jumlahSudahDiretur);
    }

    /**
     * Mengecek apakah barang memenuhi syarat untuk diretur.
     * Jika ada ketentuan yang tidak terpenuhi, program akan menampilkan
     * pesan yang menjelaskan alasan barang tidak bisa diretur.
     */
    public List<String> validasi() {
        List<String> pesan = new ArrayList<String>();
        String p = (namaProduk == null || namaProduk.trim().isEmpty()) ? "Barang" : namaProduk;

        if (!dapatDiretur) {
            pesan.add(p + ": produk cepat basi tidak dapat diretur.");
        }
        if ("FRESHSAVE".equals(sumberDiskon)) {
            pesan.add(p + ": dibeli dengan diskon FreshSave, tidak dapat diretur.");
        }

        double sisa = getJumlahBisaDiretur();
        if (sisa <= EPS) {
            pesan.add(p + ": sudah diretur seluruhnya.");
        } else if (jumlah <= EPS) {
            pesan.add(p + ": jumlah retur harus lebih dari 0.");
        } else if (jumlah > sisa + EPS) {
            pesan.add(p + ": jumlah retur " + format(jumlah)
                    + " melebihi sisa yang boleh diretur (" + format(sisa) + ").");
        }

        if (kondisi == null) {
            pesan.add(p + ": kondisi barang wajib dipilih.");
        }
        return pesan;
    }

    /**
     * Menghitung uang yang harus dikembalikan untuk barang ini.
     * Perhitungan menggunakan subtotal setelah diskon dan jumlah barang
     * yang dikembalikan. Hasilnya juga memperhitungkan retur sebelumnya
     * agar total refund tidak melebihi harga yang dibayar.
     *
     * Pastikan barang sudah lolos validasi sebelum menghitung refund.
     */
    public long hitungRefund() {
        if (jumlahDibeli <= EPS || jumlah <= EPS) {
            return 0;
        }
        double j = Math.min(jumlah, getJumlahBisaDiretur());
        long sesudah = Math.round(subtotalBaris * (jumlahSudahDiretur + j) / jumlahDibeli);
        long sebelum = Math.round(subtotalBaris * jumlahSudahDiretur / jumlahDibeli);
        return sesudah - sebelum;
    }

    // Mengecek apakah barang masih layak untuk dimasukkan kembali ke stok.
    public boolean isMasukStok(LocalDate hariIni) {
        if (kondisi != Kondisi.LAYAK_JUAL) {
            return false;
        }
        return tglKedaluwarsaLot == null || tglKedaluwarsaLot.isAfter(hariIni);
    }

    // Menentukan jenis pencatatan stok berdasarkan kondisi barang retur.
    public String getJenisStokLog(LocalDate hariIni) {
        return isMasukStok(hariIni) ? "RETUR" : "RUSAK";
    }

    // Mengatur tampilan angka agar lebih mudah dibaca.
    private static String format(double d) {
        if (Math.abs(d - Math.rint(d)) < EPS) {
            return String.valueOf((long) Math.rint(d));
        }
        return String.valueOf(Math.round(d * 100) / 100.0).replace('.', ',');
    }

    // Getter dan setter untuk mengambil atau mengubah data detail retur.

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdRetur() { return idRetur; }
    public void setIdRetur(int idRetur) { this.idRetur = idRetur; }

    public int getIdDetailTrx() { return idDetailTrx; }
    public void setIdDetailTrx(int idDetailTrx) { this.idDetailTrx = idDetailTrx; }

    public double getJumlah() { return jumlah; }
    public void setJumlah(double jumlah) { this.jumlah = jumlah; }

    public Kondisi getKondisi() { return kondisi; }
    public void setKondisi(Kondisi kondisi) { this.kondisi = kondisi; }

    public int getIdLot() { return idLot; }
    public void setIdLot(int idLot) { this.idLot = idLot; }

    public String getKodeBarang() { return kodeBarang; }
    public void setKodeBarang(String kodeBarang) { this.kodeBarang = kodeBarang; }

    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }

    public double getJumlahDibeli() { return jumlahDibeli; }
    public void setJumlahDibeli(double jumlahDibeli) { this.jumlahDibeli = jumlahDibeli; }

    public double getJumlahSudahDiretur() { return jumlahSudahDiretur; }
    public void setJumlahSudahDiretur(double jumlahSudahDiretur) { this.jumlahSudahDiretur = jumlahSudahDiretur; }

    public long getSubtotalBaris() { return subtotalBaris; }
    public void setSubtotalBaris(long subtotalBaris) { this.subtotalBaris = subtotalBaris; }

    public String getSumberDiskon() { return sumberDiskon; }
    public void setSumberDiskon(String sumberDiskon) { this.sumberDiskon = sumberDiskon; }

    public boolean isDapatDiretur() { return dapatDiretur; }
    public void setDapatDiretur(boolean dapatDiretur) { this.dapatDiretur = dapatDiretur; }

    public LocalDate getTglKedaluwarsaLot() { return tglKedaluwarsaLot; }
    public void setTglKedaluwarsaLot(LocalDate tglKedaluwarsaLot) { this.tglKedaluwarsaLot = tglKedaluwarsaLot; }

    @Override
    public String toString() {
        return "DetailRetur{" + namaProduk + " x" + format(jumlah) + ", " + kondisi + "}";
    }
}
