package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** LossGuard: catatan setiap perubahan stok (waktu, pelaku, alasan, lot) dan nilai kerugian. */
final class LossPage extends Page {
    private final JComboBox<String> period = new JComboBox<>(new String[]{"Hari ini", "7 hari", "Bulan ini", "Semua"});
    private final String[] types = {"Semua", "MASUK", "TERJUAL", "DIRETUR", "RUSAK", "KEDALUWARSA", "HILANG", "PENYESUAIAN"};
    private String type = "Semua";
    private final Ui.StatCard cExp = new Ui.StatCard("Kedaluwarsa", Ui.DANGER);
    private final Ui.StatCard cBad = new Ui.StatCard("Rusak", Ui.WARN);
    private final Ui.StatCard cLost = new Ui.StatCard("Hilang", Ui.DANGER);
    private final Ui.StatCard cTot = new Ui.StatCard("Total kerugian", Ui.TEXT);
    private final DefaultTableModel model = Ui.model("Waktu", "Jenis", "Produk", "Lot", "Jumlah", "Pelaku", "Alasan");
    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("dd MMM HH:mm", Db.ID);

    LossPage(Nav nav) {
        super(nav);
        JPanel cs = Ui.transparent(new GridLayout(1, 4, 12, 0));
        cs.add(cExp);
        cs.add(cBad);
        cs.add(cLost);
        cs.add(cTot);
        add(cs, BorderLayout.NORTH);

        JPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 10));
        JPanel top = Ui.transparent(new BorderLayout(8, 0));
        JPanel chips = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ButtonGroup bg = new ButtonGroup();
        for (String t : types) {
            JToggleButton b = Ui.chip(t.equals("Semua") ? t : t.charAt(0) + t.substring(1).toLowerCase());
            b.setSelected(t.equals(type));
            b.addActionListener(e -> { type = t; load(); });
            bg.add(b);
            chips.add(b);
        }
        top.add(chips, BorderLayout.CENTER);
        top.add(period, BorderLayout.EAST);
        c.add(top, BorderLayout.NORTH);
        JTable t = Ui.table(model);
        Ui.widths(t, 100, 110, 160, 70, 70, 80, 280);
        c.add(Ui.scroll(t), BorderLayout.CENTER);
        c.add(Ui.muted("Catatan setiap perubahan stok: waktu, pelaku, alasan, lot. Ringkasan kerugian mengikuti periode yang dipilih."), BorderLayout.SOUTH);
        JPanel wrap = Ui.transparent(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrap.add(c, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        period.addActionListener(e -> load());
        load();
    }

    @Override String title() { return "LossGuard"; }

    @Override String subtitle() { return "Catatan setiap perubahan stok: waktu, pelaku, alasan, lot"; }

    @Override void refresh() { load(); }

    private LocalDate from() {
        LocalDate t = LocalDate.now();
        switch (period.getSelectedIndex()) {
            case 0: return t;
            case 1: return t.minusDays(6);
            case 2: return t.withDayOfMonth(1);
            default: return LocalDate.of(2000, 1, 1);
        }
    }

    private void load() {
        List<StockLog> logs = db.logs(from(), LocalDate.now());
        long exp = db.lossBy(logs, "KEDALUWARSA"), bad = db.lossBy(logs, "RUSAK"), lost = db.lossBy(logs, "HILANG");
        int ne = 0, nb = 0, nl = 0;
        for (StockLog g : logs) {
            if ("KEDALUWARSA".equals(g.type) && g.loss > 0) ne++;
            if ("RUSAK".equals(g.type) && g.loss > 0) nb++;
            if ("HILANG".equals(g.type) && g.loss > 0) nl++;
        }
        cExp.set(Ui.rp(exp), ne + " kejadian");
        cBad.set(Ui.rp(bad), nb + " kejadian");
        cLost.set(Ui.rp(lost), nl + " kasus");
        cTot.set(Ui.rp(exp + bad + lost), period.getSelectedItem().toString());
        model.setRowCount(0);
        for (StockLog g : logs) {
            if (!type.equals("Semua") && !type.equals(g.type)) continue;
            model.addRow(new Object[]{g.time.format(SHORT), g.type, g.product, g.lotId == null ? "-" : g.lotId,
                    (g.delta > 0 ? "+" : "") + Ui.qty(g.delta), g.actor, g.reason});
        }
    }
}
