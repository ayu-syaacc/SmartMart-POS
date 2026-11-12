package com.smartmart.service;

import java.sql.SQLException;
import java.util.List;

import com.smartmart.dao.LogAktivitasDAO;
import com.smartmart.model.LogAktivitas;

public class LogAktivitasService {

    private final LogAktivitasDAO logAktivitasDAO;

    public LogAktivitasService() {
        this.logAktivitasDAO = new LogAktivitasDAO();
    }

    public void catatAktivitas(
            int idPegawai,
            String aksi,
            String keterangan) throws SQLException {

        logAktivitasDAO.insert(idPegawai, aksi, keterangan);
    }

    public List<LogAktivitas> getAllAktivitas()
            throws SQLException {

        return logAktivitasDAO.findAll();
    }
}