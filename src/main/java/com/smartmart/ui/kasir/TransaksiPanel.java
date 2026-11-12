package com.smartmart.ui.kasir;

import com.smartmart.dao.ProdukDAO;
import com.smartmart.model.ItemKeranjang;
import com.smartmart.model.Produk;
import com.smartmart.model.SmartBudget;
import com.smartmart.model.SmartCart;
import com.smartmart.util.FormatRupiah;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Layar utama kasir: scan / cari produk, keranjang, dan peringatan SmartBudget.
 * (branch: feature/scan-search-product, ditambah feature/smartbudget di file yang sama)
 *
 * TITIK SAMBUNG UNTUK ORANG LAIN:
 *  - Cika   : setBayarListener(cart -> ...) dipanggil saat tombol Bayar ditekan.
 *             Setelah status PAID, panggil resetTransaksi().
 *  - Cika   : setPersenMember(int) dipanggil saat member dipilih (0 bila tanpa member).
 *  - Syikbi : konstruktor menerima SmartCart.LotSource yang dibungkus StokLot.ambilLotTerdekat().
 *
 * ASUMSI: util.FormatRupiah (milik Epi) punya method static String format(long).
 */
public class TransaksiPanel extends JPanel {

    private static final String[] KATEGORI = java.util.stream.Stream
            .concat(java.util.stream.Stream.of("Semua"), java.util.stream.Stream.of(Produk.KATEGORI))
            .toArray(String[]::new);
    private static final Color HIJAU = new Color(0x1B7F3B);
    private static final Color ORANYE = new Color(0xC86A00);
    private static final Color MERAH = new Color(0xB00020);

