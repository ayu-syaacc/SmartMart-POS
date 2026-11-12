package com.smartmart.dao;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.DetailRetur;
import com.smartmart.model.Retur;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReturDAO {

    public static Connection buka() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    static final String SQL_TRANSAKSI =
            "SELECT t.id_transaksi, t.waktu, t.status, t.total, t.id_member, "
            + "       COALESCE(m.nama, 'Umum') AS pelanggan, "
            + "       COALESCE(p.metode, 'Tunai') AS metode, "
            + "       COALESCE(pg.nama, '-') AS kasir "
            + "FROM transaksi t "
            + "LEFT JOIN member m ON m.id_member = t.id_member "
            + "LEFT JOIN pembayaran p ON p.id_transaksi = t.id_transaksi "
            + "LEFT JOIN pegawai pg ON pg.id_pegawai = t.id_pegawai "
            + "WHERE t.id_transaksi = ?";

    static final String SQL_BARIS =
            "SELECT d.id_detail_trx, d.kode_barang, pr.nama, pr.satuan, pr.dapat_diretur, "
            + "       d.id_lot, l.tgl_kedaluwarsa, d.jumlah, d.subtotal, d.sumber_diskon, "
            + "       COALESCE((SELECT SUM(dr.jumlah) FROM detail_retur dr "
            + "                 WHERE dr.id_detail_trx = d.id_detail_trx), 0) AS sudah_diretur "
            + "FROM detail_transaksi d "
            + "JOIN produk pr ON pr.kode_barang = d.kode_barang "
            + "LEFT JOIN stok_lot l ON l.id_lot = d.id_lot "
            + "WHERE d.id_transaksi = ? ORDER BY d.id_detail_trx";

    static final String SQL_INSERT_RETUR =
            "INSERT INTO retur (id_transaksi, id_kasir, id_admin, waktu, alasan, total_refund) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    static final String SQL_INSERT_DETAIL =
            "INSERT INTO detail_retur (id_retur, id_detail_trx, jumlah, kondisi) VALUES (?, ?, ?, ?)";

    static final String SQL_JUMLAH_RETUR =
            "SELECT COUNT(*) FROM retur WHERE id_transaksi = ?";

    static final String SQL_ID_TERAKHIR = "SELECT last_insert_rowid()";

    public Retur ambilTransaksi(Connection c, String idTransaksi) throws SQLException {
        Retur r = null;
        PreparedStatement ps = c.prepareStatement(SQL_TRANSAKSI);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                if (!rs.next()) return null;
                r = new Retur(rs.getString("id_transaksi"));
                r.setWaktuTransaksi(WaktuUtil.parse(rs.getString("waktu")));
                r.setStatusTransaksi(rs.getString("status"));
                r.setTotalTransaksi(rs.getInt("total"));
                long idMember = rs.getLong("id_member");
                r.setIdMember(rs.wasNull() ? null : Long.valueOf(idMember));
                r.setNamaPelanggan(rs.getString("pelanggan"));
                r.setMetodeBayar(rs.getString("metode"));
                r.setNamaKasirAsal(rs.getString("kasir"));
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        r.getDetail().addAll(ambilBaris(c, idTransaksi));
        return r;
    }

    public List<DetailRetur> ambilBaris(Connection c, String idTransaksi) throws SQLException {
        List<DetailRetur> hasil = new ArrayList<DetailRetur>();
        PreparedStatement ps = c.prepareStatement(SQL_BARIS);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    DetailRetur d = new DetailRetur();
                    d.setIdDetailTrx(rs.getLong("id_detail_trx"));
                    d.setKodeBarang(rs.getString("kode_barang"));
                    d.setNamaBarang(rs.getString("nama"));
                    d.setSatuan(rs.getString("satuan"));
                    d.setDapatDiretur(WaktuUtil.bolehRetur(rs.getObject("dapat_diretur")));
                    long idLot = rs.getLong("id_lot");
                    d.setIdLot(rs.wasNull() ? null : Long.valueOf(idLot));
                    String kadaluwarsa = rs.getString("tgl_kedaluwarsa");
                    LocalDate tgl = kadaluwarsa == null || kadaluwarsa.trim().isEmpty()
                            ? null : WaktuUtil.parse(kadaluwarsa).toLocalDate();
                    d.setTglKedaluwarsaLot(tgl);
                    d.setJumlahDibeli(rs.getDouble("jumlah"));
                    d.setSubtotalBaris(rs.getInt("subtotal"));
                    d.setSumberDiskon(rs.getString("sumber_diskon"));
                    d.setSudahDiretur(rs.getDouble("sudah_diretur"));
                    hasil.add(d);
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return hasil;
    }

    public long simpan(Connection c, Retur r) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_INSERT_RETUR);
        try {
            ps.setString(1, r.getIdTransaksi());
            ps.setInt(2, r.getIdKasir());
            if (r.getIdAdmin() == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, r.getIdAdmin());
            LocalDateTime waktu = r.getWaktu() == null ? LocalDateTime.now() : r.getWaktu();
            ps.setString(4, waktu.format(OperasiDAO.FORMAT_WAKTU));
            ps.setString(5, r.getAlasan());
            ps.setInt(6, r.hitungRefund());
            ps.executeUpdate();
        } finally {
            ps.close();
        }
        long id = idTerakhir(c);
        r.setIdRetur(id);

        PreparedStatement pd = c.prepareStatement(SQL_INSERT_DETAIL);
        try {
            for (DetailRetur d : r.barangDipilih()) {
                pd.setLong(1, id);
                pd.setLong(2, d.getIdDetailTrx());
                pd.setDouble(3, d.getJumlahRetur());
                pd.setString(4, d.getKondisi());
                pd.addBatch();
            }
            pd.executeBatch();
        } finally {
            pd.close();
        }
        return id;
    }

    public int hitungRetur(Connection c, String idTransaksi) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_JUMLAH_RETUR);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                return rs.next() ? rs.getInt(1) : 0;
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    private long idTerakhir(Connection c) throws SQLException {
        Statement st = c.createStatement();
        try {
            ResultSet rs = st.executeQuery(SQL_ID_TERAKHIR);
            try {
                return rs.next() ? rs.getLong(1) : 0;
            } finally {
                rs.close();
            }
        } finally {
            st.close();
        }
    }
}
