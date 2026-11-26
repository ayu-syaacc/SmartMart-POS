package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.util.List;
import java.util.Map;

/** Dialog input berat produk (simulasi label timbangan). */
final class WeightDialog extends JDialog {
    private Double result;
    private final JTextField field = Ui.field(8);
    private final JLabel calc = Ui.label(" ", Font.BOLD, 14, Ui.PRIMARY);
    private final Product p;
    private final double max;

    private WeightDialog(Window owner, Product p, double max) {
        super(owner, "Input Berat Produk", ModalityType.APPLICATION_MODAL);
        this.p = p;
        this.max = max;
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(16, 18, 16, 18));
        root.add(left(Ui.label("Simulasi label timbangan (kg)", Font.PLAIN, 12, Ui.MUTED)));
        root.add(left(Ui.label(p.name, Font.BOLD, 18, Ui.TEXT)));
        root.add(left(Ui.label(Ui.rp(p.price) + " / kg", Font.PLAIN, 13, Ui.MUTED)));
        root.add(Box.createVerticalStrut(10));
        field.setFont(Ui.f(Font.BOLD, 26));
        field.setHorizontalAlignment(SwingConstants.RIGHT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(field);
        root.add(Box.createVerticalStrut(8));
        JPanel quick = Ui.transparent(new GridLayout(1, 4, 6, 0));
        for (String s : new String[]{"0,25", "0,5", "1", "2"}) {
            Ui.Btn b = Ui.compact(Ui.plain(s + " kg"));
            b.addActionListener(e -> field.setText(s));
            quick.add(b);
        }
        quick.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(quick);
        root.add(Box.createVerticalStrut(8));
        JPanel pad = Ui.numpad(field, true);
        pad.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(pad);
        root.add(Box.createVerticalStrut(10));
        root.add(left(calc));
        root.add(Box.createVerticalStrut(10));
        JPanel btns = Ui.transparent(new GridLayout(1, 2, 8, 0));
        Ui.Btn cancel = Ui.compact(Ui.plain("Batal"));
        Ui.Btn ok = Ui.compact(Ui.primary("Tambah ke Keranjang"));
        cancel.addActionListener(e -> dispose());
        ok.addActionListener(e -> accept());
        btns.add(cancel);
        btns.add(ok);
        btns.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(btns);
        field.getDocument().addDocumentListener(Ui.onChange(this::upd));
        field.addActionListener(e -> accept());
        setContentPane(root);
        setSize(390, 570);
        setLocationRelativeTo(owner);
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void upd() {
        double w = Ui.parseQty(field.getText());
        calc.setText(w > 0 ? Ui.qty(w) + " kg x " + Ui.rp(p.price) + " = " + Ui.rp(Math.round(w * p.price)) : " ");
    }

    private void accept() {
        double w = Ui.parseQty(field.getText());
        if (w <= 0) { Ui.err(this, "Masukkan berat lebih dari 0."); return; }
        if (w > max + 1e-9) { Ui.err(this, "Stok tersedia hanya " + Ui.qty(max) + " kg."); return; }
        result = Db.r3(w);
        dispose();
    }

    static Double ask(Component parent, Product p, double max) {
        WeightDialog d = new WeightDialog(SwingUtilities.getWindowAncestor(parent), p, max);
        d.setVisible(true);
        return d.result;
    }
}

/** Dialog memilih member. */
final class MemberPicker extends JDialog {
    private Member result;
    private final JTextField search = Ui.field(20);
    private final javax.swing.table.DefaultTableModel model = Ui.model("Nama", "No. member", "Poin");
    private final JTable table = Ui.table(model);
    private List<Member> shown;

