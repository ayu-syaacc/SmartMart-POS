package smartmart;

import javax.swing.*;
import java.awt.*;

/** VoidGuard: pembatalan transaksi terkontrol (alasan + password admin, hanya hari yang sama). */
final class VoidPage extends Page {
    private final JTextField idField = Ui.field(22);
    private final JTextField note = Ui.field(22);
    private Sale sale;
    private final JLabel lblSale = Ui.label("Belum ada transaksi dipilih", Font.BOLD, 15, Ui.TEXT);
    private final JLabel lblSub = Ui.muted(" ");
    private final JLabel lblTotal = Ui.label(" ", Font.BOLD, 20, Ui.PRIMARY);
    private final JLabel cDay = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel cRet = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel cStat = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblReject = Ui.label(" ", Font.PLAIN, 12, Ui.DANGER);
    private final JLabel iStock = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel iPts = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JRadioButton[] reasons = {new JRadioButton("Salah input", true), new JRadioButton("Pelanggan batal"),
            new JRadioButton("Salah produk"), new JRadioButton("Lainnya")};
    private final Ui.Btn btnVoid = Ui.danger("Ajukan Void");

    VoidPage(Nav nav) {
        super(nav);
        JPanel c = Ui.card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(left(Ui.label("Pembatalan transaksi yang terkontrol", Font.BOLD, 14, Ui.TEXT)));
        c.add(Box.createVerticalStrut(8));
        JPanel sr = Ui.transparent(new BorderLayout(8, 0));
        idField.putClientProperty("JTextField.placeholderText", "ID transaksi, contoh TRX-20261104-0017");
        Ui.Btn find = Ui.primary("Cari");
        sr.add(idField, BorderLayout.CENTER);
        sr.add(find, BorderLayout.EAST);
        sr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        c.add(left(sr));
        c.add(Box.createVerticalStrut(12));
        c.add(left(lblSale));
        c.add(left(lblSub));
        c.add(left(lblTotal));
        c.add(Box.createVerticalStrut(10));
        c.add(left(Ui.label("Syarat void setelah dibayar", Font.BOLD, 13, Ui.TEXT)));
        c.add(left(cDay));
        c.add(left(cRet));
        c.add(left(cStat));
        c.add(left(lblReject));
        c.add(Box.createVerticalStrut(10));
        c.add(left(Ui.label("Alasan void (wajib)", Font.BOLD, 13, Ui.TEXT)));
        ButtonGroup bg = new ButtonGroup();
        JPanel rp = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JRadioButton r : reasons) {
            r.setOpaque(false);
            bg.add(r);
            rp.add(r);
        }
        c.add(left(rp));
        note.putClientProperty("JTextField.placeholderText", "Catatan tambahan (opsional), mis. produk salah dipindai");
        note.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        c.add(Box.createVerticalStrut(4));
        c.add(left(note));
        c.add(Box.createVerticalStrut(6));
        c.add(left(Ui.muted("Tidak ada fitur edit: transaksi salah di-void lalu dibuat ulang.")));
        c.add(Box.createVerticalStrut(10));
        btnVoid.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(btnVoid);
        add(c, BorderLayout.CENTER);

        JPanel r = Ui.card();
        r.setLayout(new BoxLayout(r, BoxLayout.Y_AXIS));
        r.setPreferredSize(new Dimension(320, 10));
        r.add(left(Ui.label("Dampak void", Font.BOLD, 14, Ui.TEXT)));
        r.add(Box.createVerticalStrut(8));
        r.add(left(Ui.muted("Stok kembali")));
        r.add(left(iStock));
        r.add(Box.createVerticalStrut(6));
        r.add(left(Ui.muted("Poin disesuaikan")));
        r.add(left(iPts));
        r.add(Box.createVerticalStrut(6));
        r.add(left(Ui.muted("Tercatat di log")));
        r.add(left(Ui.label("LossGuard dan log aktivitas", Font.PLAIN, 13, Ui.TEXT)));
        r.add(Box.createVerticalStrut(14));
        r.add(left(Ui.label("Sebelum dibayar", Font.BOLD, 13, Ui.TEXT)));
        r.add(left(Ui.muted("<html>Cukup tekan <b>Kosongkan</b> di keranjang. Tanpa persetujuan admin.</html>")));
        r.add(Box.createVerticalStrut(14));
        r.add(left(Ui.label("Status setelah void", Font.BOLD, 13, Ui.TEXT)));
        r.add(left(Ui.badge("DIBATALKAN", Ui.DANGER)));
        add(r, BorderLayout.EAST);

