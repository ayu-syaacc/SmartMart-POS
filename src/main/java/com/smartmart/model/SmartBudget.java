package com.smartmart.model;

/**
 * Anggaran belanja pelanggan (branch: feature/smartbudget).
 * AMAN: di bawah 90% anggaran. MENDEKATI: 90% sampai tepat 100%. TERLAMPAUI: lebih dari 100%.
 * Batas 0 berarti tidak ada anggaran (selalu AMAN).
 */
public class SmartBudget {

    public enum Status { AMAN, MENDEKATI, TERLAMPAUI }

    public static final int AMBANG_MENDEKATI_PERSEN = 90;

    private long batas;

    public SmartBudget() {
    }

    public SmartBudget(long batas) {
        setBatas(batas);
    }

    public long getBatas() { return batas; }

    public void setBatas(long batas) {
        if (batas < 0) {
            throw new IllegalArgumentException("Anggaran tidak boleh negatif");
        }
        this.batas = batas;
    }

    public boolean isAktif() { return batas > 0; }

    public Status hitungStatus(long total) {
        if (!isAktif()) {
            return Status.AMAN;
        }
        if (total > batas) {
            return Status.TERLAMPAUI;
        }
        if (total * 100 >= batas * AMBANG_MENDEKATI_PERSEN) {
            return Status.MENDEKATI;
        }
        return Status.AMAN;
    }

    /** Sisa anggaran, bernilai negatif bila terlampaui. */
    public long sisa(long total) {
        return batas - total;
    }

    /** Persen terpakai, dibulatkan ke bawah agar konsisten dengan status (89.999 dari 100.000 = 89%). */
    public int persenTerpakai(long total) {
        return isAktif() ? (int) Math.floorDiv(total * 100, batas) : 0;
    }
}
