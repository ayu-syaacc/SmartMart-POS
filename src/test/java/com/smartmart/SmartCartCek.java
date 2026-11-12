package com.smartmart.model;

// Pengecekan manual (bukan JUnit): jalankan main() dan lihat OK/GAGAL. Angka diambil dari mockup.
// Branch: feature/smartcart-core

import java.util.Arrays;
import java.util.Collections;

public class SmartCartCek {
    static int gagal = 0;

    static void cek(String nama, long aktual, long harapan) {
        boolean ok = aktual == harapan;
        if (!ok) gagal++;
        System.out.println((ok ? "OK   " : "GAGAL") + " " + nama + ": " + aktual + " (harapan " + harapan + ")");
    }

    static void cekTrue(String nama, boolean kondisi) {
        if (!kondisi) gagal++;
        System.out.println((kondisi ? "OK   " : "GAGAL") + " " + nama);
    }

    public static void main(String[] a) {
        Produk susu = new Produk(1, "111", "Susu UHT 1L", "Susu", 17500, false, 10, true);
        Produk apel = new Produk(2, "222", "Apel Fuji", "Segar", 48000, true, 5, true);
        Produk roti = new Produk(3, "333", "Roti Tawar", "Makanan", 15000, false, 5, true);

        SmartCart.LotSource sumber = id -> {
            if (id == 3) return Arrays.asList(
                    new SmartCart.LotInfo(30, 1, 20),   // lot hampir kedaluwarsa, sisa 1, FreshSave 20%
                    new SmartCart.LotInfo(31, 10, 0));  // lot normal
            return Collections.singletonList(new SmartCart.LotInfo(id * 10, 100, 0));
        };

        // Skenario mockup: member 10%
        SmartCart c = new SmartCart(sumber);
        c.setPersenMember(10);
        c.tambah(susu, 1);
        c.tambah(apel, 0.75);
        c.tambah(roti, 1);
        cek("subtotal", c.getSubtotal(), 68500);
        cek("diskon member", c.getDiskonMember(), 5350);
        cek("diskon freshsave", c.getDiskonFreshSave(), 3000);
        cek("total", c.getTotal(), 60150);
        cek("hemat", c.getTotalHemat(), 8350);

        // Pemecahan baris: roti x3 -> 1 dari lot 30 (FreshSave) + 2 dari lot 31
        SmartCart d = new SmartCart(sumber);
        d.tambah(roti, 3);
        cek("jumlah baris roti x3", d.getItems().size(), 2);
        cek("baris1 diskon", d.getItems().get(0).getDiskon(), 3000);
        cek("baris2 diskon", d.getItems().get(1).getDiskon(), 0);

        // Stok tidak cukup: keranjang tidak berubah
        try { d.tambah(roti, 50); cekTrue("stok kurang harus error", false); }
        catch (IllegalStateException e) { cekTrue("stok kurang: " + e.getMessage(), true); }
        cek("baris tetap 2", d.getItems().size(), 2);

        // Produk non-kg tidak boleh pecahan
        try { d.tambah(susu, 1.5); cekTrue("pecahan harus error", false); }
        catch (IllegalArgumentException e) { cekTrue("pecahan: " + e.getMessage(), true); }

        // Tanpa lot (pengganti sementara): lotId null, produk yang sama digabung jadi satu baris
        SmartCart e = new SmartCart(SmartCart.tanpaLot());
        e.tambah(susu, 1);
        e.tambah(susu, 2);
        cek("tanpaLot: baris tergabung", e.getItems().size(), 1);
        cek("tanpaLot: jumlah", (long) e.getItems().get(0).getJumlah(), 3);
        cekTrue("tanpaLot: lotId null", e.getItems().get(0).getLotId() == null);
        cek("tanpaLot: total", e.getTotal(), 52500);

        System.out.println(gagal == 0 ? "SEMUA OK" : "ADA GAGAL: " + gagal);
        if (gagal > 0) System.exit(1);
    }
}
