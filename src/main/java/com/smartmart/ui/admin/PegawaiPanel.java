
package com.smartmart.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.smartmart.model.Pegawai;
import com.smartmart.service.PegawaiService;
import com.smartmart.service.LogAktivitasService;

public class PegawaiPanel extends JPanel {

    private final PegawaiService pegawaiService;
    private final Pegawai penggunaLogin;

    private JTable tabelPegawai;
    private DefaultTableModel tableModel;

    private JButton tambahButton;
    private JButton editButton;
    private JButton statusButton;
    private JButton refreshButton;

    public PegawaiPanel(Pegawai penggunaLogin) {

        this.penggunaLogin = penggunaLogin;
        this.pegawaiService = new PegawaiService();

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

        JLabel title = new JLabel("MANAJEMEN PEGAWAI");
        title.setFont(
                new java.awt.Font("Arial", java.awt.Font.BOLD, 24)
        );

        JLabel description = new JLabel(
                "Kelola data dan status akun pegawai SmartMart."
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

    // Membuat tabel pegawai
    private JScrollPane createTable() {

        String[] columns = {
                "ID",
                "Nama",
                "Username",
                "Peran",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelPegawai = new JTable(tableModel);
        tabelPegawai.setRowHeight(30);
        tabelPegawai.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        tabelPegawai.getTableHeader().setReorderingAllowed(false);

        return new JScrollPane(tabelPegawai);
    }

    // Membuat tombol aksi
    private JPanel createButtonPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );

        tambahButton = new JButton("Tambah Pegawai");
        editButton = new JButton("Edit Pegawai");
        statusButton = new JButton("Aktif/Nonaktif");
        refreshButton = new JButton("Refresh");

        tambahButton.addActionListener(e -> tambahPegawai());
        editButton.addActionListener(e -> editPegawai());
        statusButton.addActionListener(e -> ubahStatus());
        refreshButton.addActionListener(e -> loadData());

        panel.add(tambahButton);
        panel.add(editButton);
        panel.add(statusButton);
        panel.add(refreshButton);

        return panel;
    }

    // Mengambil data pegawai dari database
    private void loadData() {

        tableModel.setRowCount(0);

        try {

            List<Pegawai> daftarPegawai =
                    pegawaiService.getAllPegawai();

            for (Pegawai pegawai : daftarPegawai) {

                tableModel.addRow(new Object[]{
                        pegawai.getIdPegawai(),
                        pegawai.getNama(),
                        pegawai.getUsername(),
                        pegawai.getPeran(),
                        pegawai.isAktif() ? "Aktif" : "Nonaktif"
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal mengambil data pegawai:\n" + e.getMessage(),
                    "Kesalahan Database",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Menambahkan pegawai baru
    private void tambahPegawai() {

        JTextField namaField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JComboBox<String> peranCombo = new JComboBox<>(
                new String[]{"KASIR", "ADMIN"}
        );

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));

        form.add(new JLabel("Nama:"));
        form.add(namaField);

        form.add(new JLabel("Username:"));
        form.add(usernameField);

        form.add(new JLabel("Password:"));
        form.add(passwordField);

        form.add(new JLabel("Peran:"));
        form.add(peranCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Tambah Pegawai",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            pegawaiService.tambahPegawai(
                    namaField.getText(),
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    (String) peranCombo.getSelectedItem()
            );

            catatAktivitas(
        "TAMBAH_PEGAWAI",
        "Menambahkan pegawai: " + namaField.getText().trim()
                + " dengan username "
                + usernameField.getText().trim()
                + " dan peran "
                + peranCombo.getSelectedItem()
);

            JOptionPane.showMessageDialog(
                    this,
                    "Pegawai berhasil ditambahkan."
            );

            loadData();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Gagal Menambahkan Pegawai",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Mengedit data pegawai
    private void editPegawai() {

        int selectedRow = tabelPegawai.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih pegawai yang ingin diedit terlebih dahulu."
            );

            return;
        }

        int idPegawai = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString()
        );

        String namaLama =
                tableModel.getValueAt(selectedRow, 1).toString();

        String usernameLama =
                tableModel.getValueAt(selectedRow, 2).toString();

        String peranLama =
                tableModel.getValueAt(selectedRow, 3).toString();

        JTextField namaField = new JTextField(namaLama);
        JTextField usernameField = new JTextField(usernameLama);

        JComboBox<String> peranCombo = new JComboBox<>(
                new String[]{"KASIR", "ADMIN"}
        );

        peranCombo.setSelectedItem(peranLama);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));

        form.add(new JLabel("Nama:"));
        form.add(namaField);

        form.add(new JLabel("Username:"));
        form.add(usernameField);

        form.add(new JLabel("Peran:"));
        form.add(peranCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Pegawai",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            pegawaiService.ubahPegawai(
                    idPegawai,
                    namaField.getText(),
                    usernameField.getText(),
                    (String) peranCombo.getSelectedItem()
            );

            // Catat aktivitas edit pegawai
catatAktivitas(
        "UBAH_PEGAWAI",
        "Memperbarui data pegawai ID " + idPegawai
                + ". Nama baru: " + namaField.getText().trim()
                + ", username baru: "
                + usernameField.getText().trim()
                + ", peran baru: " + peranCombo.getSelectedItem()
);

            JOptionPane.showMessageDialog(
                    this,
                    "Data pegawai berhasil diperbarui."
            );

            loadData();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Gagal Mengedit Pegawai",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Mengubah status akun pegawai
    private void ubahStatus() {

        int selectedRow = tabelPegawai.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih pegawai terlebih dahulu."
            );

            return;
        }

        int idPegawai = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString()
        );

        if (idPegawai == penggunaLogin.getIdPegawai()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Anda tidak dapat menonaktifkan akun sendiri.",
                    "Aksi Tidak Diizinkan",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        String nama = tableModel.getValueAt(
                selectedRow, 1
        ).toString();

        String status = tableModel.getValueAt(
                selectedRow, 4
        ).toString();

        boolean statusBaru = "Nonaktif".equals(status);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Ubah status akun " + nama + " menjadi "
                        + (statusBaru ? "Aktif" : "Nonaktif") + "?",
                "Konfirmasi Status",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            pegawaiService.ubahStatus(idPegawai, statusBaru);
            // Catat aktivitas perubahan status pegawai
catatAktivitas(
        "UBAH_STATUS_PEGAWAI",
        "Mengubah status pegawai " + nama
                + " menjadi "
                + (statusBaru ? "Aktif" : "Nonaktif")
);

            JOptionPane.showMessageDialog(
                    this,
                    "Status akun berhasil diperbarui."
            );

            loadData();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Gagal Mengubah Status",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

   
// Mencatat aktivitas Admin
private void catatAktivitas(String aksi, String keterangan) {

    try {
        new LogAktivitasService().catatAktivitas(
                penggunaLogin.getIdPegawai(),
                aksi,
                keterangan
        );
    } catch (Exception e) {
        System.err.println(
                "Gagal mencatat aktivitas: " + e.getMessage()
        );
    }
}
    
}
