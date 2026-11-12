package com.smartmart.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Layar StockAlert: ringkasan 5 kategori, tabel status lot, dan sidebar tindakan admin.
 * Versi mandiri (Standalone) agar bebas error kompilasi.
 */
public class StockAlert extends JFrame {

    // --- Warna & Tema UiKit ---
    private static final Color BG = new Color(0xF3F6F4);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT = new Color(0x14281F);
    private static final Color MUTED = new Color(0x6B7C73);
    private static final Color BORDER = new Color(0xE2E8E5);
    private static final Color PRIMARY = new Color(0x0A6E50);
    private static final Color DANGER = new Color(0xDC2626);
    private static final Color ROW_ALT = new Color(0xF8FAFC);

    // --- Model Internal Lot ---
    private static class ItemLot {
        String produk, lot, status;
        int sisaHari, jumlah;

        ItemLot(String produk, String lot, int sisaHari, int jumlah, String status) {
            this.produk = produk;
            this.lot = lot;
            this.sisaHari = sisaHari;
            this.jumlah = jumlah;
            this.status = status;
        }
    }

    private String pelaku = "Admin01";
    private final List<ItemLot> data = new ArrayList<>();
    private final JLabel[] lblRingkasan = new JLabel[5];
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Produk", "Lot", "Sisa masa", "Jumlah", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    // Sidebar komponen
    private final JPanel alertBox = new JPanel();
    private final JLabel lblKondisi = label("Lot kedaluwarsa", Font.BOLD, 13f, DANGER);
    private final JLabel lblDetail = label(" ", Font.PLAIN, 13f, TEXT);
    private final JLabel lblBlokir = label("Penjualan diblokir otomatis.", Font.PLAIN, 12f, DANGER);
    private final JButton btnBuang = dangerBtn("Buang lot");
    private final JButton btnManual = outlineBtn("Kurangi stok manual");
    private final JPanel formManual = new JPanel();
    private final JLabel lblProdukLot = label(" ", Font.BOLD, 13f, TEXT);
    private final JRadioButton rbRusak = new JRadioButton("Rusak", true);
    private final JRadioButton rbHilang = new JRadioButton("Hilang");
    private final JSpinner spJumlah = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
    private final JTextField tfAlasan = new JTextField(10);
    private final JButton btnSimpan = primaryBtn("Simpan");

    public StockAlert() {
        setTitle("SmartMart POS - StockAlert");
        setSize(1180, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        String waktu = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm", new Locale("id", "ID")));
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(label("▲ StockAlert", Font.BOLD, 24f, TEXT));
        header.add(label("Peringatan stok menipis dan status kedaluwarsa per lot  |  " + waktu, Font.PLAIN, 13f, MUTED));
        root.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildSummary(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(buildTable(), BorderLayout.CENTER);
        center.add(buildSidebar(), BorderLayout.EAST);
        body.add(center, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        setContentPane(root);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) pilih();
        });

        muatDataAwal();
    }