    private final ProdukDAO produkDAO;
    private final SmartCart cart;
    private final SmartBudget budget = new SmartBudget();
    private final DecimalFormat formatJumlah =
            new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("id-ID")));

    private final JTextField txtCari = new JTextField();
    private final DefaultListModel<Produk> modelProduk = new DefaultListModel<>();
    private final JList<Produk> daftarProduk = new JList<>(modelProduk);
    private final DefaultTableModel modelKeranjang = new DefaultTableModel(
            new Object[]{"Produk", "Jumlah", "Harga", "Diskon", "Total"}, 0) {
        @Override public boolean isCellEditable(int baris, int kolom) { return false; }
    };
    private final JTable tabelKeranjang = new JTable(modelKeranjang);

    private final JLabel lblSubtotal = new JLabel();
    private final JLabel lblDiskonMember = new JLabel();
    private final JLabel lblDiskonFreshSave = new JLabel();
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblHemat = new JLabel();
    private final JTextField txtBudget = new JTextField(9);
    private final JLabel lblBudget = new JLabel(" ");

    private Consumer<SmartCart> bayarListener;
    private String kategoriAktif = "Semua";

    public TransaksiPanel(ProdukDAO produkDAO, SmartCart.LotSource lotSource) {
        this.produkDAO = produkDAO;
        this.cart = new SmartCart(lotSource);
        bangunTampilan();
        muatProduk();
        perbaruiKeranjang();
    }

    // ----- titik sambung untuk anggota lain -----

    public void setBayarListener(Consumer<SmartCart> listener) {
        this.bayarListener = listener;
    }

    public SmartCart getCart() {
        return cart;
    }

    /** Diskon member dalam persen. 0 bila pelanggan bukan member. */
    public void setPersenMember(int persen) {
        cart.setPersenMember(persen);
        perbaruiKeranjang();
    }

    /** Mengosongkan keranjang dan anggaran untuk transaksi berikutnya. */
    public void resetTransaksi() {
        cart.kosongkan();
        budget.setBatas(0);
        txtBudget.setText("");
        txtCari.setText("");
        perbaruiKeranjang();
        txtCari.requestFocusInWindow();
    }

    // ----- tampilan -----

    private void bangunTampilan() {
        setLayout(new BorderLayout(12, 0));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buatPanelProduk(), BorderLayout.CENTER);
        add(buatPanelKeranjang(), BorderLayout.EAST);
    }

    private JPanel buatPanelProduk() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));

        txtCari.setToolTipText("Scan barcode atau ketik nama produk, lalu Enter");
        txtCari.addActionListener(e -> prosesInputCari());
        JPanel atas = new JPanel(new BorderLayout(0, 8));
        atas.add(new JLabel("Scan barcode / ketik nama produk"), BorderLayout.NORTH);
        atas.add(txtCari, BorderLayout.CENTER);

        JPanel kategori = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        for (String nama : KATEGORI) {
            JButton tombol = new JButton(nama);
            tombol.addActionListener(e -> {
                kategoriAktif = nama;
                muatProduk();
            });
            kategori.add(tombol);
        }
        atas.add(kategori, BorderLayout.SOUTH);
        panel.add(atas, BorderLayout.NORTH);

        daftarProduk.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        daftarProduk.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean fokus) {
                Component c = super.getListCellRendererComponent(list, value, index, selected, fokus);
                Produk p = (Produk) value;
                String harga = FormatRupiah.format(p.getHarga()) + (p.isPerKg() ? "/kg" : "");
                setText(p.getNama() + "   -   " + harga);
                return c;
            }
        });
        daftarProduk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && daftarProduk.getSelectedValue() != null) {
                    tambahProduk(daftarProduk.getSelectedValue());
                }
            }
        });
        panel.add(new JScrollPane(daftarProduk), BorderLayout.CENTER);
        panel.add(new JLabel("Klik dua kali produk untuk menambah ke keranjang"), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buatPanelKeranjang() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(520, 0));

        tabelKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelKeranjang.setRowHeight(24);
        panel.add(new JScrollPane(tabelKeranjang), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new BorderLayout(0, 8));

        JPanel anggaran = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        anggaran.add(new JLabel("SmartBudget (Rp)"));
        anggaran.add(txtBudget);
        JButton btnAtur = new JButton("Atur");
        btnAtur.addActionListener(e -> aturAnggaran());
        anggaran.add(btnAtur);
        JPanel blokAnggaran = new JPanel(new BorderLayout());
        blokAnggaran.add(anggaran, BorderLayout.NORTH);
        blokAnggaran.add(lblBudget, BorderLayout.SOUTH);
        bawah.add(blokAnggaran, BorderLayout.NORTH);

        JPanel ringkasan = new JPanel(new GridLayout(0, 2, 4, 2));
        ringkasan.add(new JLabel("Subtotal"));
        ringkasan.add(rataKanan(lblSubtotal));
        ringkasan.add(new JLabel("Diskon Member"));
        ringkasan.add(rataKanan(lblDiskonMember));
        ringkasan.add(new JLabel("Diskon FreshSave"));
        ringkasan.add(rataKanan(lblDiskonFreshSave));
        JLabel judulTotal = new JLabel("Total");
        judulTotal.setFont(judulTotal.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        lblTotal.setFont(lblTotal.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        ringkasan.add(judulTotal);
        ringkasan.add(rataKanan(lblTotal));
        bawah.add(ringkasan, BorderLayout.CENTER);

        JPanel aksi = new JPanel(new BorderLayout(8, 0));
        aksi.add(lblHemat, BorderLayout.CENTER);
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> hapusBarisTerpilih());
        JButton btnBayar = new JButton("Bayar");
        btnBayar.addActionListener(e -> bayar());
        tombol.add(btnHapus);
        tombol.add(btnBayar);
        aksi.add(tombol, BorderLayout.EAST);

        JPanel selatan = new JPanel(new BorderLayout(0, 8));
        selatan.add(bawah, BorderLayout.CENTER);
        selatan.add(aksi, BorderLayout.SOUTH);
        panel.add(selatan, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel rataKanan(JLabel label) {
        label.setHorizontalAlignment(JLabel.RIGHT);
        return label;
    }

    // ----- aksi -----

    private void muatProduk() {
        try {
            modelProduk.clear();
            for (Produk p : produkDAO.ambilAktif(kategoriAktif)) {
                modelProduk.addElement(p);
            }
        } catch (SQLException ex) {
            tampilkanError("Gagal memuat produk", ex);
        }
    }

    /** Enter di kolom cari: coba sebagai barcode dulu, lalu sebagai nama produk. */
    private void prosesInputCari() {
        String teks = txtCari.getText().trim();
        if (teks.isEmpty()) {
            muatProduk();   // kembalikan daftar penuh setelah hasil pencarian
            return;
        }
        try {
            Produk persis = produkDAO.cariKode(teks);
            if (persis != null) {
                tambahProduk(persis);
                txtCari.setText("");
                return;
            }
            List<Produk> hasil = produkDAO.cariNama(teks);
            if (hasil.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Produk \"" + teks + "\" tidak ditemukan.",
                        "Tidak ditemukan", JOptionPane.WARNING_MESSAGE);
            } else if (hasil.size() == 1) {
                tambahProduk(hasil.get(0));
                txtCari.setText("");
            } else {
                modelProduk.clear();
                for (Produk p : hasil) {
                    modelProduk.addElement(p);
                }
                daftarProduk.requestFocusInWindow();
            }
        } catch (SQLException ex) {
            tampilkanError("Gagal mencari produk", ex);
        }
    }

    private void tambahProduk(Produk produk) {
        double jumlah = 1;
        if (produk.isPerKg()) {
            Double berat = InputBeratDialog.tampilkan(this, produk);
            if (berat == null) {
                return;
            }
            jumlah = berat;
        }
        try {
            cart.tambah(produk, jumlah);
            perbaruiKeranjang();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tidak dapat ditambahkan",
                    JOptionPane.WARNING_MESSAGE);
        }
        txtCari.requestFocusInWindow();
    }

    private void hapusBarisTerpilih() {
        int baris = tabelKeranjang.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris keranjang yang akan dihapus.");
            return;
        }
        cart.hapus(baris);
        perbaruiKeranjang();
    }

    private void aturAnggaran() {
        String teks = txtBudget.getText().trim().replace(".", "");
        try {
            budget.setBatas(teks.isEmpty() ? 0 : Long.parseLong(teks));
        } catch (IllegalArgumentException ex) {   // termasuk NumberFormatException
            JOptionPane.showMessageDialog(this, "Masukkan anggaran berupa angka, contoh: 100000",
                    "Anggaran tidak valid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        perbaruiKeranjang();
    }

    private void bayar() {
        if (cart.isKosong()) {
            JOptionPane.showMessageDialog(this, "Keranjang masih kosong.");
            return;
        }
        if (bayarListener == null) {
            JOptionPane.showMessageDialog(this, "Pembayaran belum dihubungkan (menunggu Cika).");
            return;
        }
        bayarListener.accept(cart);
    }

    // ----- penyegaran tampilan -----

    private void perbaruiKeranjang() {
        modelKeranjang.setRowCount(0);
        for (ItemKeranjang i : cart.getItems()) {
            modelKeranjang.addRow(new Object[]{
                    namaBaris(i),
                    jumlahTeks(i),
                    FormatRupiah.format(i.getHargaKotor()),
                    i.getDiskon() > 0 ? "-" + FormatRupiah.format(i.getDiskon()) : "-",
                    FormatRupiah.format(i.getTotal())});
        }
        lblSubtotal.setText(FormatRupiah.format(cart.getSubtotal()));
        lblDiskonMember.setText("-" + FormatRupiah.format(cart.getDiskonMember()));
        lblDiskonFreshSave.setText("-" + FormatRupiah.format(cart.getDiskonFreshSave()));
        lblTotal.setText(FormatRupiah.format(cart.getTotal()));
        lblHemat.setText("Hemat " + FormatRupiah.format(cart.getTotalHemat()));
        perbaruiStatusAnggaran();
    }

    private void perbaruiStatusAnggaran() {
        if (!budget.isAktif()) {
            lblBudget.setText(" ");
            return;
        }
        long total = cart.getTotal();
        SmartBudget.Status status = budget.hitungStatus(total);
        String teks;
        Color warna;
        switch (status) {
            case TERLAMPAUI:
                teks = "TERLAMPAUI: melebihi anggaran " + FormatRupiah.format(-budget.sisa(total));
                warna = MERAH;
                break;
            case MENDEKATI:
                teks = "MENDEKATI: " + budget.persenTerpakai(total) + "% terpakai, sisa "
                        + FormatRupiah.format(budget.sisa(total));
                warna = ORANYE;
                break;
            default:
                teks = "AMAN: " + budget.persenTerpakai(total) + "% terpakai, sisa "
                        + FormatRupiah.format(budget.sisa(total));
                warna = HIJAU;
                break;
        }
        lblBudget.setText(teks);
        lblBudget.setForeground(warna);
    }

    private String namaBaris(ItemKeranjang i) {
        String nama = i.getProduk().getNama();
        if (i.getJenisDiskon() == ItemKeranjang.JenisDiskon.FRESHSAVE) {
            nama += "  [FreshSave -" + i.getPersenFreshSave() + "%]";
        } else if (i.getJenisDiskon() == ItemKeranjang.JenisDiskon.MEMBER) {
            nama += "  [Member -" + i.getPersenDiskon() + "%]";
        }
        return nama;
    }

    private String jumlahTeks(ItemKeranjang i) {
        String angka = formatJumlah.format(i.getJumlah());
        return i.getProduk().isPerKg() ? angka + " kg" : angka;
    }

    private void tampilkanError(String pesan, Exception ex) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                pesan + ": " + ex.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE));
    }
}
