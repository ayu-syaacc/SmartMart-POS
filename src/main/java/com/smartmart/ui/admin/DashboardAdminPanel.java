package com.smartmart.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import com.smartmart.model.DashboardData;
import com.smartmart.model.DashboardData.LogStok;
import com.smartmart.model.DashboardData.PenjualanHarian;
import com.smartmart.model.DashboardData.PeringatanStok;
import com.smartmart.service.DashboardService;
import com.smartmart.ui.kasir.UiKit;
import com.smartmart.util.FormatRupiah;

/**
 * Dashboard Admin: ringkasan operasional hari ini.
 *
 * Isi: empat kartu ringkasan (omzet, transaksi, kerugian, stok kritis), grafik penjualan
 * 7 hari terakhir, catatan stok terbaru (LossGuard), dan peringatan stok per lot (StockAlert).
 *
 * Data dimuat di thread latar (SwingWorker), dimuat ulang tiap kali halaman ditampilkan
 * dan otomatis setiap menit selama halaman terlihat.
 *
 * Panel ini memakai tema terang sendiri (warna dari UiKit), sama seperti panel kasir,
 * sehingga MainFrame tidak mengubah warnanya saat tema gelap dipilih.
 */
public class DashboardAdminPanel extends JPanel {

    private static final Locale ID = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", ID);
    private static final DateTimeFormatter FORMAT_TANGGAL_PENDEK = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", ID);
    private static final DateTimeFormatter FORMAT_JAM = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FORMAT_JAM_MENIT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMAT_TANGGAL_JAM = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private static final int INTERVAL_REFRESH_MS = 60_000;
    private static final int MAKS_PERINGATAN_TAMPIL = 6;

    private static final Color HIJAU_MUDA = new Color(168, 214, 187);
    private static final Color ORANYE_BG = new Color(255, 232, 218);
    private static final Color ORANYE_TEKS = new Color(168, 62, 12);
    private static final Color BIRU_TEKS = new Color(30, 64, 175);

    private final DashboardService service;
    private final Consumer<String> navigasi;
    private final Timer timerRefresh;

    private boolean sedangMemuat;

    private final JLabel lblTanggal = new JLabel(" ");
    private final JLabel lblDiperbarui = new JLabel(" ");
    private final JButton btnMuatUlang = new JButton("Muat Ulang");
    private final JLabel lblGalat = new JLabel();

    private final KartuStat kartuOmzet =
            new KartuStat("Omzet hari ini", FontAwesomeSolid.CASH_REGISTER, UiKit.GREEN, UiKit.GREEN_SOFT);
    private final KartuStat kartuTransaksi =
            new KartuStat("Transaksi", FontAwesomeSolid.SHOPPING_CART, UiKit.BLUE, UiKit.BLUE_BG);
    private final KartuStat kartuKerugian =
            new KartuStat("Kerugian hari ini", FontAwesomeSolid.SHIELD_ALT, UiKit.RED_TEXT, UiKit.RED_SOFT);
    private final KartuStat kartuStokKritis =
            new KartuStat("Stok kritis", FontAwesomeSolid.EXCLAMATION_TRIANGLE, UiKit.AMBER_TEXT, UiKit.AMBER_BG);

    private final GrafikBatang grafik = new GrafikBatang();
    private final JPanel daftarLog = barisVertikal();
    private final JPanel daftarPeringatan = barisVertikal();

    /**
     * @param navigasi dipanggil dengan nama menu sidebar (mis. "StockAlert") saat tautan
     *                 "Lihat semua" ditekan
     */
    public DashboardAdminPanel(Consumer<String> navigasi) {
        this(navigasi, new DashboardService());
    }

    public DashboardAdminPanel(Consumer<String> navigasi, DashboardService service) {

        this.navigasi = navigasi;
        this.service = service;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UiKit.BG);

        add(buatHeader(), BorderLayout.NORTH);
        add(buatIsi(), BorderLayout.CENTER);

        timerRefresh = new Timer(INTERVAL_REFRESH_MS, e -> muatData());
        timerRefresh.setRepeats(true);