    // ---------- Bangun UI ----------
    private JPanel buildSummary() {
        String[] judul = {"Aman (>7 hr)", "Perhatian (4-7 hr)", "Prioritas (2-3 hr)", "Segera (1 hr)", "Kedaluwarsa (0 hr)"};
        Color[] bg = {new Color(0xDCFCE7), new Color(0xFEF08A), new Color(0xFED7AA), new Color(0xFECACA), new Color(0xEF4444)};
        Color[] fg = {new Color(0x166534), new Color(0x854D0E), new Color(0x9A3412), new Color(0x991B1B), Color.WHITE};

        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
        grid.setOpaque(false);
        for (int i = 0; i < 5; i++) {
            JPanel p = new JPanel();
            p.setBackground(bg[i]);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(14, 16, 14, 16));

            JLabel t = label(judul[i], Font.PLAIN, 12f, fg[i]);
            lblRingkasan[i] = label("0 lot", Font.BOLD, 24f, fg[i]);
            t.setAlignmentX(LEFT_ALIGNMENT);
            lblRingkasan[i].setAlignmentX(LEFT_ALIGNMENT);

            p.add(t);
            p.add(Box.createVerticalStrut(4));
            p.add(lblRingkasan[i]);
            grid.add(p);
        }
        return grid;
    }

    private JPanel buildTable() {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setCellRenderer(new BadgeCellRenderer());

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)));
        card.add(label("Status lot", Font.BOLD, 15f, TEXT), BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(CARD_BG);
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(16, 18, 16, 18)));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(310, 0));

        addComp(side, label("Tindakan admin", Font.BOLD, 16f, TEXT), 12);

        alertBox.setLayout(new BoxLayout(alertBox, BoxLayout.Y_AXIS));
        alertBox.setBackground(new Color(0xFEF2F2));
        alertBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xFECACA), 1),
                new EmptyBorder(10, 12, 10, 12)));
        alertBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        lblKondisi.setAlignmentX(LEFT_ALIGNMENT);
        lblDetail.setAlignmentX(LEFT_ALIGNMENT);
        lblBlokir.setAlignmentX(LEFT_ALIGNMENT);
        alertBox.add(lblKondisi);
        alertBox.add(Box.createVerticalStrut(4));
        alertBox.add(lblDetail);
        alertBox.add(Box.createVerticalStrut(4));
        alertBox.add(lblBlokir);
        addComp(side, alertBox, 12);

        addComp(side, btnBuang, 8);
        addComp(side, btnManual, 12);

        // Form kurangi stok manual
        formManual.setLayout(new BoxLayout(formManual, BoxLayout.Y_AXIS));
        formManual.setOpaque(false);
        formManual.setVisible(false);
        formManual.setAlignmentX(LEFT_ALIGNMENT);
        formManual.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(new EmptyBorder(12, 0, 0, 0));

        addComp(inner, label("Produk / lot", Font.PLAIN, 12f, MUTED), 2);
        addComp(inner, lblProdukLot, 8);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbRusak);
        bg.add(rbHilang);
        rbRusak.setOpaque(false);
        rbHilang.setOpaque(false);

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radios.setOpaque(false);
        radios.add(rbRusak);
        radios.add(Box.createHorizontalStrut(14));
        radios.add(rbHilang);
        addComp(inner, radios, 8);

        addComp(inner, label("Jumlah", Font.PLAIN, 12f, MUTED), 2);
        spJumlah.setMaximumSize(new Dimension(120, 32));
        addComp(inner, spJumlah, 8);

        addComp(inner, label("Alasan (wajib)", Font.PLAIN, 12f, MUTED), 2);
        tfAlasan.setToolTipText("Contoh: Kemasan sobek");
        addComp(inner, tfAlasan, 12);
        addComp(inner, btnSimpan, 0);

        formManual.add(inner);
        side.add(formManual);

        side.add(Box.createVerticalGlue());
        JLabel note = label("Tercatat otomatis di LossGuard (" + pelaku + ")", Font.ITALIC, 12f, MUTED);
        note.setAlignmentX(LEFT_ALIGNMENT);
        side.add(Box.createVerticalStrut(10));
        side.add(note);

        btnManual.addActionListener(e -> {
            formManual.setVisible(!formManual.isVisible());
            side.revalidate();
            side.repaint();
        });
        btnBuang.addActionListener(e -> buangLot());
        btnSimpan.addActionListener(e -> simpanManual());

        return side;
    }

    private static void addComp(JPanel parent, JComponent c, int gapBawah) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        if (c instanceof JButton || c instanceof JTextField) {
            c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }
        parent.add(c);
        if (gapBawah > 0) parent.add(Box.createVerticalStrut(gapBawah));
    }

    // ---------- Data & Logika ----------
    private void muatDataAwal() {
        data.clear();
        data.add(new ItemLot("Keju Slice", "E-019", 0, 6, "KEDALUWARSA"));
        data.add(new ItemLot("Yoghurt Plain", "A-231", 1, 30, "SEGERA"));
        data.add(new ItemLot("Roti Tawar", "B-118", 2, 37, "PRIORITAS"));
        data.add(new ItemLot("Salad Sayur", "C-077", 3, 18, "PRIORITAS"));
        data.add(new ItemLot("Daging Ayam", "D-042", 5, 24, "PERHATIAN"));
        data.add(new ItemLot("Bayam Ikat", "C-081", 6, 15, "PERHATIAN"));
        data.add(new ItemLot("Susu UHT 1L", "F-304", 120, 8, "MENIPIS"));

        updateKartuRingkasan();
        isiTabel(0);
    }

    private void updateKartuRingkasan() {
        int aman = 112, perhatian = 0, prioritas = 0, segera = 0, kedaluwarsa = 0;
        for (ItemLot item : data) {
            switch (item.status) {
                case "PERHATIAN" -> perhatian++;
                case "PRIORITAS" -> prioritas++;
                case "SEGERA" -> segera++;
                case "KEDALUWARSA" -> kedaluwarsa++;
            }
        }
        lblRingkasan[0].setText(aman + " lot");
        lblRingkasan[1].setText(perhatian + " lot");
        lblRingkasan[2].setText(prioritas + " lot");
        lblRingkasan[3].setText(segera + " lot");
        lblRingkasan[4].setText(kedaluwarsa + " lot");
    }

    private void isiTabel(int pilihIndex) {
        model.setRowCount(0);
        for (ItemLot a : data) {
            model.addRow(new Object[]{a.produk, a.lot, a.sisaHari + " hari", a.jumlah, a.status});
        }
        if (!data.isEmpty()) {
            int idx = Math.max(0, Math.min(pilihIndex, data.size() - 1));
            table.setRowSelectionInterval(idx, idx);
        } else {
            pilih();
        }
    }

    private ItemLot terpilih() {
        int r = table.getSelectedRow();
        return (r >= 0 && r < data.size()) ? data.get(r) : null;
    }

    private void pilih() {
        ItemLot a = terpilih();
        boolean ada = a != null;
        btnBuang.setEnabled(ada && "KEDALUWARSA".equals(a.status));
        btnManual.setEnabled(ada);
        btnSimpan.setEnabled(ada);

        if (!ada) {
            lblKondisi.setText("Tidak ada lot");
            lblDetail.setText(" ");
            lblBlokir.setVisible(false);
            lblProdukLot.setText(" ");
            return;
        }

        boolean expired = "KEDALUWARSA".equals(a.status);
        lblKondisi.setText(expired ? "Lot kedaluwarsa" : "Lot " + a.status.toLowerCase());
        lblKondisi.setForeground(expired || "SEGERA".equals(a.status) ? DANGER : TEXT);
        lblDetail.setText(a.produk + ", lot " + a.lot + ", sisa " + a.jumlah);
        lblBlokir.setVisible(expired);
        alertBox.setBackground(expired ? new Color(0xFEF2F2) : ROW_ALT);
        lblProdukLot.setText(a.produk + " " + a.lot);

        int max = Math.max(1, a.jumlah);
        spJumlah.setModel(new SpinnerNumberModel(Math.min(2, max), 1, max, 1));
    }

    private void buangLot() {
        ItemLot a = terpilih();
        if (a == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Buang seluruh sisa " + a.jumlah + " unit " + a.produk + " (lot " + a.lot + ")?",
                "Buang lot", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        terapkan(a, "KEDALUWARSA", a.jumlah, "Lot kedaluwarsa dibuang oleh admin");
    }

    private void simpanManual() {
        ItemLot a = terpilih();
        if (a == null) return;
        String alasan = tfAlasan.getText().trim();
        if (alasan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alasan wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
            tfAlasan.requestFocus();
            return;
        }
        String jenis = rbHilang.isSelected() ? "HILANG" : "RUSAK";
        if (terapkan(a, jenis, (Integer) spJumlah.getValue(), alasan)) {
            tfAlasan.setText("");
        }
    }

    private boolean terapkan(ItemLot a, String jenis, int n, String alasan) {
        int idx = Math.max(0, table.getSelectedRow());
        a.jumlah -= n;
        if (a.jumlah <= 0) {
            data.remove(a);
        }
        updateKartuRingkasan();
        isiTabel(idx);
        JOptionPane.showMessageDialog(this,
                "Pengurangan " + n + " unit (" + jenis + ") tersimpan dan tercatat di LossGuard.\nAlasan: " + alasan,
                "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    // ---------- Helper Komponen UI ----------
    private static JLabel label(String txt, int style, float size, Color color) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", style, (int) size));
        l.setForeground(color);
        return l;
    }

    private static JButton primaryBtn(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    private static JButton dangerBtn(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(DANGER);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    private static JButton outlineBtn(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(Color.WHITE);
        b.setForeground(TEXT);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    private static class BadgeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String st = String.valueOf(value);
            if (!isSelected) {
                switch (st) {
                    case "KEDALUWARSA" -> { lbl.setBackground(new Color(0xFEE2E2)); lbl.setForeground(new Color(0x991B1B)); }
                    case "SEGERA" -> { lbl.setBackground(new Color(0xFFEDD5)); lbl.setForeground(new Color(0x9A3412)); }
                    case "PRIORITAS" -> { lbl.setBackground(new Color(0xFEF3C7)); lbl.setForeground(new Color(0x92400E)); }
                    case "PERHATIAN" -> { lbl.setBackground(new Color(0xFEF9C3)); lbl.setForeground(new Color(0x854D0E)); }
                    default -> { lbl.setBackground(new Color(0xE0F2FE)); lbl.setForeground(new Color(0x075985)); }
                }
            }
            return lbl;
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new StockAlert().setVisible(true));
    }
}