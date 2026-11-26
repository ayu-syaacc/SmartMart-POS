package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

/** SalesReport: pendapatan, produk terlaris, rekap kasir, kerugian + ekspor CSV. */
final class ReportPage extends Page {
    private final JComboBox<String> period = new JComboBox<>(new String[]{"Hari ini", "7 hari", "Bulan ini"});
    private final Ui.StatCard cRev = new Ui.StatCard("Pendapatan bersih", Ui.PRIMARY);
    private final Ui.StatCard cTrx = new Ui.StatCard("Jumlah transaksi", Ui.TEXT);
    private final Ui.StatCard cLoss = new Ui.StatCard("Total kerugian", Ui.DANGER);
    private final Ui.StatCard cDiff = new Ui.StatCard("Selisih penerimaan", Ui.WARN);
    private final Ui.BarChart chart = new Ui.BarChart();
    private final DefaultTableModel lossModel = Ui.model("Jenis kerugian", "Nilai");
    private final DefaultTableModel topModel = Ui.model("Produk terlaris", "Terjual");
    private final DefaultTableModel lowModel = Ui.model("Produk terendah", "Terjual");
    private final DefaultTableModel cashModel = Ui.model("Kasir", "Transaksi", "Penjualan");
    private List<String[]> csv = new ArrayList<>();