        find.addActionListener(e -> search());
        idField.addActionListener(e -> search());
        btnVoid.addActionListener(e -> submit());
        search();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    @Override String title() { return "VoidGuard"; }

    @Override String subtitle() { return "Pembatalan transaksi yang terkontrol"; }

    void load(String id) {
        idField.setText(id);
        search();
    }

    private String mark(boolean ok, String text) { return (ok ? "[OK]  " : "[X]   ") + text; }

    private void search() {
        sale = db.sale(idField.getText());
        btnVoid.setEnabled(false);
        if (sale == null) {
            lblSale.setText(idField.getText().trim().isEmpty() ? "Belum ada transaksi dipilih" : "Transaksi tidak ditemukan");
            lblSub.setText(" ");
            lblTotal.setText(" ");
            cDay.setText("-");
            cRet.setText("-");
            cStat.setText("-");
            iStock.setText("-");
            iPts.setText("-");
            lblReject.setText(idField.getText().trim().isEmpty() ? " " : "Masukkan ID transaksi yang benar.");
            return;
        }
        boolean today = sale.time.toLocalDate().equals(java.time.LocalDate.now());
        lblSale.setText(sale.id + "   [" + sale.status + "]");
        lblSub.setText(sale.time.format(Db.DTF) + "  |  " + sale.cashier + "  |  "
                + (sale.memberName == null ? "Umum" : sale.memberName) + "  |  " + sale.items.size() + " item  |  " + sale.method);
        lblTotal.setText(Ui.rp(sale.total));
        cDay.setText(mark(today, "Hari yang sama (dibayar " + sale.time.format(Db.DTF) + ")"));
        cRet.setText(mark(!sale.hasReturn, sale.hasReturn ? "Sudah ada retur pada transaksi ini" : "Belum ada retur"));
        cStat.setText(mark("SELESAI".equals(sale.status), "Status transaksi " + sale.status));
        String blk = db.voidBlock(sale);
        lblReject.setText(blk == null ? " " : "DITOLAK: " + blk);
        btnVoid.setEnabled(blk == null);
        iStock.setText("Ke lot asal masing-masing barang (" + sale.items.size() + " baris)");
        iPts.setText(sale.memberNo == null ? "Tanpa member"
                : "Poin +" + sale.pointsEarned + " dibatalkan" + (sale.pointsUsed > 0 ? ", " + sale.pointsUsed + " poin tukar dikembalikan" : ""));
    }

    private void submit() {
        if (sale == null) return;
        String reason = "Salah input";
        for (JRadioButton r : reasons) if (r.isSelected()) reason = r.getText();
        if (!note.getText().trim().isEmpty()) reason += ": " + note.getText().trim();
        User ap = Ui.askAdmin(this, "Konfirmasi Void",
                sale.id + " | " + Ui.rp(sale.total) + "<br>Stok kembali dan poin disesuaikan. Tindakan ini tercatat di log.");
        if (ap == null) return;
        try {
            db.voidSale(sale, reason, nav.user(), ap);
            Ui.info(this, "Transaksi " + sale.id + " dibatalkan (disetujui " + ap.name + ").");
            note.setText("");
            search();
        } catch (IllegalArgumentException ex) {
            Ui.err(this, ex.getMessage());
        }
    }
}
