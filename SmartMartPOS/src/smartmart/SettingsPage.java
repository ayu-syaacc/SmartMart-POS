package smartmart;

import javax.swing.*;
import java.awt.*;

/** Pengaturan: parameter bisnis yang dipakai sistem. */
final class SettingsPage extends Page {
    private final JSpinner minStock = new JSpinner(new SpinnerNumberModel(10, 0, 9999, 1));
    private final JSpinner warn = new JSpinner(new SpinnerNumberModel(7, 2, 365, 1));
    private final JSpinner member = new JSpinner(new SpinnerNumberModel(10, 0, 100, 1));
    private final JSpinner perRp = new JSpinner(new SpinnerNumberModel(10000, 100, 10000000, 100));
    private final JSpinner unit = new JSpinner(new SpinnerNumberModel(100, 1, 100000, 1));
    private final JSpinner value = new JSpinner(new SpinnerNumberModel(5000, 100, 10000000, 100));
    private final JSpinner maxPct = new JSpinner(new SpinnerNumberModel(20, 0, 100, 1));
    private final JSpinner retDays = new JSpinner(new SpinnerNumberModel(3, 0, 365, 1));
    private final JTextField store = Ui.field(20), addr = Ui.field(20), phone = Ui.field(20);

    SettingsPage(Nav nav) {
        super(nav);
        JPanel cols = Ui.transparent(new GridLayout(1, 3, 12, 0));
        cols.add(group("Stok & Kedaluwarsa", new String[]{"Stok minimum default", "Mulai peringatan (hari)"},
                new JComponent[]{minStock, warn}, "Lot diblokir saat sisa 0 hari."));
        cols.add(group("Diskon & Poin", new String[]{"Diskon member (%)", "1 poin per Rp", "Tukar: jumlah poin", "Tukar: nilai (Rp)", "Maks. potongan (%)"}, new JComponent[]{member, perRp, unit, value, maxPct}, null));
        cols.add(group("Retur & Void", new String[]{"Batas waktu retur (hari)"}, new JComponent[]{retDays},
                "Void setelah bayar: hari yang sama. Persetujuan: password admin."));
        JPanel top = Ui.transparent(new BorderLayout(0, 12));
        top.add(cols, BorderLayout.NORTH);

        JPanel shop = Ui.card();
        shop.setLayout(new GridLayout(0, 2, 10, 8));
        shop.add(Ui.label("Struk / Toko", Font.BOLD, 14, Ui.TEXT));
        shop.add(new JLabel(" "));
        shop.add(new JLabel("Nama toko"));
        shop.add(store);
        shop.add(new JLabel("Alamat"));
        shop.add(addr);
        shop.add(new JLabel("Telepon"));
        shop.add(phone);
        JPanel mid = Ui.transparent(new BorderLayout());
        mid.add(shop, BorderLayout.NORTH);
        top.add(mid, BorderLayout.CENTER);
        add(top, BorderLayout.CENTER);

        JPanel bar = Ui.transparent(new BorderLayout());
        Ui.Btn save = Ui.primary("Simpan Pengaturan");
        Ui.Btn reset = Ui.danger("Reset Data Demo");
        save.addActionListener(e -> save());
        reset.addActionListener(e -> reset());
        bar.add(save, BorderLayout.WEST);
        bar.add(reset, BorderLayout.EAST);
        add(bar, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel group(String title, String[] labels, JComponent[] fields, String note) {
        JPanel c = Ui.card();
        c.setLayout(new BorderLayout(0, 8));
        c.add(Ui.label(title, Font.BOLD, 14, Ui.TEXT), BorderLayout.NORTH);
        JPanel g = Ui.transparent(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            g.add(new JLabel("<html>" + labels[i] + "</html>"));
            g.add(fields[i]);
        }
        JPanel wrap = Ui.transparent(new BorderLayout());
        wrap.add(g, BorderLayout.NORTH);
        c.add(wrap, BorderLayout.CENTER);
        if (note != null) c.add(Ui.muted("<html>" + note + "</html>"), BorderLayout.SOUTH);
        return c;
    }

    @Override String title() { return "Pengaturan"; }

    @Override String subtitle() { return "Parameter bisnis yang dipakai sistem"; }

    @Override void refresh() {
        Settings s = db.d.s;
        minStock.setValue(s.minStock);
        warn.setValue(s.warnDays);
        member.setValue(s.memberDisc);
        perRp.setValue(s.pointPerRp);
        unit.setValue(s.redeemUnit);
        value.setValue(s.redeemValue);
        maxPct.setValue(s.maxPointPct);
        retDays.setValue(s.returnDays);
        store.setText(s.storeName);
        addr.setText(s.address);
        phone.setText(s.phone);
    }

    private void save() {
        Settings s = db.d.s;
        s.minStock = (Integer) minStock.getValue();
        s.warnDays = (Integer) warn.getValue();
        s.memberDisc = (Integer) member.getValue();
        s.pointPerRp = (Integer) perRp.getValue();
        s.redeemUnit = (Integer) unit.getValue();
        s.redeemValue = (Integer) value.getValue();
        s.maxPointPct = (Integer) maxPct.getValue();
        s.returnDays = (Integer) retDays.getValue();
        s.storeName = store.getText().trim().isEmpty() ? "SMARTMART" : store.getText().trim();
        s.address = addr.getText().trim();
        s.phone = phone.getText().trim();
        db.log("Mengubah pengaturan sistem");
        Ui.info(this, "Pengaturan disimpan. Beberapa angka pada layar lain akan terbarui saat halaman dibuka ulang.");
    }

    private void reset() {
        if (!Ui.ask(this, "Reset seluruh data ke data demo awal? Semua transaksi, stok, dan akun akan diganti.")) return;
        User ap = Ui.askAdmin(this, "Reset Data Demo", "Tindakan ini tidak dapat dibatalkan.");
        if (ap == null) return;
        db.resetDemo();
        db.current = db.user(ap.username);
        Ui.info(this, "Data dikembalikan ke kondisi awal. Silakan login ulang bila akun Anda tidak dikenali.");
        nav.go("dash");
    }
}
