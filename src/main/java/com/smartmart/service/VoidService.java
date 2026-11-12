package com.smartmart.service;

import com.smartmart.dao.OperasiDAO;
import com.smartmart.dao.ReturDAO;
import com.smartmart.dao.VoidDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VoidService {

    public static class Hasil {
        public int barisStokKembali;
        public int poinDibatalkan;
        public int poinDikembalikan;
    }

    public static class DitolakException extends Exception {
        private static final long serialVersionUID = 1L;

        public DitolakException(String pesan) { super(pesan); }
    }

    private final VoidDAO voidDao = new VoidDAO();
    private final ReturDAO returDao = new ReturDAO();

    public List<String> validasiSetelahBayar(String idTransaksi, String alasan) throws SQLException {
        Connection c = ReturDAO.buka();
        try {
            return validasi(c, idTransaksi, alasan);
        } finally {
            c.close();
        }
    }

    private List<String> validasi(Connection c, String idTransaksi, String alasan) throws SQLException {
        List<String> p = new ArrayList<String>();
        VoidDAO.Info t = voidDao.ambilInfo(c, idTransaksi);
        if (t == null) {
            p.add("Transaksi tidak ditemukan.");
            return p;
        }
        if ("DIBATALKAN".equals(t.status)) {
            p.add("Transaksi sudah dibatalkan.");
            return p;
        }
        if (!"SELESAI".equals(t.status)) {
            p.add("Transaksi belum dibayar. Cukup tekan Batalkan di keranjang.");
            return p;
        }
        if (t.waktu == null || !t.waktu.toLocalDate().equals(LocalDate.now())) {
            p.add("Void setelah bayar hanya bisa pada hari transaksi yang sama.");
        }
        if (returDao.hitungRetur(c, idTransaksi) > 0) {
            p.add("Transaksi yang sudah diretur tidak dapat di-void.");
        }
        if (alasan == null || alasan.trim().isEmpty()) {
            p.add("Alasan void wajib diisi.");
        }
        return p;
    }

    public void batalkanSebelumBayar(String idTransaksi, int idPegawai) throws SQLException, DitolakException {
        Connection c = ReturDAO.buka();
        try {
            c.setAutoCommit(false);
            try {
                VoidDAO.Info t = voidDao.ambilInfo(c, idTransaksi);
                if (t == null) throw new DitolakException("Transaksi tidak ditemukan.");
                if (!"BERJALAN".equals(t.status)) {
                    throw new DitolakException("Transaksi sudah selesai atau dibatalkan. Gunakan void setelah bayar.");
                }
                OperasiDAO.ubahStatusTransaksi(c, idTransaksi, "DIBATALKAN");
                OperasiDAO.catatAktivitas(c, idPegawai, "BATAL",
                        idTransaksi + " dibatalkan sebelum dibayar");
                c.commit();
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

    public Hasil voidSetelahBayar(String idTransaksi, int idKasir, int idAdmin, String alasan)
            throws SQLException, DitolakException {
        Connection c = ReturDAO.buka();
        try {
            c.setAutoCommit(false);
            try {
                List<String> tolak = validasi(c, idTransaksi, alasan);
                if (!tolak.isEmpty()) throw new DitolakException(tolak.get(0));

                VoidDAO.Info t = voidDao.ambilInfo(c, idTransaksi);
                Hasil h = new Hasil();
                for (VoidDAO.Baris b : voidDao.ambilBaris(c, idTransaksi)) {
                    if (b.idLot != null) {
                        OperasiDAO.tambahStokLot(c, b.idLot.longValue(), b.jumlah);
                    }
                    OperasiDAO.catatStokLog(c, b.kodeBarang, b.idLot, "PENYESUAIAN", b.jumlah, idKasir,
                            "Void " + idTransaksi + ": " + alasan.trim());
                    h.barisStokKembali++;
                }
                if (t.idMember != null) {
                    h.poinDibatalkan = t.poinDiperoleh;
                    h.poinDikembalikan = t.poinDipakai;
                    OperasiDAO.ubahPoin(c, t.idMember.longValue(), -t.poinDiperoleh + t.poinDipakai);
                }
                OperasiDAO.ubahStatusTransaksi(c, idTransaksi, "DIBATALKAN");
                OperasiDAO.catatAktivitas(c, idKasir, "VOID",
                        idTransaksi + " | total Rp" + t.total + " | alasan: " + alasan.trim()
                        + " | admin penyetuju: " + idAdmin);
                c.commit();
                return h;
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
}
