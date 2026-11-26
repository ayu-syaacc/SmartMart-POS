package com.smartmart.util;

/**
 * Pembantu format angka ke Rupiah (branch: feature/db-schema-seed).
 * Pemisah ribuan titik, tanpa desimal, tidak bergantung pada Locale komputer.
 *
 *   format(17500)    -> "Rp17.500"
 *   format(-5350)    -> "-Rp5.350"
 *   angka(1482300)   -> "1.482.300"   (tanpa "Rp", untuk struk)
 *   parse("Rp17.500") -> 17500
 */
public final class FormatRupiah {

    private FormatRupiah() {
    }

    /** "Rp17.500"; nilai negatif menjadi "-Rp5.350". */
    public static String format(long nilai) {
        String angka = angka(nilai);
        return angka.startsWith("-") ? "-Rp" + angka.substring(1) : "Rp" + angka;
    }

    /** "17.500" tanpa awalan Rp (untuk kolom tabel dan struk). */
    public static String angka(long nilai) {
        String digit = Long.toString(nilai);
        boolean negatif = digit.startsWith("-");
        if (negatif) {
            digit = digit.substring(1);
        }
        StringBuilder sb = new StringBuilder();
        int awal = digit.length() % 3;
        if (awal > 0) {
            sb.append(digit, 0, awal);
        }
        for (int i = awal; i < digit.length(); i += 3) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(digit, i, i + 3);
        }
        return negatif ? "-" + sb : sb.toString();
    }

    /**
     * Kebalikan format(): "Rp17.500", "17.500", atau "17500" menjadi 17500.
     * Melempar NumberFormatException bila isinya bukan angka.
     */
    public static long parse(String teks) {
        if (teks == null) {
            throw new NumberFormatException("teks kosong");
        }
        String bersih = teks.trim().replace("Rp", "").replace("rp", "").replace(".", "").replace(" ", "");
        if (bersih.isEmpty() || bersih.equals("-")) {
            throw new NumberFormatException("bukan angka: " + teks);
        }
        return Long.parseLong(bersih);
    }
}