    ReportPage(Nav nav) {
        super(nav);
        JPanel top = Ui.transparent(new BorderLayout(0, 10));
        JPanel bar = Ui.transparent(new BorderLayout());
        bar.add(period, BorderLayout.WEST);
        Ui.Btn export = Ui.primary("Ekspor CSV");
        bar.add(export, BorderLayout.EAST);
        top.add(bar, BorderLayout.NORTH);
        JPanel cards = Ui.transparent(new GridLayout(1, 4, 12, 0));
        cards.add(cRev);
        cards.add(cTrx);
        cards.add(cLoss);
        cards.add(cDiff);
        top.add(cards, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel c1 = Ui.card();
        c1.setLayout(new BorderLayout(0, 6));
        c1.add(Ui.label("Pendapatan per hari", Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        c1.add(chart, BorderLayout.CENTER);

        JPanel grid = Ui.transparent(new GridLayout(2, 2, 10, 10));
        grid.add(box(lossModel));
        grid.add(box(cashModel));
        grid.add(box(topModel));
        grid.add(box(lowModel));
        JPanel mid = Ui.transparent(new GridLayout(1, 2, 12, 0));
        mid.add(c1);
        mid.add(grid);
        JPanel wrap = Ui.transparent(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        wrap.add(mid, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);

        period.addActionListener(e -> refresh());
        export.addActionListener(e -> export());
        refresh();
    }

    private JPanel box(DefaultTableModel m) {
        JPanel p = Ui.card();
        p.setLayout(new BorderLayout());
        JTable t = Ui.table(m);
        t.setRowHeight(26);
        p.add(Ui.scroll(t), BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder());
        return p;
    }

    @Override String title() { return "Laporan Penjualan"; }

    @Override String subtitle() { return "SalesReport: pendapatan, produk terlaris, rekap kasir, dan kerugian"; }

    private LocalDate from() {
        LocalDate t = LocalDate.now();
        switch (period.getSelectedIndex()) {
            case 0: return t;
            case 1: return t.minusDays(6);
            default: return t.withDayOfMonth(1);
        }
    }

    @Override void refresh() {
        LocalDate a = from(), b = LocalDate.now();
        final LocalDate periodStart = a;
        csv = new ArrayList<>();
        long rev = db.revenue(a, b);
        List<Sale> done = new ArrayList<>();
        for (Sale s : db.sales(a, b)) if ("SELESAI".equals(s.status)) done.add(s);
        long sum = 0;
        for (Sale s : done) sum += s.total;
        cRev.set(Ui.rp(rev), "PAID dikurangi refund retur");
        cTrx.set(String.valueOf(done.size()), done.isEmpty() ? " " : "Rata-rata " + Ui.rp(sum / done.size()));
        List<StockLog> logs = db.logs(a, b);
        long exp = db.lossBy(logs, "KEDALUWARSA"), bad = db.lossBy(logs, "RUSAK"), lost = db.lossBy(logs, "HILANG");
        cLoss.set(Ui.rp(exp + bad + lost), "Dari LossGuard");
        cDiff.set(Ui.rp(db.discrepancy(a, b)), "Ditampilkan terpisah");

        LocalDate ca = period.getSelectedIndex() == 0 ? b.minusDays(6) : a;
        a = ca.isAfter(a) ? a : ca;
        int n = (int) (b.toEpochDay() - a.toEpochDay()) + 1;
        String[] labels = new String[n];
        double[] vals = new double[n];
        for (int i = 0; i < n; i++) {
            LocalDate d = a.plusDays(i);
            labels[i] = n <= 7 ? d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Db.ID) : String.valueOf(d.getDayOfMonth());
            vals[i] = db.revenue(d, d);
            csv.add(new String[]{"Pendapatan harian", d.toString(), String.valueOf(Math.round(vals[i]))});
        }
        chart.set(labels, vals);

        lossModel.setRowCount(0);
        lossModel.addRow(new Object[]{"Kedaluwarsa", Ui.rp(exp)});
        lossModel.addRow(new Object[]{"Rusak", Ui.rp(bad)});
        lossModel.addRow(new Object[]{"Hilang", Ui.rp(lost)});
        csv.add(new String[]{"Kerugian", "Kedaluwarsa", String.valueOf(exp)});
        csv.add(new String[]{"Kerugian", "Rusak", String.valueOf(bad)});
        csv.add(new String[]{"Kerugian", "Hilang", String.valueOf(lost)});

        Map<String, Double> sold = new LinkedHashMap<>();
        Map<String, String> units = new HashMap<>();
        for (Product p : db.d.products) { sold.put(p.name, 0.0); units.put(p.name, p.perKg() ? " kg" : " pcs"); }
        Map<String, long[]> cash = new TreeMap<>();
        for (Sale s : done) {
            for (SaleItem it : s.items) sold.merge(it.name, it.qty - it.returned, Double::sum);
            long[] c = cash.computeIfAbsent(s.cashier, k -> new long[2]);
            c[0]++;
            c[1] += s.total;
        }
        List<Map.Entry<String, Double>> es = new ArrayList<>(sold.entrySet());
        es.sort((x, y) -> Double.compare(y.getValue(), x.getValue()));
        topModel.setRowCount(0);
        lowModel.setRowCount(0);
        for (int i = 0; i < Math.min(5, es.size()); i++) {
            Map.Entry<String, Double> e = es.get(i);
            topModel.addRow(new Object[]{e.getKey(), Ui.qty(e.getValue()) + units.get(e.getKey())});
        }
        for (int i = es.size() - 1; i >= Math.max(0, es.size() - 4); i--) {
            Map.Entry<String, Double> e = es.get(i);
            lowModel.addRow(new Object[]{e.getKey(), Ui.qty(e.getValue()) + units.get(e.getKey())});
        }
        for (Map.Entry<String, Double> e : es) csv.add(new String[]{"Produk terjual", e.getKey(), Ui.qty(e.getValue())});
        cashModel.setRowCount(0);
        for (Map.Entry<String, long[]> e : cash.entrySet()) {
            cashModel.addRow(new Object[]{e.getKey(), e.getValue()[0] + " transaksi", Ui.rp(e.getValue()[1])});
            csv.add(new String[]{"Rekap kasir", e.getKey(), e.getValue()[0] + " transaksi", String.valueOf(e.getValue()[1])});
        }
        csv.add(0, new String[]{"Ringkasan", "Pendapatan bersih", String.valueOf(rev)});
        csv.add(1, new String[]{"Ringkasan", "Jumlah transaksi", String.valueOf(done.size())});
        csv.add(2, new String[]{"Ringkasan", "Selisih penerimaan", String.valueOf(db.discrepancy(a, b))});
    }

    private void export() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("laporan_smartmart_" + from() + "_sd_" + LocalDate.now() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        StringBuilder sb = new StringBuilder("\uFEFFKelompok,Kunci,Nilai,Nilai 2\n");
        for (String[] r : csv) {
            for (int i = 0; i < r.length; i++) sb.append(i > 0 ? "," : "").append('"').append(r[i].replace("\"", "\"\"")).append('"');
            sb.append('\n');
        }
        try {
            Files.write(fc.getSelectedFile().toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            Ui.info(this, "Laporan diekspor ke:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (IOException e) {
            Ui.err(this, "Gagal mengekspor: " + e.getMessage());
        }
    }
}
