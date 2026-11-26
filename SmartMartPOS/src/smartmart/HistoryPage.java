package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Riwayat transaksi: cari, lihat detail, ajukan retur atau void. */
final class HistoryPage extends Page {
    private final JTextField search = Ui.field(22);
    private final JComboBox<String> period = new JComboBox<>(new String[]{"Semua waktu", "Hari ini", "Minggu ini"});
    private final JComboBox<String> status = new JComboBox<>(new String[]{"Semua status", "Selesai", "Dibatalkan"});
    private final DefaultTableModel model = Ui.model("No. transaksi", "Waktu", "Pelanggan", "Total", "Metode", "Status");
    private final JTable table = Ui.table(model);
    private final DefaultTableModel itemModel = Ui.model("Produk", "Qty", "Jumlah", "Ket.");
    private final JTable itemTable = Ui.table(itemModel);
    private final JLabel lblId = Ui.label("Pilih transaksi", Font.BOLD, 15, Ui.TEXT);
    private final JLabel lblInfo = Ui.muted(" ");
    private final JLabel lblTotal = Ui.label(" ", Font.BOLD, 18, Ui.PRIMARY);
    private final JLabel lblMeta = Ui.muted(" ");
    private final JPanel badgeBox = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 0, 0));
    private final Ui.Btn btnReturn = Ui.plain("Ajukan Retur");
    private final Ui.Btn btnVoid = Ui.danger("Void Transaksi");
    private final Ui.Btn btnReceipt = Ui.plain("Lihat Struk");
    private List<Sale> shown = new ArrayList<>();

    HistoryPage(Nav nav) {
        super(nav);
        JPanel left = Ui.card();
        left.setLayout(new BorderLayout(0, 10));
        search.putClientProperty("JTextField.placeholderText", "Cari no. transaksi, tanggal, atau nama produk...");
        JPanel filters = Ui.transparent(new BorderLayout(8, 0));
        filters.add(search, BorderLayout.CENTER);
        JPanel combos = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        combos.add(period);
        combos.add(status);
        filters.add(combos, BorderLayout.EAST);
        left.add(filters, BorderLayout.NORTH);
        left.add(Ui.scroll(table), BorderLayout.CENTER);
        Ui.widths(table, 150, 150, 110, 100, 70, 100);
        add(left, BorderLayout.CENTER);

        JPanel right = Ui.card();
        right.setLayout(new BorderLayout(0, 10));
        right.setPreferredSize(new Dimension(420, 10));
        JPanel head = new JPanel(new GridLayout(0, 1, 0, 2));
        head.setOpaque(false);
        head.add(badgeBox);
        head.add(lblId);
        head.add(lblInfo);
        right.add(head, BorderLayout.NORTH);
        right.add(Ui.scroll(itemTable), BorderLayout.CENTER);
        Ui.widths(itemTable, 130, 50, 90, 120);
        JPanel foot = Ui.transparent(new BorderLayout(0, 8));
        JPanel tt = Ui.transparent(new GridLayout(0, 1, 0, 2));
        tt.add(lblTotal);
        tt.add(lblMeta);
        foot.add(tt, BorderLayout.NORTH);
        JPanel btns = Ui.transparent(new GridLayout(1, 3, 6, 0));
        Ui.compact(btnReturn);
        Ui.compact(btnVoid);
        Ui.compact(btnReceipt);
        btns.add(btnReturn);
        btns.add(btnVoid);
        btns.add(btnReceipt);
        foot.add(btns, BorderLayout.CENTER);
        foot.add(Ui.muted("<html><body style='width:380px'>VoidGuard: setelah dibayar wajib alasan dan password admin, hanya di hari yang sama. "
                + "Transaksi yang sudah diretur tidak dapat di-void.</html>"), BorderLayout.SOUTH);
        right.add(foot, BorderLayout.SOUTH);
        add(right, BorderLayout.EAST);

        search.getDocument().addDocumentListener(Ui.onChange(this::load));
        period.addActionListener(e -> load());
        status.addActionListener(e -> load());
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showDetail(); });
        btnReturn.addActionListener(e -> { Sale s = selected(); if (s != null) nav.goReturn(s.id); });
        btnVoid.addActionListener(e -> { Sale s = selected(); if (s != null) nav.goVoid(s.id); });
        btnReceipt.addActionListener(e -> { Sale s = selected(); if (s != null) ReceiptDialog.show(this, s, false); });
        load();
    }

    @Override String title() { return "Riwayat Transaksi"; }

    @Override String subtitle() { return "Cari transaksi lama, ajukan retur atau void"; }

    @Override void refresh() { load(); }

    private Sale selected() {
        int r = table.getSelectedRow();
        return r < 0 || r >= shown.size() ? null : shown.get(r);
    }

    private void load() {
        String q = search.getText().trim().toLowerCase();
        LocalDate today = LocalDate.now();
        shown = new ArrayList<>();
        for (Sale s : db.d.sales) {
            LocalDate d = s.time.toLocalDate();
            if (period.getSelectedIndex() == 1 && !d.equals(today)) continue;
            if (period.getSelectedIndex() == 2 && d.isBefore(today.minusDays(6))) continue;
            if (status.getSelectedIndex() == 1 && !"SELESAI".equals(s.status)) continue;
            if (status.getSelectedIndex() == 2 && !"DIBATALKAN".equals(s.status)) continue;
            if (!q.isEmpty()) {
                boolean hit = s.id.toLowerCase().contains(q) || s.time.format(Db.DF).toLowerCase().contains(q)
                        || (s.memberName != null && s.memberName.toLowerCase().contains(q));
                for (SaleItem it : s.items) if (it.name.toLowerCase().contains(q)) hit = true;
                if (!hit) continue;
            }
            shown.add(s);
        }
        shown.sort(Comparator.comparing((Sale s) -> s.time).reversed());
        model.setRowCount(0);
        for (Sale s : shown)
            model.addRow(new Object[]{s.id, s.time.format(Db.DTF), s.memberName == null ? "Umum" : s.memberName,
                    Ui.rp(s.total), s.method, s.status});
        if (!shown.isEmpty()) table.setRowSelectionInterval(0, 0);
        else showDetail();
    }

    private void showDetail() {
        Sale s = selected();
        itemModel.setRowCount(0);
        badgeBox.removeAll();
        boolean on = s != null;
        btnReturn.setEnabled(on && "SELESAI".equals(s.status));
        btnVoid.setEnabled(on && "SELESAI".equals(s.status));
        btnReceipt.setEnabled(on);
        if (!on) {
            lblId.setText("Pilih transaksi");
            lblInfo.setText(" ");
            lblTotal.setText(" ");
            lblMeta.setText(" ");
            badgeBox.revalidate();
            badgeBox.repaint();
            return;
        }
        Color c = Ui.statusColor(s.status);
        badgeBox.add(Ui.badge(s.status, c == null ? Ui.MUTED : c));
        lblId.setText(s.id);
        lblInfo.setText(s.time.format(Db.DTF) + "  |  " + s.cashier + "  |  " + (s.memberName == null ? "Umum" : s.memberName));
        for (SaleItem it : s.items) {
            String ket = it.source.isEmpty() ? "" : it.source + " " + it.discPct + "%";
            if (it.returned > 0) ket += (ket.isEmpty() ? "" : " ") + "(retur " + Ui.qty(it.returned) + ")";
            itemModel.addRow(new Object[]{it.name, Ui.qty(it.qty) + ("KG".equals(it.unit) ? " kg" : ""), Ui.rp(it.net), ket});
        }
        lblTotal.setText("Total " + Ui.rp(s.total));
        lblMeta.setText("Metode " + s.method + "  |  Poin diperoleh +" + s.pointsEarned
                + (s.pointsUsed > 0 ? "  |  Poin ditukar " + s.pointsUsed : "")
                + ("DIBATALKAN".equals(s.status) ? "  |  Void: " + s.voidReason : ""));
        badgeBox.revalidate();
        badgeBox.repaint();
    }

    /** Dipakai halaman lain untuk memilih transaksi tertentu. */
    void select(String id) {
        search.setText(id);
    }
}
