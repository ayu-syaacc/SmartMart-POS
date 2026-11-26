package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Layar transaksi penjualan kasir. */
final class PosPage extends Page {
    private final Map<String, Double> cart = new LinkedHashMap<>();
    private final List<String> rowCodes = new ArrayList<>();
    private Member member;
    private String category = "Semua";
    private final JTextField search = Ui.field(20);
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
    private final DefaultTableModel cartModel = Ui.model("Produk", "Qty", "Jumlah");
    private final JTable cartTable = Ui.table(cartModel);
    private final JLabel lblMember = Ui.label("Pelanggan umum", Font.BOLD, 14, Ui.TEXT);
    private final JLabel lblMemberSub = Ui.muted("Tanpa diskon member");
    private final JLabel lblCount = Ui.label("Keranjang (0)", Font.BOLD, 15, Ui.TEXT);
    private final JLabel lblSub = Ui.label("Rp0", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblMem = Ui.label("-Rp0", Font.PLAIN, 13, Ui.DANGER);
    private final JLabel lblFs = Ui.label("-Rp0", Font.PLAIN, 13, Ui.DANGER);
    private final JLabel lblTotal = Ui.label("Rp0", Font.BOLD, 22, Ui.PRIMARY);
    private final JLabel lblSave = Ui.muted(" ");
    private final Ui.Btn btnPay = Ui.primary("Bayar");

    PosPage(Nav nav) {
        super(nav);
        add(buildLeft(), BorderLayout.CENTER);
        add(buildRight(), BorderLayout.EAST);
        refresh();
    }

    @Override String title() { return "Transaksi Penjualan"; }

    @Override String subtitle() { return "Scan barcode atau pilih produk"; }

    @Override void refresh() {
        loadGrid();
        refreshCart();
    }

    void setMember(Member m) {
        member = m;
        refreshCart();
    }

    // ----------------------------------------------------------- produk
    private JPanel buildLeft() {
        JPanel p = Ui.transparent(new BorderLayout(0, 10));
        JPanel top = Ui.transparent(new BorderLayout(0, 8));
        search.putClientProperty("JTextField.placeholderText", "Scan barcode / ketik nama produk...");
        search.setToolTipText("Scan barcode / ketik nama produk, lalu tekan Enter untuk menambah");
        search.getDocument().addDocumentListener(Ui.onChange(this::loadGrid));
        search.addActionListener(e -> quickAdd());
        top.add(search, BorderLayout.NORTH);
        JPanel chips = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ButtonGroup bg = new ButtonGroup();
        for (String c : new String[]{"Semua", "Minuman", "Makanan", "Segar", "Susu", "Lainnya"}) {
            JToggleButton b = Ui.chip(c);
            b.setSelected(c.equals(category));
            b.addActionListener(e -> {
                category = c;
                loadGrid();
            });
            bg.add(b);
            chips.add(b);
        }
        top.add(chips, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(grid, BorderLayout.NORTH);
        grid.setOpaque(false);
        JScrollPane sp = new JScrollPane(holder);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private List<Product> filtered() {
        String s = search.getText().trim().toLowerCase();
        List<Product> out = new ArrayList<>();
        for (Product p : db.d.products) {
            if (!category.equals("Semua") && !category.equals(p.category)) continue;
            if (!s.isEmpty() && !p.name.toLowerCase().contains(s) && !p.code.contains(s)) continue;
            out.add(p);
        }
        return out;
    }

    private void loadGrid() {
        grid.removeAll();
        List<Product> list = filtered();
        for (Product p : list) grid.add(card(p));
        for (int i = list.size(); i < 3; i++) grid.add(Ui.transparent(null));
        grid.revalidate();
        grid.repaint();
    }

    private JPanel card(Product p) {
        double sell = db.sellable(p.code);
        double stock = db.stock(p.code);
        List<Lot> lots = db.lotsOf(p.code);
        int fs = 0;
        for (Lot l : lots) if (Db.days(l) > 0) { fs = db.fsPct(Db.days(l)); break; }
        boolean blocked = sell <= 0;
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(blocked ? new Color(0xF3F4F6) : Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Ui.BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.add(Ui.label(p.name, Font.BOLD, 14, blocked ? Ui.MUTED : Ui.TEXT));
        c.add(Box.createVerticalStrut(4));
        c.add(Ui.label(Ui.rp(p.price) + (p.perKg() ? "/kg" : ""), Font.BOLD, 14, blocked ? Ui.MUTED : Ui.PRIMARY));
        c.add(Box.createVerticalStrut(6));
        String st = blocked ? (stock > 0 ? "Kedaluwarsa (diblokir)" : "Stok habis")
                : "Stok " + Ui.qty(stock) + (stock < p.minStock ? "  (menipis)" : "");
        c.add(Ui.label(st, Font.PLAIN, 12, blocked ? Ui.DANGER : (stock < p.minStock ? Ui.WARN : Ui.MUTED)));
        if (fs > 0 && !blocked) {
            c.add(Box.createVerticalStrut(6));
            JLabel b = Ui.badge("FreshSave -" + fs + "%", Ui.ORANGE);
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.add(b);
        }
        for (Component k : c.getComponents()) if (k instanceof JComponent) ((JComponent) k).setAlignmentX(Component.LEFT_ALIGNMENT);
        c.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { addProduct(p); }
        });
        return c;
    }

    private void quickAdd() {
        String s = search.getText().trim();
        if (s.isEmpty()) return;
        Product hit = null;
        for (Product p : db.d.products) if (p.code.equals(s)) hit = p;
        if (hit == null) {
            List<Product> l = filtered();
            if (!l.isEmpty()) hit = l.get(0);
        }
        if (hit == null) { Ui.err(this, "Produk tidak ditemukan: " + s); return; }
        addProduct(hit);
        search.setText("");
        search.requestFocusInWindow();
    }

    private void addProduct(Product p) {
        double sell = db.sellable(p.code);
        if (sell <= 0) {
            Ui.err(this, p.name + ": stok habis atau seluruh lot kedaluwarsa (penjualan diblokir).");
            return;
        }
        double inCart = cart.getOrDefault(p.code, 0.0);
        double avail = Db.r3(sell - inCart);
        if (p.perKg()) {
            if (avail <= 0) { Ui.err(this, "Stok " + p.name + " sudah seluruhnya ada di keranjang."); return; }
            Double w = WeightDialog.ask(this, p, avail);
            if (w != null) cart.put(p.code, Db.r3(inCart + w));
        } else {
            if (avail < 1 - 1e-9) { Ui.err(this, "Stok " + p.name + " tidak cukup (tersedia " + Ui.qty(sell) + ")."); return; }
            cart.put(p.code, inCart + 1);
        }
        refreshCart();
    }

    // --------------------------------------------------------- keranjang
    private JPanel buildRight() {
        JPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 10));
        c.setPreferredSize(new Dimension(400, 10));

        JPanel head = Ui.transparent(new BorderLayout(8, 0));
        JPanel mi = Ui.transparent(new GridLayout(2, 1));
        mi.add(lblMember);
        mi.add(lblMemberSub);
        head.add(mi, BorderLayout.CENTER);
        JPanel mb = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        Ui.Btn pick = Ui.plain("Member");
        Ui.Btn clear = Ui.plain("Lepas");
        pick.addActionListener(e -> {
            Member m = MemberPicker.pick(this);
            if (m != null) setMember(m);
        });
        clear.addActionListener(e -> setMember(null));
        mb.add(pick);
        mb.add(clear);
        head.add(mb, BorderLayout.EAST);

        JPanel north = Ui.transparent(new BorderLayout(0, 8));
        north.add(head, BorderLayout.NORTH);
        north.add(lblCount, BorderLayout.SOUTH);
        c.add(north, BorderLayout.NORTH);

        cartTable.setRowHeight(34);
        Ui.widths(cartTable, 200, 60, 90);
        c.add(Ui.scroll(cartTable), BorderLayout.CENTER);

        JPanel south = Ui.transparent(new BorderLayout(0, 8));
        JPanel ops = Ui.transparent(new GridLayout(1, 4, 6, 0));
        Ui.Btn minus = Ui.plain("-");
        Ui.Btn plus = Ui.plain("+");
        Ui.Btn del = Ui.plain("Hapus");
        Ui.Btn clr = Ui.plain("Kosongkan");
        minus.addActionListener(e -> step(-1));
        plus.addActionListener(e -> step(1));
        del.addActionListener(e -> removeSelected());
        clr.addActionListener(e -> {
            if (!cart.isEmpty() && Ui.ask(this, "Kosongkan seluruh keranjang?")) {
                cart.clear();
                refreshCart();
            }
        });
        Ui.compact(minus);
        Ui.compact(plus);
        Ui.compact(del);
        Ui.compact(clr);
        ops.add(minus);
        ops.add(plus);
        ops.add(del);
        ops.add(clr);
        south.add(ops, BorderLayout.NORTH);

        JPanel sum = Ui.transparent(new GridLayout(0, 1, 0, 3));
        sum.add(row("Subtotal", lblSub));
        sum.add(row("Diskon Member", lblMem));
        sum.add(row("Diskon FreshSave", lblFs));
        JPanel tot = Ui.transparent(new BorderLayout());
        tot.add(Ui.label("Total", Font.BOLD, 16, Ui.TEXT), BorderLayout.WEST);
        tot.add(lblTotal, BorderLayout.EAST);
        sum.add(tot);
        sum.add(lblSave);
        JPanel bottom = Ui.transparent(new BorderLayout(0, 8));
        bottom.add(sum, BorderLayout.CENTER);
        btnPay.setFont(Ui.f(Font.BOLD, 16));
        btnPay.addActionListener(e -> pay());
        bottom.add(btnPay, BorderLayout.SOUTH);
        south.add(bottom, BorderLayout.CENTER);
        c.add(south, BorderLayout.SOUTH);
        return c;
    }

