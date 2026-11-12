package com.smartmart.model;

import com.smartmart.dao.StokLogDAO;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class StokLog {

    private int idLog;
    private String waktu;
    private String kodeBarang;
    private Integer idLot;
    private String jenis;
    private double jumlah;
    private int idPegawai;
    private String alasan;

    public StokLog() {
        this.waktu = LocalDateTime.now().toString();
    }

    public StokLog(int idLog, String waktu, String kodeBarang,
                   Integer idLot, String jenis, double jumlah,
                   int idPegawai, String alasan) {
        this.idLog = idLog;
        this.waktu = waktu;
        this.kodeBarang = kodeBarang;
        this.idLot = idLot;
        this.jenis = jenis;
        this.jumlah = jumlah;
        this.idPegawai = idPegawai;
        this.alasan = alasan;
    }

    public StokLog(String kodeBarang, Integer idLot, String jenis,
                   double jumlah, int idPegawai, String alasan) {
        this(
                0,
                LocalDateTime.now().toString(),
                kodeBarang,
                idLot,
                jenis,
                jumlah,
                idPegawai,
                alasan
        );
    }

    /**
     * Menyimpan catatan perubahan stok ke database.
     */
    public int catat() throws SQLException {
        StokLogDAO dao = new StokLogDAO();
        this.idLog = dao.catat(this);
        return this.idLog;
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public Integer getIdLot() {
        return idLot;
    }

    public void setIdLot(Integer idLot) {
        this.idLot = idLot;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public double getJumlah() {
        return jumlah;
    }

    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }

    public int getIdPegawai() {
        return idPegawai;
    }

    public void setIdPegawai(int idPegawai) {
        this.idPegawai = idPegawai;
    }

    public String getAlasan() {
        return alasan;
    }

    public void setAlasan(String alasan) {
        this.alasan = alasan;
    }

    @Override
    public String toString() {
        return "StokLog{" +
                "idLog=" + idLog +
                ", waktu='" + waktu + '\'' +
                ", kodeBarang='" + kodeBarang + '\'' +
                ", idLot=" + idLot +
                ", jenis='" + jenis + '\'' +
                ", jumlah=" + jumlah +
                ", idPegawai=" + idPegawai +
                ", alasan='" + alasan + '\'' +
                '}';
    }
}