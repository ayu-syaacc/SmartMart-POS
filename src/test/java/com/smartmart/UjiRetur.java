import com.smartmart.model.*;
import java.time.*;
import java.util.*;

public class UjiRetur {
    static int gagal = 0;
    static void cek(String nama, boolean ok) { System.out.println((ok ? "OK    " : "GAGAL ") + nama); if (!ok) gagal++; }

    static Retur dasar(LocalDate beli) {
        Retur r = new Retur("TRX-20261103-0042");
        r.setWaktuTransaksi(beli.atTime(16, 20));
        r.setStatusTransaksi("SELESAI");
        r.setIdMember(42L);
        r.setAlasan("Salah beli");
        r.getDetail().add(new DetailRetur(1, "899100101", "Susu UHT 1L", "PCS", true, 10L,
                LocalDate.now().plusDays(120), 2, 0, 31500, "MEMBER"));
        r.getDetail().add(new DetailRetur(2, "899100102", "Apel Fuji", "KG", false, 11L,
                LocalDate.now().plusDays(6), 0.75, 0, 32400, "MEMBER"));
        r.getDetail().add(new DetailRetur(3, "899100104", "Yoghurt", "PCS", true, 12L,
                LocalDate.now().plusDays(1), 1, 0, 8000, "FRESHSAVE"));
        return r;
    }

    public static void main(String[] a) {
        LocalDate hariIni = LocalDate.now();

        Retur r = dasar(hariIni.minusDays(4));
        r.getDetail().get(0).setJumlahRetur(1);
        List<String> p = r.validasi(hariIni);
        cek("hari ke-4 ditolak", p.size() == 1 && p.get(0).startsWith("Lewat 3 hari"));

        r = dasar(hariIni.minusDays(3)); r.getDetail().get(0).setJumlahRetur(1);
        cek("hari ke-3 lolos", r.validasi(hariIni).isEmpty());
        r = dasar(hariIni.minusDays(1)); r.getDetail().get(0).setJumlahRetur(1);
        cek("hari ke-1 lolos", r.validasi(hariIni).isEmpty() && r.hariKe(hariIni) == 1);

        r = dasar(hariIni.minusDays(1)); r.getDetail().get(1).setJumlahRetur(0.5);
        p = r.validasi(hariIni);
        cek("buah ditolak", p.size() == 1 && p.get(0).contains("tidak dapat diretur"));
        r = dasar(hariIni.minusDays(1)); r.getDetail().get(2).setJumlahRetur(1);
        p = r.validasi(hariIni);
        cek("FreshSave ditolak", p.size() == 1 && p.get(0).contains("FreshSave"));

        r = dasar(hariIni.minusDays(1));
        r.getDetail().get(0).setSudahDiretur(1); r.getDetail().get(0).setJumlahRetur(2);
        p = r.validasi(hariIni);
        cek("jumlah melebihi ditolak", p.size() == 1 && p.get(0).contains("sisa maks. 1"));

        r = dasar(hariIni.minusDays(1)); r.setIdTransaksi(" ");
        cek("tanpa ID ditolak", r.validasi(hariIni).get(0).startsWith("Masukkan ID transaksi asli"));

        r = dasar(hariIni.minusDays(1)); r.setStatusTransaksi("DIBATALKAN"); r.getDetail().get(0).setJumlahRetur(1);
        cek("transaksi void ditolak", r.validasi(hariIni).get(0).contains("di-void"));

        r = dasar(hariIni.minusDays(1)); r.getDetail().get(0).setJumlahRetur(1); r.setKemasanUtuh(false);
        cek("kemasan tidak utuh ditolak", !r.validasi(hariIni).isEmpty());

        r = dasar(hariIni.minusDays(1)); r.getDetail().get(0).setJumlahRetur(1);
        cek("refund 1 susu = 15750", r.hitungRefund() == 15750);
        cek("poin dikurangi = 1", r.hitungPoinDikurangi() == 1);
        r.getDetail().get(0).setJumlahRetur(2);
        cek("refund 2 susu = 31500, poin 3", r.hitungRefund() == 31500 && r.hitungPoinDikurangi() == 3);
        r.setIdMember(null);
        cek("tanpa member poin 0", r.hitungPoinDikurangi() == 0);

        DetailRetur apel = new DetailRetur(9, "x", "Apel", "KG", true, 1L, null, 0.75, 0, 32400, "MEMBER");
        apel.setJumlahRetur(0.25);
        cek("refund 0,25 kg = 10800", apel.hitungRefund() == 10800);

        DetailRetur d = dasar(hariIni).getDetail().get(0);
        d.setJumlahRetur(1);
        cek("layak -> masuk stok", d.layakMasukStok(hariIni));
        d.setKondisi(DetailRetur.KONDISI_RUSAK);
        cek("rusak -> tidak masuk stok", !d.layakMasukStok(hariIni));
        d.setKondisi(DetailRetur.KONDISI_LAYAK); d.setTglKedaluwarsaLot(hariIni);
        cek("lot kedaluwarsa -> tidak masuk stok", !d.layakMasukStok(hariIni));

        System.out.println(gagal == 0 ? "\nSEMUA LULUS" : "\nGAGAL: " + gagal);
        System.exit(gagal == 0 ? 0 : 1);
    }
}