    private JPanel row(String k, JLabel v) {
        JPanel p = Ui.transparent(new BorderLayout());
        p.add(Ui.label(k, Font.PLAIN, 13, Ui.MUTED), BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    private void step(int dir) {
        int r = cartTable.getSelectedRow();
        if (r < 0) { Ui.info(this, "Pilih baris di keranjang terlebih dahulu."); return; }
        String code = rowCodes.get(r);
        Product p = db.product(code);
        double delta = p.perKg() ? 0.25 : 1;
        double now = cart.get(code);
        double nq = Db.r3(now + dir * delta);
        if (nq <= 0) cart.remove(code);
        else if (nq > db.sellable(code) + 1e-9) { Ui.err(this, "Stok tidak cukup (tersedia " + Ui.qty(db.sellable(code)) + ")."); return; }
        else cart.put(code, nq);
        refreshCart();
        if (cart.containsKey(code)) cartTable.setRowSelectionInterval(Math.min(r, cartTable.getRowCount() - 1), Math.min(r, cartTable.getRowCount() - 1));
    }

    private void removeSelected() {
        int r = cartTable.getSelectedRow();
        if (r < 0) return;
        cart.remove(rowCodes.get(r));
        refreshCart();
    }

    void refreshCart() {
        if (member != null) {
            Member fresh = db.member(member.no);
            member = fresh;
        }
        lblMember.setText(member == null ? "Pelanggan umum" : member.name);
        lblMemberSub.setText(member == null ? "Tanpa diskon member"
                : member.no + "  |  " + member.points + " poin  |  Diskon " + db.d.s.memberDisc + "%");
        Db.Quote q = db.quote(cart, member);
        cartModel.setRowCount(0);
        rowCodes.clear();
        for (Db.QLine l : q.lines) {
            String name = l.tag.isEmpty() ? l.name : "<html>" + l.name + "<br><font color='#D97706' size='-2'>" + l.tag + "</font></html>";
            cartModel.addRow(new Object[]{name, Ui.qty(l.qty) + ("KG".equals(l.unit) ? " kg" : ""), Ui.rp(l.net)});
            rowCodes.add(l.code);
        }
        lblCount.setText("Keranjang (" + q.lines.size() + ")");
        lblSub.setText(Ui.rp(q.subtotal));
        lblMem.setText("-" + Ui.rp(q.memberDisc));
        lblFs.setText("-" + Ui.rp(q.fsDisc));
        lblTotal.setText(Ui.rp(q.after));
        lblSave.setText(q.discount > 0 ? "Hemat " + Ui.rp(q.discount)
                + (member != null ? "  |  +" + (q.after / db.d.s.pointPerRp) + " poin setelah dibayar" : "") : " ");
        btnPay.setEnabled(!cart.isEmpty());
    }

    private void pay() {
        if (cart.isEmpty()) return;
        Sale s = PaymentDialog.ask(this, cart, member, nav.user());
        if (s == null) return;
        cart.clear();
        refreshCart();
        loadGrid();
        ReceiptDialog.show(this, s, true);
    }
}
