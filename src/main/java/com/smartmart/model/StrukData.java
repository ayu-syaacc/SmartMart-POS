package com.smartmart.model;

import com.smartmart.util.FormatRupiah;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Data satu struk (hasil transaksi yang sudah berstatus PAID).
 * Dipakai oleh StrukPanel (tampilan), cetak, dan simpan PDF.
 */
public class StrukData {

    public static class Baris {
        public String nama;
        public String rincian;      // contoh: "1 x 17.500" atau "0,75 kg x 48.000"
        public long jumlahKotor;    // harga sebelum diskon
        public String ketDiskon;    // contoh: "Diskon member -1.750"; null bila tanpa diskon
    }

    public int idTransaksi;
    public String nomor;
    public LocalDateTime waktu;
    public String kasir;
    public String namaMember;       // null bila tanpa member
    public String noMember;
    public final List<Baris> items = new ArrayList<>();

    public long subtotal;
    public long totalDiskon;
    public int poinDitukar;
    public long diskonPoin;
    public long total;

    public String metode;           // Tunai / QRIS / Debit
    public long dibayar;
    public long kembalian;

    public int poinDiperoleh;
    public int sisaPoin;

    public boolean adaMember() {
        return namaMember != null;
    }

    public String tanggalTeks() {
        return waktu.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", new Locale("id", "ID")));
    }

    public static String buatNomor(int idTransaksi, LocalDateTime waktu) {
        return "TRX-" + waktu.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", idTransaksi);
    }

    /** Struk dalam bentuk teks lebar tetap, untuk cetak dan PDF. */
    public List<String> teks(int lebar) {
        List<String> o = new ArrayList<>();
        String garis = "-".repeat(lebar);
        o.add(tengah("SMARTMART", lebar));
        o.add(tengah("Jl. Contoh No. 1, Pekanbaru", lebar));
        o.add(tengah("Telp. 0761-000000", lebar));
        o.add(garis);
        o.add(kiriKanan("No. transaksi", nomor, lebar));
        o.add(kiriKanan("Tanggal", tanggalTeks(), lebar));
        o.add(kiriKanan("Kasir", kasir, lebar));
        if (adaMember()) {
            o.add(kiriKanan("Member", namaMember + " (" + noMember + ")", lebar));
        }
        o.add(garis);
        for (Baris b : items) {
            o.add(kiriKanan(b.nama, FormatRupiah.angka(b.jumlahKotor), lebar));
            o.add("  " + b.rincian);
            if (b.ketDiskon != null) {
                o.add("  " + b.ketDiskon);
            }
        }
        o.add(garis);
        o.add(kiriKanan("Subtotal", FormatRupiah.angka(subtotal), lebar));
        o.add(kiriKanan("Total diskon", FormatRupiah.angka(-totalDiskon), lebar));
        if (poinDitukar > 0) {
            o.add(kiriKanan("Tukar poin (" + poinDitukar + ")", FormatRupiah.angka(-diskonPoin), lebar));
        }
        o.add(garis);
        o.add(kiriKanan("TOTAL", FormatRupiah.format(total), lebar));
        o.add(kiriKanan(metode, FormatRupiah.angka(dibayar), lebar));
        if ("Tunai".equals(metode)) {
            o.add(kiriKanan("Kembalian", FormatRupiah.angka(kembalian), lebar));
        }
        o.add(garis);
        if (adaMember()) {
            o.add(tengah("Poin diperoleh +" + poinDiperoleh + " | Sisa poin " + sisaPoin, lebar));
        }
        o.add(tengah("Terima kasih sudah berbelanja!", lebar));
        return o;
    }

    private static String tengah(String s, int lebar) {
        if (s.length() >= lebar) return s;
        return " ".repeat((lebar - s.length()) / 2) + s;
    }

    private static String kiriKanan(String kiri, String kanan, int lebar) {
        int sisa = lebar - kanan.length() - 1;
        if (kiri.length() > sisa) {
            kiri = kiri.substring(0, Math.max(0, sisa));
        }
        return kiri + " ".repeat(Math.max(1, lebar - kiri.length() - kanan.length())) + kanan;
    }
}