    private MemberPicker(Window owner) {
        super(owner, "Pilih Member", ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(14, 16, 14, 16));
        search.putClientProperty("JTextField.placeholderText", "Cari no. member atau nama...");
        root.add(search, BorderLayout.NORTH);
        root.add(Ui.scroll(table), BorderLayout.CENTER);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        Ui.Btn cancel = Ui.plain("Batal");
        Ui.Btn ok = Ui.primary("Pilih");
        cancel.addActionListener(e -> dispose());
        ok.addActionListener(e -> choose());
        btns.add(cancel);
        btns.add(ok);
        root.add(btns, BorderLayout.SOUTH);
        search.getDocument().addDocumentListener(Ui.onChange(this::load));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) choose(); }
        });
        load();
        setContentPane(root);
        setSize(480, 420);
        setLocationRelativeTo(owner);
    }

    private void load() {
        shown = Db.get().searchMembers(search.getText());
        model.setRowCount(0);
        for (Member m : shown) model.addRow(new Object[]{m.name, m.no, m.points + " poin"});
    }

    private void choose() {
        int r = table.getSelectedRow();
        if (r < 0) { Ui.err(this, "Pilih satu member terlebih dahulu."); return; }
        result = shown.get(r);
        dispose();
    }

    static Member pick(Component parent) {
        MemberPicker d = new MemberPicker(SwingUtilities.getWindowAncestor(parent));
        d.setVisible(true);
        return d.result;
    }
}

/** Dialog pembayaran: ringkasan pesanan, tukar poin, metode, uang diterima, kembalian. */
final class PaymentDialog extends JDialog {
    Sale result;
    private final Db db = Db.get();
    private final Map<String, Double> cart;
    private final Member member;
    private final User cashier;
    private final Db.Quote q;
    private String method = "Tunai";
    private int pointsUsed;
    private long total;
    private final JTextField received = Ui.field(10);
    private final JLabel lblTotal = Ui.label("", Font.BOLD, 15, Ui.TEXT);
    private final JLabel lblPts = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblEarn = Ui.label(" ", Font.PLAIN, 12, Ui.OK);
    private final JLabel lblBig = Ui.label("", Font.BOLD, 30, Ui.PRIMARY);
    private final JLabel lblRecFmt = Ui.label(" ", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel lblChange = Ui.label("", Font.BOLD, 22, Ui.OK);
    private final JLabel lblStatus = Ui.label("PENDING  >  PAID", Font.BOLD, 12, Ui.WARN);
    private final Ui.Btn confirm = Ui.primary("Konfirmasi Pembayaran");
    private JSpinner spin;
    private JPanel quick;

    private PaymentDialog(Window owner, Map<String, Double> cart, Member m, User cashier) {
        super(owner, "Pembayaran", ModalityType.APPLICATION_MODAL);
        this.cart = cart;
        this.member = m;
        this.cashier = cashier;
        this.q = db.quote(cart, m);
        JPanel root = new JPanel(new GridLayout(1, 2, 14, 0));
        root.setBorder(new EmptyBorder(14, 16, 14, 16));
        root.add(summaryCard());
        root.add(payCard());
        setContentPane(root);
        setSize(900, 640);
        setLocationRelativeTo(owner);
        recalc();
    }

    private JPanel summaryCard() {
        JPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 10));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel t = new JPanel(new GridLayout(2, 1));
        t.setOpaque(false);
        t.add(Ui.label("Ringkasan Pesanan", Font.BOLD, 16, Ui.TEXT));
        t.add(Ui.muted(db.nextSaleId(java.time.LocalDate.now()) + "  |  Pelanggan: "
                + (member == null ? "Umum" : member.name + " (" + member.no + ")")));
        head.add(t, BorderLayout.CENTER);
        head.add(Ui.badge("BERJALAN", Ui.WARN), BorderLayout.EAST);
        c.add(head, BorderLayout.NORTH);

        JPanel lines = new JPanel();
        lines.setOpaque(false);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        for (Db.QLine l : q.lines) {
            JPanel row = Ui.transparent(new BorderLayout());
            row.setBorder(new EmptyBorder(5, 8, 5, 8));
            JPanel nm = Ui.transparent(new GridLayout(2, 1));
            nm.add(Ui.label(l.name, Font.BOLD, 13, Ui.TEXT));
            String unit = "KG".equals(l.unit) ? " kg" : "";
            nm.add(Ui.muted(Ui.qty(l.qty) + unit + " x " + Ui.rp(db.product(l.code).price) + (l.tag.isEmpty() ? "" : "  |  " + l.tag)));
            row.add(nm, BorderLayout.CENTER);
            row.add(Ui.label(Ui.rp(l.net), Font.BOLD, 13, Ui.TEXT), BorderLayout.EAST);
            lines.add(row);
        }
        lines.add(Box.createVerticalGlue());
        c.add(Ui.scroll(lines), BorderLayout.CENTER);

