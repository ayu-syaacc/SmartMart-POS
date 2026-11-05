package com.smartmart.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class untuk mengatur proses retur barang dari transaksi yang sudah dilakukan.
 * Satu retur bisa berisi beberapa barang yang dikembalikan.
 *
 * Retur harus mendapat persetujuan admin terlebih dahulu.
 * Setelah disetujui, proses stok, LossGuard, poin member, dan log aktivitas
 * akan dilanjutkan oleh bagian program yang menangani proses tersebut.
 */
public class Retur {

    // Batas waktu retur maksimal 3 hari setelah pembelian.
    public static final int BATAS_HARI = 3;

    // Setiap Rp10.000 refund akan mengurangi 1 poin member.
    public static final long NILAI_PER_POIN = 10000L;

    public static final String STATUS_SELESAI = "SELESAI";

    // Data retur yang akan disimpan ke database.
    private int idRetur;
    private int idTransaksi;
    private int idKasir;
    private Integer idAdmin;
    private LocalDateTime waktu;
    private String alasan;
    private long totalRefund;

    // Data transaksi asli yang digunakan untuk mengecek apakah retur bisa dilakukan.
    private LocalDateTime waktuTransaksi;
    private String statusTransaksi;

    // Menyimpan daftar barang yang akan diretur.
    private final List<DetailRetur> daftarDetail = new ArrayList<DetailRetur>();

    public Retur() {
        this.waktu = LocalDateTime.now();
    }

    public Retur(int idTransaksi, LocalDateTime waktuTransaksi, String statusTransaksi, int idKasir) {
        this();
        this.idTransaksi = idTransaksi;
        this.waktuTransaksi = waktuTransaksi;
        this.statusTransaksi = statusTransaksi;
        this.idKasir = idKasir;
    }

    // Menambahkan barang ke daftar retur.
    public void tambahDetail(DetailRetur detail) {
        detail.setIdRetur(idRetur);
        daftarDetail.add(detail);
    }

    // Mengambil daftar barang retur tanpa mengizinkan perubahan langsung dari luar class.
    public List<DetailRetur> getDaftarDetail() {
        return Collections.unmodifiableList(daftarDetail);
    }

    // Menghitung berapa hari sejak transaksi dilakukan.
    public long getHariSejakPembelian(LocalDate hariIni) {
        return ChronoUnit.DAYS.between(waktuTransaksi.toLocalDate(), hariIni);
    }

    // Mengecek retur menggunakan tanggal hari ini.
    public List<String> validasi() {
        return validasi(LocalDate.now());
    }

    /**
     * Mengecek apakah pengajuan retur sudah memenuhi ketentuan.
     * Jika masih ada kesalahan, program akan mengembalikan pesan
     * agar kasir mengetahui alasan retur tidak bisa dilanjutkan.
     */
    public List<String> validasi(LocalDate hariIni) {
        List<String> pesan = new ArrayList<String>();

        if (idTransaksi <= 0 || waktuTransaksi == null) {
            pesan.add("ID transaksi asli wajib diisi dan harus ditemukan.");
            return pesan;
        }

        if (!STATUS_SELESAI.equals(statusTransaksi)) {
            pesan.add("Transaksi sudah dibatalkan (void) atau belum selesai, tidak dapat diretur.");
        }

        long hari = getHariSejakPembelian(hariIni);

        if (hari > BATAS_HARI) {
            pesan.add("Melewati batas retur " + BATAS_HARI + " hari (pembelian " + hari + " hari lalu).");
        }

        if (alasan == null || alasan.trim().isEmpty()) {
            pesan.add("Alasan retur wajib diisi.");
        }

        if (daftarDetail.isEmpty()) {
            pesan.add("Pilih minimal satu barang untuk diretur.");
        }

        // Mengecek setiap barang yang dimasukkan ke daftar retur.
        for (DetailRetur d : daftarDetail) {
            pesan.addAll(d.validasi());
        }

        return pesan;
    }

    // Menghitung total uang yang harus dikembalikan kepada pelanggan.
    public long hitungRefund() {
        long total = 0;

        for (DetailRetur d : daftarDetail) {
            total += d.hitungRefund();
        }

        totalRefund = total;
        return total;
    }

    // Menghitung jumlah poin member yang dikurangi berdasarkan total refund.
    public int hitungPoinDikurangi() {
        return (int) (hitungRefund() / NILAI_PER_POIN);
    }

    // Mengambil barang retur yang masih bisa dimasukkan kembali ke stok.
    public List<DetailRetur> getDetailMasukStok(LocalDate hariIni) {
        List<DetailRetur> hasil = new ArrayList<DetailRetur>();

        for (DetailRetur d : daftarDetail) {
            if (d.isMasukStok(hariIni)) {
                hasil.add(d);
            }
        }

        return hasil;
    }

    // Mengambil barang retur yang kondisinya rusak dan tidak bisa dijual kembali.
    public List<DetailRetur> getDetailRusak(LocalDate hariIni) {
        List<DetailRetur> hasil = new ArrayList<DetailRetur>();

        for (DetailRetur d : daftarDetail) {
            if (!d.isMasukStok(hariIni)) {
                hasil.add(d);
            }
        }

        return hasil;
    }

    // Menyimpan ID admin setelah persetujuan retur berhasil dilakukan.
    public void setujui(int idAdmin) {
        if (idAdmin <= 0) {
            throw new IllegalArgumentException("ID admin penyetuju tidak valid");
        }

        this.idAdmin = idAdmin;
    }

    // Mengecek apakah retur sudah disetujui oleh admin.
    public boolean isDisetujui() {
        return idAdmin != null;
    }

    // Getter dan setter untuk mengakses data retur.

    public int getIdRetur() {
        return idRetur;
    }

    public void setIdRetur(int idRetur) {
        this.idRetur = idRetur;

        for (DetailRetur d : daftarDetail) {
            d.setIdRetur(idRetur);
        }
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(int idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public int getIdKasir() {
        return idKasir;
    }

    public void setIdKasir(int idKasir) {
        this.idKasir = idKasir;
    }

    public Integer getIdAdmin() {
        return idAdmin;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }

    public long getTotalRefund() {
        return totalRefund;
    }

    public void setTotalRefund(long totalRefund) {
        this.totalRefund = totalRefund;
    }

    public LocalDateTime getWaktuTransaksi() {
        return waktuTransaksi;
    }

    public void setWaktuTransaksi(LocalDateTime waktuTransaksi) {
        this.waktuTransaksi = waktuTransaksi;
    }

    public String getStatusTransaksi() {
        return statusTransaksi;
    }

    public void setStatusTransaksi(String statusTransaksi) {
        this.statusTransaksi = statusTransaksi;
    }

    @Override
    public String toString() {
        return "Retur{transaksi=" + idTransaksi + ", baris=" + daftarDetail.size()
                + ", refund=" + totalRefund + ", disetujui=" + isDisetujui() + "}";
    }
}