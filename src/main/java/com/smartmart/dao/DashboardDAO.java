package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smartmart.model.DashboardData.LogStok;
import com.smartmart.model.DashboardData.PenjualanHarian;
import com.smartmart.model.DashboardData.PeringatanStok;

/**
 * Pembacaan data (hanya baca) untuk Dashboard Admin.
 * Memakai Connection dari pemanggil, sama seperti RiwayatDAO dan ReturDAO.
 *
 * Tanggal dibandingkan lewat substr(waktu, 1, 10) sehingga tidak bergantung pada
 * apakah waktu disimpan dengan spasi atau huruf T.
 */
public class DashboardDAO {

    static final String SQL_PENJUALAN =
            "SELECT substr(waktu, 1, 10) AS tgl, COUNT(*) AS jumlah, COALESCE(SUM(total), 0) AS omzet "
            + "FROM transaksi "
            + "WHERE UPPER(status) IN ('SELESAI', 'PAID') "
            + "  AND substr(waktu, 1, 10) BETWEEN ? AND ? "
            + "GROUP BY substr(waktu, 1, 10)";

    static final String SQL_REFUND =
            "SELECT substr(waktu, 1, 10) AS tgl, COALESCE(SUM(total_refund), 0) AS refund "
            + "FROM retur "
            + "WHERE substr(waktu, 1, 10) BETWEEN ? AND ? "
            + "GROUP BY substr(waktu, 1, 10)";

    // Nilai kerugian = jumlah barang x harga jual saat ini (stok_log tidak menyimpan harga).
    static final String SQL_KERUGIAN =
            "SELECT COALESCE(SUM(ABS(l.jumlah) * p.harga), 0) AS rugi "
            + "FROM stok_log l JOIN produk p ON p.kode_barang = l.kode_barang "
            + "WHERE UPPER(l.jenis) IN ('RUSAK', 'KEDALUWARSA', 'HILANG') "
            + "  AND substr(l.waktu, 1, 10) = ?";

    static final String SQL_LOT =
            "SELECT s.id_lot, s.kode_barang, p.nama, s.tgl_kedaluwarsa, s.jumlah_sisa "
            + "FROM stok_lot s JOIN produk p ON p.kode_barang = s.kode_barang "
            + "WHERE s.jumlah_sisa > 0 "
            + "  AND s.tgl_kedaluwarsa IS NOT NULL "
            + "  AND date(s.tgl_kedaluwarsa) <= date(?) "
            + "ORDER BY date(s.tgl_kedaluwarsa), p.nama";

    static final String SQL_MENIPIS =
            "SELECT p.kode_barang, p.nama, p.stok_minimum, COALESCE(SUM(s.jumlah_sisa), 0) AS stok "
            + "FROM produk p LEFT JOIN stok_lot s ON s.kode_barang = p.kode_barang "
            + "WHERE p.aktif = 1 AND p.stok_minimum > 0 "
            + "GROUP BY p.kode_barang, p.nama, p.stok_minimum "
            + "HAVING COALESCE(SUM(s.jumlah_sisa), 0) <= p.stok_minimum "
            + "ORDER BY stok ASC, p.nama";

    static final String SQL_LOG =
            "SELECT l.waktu, l.jenis, p.nama, l.jumlah, COALESCE(pg.nama, '-') AS pelaku, l.alasan "
            + "FROM stok_log l "
            + "JOIN produk p ON p.kode_barang = l.kode_barang "
            + "LEFT JOIN pegawai pg ON pg.id_pegawai = l.id_pegawai "
            + "ORDER BY l.waktu DESC, l.id_log DESC "
            + "LIMIT ?";

