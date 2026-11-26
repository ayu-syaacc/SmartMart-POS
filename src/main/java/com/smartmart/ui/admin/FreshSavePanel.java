package com.smartmart.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.smartmart.dao.FreshSaveDAO;
import com.smartmart.model.FreshSave;
import com.smartmart.model.Pegawai;

public class FreshSavePanel extends JPanel {

    private final FreshSaveDAO freshSaveDAO;
    private final Pegawai penggunaLogin;

    private JTable tabelFreshSave;
    private DefaultTableModel modelTabel;

    public FreshSavePanel(Pegawai penggunaLogin) {

        this.penggunaLogin = penggunaLogin;
        this.freshSaveDAO = new FreshSaveDAO();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buatHeader(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);
        add(buatPanelBawah(), BorderLayout.SOUTH);

        muatData();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel buatHeader() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel informasi = new JPanel();
        informasi.setLayout(new javax.swing.BoxLayout(
                informasi,
                javax.swing.BoxLayout.Y_AXIS
        ));
        informasi.setOpaque(false);

        JLabel judul = new JLabel("Manajemen FreshSave");
        judul.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel deskripsi = new JLabel(
                "Kelola batas hari kedaluwarsa dan persentase diskon FreshSave."
        );
        deskripsi.setFont(new Font("Arial", Font.PLAIN, 13));

        informasi.add(judul);
        informasi.add(javax.swing.Box.createVerticalStrut(5));
        informasi.add(deskripsi);

        panel.add(informasi, BorderLayout.WEST);

        return panel;
    }

    // ==========================================
    // TABEL
    // ==========================================

    private JScrollPane buatTabel() {

        modelTabel = new DefaultTableModel(
                new Object[]{
                        "ID Aturan",
                        "Batas Hari",
                        "Persen Diskon"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelFreshSave = new JTable(modelTabel);
        tabelFreshSave.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tabelFreshSave.setRowHeight(28);

        return new JScrollPane(tabelFreshSave);
    }

    // ==========================================
    // PANEL BAWAH
    // ==========================================

    private JPanel buatPanelBawah() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton tombolTambah = new JButton("Tambah");
        JButton tombolUbah = new JButton("Ubah");
        JButton tombolHapus = new JButton("Hapus");
        JButton tombolRefresh = new JButton("Refresh");

        tombolTambah.addActionListener(e -> tambahData());
        tombolUbah.addActionListener(e -> ubahData());
        tombolHapus.addActionListener(e -> hapusData());
        tombolRefresh.addActionListener(e -> muatData());

        panel.add(tombolTambah);
        panel.add(tombolUbah);
        panel.add(tombolHapus);
        panel.add(tombolRefresh);

        return panel;
    }

    // ==========================================
    // LOAD DATA
    // ==========================================

    private void muatData() {

        try {

            List<FreshSave> daftar =
                    freshSaveDAO.ambilSemua();

            modelTabel.setRowCount(0);

            for (FreshSave data : daftar) {

                modelTabel.addRow(new Object[]{
                        data.getIdAturan(),
                        data.getBatasHari() + " hari",
                        data.getPersenDiskon() + "%"
                });
            }

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal memuat data FreshSave:\n"
                    + e.getMessage()
            );
        }
    }

    // ==========================================
    // TAMBAH
    // ==========================================

    private void tambahData() {

        JTextField txtBatasHari = new JTextField();
        JTextField txtDiskon = new JTextField();

        JPanel panel = buatForm(
                txtBatasHari,
                txtDiskon
        );

        int hasil = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Tambah Aturan FreshSave",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (hasil != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            int batasHari = Integer.parseInt(
                    txtBatasHari.getText().trim()
            );

            int persenDiskon = Integer.parseInt(
                    txtDiskon.getText().trim()
            );

            validasi(
                    batasHari,
                    persenDiskon
            );

            FreshSave freshSave =
                    new FreshSave(
                            batasHari,
                            persenDiskon
                    );

            freshSaveDAO.tambah(freshSave);

