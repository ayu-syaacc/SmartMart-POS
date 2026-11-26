package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** ReceiveCheck: penerimaan barang dari supplier (setiap penerimaan menjadi satu lot). */
final class ReceivePage extends Page {
    private final JComboBox<String> supplier = new JComboBox<>(new String[]{"PT Segar Makmur", "CV Sumber Pangan", "PT Dairy Nusantara"});
    private final JTextField po = Ui.field(14);
    private final JTextField date = Ui.field(10);
    private final JLabel lblBy = Ui.label("-", Font.BOLD, 13, Ui.TEXT);
    private boolean busy;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Produk", "Dipesan", "Diterima", "Rusak", "Selisih", "Kedaluwarsa (yyyy-mm-dd)", "Masuk lot"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return c != 4 && c != 6; }
    };
    private final JTable table = Ui.table(model);
    private final JLabel sOrdered = Ui.label("0", Font.BOLD, 13, Ui.TEXT);
    private final JLabel sRecv = Ui.label("0", Font.BOLD, 13, Ui.TEXT);
    private final JLabel sDamage = Ui.label("0", Font.BOLD, 13, Ui.DANGER);
    private final JLabel sDiff = Ui.label("0", Font.BOLD, 13, Ui.WARN);
    private final JLabel sLots = Ui.label("0", Font.BOLD, 13, Ui.TEXT);
    private final JLabel sAdded = Ui.label("+0 unit", Font.BOLD, 18, Ui.OK);
    private final JLabel rules = Ui.muted(" ");
    private final DefaultTableModel histModel = Ui.model("No. pesanan", "Tanggal", "Supplier", "Lot");
    private final JComboBox<String> productBox = new JComboBox<>();

    ReceivePage(Nav nav) {
        super(nav);
        JPanel left = Ui.card();
        left.setLayout(new BorderLayout(0, 10));
        JPanel form = Ui.transparent(new GridLayout(2, 4, 10, 2));
        supplier.setEditable(true);
        form.add(Ui.muted("Supplier"));
        form.add(Ui.muted("No. pesanan"));
        form.add(Ui.muted("Tanggal terima (yyyy-mm-dd)"));
        form.add(Ui.muted("Diterima oleh"));
        form.add(supplier);
        form.add(po);
        form.add(date);
        form.add(lblBy);
        left.add(form, BorderLayout.NORTH);

        JPanel mid = Ui.transparent(new BorderLayout(0, 6));
        mid.add(Ui.label("Pemeriksaan barang", Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(productBox));
        Ui.widths(table, 190, 70, 70, 60, 60, 170, 80);
        mid.add(Ui.scroll(table), BorderLayout.CENTER);
        mid.add(Ui.muted("<html><body style='width:520px'>Barang rusak saat diterima tidak masuk lot dan dicatat di LossGuard. Selisih = dipesan - diterima, "
                + "tercatat di laporan. Masuk lot = diterima - rusak.</html>"), BorderLayout.SOUTH);
        left.add(mid, BorderLayout.CENTER);
        JPanel btns = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        Ui.Btn addRow = Ui.plain("Tambah Baris");
        Ui.Btn delRow = Ui.plain("Hapus Baris");
        Ui.Btn save = Ui.primary("Simpan Penerimaan");
        btns.add(addRow);
        btns.add(delRow);
        btns.add(save);
        left.add(btns, BorderLayout.SOUTH);
        add(left, BorderLayout.CENTER);

        JPanel right = Ui.card();
        right.setLayout(new BorderLayout(0, 10));
        right.setPreferredSize(new Dimension(300, 10));
        JPanel sum = new JPanel(new GridLayout(0, 2, 6, 6));
        sum.setOpaque(false);
        sum.add(Ui.muted("Total dipesan"));
        sum.add(sOrdered);
        sum.add(Ui.muted("Total diterima"));
        sum.add(sRecv);
        sum.add(Ui.muted("Total rusak"));
        sum.add(sDamage);
        sum.add(Ui.muted("Total selisih"));
        sum.add(sDiff);
        sum.add(Ui.muted("Lot dibuat"));
        sum.add(sLots);
        sum.add(Ui.muted("Stok bertambah"));
        sum.add(sAdded);
        JPanel top = Ui.transparent(new BorderLayout(0, 8));
        top.add(Ui.label("Ringkasan penerimaan", Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        top.add(sum, BorderLayout.CENTER);
        top.add(rules, BorderLayout.SOUTH);
        right.add(top, BorderLayout.NORTH);
        JPanel hist = Ui.transparent(new BorderLayout(0, 4));
        hist.add(Ui.label("Penerimaan terakhir", Font.BOLD, 13, Ui.TEXT), BorderLayout.NORTH);
        JTable ht = Ui.table(histModel);
        Ui.widths(ht, 100, 90, 120, 40);
        hist.add(Ui.scroll(ht), BorderLayout.CENTER);
        right.add(hist, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);

        model.addTableModelListener(e -> recalc());
        addRow.addActionListener(e -> addRow());
        delRow.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) model.removeRow(r);
            recalc();
        });
        save.addActionListener(e -> save());
        resetForm();
    }

    @Override String title() { return "ReceiveCheck"; }

    @Override String subtitle() { return "Penerimaan barang dari supplier (setiap penerimaan menjadi satu lot)"; }

    @Override void refresh() {
        productBox.removeAllItems();
        for (Product p : db.d.products) productBox.addItem(p.name);
        lblBy.setText(nav.user() == null ? "-" : nav.user().name);
        StringBuilder sb = new StringBuilder("<html><b>Aturan FreshSave</b><br>");
        for (int i = 2; i >= 0; i--) sb.append("Sisa ").append(db.d.s.fsDays[i]).append(" hari atau kurang: ").append(db.d.s.fsPct[i]).append("%<br>");
        rules.setText(sb.append("Persentase dapat diatur admin</html>").toString());
        histModel.setRowCount(0);
        for (int i = db.d.recvs.size() - 1; i >= Math.max(0, db.d.recvs.size() - 6); i--) {
            Recv r = db.d.recvs.get(i);
            histModel.addRow(new Object[]{r.po, r.date.format(Db.DF), r.supplier, r.lines.size()});
        }
    }

    private void resetForm() {
        model.setRowCount(0);
        po.setText("PO-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM")) + "-" + String.format("%03d", db.d.recvSeq + 1));
        date.setText(LocalDate.now().toString());
        refresh();
        addRow();
    }

    private void addRow() {
        if (productBox.getItemCount() == 0) return;
        model.addRow(new Object[]{productBox.getItemAt(0), "0", "0", "0", "0", LocalDate.now().plusDays(30).toString(), "0"});
    }

    private static double num(Object o) {
        double v = Ui.parseQty(String.valueOf(o));
        return v < 0 ? 0 : v;
    }

    private void recalc() {
        if (busy) return;
        busy = true;
        double o = 0, r = 0, d = 0, diff = 0, in = 0;
        int lots = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            double ord = num(model.getValueAt(i, 1)), rec = num(model.getValueAt(i, 2)), dam = num(model.getValueAt(i, 3));
            double sel = ord - rec, masuk = Math.max(0, rec - dam);
            model.setValueAt(Ui.qty(sel), i, 4);
            model.setValueAt(Ui.qty(masuk), i, 6);
            o += ord;
            r += rec;
            d += dam;
            if (sel > 0) diff += sel;
            in += masuk;
            if (masuk > 0) lots++;
        }
        sOrdered.setText(Ui.qty(o));
        sRecv.setText(Ui.qty(r));
        sDamage.setText(Ui.qty(d));
        sDiff.setText(Ui.qty(diff));
        sLots.setText(String.valueOf(lots));
        sAdded.setText("+" + Ui.qty(in) + " unit");
        busy = false;
    }

    private void save() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        String sup = String.valueOf(supplier.getEditor().getItem()).trim();
        if (sup.isEmpty() || po.getText().trim().isEmpty()) { Ui.err(this, "Supplier dan nomor pesanan wajib diisi."); return; }
        LocalDate dt;
        try { dt = LocalDate.parse(date.getText().trim()); } catch (Exception e) { Ui.err(this, "Format tanggal terima harus yyyy-mm-dd."); return; }
        List<RecvLine> lines = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String pname = String.valueOf(model.getValueAt(i, 0));
            Product p = null;
            for (Product x : db.d.products) if (x.name.equals(pname)) p = x;
            if (p == null) { Ui.err(this, "Baris " + (i + 1) + ": produk tidak valid."); return; }
            RecvLine ln = new RecvLine();
            ln.productCode = p.code;
            ln.productName = p.name;
            ln.ordered = num(model.getValueAt(i, 1));
            ln.received = num(model.getValueAt(i, 2));
            ln.damaged = num(model.getValueAt(i, 3));
            try { ln.expiry = LocalDate.parse(String.valueOf(model.getValueAt(i, 5)).trim()); }
            catch (Exception e) { Ui.err(this, "Baris " + (i + 1) + ": tanggal kedaluwarsa harus yyyy-mm-dd."); return; }
            if (ln.received <= 0 && ln.ordered <= 0) { Ui.err(this, "Baris " + (i + 1) + ": isi jumlah dipesan / diterima."); return; }
            lines.add(ln);
        }
        try {
            Recv r = db.receive(sup, po.getText().trim(), dt, nav.user(), lines);
            int lots = 0;
            for (RecvLine l : r.lines) if (l.lotId != null) lots++;
            Ui.info(this, "Penerimaan " + r.po + " tersimpan. " + lots + " lot dibuat, selisih senilai " + Ui.rp(r.discrepancyValue) + ".");
            resetForm();
        } catch (IllegalArgumentException ex) {
            Ui.err(this, ex.getMessage());
        }
    }
}
