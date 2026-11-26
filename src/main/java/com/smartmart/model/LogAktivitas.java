
package com.smartmart.model;

public class LogAktivitas {

    private int idAktivitas;
    private String waktu;
    private int idPegawai;
    private String namaPegawai;
    private String aksi;
    private String keterangan;

    public LogAktivitas(
            int idAktivitas,
            String waktu,
            int idPegawai,
            String namaPegawai,
            String aksi,
            String keterangan
    ) {
        this.idAktivitas = idAktivitas;
        this.waktu = waktu;
        this.idPegawai = idPegawai;
        this.namaPegawai = namaPegawai;
        this.aksi = aksi;
        this.keterangan = keterangan;
    }

    public int getIdAktivitas() {
        return idAktivitas;
    }

    public String getWaktu() {
        return waktu;
    }

    public int getIdPegawai() {
        return idPegawai;
    }

    public String getNamaPegawai() {
        return namaPegawai;
    }

    public String getAksi() {
        return aksi;
    }

    public String getKeterangan() {
        return keterangan;
    }
}
