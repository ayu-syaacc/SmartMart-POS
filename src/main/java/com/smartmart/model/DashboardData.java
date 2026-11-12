package com.smartmart.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data ringkasan untuk Dashboard Admin.
 * Hanya menampung hasil hitungan; pengambilan data ada di DashboardDAO
 * dan aturan status (kedaluwarsa, menipis, stok kritis) ada di DashboardService.
 */
public class DashboardData {

    // Status peringatan stok, dari yang paling mendesak.
    public static final String KEDALUWARSA = "KEDALUWARSA";
    public static final String SEGERA = "SEGERA";
    public static final String PRIORITAS = "PRIORITAS";
    public static final String PERHATIAN = "PERHATIAN";
    public static final String MENIPIS = "MENIPIS";

    /** Penjualan satu hari. */
    public static class PenjualanHarian {
        public LocalDate tanggal;
        /** Jumlah total transaksi berstatus SELESAI/PAID. */
        public long penjualan;
        /** Total refund retur yang diproses pada hari itu. */
        public long refund;
        public int jumlahTransaksi;

        /** Omzet bersih = penjualan dikurangi refund retur. */
        public long omzetBersih() {
            return penjualan - refund;
        }

        /** Rata-rata nilai per transaksi (0 bila belum ada transaksi). */
        public long rataRata() {
            return jumlahTransaksi == 0 ? 0 : Math.round((double) penjualan / jumlahTransaksi);
        }
    }

    /** Satu baris peringatan: lot yang mendekati kedaluwarsa, atau produk yang stoknya menipis. */
    public static class PeringatanStok {
        public String kodeBarang;
        public String namaProduk;
        /** Hanya terisi untuk peringatan lot. */
        public Long idLot;
        /** Sisa hari sampai kedaluwarsa; negatif bila sudah lewat. Null untuk peringatan menipis. */
        public Integer sisaHari;
        /** Jumlah sisa lot (peringatan lot) atau stok total produk (peringatan menipis). */
        public double jumlah;
        /** Hanya terisi untuk peringatan menipis. */
        public double stokMinimum;
        public String status;
    }

    /** Satu baris catatan perubahan stok (stok_log). */
    public static class LogStok {
        public LocalDateTime waktu;
        public String jenis;
        public String namaProduk;
        public double jumlah;
        public String pelaku;
        public String alasan;
    }

    public LocalDateTime dimuatPada;
    public PenjualanHarian hariIni;
    public PenjualanHarian kemarin;
    /** Tujuh hari terakhir, urut dari terlama sampai hari ini (selalu 7 elemen). */
    public List<PenjualanHarian> tujuhHari = new ArrayList<>();
    public long kerugianHariIni;
    /** Jumlah produk (tanpa duplikat) yang perlu tindakan. */
    public int stokKritis;
    /** Sudah terurut dari yang paling mendesak. */
    public List<PeringatanStok> peringatan = new ArrayList<>();
    public List<LogStok> logTerbaru = new ArrayList<>();

    /**
     * Perubahan omzet hari ini dibanding kemarin, dalam persen.
     * Null bila kemarin tidak ada omzet (persentase tidak bermakna).
     */
    public Integer persenPerubahanOmzet() {
        long omzetKemarin = kemarin == null ? 0 : kemarin.omzetBersih();
        if (omzetKemarin <= 0 || hariIni == null) {
            return null;
        }
        return (int) Math.round((hariIni.omzetBersih() - omzetKemarin) * 100.0 / omzetKemarin);
    }
}
