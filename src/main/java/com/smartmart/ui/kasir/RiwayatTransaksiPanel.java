package com.smartmart.ui.kasir;

import com.smartmart.dao.RiwayatDAO;
import com.smartmart.dao.ReturDAO;
import com.smartmart.model.Retur;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class RiwayatTransaksiPanel extends javax.swing.JPanel {

    public interface AksiListener {
        void ajukanRetur(String idTransaksi);
        void voidTransaksi(String idTransaksi);
    }

    public interface StrukListener {
        void lihatStruk(String idTransaksi);
    }

    private static final int[] LEBAR = {140, 50, 92, 88, 58, 92};

    private final RiwayatDAO dao = new RiwayatDAO();
    private AksiListener listener;
    private StrukListener strukListener;
    private boolean modeContoh;
    private List<RiwayatDAO.Baris> contohSemua = new ArrayList<RiwayatDAO.Baris>();
    private final Map<String, List<RiwayatDAO.Item>> contohItem = new HashMap<String, List<RiwayatDAO.Item>>();
    private List<RiwayatDAO.Baris> tampil = new ArrayList<RiwayatDAO.Baris>();
    private RiwayatDAO.Baris terpilih;
    private String filterStatus = "";
    private String periode = RiwayatDAO.PERIODE_HARI_INI;
    private UiKit.SearchField cari;

    public RiwayatTransaksiPanel() {
        UiKit.pasangFont();
        initComponents();
        susunTampilan();
        tampilkanDetail();
        setMinimumSize(new Dimension(1000, 875));
    }

    public void setAksiListener(AksiListener l) { listener = l; }

    public void setStrukListener(StrukListener l) { strukListener = l; }

    public void muat() {
        modeContoh = false;
        segarkan();
    }

    public void muatContoh() {
        modeContoh = true;
        isiContoh();
        segarkan();
    }

    private void susunTampilan() {
        setOpaque(true);
        setBackground(UiKit.BG);
        setBorder(new EmptyBorder(64, 16, 46, 18));
        for (JComponent c : new JComponent[]{pnlHeader, pnlJudulBlok, pnlIsi, pnlCari, pnlChips, pnlTabel, pnlDetailAtas,
            pnlRingkas, pnlAksi, pnlJudulDetail, pnlItem, pnlDaftar}) {
            c.setOpaque(false);
        }
        lblJudul.setIcon(UiKit.ikonIcon(UiKit.Ikon.DOC, 28, UiKit.TEXT));
        lblJudul.setIconTextGap(12);
        lblSub.setBorder(new EmptyBorder(0, 40, 0, 0));
        pnlHeader.setBorder(new EmptyBorder(0, 21, 0, 0));

        cari = (UiKit.SearchField) fldCari;
        cari.setPreferredSize(new Dimension(300, 46));
        cari.getField().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { segarkan(); }
            public void removeUpdate(DocumentEvent e) { segarkan(); }
            public void changedUpdate(DocumentEvent e) { segarkan(); }
        });

        chipHariIni.setSelected(true);
        pasangChip(chipHariIni, chipMingguIni, true);
        pasangChip(chipMingguIni, chipHariIni, true);
        pasangChip(chipSelesai, chipDibatalkan, false);
        pasangChip(chipDibatalkan, chipSelesai, false);
        pnlChips.setBorder(new EmptyBorder(0, -18, 0, 0));

        pnlKepala.setLayout(new GridBagLayout());
        String[] judul = {"No. transaksi", "Waktu", "Pelanggan", "Total", "Metode", "Status"};
        for (int i = 0; i < judul.length; i++) {
            JLabel l = UiKit.lbl(judul[i], java.awt.Font.PLAIN, 12, UiKit.MUTED);
            l.setHorizontalAlignment(i == 3 ? SwingConstants.RIGHT : i == 5 ? SwingConstants.CENTER : SwingConstants.LEFT);
            l.setPreferredSize(new Dimension(LEBAR[i], 20));
            l.setMinimumSize(new Dimension(LEBAR[i] / 2, 20));
            GridBagConstraints g = UiKit.gbc(i, 0, LEBAR[i], 0, GridBagConstraints.HORIZONTAL);
            g.insets = new Insets(10, i == 0 ? 14 : 4, 10, i == 5 ? 18 : 4);
            pnlKepala.add(l, g);
        }
        pnlTabel.setBorder(new EmptyBorder(0, 0, 0, 0));
        pnlDaftar.setLayout(new BoxLayout(pnlDaftar, BoxLayout.Y_AXIS));
        JPanel pembungkus = new JPanel(new java.awt.BorderLayout());
        pembungkus.setOpaque(false);
        scrollDaftar.setViewportView(pembungkus);
        pembungkus.add(pnlDaftar, java.awt.BorderLayout.NORTH);
        scrollDaftar.setBorder(BorderFactory.createEmptyBorder());
        scrollDaftar.setOpaque(false);
        scrollDaftar.getViewport().setOpaque(false);
        scrollDaftar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollDaftar.getVerticalScrollBar().setUnitIncrement(18);
        scrollDaftar.setPreferredSize(new Dimension(100, 120));
        scrollDaftar.setMinimumSize(new Dimension(100, 60));
        UiKit.tipisScroll(scrollDaftar);

        pnlItem.setLayout(new BoxLayout(pnlItem, BoxLayout.Y_AXIS));
        lblId.setBorder(new EmptyBorder(0, 0, 0, 0));
        pnlDetailAtas.add(javax.swing.Box.createVerticalStrut(8), 1);
        pnlDetailAtas.add(javax.swing.Box.createVerticalStrut(14), 4);
        pnlDetailAtas.add(javax.swing.Box.createVerticalStrut(14), 6);
        UiKit.susunVertikal(pnlDetailAtas);

        btnStruk.setPreferredSize(new Dimension(100, 52));
        btnRetur.setPreferredSize(new Dimension(100, 52));
        btnVoid.setPreferredSize(new Dimension(100, 52));
        pnlAksi.add(javax.swing.Box.createVerticalStrut(12), 1);
        pnlAksi.add(javax.swing.Box.createVerticalStrut(12), 3);
        pnlAksi.add(javax.swing.Box.createVerticalStrut(14), 5);
        btnStruk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (terpilih != null && strukListener != null) strukListener.lihatStruk(terpilih.id);
            }
        });
        UiKit.susunVertikal(pnlAksi);
        btnRetur.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (terpilih != null && listener != null) listener.ajukanRetur(terpilih.id);
            }
        });
        btnVoid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (terpilih != null && listener != null) listener.voidTransaksi(terpilih.id);
            }
        });
        tampilkanDetail();
    }

    private void pasangChip(final javax.swing.JToggleButton ini, final javax.swing.JToggleButton lawan,
                            final boolean kelompokWaktu) {
        ini.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (ini.isSelected()) lawan.setSelected(false);
                if (kelompokWaktu) {
                    periode = !ini.isSelected() ? RiwayatDAO.PERIODE_SEMUA
                            : ini == chipHariIni ? RiwayatDAO.PERIODE_HARI_INI : RiwayatDAO.PERIODE_MINGGU_INI;
                } else {
                    filterStatus = !ini.isSelected() ? "" : ini == chipSelesai ? "SELESAI" : "DIBATALKAN";
                }
                segarkan();
            }
        });
    }

    private void segarkan() {
        String kata = cari == null ? "" : cari.getField().getText().trim();
        if (modeContoh) {
            tampil = new ArrayList<RiwayatDAO.Baris>();
            for (RiwayatDAO.Baris b : contohSemua) {
                if (filterStatus.length() > 0 && !filterStatus.equals(b.status)) continue;
                if (kata.length() > 0 && !cocokContoh(b, kata.toLowerCase())) continue;
                tampil.add(b);
            }
        } else {
            try {
                Connection c = ReturDAO.buka();
                try {
                    tampil = dao.cari(c, kata, filterStatus, periode);
                } finally {
                    c.close();
                }
                lblSub.setText("Cari transaksi lama, ajukan retur atau void");
                lblSub.setForeground(UiKit.MUTED);
            } catch (SQLException e) {
                tampil = new ArrayList<RiwayatDAO.Baris>();
                lblSub.setText("Gagal membaca database: " + e.getMessage());
                lblSub.setForeground(UiKit.RED_TEXT);
            }
        }
        if (terpilih == null || !adaDiDaftar(terpilih.id)) {
            terpilih = tampil.isEmpty() ? null : tampil.get(0);
        } else {
            for (RiwayatDAO.Baris b : tampil) if (b.id.equals(terpilih.id)) terpilih = b;
        }
        bangunDaftar();
        tampilkanDetail();
    }

    private boolean adaDiDaftar(String id) {
        for (RiwayatDAO.Baris b : tampil) if (b.id.equals(id)) return true;
        return false;
    }

    private boolean cocokContoh(RiwayatDAO.Baris b, String q) {
        StringBuilder sb = new StringBuilder();
        sb.append(b.id).append(' ').append(b.waktu).append(' ').append(b.pelanggan).append(' ')
                .append(b.metode).append(' ').append(b.status).append(' ');
        List<RiwayatDAO.Item> items = contohItem.get(b.id);
        if (items != null) for (RiwayatDAO.Item i : items) sb.append(i.nama).append(' ');
        return sb.toString().toLowerCase().contains(q);
    }

    private void bangunDaftar() {
        pnlDaftar.removeAll();
        if (tampil.isEmpty()) {
            JLabel kosong = UiKit.lbl("Tidak ada transaksi yang cocok", java.awt.Font.PLAIN, 13, UiKit.MUTED);
            kosong.setBorder(new EmptyBorder(24, 8, 0, 0));
            pnlDaftar.add(kosong);
        }
        for (RiwayatDAO.Baris b : tampil) {
            pnlDaftar.add(buatBaris(b));
            pnlDaftar.add(javax.swing.Box.createVerticalStrut(8));
        }
        pnlDaftar.revalidate();
        pnlDaftar.repaint();
    }

    private JPanel buatBaris(final RiwayatDAO.Baris t) {
        boolean pilih = terpilih != null && t.id.equals(terpilih.id);
        UiKit.Box b = new UiKit.Box(pilih ? UiKit.SELECTED : UiKit.ROW_BG, 16);
        if (pilih) b.setBorderColor(UiKit.GREEN, 2f);
        b.setLayout(new GridBagLayout());
        b.setPreferredSize(new Dimension(100, 48));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        JLabel id = UiKit.lbl(t.id, java.awt.Font.BOLD, 12, UiKit.TEXT);
        JLabel waktu = UiKit.lbl(t.waktu == null ? "-" : String.format("%02d:%02d", t.waktu.getHour(), t.waktu.getMinute()),
                java.awt.Font.PLAIN, 12, UiKit.TEXT);
        JLabel plg = UiKit.lbl(t.pelanggan, java.awt.Font.PLAIN, 12, UiKit.TEXT);
        JLabel tot = UiKit.lbl(UiKit.rp(t.total), java.awt.Font.BOLD, 12, UiKit.TEXT);
        tot.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel met = UiKit.lbl(t.metode, java.awt.Font.PLAIN, 12, UiKit.TEXT);
        UiKit.Badge st = UiKit.badgeStatus(t.status);
        st.setFont(UiKit.font(java.awt.Font.BOLD, 10));
        JPanel stw = UiKit.transparan();
        stw.setLayout(new GridBagLayout());
        stw.add(st);

        JComponent[] sel = {id, waktu, plg, tot, met, stw};
        for (int i = 0; i < sel.length; i++) {
            sel[i].setPreferredSize(new Dimension(LEBAR[i], 22));
            sel[i].setMinimumSize(new Dimension(LEBAR[i] / 2, 22));
            GridBagConstraints g = UiKit.gbc(i, 0, LEBAR[i], 1, GridBagConstraints.BOTH);
            g.insets = new Insets(0, i == 0 ? 14 : 4, 0, i == 5 ? 8 : 4);
            b.add(sel[i], g);
        }
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                terpilih = t;
                bangunDaftar();
                tampilkanDetail();
            }
        });
        return b;
    }

    private List<RiwayatDAO.Item> ambilItem(String id) {
        if (modeContoh) {
            List<RiwayatDAO.Item> l = contohItem.get(id);
            return l == null ? new ArrayList<RiwayatDAO.Item>() : l;
        }
        try {
            Connection c = ReturDAO.buka();
            try {
                return dao.ambilItem(c, id);
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            return new ArrayList<RiwayatDAO.Item>();
        }
    }

    private void tampilkanDetail() {
        pnlItem.removeAll();
        if (terpilih == null) {
            lblId.setText(" ");
            lblInfo.setText(" ");
            lblTotal.setText(" ");
            lblMetode.setText(" ");
            lblPoin.setText(" ");
            btnStruk.setEnabled(false);
            btnRetur.setEnabled(false);
            btnVoid.setEnabled(false);
            UiKit.susunVertikal(pnlDetailAtas);
            return;
        }
        RiwayatDAO.Baris t = terpilih;
        UiKit.terapkanStatus((UiKit.Badge) badgeStatus, t.status);
        lblId.setText(t.id);
        String tgl = t.waktu == null ? "-" : Retur.tanggal(t.waktu.toLocalDate()) + String.format(" %02d:%02d", t.waktu.getHour(), t.waktu.getMinute());
        lblInfo.setText(tgl + " | " + t.kasir + " | " + t.pelanggan);
        lblTotal.setText(UiKit.rp(t.total));
        lblMetode.setText(t.metode);
        lblPoin.setText(t.poinDiperoleh > 0 ? "+" + t.poinDiperoleh : "-");

        List<RiwayatDAO.Item> items = ambilItem(t.id);
        if (items.isEmpty()) {
            JLabel l = UiKit.lbl("Rincian barang belum tersedia.", java.awt.Font.PLAIN, 12, UiKit.MUTED);
            l.setBorder(new EmptyBorder(8, 2, 8, 2));
            pnlItem.add(l);
        }
        for (RiwayatDAO.Item it : items) {
            pnlItem.add(buatItem(it));
            pnlItem.add(javax.swing.Box.createVerticalStrut(8));
        }
        UiKit.susunVertikal(pnlItem);
        boolean selesai = "SELESAI".equals(t.status);
        btnStruk.setEnabled(true);
        btnRetur.setEnabled(selesai);
        btnVoid.setEnabled(selesai);
        UiKit.susunVertikal(pnlDetailAtas);
    }

    private JPanel buatItem(RiwayatDAO.Item it) {
        UiKit.Box b = new UiKit.Box(UiKit.ROW_BG, 16);
        b.setLayout(new GridBagLayout());
        b.setPreferredSize(new Dimension(100, 68));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridheight = 2;
        c.insets = new Insets(0, 10, 0, 12);
        b.add(UiKit.ikonProduk(UiKit.produkDariNama(it.nama), 44), c);

        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 0; c.weightx = 1; c.anchor = GridBagConstraints.SOUTHWEST;
        c.insets = new Insets(0, 0, 0, 8);
        b.add(UiKit.lbl(it.nama, java.awt.Font.BOLD, 14, UiKit.TEXT), c);
        boolean kg = "KG".equalsIgnoreCase(it.satuan);
        String sub = kg ? com.smartmart.model.DetailRetur.angka(it.jumlah) + " kg"
                : com.smartmart.model.DetailRetur.angka(it.jumlah) + " x " + UiKit.rp(it.hargaAwal);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1; c.weightx = 1; c.anchor = GridBagConstraints.NORTHWEST;
        b.add(UiKit.lbl(sub, java.awt.Font.PLAIN, 11, UiKit.MUTED), c);

        c = new GridBagConstraints();
        c.gridx = 2; c.gridy = 0; c.anchor = GridBagConstraints.SOUTHEAST;
        c.insets = new Insets(0, 0, 0, 12);
        b.add(UiKit.lbl(UiKit.rp(it.subtotal), java.awt.Font.BOLD, 14, UiKit.TEXT), c);

        String sumber = it.sumberDiskon == null ? "" : it.sumberDiskon.toUpperCase();
        if (sumber.equals("MEMBER") || sumber.equals("FRESHSAVE")) {
            boolean fs = sumber.equals("FRESHSAVE");
            UiKit.Badge tag = new UiKit.Badge(sumber, fs ? UiKit.ORANGE_BG : UiKit.GREEN_SOFT, fs ? UiKit.ORANGE_TEXT : UiKit.GREEN);
            tag.setFont(UiKit.font(java.awt.Font.BOLD, 9));
            tag.setBorder(new EmptyBorder(2, 10, 2, 10));
            c = new GridBagConstraints();
            c.gridx = 2; c.gridy = 1; c.anchor = GridBagConstraints.NORTHEAST;
            c.insets = new Insets(2, 0, 0, 12);
            b.add(tag, c);
        }
        return b;
    }

    private RiwayatDAO.Baris baris(String id, int jam, int menit, String plg, int total, String metode, String status) {
        RiwayatDAO.Baris b = new RiwayatDAO.Baris();
        b.id = id;
        b.waktu = LocalDate.now().atTime(jam, menit);
        b.pelanggan = plg;
        b.total = total;
        b.metode = metode;
        b.status = status;
        b.adaMember = !"Umum".equals(plg);
        b.poinDiperoleh = b.adaMember ? total / 10000 : 0;
        b.kasir = "Kasir01";
        return b;
    }

    private RiwayatDAO.Item item(String nama, String satuan, double jml, int hargaAwal, int subtotal, String sumber) {
        RiwayatDAO.Item i = new RiwayatDAO.Item();
        i.nama = nama; i.satuan = satuan; i.jumlah = jml; i.hargaAwal = hargaAwal; i.subtotal = subtotal; i.sumberDiskon = sumber;
        return i;
    }

    private void isiContoh() {
        contohSemua = new ArrayList<RiwayatDAO.Baris>();
        contohSemua.add(baris("TRX-20261104-0017", 22, 41, "Siti Rahma", 55150, "Tunai", "SELESAI"));
        contohSemua.add(baris("TRX-20261104-0016", 22, 10, "Umum", 128400, "QRIS", "SELESAI"));
        contohSemua.add(baris("TRX-20261104-0015", 21, 32, "Budi S.", 42300, "Debit", "SELESAI"));
        contohSemua.add(baris("TRX-20261104-0014", 20, 58, "Umum", 76000, "Tunai", "DIBATALKAN"));
        contohSemua.add(baris("TRX-20261104-0013", 20, 15, "Rina A.", 215900, "QRIS", "SELESAI"));
        contohSemua.add(baris("TRX-20261104-0012", 19, 47, "Umum", 23500, "Tunai", "SELESAI"));
        contohSemua.add(baris("TRX-20261104-0011", 19, 20, "Siti Rahma", 91200, "Debit", "SELESAI"));
        contohSemua.add(baris("TRX-20261104-0010", 18, 55, "Umum", 34800, "Tunai", "SELESAI"));
        List<RiwayatDAO.Item> l = new ArrayList<RiwayatDAO.Item>();
        l.add(item("Susu UHT 1L", "PCS", 1, 17500, 15750, "MEMBER"));
        l.add(item("Apel Fuji", "KG", 0.75, 48000, 32400, "MEMBER"));
        l.add(item("Roti Tawar", "PCS", 1, 15000, 12000, "FRESHSAVE"));
        contohItem.put("TRX-20261104-0017", l);
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
        pnlKiri = new com.smartmart.ui.kasir.UiKit.Card();
        pnlCari = new javax.swing.JPanel();
        fldCari = new com.smartmart.ui.kasir.UiKit.SearchField("Cari no. transaksi, tanggal, atau nama produk...", com.smartmart.ui.kasir.UiKit.Ikon.SEARCH, com.smartmart.ui.kasir.UiKit.GREEN);
        pnlChips = new javax.swing.JPanel();
        chipHariIni = new com.smartmart.ui.kasir.UiKit.Chip("Hari ini", com.smartmart.ui.kasir.UiKit.GREEN);
        chipMingguIni = new com.smartmart.ui.kasir.UiKit.Chip("Minggu ini", com.smartmart.ui.kasir.UiKit.GREEN);
        chipSelesai = new com.smartmart.ui.kasir.UiKit.Chip("Selesai", com.smartmart.ui.kasir.UiKit.GREEN);
        chipDibatalkan = new com.smartmart.ui.kasir.UiKit.Chip("Dibatalkan", com.smartmart.ui.kasir.UiKit.GREEN);
        pnlTabel = new javax.swing.JPanel();
        pnlKepala = new com.smartmart.ui.kasir.UiKit.Box(com.smartmart.ui.kasir.UiKit.HEADER_BG, 14);
        scrollDaftar = new javax.swing.JScrollPane();
        pnlDaftar = new javax.swing.JPanel();
        pnlDetail = new com.smartmart.ui.kasir.UiKit.Card();
        pnlDetailAtas = new javax.swing.JPanel();
        pnlJudulDetail = new javax.swing.JPanel();
        lblDetail = new javax.swing.JLabel();
        badgeStatus = new com.smartmart.ui.kasir.UiKit.Badge("SELESAI", com.smartmart.ui.kasir.UiKit.GREEN_SOFT, com.smartmart.ui.kasir.UiKit.GREEN);
        lblId = new javax.swing.JLabel();
        lblInfo = new javax.swing.JLabel();
        pnlItem = new javax.swing.JPanel();
        pnlRingkas = new javax.swing.JPanel();
        lblTotalT = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        lblMetodeT = new javax.swing.JLabel();
        lblMetode = new javax.swing.JLabel();
        lblPoinT = new javax.swing.JLabel();
        lblPoin = new javax.swing.JLabel();
        pnlAksi = new javax.swing.JPanel();
        btnStruk = new com.smartmart.ui.kasir.UiKit.RoundButton("Lihat Struk", com.smartmart.ui.kasir.UiKit.Ikon.DOC, com.smartmart.ui.kasir.UiKit.GRAY_SOFT, com.smartmart.ui.kasir.UiKit.TEXT, null);
        btnRetur = com.smartmart.ui.kasir.UiKit.tombolHijau("Ajukan Retur", com.smartmart.ui.kasir.UiKit.Ikon.UNDO);
        btnVoid = com.smartmart.ui.kasir.UiKit.tombolOutlineMerah("Void Transaksi", com.smartmart.ui.kasir.UiKit.Ikon.TRASH);
        pnlNote = com.smartmart.ui.kasir.UiKit.catatanKuning("VoidGuard", "Setelah dibayar: wajib alasan dan password admin, hanya hari yang sama. Transaksi yang sudah diretur tidak dapat di-void.", false, 232);

        setLayout(new java.awt.BorderLayout(0, 14));
        pnlHeader.setLayout(new java.awt.BorderLayout());
        pnlJudulBlok.setLayout(new java.awt.GridLayout(2, 1));
        lblJudul.setText("Riwayat Transaksi");
        lblJudul.setFont(new java.awt.Font("Poppins", 1, 29)); // NOI18N
        lblJudul.setForeground(new java.awt.Color(19, 38, 29));
        pnlJudulBlok.add(lblJudul);

        lblSub.setText("Cari transaksi lama, ajukan retur atau void");
        lblSub.setFont(new java.awt.Font("Poppins", 0, 14)); // NOI18N
        lblSub.setForeground(new java.awt.Color(107, 122, 114));
        pnlJudulBlok.add(lblSub);

        pnlHeader.add(pnlJudulBlok, java.awt.BorderLayout.WEST);

        pnlHeader.add(barInfo, java.awt.BorderLayout.EAST);

        add(pnlHeader, java.awt.BorderLayout.NORTH);

        pnlIsi.setLayout(new java.awt.BorderLayout(20, 0));
        pnlKiri.setLayout(new java.awt.BorderLayout(0, 14));
        pnlCari.setLayout(new java.awt.BorderLayout(0, 14));
        pnlCari.add(fldCari, java.awt.BorderLayout.NORTH);

        pnlChips.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 18, 0));
        pnlChips.add(chipHariIni);

        pnlChips.add(chipMingguIni);

        pnlChips.add(chipSelesai);

        pnlChips.add(chipDibatalkan);

        pnlCari.add(pnlChips, java.awt.BorderLayout.SOUTH);

        pnlKiri.add(pnlCari, java.awt.BorderLayout.NORTH);

        pnlTabel.setLayout(new java.awt.BorderLayout(0, 10));
        pnlKepala.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        pnlTabel.add(pnlKepala, java.awt.BorderLayout.NORTH);

        pnlDaftar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollDaftar.setViewportView(pnlDaftar);

        pnlTabel.add(scrollDaftar, java.awt.BorderLayout.CENTER);

        pnlKiri.add(pnlTabel, java.awt.BorderLayout.CENTER);

        pnlIsi.add(pnlKiri, java.awt.BorderLayout.CENTER);

        pnlDetail.setPreferredSize(new java.awt.Dimension(352, 0));
        pnlDetail.setLayout(new java.awt.BorderLayout(0, 12));
        pnlDetailAtas.setLayout(new javax.swing.BoxLayout(pnlDetailAtas, javax.swing.BoxLayout.PAGE_AXIS));
        pnlJudulDetail.setLayout(new java.awt.BorderLayout());
        lblDetail.setText("Detail transaksi");
        lblDetail.setFont(new java.awt.Font("Poppins", 1, 18)); // NOI18N
        lblDetail.setForeground(new java.awt.Color(19, 38, 29));
        pnlJudulDetail.add(lblDetail, java.awt.BorderLayout.WEST);

        pnlJudulDetail.add(badgeStatus, java.awt.BorderLayout.EAST);

        pnlDetailAtas.add(pnlJudulDetail);

        lblId.setText("TRX-...");
        lblId.setFont(new java.awt.Font("Poppins", 1, 15)); // NOI18N
        lblId.setForeground(new java.awt.Color(19, 38, 29));
        pnlDetailAtas.add(lblId);

        lblInfo.setText("Tanggal | Kasir | Pelanggan");
        lblInfo.setFont(new java.awt.Font("Poppins", 0, 11)); // NOI18N
        lblInfo.setForeground(new java.awt.Color(107, 122, 114));
        pnlDetailAtas.add(lblInfo);

        pnlItem.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        pnlDetailAtas.add(pnlItem);

        pnlRingkas.setLayout(new java.awt.GridLayout(3, 2, 0, 9));
        lblTotalT.setText("Total");
        lblTotalT.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblTotalT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRingkas.add(lblTotalT);

        lblTotal.setText("Rp0");
        lblTotal.setFont(new java.awt.Font("Poppins", 1, 16)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(15, 145, 88));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRingkas.add(lblTotal);

        lblMetodeT.setText("Metode");
        lblMetodeT.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblMetodeT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRingkas.add(lblMetodeT);

        lblMetode.setText("-");
        lblMetode.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblMetode.setForeground(new java.awt.Color(19, 38, 29));
        lblMetode.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRingkas.add(lblMetode);

        lblPoinT.setText("Poin diperoleh");
        lblPoinT.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblPoinT.setForeground(new java.awt.Color(107, 122, 114));
        pnlRingkas.add(lblPoinT);

        lblPoin.setText("-");
        lblPoin.setFont(new java.awt.Font("Poppins", 0, 13)); // NOI18N
        lblPoin.setForeground(new java.awt.Color(19, 38, 29));
        lblPoin.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        pnlRingkas.add(lblPoin);

        pnlDetailAtas.add(pnlRingkas);

        pnlDetail.add(pnlDetailAtas, java.awt.BorderLayout.NORTH);

        pnlAksi.setLayout(new javax.swing.BoxLayout(pnlAksi, javax.swing.BoxLayout.PAGE_AXIS));
        pnlAksi.add(btnStruk);
        pnlAksi.add(btnRetur);

        pnlAksi.add(btnVoid);

        pnlAksi.add(pnlNote);

        pnlDetail.add(pnlAksi, java.awt.BorderLayout.SOUTH);

        pnlIsi.add(pnlDetail, java.awt.BorderLayout.EAST);

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
                javax.swing.JFrame f = new javax.swing.JFrame("SmartMart POS - Riwayat Transaksi");
                f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                RiwayatTransaksiPanel p = new RiwayatTransaksiPanel();
                p.setAksiListener(new AksiListener() {
                    public void ajukanRetur(String id) {
                        javax.swing.JOptionPane.showMessageDialog(null, "Buka SmartReturn untuk " + id);
                    }

                    public void voidTransaksi(String id) {
                        javax.swing.JOptionPane.showMessageDialog(null, "Buka VoidGuard untuk " + id);
                    }
                });
                p.muatContoh();
                f.setContentPane(UiKit.bungkusScroll(p));
                f.setSize(1120, 780);
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel badgeStatus;
    private javax.swing.JPanel barInfo;
    private javax.swing.JButton btnStruk;
    private javax.swing.JButton btnRetur;
    private javax.swing.JButton btnVoid;
    private javax.swing.JToggleButton chipDibatalkan;
    private javax.swing.JToggleButton chipHariIni;
    private javax.swing.JToggleButton chipMingguIni;
    private javax.swing.JToggleButton chipSelesai;
    private javax.swing.JPanel fldCari;
    private javax.swing.JLabel lblDetail;
    private javax.swing.JLabel lblId;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblMetode;
    private javax.swing.JLabel lblMetodeT;
    private javax.swing.JLabel lblPoin;
    private javax.swing.JLabel lblPoinT;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblTotalT;
    private javax.swing.JPanel pnlAksi;
    private javax.swing.JPanel pnlCari;
    private javax.swing.JPanel pnlChips;
    private javax.swing.JPanel pnlDaftar;
    private javax.swing.JPanel pnlDetail;
    private javax.swing.JPanel pnlDetailAtas;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlIsi;
    private javax.swing.JPanel pnlItem;
    private javax.swing.JPanel pnlJudulBlok;
    private javax.swing.JPanel pnlJudulDetail;
    private javax.swing.JPanel pnlKepala;
    private javax.swing.JPanel pnlKiri;
    private javax.swing.JPanel pnlNote;
    private javax.swing.JPanel pnlRingkas;
    private javax.swing.JPanel pnlTabel;
    private javax.swing.JScrollPane scrollDaftar;
    // End of variables declaration//GEN-END:variables
}
