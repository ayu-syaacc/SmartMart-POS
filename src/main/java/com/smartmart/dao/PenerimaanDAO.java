package com.smartmart.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.smartmart.db.DatabaseConnection;
import com.smartmart.model.DetailPenerimaan;
import com.smartmart.model.PenerimaanBarang;

public class PenerimaanDAO {

    /**
     * Menyimpan penerimaan, detail penerimaan,
     * sekaligus membuat stok_lot.
     */
    public void simpanPenerimaan(PenerimaanBarang penerimaan)
            throws SQLException {

        if (penerimaan == null) {
            throw new IllegalArgumentException(
                    "Data penerimaan tidak boleh kosong."
            );
        }

        if (penerimaan.getDetail() == null
                || penerimaan.getDetail().isEmpty()) {
            throw new IllegalArgumentException(
                    "Detail penerimaan minimal satu barang."
            );
        }

        penerimaan.hitungTotal();

        try (Connection conn = DatabaseConnection.getConnection()) {

            boolean autoCommitAwal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                int idPenerimaan = simpanHeader(conn, penerimaan);
                penerimaan.setIdPenerimaan(idPenerimaan);

                for (DetailPenerimaan detail : penerimaan.getDetail()) {

                    validasiDetail(detail);

                    detail.setIdPenerimaan(idPenerimaan);

                    int idDetail = simpanDetail(conn, detail);
                    detail.setIdDetail(idDetail);

                    // Setiap detail penerimaan membuat satu LOT.
                    buatLot(conn, detail);
                }

                conn.commit();

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(autoCommitAwal);
            }
        }
    }

    private int simpanHeader(
            Connection conn,
            PenerimaanBarang penerimaan
    ) throws SQLException {

        String sql = """
                INSERT INTO penerimaan_barang
                    (no_penerimaan, tgl_terima,
                     id_supplier, id_pegawai, total)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, penerimaan.getNoPenerimaan());
            ps.setString(2, penerimaan.getTglTerima().toString());

            if (penerimaan.getIdSupplier() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, penerimaan.getIdSupplier());
            }

            ps.setInt(4, penerimaan.getIdPegawai());
            ps.setLong(5, penerimaan.getTotal());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Gagal mendapatkan ID penerimaan."
        );
    }

    private int simpanDetail(
            Connection conn,
            DetailPenerimaan detail
    ) throws SQLException {

        String sql = """
                INSERT INTO detail_penerimaan
                    (id_penerimaan, kode_barang,
                     jumlah, harga_beli, tgl_kedaluwarsa)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, detail.getIdPenerimaan());
            ps.setString(2, detail.getKodeBarang());
            ps.setInt(3, detail.getJumlah());
            ps.setLong(4, detail.getHargaBeli());

            if (detail.getTglKedaluwarsa() == null) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(
                        5,
                        detail.getTglKedaluwarsa().toString()
                );
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Gagal mendapatkan ID detail penerimaan."
        );
    }

    /**
     * Business Rule:
     * setiap detail penerimaan menghasilkan satu stok_lot.
     */
    private void buatLot(
            Connection conn,
            DetailPenerimaan detail
    ) throws SQLException {

        String sql = """
                INSERT INTO stok_lot
                    (kode_barang, id_detail,
                     tgl_kedaluwarsa, jumlah_sisa)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, detail.getKodeBarang());
            ps.setInt(2, detail.getIdDetail());

            if (detail.getTglKedaluwarsa() == null) {
                ps.setNull(3, java.sql.Types.VARCHAR);
            } else {
                ps.setString(
                        3,
                        detail.getTglKedaluwarsa().toString()
                );
            }

            ps.setInt(4, detail.getJumlah());

            ps.executeUpdate();
        }
    }

    private void validasiDetail(DetailPenerimaan detail) {

        if (detail == null) {
            throw new IllegalArgumentException(
                    "Detail penerimaan tidak boleh kosong."
            );
        }

        if (detail.getKodeBarang() == null
                || detail.getKodeBarang().isBlank()) {
            throw new IllegalArgumentException(
                    "Kode barang wajib diisi."
            );
        }

        if (detail.getJumlah() <= 0) {
            throw new IllegalArgumentException(
                    "Jumlah barang harus lebih dari 0."
            );
        }

        if (detail.getHargaBeli() < 0) {
            throw new IllegalArgumentException(
                    "Harga beli tidak boleh negatif."
            );
        }
    }
}