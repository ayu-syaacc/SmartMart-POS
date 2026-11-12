
package com.smartmart.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.smartmart.model.Pegawai;
import com.smartmart.model.Produk;
import com.smartmart.service.ProdukService;

public class PersediaanPanel extends JPanel {

    private final ProdukService produkService;
    private final Pegawai penggunaLogin;

    private JTable tabelProduk;
    private DefaultTableModel modelTabel;
    private JTextField txtCari;

    private final DecimalFormat formatAngka =
            new DecimalFormat("#,###",
                    DecimalFormatSymbols.getInstance(Locale.forLanguageTag("id-ID")));

    public PersediaanPanel(Pegawai penggunaLogin) {

        this.penggunaLogin = penggunaLogin;
        this.produkService = new ProdukService();

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buatHeader(), BorderLayout.NORTH);
        add(buatTabel(), BorderLayout.CENTER);
        add(buatPanelBawah(), BorderLayout.SOUTH);

        muatDataProduk();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel buatHeader() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel informasi = new JPanel();
        informasi.setLayout(new javax.swing.BoxLayout(
                informasi, javax.swing.BoxLayout.Y_AXIS));
        informasi.setOpaque(false);

        JLabel judul = new JLabel("Manajemen Persediaan");
        judul.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel deskripsi = new JLabel(
                "Kelola data produk, harga, satuan, dan stok minimum."
        );
        deskripsi.setFont(new Font("Arial", Font.PLAIN, 13));

        informasi.add(judul);
        informasi.add(javax.swing.Box.createVerticalStrut(5));
        informasi.add(deskripsi);

        JPanel pencarian = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pencarian.setOpaque(false);

        txtCari = new JTextField(20);
        txtCari.setToolTipText("Cari berdasarkan nama produk");

        JButton tombolCari = new JButton("Cari");
        JButton tombolReset = new JButton("Reset");

        tombolCari.addActionListener(e -> cariProduk());
        tombolReset.addActionListener(e -> {
            txtCari.setText("");
            muatDataProduk();
        });

        txtCari.addActionListener(e -> cariProduk());

        pencarian.add(new JLabel("Cari Produk:"));
        pencarian.add(txtCari);
        pencarian.add(tombolCari);
        pencarian.add(tombolReset);

        panel.add(informasi, BorderLayout.WEST);
        panel.add(pencarian, BorderLayout.SOUTH);

