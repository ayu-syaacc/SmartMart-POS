package com.smartmart.ui.kasir;

import com.smartmart.model.DetailRetur;
import com.smartmart.model.Retur;
import com.smartmart.service.ReturService;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class SmartReturnPanel extends javax.swing.JPanel {

    public interface ReturListener {
        void returSelesai(String idTransaksi, int refund);
        void dibatalkan();
    }

    private static final String ID_CONTOH = "TRX-20261103-0042";

    private final ReturService service = new ReturService();
    private boolean modeContoh;
    private Retur retur;
    private String alasan = "Salah beli";
    private int idKasir;
    private DialogAdminBase.Pemeriksa pemeriksa;
    private ReturListener listener;
    private UiKit.SearchField cari;

    public SmartReturnPanel() {
        UiKit.pasangFont();
        initComponents();
        susunTampilan();
        setMinimumSize(new Dimension(1000, 905));
    }

    public void setReturListener(ReturListener l) { listener = l; }

    public void setIdKasir(int id) { idKasir = id; }

    public void setPemeriksa(DialogAdminBase.Pemeriksa p) { pemeriksa = p; }

    public void mulaiDariTransaksi(String id) {
        cari.getField().setText(id);
        tampilLangkah("cari");
        lakukanCari();
    }

    public void muatContoh() {
        modeContoh = true;
        cari.getField().setText(ID_CONTOH);
        lakukanCari();
    }

    private void susunTampilan() {
        setOpaque(true);
        setBackground(UiKit.BG);
        setBorder(new EmptyBorder(64, 16, 46, 18));
        for (JComponent c : new JComponent[]{pnlHeader, pnlJudulBlok, pnlIsi, pnlCari, pnlCariAtas, pnlChipsCari, pnlCariBaris,
            pnlHasilTolak, pnlHasilAtas, pnlHasilJudul, pnlHasilKiri, pnlHasilKanan, pnlStatusWrap, pnlHasilBawah,
            pnlTolakJudul, pnlTolakList, pnlPilih, pnlBarangAtas, pnlTiga, pnlBarang, pnlBarangBawah, pnlReturAtas,
            pnlChipsAlasan, pnlKond1, pnlKond2, pnlKond3, pnlRingkasGrid, pnlRefund, pnlReturBawah}) {
            c.setOpaque(false);
        }
        lblJudul.setIcon(UiKit.ikonIcon(UiKit.Ikon.UNDO, 28, UiKit.TEXT));
        lblJudul.setIconTextGap(12);
        lblSub.setBorder(new EmptyBorder(0, 40, 0, 0));
        pnlHeader.setBorder(new EmptyBorder(0, 21, 0, 0));

        cari = (UiKit.SearchField) fldCari;
        cari.setPreferredSize(new Dimension(300, 48));
        cari.setAktif(true);
        cari.getField().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { lakukanCari(); }
        });
        btnCari.setPreferredSize(new Dimension(210, 48));
        btnCari.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { lakukanCari(); }
        });
        chipId.setSelected(true);
        pilihSatu(new javax.swing.JToggleButton[]{chipId, chipScan});
        pnlChipsCari.setBorder(new EmptyBorder(0, -10, 0, 0));
        pnlCariBaris.setBorder(new EmptyBorder(4, 0, 0, 0));

        pnlCariAtas.add(javax.swing.Box.createVerticalStrut(10), 1);
        UiKit.susunVertikal(pnlCariAtas);

        pnlHasilBox.setBorder(new EmptyBorder(18, 20, 18, 20));
        pnlHasilBox.setPreferredSize(new Dimension(100, 130));
        pnlHasilKiri.setBorder(new EmptyBorder(0, 0, 0, 0));
        hInfo2.setVerticalAlignment(SwingConstants.TOP);
        pnlHasilAtas.add(javax.swing.Box.createVerticalStrut(14), 1);
        pnlHasilAtas.add(javax.swing.Box.createVerticalStrut(14), 3);
        pnlHasilAtas.add(javax.swing.Box.createVerticalStrut(8), 5);
        pnlHasilAtas.add(javax.swing.Box.createVerticalStrut(8), 7);
        UiKit.susunVertikal(pnlHasilAtas);

        btnLanjut.setPreferredSize(new Dimension(100, 58));
        btnLanjut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { masukPilih(); }
        });
        pnlHasilBawah.add(javax.swing.Box.createVerticalStrut(10), 1);
        UiKit.susunVertikal(pnlHasilBawah);

        for (int i = 4; i >= 1; i--) pnlTolakList.add(javax.swing.Box.createVerticalStrut(11), i);
        UiKit.susunVertikal(pnlTolakList);

        pnlBarangAtas.add(javax.swing.Box.createVerticalStrut(12), 1);
        pnlBarangAtas.add(javax.swing.Box.createVerticalStrut(22), 3);
        pnlBarangAtas.add(javax.swing.Box.createVerticalStrut(12), 5);
        pnlKepalaBarang.setLayout(new GridBagLayout());
        GridBagConstraints g = UiKit.gbc(0, 0, 1, 0, GridBagConstraints.HORIZONTAL);
        g.gridwidth = 3;
        g.insets = new Insets(10, 16, 10, 4);
        pnlKepalaBarang.add(UiKit.lbl("Produk", Font.PLAIN, 12, UiKit.MUTED), g);
        String[] jd = {"Dibeli", "Retur", "Status"};
        int[] lb = {70, 96, 150};
        for (int i = 0; i < 3; i++) {
            JLabel l = UiKit.lbl(jd[i], Font.PLAIN, 12, UiKit.MUTED);
            l.setHorizontalAlignment(i == 2 ? SwingConstants.RIGHT : SwingConstants.CENTER);
            l.setPreferredSize(new Dimension(lb[i], 18));
            g = new GridBagConstraints();
            g.gridx = 3 + i; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(10, 4, 10, i == 2 ? 16 : 4);
            pnlKepalaBarang.add(l, g);
        }
        UiKit.susunVertikal(pnlBarangAtas);
        pnlBarang.setLayout(new BoxLayout(pnlBarang, BoxLayout.Y_AXIS));
        pnlBarang.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlBarangBawah.add(javax.swing.Box.createVerticalStrut(12), 1);
        UiKit.susunVertikal(pnlBarangBawah);

        chipSalah.setSelected(true);
        pasangAlasan(chipSalah, chipRusak, chipLain);
        pasangAlasan(chipRusak, chipSalah, chipLain);
        pasangAlasan(chipLain, chipSalah, chipRusak);
        pnlChipsAlasan.setBorder(new EmptyBorder(0, -8, 0, 0));
        JPanel[] kond = {pnlKond1, pnlKond2, pnlKond3};
        for (JPanel k : kond) k.setBorder(new EmptyBorder(0, 0, 0, 0));
        pnlRingkasan.setBorder(new EmptyBorder(16, 18, 16, 18));
        lblStok.setBorder(new EmptyBorder(0, 0, 0, 0));
        lblAlasan.setBorder(new EmptyBorder(0, 0, 0, 0));

        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(16), 1);
        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(8), 3);
        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(18), 5);
        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(8), 7);
        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(10), 9);
        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(10), 11);
        pnlReturAtas.add(javax.swing.Box.createVerticalStrut(14), 13);
        UiKit.susunVertikal(pnlReturAtas);

        btnAjukan.setPreferredSize(new Dimension(100, 54));
        btnBatalRetur.setPreferredSize(new Dimension(100, 50));
        pnlReturBawah.add(javax.swing.Box.createVerticalStrut(10), 1);
        pnlReturBawah.add(javax.swing.Box.createVerticalStrut(10), 3);
        UiKit.susunVertikal(pnlReturBawah);
        btnAjukan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { ajukanRetur(); }
        });
        btnBatalRetur.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tampilLangkah("cari");
                if (listener != null) listener.dibatalkan();
            }
        });
        for (final javax.swing.JToggleButton cb : new javax.swing.JToggleButton[]{cbBelum, cbUtuh, cbLayak}) {
            cb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { hitungRingkasan(); }
            });
        }
        tampilkanAwal();
    }

    private void pilihSatu(final javax.swing.JToggleButton[] semua) {
        for (final javax.swing.JToggleButton b : semua) {
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for (javax.swing.JToggleButton o : semua) o.setSelected(o == b);
                }
            });
        }
    }

    private void pasangAlasan(final javax.swing.JToggleButton ini, final javax.swing.JToggleButton a,
                              final javax.swing.JToggleButton b) {
        ini.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ini.setSelected(true);
                a.setSelected(false);
                b.setSelected(false);
                alasan = ini.getText();
            }
        });
    }

    private void tampilLangkah(String nama) {
        ((CardLayout) pnlIsi.getLayout()).show(pnlIsi, nama);
        if ("cari".equals(nama)) {
            lblSub.setText("Langkah 1 dari 3: cari transaksi asli");
        } else if (retur != null && retur.getWaktuTransaksi() != null) {
            lblSub.setText(retur.getIdTransaksi() + "  |  " + Retur.tanggal(retur.getWaktuTransaksi().toLocalDate())
                    + "  |  " + retur.getNamaPelanggan());
        }
    }

    private void lakukanCari() {
        String id = cari.getField().getText().trim();
        try {
            retur = modeContoh ? contoh(id) : service.siapkan(id);
        } catch (SQLException e) {
            retur = null;
            tampilkanHasil(null);
            lblCatCari.setText("Gagal membaca database: " + e.getMessage());
            lblCatCari.setForeground(UiKit.RED_TEXT);
            return;
        }
        tampilkanHasil(retur);
    }

    private void tampilkanAwal() {
        ((UiKit.Badge) badgeHasil).set("BELUM DICARI", UiKit.GRAY_SOFT, UiKit.MUTED);
        hId.setText("Belum ada transaksi dicari");
        hInfo1.setText("Masukkan ID transaksi lalu tekan Cari.");
        hInfo2.setText(" ");
        hTotal.setText(" ");
        hStatus.setVisible(false);
        cek1.setVisible(false);
        cek2.setVisible(false);
        cek3.setVisible(false);
        lblCatCari.setForeground(UiKit.MUTED);
        lblCatCari.setText("Retur diproses setelah admin menyetujui");
        btnLanjut.setEnabled(false);
        UiKit.Badge badge = (UiKit.Badge) badgeTolak;
        lblTolakJudul.setText("Pemeriksaan otomatis");
        badge.set("MENUNGGU", UiKit.GRAY_SOFT, UiKit.MUTED);
        pnlTolakList.removeAll();
        if (modeContoh) {
            isiKartuPenolakan(null);
            return;
        }
        JLabel info = UiKit.lbl("Hasil pemeriksaan syarat retur akan muncul setelah transaksi dicari.", Font.PLAIN, 13, UiKit.MUTED);
        info.setBorder(new EmptyBorder(8, 2, 0, 0));
        pnlTolakList.add(info);
        UiKit.susunVertikal(pnlTolakList);
    }

    private void isiKartuPenolakan(Retur r) {
        UiKit.Badge badge = (UiKit.Badge) badgeTolak;
        pnlTolakList.removeAll();
        if (modeContoh) {
            lblTolakJudul.setText("Contoh penolakan otomatis");
            badge.set("DITOLAK", UiKit.RED_SOFT, UiKit.RED_TEXT);
            JPanel[] semua = {tolak1, tolak2, tolak3, tolak4, tolak5};
            for (int i = 0; i < semua.length; i++) {
                if (i > 0) pnlTolakList.add(javax.swing.Box.createVerticalStrut(11));
                pnlTolakList.add(semua[i]);
            }
        } else {
            List<String[]> kartu = new java.util.ArrayList<String[]>();
            if (r == null) {
                kartu.add(new String[]{"Transaksi tidak ditemukan", "Masukkan ID transaksi asli untuk melanjutkan."});
            } else {
                for (String m : r.validasiTransaksi(LocalDate.now())) {
                    kartu.add(new String[]{"Transaksi tidak dapat diretur", m});
                }
                for (DetailRetur d : r.getDetail()) {
                    String t = d.alasanTolakBarang();
                    if (t == null) continue;
                    String awalan = d.getNamaBarang() + ": ";
                    kartu.add(new String[]{d.getNamaBarang(), t.startsWith(awalan) ? t.substring(awalan.length()) : t});
                }
            }
            lblTolakJudul.setText("Pemeriksaan otomatis");
            if (kartu.isEmpty()) {
                badge.set("LOLOS", UiKit.GREEN_SOFT, UiKit.GREEN);
                JLabel ok = UiKit.lbl("Semua barang pada transaksi ini memenuhi syarat retur.", Font.PLAIN, 13, UiKit.MUTED);
                ok.setBorder(new EmptyBorder(8, 2, 0, 0));
                pnlTolakList.add(ok);
            } else {
                badge.set("DITOLAK", UiKit.RED_SOFT, UiKit.RED_TEXT);
                int maks = Math.min(kartu.size(), 5);
                for (int i = 0; i < maks; i++) {
                    if (i > 0) pnlTolakList.add(javax.swing.Box.createVerticalStrut(11));
                    pnlTolakList.add(UiKit.kartuTolak(kartu.get(i)[0], kartu.get(i)[1]));
                }
                if (kartu.size() > maks) {
                    JLabel lagi = UiKit.lbl("+" + (kartu.size() - maks) + " alasan lainnya", Font.PLAIN, 12, UiKit.MUTED);
                    lagi.setBorder(new EmptyBorder(8, 2, 0, 0));
                    pnlTolakList.add(lagi);
                }
            }
        }
        UiKit.susunVertikal(pnlTolakList);
        pnlTolakList.revalidate();
        pnlTolakList.repaint();
    }

    private void tampilkanHasil(Retur r) {
        isiKartuPenolakan(r);
        cek1.setVisible(true);
        cek2.setVisible(true);
        cek3.setVisible(true);
        UiKit.Badge badge = (UiKit.Badge) badgeHasil;
        UiKit.BarisValid c1 = (UiKit.BarisValid) cek1, c2 = (UiKit.BarisValid) cek2, c3 = (UiKit.BarisValid) cek3;
        lblCatCari.setForeground(UiKit.MUTED);
        lblCatCari.setText("Retur diproses setelah admin menyetujui");
        if (r == null) {
            badge.set("TIDAK DITEMUKAN", UiKit.RED_SOFT, UiKit.RED_TEXT);
            hId.setText("Transaksi tidak ditemukan");
            hInfo1.setText("Masukkan ID transaksi asli untuk melanjutkan.");
            hInfo2.setText(" ");
            hTotal.setText(" ");
            hStatus.setVisible(false);
            c1.set("Masa retur", "-", false);
            c2.set("Struk / ID transaksi", "Tidak ditemukan", false);
            c3.set("Belum pernah di-void", "-", false);
            btnLanjut.setEnabled(false);
            return;
        }
        badge.set("DITEMUKAN", UiKit.GREEN_SOFT, UiKit.GREEN);
        LocalDateTime w = r.getWaktuTransaksi();
        hId.setText(r.getIdTransaksi());
        hInfo1.setText(Retur.tanggal(w.toLocalDate()) + String.format(" %02d:%02d", w.getHour(), w.getMinute())
                + "  |  " + (r.getNamaKasirAsal() == null ? "-" : r.getNamaKasirAsal()));
        hInfo2.setText(r.getNamaPelanggan() + "  |  " + r.getMetodeBayar() + "  |  " + r.getDetail().size() + " item");
        hTotal.setText(UiKit.rp(r.getTotalTransaksi()));
        hStatus.setVisible(true);
        UiKit.terapkanStatus((UiKit.Badge) hStatus, r.getStatusTransaksi());

        List<String> tolak = r.validasiTransaksi(LocalDate.now());
        long hari = r.hariKe(LocalDate.now());
        c1.set("Masa retur", "Hari ke-" + hari + " dari maks. " + Retur.BATAS_HARI, hari <= Retur.BATAS_HARI);
        c2.set("Struk / ID transaksi", "Ditemukan", true);
        boolean void_ = "DIBATALKAN".equals(r.getStatusTransaksi());
        c3.set("Belum pernah di-void", void_ ? "Transaksi sudah di-void" : "Valid", !void_);
        btnLanjut.setEnabled(tolak.isEmpty());
        if (!tolak.isEmpty()) {
            lblCatCari.setText(tolak.get(0));
            lblCatCari.setForeground(UiKit.RED_TEXT);
        }
    }

    private void masukPilih() {
        if (retur == null) return;
        boolean sudahPilih = false;
        for (DetailRetur d : retur.getDetail()) {
            d.setKondisi(DetailRetur.KONDISI_LAYAK);
            d.setJumlahRetur(0);
            if (!sudahPilih && bisa(d)) {
                d.setJumlahRetur(Math.min(1, d.sisaBisaDiretur()));
                sudahPilih = true;
            }
        }
        alasan = "Salah beli";
        chipSalah.setSelected(true);
        chipRusak.setSelected(false);
        chipLain.setSelected(false);
        cbBelum.setSelected(true);
        cbUtuh.setSelected(true);
        cbLayak.setSelected(true);
        UiKit.BarisValid a = (UiKit.BarisValid) cekA, b = (UiKit.BarisValid) cekB, c = (UiKit.BarisValid) cekC;
        a.set("Masa retur", "Hari ke-" + retur.hariKe(LocalDate.now()) + " dari maks. " + Retur.BATAS_HARI, true);
        b.set("Struk / ID transaksi", "Ditemukan", true);
        c.set("Jumlah retur", "Tidak melebihi pembelian", true);
        tampilPesan("Pesan penolakan otomatis ditampilkan untuk barang yang tidak memenuhi syarat.", false);
        tampilLangkah("pilih");
        perbaruiBarang();
    }

    private static boolean bisa(DetailRetur d) {
        return d.alasanTolakBarang() == null && d.sisaBisaDiretur() > 1e-9;
    }

    private static String teksStatus(DetailRetur d) {
        if (!d.isDapatDiretur()) return "Tidak dapat diretur";
        if ("FRESHSAVE".equalsIgnoreCase(d.getSumberDiskon())) return "Diskon FreshSave";
        if (d.sisaBisaDiretur() <= 1e-9) return "Sudah diretur semua";
        return "Dapat diretur";
    }

    private static String pesanTolak(DetailRetur d) {
        String t = d.alasanTolakBarang();
        return t != null ? t : d.getNamaBarang() + ": semua jumlah sudah diretur.";
    }

    private static String subBarang(DetailRetur d) {
        if (!d.isDapatDiretur()) return d.getNamaBarang().toLowerCase().contains("roti") ? "Cepat basi" : "Produk segar";
        if ("FRESHSAVE".equalsIgnoreCase(d.getSumberDiskon())) return "Dibeli diskon FreshSave";
        return UiKit.rp((int) Math.round(d.hargaSatuanEfektif())) + " / " + ("KG".equalsIgnoreCase(d.getSatuan()) ? "kg" : "unit");
    }

    private void perbaruiBarang() {
        pnlBarang.removeAll();
        for (DetailRetur d : retur.getDetail()) {
            pnlBarang.add(buatBaris(d));
            pnlBarang.add(javax.swing.Box.createVerticalStrut(10));
        }
        UiKit.susunVertikal(pnlBarang);
        hitungRingkasan();
    }

    private JPanel buatBaris(final DetailRetur d) {
        final boolean bisa = bisa(d);
        boolean pilih = bisa && d.getJumlahRetur() > 0;
        UiKit.Box r = new UiKit.Box(pilih ? UiKit.SELECTED : UiKit.ROW_BG, 18);
        if (pilih) r.setBorderColor(UiKit.GREEN, 2f);
        r.setLayout(new GridBagLayout());
        r.setPreferredSize(new Dimension(100, 72));
        r.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        UiKit.CentangBox cb = new UiKit.CentangBox(26, pilih);
        cb.setEnabled(bisa);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.insets = new Insets(0, 16, 0, 12);
        r.add(cb, g);
        g = new GridBagConstraints();
        g.gridx = 1; g.insets = new Insets(0, 0, 0, 12);
        r.add(UiKit.ikonProduk(UiKit.produkDariNama(d.getNamaBarang()), 38), g);

        JPanel nama = UiKit.transparan();
        nama.setLayout(new GridBagLayout());
        GridBagConstraints n = new GridBagConstraints();
        n.gridx = 0; n.gridy = 0; n.weightx = 1; n.anchor = GridBagConstraints.WEST;
        String judul = d.getNamaBarang() + ("KG".equalsIgnoreCase(d.getSatuan()) && !bisa
                ? " " + DetailRetur.angka(d.getJumlahDibeli()) + " kg" : "");
        nama.add(UiKit.lbl(judul, Font.BOLD, 14, bisa ? UiKit.TEXT : UiKit.MUTED), n);
        n = new GridBagConstraints();
        n.gridx = 0; n.gridy = 1; n.weightx = 1; n.anchor = GridBagConstraints.WEST;
        nama.add(UiKit.lbl(subBarang(d), Font.PLAIN, 11, UiKit.MUTED), n);
        g = UiKit.gbc(2, 0, 1, 0, GridBagConstraints.HORIZONTAL);
        r.add(nama, g);

        JLabel dibeli = UiKit.lbl(DetailRetur.angka(d.getJumlahDibeli()), Font.PLAIN, 13, bisa ? UiKit.TEXT : UiKit.MUTED);
        dibeli.setHorizontalAlignment(SwingConstants.CENTER);
        dibeli.setPreferredSize(new Dimension(70, 20));
        g = new GridBagConstraints();
        g.gridx = 3; g.insets = new Insets(0, 4, 0, 4);
        r.add(dibeli, g);

        JComponent kolomRetur;
        if (pilih) {
            kolomRetur = stepper(d);
        } else {
            JLabel strip = UiKit.lbl("-", Font.PLAIN, 13, UiKit.MUTED);
            strip.setHorizontalAlignment(SwingConstants.CENTER);
            kolomRetur = strip;
        }
        kolomRetur.setPreferredSize(new Dimension(96, 32));
        g = new GridBagConstraints();
        g.gridx = 4; g.insets = new Insets(0, 4, 0, 4);
        r.add(kolomRetur, g);

        UiKit.Badge st = bisa ? new UiKit.Badge(teksStatus(d), UiKit.GREEN_SOFT, UiKit.GREEN)
                : new UiKit.Badge(teksStatus(d), UiKit.RED_SOFT, UiKit.RED_TEXT);
        st.setFont(UiKit.font(Font.BOLD, 10));
        JPanel stw = UiKit.transparan();
        stw.setLayout(new GridBagLayout());
        GridBagConstraints sg = new GridBagConstraints();
        sg.anchor = GridBagConstraints.EAST; sg.weightx = 1;
        stw.add(st, sg);
        stw.setPreferredSize(new Dimension(150, 24));
        g = new GridBagConstraints();
        g.gridx = 5; g.insets = new Insets(0, 4, 0, 16);
        r.add(stw, g);

        MouseAdapter klik = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { klikBarang(d); }
        };
        r.addMouseListener(klik);
        cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { klikBarang(d); }
        });
        return r;
    }

    private JComponent stepper(final DetailRetur d) {
        UiKit.Box s = new UiKit.Box(Color.WHITE, 14);
        s.setBorderColor(UiKit.GREEN, 1.5f);
        s.setLayout(new GridBagLayout());
        JLabel min = UiKit.lbl("\u2212", Font.BOLD, 16, UiKit.GREEN);
        JLabel plus = UiKit.lbl("+", Font.BOLD, 16, UiKit.GREEN);
        JLabel nilai = UiKit.lbl(DetailRetur.angka(d.getJumlahRetur()), Font.BOLD, 13, UiKit.TEXT);
        nilai.setHorizontalAlignment(SwingConstants.CENTER);
        for (JLabel l : new JLabel[]{min, plus}) {
            l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setPreferredSize(new Dimension(26, 24));
        }
        min.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { ubahJumlah(d, -1); }
        });
        plus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { ubahJumlah(d, +1); }
        });
        s.add(min, new GridBagConstraints());
        s.add(nilai, UiKit.gbc(1, 0, 1, 0, GridBagConstraints.HORIZONTAL));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 2;
        s.add(plus, g);
        return s;
    }

    private void klikBarang(DetailRetur d) {
        if (!bisa(d)) {
            tampilPesan(pesanTolak(d), true);
            return;
        }
        d.setJumlahRetur(d.getJumlahRetur() > 0 ? 0 : Math.min(1, d.sisaBisaDiretur()));
        tampilPesan("Pesan penolakan otomatis ditampilkan untuk barang yang tidak memenuhi syarat.", false);
        perbaruiBarang();
    }

    private void ubahJumlah(DetailRetur d, int arah) {
        double langkah = "KG".equalsIgnoreCase(d.getSatuan()) ? 0.25 : 1;
        double lama = d.getJumlahRetur();
        double baru = lama + arah * langkah;
        if (baru < langkah - 1e-9) return;
        d.setJumlahRetur(baru);
        String tolak = d.alasanTolakJumlah();
        if (tolak != null) {
            d.setJumlahRetur(lama);
            tampilPesan(tolak, true);
            return;
        }
        tampilPesan("Pesan penolakan otomatis ditampilkan untuk barang yang tidak memenuhi syarat.", false);
        perbaruiBarang();
    }

    private void tampilPesan(String teks, boolean galat) {
        lblPesan.setText(teks);
        lblPesan.setForeground(galat ? UiKit.RED_TEXT : UiKit.MUTED);
        lblPesan.setFont(UiKit.font(galat ? Font.BOLD : Font.PLAIN, 11));
    }

    private void terapkanIsianKeRetur() {
        retur.setAlasan(alasan);
        retur.setBelumDigunakan(cbBelum.isSelected());
        retur.setKemasanUtuh(cbUtuh.isSelected());
        String kondisi = cbLayak.isSelected() ? DetailRetur.KONDISI_LAYAK : DetailRetur.KONDISI_RUSAK;
        for (DetailRetur d : retur.getDetail()) d.setKondisi(kondisi);
    }

    private void hitungRingkasan() {
        if (retur == null) return;
        terapkanIsianKeRetur();
        List<DetailRetur> dipilih = retur.barangDipilih();
        if (dipilih.isEmpty()) {
            rBarang.setText("-");
        } else if (dipilih.size() == 1) {
            rBarang.setText(DetailRetur.angka(dipilih.get(0).getJumlahRetur()) + " x " + dipilih.get(0).getNamaBarang());
        } else {
            rBarang.setText(dipilih.size() + " jenis barang");
        }
        rMetode.setText("Tunai");
        rPoin.setText(retur.getIdMember() == null ? "-" : "-" + retur.hitungPoinDikurangi() + " poin");
        rRefund.setText(UiKit.rp(retur.hitungRefund()));
    }

    private void ajukanRetur() {
        if (retur == null) return;
        terapkanIsianKeRetur();
        List<String> tolak = retur.validasi(LocalDate.now());
        if (!tolak.isEmpty()) {
            tampilPesan(tolak.get(0), true);
            return;
        }
        List<DetailRetur> dipilih = retur.barangDipilih();
        String ringkas = "Retur " + (dipilih.size() == 1
                ? dipilih.get(0).getNamaBarang() + " x" + DetailRetur.angka(dipilih.get(0).getJumlahRetur())
                : dipilih.size() + " jenis barang") + "  |  " + UiKit.rp(retur.hitungRefund());
        Integer idAdmin = KonfirmasiReturDialog.minta(this, ringkas, pemeriksa, modeContoh);
        if (idAdmin == null) return;

        int refund = retur.hitungRefund();
        String id = retur.getIdTransaksi();
        if (!modeContoh) {
            try {
                service.proses(retur, idKasir, idAdmin.intValue());
            } catch (ReturService.DitolakException e) {
                tampilPesan(e.getPesan().get(0), true);
                return;
            } catch (SQLException e) {
                tampilPesan("Retur gagal disimpan: " + e.getMessage(), true);
                return;
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this, "Retur disetujui admin. Refund " + UiKit.rp(refund)
                + (modeContoh ? " (data contoh, tidak disimpan)." : "."));
        if (listener != null) listener.returSelesai(id, refund);
        tampilLangkah("cari");
        lakukanCari();
    }

    private Retur contoh(String id) {
        if (!ID_CONTOH.equals(id)) return null;
        Retur r = new Retur(ID_CONTOH);
        r.setWaktuTransaksi(LocalDate.now().minusDays(1).atTime(16, 20));
        r.setStatusTransaksi("SELESAI");
        r.setIdMember(42L);
        r.setNamaPelanggan("Siti Rahma");
        r.setMetodeBayar("Tunai");
        r.setNamaKasirAsal("Kasir01");
        r.setTotalTransaksi(95400);
        LocalDate hariIni = LocalDate.now();
        r.getDetail().add(new DetailRetur(1, "899100101", "Susu UHT 1L", "PCS", true, 10L, hariIni.plusDays(120), 2, 0, 31500, "MEMBER"));
        r.getDetail().add(new DetailRetur(2, "899100102", "Apel Fuji", "KG", false, 11L, hariIni.plusDays(6), 0.75, 0, 32400, "MEMBER"));
        r.getDetail().add(new DetailRetur(3, "899100103", "Roti Tawar", "PCS", false, 12L, hariIni.plusDays(2), 1, 0, 15000, "TIDAK_ADA"));
        r.getDetail().add(new DetailRetur(4, "899100104", "Yoghurt", "PCS", true, 13L, hariIni.plusDays(1), 1, 0, 8000, "FRESHSAVE"));
        return r;
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
        pnlCari = new javax.swing.JPanel();
        kartuCari = new com.smartmart.ui.kasir.UiKit.Card();
        pnlCariAtas = new javax.swing.JPanel();
        lblCariJudul = new javax.swing.JLabel();
        pnlChipsCari = new javax.swing.JPanel();
        chipId = new com.smartmart.ui.kasir.UiKit.Chip("ID transaksi", com.smartmart.ui.kasir.UiKit.GREEN);
        chipScan = new com.smartmart.ui.kasir.UiKit.Chip("Scan struk", com.smartmart.ui.kasir.UiKit.GREEN);
        pnlCariBaris = new javax.swing.JPanel();
        fldCari = new com.smartmart.ui.kasir.UiKit.SearchField("Masukkan ID transaksi asli", com.smartmart.ui.kasir.UiKit.Ikon.SEARCH, com.smartmart.ui.kasir.UiKit.GREEN);
        btnCari = com.smartmart.ui.kasir.UiKit.tombolHijau("Cari", null);
        pnlHasilTolak = new javax.swing.JPanel();
        kartuHasil = new com.smartmart.ui.kasir.UiKit.Card();
        pnlHasilAtas = new javax.swing.JPanel();
        pnlHasilJudul = new javax.swing.JPanel();
        lblHasilJudul = new javax.swing.JLabel();
        badgeHasil = new com.smartmart.ui.kasir.UiKit.Badge("DITEMUKAN", com.smartmart.ui.kasir.UiKit.GREEN_SOFT, com.smartmart.ui.kasir.UiKit.GREEN);
        pnlHasilBox = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.ROW_BG, 20);
        pnlHasilKiri = new javax.swing.JPanel();
        hId = new javax.swing.JLabel();
        hInfo1 = new javax.swing.JLabel();
        hInfo2 = new javax.swing.JLabel();
        pnlHasilKanan = new javax.swing.JPanel();
        hTotal = new javax.swing.JLabel();
        pnlStatusWrap = new javax.swing.JPanel();
        hStatus = com.smartmart.ui.kasir.UiKit.badgeStatus("SELESAI");
        cek1 = new com.smartmart.ui.kasir.UiKit.BarisValid("Masa retur", "Hari ke-1 dari maks. 3");
        cek2 = new com.smartmart.ui.kasir.UiKit.BarisValid("Struk / ID transaksi", "Ditemukan");
        cek3 = new com.smartmart.ui.kasir.UiKit.BarisValid("Belum pernah di-void", "Valid");
        pnlHasilBawah = new javax.swing.JPanel();
        btnLanjut = com.smartmart.ui.kasir.UiKit.tombolHijau("Lanjut: Pilih Barang", null);
        lblCatCari = new javax.swing.JLabel();
        kartuTolak = new com.smartmart.ui.kasir.UiKit.Card();
        pnlTolakJudul = new javax.swing.JPanel();
        lblTolakJudul = new javax.swing.JLabel();
        badgeTolak = new com.smartmart.ui.kasir.UiKit.Badge("DITOLAK", com.smartmart.ui.kasir.UiKit.RED_SOFT, com.smartmart.ui.kasir.UiKit.RED_TEXT);
        pnlTolakList = new javax.swing.JPanel();
        tolak1 = com.smartmart.ui.kasir.UiKit.kartuTolak("Lewat 3 hari", "Pembelian 12 Okt 2026, batas retur 3 hari sudah lewat.");
        tolak2 = com.smartmart.ui.kasir.UiKit.kartuTolak("Barang tidak dapat diretur", "Sayur, buah, yoghurt, roti, dan produk cepat basi.");
        tolak3 = com.smartmart.ui.kasir.UiKit.kartuTolak("Dibeli dengan diskon FreshSave", "Barang FreshSave tidak dapat diretur.");
        tolak4 = com.smartmart.ui.kasir.UiKit.kartuTolak("Jumlah melebihi pembelian", "Dibeli 2, sudah diretur 1, sisa maks. 1.");
        tolak5 = com.smartmart.ui.kasir.UiKit.kartuTolak("Tanpa struk atau ID transaksi", "Masukkan ID transaksi asli untuk melanjutkan.");
        pnlPilih = new javax.swing.JPanel();
        kartuBarang = new com.smartmart.ui.kasir.UiKit.Card();
        pnlBarangAtas = new javax.swing.JPanel();
        lblValidasi = new javax.swing.JLabel();
        pnlTiga = new javax.swing.JPanel();
        cekA = new com.smartmart.ui.kasir.UiKit.BarisValid("Masa retur", "Hari ke-1 dari maks. 3");
        cekB = new com.smartmart.ui.kasir.UiKit.BarisValid("Struk / ID transaksi", "Ditemukan");
        cekC = new com.smartmart.ui.kasir.UiKit.BarisValid("Jumlah retur", "Tidak melebihi pembelian");
        lblPilih = new javax.swing.JLabel();
        pnlKepalaBarang = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.HEADER_BG, 14);
        pnlBarang = new javax.swing.JPanel();
        pnlBarangBawah = new javax.swing.JPanel();
        lblPesan = new javax.swing.JLabel();
        pnlCatatan = com.smartmart.ui.kasir.UiKit.catatanKuning("Perlu persetujuan Admin", "Retur diproses setelah admin memasukkan password.", true, 420);
        kartuRetur = new com.smartmart.ui.kasir.UiKit.Card();
        pnlReturAtas = new javax.swing.JPanel();
        lblDetailRetur = new javax.swing.JLabel();
        lblAlasan = new javax.swing.JLabel();
        pnlChipsAlasan = new javax.swing.JPanel();
        chipSalah = new com.smartmart.ui.kasir.UiKit.Chip("Salah beli", com.smartmart.ui.kasir.UiKit.GREEN);
        chipRusak = new com.smartmart.ui.kasir.UiKit.Chip("Kemasan rusak", com.smartmart.ui.kasir.UiKit.GREEN);
        chipLain = new com.smartmart.ui.kasir.UiKit.Chip("Lainnya", com.smartmart.ui.kasir.UiKit.GREEN);
        lblKondisi = new javax.swing.JLabel();
        pnlKond1 = new javax.swing.JPanel();
        cbBelum = new com.smartmart.ui.kasir.UiKit.CentangBox(22, true);
        lblKond1 = new javax.swing.JLabel();
        pnlKond2 = new javax.swing.JPanel();
        cbUtuh = new com.smartmart.ui.kasir.UiKit.CentangBox(22, true);
        lblKond2 = new javax.swing.JLabel();
        pnlKond3 = new javax.swing.JPanel();
        cbLayak = new com.smartmart.ui.kasir.UiKit.CentangBox(22, true);
        lblKond3 = new javax.swing.JLabel();
        pnlRingkasan = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.ROW_BG, 18);
        lblRingkasan = new javax.swing.JLabel();
        pnlRingkasGrid = new javax.swing.JPanel();
        lblBarangT = new javax.swing.JLabel();
        rBarang = new javax.swing.JLabel();
        lblMetodeT = new javax.swing.JLabel();
        rMetode = new javax.swing.JLabel();
        lblPoinT = new javax.swing.JLabel();
        rPoin = new javax.swing.JLabel();
        pnlRefund = new javax.swing.JPanel();
        lblRefundT = new javax.swing.JLabel();
        rRefund = new javax.swing.JLabel();
        pnlReturBawah = new javax.swing.JPanel();
        btnAjukan = com.smartmart.ui.kasir.UiKit.tombolHijau("Ajukan Retur", null);
        btnBatalRetur = com.smartmart.ui.kasir.UiKit.tombolAbu("Batal");
        lblStok = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout(0, 14));
        pnlHeader.setLayout(new java.awt.BorderLayout());
        pnlJudulBlok.setLayout(new java.awt.GridLayout(2, 1));
        lblJudul.setText("SmartReturn");
        lblJudul.setFont(new java.awt.Font("Poppins", 1, 29)); // NOI18N
        lblJudul.setForeground(new java.awt.Color(19, 38, 29));
        pnlJudulBlok.add(lblJudul);

        lblSub.setText("Langkah 1 dari 3: cari transaksi asli");
        lblSub.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblSub.setForeground(new java.awt.Color(107, 122, 114));
        pnlJudulBlok.add(lblSub);

        pnlHeader.add(pnlJudulBlok, java.awt.BorderLayout.WEST);

        pnlHeader.add(barInfo, java.awt.BorderLayout.EAST);

        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.CardLayout());
        pnlCari.setLayout(new java.awt.BorderLayout(0, 18));
        kartuCari.setLayout(new java.awt.BorderLayout(0, 14));
        pnlCariAtas.setLayout(new javax.swing.BoxLayout(pnlCariAtas, javax.swing.BoxLayout.PAGE_AXIS));
        lblCariJudul.setText("Cari transaksi");
        lblCariJudul.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblCariJudul.setForeground(new java.awt.Color(19, 38, 29));
        pnlCariAtas.add(lblCariJudul);

        pnlChipsCari.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        pnlChipsCari.add(chipId);

        pnlChipsCari.add(chipScan);

        pnlCariAtas.add(pnlChipsCari);

        kartuCari.add(pnlCariAtas, java.awt.BorderLayout.NORTH);

        pnlCariBaris.setLayout(new java.awt.BorderLayout(16, 0));
        pnlCariBaris.add(fldCari, java.awt.BorderLayout.CENTER);

        pnlCariBaris.add(btnCari, java.awt.BorderLayout.EAST);

        kartuCari.add(pnlCariBaris, java.awt.BorderLayout.CENTER);

        pnlCari.add(kartuCari, java.awt.BorderLayout.NORTH);

        pnlHasilTolak.setLayout(new java.awt.GridLayout(1, 2, 20, 0));
        kartuHasil.setLayout(new java.awt.BorderLayout(0, 14));
        pnlHasilAtas.setLayout(new javax.swing.BoxLayout(pnlHasilAtas, javax.swing.BoxLayout.PAGE_AXIS));
        pnlHasilJudul.setLayout(new java.awt.BorderLayout());
        lblHasilJudul.setText("Hasil pencarian");
        lblHasilJudul.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblHasilJudul.setForeground(new java.awt.Color(19, 38, 29));
        pnlHasilJudul.add(lblHasilJudul, java.awt.BorderLayout.WEST);

        pnlHasilJudul.add(badgeHasil, java.awt.BorderLayout.EAST);

        pnlHasilAtas.add(pnlHasilJudul);

        pnlHasilBox.setLayout(new java.awt.BorderLayout(10, 0));
        pnlHasilKiri.setLayout(new java.awt.GridLayout(3, 1, 0, 4));
        hId.setText("TRX-20261103-0042");
        hId.setFont(new java.awt.Font("Poppins", 1, 16)); // NOI18N
        hId.setForeground(new java.awt.Color(19, 38, 29));
        pnlHasilKiri.add(hId);

        hInfo1.setText("03 Nov 2026 16:20  |  Kasir01");
        hInfo1.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        hInfo1.setForeground(new java.awt.Color(107, 122, 114));
        pnlHasilKiri.add(hInfo1);

        hInfo2.setText("Siti Rahma  |  Tunai  |  3 item");
        hInfo2.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        hInfo2.setForeground(new java.awt.Color(107, 122, 114));
        pnlHasilKiri.add(hInfo2);

        pnlHasilBox.add(pnlHasilKiri, java.awt.BorderLayout.CENTER);

        pnlHasilKanan.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
        hTotal.setText("Rp95.400");
        hTotal.setFont(new java.awt.Font("Poppins", 1, 20)); // NOI18N
        hTotal.setForeground(new java.awt.Color(15, 145, 88));
        hTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlHasilKanan.add(hTotal);

        pnlStatusWrap.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        pnlStatusWrap.add(hStatus);

        pnlHasilKanan.add(pnlStatusWrap);

        pnlHasilBox.add(pnlHasilKanan, java.awt.BorderLayout.EAST);

        pnlHasilAtas.add(pnlHasilBox);

        pnlHasilAtas.add(cek1);

        pnlHasilAtas.add(cek2);

        pnlHasilAtas.add(cek3);

        kartuHasil.add(pnlHasilAtas, java.awt.BorderLayout.NORTH);

        pnlHasilBawah.setLayout(new javax.swing.BoxLayout(pnlHasilBawah, javax.swing.BoxLayout.PAGE_AXIS));
        pnlHasilBawah.add(btnLanjut);

        lblCatCari.setText("Retur diproses setelah admin menyetujui");
        lblCatCari.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        lblCatCari.setForeground(new java.awt.Color(107, 122, 114));
        lblCatCari.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlHasilBawah.add(lblCatCari);

        kartuHasil.add(pnlHasilBawah, java.awt.BorderLayout.SOUTH);

        pnlHasilTolak.add(kartuHasil);

        kartuTolak.setLayout(new java.awt.BorderLayout(0, 14));
        pnlTolakJudul.setLayout(new java.awt.BorderLayout());
        lblTolakJudul.setText("Contoh penolakan otomatis");
        lblTolakJudul.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblTolakJudul.setForeground(new java.awt.Color(19, 38, 29));
        pnlTolakJudul.add(lblTolakJudul, java.awt.BorderLayout.WEST);

        pnlTolakJudul.add(badgeTolak, java.awt.BorderLayout.EAST);

        kartuTolak.add(pnlTolakJudul, java.awt.BorderLayout.NORTH);

        pnlTolakList.setLayout(new javax.swing.BoxLayout(pnlTolakList, javax.swing.BoxLayout.PAGE_AXIS));
        pnlTolakList.add(tolak1);

        pnlTolakList.add(tolak2);

        pnlTolakList.add(tolak3);

        pnlTolakList.add(tolak4);

        pnlTolakList.add(tolak5);

        kartuTolak.add(pnlTolakList, java.awt.BorderLayout.CENTER);

        pnlHasilTolak.add(kartuTolak);

        pnlCari.add(pnlHasilTolak, java.awt.BorderLayout.CENTER);

        pnlIsi.add(pnlCari, "cari");

        pnlPilih.setLayout(new java.awt.BorderLayout(20, 0));
        kartuBarang.setLayout(new java.awt.BorderLayout(0, 12));
        pnlBarangAtas.setLayout(new javax.swing.BoxLayout(pnlBarangAtas, javax.swing.BoxLayout.PAGE_AXIS));
        lblValidasi.setText("Validasi otomatis");
        lblValidasi.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblValidasi.setForeground(new java.awt.Color(19, 38, 29));
        pnlBarangAtas.add(lblValidasi);

        pnlTiga.setLayout(new java.awt.GridLayout(1, 3, 12, 0));
        pnlTiga.add(cekA);

        pnlTiga.add(cekB);

        pnlTiga.add(cekC);

        pnlBarangAtas.add(pnlTiga);

        lblPilih.setText("Pilih barang yang diretur");
        lblPilih.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblPilih.setForeground(new java.awt.Color(19, 38, 29));
        pnlBarangAtas.add(lblPilih);

        pnlKepalaBarang.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        pnlBarangAtas.add(pnlKepalaBarang);

        kartuBarang.add(pnlBarangAtas, java.awt.BorderLayout.NORTH);

        pnlBarang.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        kartuBarang.add(pnlBarang, java.awt.BorderLayout.CENTER);

        pnlBarangBawah.setLayout(new javax.swing.BoxLayout(pnlBarangBawah, javax.swing.BoxLayout.PAGE_AXIS));
        lblPesan.setText("Pesan penolakan otomatis ditampilkan untuk barang yang tidak memenuhi syarat.");
        lblPesan.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        lblPesan.setForeground(new java.awt.Color(107, 122, 114));
        pnlBarangBawah.add(lblPesan);

        pnlBarangBawah.add(pnlCatatan);

        kartuBarang.add(pnlBarangBawah, java.awt.BorderLayout.SOUTH);

        pnlPilih.add(kartuBarang, java.awt.BorderLayout.CENTER);

        kartuRetur.setPreferredSize(new java.awt.Dimension(372, 0));
        kartuRetur.setLayout(new java.awt.BorderLayout(0, 12));
        pnlReturAtas.setLayout(new javax.swing.BoxLayout(pnlReturAtas, javax.swing.BoxLayout.PAGE_AXIS));
        lblDetailRetur.setText("Detail retur");
        lblDetailRetur.setFont(new java.awt.Font("Poppins", 1, 17)); // NOI18N
        lblDetailRetur.setForeground(new java.awt.Color(19, 38, 29));
        pnlReturAtas.add(lblDetailRetur);

        lblAlasan.setText("Alasan");
        lblAlasan.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblAlasan.setForeground(new java.awt.Color(107, 122, 114));
        pnlReturAtas.add(lblAlasan);

        pnlChipsAlasan.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        pnlChipsAlasan.add(chipSalah);

        pnlChipsAlasan.add(chipRusak);

        pnlChipsAlasan.add(chipLain);

        pnlReturAtas.add(pnlChipsAlasan);

        lblKondisi.setText("Kondisi barang");
        lblKondisi.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblKondisi.setForeground(new java.awt.Color(107, 122, 114));
        pnlReturAtas.add(lblKondisi);

        pnlKond1.setLayout(new java.awt.BorderLayout(12, 0));
        pnlKond1.add(cbBelum, java.awt.BorderLayout.WEST);

        lblKond1.setText("Belum digunakan");
        lblKond1.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblKond1.setForeground(new java.awt.Color(19, 38, 29));
        pnlKond1.add(lblKond1, java.awt.BorderLayout.CENTER);

        pnlReturAtas.add(pnlKond1);

        pnlKond2.setLayout(new java.awt.BorderLayout(12, 0));
        pnlKond2.add(cbUtuh, java.awt.BorderLayout.WEST);

        lblKond2.setText("Kemasan utuh");
        lblKond2.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblKond2.setForeground(new java.awt.Color(19, 38, 29));
        pnlKond2.add(lblKond2, java.awt.BorderLayout.CENTER);

        pnlReturAtas.add(pnlKond2);

        pnlKond3.setLayout(new java.awt.BorderLayout(12, 0));
        pnlKond3.add(cbLayak, java.awt.BorderLayout.WEST);

        lblKond3.setText("Layak jual kembali");
        lblKond3.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblKond3.setForeground(new java.awt.Color(19, 38, 29));
        pnlKond3.add(lblKond3, java.awt.BorderLayout.CENTER);

        pnlReturAtas.add(pnlKond3);

        pnlRingkasan.setLayout(new java.awt.BorderLayout(0, 8));
        lblRingkasan.setText("Ringkasan refund");
        lblRingkasan.setFont(new java.awt.Font("Poppins", 1, 13)); // NOI18N
        lblRingkasan.setForeground(new java.awt.Color(19, 38, 29));
        pnlRingkasan.add(lblRingkasan, java.awt.BorderLayout.NORTH);

        pnlRingkasGrid.setLayout(new java.awt.GridLayout(3, 2, 0, 8));
        lblBarangT.setText("Barang");
        lblBarangT.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblBarangT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRingkasGrid.add(lblBarangT);

        rBarang.setText("-");
        rBarang.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        rBarang.setForeground(new java.awt.Color(19, 38, 29));
        rBarang.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRingkasGrid.add(rBarang);

        lblMetodeT.setText("Metode");
        lblMetodeT.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblMetodeT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRingkasGrid.add(lblMetodeT);

        rMetode.setText("Tunai");
        rMetode.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        rMetode.setForeground(new java.awt.Color(19, 38, 29));
        rMetode.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRingkasGrid.add(rMetode);

        lblPoinT.setText("Poin member");
        lblPoinT.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblPoinT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRingkasGrid.add(lblPoinT);

        rPoin.setText("-");
        rPoin.setFont(new java.awt.Font("Poppins", 1, 12)); // NOI18N
        rPoin.setForeground(new java.awt.Color(19, 38, 29));
        rPoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRingkasGrid.add(rPoin);

        pnlRingkasan.add(pnlRingkasGrid, java.awt.BorderLayout.CENTER);

        pnlRefund.setLayout(new java.awt.BorderLayout());
        lblRefundT.setText("Refund");
        lblRefundT.setFont(new java.awt.Font("Poppins", 0, 12)); // NOI18N
        lblRefundT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRefund.add(lblRefundT, java.awt.BorderLayout.WEST);

        rRefund.setText("Rp0");
        rRefund.setFont(new java.awt.Font("Poppins", 1, 22)); // NOI18N
        rRefund.setForeground(new java.awt.Color(15, 145, 88));
        rRefund.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRefund.add(rRefund, java.awt.BorderLayout.EAST);

        pnlRingkasan.add(pnlRefund, java.awt.BorderLayout.SOUTH);

        pnlReturAtas.add(pnlRingkasan);

        kartuRetur.add(pnlReturAtas, java.awt.BorderLayout.NORTH);

        pnlReturBawah.setLayout(new javax.swing.BoxLayout(pnlReturBawah, javax.swing.BoxLayout.PAGE_AXIS));
        pnlReturBawah.add(btnAjukan);

        pnlReturBawah.add(btnBatalRetur);

        lblStok.setText("Stok layak kembali ke lot asal");
        lblStok.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        lblStok.setForeground(new java.awt.Color(107, 122, 114));
        lblStok.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pnlReturBawah.add(lblStok);

        kartuRetur.add(pnlReturBawah, java.awt.BorderLayout.SOUTH);

        pnlPilih.add(kartuRetur, java.awt.BorderLayout.EAST);

        pnlIsi.add(pnlPilih, "pilih");

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
                javax.swing.JFrame f = new javax.swing.JFrame("SmartMart POS - SmartReturn");
                f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                SmartReturnPanel p = new SmartReturnPanel();
                p.muatContoh();
                f.setContentPane(UiKit.bungkusScroll(p));
                f.setSize(1120, 860);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel badgeHasil;
    private javax.swing.JLabel badgeTolak;
    private javax.swing.JPanel barInfo;
    private javax.swing.JButton btnAjukan;
    private javax.swing.JButton btnBatalRetur;
    private javax.swing.JButton btnCari;
    private javax.swing.JButton btnLanjut;
    private javax.swing.JToggleButton cbBelum;
    private javax.swing.JToggleButton cbLayak;
    private javax.swing.JToggleButton cbUtuh;
    private javax.swing.JPanel cek1;
    private javax.swing.JPanel cek2;
    private javax.swing.JPanel cek3;
    private javax.swing.JPanel cekA;
    private javax.swing.JPanel cekB;
    private javax.swing.JPanel cekC;
    private javax.swing.JToggleButton chipId;
    private javax.swing.JToggleButton chipLain;
    private javax.swing.JToggleButton chipRusak;
    private javax.swing.JToggleButton chipSalah;
    private javax.swing.JToggleButton chipScan;
    private javax.swing.JPanel fldCari;
    private javax.swing.JLabel hId;
    private javax.swing.JLabel hInfo1;
    private javax.swing.JLabel hInfo2;
    private javax.swing.JLabel hStatus;
    private javax.swing.JLabel hTotal;
    private javax.swing.JPanel kartuBarang;
    private javax.swing.JPanel kartuCari;
    private javax.swing.JPanel kartuHasil;
    private javax.swing.JPanel kartuRetur;
    private javax.swing.JPanel kartuTolak;
    private javax.swing.JLabel lblAlasan;
    private javax.swing.JLabel lblBarangT;
    private javax.swing.JLabel lblCariJudul;
    private javax.swing.JLabel lblCatCari;
    private javax.swing.JLabel lblDetailRetur;
    private javax.swing.JLabel lblHasilJudul;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblKond1;
    private javax.swing.JLabel lblKond2;
    private javax.swing.JLabel lblKond3;
    private javax.swing.JLabel lblKondisi;
    private javax.swing.JLabel lblMetodeT;
    private javax.swing.JLabel lblPesan;
    private javax.swing.JLabel lblPilih;
    private javax.swing.JLabel lblPoinT;
    private javax.swing.JLabel lblRefundT;
    private javax.swing.JLabel lblRingkasan;
    private javax.swing.JLabel lblStok;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTolakJudul;
    private javax.swing.JLabel lblValidasi;
    private javax.swing.JPanel pnlBarang;
    private javax.swing.JPanel pnlBarangAtas;
    private javax.swing.JPanel pnlBarangBawah;
    private javax.swing.JPanel pnlCari;
    private javax.swing.JPanel pnlCariAtas;
    private javax.swing.JPanel pnlCariBaris;
    private javax.swing.JPanel pnlCatatan;
    private javax.swing.JPanel pnlChipsAlasan;
    private javax.swing.JPanel pnlChipsCari;
    private javax.swing.JPanel pnlHasilAtas;
    private javax.swing.JPanel pnlHasilBawah;
    private javax.swing.JPanel pnlHasilBox;
    private javax.swing.JPanel pnlHasilJudul;
    private javax.swing.JPanel pnlHasilKanan;
    private javax.swing.JPanel pnlHasilKiri;
    private javax.swing.JPanel pnlHasilTolak;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlJudulBlok;
    private javax.swing.JPanel pnlKepalaBarang;
    private javax.swing.JPanel pnlKond1;
    private javax.swing.JPanel pnlKond2;
    private javax.swing.JPanel pnlKond3;
    private javax.swing.JPanel pnlPilih;
    private javax.swing.JPanel pnlRefund;
    private javax.swing.JPanel pnlReturAtas;
    private javax.swing.JPanel pnlReturBawah;
    private javax.swing.JPanel pnlRingkasGrid;
    private javax.swing.JPanel pnlRingkasan;
    private javax.swing.JPanel pnlStatusWrap;
    private javax.swing.JPanel pnlTiga;
    private javax.swing.JPanel pnlTolakJudul;
    private javax.swing.JPanel pnlTolakList;
    private javax.swing.JLabel rBarang;
    private javax.swing.JLabel rMetode;
    private javax.swing.JLabel rPoin;
    private javax.swing.JLabel rRefund;
    private javax.swing.JPanel tolak1;
    private javax.swing.JPanel tolak2;
    private javax.swing.JPanel tolak3;
    private javax.swing.JPanel tolak4;
    private javax.swing.JPanel tolak5;
    // End of variables declaration//GEN-END:variables
}
