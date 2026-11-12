package com.smartmart.ui.admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public final class laporanPenjualan extends javax.swing.JFrame {

    public LaporanDAO laporanDAO = new LaporanDAO();

    private JLabel lblPendapatan, lblTransaksi, lblRataRata, lblKerugian;
    private JLabel lblSelisih, lblKedaluwarsa, lblRusak, lblHilang;
    private JComboBox<String> cmbPeriode;
    private JTable tabelTerlaris, tabelTerendah, tabelKasir;

    public laporanPenjualan() {
        buatKomponenLaporan();
        tampilkanLaporan();
        setLocationRelativeTo(null);
    }

    public void buatKomponenLaporan() {
        setTitle("Laporan Penjualan - SmartMart");
        setSize(1150, 750);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(new Color(245, 246, 248));

        // 1. HEADER (Judul & Filter)
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);

        JLabel lblJudul = new JLabel("Laporan Penjualan");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 22));

        cmbPeriode = new JComboBox<>(new String[]{"Hari ini", "7 hari", "Bulan ini"});
        cmbPeriode.setSelectedIndex(1);
        cmbPeriode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbPeriode.addActionListener(e -> tampilkanLaporan());

        panelHeader.add(lblJudul, BorderLayout.WEST);
        panelHeader.add(cmbPeriode, BorderLayout.EAST);

        // 2. KARTU RINGKASAN (4 KARTU SEJAJAR HORISONTAL)
        JPanel panelRingkasan = new JPanel(new GridLayout(1, 4, 12, 12));
        panelRingkasan.setOpaque(false);

        lblPendapatan = new JLabel("Rp0");
        lblTransaksi = new JLabel("0");
        lblRataRata = new JLabel("Rp0");
        lblKerugian = new JLabel("Rp0");

        panelRingkasan.add(buatKartu("Pendapatan bersih", lblPendapatan, "PAID dikurangi refund retur"));
        panelRingkasan.add(buatKartu("Jumlah transaksi", lblTransaksi, "Rata-rata -"));
        panelRingkasan.add(buatKartu("Total kerugian", lblKerugian, "Dari LossGuard"));
        panelRingkasan.add(buatKartuKerugianDetail());

        // 3. GRAFIK PENDAPATAN PER HARI
        JPanel panelGrafik = new JPanel(new BorderLayout(5, 5));
        panelGrafik.setBackground(Color.WHITE);
        panelGrafik.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel lblTitleGrafik = new JLabel("Pendapatan per hari");
        lblTitleGrafik.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelGrafik.add(lblTitleGrafik, BorderLayout.NORTH);

        JPanel areaGrafik = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Grid horizontal halus
                g2.setColor(new Color(240, 240, 240));
                g2.drawLine(0, 20, getWidth(), 20);
                g2.drawLine(0, 50, getWidth(), 50);

                // Garis Tren Hijau SmartMart
                g2.setColor(new Color(16, 185, 129));
                g2.setStroke(new BasicStroke(2.5f));

                int[] xPoints = {30, 160, 290, 420, 550, 680, 810};
                int[] yPoints = {55, 35, 45, 25, 15, 30, 20};

                for (int i = 0; i < xPoints.length - 1; i++) {
                    g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
                    g2.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
                }
                g2.fillOval(xPoints[6] - 3, yPoints[6] - 3, 6, 6);
            }
        };
        areaGrafik.setOpaque(false);
        areaGrafik.setPreferredSize(new Dimension(800, 75));
        panelGrafik.add(areaGrafik, BorderLayout.CENTER);

        // 4. 3 TABEL DATA DI BAGIAN BAWAH
        tabelTerlaris = buatTabelFormat();
        tabelTerendah = buatTabelFormat();
        tabelKasir = buatTabelFormat();

        JPanel panelTabel = new JPanel(new GridLayout(1, 3, 12, 12));
        panelTabel.setOpaque(false);

        panelTabel.add(buatContainerTabel("Produk terlaris", tabelTerlaris));
        panelTabel.add(buatContainerTabel("Produk terendah", tabelTerendah));
        panelTabel.add(buatContainerTabel("Rekap per kasir", tabelKasir));

        // 5. GABUNGKAN GRAFIK & TABEL DENGAN VERTIKAL BOX
        JPanel panelKontenBawah = new JPanel();
        panelKontenBawah.setLayout(new BoxLayout(panelKontenBawah, BoxLayout.Y_AXIS));
        panelKontenBawah.setOpaque(false);

        panelKontenBawah.add(panelGrafik);
        panelKontenBawah.add(Box.createVerticalStrut(12));
        panelKontenBawah.add(panelTabel);

        // SUSUNAN UTAMA FRAME
        mainPanel.add(panelHeader, BorderLayout.NORTH);
        
        JPanel centerWrapper = new JPanel(new BorderLayout(12, 12));
        centerWrapper.setOpaque(false);
        centerWrapper.add(panelRingkasan, BorderLayout.NORTH);
        centerWrapper.add(panelKontenBawah, BorderLayout.CENTER);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private JPanel buatKartu(String judul, JLabel lblNilai, String sub) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(200, 95));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lJudul = new JLabel(judul);
        lJudul.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lJudul.setForeground(Color.GRAY);

        lblNilai.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel lSub = new JLabel(sub);
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lSub.setForeground(Color.LIGHT_GRAY);

        p.add(lJudul, BorderLayout.NORTH);
        p.add(lblNilai, BorderLayout.CENTER);
        p.add(lSub, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buatKartuKerugianDetail() {
        JPanel p = new JPanel(new GridLayout(4, 1, 1, 1));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(200, 95));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        lblSelisih = new JLabel("Selisih: Rp0");
        lblKedaluwarsa = new JLabel("• Kedaluwarsa: Rp0");
        lblRusak = new JLabel("• Rusak: Rp0");
        lblHilang = new JLabel("• Hilang: Rp0");

        Font f = new Font("Segoe UI", Font.PLAIN, 11);
        lblSelisih.setFont(f);
        lblKedaluwarsa.setFont(f);
        lblRusak.setFont(f);
        lblHilang.setFont(f);

        p.add(lblSelisih);
        p.add(lblKedaluwarsa);
        p.add(lblRusak);
        p.add(lblHilang);
        return p;
    }

    private JTable buatTabelFormat() {
        JTable t = new JTable();
        t.setRowHeight(24);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        return t;
    }

    private JPanel buatContainerTabel(String judul, JTable tabel) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(200, 250));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel l = new JLabel(judul);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane sp = new JScrollPane(tabel);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        p.add(l, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    public void tampilkanLaporan() {
        if (cmbPeriode == null) return;

        try {
            int idx = cmbPeriode.getSelectedIndex();
            LaporanDAO.Periode p = (idx == 0) ? LaporanDAO.Periode.HARI_INI :
                                  (idx == 1) ? LaporanDAO.Periode.TUJUH_HARI :
                                  LaporanDAO.Periode.BULAN_INI;

            LaporanDAO.Laporan lap = laporanDAO.ambil(p);
            if (lap != null) {
                lblPendapatan.setText(lap.pendapatanBersih);
                lblTransaksi.setText(String.valueOf(lap.jumlahTransaksi));
                lblRataRata.setText(lap.rataRata);
                lblKerugian.setText(lap.totalKerugian);

                lblSelisih.setText("Selisih penerimaan: " + lap.selisihPenerimaan);
                lblKedaluwarsa.setText("• Kedaluwarsa: " + lap.kerugianKedaluwarsa);
                lblRusak.setText("• Rusak: " + lap.kerugianRusak);
                lblHilang.setText("• Hilang: " + lap.kerugianHilang);

                // Tabel Terlaris
                DefaultTableModel mTerlaris = new DefaultTableModel(new String[]{"Produk", "Jumlah"}, 0);
                for (LaporanDAO.BarisProduk b : lap.terlaris) mTerlaris.addRow(new Object[]{b.nama, b.jumlah});
                tabelTerlaris.setModel(mTerlaris);

                // Tabel Terendah
                DefaultTableModel mTerendah = new DefaultTableModel(new String[]{"Produk", "Jumlah"}, 0);
                for (LaporanDAO.BarisProduk b : lap.terendah) mTerendah.addRow(new Object[]{b.nama, b.jumlah});
                tabelTerendah.setModel(mTerendah);

                // Tabel Kasir
                DefaultTableModel mKasir = new DefaultTableModel(new String[]{"Kasir", "Transaksi", "Total"}, 0);
                for (LaporanDAO.BarisKasir k : lap.kasir) mKasir.addRow(new Object[]{k.nama, k.transaksi + " transaksi", k.total});
                tabelKasir.setModel(mKasir);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil laporan: " + e.getMessage());
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new laporanPenjualan().setVisible(true));
    }
}