        return panel;
    }

    // ==========================================
    // TABEL PRODUK
    // ==========================================

    private JScrollPane buatTabel() {

        String[] kolom = {
            "Kode Barang",
            "Nama Produk",
            "Harga",
            "Satuan",
            "Stok",
            "Stok Minimum",
            "Dapat Diretur"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelProduk = new JTable(modelTabel);
        tabelProduk.setRowHeight(32);
        tabelProduk.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelProduk.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12));

        tabelProduk.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        tabelProduk.setAutoCreateRowSorter(true);

        return new JScrollPane(tabelProduk);
    }

    // ==========================================
    // TOMBOL AKSI
    // ==========================================

    private JPanel buatPanelBawah() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5));

        panel.setOpaque(false);

        JButton tombolTambah = new JButton(" + Tambah Produk");
        JButton tombolUbah = new JButton("Ubah");
        JButton tombolHapus = new JButton("Hapus");
        JButton tombolMuatUlang = new JButton("Muat Ulang");

        tombolTambah.addActionListener(e -> tambahProduk());
        tombolUbah.addActionListener(e -> ubahProduk());
        tombolHapus.addActionListener(e -> hapusProduk());
        tombolMuatUlang.addActionListener(e -> muatDataProduk());

        panel.add(tombolTambah);
        panel.add(tombolUbah);
        panel.add(tombolHapus);
        panel.add(tombolMuatUlang);

        return panel;
    }

    // ==========================================
    // MEMUAT DATA
    // ==========================================

    private void muatDataProduk() {

        try {

            List<Produk> daftarProduk =
                    produkService.getAllProduk();

            tampilkanData(daftarProduk);

        } catch (SQLException e) {

            tampilkanError("Gagal memuat data produk: "
                    + e.getMessage());
        }
    }

    private void tampilkanData(List<Produk> daftarProduk)
        throws SQLException {

    modelTabel.setRowCount(0);

    for (Produk produk : daftarProduk) {

        int stokAktual =
                produkService.getStokProduk(produk.getKodeBarang());

        modelTabel.addRow(new Object[] {
            produk.getKodeBarang(),
            produk.getNama(),
            "Rp " + formatAngka.format(produk.getHarga()),
            produk.getSatuan(),
            formatAngka.format(stokAktual),
            produk.getStokMinimum(),
            produk.isDapatDiretur() ? "Ya" : "Tidak"
        });
    }
}
    // ==========================================
    // PENCARIAN
    // ==========================================

    private void cariProduk() {

        try {

            List<Produk> hasil =
                    produkService.cariNama(txtCari.getText());

            tampilkanData(hasil);

            if (hasil.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Produk tidak ditemukan.",
                        "Informasi",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (SQLException e) {

            tampilkanError("Pencarian gagal: " + e.getMessage());
        }
    }

    // ==========================================
    // TAMBAH PRODUK
    // ==========================================

    private void tambahProduk() {

        FormProduk form = new FormProduk(null);

        if (!form.tampilkan(this, "Tambah Produk")) {
            return;
        }

        try {

            produkService.tambahProduk(
                    form.getKodeBarang(),
                    form.getNama(),
                    form.getHarga(),
                    form.getSatuan(),
                    form.getStokMinimum(),
                    form.isDapatDiretur()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Produk berhasil ditambahkan.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            muatDataProduk();

        } catch (SQLException | IllegalArgumentException e) {

            tampilkanError("Gagal menambahkan produk: "
                    + e.getMessage());
        }
    }

    // ==========================================
    // UBAH PRODUK
    // ==========================================

    private void ubahProduk() {

        Produk produk = produkTerpilih();

        if (produk == null) {
            return;
        }

        FormProduk form = new FormProduk(produk);

        if (!form.tampilkan(this, "Ubah Produk")) {
            return;
        }

        try {

            produkService.ubahProduk(
                    produk.getKodeBarang(),
                    form.getKodeBarang(),
                    form.getNama(),
                    form.getHarga(),
                    form.getSatuan(),
                    form.getStokMinimum(),
                    form.isDapatDiretur()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Data produk berhasil diperbarui.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            muatDataProduk();

        } catch (SQLException | IllegalArgumentException e) {

            tampilkanError("Gagal mengubah produk: "
                    + e.getMessage());
        }
    }

    // ==========================================
    // HAPUS PRODUK
    // ==========================================

    private void hapusProduk() {

        Produk produk = produkTerpilih();

        if (produk == null) {
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Hapus produk " + produk.getNama()
                        + " (" + produk.getKodeBarang() + ")?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            produkService.hapusProduk(produk.getKodeBarang());

            JOptionPane.showMessageDialog(
                    this,
                   "Produk berhasil dinonaktifkan.",
                    "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            muatDataProduk();

        } catch (SQLException | IllegalArgumentException e) {

            tampilkanError(
                    "Gagal menghapus produk: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // PRODUK TERPILIH
    // ==========================================

    private Produk produkTerpilih() {

        int baris = tabelProduk.getSelectedRow();

        if (baris == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Pilih produk pada tabel terlebih dahulu.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );

            return null;
        }

        int barisModel = tabelProduk.convertRowIndexToModel(baris);

        String kode = modelTabel.getValueAt(barisModel, 0).toString();

        try {
            return produkService.cariProduk(kode);
        } catch (SQLException e) {
            tampilkanError("Gagal mengambil data produk: "
                    + e.getMessage());
            return null;
        }
    }

    // ==========================================
    // PESAN ERROR
    // ==========================================

    private void tampilkanError(String pesan) {

        JOptionPane.showMessageDialog(
                this,
                pesan,
                "Terjadi Kesalahan",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // ==========================================
    // FORM TAMBAH DAN UBAH PRODUK
    // ==========================================

    private static class FormProduk {

        private final JTextField txtKode = new JTextField(20);
        private final JTextField txtNama = new JTextField(20);
        private final JTextField txtHarga = new JTextField(20);
        private final JComboBox<String> cmbSatuan =
                new JComboBox<>(new String[] {"pcs", "kg", "liter"});
        private final JTextField txtStokMinimum = new JTextField(20);
        private final JCheckBox chkDapatDiretur =
                new JCheckBox("Produk dapat diretur");

        private boolean disimpan = false;

        FormProduk(Produk produk) {

            if (produk != null) {

                txtKode.setText(produk.getKodeBarang());
                txtKode.setEnabled(false);

                txtNama.setText(produk.getNama());
                txtHarga.setText(String.valueOf(produk.getHarga()));
                cmbSatuan.setSelectedItem(produk.getSatuan());

                txtStokMinimum.setText(
                        String.valueOf(produk.getStokMinimum()));

                chkDapatDiretur.setSelected(
                        produk.isDapatDiretur());
            }
        }

        boolean tampilkan(JPanel parent, String judul) {

            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(7, 7, 7, 7);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            tambahBaris(panel, gbc, 0, "Kode Barang:", txtKode);
            tambahBaris(panel, gbc, 1, "Nama Produk:", txtNama);
            tambahBaris(panel, gbc, 2, "Harga (Rupiah):", txtHarga);
            tambahBaris(panel, gbc, 3, "Satuan:", cmbSatuan);
            tambahBaris(panel, gbc, 4, "Stok Minimum:", txtStokMinimum);

            gbc.gridx = 1;
            gbc.gridy = 5;
            panel.add(chkDapatDiretur, gbc);

            int hasil = JOptionPane.showConfirmDialog(
                    parent,
                    panel,
                    judul,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (hasil != JOptionPane.OK_OPTION) {
                return false;
            }

            try {

                if (getKodeBarang().isBlank()
                        || getNama().isBlank()) {
                    throw new IllegalArgumentException(
                            "Kode barang dan nama produk wajib diisi.");
                }

                if (getHarga() < 0 || getStokMinimum() < 0) {
                    throw new IllegalArgumentException(
                            "Harga dan stok minimum tidak boleh negatif.");
                }

                disimpan = true;
                return true;

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(
                        parent,
                        "Harga dan stok minimum harus berupa angka.",
                        "Input Tidak Valid",
                        JOptionPane.WARNING_MESSAGE
                );

                return false;
            } catch (IllegalArgumentException e) {

                JOptionPane.showMessageDialog(
                        parent,
                        e.getMessage(),
                        "Input Tidak Valid",
                        JOptionPane.WARNING_MESSAGE
                );

                return false;
            }
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

            panel.add(new JLabel(label), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;

            panel.add(komponen, gbc);
        }

        String getKodeBarang() {
            return txtKode.getText().trim();
        }

        String getNama() {
            return txtNama.getText().trim();
        }

        long getHarga() {
            return Long.parseLong(txtHarga.getText().trim());
        }

        String getSatuan() {
            return cmbSatuan.getSelectedItem().toString();
        }

        int getStokMinimum() {
            return Integer.parseInt(txtStokMinimum.getText().trim());
        }

        boolean isDapatDiretur() {
            return chkDapatDiretur.isSelected();
        }
    }
}
