package com.smartmart.ui.kasir;

import com.smartmart.model.StrukData;
import com.smartmart.util.FormatRupiah;
import com.smartmart.util.PdfSederhana;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Halaman "Pratinjau Struk" yang tampil otomatis setelah pembayaran berstatus PAID.
 * Kiri: struk. Kanan: ringkasan transaksi, Cetak Struk, Simpan PDF, Transaksi Baru.
 */
public class StrukPanel extends JPanel {

    private static final Color LIME = new Color(0xE4EB5A);
    private static final Color UNGU = new Color(0x6D4AE0);

    private final StrukData struk;
    private final Runnable aksiTransaksiBaru;

    public StrukPanel(StrukData struk, Runnable aksiTransaksiBaru) {
        this.struk = struk;
        this.aksiTransaksiBaru = aksiTransaksiBaru;
        UiKit.pasangFont();
        setOpaque(true);
        setBackground(UiKit.BG);
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(buatHeader(), BorderLayout.NORTH);

        JPanel isi = UiKit.transparan();
        isi.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        isi.add(buatStruk(), c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 24, 0, 0);
        isi.add(buatKartuAksi(), c);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        isi.add(UiKit.transparan(), c);
        add(isi, BorderLayout.CENTER);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    // ----- header -----

    private JPanel buatHeader() {
        JPanel p = UiKit.transparan();
        p.setLayout(new BorderLayout());
        p.add(UiKit.header(UiKit.Ikon.DOC, "Pratinjau Struk", "Dibuat otomatis setelah pembayaran berstatus PAID"),
                BorderLayout.CENTER);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        kanan.setOpaque(false);
        String tgl = struk.waktu.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", new Locale("id", "ID")));
        kanan.add(UiKit.ikon(UiKit.Ikon.CALENDAR, 18, UiKit.TEXT));
        kanan.add(UiKit.lbl(tgl, Font.BOLD, 13, UiKit.TEXT));
        kanan.add(UiKit.lbl(struk.waktu.format(DateTimeFormatter.ofPattern("HH:mm")), Font.PLAIN, 13, UiKit.MUTED));
        JLabel online = UiKit.lbl("Online", Font.BOLD, 13, UiKit.TEXT);
        online.setIcon(new javax.swing.Icon() {
            public void paintIcon(java.awt.Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UiKit.GREEN);
                g2.fillOval(x, y, 10, 10);
                g2.dispose();
            }
            public int getIconWidth() { return 10; }
            public int getIconHeight() { return 10; }
        });
        online.setIconTextGap(8);
        kanan.add(online);
        p.add(kanan, BorderLayout.EAST);
        return p;
    }

    // ----- struk (kiri) -----

    private JComponent buatStruk() {
        Kertas k = new Kertas();
        k.add(tengah("SMARTMART", Font.BOLD, 20, UiKit.TEXT));
        k.add(tengah("Jl. Contoh No. 1, Pekanbaru", Font.PLAIN, 11, UiKit.MUTED));
        k.add(tengah("Telp. 0761-000000", Font.PLAIN, 11, UiKit.MUTED));
        k.add(new Garis());

        k.add(kv("No. transaksi", struk.nomor, UiKit.TEXT, Font.PLAIN, 11));
        k.add(kv("Tanggal", struk.tanggalTeks(), UiKit.TEXT, Font.PLAIN, 11));
        k.add(kv("Kasir", struk.kasir, UiKit.TEXT, Font.PLAIN, 11));
        if (struk.adaMember()) {
            k.add(kv("Member", struk.namaMember + " (" + struk.noMember + ")", UiKit.TEXT, Font.PLAIN, 11));
        }
        k.add(new Garis());

        for (StrukData.Baris b : struk.items) {
            k.add(kv(b.nama, FormatRupiah.angka(b.jumlahKotor), UiKit.TEXT, Font.BOLD, 12));
            k.add(kiri(b.rincian, Font.PLAIN, 10, UiKit.MUTED));
            if (b.ketDiskon != null) {
                k.add(kiri(b.ketDiskon, Font.PLAIN, 10, UiKit.ORANGE_TEXT));
            }
            k.add(jarak(8));
        }
        k.add(new Garis());

        k.add(kv("Subtotal", FormatRupiah.angka(struk.subtotal), UiKit.TEXT, Font.PLAIN, 11));
        k.add(kv("Total diskon", FormatRupiah.angka(-struk.totalDiskon), UiKit.RED_TEXT, Font.PLAIN, 11));
        if (struk.poinDitukar > 0) {
            k.add(kv("Tukar poin (" + struk.poinDitukar + ")", FormatRupiah.angka(-struk.diskonPoin), UNGU, Font.PLAIN, 11));
        }
        k.add(new Garis());

        JPanel total = UiKit.transparan();
        total.setLayout(new BorderLayout());
        total.add(UiKit.lbl("TOTAL", Font.BOLD, 16, UiKit.TEXT), BorderLayout.WEST);
        total.add(UiKit.lbl(FormatRupiah.format(struk.total), Font.BOLD, 18, UiKit.GREEN), BorderLayout.EAST);
        total.setAlignmentX(LEFT_ALIGNMENT);
        total.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        k.add(total);
        k.add(jarak(4));
        k.add(kv(struk.metode, FormatRupiah.angka(struk.dibayar), UiKit.TEXT, Font.PLAIN, 11));
        if ("Tunai".equals(struk.metode)) {
            k.add(kv("Kembalian", FormatRupiah.angka(struk.kembalian), UiKit.TEXT, Font.PLAIN, 11));
        }
        k.add(new Garis());

        if (struk.adaMember()) {
            k.add(tengah("Poin diperoleh +" + struk.poinDiperoleh + "  |  Sisa poin " + struk.sisaPoin,
                    Font.PLAIN, 10, UiKit.GREEN));
            k.add(jarak(6));
        }
        k.add(tengah("Terima kasih sudah berbelanja!", Font.BOLD, 11, UiKit.TEXT));
        return k;
    }

