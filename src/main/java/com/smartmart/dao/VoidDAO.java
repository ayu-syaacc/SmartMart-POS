package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VoidDAO {

    public static class Info {
        public String id;
        public LocalDateTime waktu;
        public String status;
        public Long idMember;
        public int poinDiperoleh;
        public int poinDipakai;
        public int total;
    }

    public static class Baris {
        public long idDetailTrx;
        public String kodeBarang;
        public Long idLot;
        public double jumlah;
    }

    static final String SQL_INFO =
            "SELECT id_transaksi, waktu, status, id_member, "
            + "       COALESCE(poin_diperoleh, 0) AS poin_diperoleh, COALESCE(poin_dipakai, 0) AS poin_dipakai, total "
            + "FROM transaksi WHERE id_transaksi = ?";

    static final String SQL_BARIS =
            "SELECT id_detail_trx, kode_barang, id_lot, jumlah FROM detail_transaksi "
            + "WHERE id_transaksi = ? ORDER BY id_detail_trx";

    public Info ambilInfo(Connection c, String idTransaksi) throws SQLException {
        PreparedStatement ps = c.prepareStatement(SQL_INFO);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                if (!rs.next()) return null;
                Info i = new Info();
                i.id = rs.getString("id_transaksi");
                i.waktu = WaktuUtil.parse(rs.getString("waktu"));
                i.status = rs.getString("status");
                long m = rs.getLong("id_member");
                i.idMember = rs.wasNull() ? null : Long.valueOf(m);
                i.poinDiperoleh = rs.getInt("poin_diperoleh");
                i.poinDipakai = rs.getInt("poin_dipakai");
                i.total = rs.getInt("total");
                return i;
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
    }

    public List<Baris> ambilBaris(Connection c, String idTransaksi) throws SQLException {
        List<Baris> hasil = new ArrayList<Baris>();
        PreparedStatement ps = c.prepareStatement(SQL_BARIS);
        try {
            ps.setString(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    Baris b = new Baris();
                    b.idDetailTrx = rs.getLong("id_detail_trx");
                    b.kodeBarang = rs.getString("kode_barang");
                    long lot = rs.getLong("id_lot");
                    b.idLot = rs.wasNull() ? null : Long.valueOf(lot);
                    b.jumlah = rs.getDouble("jumlah");
                    hasil.add(b);
                }
            } finally {
                rs.close();
            }
        } finally {
            ps.close();
        }
        return hasil;
    }
}
