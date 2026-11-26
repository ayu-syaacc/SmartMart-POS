package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;

/** Dashboard admin: ringkasan operasional hari ini. */
final class DashboardPage extends Page {
    private final Ui.StatCard cOmzet = new Ui.StatCard("Omzet hari ini", Ui.PRIMARY);
    private final Ui.StatCard cTrx = new Ui.StatCard("Transaksi", Ui.TEXT);
    private final Ui.StatCard cLoss = new Ui.StatCard("Kerugian hari ini", Ui.DANGER);
    private final Ui.StatCard cCrit = new Ui.StatCard("Stok kritis", Ui.ORANGE);
    private final Ui.BarChart chart = new Ui.BarChart();
    private final DefaultTableModel lossModel = Ui.model("Waktu", "Jenis", "Produk", "Jumlah", "Pelaku");
    private final DefaultTableModel alertModel = Ui.model("Produk", "Keterangan", "Status");

    DashboardPage(Nav nav) {
        super(nav);
        JPanel cards = Ui.transparent(new GridLayout(1, 4, 12, 0));
        cards.add(cOmzet);
        cards.add(cTrx);
        cards.add(cLoss);
        cards.add(cCrit);
        add(cards, BorderLayout.NORTH);

        JPanel left = Ui.transparent(new GridLayout(2, 1, 0, 12));
        JPanel c1 = Ui.card();
        c1.setLayout(new BorderLayout(0, 6));
        c1.add(Ui.label("Penjualan 7 hari terakhir", Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        c1.add(chart, BorderLayout.CENTER);
        left.add(c1);
        JPanel c2 = Ui.card();
        c2.setLayout(new BorderLayout(0, 6));
        JPanel h2 = Ui.transparent(new BorderLayout());
        h2.add(Ui.label("Log LossGuard terbaru", Font.BOLD, 14, Ui.TEXT), BorderLayout.WEST);
        Ui.Btn all = Ui.plain("Lihat semua");
        all.addActionListener(e -> nav.go("loss"));
        h2.add(all, BorderLayout.EAST);
        c2.add(h2, BorderLayout.NORTH);
        JTable lt = Ui.table(lossModel);
        Ui.widths(lt, 70, 130, 140, 60, 70);
        c2.add(Ui.scroll(lt), BorderLayout.CENTER);
        left.add(c2);

        JPanel right = Ui.card();
        right.setLayout(new BorderLayout(0, 6));
        JPanel h3 = Ui.transparent(new BorderLayout());
        h3.add(Ui.label("StockAlert", Font.BOLD, 14, Ui.TEXT), BorderLayout.WEST);
        Ui.Btn open = Ui.plain("Kelola");
        open.addActionListener(e -> nav.go("stock"));
        h3.add(open, BorderLayout.EAST);
        right.add(h3, BorderLayout.NORTH);
        JTable at = Ui.table(alertModel);
        Ui.widths(at, 140, 150, 100);
        right.add(Ui.scroll(at), BorderLayout.CENTER);
        JPanel foot = Ui.transparent(new BorderLayout(0, 6));
        Ui.Btn dispose = Ui.danger("Buang lot kedaluwarsa");
        dispose.addActionListener(e -> disposeExpired());
        foot.add(dispose, BorderLayout.CENTER);
        foot.add(Ui.muted("Penjualan lot kedaluwarsa otomatis diblokir"), BorderLayout.SOUTH);
        right.add(foot, BorderLayout.SOUTH);

        JPanel mid = Ui.transparent(new GridLayout(1, 2, 12, 0));
        mid.add(left);
        mid.add(right);
        JPanel wrap = Ui.transparent(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrap.add(mid, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        refresh();
    }

    @Override String title() { return "Dashboard Admin"; }

    @Override String subtitle() { return "Ringkasan operasional hari ini"; }

    static int sev(String status) {
        switch (status) {
            case "KEDALUWARSA": return 0;
            case "SEGERA": return 1;
            case "PRIORITAS": return 2;
            case "HABIS": case "MENIPIS": return 3;
            case "PERHATIAN": return 4;
            default: return 5;
        }
    }

    @Override void refresh() {
        LocalDate today = LocalDate.now();
        long rev = db.revenue(today, today), yest = db.revenue(today.minusDays(1), today.minusDays(1));
        cOmzet.set(Ui.rp(rev), yest > 0 ? String.format(Db.ID, "%+d%% dari kemarin", Math.round((rev - yest) * 100.0 / yest)) : "Belum ada data kemarin");
        int n = 0;
        for (Sale s : db.sales(today, today)) if ("SELESAI".equals(s.status)) n++;
        cTrx.set(String.valueOf(n), n > 0 ? "Rata-rata " + Ui.rp(db.sales(today, today).stream().filter(s -> "SELESAI".equals(s.status))
                .mapToLong(s -> s.total).sum() / n) : "Belum ada transaksi");
        cLoss.set(Ui.rp(db.lossBy(db.logs(today, today), null)), "Kedaluwarsa, rusak, hilang");
        int crit = db.criticalCount();
        cCrit.set(crit + " produk", "Perlu tindakan");

        String[] labels = new String[7];
        double[] vals = new double[7];
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            labels[i] = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Db.ID);
            vals[i] = db.revenue(d, d);
        }
        chart.set(labels, vals);

        lossModel.setRowCount(0);
        List<StockLog> logs = db.logs(LocalDate.of(2000, 1, 1), today);
        for (int i = 0; i < Math.min(5, logs.size()); i++) {
            StockLog g = logs.get(i);
            lossModel.addRow(new Object[]{g.time.format(Db.TF), g.type, g.product, (g.delta > 0 ? "+" : "") + Ui.qty(g.delta), g.actor});
        }

        alertModel.setRowCount(0);
        List<Object[]> rows = new ArrayList<>();
        for (Product p : db.d.products) {
            String st = db.productStatus(p);
            if ("AMAN".equals(st)) continue;
            List<Lot> lots = db.lotsOf(p.code);
            String info;
            if (st.equals("MENIPIS") || st.equals("HABIS") || lots.isEmpty())
                info = "Stok " + Ui.qty(db.stock(p.code)) + " | minimum " + p.minStock;
            else {
                Lot l = lots.get(0);
                info = "Lot " + l.id + " | sisa " + Math.max(0, Db.days(l)) + " hari";
            }
            rows.add(new Object[]{p.name, info, st});
        }
        rows.sort((a, b) -> Integer.compare(sev((String) a[2]), sev((String) b[2])));
        for (Object[] r : rows) alertModel.addRow(r);
    }

    private void disposeExpired() {
        if (!Ui.ask(this, "Buang semua lot yang sudah kedaluwarsa? Tindakan ini tercatat di LossGuard.")) return;
        int n = db.disposeExpired(nav.user());
        Ui.info(this, n == 0 ? "Tidak ada lot kedaluwarsa." : n + " lot kedaluwarsa dibuang dan tercatat di LossGuard.");
        refresh();
    }
}