    private static JLabel kiri(String teks, int style, int size, Color warna) {
        JLabel l = UiKit.lbl(teks, style, size, warna);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 2));
        return l;
    }

    private static JLabel tengah(String teks, int style, int size, Color warna) {
        JLabel l = UiKit.lbl(teks, style, size, warna);
        l.setHorizontalAlignment(JLabel.CENTER);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 4));
        return l;
    }

    private static JComponent kv(String kunci, String nilai, Color warnaNilai, int styleNilai, int size) {
        JPanel p = UiKit.transparan();
        p.setLayout(new BorderLayout(12, 0));
        p.add(UiKit.lbl(kunci, styleNilai == Font.BOLD ? Font.BOLD : Font.PLAIN, size,
                styleNilai == Font.BOLD ? UiKit.TEXT : UiKit.MUTED), BorderLayout.WEST);
        p.add(UiKit.lbl(nilai, styleNilai, size, warnaNilai), BorderLayout.EAST);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 6));
        p.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private static JComponent jarak(int px) {
        JPanel p = UiKit.transparan();
        p.setPreferredSize(new Dimension(1, px));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, px));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    /** Kertas struk dengan tepi bawah bergerigi. */
    private static final class Kertas extends JPanel {
        Kertas() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EmptyBorder(24, 28, 34, 28));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(350, d.height);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 4;
            int h = getHeight() - 4;
            int gigi = 9;
            int n = Math.max(1, Math.round(w / 12f));
            double langkah = w / (double) n;
            Path2D.Double p = new Path2D.Double();
            p.moveTo(0, 0);
            p.lineTo(w, 0);
            p.lineTo(w, h - gigi);
            for (int i = 0; i < n; i++) {
                double x = w - i * langkah;
                p.lineTo(x - langkah / 2, h);
                p.lineTo(x - langkah, h - gigi);
            }
            p.closePath();
            g2.translate(0, 3);
            g2.setColor(new Color(0, 70, 40, 22));
            g2.fill(p);
            g2.translate(0, -3);
            g2.setColor(Color.WHITE);
            g2.fill(p);
            g2.dispose();
        }
    }

    private static final class Garis extends JComponent {
        Garis() {
            setPreferredSize(new Dimension(1, 18));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
            setAlignmentX(LEFT_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0xC9D3CD));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            g2.dispose();
        }
    }

    // ----- kartu aksi (kanan) -----

    private JComponent buatKartuAksi() {
        UiKit.Card kartu = new UiKit.Card(30);
        kartu.setBorder(new EmptyBorder(24, 26, 28, 26));
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));

        JPanel judul = UiKit.transparan();
        judul.setLayout(new BorderLayout());
        judul.add(UiKit.lbl("Transaksi selesai", Font.BOLD, 24, UiKit.TEXT), BorderLayout.WEST);
        JPanel lencana = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        lencana.setOpaque(false);
        lencana.add(new UiKit.Badge("PAID", UiKit.GREEN_SOFT, UiKit.GREEN));
        judul.add(lencana, BorderLayout.EAST);
        judul.setAlignmentX(LEFT_ALIGNMENT);
        judul.setMaximumSize(new Dimension(Integer.MAX_VALUE, judul.getPreferredSize().height));
        kartu.add(judul);

        JLabel sub = UiKit.lbl("Total " + FormatRupiah.format(struk.total) + " dibayar "
                + struk.metode.toLowerCase(Locale.ROOT), Font.PLAIN, 13, UiKit.MUTED);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(sub);
        kartu.add(jarak(18));

        UiKit.RoundButton cetak = UiKit.tombolHijau("Cetak Struk", UiKit.Ikon.DOC);
        UiKit.RoundButton pdf = new UiKit.RoundButton("Simpan PDF", null, UiKit.GRAY_SOFT, UiKit.TEXT, null);
        cetak.addActionListener(e -> cetak());
        pdf.addActionListener(e -> simpanPdf());
        JPanel dua = UiKit.transparan();
        dua.setLayout(new java.awt.GridLayout(1, 2, 14, 0));
        cetak.setPreferredSize(new Dimension(150, 56));
        pdf.setPreferredSize(new Dimension(150, 56));
        dua.add(cetak);
        dua.add(pdf);
        dua.setAlignmentX(LEFT_ALIGNMENT);
        dua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        kartu.add(dua);
        kartu.add(jarak(14));

        UiKit.RoundButton baru = new UiKit.RoundButton("+   Transaksi Baru", null, LIME, UiKit.GREEN_DARK, null);
        baru.setPreferredSize(new Dimension(150, 58));
        baru.addActionListener(e -> aksiTransaksiBaru.run());
        baru.setAlignmentX(LEFT_ALIGNMENT);
        baru.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        kartu.add(baru);
        kartu.add(jarak(26));

        JLabel tersimpan = UiKit.lbl("Yang tersimpan otomatis", Font.BOLD, 16, UiKit.TEXT);
        tersimpan.setAlignmentX(LEFT_ALIGNMENT);
        kartu.add(tersimpan);
        kartu.add(jarak(12));

        kartu.add(baris("Transaksi tersimpan, status SELESAI"));
        kartu.add(baris("Stok lot berkurang (lot terdekat kedaluwarsa)"));
        kartu.add(baris(struk.adaMember()
                ? "Poin member +" + struk.poinDiperoleh + " ditambahkan"
                : "Pembelian tanpa member, tidak ada poin"));
        kartu.add(baris("Perubahan stok tercatat di LossGuard"));
        kartu.add(baris("Struk dapat dibuka lagi dari Riwayat"));
        kartu.add(jarak(10));

        UiKit.Box catatan = UiKit.catatanKuning("Struk bersifat simulasi",
                "Pembayaran QRIS dan debit juga memakai tampilan struk yang sama.", false, 380);
        catatan.setAlignmentX(LEFT_ALIGNMENT);
        catatan.setMaximumSize(new Dimension(Integer.MAX_VALUE, catatan.getPreferredSize().height));
        kartu.add(catatan);
        return kartu;
    }

    private JComponent baris(String teks) {
        UiKit.Box b = new UiKit.Box(UiKit.FIELD_BG, 16);
        b.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(10, 14, 10, 12);
        b.add(UiKit.ikonBulat(UiKit.Ikon.CHECK, 26, UiKit.GREEN, Color.WHITE), c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        b.add(UiKit.lbl(teks, Font.PLAIN, 13, UiKit.TEXT), c);
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, b.getPreferredSize().height));

        JPanel bungkus = UiKit.transparan();
        bungkus.setLayout(new BorderLayout());
        bungkus.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        bungkus.add(b, BorderLayout.CENTER);
        bungkus.setAlignmentX(LEFT_ALIGNMENT);
        bungkus.setMaximumSize(new Dimension(Integer.MAX_VALUE, bungkus.getPreferredSize().height));
        return bungkus;
    }

    // ----- cetak dan PDF -----

    private void cetak() {
        List<String> baris = struk.teks(42);
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName(struk.nomor);
        job.setPrintable((Graphics g, PageFormat pf, int halaman) -> {
            if (halaman > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            g2.setColor(Color.BLACK);
            g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
            FontMetrics fm = g2.getFontMetrics();
            int y = fm.getAscent();
            for (String b : baris) {
                g2.drawString(b, 0, y);
                y += fm.getHeight();
            }
            return Printable.PAGE_EXISTS;
        });
        try {
            if (job.printDialog()) {
                job.print();
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak struk: " + ex.getMessage(),
                    "Cetak Struk", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simpanPdf() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan struk sebagai PDF");
        fc.setSelectedFile(new File(struk.nomor + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            f = new File(f.getParentFile(), f.getName() + ".pdf");
        }
        try {
            PdfSederhana.tulis(f, struk.teks(42));
            JOptionPane.showMessageDialog(this, "Struk disimpan di:\n" + f.getAbsolutePath(),
                    "Simpan PDF", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan PDF: " + ex.getMessage(),
                    "Simpan PDF", JOptionPane.ERROR_MESSAGE);
        }
    }
}
