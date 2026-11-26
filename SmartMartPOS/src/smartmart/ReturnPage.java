package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.LocalDate;

/** SmartReturn: pengembalian barang dengan validasi otomatis dan persetujuan admin. */
final class ReturnPage extends Page {
    private final JTextField idField = Ui.field(22);
    private Sale sale;
    private boolean saleOk;
    private final boolean[] allowed = new boolean[64];
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Produk", "Dibeli", "Sudah retur", "Retur", "Status"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return c == 3 && r < allowed.length && allowed[r]; }
    };
    private final JTable table = Ui.table(model);
    private final JLabel lblResult = Ui.label(" ", Font.BOLD, 14, Ui.TEXT);
    private final JLabel lblSub = Ui.muted(" ");
    private final JLabel lblAge = Ui.label("Masa retur: -", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel lblFound = Ui.label("Struk / ID transaksi: -", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel lblVoid = Ui.label("Status void: -", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel lblReject = Ui.label(" ", Font.PLAIN, 12, Ui.DANGER);
    private final JRadioButton[] reasons = {new JRadioButton("Salah beli", true), new JRadioButton("Kemasan rusak"), new JRadioButton("Lainnya")};
    private final JCheckBox c1 = new JCheckBox("Belum digunakan", true);
    private final JCheckBox c2 = new JCheckBox("Kemasan utuh", true);
    private final JCheckBox c3 = new JCheckBox("Layak jual kembali", true);
    private final JLabel lblItems = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblPts = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblRefund = Ui.label("Rp0", Font.BOLD, 20, Ui.PRIMARY);
    private final Ui.Btn btnSubmit = Ui.primary("Ajukan Retur");

    ReturnPage(Nav nav) {
        super(nav);
        JPanel left = Ui.card();
        left.setLayout(new BorderLayout(0, 10));
        JPanel top = Ui.transparent(new GridLayout(0, 1, 0, 6));
        top.add(Ui.label("Langkah 1: cari transaksi asli", Font.BOLD, 14, Ui.TEXT));
        JPanel sr = Ui.transparent(new BorderLayout(8, 0));
        idField.putClientProperty("JTextField.placeholderText", "ID transaksi, contoh TRX-20261103-0042");
        Ui.Btn find = Ui.primary("Cari");
        sr.add(idField, BorderLayout.CENTER);
        sr.add(find, BorderLayout.EAST);
        top.add(sr);
        top.add(lblResult);
        top.add(lblSub);
        JPanel val = Ui.transparent(new GridLayout(1, 3, 8, 0));
        val.add(lblAge);
        val.add(lblFound);
        val.add(lblVoid);
        top.add(val);
        top.add(lblReject);
        left.add(top, BorderLayout.NORTH);
        left.add(Ui.scroll(table), BorderLayout.CENTER);
        Ui.widths(table, 170, 70, 80, 70, 220);
        left.add(Ui.muted("<html><body style='width:560px'>Aturan otomatis: maksimal " + db.d.s.returnDays + " hari, barang segar/cepat basi dan barang diskon FreshSave tidak dapat "
                + "diretur, jumlah retur tidak boleh melebihi pembelian, wajib ID transaksi. Isi kolom <b>Retur</b> dengan jumlah yang diretur.</html>"),
                BorderLayout.SOUTH);
        add(left, BorderLayout.CENTER);

        JPanel right = Ui.card();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(330, 10));
        right.add(left(Ui.label("Detail retur", Font.BOLD, 14, Ui.TEXT)));
        right.add(Box.createVerticalStrut(8));
        right.add(left(Ui.muted("Alasan")));
        ButtonGroup bg = new ButtonGroup();
        for (JRadioButton r : reasons) {
            r.setOpaque(false);
            bg.add(r);
            right.add(left(r));
        }
        right.add(Box.createVerticalStrut(8));
        right.add(left(Ui.muted("Kondisi barang")));
        for (JCheckBox c : new JCheckBox[]{c1, c2, c3}) {
            c.setOpaque(false);
            right.add(left(c));
        }
        right.add(left(Ui.muted("<html>Stok kembali ke lot asal bila <b>Layak jual kembali</b> dicentang; bila tidak, dicatat sebagai RUSAK.</html>")));
        right.add(Box.createVerticalStrut(12));
        right.add(left(Ui.label("Ringkasan refund", Font.BOLD, 14, Ui.TEXT)));
        right.add(left(lblItems));
        right.add(left(Ui.muted("Metode: Tunai")));
        right.add(left(lblPts));
        right.add(Box.createVerticalStrut(4));
        right.add(left(lblRefund));
        right.add(Box.createVerticalStrut(12));
        JPanel btns = Ui.transparent(new GridLayout(1, 2, 8, 0));
        Ui.Btn cancel = Ui.plain("Batal");
        btns.add(cancel);
        btns.add(btnSubmit);
        right.add(left(btns));
        right.add(Box.createVerticalGlue());
        right.add(left(Ui.muted("<html>Retur diproses setelah admin memasukkan password.</html>")));
        add(right, BorderLayout.EAST);

        find.addActionListener(e -> search());
        idField.addActionListener(e -> search());
        cancel.addActionListener(e -> clear());
        btnSubmit.addActionListener(e -> submit());
        model.addTableModelListener(e -> summary());
        clear();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    @Override String title() { return "SmartReturn"; }

    @Override String subtitle() { return "Pengembalian barang dengan validasi otomatis"; }

    void load(String id) {
        idField.setText(id);
        search();
    }

    private void clear() {
        sale = null;
        saleOk = false;
        idField.setText("");
        model.setRowCount(0);
        lblResult.setText("Belum ada transaksi dipilih");
        lblSub.setText(" ");
        lblAge.setText("Masa retur: -");
        lblFound.setText("Struk / ID transaksi: -");
        lblVoid.setText("Status void: -");
        lblReject.setText(" ");
        summary();
    }

    private void search() {
        String id = idField.getText().trim();
        sale = db.sale(id);
        model.setRowCount(0);
        java.util.Arrays.fill(allowed, false);
        if (sale == null) {
            lblResult.setText("Tidak ditemukan");
            lblSub.setText(" ");
            lblAge.setText("Masa retur: -");
            lblFound.setText("Struk / ID transaksi: tidak ditemukan");
            lblVoid.setText("Status void: -");
            lblReject.setText(id.isEmpty() ? "Masukkan ID transaksi asli untuk melanjutkan." : "Transaksi " + id + " tidak ditemukan.");
            saleOk = false;
            summary();
            return;
        }
        String blk = db.saleReturnBlock(sale);
        saleOk = blk == null;
        long age = ChronoUnit.DAYS.between(sale.time.toLocalDate(), LocalDate.now());
        lblResult.setText(sale.id + "   " + Ui.rp(sale.total));
        lblSub.setText(sale.time.format(Db.DTF) + "  |  " + sale.cashier + "  |  "
                + (sale.memberName == null ? "Umum" : sale.memberName) + "  |  " + sale.method + "  |  " + sale.status);
        lblAge.setText("Masa retur: hari ke-" + age + " dari maks. " + db.d.s.returnDays);
        lblFound.setText("Struk / ID transaksi: ditemukan");
        lblVoid.setText("DIBATALKAN".equals(sale.status) ? "Status void: sudah di-void" : "Status void: belum pernah di-void");
        lblReject.setText(blk == null ? " " : "DITOLAK: " + blk);
        for (int i = 0; i < sale.items.size(); i++) {
            SaleItem it = sale.items.get(i);
            String ib = saleOk ? db.itemReturnBlock(it) : "Tidak dapat diretur";
            allowed[i] = ib == null;
            if (ib != null && ib.contains("FreshSave")) ib = "Diskon FreshSave";
            model.addRow(new Object[]{it.name, Ui.qty(it.qty) + ("KG".equals(it.unit) ? " kg" : ""),
                    it.returned > 0 ? Ui.qty(it.returned) : "-", allowed[i] ? "0" : "-", ib == null ? "Dapat diretur" : ib});
        }
        summary();
    }

    private Map<Integer, Double> chosen() {
        Map<Integer, Double> q = new LinkedHashMap<>();
        if (sale == null) return q;
        for (int r = 0; r < model.getRowCount() && r < sale.items.size(); r++) {
            if (!allowed[r]) continue;
            double v = Ui.parseQty(String.valueOf(model.getValueAt(r, 3)));
            if (v > 0) q.put(r, v);
        }
        return q;
    }

    private void summary() {
        if (sale == null) {
            lblItems.setText("-");
            lblPts.setText("-");
            lblRefund.setText("Rp0");
            btnSubmit.setEnabled(false);
            return;
        }
        long refund = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Double> e : chosen().entrySet()) {
            SaleItem it = sale.items.get(e.getKey());
            refund += Db.itemRefund(it, e.getValue());
            if (sb.length() > 0) sb.append(", ");
            sb.append(Ui.qty(e.getValue())).append(" x ").append(it.name);
        }
        lblItems.setText(sb.length() == 0 ? "Barang: -" : "<html>Barang: " + sb + "</html>");
        lblPts.setText(sale.memberNo == null ? "Poin member: -" : "Poin member: -" + (refund / db.d.s.pointPerRp) + " poin");
        lblRefund.setText(Ui.rp(refund));
        btnSubmit.setEnabled(saleOk && refund > 0);
    }

    private void submit() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        Map<Integer, Double> q = chosen();
        if (q.isEmpty()) { Ui.err(this, "Isi jumlah retur pada minimal satu barang."); return; }
        for (Map.Entry<Integer, Double> e : q.entrySet()) {
            SaleItem it = sale.items.get(e.getKey());
            double left = Db.r3(it.qty - it.returned);
            if (e.getValue() > left + 1e-9) {
                Ui.err(this, "Jumlah melebihi pembelian. Dibeli " + Ui.qty(it.qty) + ", sudah diretur " + Ui.qty(it.returned)
                        + ", sisa maks. " + Ui.qty(left) + " (" + it.name + ").");
                return;
            }
        }
        String reason = "Salah beli";
        for (JRadioButton r : reasons) if (r.isSelected()) reason = r.getText();
        StringBuilder cond = new StringBuilder();
        for (JCheckBox c : new JCheckBox[]{c1, c2, c3}) if (c.isSelected()) cond.append(cond.length() > 0 ? ", " : "").append(c.getText());
        long refund = 0;
        for (Map.Entry<Integer, Double> e : q.entrySet()) refund += Db.itemRefund(sale.items.get(e.getKey()), e.getValue());
        User ap = Ui.askAdmin(this, "Retur " + sale.id, "Refund " + Ui.rp(refund) + " (tunai). Dialog ini juga dipakai untuk void transaksi.");
        if (ap == null) return;
        try {
            long[] res = db.processReturn(sale, q, reason, cond.length() == 0 ? "-" : cond.toString(), c3.isSelected(), nav.user(), ap);
            Ui.info(this, "Retur disetujui oleh " + ap.name + ".\nRefund tunai: " + Ui.rp(res[0])
                    + (sale.memberNo != null ? "\nPoin member dikurangi: " + res[1] : ""));
            search();
        } catch (IllegalArgumentException ex) {
            Ui.err(this, ex.getMessage());
        }
    }
}
