
package com.smartmart.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.smartmart.model.LogAktivitas;
import com.smartmart.model.Pegawai;
import com.smartmart.service.LogAktivitasService;

public class LogAktivitasPanel extends JPanel {

    private final LogAktivitasService logService;
    private final Pegawai penggunaLogin;

    private JTable tabelAktivitas;
    private DefaultTableModel tableModel;

    public LogAktivitasPanel(Pegawai penggunaLogin) {

        this.penggunaLogin = penggunaLogin;
        this.logService = new LogAktivitasService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadData();
    }

    // Membuat bagian judul
    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("LOG AKTIVITAS");
        title.setFont(
                new java.awt.Font("Arial", java.awt.Font.BOLD, 24)
        );

        JLabel description = new JLabel(
                "Pantau riwayat aktivitas pengguna SmartMart POS."
        );

        JPanel textPanel = new JPanel(
                new GridLayout(2, 1, 0, 5)
        );

        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(description);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    // Membuat tabel aktivitas
    private JScrollPane createTable() {

        String[] columns = {
                "ID",
                "Waktu",
                "ID Pegawai",
                "Nama Pegawai",
                "Aktivitas",
                "Keterangan"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelAktivitas = new JTable(tableModel);
        tabelAktivitas.setRowHeight(30);
        tabelAktivitas.setAutoResizeMode(
                JTable.AUTO_RESIZE_ALL_COLUMNS
        );

        tabelAktivitas.getTableHeader()
                .setReorderingAllowed(false);

        return new JScrollPane(tabelAktivitas);
    }

    // Membuat tombol aksi
    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        JButton refreshButton = new JButton("Refresh");

        refreshButton.addActionListener(e -> loadData());

        panel.add(refreshButton);

        return panel;
    }

    // Mengambil data aktivitas dari database
    private void loadData() {

        tableModel.setRowCount(0);

        try {

            List<LogAktivitas> daftarAktivitas =
                    logService.getAllAktivitas();

            for (LogAktivitas aktivitas : daftarAktivitas) {

                tableModel.addRow(new Object[]{
                        aktivitas.getIdAktivitas(),
                        aktivitas.getWaktu(),
                        aktivitas.getIdPegawai(),
                        aktivitas.getNamaPegawai(),
                        aktivitas.getAksi(),
                        aktivitas.getKeterangan()
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal mengambil data aktivitas:\n"
                            + e.getMessage(),
                    "Kesalahan Database",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
