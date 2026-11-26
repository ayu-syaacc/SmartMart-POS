package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** StockAlert + Penyesuaian Stok: status lot per kedaluwarsa, stok menipis, buang lot, kurangi stok manual. */
final class StockPage extends Page {
    private final Ui.StatCard[] cards = {new Ui.StatCard("Aman (>" + db().d.s.warnDays + " hr)", Ui.OK),
            new Ui.StatCard("Perhatian (4-" + db().d.s.warnDays + " hr)", Ui.WARN), new Ui.StatCard("Prioritas (2-3 hr)", Ui.ORANGE),
            new Ui.StatCard("Segera (1 hr)", Ui.ORANGE), new Ui.StatCard("Kedaluwarsa (0 hr)", Ui.DANGER)};
    private final DefaultTableModel model = Ui.model("Produk", "Lot", "Sisa masa", "Jumlah", "Status");
    private final JTable table = Ui.table(model);
    private final List<Lot> rowLots = new ArrayList<>();
    private final JLabel lblExpired = Ui.label(" ", Font.PLAIN, 13, Ui.TEXT);
    private final JComboBox<String> lotBox = new JComboBox<>();
    private final List<Lot> boxLots = new ArrayList<>();
    private final JRadioButton[] types = {new JRadioButton("Rusak", true), new JRadioButton("Hilang"),
            new JRadioButton("Buang kedaluwarsa"), new JRadioButton("Penyesuaian")};
    private final String[] typeKeys = {"RUSAK", "HILANG", "KEDALUWARSA", "PENYESUAIAN"};
    private final JTextField qty = Ui.field(8);
    private final JTextField reason = Ui.field(20);
    private final JLabel lblAfter = Ui.label(" ", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblAs = Ui.label(" ", Font.BOLD, 13, Ui.WARN);

    private static Db db() { return Db.get(); }

    StockPage(Nav nav) {
        super(nav);
        JPanel cs = Ui.transparent(new GridLayout(1, 5, 10, 0));
        for (Ui.StatCard c : cards) cs.add(c);
        add(cs, BorderLayout.NORTH);

        JPanel left = Ui.card();
        left.setLayout(new BorderLayout(0, 8));
        left.add(Ui.label("Status lot dan stok menipis", Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        left.add(Ui.scroll(table), BorderLayout.CENTER);
        Ui.widths(table, 180, 70, 90, 80, 110);
        add(left, BorderLayout.CENTER);

        JPanel right = Ui.card();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(340, 10));
        right.add(left(Ui.label("Tindakan admin", Font.BOLD, 14, Ui.TEXT)));
        right.add(Box.createVerticalStrut(8));
        right.add(left(Ui.label("Lot kedaluwarsa", Font.BOLD, 13, Ui.DANGER)));
        right.add(left(lblExpired));
        right.add(left(Ui.muted("Penjualan diblokir otomatis.")));
        right.add(Box.createVerticalStrut(4));
        Ui.Btn dispose = Ui.danger("Buang lot kedaluwarsa");
        dispose.setAlignmentX(Component.LEFT_ALIGNMENT);
        dispose.addActionListener(e -> disposeExpired());
        right.add(dispose);
        right.add(Box.createVerticalStrut(14));
        right.add(left(Ui.label("Kurangi stok manual", Font.BOLD, 13, Ui.TEXT)));
        right.add(left(Ui.muted("Produk / lot")));
        lotBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        lotBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        right.add(lotBox);
        ButtonGroup bg = new ButtonGroup();
        JPanel tp = Ui.transparent(new GridLayout(2, 2));
        for (JRadioButton r : types) {
            r.setOpaque(false);
            bg.add(r);
            tp.add(r);
            r.addActionListener(e -> preview());
        }
        right.add(Box.createVerticalStrut(4));
        right.add(left(tp));
        right.add(left(Ui.muted("Jumlah dikurangi")));
        qty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        right.add(left(qty));
        right.add(left(Ui.muted("Alasan (wajib)")));
        reason.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        right.add(left(reason));
        right.add(Box.createVerticalStrut(6));
        right.add(left(lblAfter));
        right.add(left(lblAs));
        right.add(left(Ui.muted("Tercatat di LossGuard: waktu, pelaku, alasan, dan lot ikut disimpan.")));
        right.add(Box.createVerticalStrut(8));
        Ui.Btn save = Ui.primary("Simpan Penyesuaian");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> save());
        right.add(save);
        add(right, BorderLayout.EAST);

        lotBox.addActionListener(e -> preview());
        qty.getDocument().addDocumentListener(Ui.onChange(this::preview));
        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            if (e.getValueIsAdjusting() || r < 0 || r >= rowLots.size() || rowLots.get(r) == null) return;
            int idx = boxLots.indexOf(rowLots.get(r));
            if (idx >= 0) lotBox.setSelectedIndex(idx);
        });
        refresh();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    @Override String title() { return "StockAlert"; }