            JOptionPane.showMessageDialog(
                    this,
                    "Aturan FreshSave berhasil ditambahkan.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            muatData();

        } catch (NumberFormatException e) {

            tampilkanError(
                    "Batas hari dan persen diskon harus berupa angka."
            );

        } catch (IllegalArgumentException e) {

            tampilkanError(e.getMessage());

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal menyimpan data:\n"
                    + e.getMessage()
            );
        }
    }

    // ==========================================
    // UBAH
    // ==========================================

    private void ubahData() {

        int baris = tabelFreshSave.getSelectedRow();

        if (baris == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih aturan yang ingin diubah terlebih dahulu.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int idAturan =
                Integer.parseInt(
                        modelTabel.getValueAt(baris, 0).toString()
                );

        int batasHariLama =
                Integer.parseInt(
                        modelTabel
                                .getValueAt(baris, 1)
                                .toString()
                                .replace(" hari", "")
                );

        int diskonLama =
                Integer.parseInt(
                        modelTabel
                                .getValueAt(baris, 2)
                                .toString()
                                .replace("%", "")
                );

        JTextField txtBatasHari =
                new JTextField(String.valueOf(batasHariLama));

        JTextField txtDiskon =
                new JTextField(String.valueOf(diskonLama));

        JPanel panel = buatForm(
                txtBatasHari,
                txtDiskon
        );

        int hasil = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Ubah Aturan FreshSave",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (hasil != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            int batasHari = Integer.parseInt(
                    txtBatasHari.getText().trim()
            );

            int persenDiskon = Integer.parseInt(
                    txtDiskon.getText().trim()
            );

            validasi(
                    batasHari,
                    persenDiskon
            );

            FreshSave freshSave =
                    new FreshSave(
                            idAturan,
                            batasHari,
                            persenDiskon
                    );

            freshSaveDAO.ubah(freshSave);

            JOptionPane.showMessageDialog(
                    this,
                    "Aturan FreshSave berhasil diubah.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            muatData();

        } catch (NumberFormatException e) {

            tampilkanError(
                    "Batas hari dan persen diskon harus berupa angka."
            );

        } catch (IllegalArgumentException e) {

            tampilkanError(e.getMessage());

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal mengubah data:\n"
                    + e.getMessage()
            );
        }
    }

    // ==========================================
    // HAPUS
    // ==========================================

    private void hapusData() {

        int baris = tabelFreshSave.getSelectedRow();

        if (baris == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih aturan yang ingin dihapus terlebih dahulu.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int idAturan =
                Integer.parseInt(
                        modelTabel.getValueAt(baris, 0).toString()
                );

        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus aturan FreshSave ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            freshSaveDAO.hapus(idAturan);

            JOptionPane.showMessageDialog(
                    this,
                    "Aturan FreshSave berhasil dihapus.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            muatData();

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal menghapus data:\n"
                    + e.getMessage()
            );
        }
    }

    // ==========================================
    // FORM
    // ==========================================

    private JPanel buatForm(
            JTextField txtBatasHari,
            JTextField txtDiskon
    ) {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(
                new JLabel("Batas Hari:"),
                gbc
        );

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(
                txtBatasHari,
                gbc
        );

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;

        panel.add(
                new JLabel("Persen Diskon:"),
                gbc
        );

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(
                txtDiskon,
                gbc
        );

        return panel;
    }

    // ==========================================
    // VALIDASI
    // ==========================================

    private void validasi(
            int batasHari,
            int persenDiskon
    ) {

        if (batasHari <= 0) {
            throw new IllegalArgumentException(
                    "Batas hari harus lebih dari 0."
            );
        }

        if (persenDiskon < 0 || persenDiskon > 100) {
            throw new IllegalArgumentException(
                    "Persen diskon harus berada antara 0 sampai 100."
            );
        }
    }

    // ==========================================
    // ERROR
    // ==========================================

    private void tampilkanError(String pesan) {

        JOptionPane.showMessageDialog(
                this,
                pesan,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}