        JPanel sum = new JPanel(new GridLayout(0, 1, 0, 4));
        sum.setOpaque(false);
        sum.add(kv("Subtotal", Ui.rp(q.subtotal), Ui.TEXT));
        sum.add(kv("Diskon (member & FreshSave)", "-" + Ui.rp(q.discount), Ui.DANGER));
        sum.add(kv("Tukar poin", "", Ui.TEXT, lblPts));
        if (member != null) {
            int max = db.maxRedeemPoints(member, q.after);
            if (max > 0) {
                spin = new JSpinner(new SpinnerNumberModel(0, 0, max, db.d.s.redeemUnit));
                spin.addChangeListener(e -> recalc());
                JPanel sp = new JPanel(new BorderLayout(8, 0));
                sp.setOpaque(false);
                sp.add(Ui.muted("Poin ditukar (maks. " + max + " poin, kelipatan " + db.d.s.redeemUnit + ")"), BorderLayout.CENTER);
                sp.add(spin, BorderLayout.EAST);
                sum.add(sp);
            } else sum.add(Ui.muted("Saldo " + member.points + " poin belum memenuhi syarat penukaran."));
        }
        JPanel tot = new JPanel(new BorderLayout());
        tot.setOpaque(false);
        tot.add(Ui.label("Total", Font.BOLD, 15, Ui.TEXT), BorderLayout.WEST);
        tot.add(lblTotal, BorderLayout.EAST);
        sum.add(tot);
        sum.add(lblEarn);
        c.add(sum, BorderLayout.SOUTH);
        return c;
    }

    private JPanel kv(String k, String v, Color c) {
        return kv(k, v, c, null);
    }

    private JPanel kv(String k, String v, Color c, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Ui.label(k, Font.PLAIN, 13, Ui.MUTED), BorderLayout.WEST);
        if (valueLabel != null) p.add(valueLabel, BorderLayout.EAST);
        else p.add(Ui.label(v, Font.PLAIN, 13, c), BorderLayout.EAST);
        return p;
    }

    private JPanel payCard() {
        JPanel c = Ui.card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JPanel methods = Ui.transparent(new GridLayout(1, 3, 6, 0));
        ButtonGroup bg = new ButtonGroup();
        for (String m : new String[]{"Tunai", "QRIS", "Debit"}) {
            JToggleButton b = Ui.chip(m);
            b.setSelected(m.equals(method));
            b.addActionListener(e -> setMethod(m));
            bg.add(b);
            methods.add(b);
        }
        c.add(left(methods));
        c.add(Box.createVerticalStrut(10));
        c.add(left(Ui.muted("Total tagihan")));
        c.add(left(lblBig));
        c.add(Box.createVerticalStrut(6));
        c.add(left(Ui.muted("Uang diterima")));
        received.setFont(Ui.f(Font.BOLD, 20));
        received.setHorizontalAlignment(SwingConstants.RIGHT);
        received.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        received.getDocument().addDocumentListener(Ui.onChange(this::recalc));
        c.add(left(received));
        c.add(left(lblRecFmt));
        c.add(Box.createVerticalStrut(6));
        quick = Ui.transparent(new GridLayout(1, 4, 6, 0));
        Ui.Btn exact = Ui.compact(Ui.plain("Uang pas"));
        exact.addActionListener(e -> received.setText(String.valueOf(total)));
        quick.add(exact);
        for (long v : new long[]{50000, 100000, 200000}) {
            Ui.Btn b = Ui.compact(Ui.plain(Ui.num(v)));
            b.addActionListener(e -> received.setText(String.valueOf(v)));
            quick.add(b);
        }
        c.add(left(quick));
        c.add(Box.createVerticalStrut(8));
        c.add(left(Ui.muted("Kembalian")));
        c.add(left(lblChange));
        c.add(Box.createVerticalStrut(8));
        c.add(left(Ui.numpad(received, false)));
        c.add(Box.createVerticalStrut(8));
        c.add(left(lblStatus));
        c.add(Box.createVerticalStrut(6));
        JPanel btns = Ui.transparent(new GridLayout(1, 2, 8, 0));
        Ui.Btn cancel = Ui.plain("Batalkan");
        cancel.addActionListener(e -> dispose());
        confirm.addActionListener(e -> pay());
        btns.add(cancel);
        btns.add(confirm);
        c.add(left(btns));
        c.add(Box.createVerticalStrut(6));
        c.add(left(Ui.muted("Simulasi pembayaran. Tidak ada integrasi pembayaran sungguhan.")));
        return c;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void setMethod(String m) {
        method = m;
        boolean cash = "Tunai".equals(m);
        received.setEnabled(cash);
        for (Component k : quick.getComponents()) k.setEnabled(cash);
        received.setText(cash ? "" : String.valueOf(total));
        recalc();
    }

    private void recalc() {
        pointsUsed = spin == null ? 0 : (Integer) spin.getValue();
        long pd = (long) (pointsUsed / db.d.s.redeemUnit) * db.d.s.redeemValue;
        total = q.after - pd;
        lblPts.setText(pointsUsed > 0 ? "(" + pointsUsed + " poin)  -" + Ui.rp(pd) : "-");
        lblTotal.setText(Ui.rp(total));
        lblBig.setText(Ui.rp(total));
        int earn = member != null ? (int) (total / db.d.s.pointPerRp) : 0;
        lblEarn.setText(member != null ? "Poin diperoleh setelah PAID: +" + earn + " poin" : " ");
        boolean cash = "Tunai".equals(method);
        long rec = cash ? Ui.parseMoney(received.getText()) : total;
        lblRecFmt.setText(cash ? Ui.rp(rec) : "Dibayar via " + method + " (simulasi)");
        lblChange.setText(Ui.rp(Math.max(rec - total, 0)));
        confirm.setEnabled(!cash || rec >= total);
    }

    private void pay() {
        boolean cash = "Tunai".equals(method);
        long rec = cash ? Ui.parseMoney(received.getText()) : total;
        try {
            result = db.checkout(cart, member, pointsUsed, method, rec, cashier);
            lblStatus.setText("PAID");
            lblStatus.setForeground(Ui.OK);
            dispose();
        } catch (IllegalArgumentException ex) {
            Ui.err(this, ex.getMessage());
        }
    }

    /** Mengembalikan transaksi yang sudah PAID, atau null bila dibatalkan. */
    static Sale ask(Component parent, Map<String, Double> cart, Member m, User cashier) {
        PaymentDialog d = new PaymentDialog(SwingUtilities.getWindowAncestor(parent), cart, m, cashier);
        d.setVisible(true);
        return d.result;
    }
}

