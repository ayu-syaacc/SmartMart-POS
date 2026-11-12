package com.smartmart.model;

// Pengecekan manual (bukan JUnit). Branch: feature/smartbudget

public class SmartBudgetCek {
    static int gagal = 0;

    static void cek(String nama, Object aktual, Object harapan) {
        boolean ok = aktual.equals(harapan);
        if (!ok) gagal++;
        System.out.println((ok ? "OK   " : "GAGAL") + " " + nama + ": " + aktual + " (harapan " + harapan + ")");
    }

    public static void main(String[] a) {
        SmartBudget b = new SmartBudget(100000);
        cek("89.999", b.hitungStatus(89999), SmartBudget.Status.AMAN);
        cek("90.000", b.hitungStatus(90000), SmartBudget.Status.MENDEKATI);
        cek("100.000", b.hitungStatus(100000), SmartBudget.Status.MENDEKATI);
        cek("100.001", b.hitungStatus(100001), SmartBudget.Status.TERLAMPAUI);
        cek("tanpa anggaran", new SmartBudget().hitungStatus(5000000), SmartBudget.Status.AMAN);

        // persen dibulatkan ke bawah, konsisten dengan status
        cek("persen 89.999", b.persenTerpakai(89999), 89);
        cek("persen 99.600", b.persenTerpakai(99600), 99);
        cek("persen 100.000", b.persenTerpakai(100000), 100);
        cek("persen tanpa anggaran", new SmartBudget().persenTerpakai(5000), 0);
        cek("sisa terlampaui", b.sisa(120000), -20000L);

        try { new SmartBudget(-1); cek("negatif harus error", false, true); }
        catch (IllegalArgumentException e) { cek("negatif ditolak", true, true); }

        System.out.println(gagal == 0 ? "SEMUA OK" : "ADA GAGAL: " + gagal);
        if (gagal > 0) System.exit(1);
    }
}