        // Muat saat halaman terlihat (juga saat kembali dari menu lain); berhenti saat tersembunyi.
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    muatData();
                    timerRefresh.restart();
                } else {
                    timerRefresh.stop();
                }
            }
        });

        tampilkanKosong();
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel buatHeader() {

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(22, 28, 14, 28));

        JLabel judul = new JLabel("Dashboard Admin");
        judul.setFont(f(Font.BOLD, 25));
        judul.setForeground(UiKit.TEXT);

        JLabel deskripsi = new JLabel("Ringkasan operasional hari ini");
        deskripsi.setFont(f(Font.PLAIN, 13));
        deskripsi.setForeground(UiKit.MUTED);

        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
        kiri.add(judul);
        kiri.add(Box.createVerticalStrut(4));
        kiri.add(deskripsi);

        lblTanggal.setFont(f(Font.BOLD, 13));
        lblTanggal.setForeground(UiKit.TEXT);
        lblTanggal.setHorizontalAlignment(SwingConstants.RIGHT);

        lblDiperbarui.setFont(f(Font.PLAIN, 11));
        lblDiperbarui.setForeground(UiKit.MUTED);
        lblDiperbarui.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel waktu = new JPanel();
        waktu.setOpaque(false);
        waktu.setLayout(new BoxLayout(waktu, BoxLayout.Y_AXIS));
        lblTanggal.setAlignmentX(Component.RIGHT_ALIGNMENT);
        lblDiperbarui.setAlignmentX(Component.RIGHT_ALIGNMENT);
        waktu.add(lblTanggal);
        waktu.add(Box.createVerticalStrut(3));
        waktu.add(lblDiperbarui);

        btnMuatUlang.setFont(f(Font.BOLD, 12));
        btnMuatUlang.setForeground(Color.WHITE);
        btnMuatUlang.setBackground(UiKit.GREEN);
        btnMuatUlang.setOpaque(true);
        btnMuatUlang.setFocusPainted(false);
        btnMuatUlang.setBorderPainted(false);
        btnMuatUlang.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMuatUlang.setBorder(new EmptyBorder(9, 16, 9, 16));
        btnMuatUlang.addActionListener(e -> muatData());

        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0;
        g.insets = new Insets(0, 0, 0, 14);
        kanan.add(waktu, g);
        g.insets = new Insets(0, 0, 0, 0);
        kanan.add(btnMuatUlang, g);

        header.add(kiri, BorderLayout.WEST);
        header.add(kanan, BorderLayout.EAST);

        return header;
    }

    // ==========================================
    // ISI HALAMAN
    // ==========================================

    private JScrollPane buatIsi() {

        AreaGulir isi = new AreaGulir(new BorderLayout(0, 16));
        isi.setOpaque(false);
        isi.setBorder(new EmptyBorder(2, 28, 28, 28));

        // Banner galat (tersembunyi bila tidak ada masalah) + kartu ringkasan
        lblGalat.setFont(f(Font.BOLD, 12));
        lblGalat.setForeground(UiKit.RED_TEXT);
        lblGalat.setOpaque(true);
        lblGalat.setBackground(UiKit.RED_SOFT);
        lblGalat.setBorder(new EmptyBorder(10, 14, 10, 14));
        lblGalat.setVisible(false);

        JPanel baris4 = new JPanel(new GridLayout(1, 4, 16, 0));
        baris4.setOpaque(false);
        baris4.add(kartuOmzet);
        baris4.add(kartuTransaksi);
        baris4.add(kartuKerugian);
        baris4.add(kartuStokKritis);

        JPanel atas = new JPanel(new BorderLayout(0, 12));
        atas.setOpaque(false);
        atas.add(lblGalat, BorderLayout.NORTH);
        atas.add(baris4, BorderLayout.CENTER);

        // Kolom kiri: grafik + log stok. Kolom kanan: peringatan stok.
        JPanel kolomKiri = new KolomProporsi();
        kolomKiri.setLayout(new BoxLayout(kolomKiri, BoxLayout.Y_AXIS));

        JPanel kartuGrafik = kartuSeksi("Penjualan 7 hari terakhir", FontAwesomeSolid.CHART_BAR, UiKit.GREEN,
                null, null, grafik);
        JPanel kartuLog = kartuSeksi("Log LossGuard terbaru", FontAwesomeSolid.SHIELD_ALT, UiKit.RED_TEXT,
                "Lihat semua", "LossGuard", daftarLog);

        kartuGrafik.setAlignmentX(Component.LEFT_ALIGNMENT);
        kartuLog.setAlignmentX(Component.LEFT_ALIGNMENT);
        kolomKiri.add(kartuGrafik);
        kolomKiri.add(Box.createVerticalStrut(16));
        kolomKiri.add(kartuLog);

        JPanel kartuPeringatan = kartuSeksi("StockAlert per lot", FontAwesomeSolid.EXCLAMATION_TRIANGLE,
                UiKit.AMBER, "Lihat semua", "StockAlert", daftarPeringatan);

        JPanel kolomKanan = new KolomProporsi();
        kolomKanan.setLayout(new BorderLayout());
        kolomKanan.add(kartuPeringatan, BorderLayout.CENTER);

        JPanel tengah = new JPanel(new GridBagLayout());
        tengah.setOpaque(false);

        susunTengah(tengah, kolomKiri, kolomKanan, false);

        isi.add(atas, BorderLayout.NORTH);
        isi.add(tengah, BorderLayout.CENTER);

        // Responsif. Lebar isi = lebar halaman dikurangi 56 px bantalan kiri-kanan.
        //  - di bawah 860 px: kartu ringkasan 2 x 2 (angka tidak terpotong)
        //  - di bawah 900 px: grafik/log dan StockAlert ditumpuk, bukan berdampingan
        isi.addComponentListener(new ComponentAdapter() {
            private Boolean dua;
            private Boolean tumpuk;

            @Override
            public void componentResized(ComponentEvent e) {
                boolean kartuDua = isi.getWidth() < 860;
                boolean kolomTumpuk = isi.getWidth() < 900;
                if (Boolean.valueOf(kartuDua).equals(dua) && Boolean.valueOf(kolomTumpuk).equals(tumpuk)) {
                    return;
                }
                dua = kartuDua;
                tumpuk = kolomTumpuk;
                baris4.setLayout(kartuDua ? new GridLayout(2, 2, 16, 16) : new GridLayout(1, 4, 16, 0));
                susunTengah(tengah, kolomKiri, kolomKanan, kolomTumpuk);
                isi.revalidate();
            }
        });

        return UiKit.bungkusScroll(isi);
    }

    /** Menyusun dua kolom bagian tengah: berdampingan (58 : 42) atau bertumpuk. */
    private static void susunTengah(JPanel tengah, JComponent kiri, JComponent kanan, boolean tumpuk) {

        tengah.removeAll();

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.NORTHWEST;

        if (tumpuk) {
            g.gridx = 0;
            g.weightx = 1;
            g.weighty = 0;
            g.gridy = 0;
            g.insets = new Insets(0, 0, 16, 0);
            tengah.add(kiri, g);
            g.gridy = 1;
            g.weighty = 1;
            g.insets = new Insets(0, 0, 0, 0);
            tengah.add(kanan, g);
        } else {
            g.gridy = 0;
            g.weighty = 1;
            g.gridx = 0;
            g.weightx = 0.58;
            g.insets = new Insets(0, 0, 0, 8);
            tengah.add(kiri, g);
            g.gridx = 1;
            g.weightx = 0.42;
            g.insets = new Insets(0, 8, 0, 0);
            tengah.add(kanan, g);
        }

        tengah.revalidate();
    }

    private JPanel kartuSeksi(String judul, FontAwesomeSolid ikon, Color warnaIkon,
                              String teksTautan, String tujuan, JComponent isi) {

        UiKit.Card kartu = new UiKit.Card();
        kartu.setLayout(new BorderLayout(0, 12));

        JLabel lblJudul = new JLabel(judul, FontIcon.of(ikon, 16, warnaIkon), SwingConstants.LEFT);
        lblJudul.setIconTextGap(10);
        lblJudul.setFont(f(Font.BOLD, 15));
        lblJudul.setForeground(UiKit.TEXT);

        JPanel kepala = new JPanel(new BorderLayout());
        kepala.setOpaque(false);
        kepala.add(lblJudul, BorderLayout.WEST);

        if (teksTautan != null) {
            JButton tautan = new JButton(teksTautan);
            tautan.setFont(f(Font.BOLD, 12));
            tautan.setForeground(UiKit.GREEN);
            tautan.setContentAreaFilled(false);
            tautan.setBorderPainted(false);
            tautan.setFocusPainted(false);
            tautan.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tautan.setBorder(new EmptyBorder(2, 6, 2, 0));
            tautan.addActionListener(e -> {
                if (navigasi != null) {
                    navigasi.accept(tujuan);
                }
            });
            kepala.add(tautan, BorderLayout.EAST);
        }

        kartu.add(kepala, BorderLayout.NORTH);
        kartu.add(isi, BorderLayout.CENTER);

        return kartu;
    }

    private static JPanel barisVertikal() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    // ==========================================
    // MEMUAT DATA
    // ==========================================

    private void muatData() {

        if (sedangMemuat) {
            return;
        }

        sedangMemuat = true;
        btnMuatUlang.setEnabled(false);
        btnMuatUlang.setText("Memuat...");

        new SwingWorker<DashboardData, Void>() {

            @Override
            protected DashboardData doInBackground() throws Exception {
                return service.ambilData();
            }

            @Override
            protected void done() {
                sedangMemuat = false;
                btnMuatUlang.setEnabled(true);
                btnMuatUlang.setText("Muat Ulang");

                try {
                    tampilkan(get());
                    lblGalat.setVisible(false);
                } catch (Exception e) {
                    Throwable sebab = e.getCause() != null ? e.getCause() : e;
                    lblGalat.setText("Gagal memuat data dashboard: " + sebab.getMessage());
                    lblGalat.setVisible(true);
                }
            }
        }.execute();
    }

    // ==========================================
    // MENAMPILKAN DATA
    // ==========================================

    private void tampilkanKosong() {
        lblTanggal.setText(LocalDate.now().format(FORMAT_TANGGAL));
        kartuOmzet.set("Rp0", UiKit.TEXT, "Memuat data...", UiKit.MUTED);
        kartuTransaksi.set("0", UiKit.TEXT, "Memuat data...", UiKit.MUTED);
        kartuKerugian.set("Rp0", UiKit.TEXT, "Memuat data...", UiKit.MUTED);
        kartuStokKritis.set("0 produk", UiKit.TEXT, "Memuat data...", UiKit.MUTED);
    }

    private void tampilkan(DashboardData d) {

        lblTanggal.setText(LocalDate.now().format(FORMAT_TANGGAL));
        lblDiperbarui.setText("Diperbarui " + d.dimuatPada.format(FORMAT_JAM));

        // Omzet
        long omzet = d.hariIni.omzetBersih();
        Integer persen = d.persenPerubahanOmzet();
        if (persen != null) {
            boolean naik = persen >= 0;
            kartuOmzet.set(FormatRupiah.format(omzet), UiKit.TEXT,
                    (naik ? "\u25B2 +" : "\u25BC ") + persen + "% dari kemarin",
                    naik ? UiKit.GREEN : UiKit.RED_TEXT);
        } else {
            kartuOmzet.set(FormatRupiah.format(omzet), UiKit.TEXT,
                    omzet == 0 ? "Belum ada penjualan" : "Kemarin belum ada penjualan", UiKit.MUTED);
        }

        // Transaksi
        kartuTransaksi.set(String.valueOf(d.hariIni.jumlahTransaksi), UiKit.TEXT,
                d.hariIni.jumlahTransaksi == 0
                        ? "Belum ada transaksi"
                        : "Rata-rata " + FormatRupiah.format(d.hariIni.rataRata()),
                UiKit.MUTED);

        // Kerugian
        kartuKerugian.set(FormatRupiah.format(d.kerugianHariIni),
                d.kerugianHariIni > 0 ? UiKit.RED_TEXT : UiKit.TEXT,
                "Kedaluwarsa, rusak, hilang", UiKit.MUTED);

        // Stok kritis
        if (d.stokKritis > 0) {
            kartuStokKritis.set(d.stokKritis + " produk", UiKit.AMBER_TEXT, "Perlu tindakan", UiKit.AMBER_TEXT);
        } else {
            kartuStokKritis.set("0 produk", UiKit.GREEN, "Semua stok aman", UiKit.GREEN);
        }

        grafik.setData(d.tujuhHari);
        isiLog(d.logTerbaru);
        isiPeringatan(d.peringatan);
    }

    private void isiLog(List<LogStok> log) {

        daftarLog.removeAll();

        if (log.isEmpty()) {
            daftarLog.add(pesanKosong("Belum ada catatan perubahan stok."));
        }

        LocalDate hariIni = LocalDate.now();

        for (int i = 0; i < log.size(); i++) {
            LogStok l = log.get(i);

            String waktu = l.waktu == null ? "-"
                    : l.waktu.toLocalDate().equals(hariIni)
                            ? l.waktu.format(FORMAT_JAM_MENIT)
                            : l.waktu.format(FORMAT_TANGGAL_JAM);

            UiKit.Badge badge = badgeJenis(l.jenis);

            JLabel lblWaktu = new JLabel(waktu);
            lblWaktu.setFont(f(Font.PLAIN, 12));
            lblWaktu.setForeground(UiKit.MUTED);
            lblWaktu.setPreferredSize(new Dimension(74, 20));

            JPanel kolomBadge = new JPanel(new BorderLayout());
            kolomBadge.setOpaque(false);
            kolomBadge.setPreferredSize(new Dimension(120, 26));
            kolomBadge.add(badge, BorderLayout.WEST);

            JLabel lblProduk = new JLabel(l.namaProduk + "  " + formatJumlah(l.jumlah, true));
            lblProduk.setFont(f(Font.BOLD, 12));
            lblProduk.setForeground(UiKit.TEXT);
            lblProduk.setToolTipText(l.alasan);

            JLabel lblPelaku = new JLabel(l.pelaku);
            lblPelaku.setFont(f(Font.PLAIN, 12));
            lblPelaku.setForeground(UiKit.MUTED);
            lblPelaku.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel baris = new JPanel(new GridBagLayout());
            baris.setOpaque(false);
            baris.setBorder(pembatas(i < log.size() - 1));

            GridBagConstraints g = new GridBagConstraints();
            g.gridy = 0;
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.HORIZONTAL;

            g.gridx = 0;
            g.weightx = 0;
            baris.add(lblWaktu, g);
            g.gridx = 1;
            g.insets = new Insets(0, 0, 0, 10);
            baris.add(kolomBadge, g);
            g.gridx = 2;
            g.weightx = 1;
            g.insets = new Insets(0, 0, 0, 10);
            baris.add(lblProduk, g);
            g.gridx = 3;
            g.weightx = 0;
            g.insets = new Insets(0, 0, 0, 0);
            baris.add(lblPelaku, g);

            tambahBaris(daftarLog, baris);
        }

        daftarLog.revalidate();
        daftarLog.repaint();
    }

    private void isiPeringatan(List<PeringatanStok> peringatan) {

        daftarPeringatan.removeAll();

        if (peringatan.isEmpty()) {
            daftarPeringatan.add(pesanKosong("Tidak ada peringatan. Semua lot dan stok aman."));
        }

        int tampil = Math.min(peringatan.size(), MAKS_PERINGATAN_TAMPIL);

        for (int i = 0; i < tampil; i++) {
            PeringatanStok p = peringatan.get(i);

            JLabel lblNama = new JLabel(p.namaProduk);
            lblNama.setFont(f(Font.BOLD, 13));
            lblNama.setForeground(UiKit.TEXT);

            JLabel lblDetail = new JLabel(detailPeringatan(p));
            lblDetail.setFont(f(Font.PLAIN, 11));
            lblDetail.setForeground(UiKit.MUTED);
            lblDetail.setToolTipText(lblDetail.getText());

            JPanel teks = new JPanel();
            teks.setOpaque(false);
            teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
            teks.add(lblNama);
            teks.add(Box.createVerticalStrut(2));
            teks.add(lblDetail);

            JPanel baris = new JPanel(new BorderLayout(10, 0));
            baris.setOpaque(false);
            baris.setBorder(pembatas(i < tampil - 1 || peringatan.size() > tampil));
            baris.add(teks, BorderLayout.CENTER);
            baris.add(badgePeringatan(p.status), BorderLayout.EAST);

            tambahBaris(daftarPeringatan, baris);
        }

        if (peringatan.size() > tampil) {
            JLabel lainnya = new JLabel("+" + (peringatan.size() - tampil) + " peringatan lainnya");
            lainnya.setFont(f(Font.PLAIN, 12));
            lainnya.setForeground(UiKit.MUTED);
            lainnya.setBorder(new EmptyBorder(10, 0, 0, 0));
            lainnya.setAlignmentX(Component.LEFT_ALIGNMENT);
            daftarPeringatan.add(lainnya);
        }

        daftarPeringatan.add(Box.createVerticalGlue());
        daftarPeringatan.revalidate();
        daftarPeringatan.repaint();
    }

    private static String detailPeringatan(PeringatanStok p) {

        if (DashboardData.MENIPIS.equals(p.status)) {
            return "Stok " + formatJumlah(p.jumlah, false) + " | minimum " + formatJumlah(p.stokMinimum, false);
        }

        int hari = p.sisaHari == null ? 0 : p.sisaHari;
        String masa = hari < 0 ? "lewat " + (-hari) + " hari" : "sisa " + hari + " hari";

        return "Lot #" + p.idLot + " | " + masa + " | jumlah " + formatJumlah(p.jumlah, false);
    }

    private static void tambahBaris(JPanel daftar, JPanel baris) {
        baris.setAlignmentX(Component.LEFT_ALIGNMENT);
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, baris.getPreferredSize().height));
        daftar.add(baris);
    }

    private static javax.swing.border.Border pembatas(boolean denganGaris) {
        return BorderFactory.createCompoundBorder(
                denganGaris ? BorderFactory.createMatteBorder(0, 0, 1, 0, UiKit.LINE) : BorderFactory.createEmptyBorder(),
                new EmptyBorder(10, 0, 10, 0));
    }

    private static JLabel pesanKosong(String teks) {
        JLabel l = new JLabel(teks);
        l.setFont(f(Font.PLAIN, 12));
        l.setForeground(UiKit.MUTED);
        l.setBorder(new EmptyBorder(14, 0, 14, 0));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ==========================================
    // BADGE
    // ==========================================

    private static UiKit.Badge badgePeringatan(String status) {
        switch (status) {
            case DashboardData.KEDALUWARSA:
                return new UiKit.Badge(status, UiKit.RED_SOFT, UiKit.RED_TEXT);
            case DashboardData.SEGERA:
                return new UiKit.Badge(status, ORANYE_BG, ORANYE_TEKS);
            case DashboardData.PRIORITAS:
                return new UiKit.Badge(status, UiKit.AMBER_BG, UiKit.AMBER_TEXT);
            case DashboardData.PERHATIAN:
                return new UiKit.Badge(status, UiKit.BLUE_BG, BIRU_TEKS);
            default:
                return new UiKit.Badge(status, UiKit.GRAY_SOFT, UiKit.MUTED);
        }
    }

    private static UiKit.Badge badgeJenis(String jenis) {
        String j = jenis == null ? "" : jenis.trim().toUpperCase(Locale.ROOT);
        switch (j) {
            case "MASUK":
                return new UiKit.Badge(j, UiKit.GREEN_SOFT, UiKit.GREEN);
            case "TERJUAL":
                return new UiKit.Badge(j, UiKit.BLUE_BG, BIRU_TEKS);
            case "RETUR":
            case "DIRETUR":
                return new UiKit.Badge("DIRETUR", UiKit.AMBER_BG, UiKit.AMBER_TEXT);
            case "RUSAK":
            case "KEDALUWARSA":
            case "HILANG":
                return new UiKit.Badge(j, UiKit.RED_SOFT, UiKit.RED_TEXT);
            default:
                return new UiKit.Badge(j.isEmpty() ? "-" : j, UiKit.GRAY_SOFT, UiKit.MUTED);
        }
    }

    // ==========================================
    // FORMAT
    // ==========================================

    private static Font f(int gaya, int ukuran) {
        return new Font("Arial", gaya, ukuran);
    }

    /** 6 menjadi "6", 0,75 menjadi "0,75"; bila {@code bertanda}, ditambah awalan + atau -. */
    private static String formatJumlah(double nilai, boolean bertanda) {
        NumberFormat nf = NumberFormat.getNumberInstance(ID);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(true);
        String angka = nf.format(Math.abs(nilai));
        if (!bertanda) {
            return nilai < 0 ? "-" + angka : angka;
        }
        return (nilai < 0 ? "-" : "+") + angka;
    }

    /** 4.820.000 menjadi "4,8 jt", 850.000 menjadi "850 rb". */
    private static String ringkasRupiah(long nilai) {
        long n = Math.abs(nilai);
        String teks;
        if (n >= 1_000_000) {
            double juta = n / 1_000_000.0;
            teks = Math.abs(juta - Math.rint(juta)) < 0.05
                    ? String.format(ID, "%.0f jt", juta)
                    : String.format(ID, "%.1f jt", juta);
        } else if (n >= 1_000) {
            teks = Math.round(n / 1_000.0) + " rb";
        } else {
            teks = String.valueOf(n);
        }
        return nilai < 0 ? "-" + teks : teks;
    }

    // ==========================================
    // KARTU RINGKASAN
    // ==========================================

    private static class KartuStat extends UiKit.Card {

        private final JLabel lblNilai = new JLabel(" ");
        private final JLabel lblKeterangan = new JLabel(" ");

        KartuStat(String judul, FontAwesomeSolid ikon, Color warnaIkon, Color latarIkon) {

            setLayout(new BorderLayout(0, 8));
            setBorder(new EmptyBorder(16, 18, 16, 18));

            JLabel lblJudul = new JLabel(judul);
            lblJudul.setFont(f(Font.BOLD, 12));
            lblJudul.setForeground(UiKit.MUTED);

            JLabel lblIkon = new JLabel(new IkonBulat(ikon, 34, latarIkon, warnaIkon));

            JPanel atas = new JPanel(new BorderLayout(8, 0));
            atas.setOpaque(false);
            atas.add(lblJudul, BorderLayout.CENTER);
            atas.add(lblIkon, BorderLayout.EAST);

            lblNilai.setFont(f(Font.BOLD, 22));
            lblNilai.setForeground(UiKit.TEXT);

            lblKeterangan.setFont(f(Font.PLAIN, 12));
            lblKeterangan.setForeground(UiKit.MUTED);

            add(atas, BorderLayout.NORTH);
            add(lblNilai, BorderLayout.CENTER);
            add(lblKeterangan, BorderLayout.SOUTH);
        }

        void set(String nilai, Color warnaNilai, String keterangan, Color warnaKeterangan) {
            lblNilai.setText(nilai);
            lblNilai.setForeground(warnaNilai);
            lblKeterangan.setText(keterangan);
            lblKeterangan.setForeground(warnaKeterangan);
        }
    }

    /** Ikon FontAwesome di dalam lingkaran berwarna. */
    private static class IkonBulat implements Icon {

        private final Icon isi;
        private final int ukuran;
        private final Color latar;

        IkonBulat(FontAwesomeSolid ikon, int ukuran, Color latar, Color warna) {
            this.isi = FontIcon.of(ikon, Math.round(ukuran * 0.45f), warna);
            this.ukuran = ukuran;
            this.latar = latar;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(latar);
            g2.fillOval(x, y, ukuran, ukuran);
            g2.dispose();
            isi.paintIcon(c, g, x + (ukuran - isi.getIconWidth()) / 2, y + (ukuran - isi.getIconHeight()) / 2);
        }

        @Override
        public int getIconWidth() {
            return ukuran;
        }

        @Override
        public int getIconHeight() {
            return ukuran;
        }
    }

    // ==========================================
    // GRAFIK BATANG
    // ==========================================

    /** Grafik batang omzet bersih 7 hari; hari ini berwarna hijau tua. Tooltip menampilkan nilai lengkap. */
    private static class GrafikBatang extends JComponent {

        private static final int MARGIN_ATAS = 22;
        private static final int MARGIN_BAWAH = 26;
        private static final int MARGIN_SAMPING = 4;

        private List<PenjualanHarian> data = Collections.emptyList();

        GrafikBatang() {
            setPreferredSize(new Dimension(300, 200));
            setToolTipText(" "); // mendaftarkan komponen ke ToolTipManager
        }

        void setData(List<PenjualanHarian> data) {
            this.data = data == null ? Collections.<PenjualanHarian>emptyList() : data;
            repaint();
        }

        private int indeksDi(int x) {
            if (data.isEmpty()) {
                return -1;
            }
            double lebarSlot = (getWidth() - 2.0 * MARGIN_SAMPING) / data.size();
            int i = (int) ((x - MARGIN_SAMPING) / lebarSlot);
            return i >= 0 && i < data.size() ? i : -1;
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int i = indeksDi(e.getX());
            if (i < 0) {
                return null;
            }
            PenjualanHarian p = data.get(i);
            return p.tanggal.format(FORMAT_TANGGAL_PENDEK) + ": " + FormatRupiah.format(p.omzetBersih())
                    + " (" + p.jumlahTransaksi + " transaksi)";
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int lebar = getWidth();
            int tinggi = getHeight();
            int dasar = tinggi - MARGIN_BAWAH;
            int areaTinggi = Math.max(10, dasar - MARGIN_ATAS);

            // Garis dasar
            g2.setColor(UiKit.LINE);
            g2.drawLine(MARGIN_SAMPING, dasar, lebar - MARGIN_SAMPING, dasar);

            if (data.isEmpty()) {
                g2.dispose();
                return;
            }

            long maks = 0;
            for (PenjualanHarian p : data) {
                maks = Math.max(maks, p.omzetBersih());
            }

            double lebarSlot = (lebar - 2.0 * MARGIN_SAMPING) / data.size();
            int lebarBatang = (int) Math.min(46, lebarSlot * 0.52);

            Font fontNilai = f(Font.BOLD, 11);
            Font fontHari = f(Font.PLAIN, 11);
            Font fontHariIni = f(Font.BOLD, 11);

            for (int i = 0; i < data.size(); i++) {

                PenjualanHarian p = data.get(i);
                boolean hariIni = i == data.size() - 1;

                int pusat = (int) Math.round(MARGIN_SAMPING + lebarSlot * i + lebarSlot / 2);
                long nilai = Math.max(0, p.omzetBersih());

                int tinggiBatang = 0;
                if (nilai > 0 && maks > 0) {
                    tinggiBatang = Math.max(4, (int) Math.round(areaTinggi * (nilai / (double) maks)));
                }

                if (tinggiBatang > 0) {
                    // Hanya sudut atas yang membulat: gambar kotak bulat lalu potong di garis dasar.
                    g2.setClip(0, 0, lebar, dasar);
                    g2.setColor(hariIni ? UiKit.GREEN : HIJAU_MUDA);
                    g2.fill(new RoundRectangle2D.Float(pusat - lebarBatang / 2f, dasar - tinggiBatang,
                            lebarBatang, tinggiBatang + 10, 10, 10));
                    g2.setClip(null);
                }

                // Nilai di atas batang
                g2.setFont(fontNilai);
                g2.setColor(hariIni ? UiKit.TEXT : UiKit.MUTED);
                String teksNilai = nilai == 0 ? "0" : ringkasRupiah(nilai);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(teksNilai, pusat - fm.stringWidth(teksNilai) / 2, dasar - tinggiBatang - 6);

                // Nama hari di bawah garis dasar
                g2.setFont(hariIni ? fontHariIni : fontHari);
                g2.setColor(hariIni ? UiKit.GREEN : UiKit.MUTED);
                String hari = p.tanggal.getDayOfWeek().getDisplayName(TextStyle.SHORT, ID);
                fm = g2.getFontMetrics();
                g2.drawString(hari, pusat - fm.stringWidth(hari) / 2, dasar + 17);
            }

            if (maks <= 0) {
                g2.setFont(f(Font.PLAIN, 12));
                g2.setColor(UiKit.MUTED);
                String pesan = "Belum ada penjualan dalam 7 hari terakhir";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pesan, (lebar - fm.stringWidth(pesan)) / 2, MARGIN_ATAS + areaTinggi / 2);
            }

            g2.dispose();
        }
    }

    // ==========================================
    // KOLOM PROPORSIONAL
    // ==========================================

    /**
     * Kolom yang lebarnya hanya ditentukan oleh bobot GridBagLayout, bukan oleh isinya,
     * sehingga perbandingan kolom tidak bergeser saat data selesai dimuat.
     */
    private static class KolomProporsi extends JPanel {

        KolomProporsi() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1, super.getPreferredSize().height);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(1, super.getMinimumSize().height);
        }
    }

    // ==========================================
    // AREA GULIR
    // ==========================================

    /** Panel isi yang selalu selebar jendela (tidak ada gulir horizontal), dan setinggi jendela bila isinya lebih pendek. */
    private static class AreaGulir extends JPanel implements Scrollable {

        AreaGulir(BorderLayout layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(40, visibleRect.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport
                    && getParent().getHeight() > getPreferredSize().height;
        }
    }
}
