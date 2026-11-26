package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** FreshSave: diskon otomatis berdasarkan sisa masa simpan, dihitung per lot. */
final class FreshPage extends Page {
    private final JSpinner[] days = new JSpinner[3];
    private final JSpinner[] pct = new JSpinner[3];
    private final DefaultTableModel model = Ui.model("Produk", "Lot", "Sisa", "FreshSave", "Member", "Dipakai");

    FreshPage(Nav nav) {
        super(nav);
        JPanel left = Ui.card();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(360, 10));
        left.add(left(Ui.label("Aturan diskon", Font.BOLD, 14, Ui.TEXT)));
        left.add(left(Ui.muted("<html>Persentase dapat diubah admin. Kasir hanya melihat hasilnya.</html>")));
        left.add(Box.createVerticalStrut(10));
        // urutan tampilan: 7 hari, 3 hari, 1 hari (indeks 0, 1, 2 pada pengaturan)
        for (int i = 0; i < 3; i++) {
            days[i] = new JSpinner(new SpinnerNumberModel(db.d.s.fsDays[i], 1, 365, 1));
            pct[i] = new JSpinner(new SpinnerNumberModel(db.d.s.fsPct[i], 0, 100, 1));
            JPanel row = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.add(new JLabel("Sisa"));
            row.add(days[i]);
            row.add(new JLabel("hari atau kurang:"));
            row.add(pct[i]);
            row.add(new JLabel("%"));
            left.add(left(row));
            days[i].addChangeListener(e -> preview());
            pct[i].addChangeListener(e -> preview());
        }
        left.add(left(Ui.muted("Di atas batas terbesar: tidak ada diskon FreshSave (0%).")));
        left.add(Box.createVerticalStrut(10));
        left.add(left(Ui.label("Diskon tidak ditumpuk", Font.BOLD, 13, Ui.TEXT)));
        left.add(left(Ui.muted("<html>Tiap barang memakai diskon terbesar antara FreshSave dan member. Jika sama, sumber dicatat FRESHSAVE.</html>")));
        left.add(Box.createVerticalStrut(10));
        left.add(left(Ui.label("Barang FreshSave tidak dapat diretur", Font.BOLD, 13, Ui.TEXT)));
        left.add(left(Ui.muted("<html>Lot kedaluwarsa (0 hari) diblokir dan tanpa diskon.</html>")));
        left.add(Box.createVerticalStrut(14));
        Ui.Btn save = Ui.primary("Simpan Aturan");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> save());
        left.add(save);
        add(left, BorderLayout.WEST);

        JPanel right = Ui.card();
        right.setLayout(new BorderLayout(0, 8));
        right.add(Ui.label("Pratinjau per lot (mengikuti nilai di kiri)", Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        JTable t = Ui.table(model);
        Ui.widths(t, 180, 70, 80, 90, 80, 110);
        right.add(Ui.scroll(t), BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
        preview();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    @Override String title() { return "FreshSave"; }

    @Override String subtitle() { return "Diskon otomatis berdasarkan sisa masa simpan, dihitung per lot"; }

    @Override void refresh() {
        for (int i = 0; i < 3; i++) {
            days[i].setValue(db.d.s.fsDays[i]);
            pct[i].setValue(db.d.s.fsPct[i]);
        }
        preview();
    }

    private int pctFor(long d) {
        if (d <= 0) return 0;
        int best = 0, bestDays = Integer.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            int dd = (Integer) days[i].getValue();
            if (d <= dd && dd < bestDays) {
                best = (Integer) pct[i].getValue();
                bestDays = dd;
            }
        }
        return best;
    }

    private void preview() {
        model.setRowCount(0);
        List<Lot> lots = new ArrayList<>();
        for (Lot l : db.d.lots) if (l.qty > 1e-9) lots.add(l);
        lots.sort((a, b) -> Long.compare(Db.days(a), Db.days(b)));
        for (Lot l : lots) {
            long d = Db.days(l);
            int fs = pctFor(d), mem = db.d.s.memberDisc;
            String used = d <= 0 ? "DIBLOKIR" : (fs <= 0 && mem <= 0 ? "-" : (fs >= mem ? "FRESHSAVE" : "MEMBER"));
            model.addRow(new Object[]{db.product(l.productCode).name, l.id, d + " hari", d <= 0 ? "Blokir" : (fs > 0 ? fs + "%" : "-"),
                    d <= 0 ? "-" : mem + "%", used});
        }
    }

    private void save() {
        int[] d = new int[3], p = new int[3];
        for (int i = 0; i < 3; i++) {
            d[i] = (Integer) days[i].getValue();
            p[i] = (Integer) pct[i].getValue();
        }
        if (d[0] == d[1] || d[0] == d[2] || d[1] == d[2]) { Ui.err(this, "Batas hari tidak boleh sama satu dengan yang lain."); return; }
        db.d.s.fsDays = d;
        db.d.s.fsPct = p;
        db.log("Mengubah aturan FreshSave: " + d[0] + "h/" + p[0] + "%, " + d[1] + "h/" + p[1] + "%, " + d[2] + "h/" + p[2] + "%");
        Ui.info(this, "Aturan FreshSave disimpan. Berlaku untuk transaksi berikutnya.");
        preview();
    }
}
