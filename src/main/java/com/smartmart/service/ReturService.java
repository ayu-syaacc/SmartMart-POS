package com.smartmart.service;

import com.smartmart.dao.OperasiDAO;
import com.smartmart.dao.ReturDAO;
import com.smartmart.model.DetailRetur;
import com.smartmart.model.Retur;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReturService {

    public static class Hasil {
        public long idRetur;
        public int refund;
        public int poinDikurangi;
        public int barangMasukStok;
        public int barangRusak;
    }

    public static class DitolakException extends Exception {
        private static final long serialVersionUID = 1L;
        private final List<String> pesan;

        public DitolakException(List<String> pesan) {
            super(pesan.isEmpty() ? "Retur ditolak." : pesan.get(0));
            this.pesan = pesan;
        }

        public List<String> getPesan() { return pesan; }
    }

    private final ReturDAO dao = new ReturDAO();

    public Retur siapkan(String idTransaksi) throws SQLException {
        Connection c = ReturDAO.buka();
        try {
            return dao.ambilTransaksi(c, idTransaksi == null ? "" : idTransaksi.trim());
        } finally {
            c.close();
        }
    }

    public List<String> validasi(Retur r) {
        return r.validasi(LocalDate.now());
    }

    public Hasil proses(Retur input, int idKasir, int idAdmin) throws SQLException, DitolakException {
        Connection c = ReturDAO.buka();
        try {
            c.setAutoCommit(false);
            try {
                Hasil hasil = prosesDiDalam(c, input, idKasir, idAdmin);
                c.commit();
                return hasil;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } catch (DitolakException e) {
                c.rollback();
                throw e;
            } catch (RuntimeException e) {
                c.rollback();
                throw e;
            }
        } finally {
            c.close();
        }
    }

    private Hasil prosesDiDalam(Connection c, Retur input, int idKasir, int idAdmin)
            throws SQLException, DitolakException {
        Retur r = dao.ambilTransaksi(c, input.getIdTransaksi() == null ? "" : input.getIdTransaksi().trim());
        if (r == null) {
            List<String> p = new ArrayList<String>();
            p.add("Masukkan ID transaksi asli untuk melanjutkan.");
            throw new DitolakException(p);
        }
        for (DetailRetur d : r.getDetail()) {
            for (DetailRetur masuk : input.getDetail()) {
                if (masuk.getIdDetailTrx() == d.getIdDetailTrx()) {
                    d.setJumlahRetur(masuk.getJumlahRetur());
                    d.setKondisi(masuk.getKondisi());
                }
            }
        }
        r.setAlasan(input.getAlasan());
        r.setBelumDigunakan(input.isBelumDigunakan());
        r.setKemasanUtuh(input.isKemasanUtuh());
        r.setIdKasir(idKasir);
        r.setIdAdmin(Integer.valueOf(idAdmin));
        r.setWaktu(LocalDateTime.now());

        List<String> tolak = r.validasi(LocalDate.now());
        if (!tolak.isEmpty()) throw new DitolakException(tolak);

        Hasil h = new Hasil();
        h.refund = r.hitungRefund();
        h.idRetur = dao.simpan(c, r);

        LocalDate hariIni = LocalDate.now();
        for (DetailRetur d : r.barangDipilih()) {
            if (d.layakMasukStok(hariIni)) {
                OperasiDAO.tambahStokLot(c, d.getIdLot().longValue(), d.getJumlahRetur());
                OperasiDAO.catatStokLog(c, d.getKodeBarang(), d.getIdLot(), "RETUR",
                        d.getJumlahRetur(), idKasir, "Retur " + r.getIdTransaksi() + " (disetujui admin)");
                h.barangMasukStok++;
            } else {
                String sebab = DetailRetur.KONDISI_RUSAK.equals(d.getKondisi())
                        ? "barang rusak" : "tidak layak jual / lot asal kedaluwarsa";
                OperasiDAO.catatStokLog(c, d.getKodeBarang(), d.getIdLot(), "RUSAK",
                        -d.getJumlahRetur(), idKasir, "Retur " + r.getIdTransaksi() + ": " + sebab);
                h.barangRusak++;
            }
        }

        h.poinDikurangi = r.hitungPoinDikurangi();
        if (r.getIdMember() != null && h.poinDikurangi > 0) {
            OperasiDAO.ubahPoin(c, r.getIdMember().longValue(), -h.poinDikurangi);
        }

        OperasiDAO.catatAktivitas(c, idKasir, "RETUR",
                r.getIdTransaksi() + " | refund Rp" + h.refund + " | alasan: " + r.getAlasan()
                + " | admin penyetuju: " + idAdmin);
        return h;
    }
}