/** Pembuat teks struk (lebar 34 karakter, ASCII agar aman dicetak / jadi PDF). */
final class Receipt {
    private static final int W = 34;

    private Receipt() { }

    private static void center(StringBuilder b, String s) {
        int pad = Math.max(0, (W - s.length()) / 2);
        b.append(" ".repeat(pad)).append(s).append('\n');
    }

    private static void row(StringBuilder b, String l, String r) {
        int space = W - l.length() - r.length();
        if (space < 1) {
            l = l.substring(0, Math.max(0, W - r.length() - 1));
            space = Math.max(1, W - l.length() - r.length());
        }
        b.append(l).append(" ".repeat(space)).append(r).append('\n');
    }

    private static void rule(StringBuilder b) { b.append("-".repeat(W)).append('\n'); }

    static String text(Sale s) {
        Db db = Db.get();
        Settings st = db.d.s;
        StringBuilder b = new StringBuilder();
        center(b, st.storeName);
        center(b, st.address);
        center(b, st.phone);
        rule(b);
        row(b, "No. transaksi", s.id);
        row(b, "Tanggal", s.time.format(Db.DTF));
        row(b, "Kasir", s.cashier);
        row(b, "Member", s.memberNo == null ? "Umum" : s.memberName + " (" + s.memberNo + ")");
        rule(b);
        for (SaleItem it : s.items) {
            row(b, it.name, Ui.num(it.gross));
            String q = "KG".equals(it.unit) ? Ui.qty(it.qty) + " kg" : Ui.qty(it.qty);
            b.append(' ').append(q).append(" x ").append(Ui.num(it.unitPrice)).append('\n');
            if (it.discAmt > 0)
                row(b, " " + ("FRESHSAVE".equals(it.source) ? "FreshSave " + it.discPct + "%" : "Diskon member"),
                        "-" + Ui.num(it.discAmt));
        }
        rule(b);
        row(b, "Subtotal", Ui.num(s.subtotal));
        if (s.discount > 0) row(b, "Total diskon", "-" + Ui.num(s.discount));
        if (s.pointsUsed > 0) row(b, "Tukar poin (" + s.pointsUsed + ")", "-" + Ui.num(s.pointsDiscount));
        row(b, "TOTAL", Ui.rp(s.total));
        row(b, s.method, Ui.num(s.paid));
        if ("Tunai".equals(s.method)) row(b, "Kembalian", Ui.num(s.change));
        if (s.memberNo != null) {
            Member m = db.member(s.memberNo);
            rule(b);
            row(b, "Poin diperoleh +" + s.pointsEarned, "Sisa poin " + (m == null ? 0 : m.points));
        }
        rule(b);
        if ("DIBATALKAN".equals(s.status)) center(b, "*** TRANSAKSI DIBATALKAN ***");
        center(b, "Terima kasih sudah berbelanja!");
        return b.toString();
    }
}