    /**
     * Penjualan dan refund per hari pada rentang [dari, sampai].
     * Hari tanpa transaksi dan tanpa refund tidak ada di hasil.
     */
    public Map<LocalDate, PenjualanHarian> penjualanPerHari(Connection c, LocalDate dari, LocalDate sampai)
            throws SQLException {

        Map<LocalDate, PenjualanHarian> hasil = new HashMap<>();

        try (PreparedStatement ps = c.prepareStatement(SQL_PENJUALAN)) {
            ps.setString(1, dari.toString());
            ps.setString(2, sampai.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PenjualanHarian p = ambil(hasil, LocalDate.parse(rs.getString("tgl")));
                    p.jumlahTransaksi = rs.getInt("jumlah");
                    p.penjualan = rs.getLong("omzet");
                }
            }
        }

        try (PreparedStatement ps = c.prepareStatement(SQL_REFUND)) {
            ps.setString(1, dari.toString());
            ps.setString(2, sampai.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ambil(hasil, LocalDate.parse(rs.getString("tgl"))).refund = rs.getLong("refund");
                }
            }
        }

        return hasil;
    }

    private static PenjualanHarian ambil(Map<LocalDate, PenjualanHarian> peta, LocalDate tanggal) {
        PenjualanHarian p = peta.get(tanggal);
        if (p == null) {
            p = new PenjualanHarian();
            p.tanggal = tanggal;
            peta.put(tanggal, p);
        }
        return p;
    }

    /** Nilai rupiah barang rusak, kedaluwarsa, dan hilang yang dicatat pada tanggal tersebut. */
    public long kerugianPada(Connection c, LocalDate tanggal) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_KERUGIAN)) {
            ps.setString(1, tanggal.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Math.round(rs.getDouble("rugi")) : 0L;
            }
        }
    }

    /**
     * Lot yang masih ada isinya dan kedaluwarsa paling lambat {@code batasHari} hari dari
     * {@code hariIni} (termasuk yang sudah lewat). Status belum diisi.
     */
    public List<PeringatanStok> lotMendekatiKedaluwarsa(Connection c, LocalDate hariIni, int batasHari)
            throws SQLException {

        List<PeringatanStok> hasil = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(SQL_LOT)) {
            ps.setString(1, hariIni.plusDays(batasHari).toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate kedaluwarsa;
                    try {
                        kedaluwarsa = LocalDate.parse(rs.getString("tgl_kedaluwarsa").trim().substring(0, 10));
                    } catch (DateTimeParseException | IndexOutOfBoundsException e) {
                        continue; // format tanggal tidak dikenali, lewati lot ini
                    }
                    PeringatanStok p = new PeringatanStok();
                    p.kodeBarang = rs.getString("kode_barang");
                    p.namaProduk = rs.getString("nama");
                    p.idLot = rs.getLong("id_lot");
                    p.sisaHari = (int) ChronoUnit.DAYS.between(hariIni, kedaluwarsa);
                    p.jumlah = rs.getDouble("jumlah_sisa");
                    hasil.add(p);
                }
            }
        }

        return hasil;
    }

    /** Produk aktif yang stok totalnya sudah di bawah atau sama dengan stok minimum. Status belum diisi. */
    public List<PeringatanStok> produkMenipis(Connection c) throws SQLException {

        List<PeringatanStok> hasil = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(SQL_MENIPIS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PeringatanStok p = new PeringatanStok();
                p.kodeBarang = rs.getString("kode_barang");
                p.namaProduk = rs.getString("nama");
                p.jumlah = rs.getDouble("stok");
                p.stokMinimum = rs.getDouble("stok_minimum");
                hasil.add(p);
            }
        }

        return hasil;
    }

    /** Catatan perubahan stok terbaru. */
    public List<LogStok> logStokTerbaru(Connection c, int batas) throws SQLException {

        List<LogStok> hasil = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(SQL_LOG)) {
            ps.setInt(1, batas);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LogStok l = new LogStok();
                    l.waktu = WaktuUtil.parse(rs.getString("waktu"));
                    l.jenis = rs.getString("jenis");
                    l.namaProduk = rs.getString("nama");
                    l.jumlah = rs.getDouble("jumlah");
                    l.pelaku = rs.getString("pelaku");
                    l.alasan = rs.getString("alasan");
                    hasil.add(l);
                }
            }
        }

        return hasil;
    }
}
