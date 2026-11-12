package com.smartmart.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smartmart.dao.DashboardDAO;
import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.DashboardData;
import com.smartmart.model.DashboardData.PenjualanHarian;
import com.smartmart.model.DashboardData.PeringatanStok;

/**
 * Menyusun data Dashboard Admin dari tabel yang sudah ada
 * (transaksi, retur, stok_lot, stok_log, produk).
 */
public class DashboardService {

    /** Lot mulai diberi peringatan saat sisa masa simpan tinggal sekian hari (sama dengan batas FreshSave). */
    public static final int BATAS_PERINGATAN_HARI = 7;

    private static final int JUMLAH_LOG = 5;

    private final DashboardDAO dao;

    public DashboardService() {
        this(new DashboardDAO());
    }

    public DashboardService(DashboardDAO dao) {
        this.dao = dao;
    }

    public DashboardData ambilData() throws SQLException {
        return ambilData(LocalDate.now());
    }

    public DashboardData ambilData(LocalDate hariIni) throws SQLException {

        try (Connection c = DatabaseConnection.getConnection()) {

            DashboardData data = new DashboardData();

            // Tujuh hari terakhir, hari tanpa transaksi diisi nol.
            LocalDate awal = hariIni.minusDays(6);
            Map<LocalDate, PenjualanHarian> perHari = dao.penjualanPerHari(c, awal, hariIni);

            for (int i = 0; i < 7; i++) {
                LocalDate tanggal = awal.plusDays(i);
                PenjualanHarian p = perHari.get(tanggal);
                if (p == null) {
                    p = new PenjualanHarian();
                    p.tanggal = tanggal;
                }
                data.tujuhHari.add(p);
            }

            data.hariIni = data.tujuhHari.get(6);
            data.kemarin = data.tujuhHari.get(5);

            data.kerugianHariIni = dao.kerugianPada(c, hariIni);

            data.peringatan = susunPeringatan(
                    dao.lotMendekatiKedaluwarsa(c, hariIni, BATAS_PERINGATAN_HARI),
                    dao.produkMenipis(c));
            data.stokKritis = hitungStokKritis(data.peringatan);

            data.logTerbaru = dao.logStokTerbaru(c, JUMLAH_LOG);
            data.dimuatPada = LocalDateTime.now();

            return data;
        }
    }

    /** Status lot menurut sisa hari: 0 atau kurang = KEDALUWARSA, 1 = SEGERA, 2-3 = PRIORITAS, selebihnya PERHATIAN. */
    public static String statusLot(int sisaHari) {
        if (sisaHari <= 0) {
            return DashboardData.KEDALUWARSA;
        }
        if (sisaHari == 1) {
            return DashboardData.SEGERA;
        }
        if (sisaHari <= 3) {
            return DashboardData.PRIORITAS;
        }
        return DashboardData.PERHATIAN;
    }

    /** Memberi status lalu mengurutkan: paling mendesak dulu, lalu sisa hari terkecil, lalu nama. */
    static List<PeringatanStok> susunPeringatan(List<PeringatanStok> lot, List<PeringatanStok> menipis) {

        List<PeringatanStok> semua = new ArrayList<>();

        for (PeringatanStok p : lot) {
            p.status = statusLot(p.sisaHari);
            semua.add(p);
        }
        for (PeringatanStok p : menipis) {
            p.status = DashboardData.MENIPIS;
            semua.add(p);
        }

        semua.sort(Comparator
                .comparingInt((PeringatanStok p) -> urutan(p.status))
                .thenComparingInt(p -> p.sisaHari == null ? Integer.MAX_VALUE : p.sisaHari)
                .thenComparing(p -> p.namaProduk, String.CASE_INSENSITIVE_ORDER));

        return semua;
    }

    /**
     * Stok kritis = jumlah produk (tanpa duplikat) yang punya peringatan KEDALUWARSA, SEGERA,
     * PRIORITAS, atau MENIPIS. Status PERHATIAN belum dianggap kritis.
     */
    static int hitungStokKritis(List<PeringatanStok> peringatan) {
        Set<String> produk = new HashSet<>();
        for (PeringatanStok p : peringatan) {
            if (!DashboardData.PERHATIAN.equals(p.status)) {
                produk.add(p.kodeBarang);
            }
        }
        return produk.size();
    }

    private static int urutan(String status) {
        switch (status) {
            case DashboardData.KEDALUWARSA: return 0;
            case DashboardData.SEGERA: return 1;
            case DashboardData.PRIORITAS: return 2;
            case DashboardData.PERHATIAN: return 3;
            default: return 4;
        }
    }
}
