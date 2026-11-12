package com.smartmart.service;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.SmartCart;
import com.smartmart.model.SmartCart.LotInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Sumber lot untuk SmartCart yang membaca tabel stok_lot dan aturan_freshsave.
 *
 *  - Lot yang sudah kedaluwarsa atau kosong tidak ikut dijual.
 *  - Lot diurutkan dari yang paling dekat kedaluwarsa (FEFO).
 *  - Diskon FreshSave diambil dari aturan dengan batas hari terkecil yang masih mencakup sisa umur lot.
 *  - Produk yang belum punya lot sama sekali dianggap stok tak terbatas (perilaku lama).
 */
public class StokLotSource implements SmartCart.LotSource {

    private static final String SQL_ADA_LOT =
            "SELECT COUNT(*) FROM stok_lot WHERE kode_barang = ?";

    private static final String SQL_ATURAN =
            "SELECT batas_hari, persen_diskon FROM aturan_freshsave ORDER BY batas_hari ASC";

    private static final String SQL_LOT =
            "SELECT id_lot, tgl_kedaluwarsa, jumlah_sisa FROM stok_lot "
            + "WHERE kode_barang = ? AND jumlah_sisa > 0 "
            + "  AND (tgl_kedaluwarsa IS NULL OR date(tgl_kedaluwarsa) >= date('now', 'localtime')) "
            + "ORDER BY CASE WHEN tgl_kedaluwarsa IS NULL THEN 1 ELSE 0 END, date(tgl_kedaluwarsa), id_lot";

    @Override
    public List<LotInfo> ambilLotTerdekat(String kodeBarang) {
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(SQL_ADA_LOT)) {
                ps.setString(1, kodeBarang);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return SmartCart.tanpaLot().ambilLotTerdekat(kodeBarang);
                    }
                }
            }

            List<int[]> aturan = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(SQL_ATURAN);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    aturan.add(new int[]{rs.getInt(1), rs.getInt(2)});
                }
            }

            List<LotInfo> hasil = new ArrayList<>();
            LocalDate hariIni = LocalDate.now();
            try (PreparedStatement ps = c.prepareStatement(SQL_LOT)) {
                ps.setString(1, kodeBarang);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String tgl = rs.getString("tgl_kedaluwarsa");
                        int persen = 0;
                        if (tgl != null && tgl.length() >= 10) {
                            long sisaHari = ChronoUnit.DAYS.between(hariIni, LocalDate.parse(tgl.substring(0, 10)));
                            for (int[] a : aturan) {
                                if (sisaHari <= a[0]) {
                                    persen = a[1];
                                    break;
                                }
                            }
                        }
                        hasil.add(new LotInfo(rs.getInt("id_lot"), rs.getDouble("jumlah_sisa"), persen));
                    }
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw new IllegalStateException("Gagal membaca stok: " + e.getMessage(), e);
        }
    }
}
