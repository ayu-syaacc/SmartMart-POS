
package com.smartmart.dao;

import com.smartmart.model.Member;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {

    private final Connection connection;

    public MemberDAO(Connection connection) {
        this.connection = connection;
    }

    // Menambahkan data member
    public boolean tambah(Member member) {

        String sql = "INSERT INTO member "
                + "(kode_member, nama_member, nomor_telepon, "
                + "alamat, poin, tanggal_daftar, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, member.getKodeMember());
            ps.setString(2, member.getNamaMember());
            ps.setString(3, member.getNomorTelepon());
            ps.setString(4, member.getAlamat());
            ps.setInt(5, member.getPoin());
            ps.setString(6,
                    member.getTanggalDaftar().toString());
            ps.setString(7, member.getStatus());

            int hasil = ps.executeUpdate();

            if (hasil > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        member.setIdMember(rs.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Gagal menambahkan member: "
                    + e.getMessage());
        }

        return false;
    }

    // Mencari member berdasarkan ID
    public Member cariById(int id) {

        String sql = "SELECT * FROM member "
                + "WHERE id_member = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ambilData(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Gagal mencari member: "
                    + e.getMessage());
        }

        return null;
    }

    // Mencari member berdasarkan kode
    public Member cariByKode(String kode) {

        String sql = "SELECT * FROM member "
                + "WHERE kode_member = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setString(1, kode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ambilData(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Gagal mencari member: "
                    + e.getMessage());
        }

        return null;
    }

    // Mengambil seluruh data member
    public List<Member> ambilSemua() {

        List<Member> daftar = new ArrayList<>();

        String sql = "SELECT * FROM member "
                + "ORDER BY nama_member ASC";

        try (PreparedStatement ps =
                connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftar.add(ambilData(rs));
            }

        } catch (SQLException e) {
            System.out.println("Gagal mengambil data member: "
                    + e.getMessage());
        }

        return daftar;
    }

    // Memperbarui poin member
    public boolean ubahPoin(int idMember, int poinBaru) {

        if (poinBaru < 0) {
            return false;
        }

        String sql = "UPDATE member SET poin = ? "
                + "WHERE id_member = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setInt(1, poinBaru);
            ps.setInt(2, idMember);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Gagal memperbarui poin: "
                    + e.getMessage());
        }

        return false;
    }

    // Mengambil data dari database
    private Member ambilData(ResultSet rs)
            throws SQLException {

        Member member = new Member();

        member.setIdMember(rs.getInt("id_member"));
        member.setKodeMember(rs.getString("kode_member"));
        member.setNamaMember(rs.getString("nama_member"));
        member.setNomorTelepon(rs.getString("nomor_telepon"));
        member.setAlamat(rs.getString("alamat"));
        member.setPoin(rs.getInt("poin"));

        String tanggal = rs.getString("tanggal_daftar");

        if (tanggal != null) {
            member.setTanggalDaftar(
                    java.time.LocalDate.parse(tanggal));
        }

        member.setStatus(rs.getString("status"));

        return member;
    }
}
