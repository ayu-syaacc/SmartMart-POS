package com.smartmart.ui.admin;

import com.smartmart.dao.StokLogDAO;
import com.smartmart.model.StokLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class LossGuardPanel extends JPanel {

    private final StokLogDAO stokLogDAO;

    private JTable tabel;
    private DefaultTableModel modelTabel;

    private JTextField txtKodeBarang;
    private JComboBox<String> cmbJenis;

    public LossGuardPanel() {
        stokLogDAO = new StokLogDAO();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        buatHeader();
        buatFilter();
        buatTabel();
        buatPanelBawah();

        muatData();
    }

    private void buatHeader() {
        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
        panelHeader.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 10, 0)
        );

        JLabel judul = new JLabel("LossGuard");
        judul.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel deskripsi = new JLabel(
                "Pantau riwayat perubahan stok untuk membantu mengidentifikasi kehilangan dan kerusakan barang."
        );
        deskripsi.setFont(new Font("Arial", Font.PLAIN, 13));

        panelHeader.add(judul);
        panelHeader.add(Box.createVerticalStrut(5));
        panelHeader.add(deskripsi);

        add(panelHeader, BorderLayout.NORTH);
    }

    private void buatFilter() {
        JPanel panelFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel labelKode = new JLabel("Kode Barang:");
        txtKodeBarang = new JTextField(12);

        JLabel labelJenis = new JLabel("Jenis:");
        cmbJenis = new JComboBox<>(
                new String[]{
                        "Semua",
                        "JUAL",
                        "RUSAK",
                        "PENYESUAIAN"
                }
        );

        JButton tombolFilter = new JButton("Filter");
        JButton tombolReset = new JButton("Reset");

        tombolFilter.addActionListener(e -> filterData());
        tombolReset.addActionListener(e -> resetFilter());

        panelFilter.add(labelKode);
        panelFilter.add(txtKodeBarang);
        panelFilter.add(labelJenis);
        panelFilter.add(cmbJenis);
        panelFilter.add(tombolFilter);
        panelFilter.add(tombolReset);

        add(panelFilter, BorderLayout.CENTER);
    }

    private void buatTabel() {
        String[] kolom = {
                "ID Log",
                "Waktu",
                "Kode Barang",
                "ID Lot",
                "Jenis",
                "Jumlah",
                "ID Pegawai",
                "Alasan"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabel = new JTable(modelTabel);
        tabel.setRowHeight(25);
        tabel.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(tabel);

        add(scrollPane, BorderLayout.SOUTH);
    }

    private void buatPanelBawah() {
        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton tombolRefresh = new JButton("Refresh");

        tombolRefresh.addActionListener(e -> {
            txtKodeBarang.setText("");
            cmbJenis.setSelectedItem("Semua");
            muatData();
        });

        panelBawah.add(tombolRefresh);

        add(panelBawah, BorderLayout.PAGE_END);
    }

    private void muatData() {
        try {
            List<StokLog> data = stokLogDAO.ambilSemua();
            tampilkanData(data);
        } catch (SQLException e) {
            tampilkanError("Gagal mengambil data stok log.", e);
        }
    }

    private void filterData() {
        String kodeBarang = txtKodeBarang.getText().trim();
        String jenis = (String) cmbJenis.getSelectedItem();

        try {
            List<StokLog> data;

            if (!kodeBarang.isEmpty()) {
                data = stokLogDAO.ambilBerdasarkanBarang(kodeBarang);
            } else if ("Semua".equals(jenis)) {
                data = stokLogDAO.ambilSemua();
            } else {
                data = stokLogDAO.ambilBerdasarkanJenis(jenis);
            }

            tampilkanData(data);

        } catch (SQLException e) {
            tampilkanError("Gagal melakukan filter data.", e);
        }
    }

    private void resetFilter() {
        txtKodeBarang.setText("");
        cmbJenis.setSelectedItem("Semua");
        muatData();
    }

    private void tampilkanData(List<StokLog> data) {
        modelTabel.setRowCount(0);

        for (StokLog log : data) {
            modelTabel.addRow(new Object[]{
                    log.getIdLog(),
                    log.getWaktu(),
                    log.getKodeBarang(),
                    log.getIdLot(),
                    log.getJenis(),
                    log.getJumlah(),
                    log.getIdPegawai(),
                    log.getAlasan()
            });
        }
    }

    private void tampilkanError(String pesan, Exception e) {
        JOptionPane.showMessageDialog(
                this,
                pesan + "\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}