package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class OperasiDAO {

    private OperasiDAO() { }

    public static final DateTimeFormatter FORMAT_WAKTU = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final String SQL_TAMBAH_STOK_LOT =
            "UPDATE stok_lot SET jumlah_sisa = jumlah_sisa + ? WHERE id_lot = ?";

    static final String SQL_CATAT_STOK_LOG =
            "INSERT INTO stok_log (waktu, kode_barang, id_lot, jenis, jumlah, id_pegawai, alasan) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    static final String SQL_UBAH_POIN =
            "UPDATE member SET poin = MAX(0, poin + ?) WHERE id_member = ?";

    static final String SQL_CATAT_AKTIVITAS =
            "INSERT INTO log_aktivitas (waktu, id_pegawai, aksi, keterangan) VALUES (?, ?, ?, ?)";

    static final String SQL_UBAH_STATUS_TRANSAKSI =
            "UPDATE transaksi SET status = ? WHERE id_transaksi = ?";

    public static String sekarang() {
        return LocalDateTime.now().format(FORMAT_WAKTU);
    }

    public static void tambahStokLot(Connection c, long idLot, double jumlah) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_TAMBAH_STOK_LOT);
        try {
            ps.setDouble(1, jumlah);
            ps.setLong(2, idLot);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    public static void catatStokLog(Connection c, String kodeBarang, Long idLot, String jenis,
                                    double jumlah, int idPegawai, String alasan) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_CATAT_STOK_LOG);
        try {
            ps.setString(1, sekarang());
            ps.setString(2, kodeBarang);
            if (idLot == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setLong(3, idLot);
            ps.setString(4, jenis);
            ps.setDouble(5, jumlah);
            ps.setInt(6, idPegawai);
            ps.setString(7, alasan);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    public static void ubahPoin(Connection c, long idMember, int delta) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_UBAH_POIN);
        try {
            ps.setInt(1, delta);
            ps.setLong(2, idMember);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    public static void catatAktivitas(Connection c, int idPegawai, String aksi, String keterangan)
            throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_CATAT_AKTIVITAS);
        try {
            ps.setString(1, sekarang());
            ps.setInt(2, idPegawai);
            ps.setString(3, aksi);
            ps.setString(4, keterangan);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    public static void ubahStatusTransaksi(Connection c, String idTransaksi, String status) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_UBAH_STATUS_TRANSAKSI);
        try {
            ps.setString(1, status);
            ps.setString(2, idTransaksi);
            ps.executeUpdate();
        } finally {
            ps.close();
        }
    }
}
