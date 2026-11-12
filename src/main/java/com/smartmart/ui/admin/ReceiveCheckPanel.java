package com.smartmart.ui.admin;

import com.smartmart.dao.PenerimaanDAO;
import com.smartmart.dao.ProdukDAO;
import com.smartmart.dao.SupplierDAO;
import com.smartmart.model.DetailPenerimaan;
import com.smartmart.model.Pegawai;
import com.smartmart.model.PenerimaanBarang;
import com.smartmart.model.Produk;
import com.smartmart.model.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReceiveCheckPanel extends JPanel {

    private final Pegawai penggunaLogin;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PenerimaanDAO penerimaanDAO = new PenerimaanDAO();

    private JComboBox<Supplier> cmbSupplier;
    private JTextField txtNoPenerimaan;
    private JTextField txtKodeBarang;
    private JTextField txtJumlah;
    private JTextField txtHargaBeli;
    private JTextField txtKedaluwarsa;

    private JTable tabelDetail;
    private DefaultTableModel modelTabel;

    public ReceiveCheckPanel(Pegawai penggunaLogin) {

        this.penggunaLogin = penggunaLogin;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(
                20, 20, 20, 20
        ));

        add(buatFormHeader(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);
        add(buatPanelBawah(), BorderLayout.SOUTH);

        muatSupplier();
    }

    private JPanel buatFormHeader() {

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNoPenerimaan = new JTextField(18);

        txtKodeBarang = new JTextField(18);

        txtJumlah = new JTextField(18);

        txtHargaBeli = new JTextField(18);

        txtKedaluwarsa = new JTextField(18);
        txtKedaluwarsa.setToolTipText(
                "Format: yyyy-MM-dd"
        );

        cmbSupplier = new JComboBox<>();

        int baris = 0;

        tambahBaris(
                panel, gbc, baris++,
                "No. Penerimaan:",
                txtNoPenerimaan
        );

        tambahBaris(
                panel, gbc, baris++,
                "Supplier:",
                cmbSupplier
        );

        tambahBaris(
                panel, gbc, baris++,
                "Kode Barang:",
                txtKodeBarang
        );

        tambahBaris(
                panel, gbc, baris++,
                "Jumlah:",
                txtJumlah
        );

        tambahBaris(
                panel, gbc, baris++,
                "Harga Beli:",
                txtHargaBeli
        );

        tambahBaris(
                panel, gbc, baris++,
                "Kedaluwarsa:",
                txtKedaluwarsa
        );

        JButton tombolTambah = new JButton(
                "Tambah ke Detail"
        );

        tombolTambah.addActionListener(
                e -> tambahDetail()
        );

        gbc.gridx = 1;
        gbc.gridy = baris;

        panel.add(tombolTambah, gbc);

        return panel;
    }

    private JScrollPane buatTabel() {

        String[] kolom = {
            "Kode Barang",
            "Nama Produk",
            "Jumlah",
            "Harga Beli",
            "Kedaluwarsa"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        tabelDetail = new JTable(modelTabel);

        tabelDetail.setRowHeight(30);

        return new JScrollPane(tabelDetail);
    }

    private JPanel buatPanelBawah() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        JButton tombolHapus = new JButton(
                "Hapus Detail"
        );

        JButton tombolSimpan = new JButton(
                "Simpan Penerimaan"
        );

        tombolHapus.addActionListener(
                e -> hapusDetail()
        );

        tombolSimpan.addActionListener(
                e -> simpanPenerimaan()
        );

        panel.add(tombolHapus);
        panel.add(tombolSimpan);

        return panel;
    }

    private void muatSupplier() {

        try {

            List<Supplier> daftar =
                    supplierDAO.ambilSemuaAktif();

            cmbSupplier.removeAllItems();

            for (Supplier supplier : daftar) {
                cmbSupplier.addItem(supplier);
            }

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal memuat supplier: "
                    + e.getMessage()
            );
        }
    }

    private void tambahDetail() {

        try {

            String kode = txtKodeBarang
                    .getText()
                    .trim();

            if (kode.isBlank()) {
                throw new IllegalArgumentException(
                        "Kode barang wajib diisi."
                );
            }

            int jumlah = Integer.parseInt(
                    txtJumlah.getText().trim()
            );

            long harga = Long.parseLong(
                    txtHargaBeli.getText().trim()
            );

            if (jumlah <= 0) {
                throw new IllegalArgumentException(
                        "Jumlah harus lebih dari 0."
                );
            }

            if (harga < 0) {
                throw new IllegalArgumentException(
                        "Harga beli tidak boleh negatif."
                );
            }

            Produk produk = produkDAO.cariKode(kode);

            if (produk == null) {
                throw new IllegalArgumentException(
                        "Produk dengan kode "
                        + kode
                        + " tidak ditemukan."
                );
            }

            LocalDate tanggalKadaluarsa = null;

            String teksTanggal =
                    txtKedaluwarsa.getText().trim();

            if (!teksTanggal.isBlank()) {

                tanggalKadaluarsa =
                        LocalDate.parse(teksTanggal);
            }

            modelTabel.addRow(new Object[] {
                produk.getKodeBarang(),
                produk.getNama(),
                jumlah,
                harga,
                tanggalKadaluarsa == null
                        ? "-"
                        : tanggalKadaluarsa.toString()
            });

            bersihkanFormDetail();

        } catch (NumberFormatException e) {

            tampilkanError(
                    "Jumlah dan harga beli harus berupa angka."
            );

        } catch (java.time.format.DateTimeParseException e) {

            tampilkanError(
                    "Format tanggal harus yyyy-MM-dd."
            );

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal mencari produk: "
                    + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            tampilkanError(e.getMessage());
        }
    }

    private void hapusDetail() {

        int baris = tabelDetail.getSelectedRow();

        if (baris == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih detail yang ingin dihapus.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        modelTabel.removeRow(baris);
    }

    private void simpanPenerimaan() {

        try {

            String noPenerimaan =
                    txtNoPenerimaan
                            .getText()
                            .trim();

            if (noPenerimaan.isBlank()) {

                throw new IllegalArgumentException(
                        "Nomor penerimaan wajib diisi."
                );
            }

            if (cmbSupplier.getSelectedItem() == null) {

                throw new IllegalArgumentException(
                        "Supplier wajib dipilih."
                );
            }

            if (modelTabel.getRowCount() == 0) {

                throw new IllegalArgumentException(
                        "Minimal harus ada satu detail barang."
                );
            }

            Supplier supplier =
                    (Supplier) cmbSupplier
                            .getSelectedItem();

            PenerimaanBarang penerimaan =
                    new PenerimaanBarang(
                            noPenerimaan,
                            LocalDate.now(),
                            supplier.getIdSupplier(),
                            penggunaLogin.getIdPegawai()
                    );

            for (int i = 0;
                 i < modelTabel.getRowCount();
                 i++) {

                String kode =
                        modelTabel
                                .getValueAt(i, 0)
                                .toString();

                int jumlah =
                        Integer.parseInt(
                                modelTabel
                                        .getValueAt(i, 2)
                                        .toString()
                        );

                long harga =
                        Long.parseLong(
                                modelTabel
                                        .getValueAt(i, 3)
                                        .toString()
                        );

                String teksTanggal =
                        modelTabel
                                .getValueAt(i, 4)
                                .toString();

                LocalDate tanggal = null;

                if (!teksTanggal.equals("-")) {
                    tanggal =
                            LocalDate.parse(teksTanggal);
                }

                penerimaan.tambahDetail(
                        new DetailPenerimaan(
                                kode,
                                jumlah,
                                harga,
                                tanggal
                        )
                );
            }

            penerimaanDAO.simpanPenerimaan(
                    penerimaan
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Penerimaan berhasil disimpan.\n"
                    + "LOT stok otomatis dibuat.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            bersihkanForm();

        } catch (SQLException e) {

            tampilkanError(
                    "Gagal menyimpan penerimaan: "
                    + e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            tampilkanError(e.getMessage());
        }
    }

    private void bersihkanFormDetail() {

        txtKodeBarang.setText("");
        txtJumlah.setText("");
        txtHargaBeli.setText("");
        txtKedaluwarsa.setText("");

        txtKodeBarang.requestFocus();
    }

    private void bersihkanForm() {

        txtNoPenerimaan.setText("");

        modelTabel.setRowCount(0);

        bersihkanFormDetail();

        muatSupplier();
    }

    private void tambahBaris(
            JPanel panel,
            GridBagConstraints gbc,
            int baris,
            String label,
            java.awt.Component komponen) {

        gbc.gridy = baris;

        gbc.gridx = 0;
        gbc.weightx = 0;

        panel.add(
                new JLabel(label),
                gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(
                komponen,
                gbc
        );
    }

    private void tampilkanError(String pesan) {

        JOptionPane.showMessageDialog(
                this,
                pesan,
                "Terjadi Kesalahan",
                JOptionPane.ERROR_MESSAGE
        );
    }
}