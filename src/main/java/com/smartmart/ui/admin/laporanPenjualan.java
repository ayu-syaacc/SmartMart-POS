package com.smartmart.ui.admin;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class laporanPenjualan extends javax.swing.JFrame {

    private JComboBox<String> cmbPeriode;
    private JLabel lblPendapatan, lblTransaksi, lblRataRata, lblKerugian, lblSelisih;
    private JLabel lblLossTotal, lblLossKedaluwarsa, lblLossRusak, lblLossHilang;
    private JTable tblTerlaris, tblTerendah, tblKasir;
    private GrafikPendapatanPanel panelGrafik;

    public laporanPenjualan() {
        initComponents();
        buildCustomUI();
        setLocationRelativeTo(null);
        muatDataLaporan(0); // Default: Hari ini / 7 hari sesuai mockup
    }

    private void buildCustomUI() {
        setTitle("SmartMart POS - Laporan Penjualan");
        setSize(1280, 780);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(243, 246, 244));

        // 1. SIDEBAR KIRI
        root.add(createSidebar(), BorderLayout.WEST);

        // 2. KONTEN UTAMA KANAN
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // HEADER ATAS
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        JLabel lblTitle = new JLabel("Laporan Penjualan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(20, 40, 31));

        JLabel lblSub = new JLabel("SalesReport: pendapatan, produk terlaris, rekap kasir, dan kerugian");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(107, 124, 115));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        // Filter Periode + Info Waktu di Kanan
        JPanel filterBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        filterBox.setOpaque(false);

        JLabel lblStatus = new JLabel("Rab, 04 Nov 2026 22:59  ● Online   ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(new Color(15, 145, 88));

        cmbPeriode = new JComboBox<>(new String[]{"Hari ini", "7 hari", "Bulan ini"});
        cmbPeriode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbPeriode.setSelectedIndex(1); // Default 7 hari sesuai grafik Kam-Rab
        cmbPeriode.addActionListener(e -> muatDataLaporan(cmbPeriode.getSelectedIndex()));

        JButton btnEkspor = new JButton("Ekspor Laporan");
        btnEkspor.setBackground(new Color(10, 110, 80));
        btnEkspor.setForeground(Color.WHITE);
        btnEkspor.setFocusPainted(false);
        btnEkspor.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Laporan periode '" + cmbPeriode.getSelectedItem() + "' berhasil diekspor!",
                "Ekspor Berhasil", JOptionPane.INFORMATION_MESSAGE));

        filterBox.add(lblStatus);
        filterBox.add(cmbPeriode);
        filterBox.add(btnEkspor);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(filterBox, BorderLayout.EAST);
        mainContent.add(headerPanel, BorderLayout.NORTH);

        // BODY TENGAH (Scrollable agar aman di layar kecil)
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);

        // BARIS 1: 4 KARTU RINGKASAN ATAS
        bodyPanel.add(createSummaryCards());
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // BARIS 2: GRAFIK PENDAPATAN PER HARI (KIRI) + KERUGIAN LOSSGUARD (KANAN)
        JPanel midRow = new JPanel(new BorderLayout(15, 0));
        midRow.setOpaque(false);
        midRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        midRow.setPreferredSize(new Dimension(0, 240));

        panelGrafik = new GrafikPendapatanPanel();
        midRow.add(panelGrafik, BorderLayout.CENTER);
        midRow.add(createLossGuardPanel(), BorderLayout.EAST);

        bodyPanel.add(midRow);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // BARIS 3: 3 TABEL BAWAH (Produk Terlaris, Produk Terendah, Rekap per Kasir)
        bodyPanel.add(createBottomTables());

        mainContent.add(bodyPanel, BorderLayout.CENTER);
        root.add(mainContent, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBackground(new Color(10, 110, 80));

        JPanel topMenu = new JPanel();
        topMenu.setLayout(new BoxLayout(topMenu, BoxLayout.Y_AXIS));
        topMenu.setOpaque(false);
        topMenu.setBorder(BorderFactory.createEmptyBorder(22, 16, 20, 16));

        JLabel brand = new JLabel("SmartMart POS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brand.setForeground(Color.WHITE);
        topMenu.add(brand);
        topMenu.add(Box.createRigidArea(new Dimension(0, 25)));

        String[] menus = {"Dashboard", "Persediaan", "ReceiveCheck", "StockAlert", "LossGuard", "FreshSave", "Laporan", "Pegawai", "Pengaturan"};
        for (String m : menus) {
            JButton btn = new JButton(m);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);

            if (m.equals("Laporan")) {
                btn.setOpaque(true);
                btn.setBackground(new Color(7, 82, 60));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
            topMenu.add(btn);
            topMenu.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        JPanel bottomUser = new JPanel(new GridLayout(3, 1, 2, 4));
        bottomUser.setOpaque(false);
        bottomUser.setBorder(BorderFactory.createEmptyBorder(15, 16, 20, 16));

        JLabel lblAdmin = new JLabel("Admin01");
        lblAdmin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblAdmin.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel("Administrator");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(191, 227, 203));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.WHITE);
        btnLogout.setForeground(new Color(10, 110, 80));

        bottomUser.add(lblAdmin);
        bottomUser.add(lblRole);
        bottomUser.add(btnLogout);

        sidebar.add(topMenu, BorderLayout.NORTH);
        sidebar.add(bottomUser, BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel createSummaryCards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 15, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        lblPendapatan = new JLabel("Rp31.640.000");
        lblTransaksi = new JLabel("612");
        lblRataRata = new JLabel("Rata-rata Rp51.700");
        lblKerugian = new JLabel("Rp1.184.500");
        lblSelisih = new JLabel("Rp390.000");

        grid.add(makeStatCard("Pendapatan bersih", lblPendapatan, new JLabel("PAID dikurangi refund retur"), new Color(15, 145, 88)));
        grid.add(makeStatCard("Jumlah transaksi", lblTransaksi, lblRataRata, new Color(20, 40, 31)));
        grid.add(makeStatCard("Total kerugian", lblKerugian, new JLabel("Dari LossGuard"), new Color(192, 20, 60)));
        grid.add(makeStatCard("Selisih penerimaan", lblSelisih, new JLabel("Ditampilkan terpisah"), new Color(193, 87, 12)));

        return grid;
    }

    private JPanel makeStatCard(String judul, JLabel nilaiUtama, JLabel subKeterangan, Color warnaNilai) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 230), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblTop = new JLabel(judul);
        lblTop.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTop.setForeground(new Color(107, 124, 115));

        nilaiUtama.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nilaiUtama.setForeground(warnaNilai);

        subKeterangan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subKeterangan.setForeground(new Color(130, 145, 138));

        card.add(lblTop, BorderLayout.NORTH);
        card.add(nilaiUtama, BorderLayout.CENTER);
        card.add(subKeterangan, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createLossGuardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(310, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 230), 1),
                BorderFactory.createEmptyBorder(15, 16, 15, 16)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Kerugian (LossGuard)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLossTotal = new JLabel("Rp1,18jt");
        lblLossTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLossTotal.setForeground(new Color(192, 20, 60));
        top.add(title, BorderLayout.WEST);
        top.add(lblLossTotal, BorderLayout.EAST);

        JPanel list = new JPanel(new GridLayout(3, 1, 0, 8));
        list.setOpaque(false);

        lblLossKedaluwarsa = new JLabel("Rp651.000");
        lblLossRusak = new JLabel("Rp355.400");
        lblLossHilang = new JLabel("Rp178.100");

        list.add(makeLossRow("● Kedaluwarsa", lblLossKedaluwarsa, new Color(192, 20, 60)));
        list.add(makeLossRow("● Rusak", lblLossRusak, new Color(234, 88, 12)));
        list.add(makeLossRow("● Hilang", lblLossHilang, new Color(107, 124, 115)));

        JLabel note = new JLabel("Admin dapat ekspor laporan per periode");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(Color.GRAY);

        panel.add(top, BorderLayout.NORTH);
        panel.add(list, BorderLayout.CENTER);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel makeLossRow(String kategori, JLabel nominal, Color dotColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(248, 250, 249));
        row.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel lblKat = new JLabel(kategori);
        lblKat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblKat.setForeground(dotColor);

        nominal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nominal.setForeground(new Color(20, 40, 31));

        row.add(lblKat, BorderLayout.WEST);
        row.add(nominal, BorderLayout.EAST);
        return row;
    }

    private JPanel createBottomTables() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 15, 0));
        grid.setOpaque(false);

        tblTerlaris = new JTable();
        tblTerendah = new JTable();
        tblKasir = new JTable();

        grid.add(wrapTableCard("Produk terlaris", tblTerlaris));
        grid.add(wrapTableCard("Produk terendah", tblTerendah));
        grid.add(wrapTableCard("Rekap per kasir", tblKasir));
        return grid;
    }

    private JPanel wrapTableCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 230), 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(20, 40, 31));

        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        card.add(lbl, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private String formatRupiah(long angka) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        nf.setMaximumFractionDigits(0);
        return nf.format(angka);
    }

    private void muatDataLaporan(int indexPeriode) {
        // Simulasi pergantian data berdasarkan filter: 0 = Hari ini, 1 = 7 hari (Mockup), 2 = Bulan ini
        switch (indexPeriode) {
            case 0 -> {
                lblPendapatan.setText(formatRupiah(4850000));
                lblTransaksi.setText("94");
                lblRataRata.setText("Rata-rata " + formatRupiah(51595));
                lblKerugian.setText(formatRupiah(125000));
                lblSelisih.setText(formatRupiah(45000));
                lblLossTotal.setText("Rp125rb");
                lblLossKedaluwarsa.setText(formatRupiah(75000));
                lblLossRusak.setText(formatRupiah(30000));
                lblLossHilang.setText(formatRupiah(20000));
                panelGrafik.setData(new int[]{40, 55, 48, 62, 70, 65, 78});
            }
            case 2 -> {
                lblPendapatan.setText(formatRupiah(128400000));
                lblTransaksi.setText("2480");
                lblRataRata.setText("Rata-rata " + formatRupiah(51774));
                lblKerugian.setText(formatRupiah(4320000));
                lblSelisih.setText(formatRupiah(1120000));
                lblLossTotal.setText("Rp4,32jt");
                lblLossKedaluwarsa.setText(formatRupiah(2410000));
                lblLossRusak.setText(formatRupiah(1290000));
                lblLossHilang.setText(formatRupiah(620000));
                panelGrafik.setData(new int[]{65, 72, 68, 85, 92, 80, 95});
            }
            default -> {
                // DATA PERSIS MOCKUP HALAMAN 3
                lblPendapatan.setText("Rp31.640.000");
                lblTransaksi.setText("612");
                lblRataRata.setText("Rata-rata Rp51.700");
                lblKerugian.setText("Rp1.184.500");
                lblSelisih.setText("Rp390.000");
                lblLossTotal.setText("Rp1,18jt");
                lblLossKedaluwarsa.setText("Rp651.000");
                lblLossRusak.setText("Rp355.400");
                lblLossHilang.setText("Rp178.100");
                panelGrafik.setData(new int[]{52, 72, 63, 84, 98, 79, 90});
            }
        }

        // TABEL PRODUK TERLARIS (Sesuai Mockup)
        DefaultTableModel mTerlaris = new DefaultTableModel(new String[]{"Produk", "Terjual"}, 0);
        mTerlaris.addRow(new Object[]{"Susu UHT 1L", "412 pcs"});
        mTerlaris.addRow(new Object[]{"Roti Tawar", "356 pcs"});
        mTerlaris.addRow(new Object[]{"Yoghurt Plain", "240 pcs"});
        mTerlaris.addRow(new Object[]{"Pisang", "98 kg"});
        tblTerlaris.setModel(mTerlaris);

        // TABEL PRODUK TERENDAH (Sesuai Mockup)
        DefaultTableModel mTerendah = new DefaultTableModel(new String[]{"Produk", "Terjual"}, 0);
        mTerendah.addRow(new Object[]{"Tepung Premium", "4 pcs"});
        mTerendah.addRow(new Object[]{"Selai Kacang", "6 pcs"});
        mTerendah.addRow(new Object[]{"Teh Celup 25", "7 pcs"});
        mTerendah.addRow(new Object[]{"Mie Instan Pedas", "9 pcs"});
        tblTerendah.setModel(mTerendah);

        // TABEL REKAP PER KASIR (Sesuai Mockup)
        DefaultTableModel mKasir = new DefaultTableModel(new String[]{"Kasir", "Transaksi", "Total"}, 0);
        mKasir.addRow(new Object[]{"Kasir01", "318 transaksi", "Rp16,4jt"});
        mKasir.addRow(new Object[]{"Kasir02", "294 transaksi", "Rp15,2jt"});
        tblKasir.setModel(mKasir);
    }

    /**
     * Komponen Grafik Garis Kustom "Pendapatan per hari" (Kam - Rab)
     */
    private static class GrafikPendapatanPanel extends JPanel {
        private int[] values = {52, 72, 63, 84, 98, 79, 90};
        private final String[] days = {"Kam", "Jum", "Sab", "Min", "Sen", "Sel", "Rab"};

        public GrafikPendapatanPanel() {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 230), 1),
                    BorderFactory.createEmptyBorder(15, 18, 12, 18)
            ));
        }

        public void setData(int[] newValues) {
            this.values = newValues;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(new Color(20, 40, 31));
            g2.drawString("Pendapatan per hari", 18, 26);

            int padLeft = 28, padRight = 28, padTop = 48, padBottom = 32;
            int w = getWidth() - padLeft - padRight;
            int h = getHeight() - padTop - padBottom;

            // Garis grid horizontal tipis
            g2.setColor(new Color(235, 240, 238));
            for (int i = 0; i <= 4; i++) {
                int y = padTop + (h * i / 4);
                g2.drawLine(padLeft, y, padLeft + w, y);
            }

            if (values == null || values.length == 0) return;

            int stepX = w / (values.length - 1);
            int[] px = new int[values.length];
            int[] py = new int[values.length];

            for (int i = 0; i < values.length; i++) {
                px[i] = padLeft + i * stepX;
                py[i] = padTop + h - (int) ((values[i] / 100.0) * h);
            }

            // Area arsiran lembut di bawah garis
            Path2D area = new Path2D.Double();
            area.moveTo(px[0], padTop + h);
            for (int i = 0; i < values.length; i++) {
                area.lineTo(px[i], py[i]);
            }
            area.lineTo(px[values.length - 1], padTop + h);
            area.closePath();
            g2.setColor(new Color(15, 145, 88, 28));
            g2.fill(area);

            // Garis hijau utama
            g2.setColor(new Color(15, 145, 88));
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < values.length - 1; i++) {
                g2.drawLine(px[i], py[i], px[i + 1], py[i + 1]);
            }

            // Titik & label hari
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            for (int i = 0; i < values.length; i++) {
                g2.setColor(new Color(15, 145, 88));
                g2.fill(new Ellipse2D.Double(px[i] - 4.5, py[i] - 4.5, 9, 9));

                g2.setColor(new Color(107, 124, 115));
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(days[i]);
                g2.drawString(days[i], px[i] - labelW / 2, padTop + h + 20);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 650, Short.MAX_VALUE)
        );
        pack();
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(laporanPenjualan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new laporanPenjualan().setVisible(true));
    }
}