    @Override String subtitle() { return "Peringatan stok menipis, status kedaluwarsa per lot, dan penyesuaian stok"; }

    @Override void refresh() {
        int[] counts = new int[5];
        List<Lot> lots = new ArrayList<>();
        for (Lot l : db.d.lots) if (l.qty > 1e-9) lots.add(l);
        lots.sort((a, b) -> Long.compare(Db.days(a), Db.days(b)));
        for (Lot l : lots) {
            String st = db.lotStatus(Db.days(l));
            switch (st) {
                case "AMAN": counts[0]++; break;
                case "PERHATIAN": counts[1]++; break;
                case "PRIORITAS": counts[2]++; break;
                case "SEGERA": counts[3]++; break;
                default: counts[4]++;
            }
        }
        for (int i = 0; i < 5; i++) cards[i].set(counts[i] + " lot", " ");

        model.setRowCount(0);
        rowLots.clear();
        for (Lot l : lots) {
            String st = db.lotStatus(Db.days(l));
            if (st.equals("AMAN")) continue;
            model.addRow(new Object[]{db.product(l.productCode).name, l.id, Math.max(0, Db.days(l)) + " hari", Ui.qty(l.qty), st});
            rowLots.add(l);
        }
        for (Product p : db.d.products) {
            if (db.stock(p.code) < p.minStock) {
                model.addRow(new Object[]{p.name, "-", "-", Ui.qty(db.stock(p.code)), db.stock(p.code) <= 0 ? "HABIS" : "MENIPIS"});
                rowLots.add(null);
            }
        }
        int expired = 0;
        double eq = 0;
        for (Lot l : lots) if (Db.days(l) <= 0) { expired++; eq += l.qty; }
        lblExpired.setText(expired == 0 ? "Tidak ada lot kedaluwarsa." : expired + " lot, total " + Ui.qty(eq) + " unit");

        Object sel = lotBox.getSelectedItem();
        lotBox.removeAllItems();
        boxLots.clear();
        for (Lot l : lots) {
            boxLots.add(l);
            lotBox.addItem(db.product(l.productCode).name + " | Lot " + l.id + " | sisa " + Math.max(0, Db.days(l)) + " hr | jml " + Ui.qty(l.qty));
        }
        if (sel != null) lotBox.setSelectedItem(sel);
        preview();
    }

    private Lot chosenLot() {
        int i = lotBox.getSelectedIndex();
        return i < 0 || i >= boxLots.size() ? null : boxLots.get(i);
    }

    private String chosenType() {
        for (int i = 0; i < types.length; i++) if (types[i].isSelected()) return typeKeys[i];
        return "RUSAK";
    }

    private void preview() {
        Lot l = chosenLot();
        double q = Ui.parseQty(qty.getText());
        if (l == null) { lblAfter.setText(" "); lblAs.setText(" "); return; }
        lblAfter.setText("Stok lot: " + Ui.qty(l.qty) + "  >  Sesudah: " + (q > 0 ? Ui.qty(Math.max(0, l.qty - q)) : "-"));
        lblAs.setText("Dicatat sebagai " + chosenType());
    }

    private void save() {
        Lot l = chosenLot();
        if (l == null) { Ui.err(this, "Pilih produk / lot terlebih dahulu."); return; }
        double q = Ui.parseQty(qty.getText());
        try {
            db.adjust(l, chosenType(), q, reason.getText(), nav.user());
            Ui.info(this, "Penyesuaian tersimpan dan tercatat di LossGuard.");
            qty.setText("");
            reason.setText("");
            refresh();
        } catch (IllegalArgumentException ex) {
            Ui.err(this, ex.getMessage());
        }
    }

    private void disposeExpired() {
        if (!Ui.ask(this, "Buang semua lot kedaluwarsa? Tindakan ini tercatat di LossGuard.")) return;
        int n = db.disposeExpired(nav.user());
        Ui.info(this, n == 0 ? "Tidak ada lot kedaluwarsa." : n + " lot dibuang.");
        refresh();
    }
}
