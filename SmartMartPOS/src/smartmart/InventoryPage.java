package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Persediaan: data produk, harga, stok minimum, aturan retur. */
final class InventoryPage extends Page {
    private final JTextField search = Ui.field(24);
    private final DefaultTableModel model = Ui.model("Kode", "Nama produk", "Kategori", "Harga", "Satuan", "Stok", "Min.", "Retur", "Status lot");
    private final JTable table = Ui.table(model);
    private List<Product> shown = new ArrayList<>();

    InventoryPage(Nav nav) {
        super(nav);
        JPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 10));
        JPanel top = Ui.transparent(new BorderLayout(8, 0));
        search.putClientProperty("JTextField.placeholderText", "Cari kode atau nama produk...");
        top.add(search, BorderLayout.CENTER);
        JPanel btns = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        Ui.Btn add = Ui.primary("Tambah Produk");
        Ui.Btn edit = Ui.plain("Ubah Produk");
        Ui.Btn adj = Ui.plain("Penyesuaian Stok");
        btns.add(add);
        btns.add(edit);
        btns.add(adj);
        top.add(btns, BorderLayout.EAST);
        c.add(top, BorderLayout.NORTH);
        c.add(Ui.scroll(table), BorderLayout.CENTER);
        Ui.widths(table, 100, 190, 80, 90, 60, 70, 50, 100, 110);
        c.add(Ui.muted("Stok produk = jumlah sisa seluruh lot. Harga per kg dihitung berat x harga, dibulatkan ke rupiah."), BorderLayout.SOUTH);
        add(c, BorderLayout.CENTER);
        search.getDocument().addDocumentListener(Ui.onChange(this::load));
        add.addActionListener(e -> edit(null));
        edit.addActionListener(e -> { Product p = selected(); if (p != null) edit(p); });
        adj.addActionListener(e -> nav.go("stock"));
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { Product p = selected(); if (p != null) edit(p); }
            }
        });
        load();
    }

    @Override String title() { return "Persediaan"; }

    @Override String subtitle() { return "Data produk, harga, stok minimum, dan aturan retur"; }

    @Override void refresh() { load(); }

    private Product selected() {
        int r = table.getSelectedRow();
        return r < 0 || r >= shown.size() ? null : shown.get(r);
    }

    private void load() {
        String q = search.getText().trim().toLowerCase();
        shown = new ArrayList<>();
        model.setRowCount(0);
        for (Product p : db.d.products) {
            if (!q.isEmpty() && !p.name.toLowerCase().contains(q) && !p.code.contains(q)) continue;
            shown.add(p);
            model.addRow(new Object[]{p.code, p.name, p.category, Ui.rp(p.price), p.unit, Ui.qty(db.stock(p.code)), p.minStock,
                    p.returnable ? "Dapat diretur" : "Tidak", db.productStatus(p)});
        }
    }

    private void edit(Product p) {
        boolean isNew = p == null;
        JTextField code = Ui.field(14), name = Ui.field(20), price = Ui.field(10), min = Ui.field(6);
        JComboBox<String> cat = new JComboBox<>(new String[]{"Segar", "Susu", "Makanan", "Minuman", "Lainnya"});
        JComboBox<String> unit = new JComboBox<>(new String[]{"PCS", "KG"});
        JCheckBox ret = new JCheckBox("Dapat diretur", true);
        if (isNew) min.setText(String.valueOf(db.d.s.minStock));
        else {
            code.setText(p.code);
            code.setEditable(false);
            name.setText(p.name);
            price.setText(String.valueOf(p.price));
            min.setText(String.valueOf(p.minStock));
            cat.setSelectedItem(p.category);
            unit.setSelectedItem(p.unit);
            ret.setSelected(p.returnable);
        }
        JPanel f = new JPanel(new GridLayout(0, 2, 8, 8));
        f.add(new JLabel("Kode / barcode"));
        f.add(code);
        f.add(new JLabel("Nama produk"));
        f.add(name);
        f.add(new JLabel("Kategori"));
        f.add(cat);
        f.add(new JLabel("Harga (Rp)"));
        f.add(price);
        f.add(new JLabel("Satuan"));
        f.add(unit);
        f.add(new JLabel("Stok minimum"));
        f.add(min);
        f.add(new JLabel("Aturan retur"));
        f.add(ret);
        if (JOptionPane.showConfirmDialog(this, f, isNew ? "Tambah Produk" : "Ubah Produk", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        String cd = code.getText().trim();
        long pr = Ui.parseMoney(price.getText());
        long mn = Ui.parseMoney(min.getText());
        if (cd.isEmpty() || name.getText().trim().isEmpty() || pr <= 0) { Ui.err(this, "Kode, nama, dan harga wajib diisi dengan benar."); return; }
        if (isNew && db.product(cd) != null) { Ui.err(this, "Kode produk sudah ada."); return; }
        if (isNew) {
            p = new Product();
            p.code = cd;
            db.d.products.add(p);
        }
        p.name = name.getText().trim();
        p.category = (String) cat.getSelectedItem();
        p.price = pr;
        p.unit = (String) unit.getSelectedItem();
        p.minStock = (int) mn;
        p.returnable = ret.isSelected();
        db.log((isNew ? "Menambah produk " : "Mengubah produk ") + p.code + " " + p.name);
        load();
    }
}
