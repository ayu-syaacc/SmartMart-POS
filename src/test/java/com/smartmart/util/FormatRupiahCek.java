package com.smartmart.util;

// Pengecekan manual (bukan JUnit): jalankan main() dan lihat OK/GAGAL.
public class FormatRupiahCek {
    static int gagal = 0;

    static void cek(String nama, Object aktual, Object harapan) {
        boolean ok = aktual.equals(harapan);
        if (!ok) gagal++;
        System.out.println((ok ? "OK   " : "GAGAL") + " " + nama + ": " + aktual + (ok ? "" : " (harapan " + harapan + ")"));
    }

    public static void main(String[] a) {
        cek("0", FormatRupiah.format(0), "Rp0");
        cek("999", FormatRupiah.format(999), "Rp999");
        cek("1.000", FormatRupiah.format(1000), "Rp1.000");
        cek("17.500", FormatRupiah.format(17500), "Rp17.500");
        cek("60.150", FormatRupiah.format(60150), "Rp60.150");
        cek("1.482.300", FormatRupiah.format(1482300), "Rp1.482.300");
        cek("juta bulat", FormatRupiah.format(4820000), "Rp4.820.000");
        cek("negatif", FormatRupiah.format(-5350), "-Rp5.350");
        cek("negatif 3 digit", FormatRupiah.format(-500), "-Rp500");
        cek("Long.MIN_VALUE tidak error", FormatRupiah.format(Long.MIN_VALUE), "-Rp9.223.372.036.854.775.808");
        cek("angka tanpa Rp", FormatRupiah.angka(1482300), "1.482.300");
        cek("parse Rp", FormatRupiah.parse("Rp17.500"), 17500L);
        cek("parse titik", FormatRupiah.parse("1.482.300"), 1482300L);
        cek("parse polos", FormatRupiah.parse("  50000 "), 50000L);
        cek("parse negatif", FormatRupiah.parse("-Rp5.350"), -5350L);
        for (String buruk : new String[] {"", "abc", "Rp", null}) {
            try { FormatRupiah.parse(buruk); cek("parse " + buruk + " harus error", false, true); }
            catch (NumberFormatException e) { cek("parse \"" + buruk + "\" ditolak", true, true); }
        }
        long[] uji = {0, 7, 999, 1000, 123456, 99999999};
        for (long n : uji) cek("bolak-balik " + n, FormatRupiah.parse(FormatRupiah.format(n)), n);
        System.out.println(gagal == 0 ? "SEMUA OK" : "ADA GAGAL: " + gagal);
        if (gagal > 0) System.exit(1);
    }
}
