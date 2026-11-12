package com.smartmart.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Keranjang belanja (branch: feature/smartcart-core).
 *
 * Aturan yang dikerjakan di sini:
 *  - Barang diambil dari lot terdekat kedaluwarsa lebih dulu.
 *  - Bila stok lot tidak cukup, baris dipecah ke lot berikutnya.
 *  - Tiap baris memakai diskon terbesar saja (member atau FreshSave, tidak ditumpuk).
 *
 * Keranjang TIDAK mengurangi stok. Pengurangan stok baru terjadi setelah status PAID
 * (TransaksiService milik Cika, lewat StokLot.kurangi() milik Syikbi).
 */
public class SmartCart {

    /** Lot yang boleh dijual, diurutkan dari yang paling dekat kedaluwarsa. */
    public static class LotInfo {
        private final Integer lotId;
        private final double sisaStok;
        private final int persenFreshSave;

        public LotInfo(Integer lotId, double sisaStok, int persenFreshSave) {
            this.lotId = lotId;
            this.sisaStok = sisaStok;
            this.persenFreshSave = persenFreshSave;
        }

        public Integer getLotId() { return lotId; }
        public double getSisaStok() { return sisaStok; }
        public int getPersenFreshSave() { return persenFreshSave; }
    }

    /**
     * Sumber data lot. Nanti dihubungkan ke StokLot.ambilLotTerdekat() milik Syikbi
     * dan FreshSave.hitungDiskon(lot). Lot kedaluwarsa tidak boleh ikut dikembalikan.
     */
    public interface LotSource {
        List<LotInfo> ambilLotTerdekat(String kodeBarang);
    }

    /**
     * Pengganti sementara selama StokLot belum ada: stok dianggap tak terbatas, tanpa FreshSave.
     * lotId = null (artinya "tanpa lot"), jadi aman bila disimpan ke kolom lot_id yang nullable.
     */
    public static LotSource tanpaLot() {
        return kodeBarang -> Collections.singletonList(new LotInfo(null, Double.MAX_VALUE, 0));
    }

    private static final double EPS = 1e-9;

    private final LotSource lotSource;
    private final List<ItemKeranjang> items = new ArrayList<>();
    private int persenMember;

    public SmartCart(LotSource lotSource) {
        this.lotSource = lotSource;
    }

    /**
     * Menambah produk. Melempar IllegalArgumentException bila jumlah tidak valid,
     * dan IllegalStateException bila stok semua lot tidak cukup (keranjang tidak berubah).
     */
    public void tambah(Produk produk, double jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah harus lebih dari 0");
        }
        if (!produk.isPerKg() && Math.abs(jumlah - Math.rint(jumlah)) > EPS) {
            throw new IllegalArgumentException("Jumlah " + produk.getNama() + " harus bilangan bulat");
        }

        // Alokasi dihitung dulu agar keranjang tidak berubah separuh jalan bila stok kurang.
        Map<Integer, Double> alokasi = new java.util.LinkedHashMap<>();
        Map<Integer, LotInfo> infoLot = new HashMap<>();
        double sisa = jumlah;
        for (LotInfo lot : lotSource.ambilLotTerdekat(produk.getKodeBarang())) {
            if (sisa <= EPS) {
                break;
            }
            double tersedia = lot.getSisaStok() - sudahDiKeranjang(produk.getKodeBarang(), lot.getLotId());
            if (tersedia <= EPS) {
                continue;
            }
            double ambil = Math.min(sisa, tersedia);
            alokasi.put(lot.getLotId(), ambil);
            infoLot.put(lot.getLotId(), lot);
            sisa -= ambil;
        }
        if (sisa > EPS) {
            throw new IllegalStateException("Stok " + produk.getNama() + " tidak cukup");
        }

        for (Map.Entry<Integer, Double> e : alokasi.entrySet()) {
            ItemKeranjang ada = cariBaris(produk.getKodeBarang(), e.getKey());
            if (ada != null) {
                ada.tambahJumlah(e.getValue());
            } else {
                LotInfo lot = infoLot.get(e.getKey());
                ItemKeranjang baru = new ItemKeranjang(
                        produk, lot.getLotId(), lot.getPersenFreshSave(), e.getValue());
                baru.setPersenMember(persenMember);
                items.add(baru);
            }
        }
    }

    public void hapus(int indeks) {
        items.remove(indeks);
    }

    public void kosongkan() {
        items.clear();
    }

    /** Diskon member (persen). Diterapkan ke semua baris, lalu diskon terbesar yang menang. */
    public void setPersenMember(int persen) {
        this.persenMember = Math.max(0, persen);
        for (ItemKeranjang item : items) {
            item.setPersenMember(this.persenMember);
        }
    }

    public int getPersenMember() { return persenMember; }

    public List<ItemKeranjang> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean isKosong() { return items.isEmpty(); }

    public long getSubtotal() {
        long total = 0;
        for (ItemKeranjang i : items) {
            total += i.getHargaKotor();
        }
        return total;
    }

    public long getDiskonMember() {
        return jumlahDiskon(ItemKeranjang.JenisDiskon.MEMBER);
    }

    public long getDiskonFreshSave() {
        return jumlahDiskon(ItemKeranjang.JenisDiskon.FRESHSAVE);
    }

    public long getTotalHemat() {
        return getDiskonMember() + getDiskonFreshSave();
    }

    public long getTotal() {
        return getSubtotal() - getTotalHemat();
    }

    private long jumlahDiskon(ItemKeranjang.JenisDiskon jenis) {
        long total = 0;
        for (ItemKeranjang i : items) {
            if (i.getJenisDiskon() == jenis) {
                total += i.getDiskon();
            }
        }
        return total;
    }

    private double sudahDiKeranjang(String kodeBarang, Integer lotId) {
        ItemKeranjang i = cariBaris(kodeBarang, lotId);
        return i == null ? 0 : i.getJumlah();
    }

    private ItemKeranjang cariBaris(String kodeBarang, Integer lotId) {
        for (ItemKeranjang i : items) {
            if (Objects.equals(i.getProduk().getKodeBarang(), kodeBarang) && Objects.equals(i.getLotId(), lotId)) {
                return i;
            }
        }
        return null;
    }
}