/** Pratinjau struk + cetak / simpan PDF. */
final class ReceiptDialog extends JDialog {
    private ReceiptDialog(Window owner, Sale s, boolean justPaid) {
        super(owner, "Pratinjau Struk", ModalityType.APPLICATION_MODAL);
        String text = Receipt.text(s);
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(12, 14, 12, 14));
        JPanel root = new JPanel(new BorderLayout(14, 0));
        root.setBorder(new EmptyBorder(14, 16, 14, 16));
        root.add(Ui.scroll(area), BorderLayout.CENTER);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(250, 10));
        if (justPaid) {
            side.add(Ui.label("Transaksi selesai", Font.BOLD, 16, Ui.OK));
            side.add(Ui.label("PAID", Font.BOLD, 13, Ui.OK));
            side.add(Ui.muted("Total " + Ui.rp(s.total) + " dibayar " + s.method.toLowerCase()));
            side.add(Box.createVerticalStrut(14));
            side.add(Ui.label("Yang tersimpan otomatis", Font.BOLD, 13, Ui.TEXT));
            for (String t : new String[]{"Transaksi tersimpan, status SELESAI", "Stok lot berkurang (lot terdekat kedaluwarsa)",
                    s.memberNo != null ? "Poin member +" + s.pointsEarned + " ditambahkan" : "Tanpa member, tidak ada poin",
                    "Perubahan stok tercatat di LossGuard", "Struk dapat dibuka lagi dari Riwayat"})
                side.add(Ui.muted("<html>- " + t + "</html>"));
            side.add(Box.createVerticalStrut(10));
            side.add(Ui.muted("<html>Struk bersifat simulasi.</html>"));
        } else {
            side.add(Ui.label(s.id, Font.BOLD, 14, Ui.TEXT));
            side.add(Ui.badge(s.status, Ui.statusColor(s.status) == null ? Ui.MUTED : Ui.statusColor(s.status)));
        }
        side.add(Box.createVerticalGlue());
        Ui.Btn print = Ui.primary("Cetak Struk");
        Ui.Btn pdf = Ui.plain("Simpan PDF");
        Ui.Btn close = Ui.plain(justPaid ? "Transaksi Baru" : "Tutup");
        for (Ui.Btn b : new Ui.Btn[]{print, pdf, close}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            side.add(b);
            side.add(Box.createVerticalStrut(6));
        }
        print.addActionListener(e -> {
            try { area.print(); } catch (PrinterException ex) { Ui.err(this, "Gagal mencetak: " + ex.getMessage()); }
        });
        pdf.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("Struk_" + s.id + ".pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Ui.savePdf(fc.getSelectedFile(), text);
                    Ui.info(this, "Struk disimpan: " + fc.getSelectedFile().getAbsolutePath());
                } catch (Exception ex) {
                    Ui.err(this, "Gagal menyimpan PDF: " + ex.getMessage());
                }
            }
        });
        close.addActionListener(e -> dispose());
        root.add(side, BorderLayout.EAST);
        setContentPane(root);
        setSize(720, 640);
        setLocationRelativeTo(owner);
    }

    static void show(Component parent, Sale s, boolean justPaid) {
        new ReceiptDialog(SwingUtilities.getWindowAncestor(parent), s, justPaid).setVisible(true);
    }
}
