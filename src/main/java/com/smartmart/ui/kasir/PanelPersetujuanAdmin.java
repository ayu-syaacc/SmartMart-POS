package com.smartmart.ui.kasir;

import com.smartmart.ui.kasir.UiKit.Ikon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class PanelPersetujuanAdmin extends javax.swing.JPanel {

    public PanelPersetujuanAdmin() {
        UiKit.pasangFont();
        initComponents();
        setOpaque(false);
        setBorder(new EmptyBorder(34, 36, 28, 36));
        pnlTombol.setOpaque(false);
        btnBatal.setPreferredSize(new Dimension(100, 52));
        btnOk.setPreferredSize(new Dimension(100, 52));
        btnOk.setFont(UiKit.font(java.awt.Font.BOLD, 14));
    }

    public static PanelPersetujuanAdmin untukRetur(String ringkasan) {
        PanelPersetujuanAdmin p = new PanelPersetujuanAdmin();
        p.konfigurasi(Ikon.LOCK, UiKit.GREEN, UiKit.GREEN_SOFT, "Persetujuan Admin", ringkasan,
                null, new String[]{"Dialog ini juga dipakai untuk void transaksi.",
                    "Percobaan salah tercatat di log aktivitas."}, "Setujui", null);
        return p;
    }

    public static PanelPersetujuanAdmin untukVoid(String ringkasan) {
        PanelPersetujuanAdmin p = new PanelPersetujuanAdmin();
        p.konfigurasi(Ikon.TRASH, UiKit.RED, UiKit.RED_SOFT, "Konfirmasi Void", ringkasan,
                new String[]{"Stok kembali dan poin disesuaikan.", "Tindakan ini tercatat di log."},
                null, "Void", "Dialog password sama dengan SmartReturn");
        return p;
    }

    private void konfigurasi(Ikon ikon, Color aksen, Color aksenSoft, String judul, String ringkasan,
                             String[] catAtas, String[] catBawah, String teksOk, String footer) {
        lblIkon.setIcon(UiKit.ikonBulatIcon(ikon, 64, aksenSoft, aksen));
        lblJudulDlg.setText(judul);
        lblSubDlg.setText(ringkasan);
        ((UiKit.PasswordBox) fldPwd).setGaris(aksen);
        btnOk.setText(teksOk);
        ((UiKit.RoundButton) btnOk).setWarna(aksen);
        isiCatatan(pnlCatAtas, catAtas, UiKit.RED_TEXT);
        isiCatatan(pnlCatBawah, catBawah, UiKit.AMBER_TEXT);
        lblFooter.setText(footer == null ? " " : footer);
        lblFooter.setVisible(footer != null);
        susunUlang();
    }

    private void susunUlang() {
        JComponent[] urut = {lblIkon, lblJudulDlg, lblSubDlg, pnlCatAtas, lblPwd, fldPwd, pnlCatBawah,
            lblGalat, pnlTombol, lblFooter};
        int[] jarak = {14, 4, 22, 16, 6, 14, 14, 8, 14, 0};
        removeAll();
        for (int i = 0; i < urut.length; i++) {
            if (!urut[i].isVisible()) continue;
            add(urut[i]);
            if (jarak[i] > 0) add(javax.swing.Box.createVerticalStrut(jarak[i]));
        }
        UiKit.susunVertikal(this);
        setPreferredSize(null);
        Dimension d = getPreferredSize();
        setPreferredSize(new Dimension(430, d.height + 5));
    }

    private void isiCatatan(javax.swing.JPanel kotak, String[] baris, Color warna) {
        kotak.removeAll();
        kotak.setVisible(baris != null);
        if (baris == null) return;
        kotak.setLayout(new java.awt.GridLayout(baris.length, 1, 0, 1));
        kotak.setBorder(new EmptyBorder(12, 16, 12, 16));
        for (String b : baris) {
            kotak.add(UiKit.lbl(b, java.awt.Font.PLAIN, 12, warna));
        }
    }

    public javax.swing.JPasswordField getFieldPassword() { return ((UiKit.PasswordBox) fldPwd).getField(); }
    public JLabel getLabelGalat() { return lblGalat; }
    public javax.swing.JButton getTombolBatal() { return btnBatal; }
    public javax.swing.JButton getTombolOk() { return btnOk; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(0, 40, 20, 28));
        g2.fill(new RoundRectangle2D.Double(0, 5, w, h - 5, 32, 32));
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(0, 0, w, h - 5, 32, 32));
        g2.dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // @SuppressWarnings("unchecked")
    private void initComponents() {

        lblIkon = new javax.swing.JLabel();
        lblJudulDlg = new javax.swing.JLabel();
        lblSubDlg = new javax.swing.JLabel();
        pnlCatAtas = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.RED_SOFT, 14);
        lblPwd = new javax.swing.JLabel();
        fldPwd = new com.smartmart.ui.kasir.UiKit.PasswordBox(com.smartmart.ui.kasir.UiKit.GREEN);
        pnlCatBawah = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.AMBER_BG, 14);
        lblGalat = new javax.swing.JLabel();
        pnlTombol = new javax.swing.JPanel();
        btnBatal = com.smartmart.ui.kasir.UiKit.tombolAbu("Batal");
        btnOk = new com.smartmart.ui.kasir.UiKit.RoundButton("Setujui", null, com.smartmart.ui.kasir.UiKit.GREEN, java.awt.Color.WHITE, null);
        lblFooter = new javax.swing.JLabel();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));
        lblIkon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(lblIkon);

        lblJudulDlg.setText("Persetujuan Admin");
        lblJudulDlg.setFont(new java.awt.Font("Poppins", 1, 22)); // NOI18N
        lblJudulDlg.setForeground(new java.awt.Color(19, 38, 29));
        lblJudulDlg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(lblJudulDlg);

        lblSubDlg.setText("Retur ...");
        lblSubDlg.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblSubDlg.setForeground(new java.awt.Color(107, 122, 114));
        lblSubDlg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(lblSubDlg);

        pnlCatAtas.setLayout(new java.awt.GridLayout(2, 1));
        add(pnlCatAtas);

        lblPwd.setText("Password admin");
        lblPwd.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblPwd.setForeground(new java.awt.Color(107, 122, 114));
        add(lblPwd);

        add(fldPwd);

        pnlCatBawah.setLayout(new java.awt.GridLayout(2, 1));
        add(pnlCatBawah);

        lblGalat.setText(" ");
        lblGalat.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        lblGalat.setForeground(new java.awt.Color(200, 22, 69));
        add(lblGalat);

        pnlTombol.setLayout(new java.awt.GridLayout(1, 2, 14, 0));
        pnlTombol.add(btnBatal);

        pnlTombol.add(btnOk);

        add(pnlTombol);

        lblFooter.setText(" ");
        lblFooter.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        lblFooter.setForeground(new java.awt.Color(107, 122, 114));
        lblFooter.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(lblFooter);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBatal;
    private javax.swing.JButton btnOk;
    private javax.swing.JPanel fldPwd;
    private javax.swing.JLabel lblFooter;
    private javax.swing.JLabel lblGalat;
    private javax.swing.JLabel lblIkon;
    private javax.swing.JLabel lblJudulDlg;
    private javax.swing.JLabel lblPwd;
    private javax.swing.JLabel lblSubDlg;
    private javax.swing.JPanel pnlCatAtas;
    private javax.swing.JPanel pnlCatBawah;
    private javax.swing.JPanel pnlTombol;
    // End of variables declaration//GEN-END:variables
}
