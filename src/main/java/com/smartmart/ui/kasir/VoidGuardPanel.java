package com.smartmart.ui.kasir;

import com.smartmart.dao.ReturDAO;
import com.smartmart.dao.RiwayatDAO;
import com.smartmart.model.Retur;
import com.smartmart.service.VoidService;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class VoidGuardPanel extends javax.swing.JPanel {

    public interface VoidListener {
        void voidSelesai(String idTransaksi);
    }

    private final VoidService service = new VoidService();
    private final RiwayatDAO riwayatDao = new RiwayatDAO();
    private boolean modeContoh;
    private static final String KET_CONTOH = "Produk salah dipindai, akan dibuat transaksi baru.";
    private String idTrx = "";
    private int total;
    private String alasan = "Salah input";
    private int idKasir;
    private DialogAdminBase.Pemeriksa pemeriksa;
    private VoidListener listener;

    public VoidGuardPanel() {
        UiKit.pasangFont();
        initComponents();
        susunTampilan();
        setMinimumSize(new Dimension(1000, 885));
    }

    public void setVoidListener(VoidListener l) { listener = l; }

    public void setIdKasir(int id) { idKasir = id; }

    public void setPemeriksa(DialogAdminBase.Pemeriksa p) { pemeriksa = p; }

    public void muatContoh() {
        modeContoh = true;
        RiwayatDAO.Baris b = new RiwayatDAO.Baris();
        b.id = "TRX-20261104-0017";
        b.waktu = LocalDate.now().atTime(22, 41);
        b.pelanggan = "Siti Rahma";
        b.total = 55150;
        b.metode = "Tunai";
        b.status = "SELESAI";
        b.poinDiperoleh = 5;
        b.kasir = "Kasir01";
        tampilkan(b, 3, true, true);
    }

    public void setTransaksi(String id) {
        modeContoh = false;
        try {
            Connection c = ReturDAO.buka();
            try {
                RiwayatDAO.Baris b = riwayatDao.ambilSatu(c, id);
                if (b == null) {
                    kosongkan();
                    lblCatatan.setText("Transaksi " + id + " tidak ditemukan.");
                    lblCatatan.setForeground(UiKit.RED_TEXT);
                    return;
                }
                int jumlahItem = riwayatDao.ambilItem(c, id).size();
                List<String> masalah = service.validasiSetelahBayar(id, "cek");
                boolean hariSama = true, belumRetur = true;
                for (String m : masalah) {
                    if (m.contains("hari transaksi yang sama")) hariSama = false;
                    if (m.contains("diretur")) belumRetur = false;
                }
                tampilkan(b, jumlahItem, hariSama, belumRetur);
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            kosongkan();
            lblCatatan.setText("Gagal membaca database: " + e.getMessage());
            lblCatatan.setForeground(UiKit.RED_TEXT);
        }
    }

    private void kosongkan() {
        idTrx = "";
        total = 0;
        vId.setText("-");
        vInfo1.setText(" ");
        vInfo2.setText(" ");
        vTotal.setText(" ");
        txtKet.setText("");
        ((UiKit.BarisValid) cekHari).set("Hari yang sama", "-", true);
        ((UiKit.BarisValid) cekRetur).set("Belum ada retur", "-", true);
        ((UiKit.Dampak) damp2).setSub("Poin disesuaikan bila ada");
        btnAjukanVoid.setEnabled(false);
    }

    private void tampilkan(RiwayatDAO.Baris b, int jumlahItem, boolean hariSama, boolean belumRetur) {
        idTrx = b.id;
        total = b.total;
        txtKet.setText(modeContoh ? KET_CONTOH : "");
        alasan = chipInput.getText();
        chipInput.setSelected(true);
        chipBatal.setSelected(false);
        chipProduk.setSelected(false);
        vId.setText(b.id);
        String jam = b.waktu == null ? "-" : String.format("%02d:%02d", b.waktu.getHour(), b.waktu.getMinute());
        String tgl = b.waktu == null ? "-" : Retur.tanggal(b.waktu.toLocalDate());
        vInfo1.setText(tgl + " " + jam + "  |  " + b.kasir + "  |  " + b.pelanggan);
        vInfo2.setText(jumlahItem + " item  |  " + b.metode);
        vTotal.setText(UiKit.rp(b.total));
        UiKit.terapkanStatus((UiKit.Badge) badgeVoid, b.status);
        ((UiKit.BarisValid) cekHari).set("Hari yang sama",
                hariSama ? "Dibayar hari ini " + jam : "Void hanya bisa pada hari yang sama", hariSama);
        ((UiKit.BarisValid) cekRetur).set("Belum ada retur",
                belumRetur ? "Tidak ada retur pada transaksi ini" : "Transaksi sudah diretur", belumRetur);
        ((UiKit.BarisValid) cekAdmin).set("Persetujuan admin", "Wajib alasan dan password admin", true);
        ((UiKit.Dampak) damp2).setSub(b.poinDiperoleh > 0 ? "Poin +" + b.poinDiperoleh + " dibatalkan" : "Tidak ada poin untuk dibatalkan");
        lblCatatan.setText("Tidak ada fitur edit: transaksi salah di-void lalu dibuat ulang.");
        lblCatatan.setForeground(UiKit.MUTED);
        btnAjukanVoid.setEnabled("SELESAI".equals(b.status) && hariSama && belumRetur);
    }

    private void susunTampilan() {
        setOpaque(true);
        setBackground(UiKit.BG);
        setBorder(new EmptyBorder(64, 16, 46, 18));
        for (JComponent c : new JComponent[]{pnlHeader, pnlJudulBlok, pnlIsi, pnlVoidAtas, pnlVoidJudul, pnlDipilihKiri,
            pnlChipsVoid, pnlDampakAtas}) {
            c.setOpaque(false);
        }
        lblJudul.setIcon(UiKit.ikonIcon(UiKit.Ikon.SHIELD, 28, UiKit.TEXT));
        lblJudul.setIconTextGap(12);
        lblSub.setBorder(new EmptyBorder(0, 40, 0, 0));
        pnlHeader.setBorder(new EmptyBorder(0, 21, 0, 0));

        pnlDipilih.setBorder(new EmptyBorder(16, 20, 16, 20));
        pnlDipilih.setPreferredSize(new Dimension(100, 104));
        vTotal.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        pnlChipsVoid.setBorder(new EmptyBorder(0, -10, 0, 0));
        pnlKotak.setBorder(new EmptyBorder(0, 0, 0, 0));
        ((UiKit.Box) pnlKotak).setBorderColor(UiKit.LINE, 2f);
        txtKet.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16));
        pnlKotak.setPreferredSize(new Dimension(100, 84));

        chipInput.setSelected(true);
        pasangAlasan(chipInput, chipBatal, chipProduk);
        pasangAlasan(chipBatal, chipInput, chipProduk);
        pasangAlasan(chipProduk, chipInput, chipBatal);

        int[] posisi = {9, 8, 7, 6, 5, 4, 3, 2, 1};
        int[] px = {8, 12, 8, 18, 8, 8, 10, 18, 14};
        for (int i = 0; i < posisi.length; i++) {
            pnlVoidAtas.add(javax.swing.Box.createVerticalStrut(px[i]), posisi[i]);
        }
        UiKit.susunVertikal(pnlVoidAtas);

        btnAjukanVoid.setPreferredSize(new Dimension(100, 58));
        btnAjukanVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { ajukanVoid(); }
        });

        pnlBiru.setBorder(new EmptyBorder(20, 22, 20, 22));
        pnlDampakAtas.add(javax.swing.Box.createVerticalStrut(10), 4);
        pnlDampakAtas.add(javax.swing.Box.createVerticalStrut(10), 3);
        pnlDampakAtas.add(javax.swing.Box.createVerticalStrut(10), 2);
        pnlDampakAtas.add(javax.swing.Box.createVerticalStrut(16), 1);
        UiKit.susunVertikal(pnlDampakAtas);
        kosongkan();
        lblCatatan.setText("Pilih transaksi dari Riwayat untuk di-void.");
    }

    private void pasangAlasan(final javax.swing.JToggleButton ini, final javax.swing.JToggleButton a,
                              final javax.swing.JToggleButton b) {
        ini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ini.setSelected(true);
                a.setSelected(false);
                b.setSelected(false);
                alasan = ini.getText();
            }
        });
    }

    private void tampilCatatan(String teks, boolean galat) {
        lblCatatan.setText(teks);
        lblCatatan.setForeground(galat ? UiKit.RED_TEXT : UiKit.MUTED);
        lblCatatan.setFont(UiKit.font(galat ? java.awt.Font.BOLD : java.awt.Font.PLAIN, 11));
    }

    private void ajukanVoid() {
        if (idTrx.isEmpty()) return;
        String ket = txtKet.getText().trim();
        String alasanLengkap = alasan + (ket.isEmpty() ? "" : " - " + ket);
        if (ket.isEmpty()) {
            tampilCatatan("Alasan wajib diisi sebelum mengajukan void.", true);
            return;
        }
        if (!modeContoh) {
            try {
                List<String> masalah = service.validasiSetelahBayar(idTrx, alasanLengkap);
                if (!masalah.isEmpty()) {
                    tampilCatatan(masalah.get(0), true);
                    return;
                }
            } catch (SQLException e) {
                tampilCatatan("Gagal membaca database: " + e.getMessage(), true);
                return;
            }
        }
        tampilCatatan("Tidak ada fitur edit: transaksi salah di-void lalu dibuat ulang.", false);
        Integer idAdmin = KonfirmasiVoidDialog.minta(this, idTrx + "  |  " + UiKit.rp(total), pemeriksa, modeContoh);
        if (idAdmin == null) return;

        if (!modeContoh) {
            try {
                service.voidSetelahBayar(idTrx, idKasir, idAdmin.intValue(), alasanLengkap);
            } catch (VoidService.DitolakException e) {
                tampilCatatan(e.getMessage(), true);
                return;
            } catch (SQLException e) {
                tampilCatatan("Void gagal disimpan: " + e.getMessage(), true);
                return;
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this, "Transaksi " + idTrx + " di-void"
                + (modeContoh ? " (data contoh, tidak disimpan)." : ".") + " Status: DIBATALKAN.");
        UiKit.terapkanStatus((UiKit.Badge) badgeVoid, "DIBATALKAN");
        btnAjukanVoid.setEnabled(false);
        if (listener != null) listener.voidSelesai(idTrx);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // @SuppressWarnings("unchecked")
    private void initComponents() {

        pnlHeader = new javax.swing.JPanel();
        pnlJudulBlok = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        lblSub = new javax.swing.JLabel();
        barInfo = new com.smartmart.ui.kasir.UiKit.BarInfo();
        pnlIsi = new javax.swing.JPanel();
        kartuTransaksi = new com.smartmart.ui.kasir.UiKit.Card();
        pnlVoidAtas = new javax.swing.JPanel();
        pnlVoidJudul = new javax.swing.JPanel();
        lblTrxDipilih = new javax.swing.JLabel();
        badgeVoid = com.smartmart.ui.kasir.UiKit.badgeStatus("SELESAI");
        pnlDipilih = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.ROW_BG, 20);
        pnlDipilihKiri = new javax.swing.JPanel();
        vId = new javax.swing.JLabel();
        vInfo1 = new javax.swing.JLabel();
        vInfo2 = new javax.swing.JLabel();
        vTotal = new javax.swing.JLabel();
        lblSyarat = new javax.swing.JLabel();
        cekHari = new com.smartmart.ui.kasir.UiKit.BarisValid("Hari yang sama", "Dibayar hari ini 22:41");
        cekRetur = new com.smartmart.ui.kasir.UiKit.BarisValid("Belum ada retur", "Tidak ada retur pada transaksi ini");
        cekAdmin = new com.smartmart.ui.kasir.UiKit.BarisValid("Persetujuan admin", "Wajib alasan dan password admin");
        lblAlasanVoid = new javax.swing.JLabel();
        pnlChipsVoid = new javax.swing.JPanel();
        chipInput = new com.smartmart.ui.kasir.UiKit.Chip("Salah input", com.smartmart.ui.kasir.UiKit.GREEN);
        chipBatal = new com.smartmart.ui.kasir.UiKit.Chip("Pelanggan batal", com.smartmart.ui.kasir.UiKit.GREEN);
        chipProduk = new com.smartmart.ui.kasir.UiKit.Chip("Salah produk", com.smartmart.ui.kasir.UiKit.GREEN);
        pnlKotak = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.FIELD_BG, 18);
        txtKet = new javax.swing.JTextArea();
        lblCatatan = new javax.swing.JLabel();
        btnAjukanVoid = com.smartmart.ui.kasir.UiKit.tombolMerah("Ajukan Void", com.smartmart.ui.kasir.UiKit.Ikon.TRASH);
        kartuDampak = new com.smartmart.ui.kasir.UiKit.Card();
        pnlDampakAtas = new javax.swing.JPanel();
        lblDampak = new javax.swing.JLabel();
        damp1 = new com.smartmart.ui.kasir.UiKit.Dampak(com.smartmart.ui.kasir.UiKit.Ikon.BOX, "Stok kembali", "Ke lot asal masing-masing barang");
        damp2 = new com.smartmart.ui.kasir.UiKit.Dampak(com.smartmart.ui.kasir.UiKit.Ikon.CARD, "Poin disesuaikan", "Poin +5 dibatalkan");
        damp3 = new com.smartmart.ui.kasir.UiKit.Dampak(com.smartmart.ui.kasir.UiKit.Ikon.DOC, "Tercatat di log", "LossGuard dan log aktivitas");
        pnlBiru = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.BLUE_BG, 20);
        lblBiru1 = new javax.swing.JLabel();
        lblBiru2 = new javax.swing.JLabel();
        lblBiru3 = new javax.swing.JLabel();
        lblBiru4 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(0, 14));
        pnlHeader.setLayout(new java.awt.BorderLayout());
        pnlJudulBlok.setLayout(new java.awt.GridLayout(2, 1));
        lblJudul.setText("VoidGuard");
        lblJudul.setFont(new java.awt.Font("Poppins", 1, 29)); // NOI18N
        lblJudul.setForeground(new java.awt.Color(19, 38, 29));
        pnlJudulBlok.add(lblJudul);

        lblSub.setText("Pembatalan transaksi yang terkontrol");
        lblSub.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblSub.setForeground(new java.awt.Color(107, 122, 114));
        pnlJudulBlok.add(lblSub);

        pnlHeader.add(pnlJudulBlok, java.awt.BorderLayout.WEST);

        pnlHeader.add(barInfo, java.awt.BorderLayout.EAST);

        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(20, 0));
        kartuTransaksi.setLayout(new java.awt.BorderLayout(0, 14));
        pnlVoidAtas.setLayout(new javax.swing.BoxLayout(pnlVoidAtas, javax.swing.BoxLayout.PAGE_AXIS));
        pnlVoidJudul.setLayout(new java.awt.BorderLayout());
        lblTrxDipilih.setText("Transaksi dipilih");
        lblTrxDipilih.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblTrxDipilih.setForeground(new java.awt.Color(19, 38, 29));
        pnlVoidJudul.add(lblTrxDipilih, java.awt.BorderLayout.WEST);

        pnlVoidJudul.add(badgeVoid, java.awt.BorderLayout.EAST);

        pnlVoidAtas.add(pnlVoidJudul);

        pnlDipilih.setLayout(new java.awt.BorderLayout(10, 0));
        pnlDipilihKiri.setLayout(new java.awt.GridLayout(3, 1, 0, 4));
        vId.setText("TRX-20261104-0017");
        vId.setFont(new java.awt.Font("Poppins", 1, 16)); // NOI18N
        vId.setForeground(new java.awt.Color(19, 38, 29));
        pnlDipilihKiri.add(vId);

        vInfo1.setText("04 Nov 2026 22:41  |  Kasir01  |  Siti Rahma");
        vInfo1.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        vInfo1.setForeground(new java.awt.Color(107, 122, 114));
        pnlDipilihKiri.add(vInfo1);

        vInfo2.setText("3 item  |  Tunai");
        vInfo2.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        vInfo2.setForeground(new java.awt.Color(107, 122, 114));
        pnlDipilihKiri.add(vInfo2);

        pnlDipilih.add(pnlDipilihKiri, java.awt.BorderLayout.CENTER);

        vTotal.setText("Rp55.150");
        vTotal.setFont(new java.awt.Font("Poppins", 1, 20)); // NOI18N
        vTotal.setForeground(new java.awt.Color(15, 145, 88));
        vTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlDipilih.add(vTotal, java.awt.BorderLayout.EAST);

        pnlVoidAtas.add(pnlDipilih);

        lblSyarat.setText("Syarat void setelah dibayar");
        lblSyarat.setFont(new java.awt.Font("Poppins", 1, 15)); // NOI18N
        lblSyarat.setForeground(new java.awt.Color(19, 38, 29));
        pnlVoidAtas.add(lblSyarat);

        pnlVoidAtas.add(cekHari);

        pnlVoidAtas.add(cekRetur);

        pnlVoidAtas.add(cekAdmin);

        lblAlasanVoid.setText("Alasan void");
        lblAlasanVoid.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblAlasanVoid.setForeground(new java.awt.Color(107, 122, 114));
        pnlVoidAtas.add(lblAlasanVoid);

        pnlChipsVoid.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        pnlChipsVoid.add(chipInput);

        pnlChipsVoid.add(chipBatal);

        pnlChipsVoid.add(chipProduk);

        pnlVoidAtas.add(pnlChipsVoid);

        pnlKotak.setPreferredSize(new java.awt.Dimension(100, 84));
        pnlKotak.setLayout(new java.awt.BorderLayout());
        txtKet.setText("Produk salah dipindai, akan dibuat transaksi baru.");
        txtKet.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        txtKet.setForeground(new java.awt.Color(19, 38, 29));
        txtKet.setLineWrap(true);
        txtKet.setWrapStyleWord(true);
        txtKet.setOpaque(false);
        pnlKotak.add(txtKet, java.awt.BorderLayout.CENTER);

        pnlVoidAtas.add(pnlKotak);

        lblCatatan.setText("Tidak ada fitur edit: transaksi salah di-void lalu dibuat ulang.");
        lblCatatan.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        lblCatatan.setForeground(new java.awt.Color(107, 122, 114));
        pnlVoidAtas.add(lblCatatan);

        kartuTransaksi.add(pnlVoidAtas, java.awt.BorderLayout.NORTH);

        kartuTransaksi.add(btnAjukanVoid, java.awt.BorderLayout.SOUTH);

        pnlIsi.add(kartuTransaksi, java.awt.BorderLayout.CENTER);

        kartuDampak.setPreferredSize(new java.awt.Dimension(420, 0));
        kartuDampak.setLayout(new java.awt.BorderLayout(0, 12));
        pnlDampakAtas.setLayout(new javax.swing.BoxLayout(pnlDampakAtas, javax.swing.BoxLayout.PAGE_AXIS));
        lblDampak.setText("Dampak void");
        lblDampak.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblDampak.setForeground(new java.awt.Color(19, 38, 29));
        pnlDampakAtas.add(lblDampak);

        pnlDampakAtas.add(damp1);

        pnlDampakAtas.add(damp2);

        pnlDampakAtas.add(damp3);

        pnlBiru.setLayout(new java.awt.GridLayout(4, 1, 0, 4));
        lblBiru1.setText("Sebelum dibayar");
        lblBiru1.setFont(new java.awt.Font("Poppins", 1, 15)); // NOI18N
        lblBiru1.setForeground(new java.awt.Color(29, 111, 184));
        pnlBiru.add(lblBiru1);

        lblBiru2.setText("Cukup tekan Batalkan di keranjang.");
        lblBiru2.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblBiru2.setForeground(new java.awt.Color(19, 38, 29));
        pnlBiru.add(lblBiru2);

        lblBiru3.setText("Tanpa persetujuan admin.");
        lblBiru3.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblBiru3.setForeground(new java.awt.Color(19, 38, 29));
        pnlBiru.add(lblBiru3);

        lblBiru4.setText("Status transaksi: DIBATALKAN");
        lblBiru4.setFont(new java.awt.Font("Poppins", 1, 11)); // NOI18N
        lblBiru4.setForeground(new java.awt.Color(29, 111, 184));
        pnlBiru.add(lblBiru4);

        pnlDampakAtas.add(pnlBiru);

        kartuDampak.add(pnlDampakAtas, java.awt.BorderLayout.NORTH);

        pnlIsi.add(kartuDampak, java.awt.BorderLayout.EAST);

        add(pnlIsi, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        UiKit.pintarLatar((java.awt.Graphics2D) g, getWidth(), getHeight());
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                javax.swing.JFrame f = new javax.swing.JFrame("SmartMart POS - VoidGuard");
                f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                VoidGuardPanel p = new VoidGuardPanel();
                p.muatContoh();
                f.setContentPane(UiKit.bungkusScroll(p));
                f.setSize(1120, 860);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel badgeVoid;
    private javax.swing.JPanel barInfo;
    private javax.swing.JButton btnAjukanVoid;
    private javax.swing.JPanel cekAdmin;
    private javax.swing.JPanel cekHari;
    private javax.swing.JPanel cekRetur;
    private javax.swing.JToggleButton chipBatal;
    private javax.swing.JToggleButton chipInput;
    private javax.swing.JToggleButton chipProduk;
    private javax.swing.JPanel damp1;
    private javax.swing.JPanel damp2;
    private javax.swing.JPanel damp3;
    private javax.swing.JPanel kartuDampak;
    private javax.swing.JPanel kartuTransaksi;
    private javax.swing.JLabel lblAlasanVoid;
    private javax.swing.JLabel lblBiru1;
    private javax.swing.JLabel lblBiru2;
    private javax.swing.JLabel lblBiru3;
    private javax.swing.JLabel lblBiru4;
    private javax.swing.JLabel lblCatatan;
    private javax.swing.JLabel lblDampak;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblSyarat;
    private javax.swing.JLabel lblTrxDipilih;
    private javax.swing.JPanel pnlBiru;
    private javax.swing.JPanel pnlChipsVoid;
    private javax.swing.JPanel pnlDampakAtas;
    private javax.swing.JPanel pnlDipilih;
    private javax.swing.JPanel pnlDipilihKiri;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudulBlok;
    private javax.swing.JPanel pnlKotak;
    private javax.swing.JPanel pnlVoidAtas;
    private javax.swing.JPanel pnlVoidJudul;
    private javax.swing.JTextArea txtKet;
    private javax.swing.JLabel vId;
    private javax.swing.JLabel vInfo1;
    private javax.swing.JLabel vInfo2;
    private javax.swing.JLabel vTotal;
    // End of variables declaration//GEN-END:variables
}
