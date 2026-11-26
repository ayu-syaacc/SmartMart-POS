package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Member: pencarian, pendaftaran, riwayat belanja, poin dan penukaran. */
final class MemberPage extends Page {
    private final JTextField search = Ui.field(18);
    private final DefaultTableModel listModel = Ui.model("Nama", "No. member", "Poin");
    private final JTable listTable = Ui.table(listModel);
    private final DefaultTableModel buyModel = Ui.model("Transaksi", "Tanggal", "Total", "Poin");
    private final DefaultTableModel ptModel = Ui.model("Tanggal", "Keterangan", "Poin");
    private final JLabel lblName = Ui.label("Pilih member", Font.BOLD, 18, Ui.TEXT);
    private final JLabel lblSub = Ui.muted(" ");
    private final JLabel lblDisc = Ui.label("-", Font.BOLD, 14, Ui.PRIMARY);
    private final JLabel lblPts = Ui.label("-", Font.BOLD, 14, Ui.PRIMARY);
    private final JLabel lblRedeem = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblMax = Ui.label("-", Font.PLAIN, 13, Ui.TEXT);
    private final JLabel lblSpent = Ui.label("-", Font.BOLD, 14, Ui.TEXT);
    private final Ui.Btn btnUse = Ui.primary("Pilih untuk Transaksi");
    private final Ui.Btn btnEdit = Ui.plain("Ubah Data");
    private List<Member> shown = new ArrayList<>();

    MemberPage(Nav nav) {
        super(nav);
        JPanel left = Ui.card();
        left.setLayout(new BorderLayout(0, 10));
        left.setPreferredSize(new Dimension(380, 10));
        search.putClientProperty("JTextField.placeholderText", "Cari no. member atau nama...");
        left.add(search, BorderLayout.NORTH);
        left.add(Ui.scroll(listTable), BorderLayout.CENTER);
        Ui.Btn add = Ui.primary("Daftarkan Member Baru");
        left.add(add, BorderLayout.SOUTH);
        add(left, BorderLayout.WEST);

        JPanel right = Ui.card();
        right.setLayout(new BorderLayout(0, 10));
        JPanel head = Ui.transparent(new GridLayout(0, 1, 0, 2));
        head.add(lblName);
        head.add(lblSub);
        JPanel stats = Ui.transparent(new GridLayout(2, 3, 12, 6));
        stats.add(box("Diskon member", lblDisc));
        stats.add(box("Poin tersedia", lblPts));
        stats.add(box("Total belanja", lblSpent));
        stats.add(box("Tukar poin", lblRedeem));
        stats.add(box("Maks. ditukar sekarang", lblMax));
        stats.add(box("Potongan maks.", Ui.label(db.d.s.maxPointPct + "% dari subtotal", Font.PLAIN, 13, Ui.TEXT)));
        JPanel top = Ui.transparent(new BorderLayout(0, 10));
        top.add(head, BorderLayout.NORTH);
        top.add(stats, BorderLayout.CENTER);
        right.add(top, BorderLayout.NORTH);

        JPanel tables = Ui.transparent(new GridLayout(2, 1, 0, 10));
        JPanel t1 = Ui.transparent(new BorderLayout(0, 4));
        t1.add(Ui.label("Riwayat belanja", Font.BOLD, 13, Ui.TEXT), BorderLayout.NORTH);
        t1.add(Ui.scroll(Ui.table(buyModel)), BorderLayout.CENTER);
        JPanel t2 = Ui.transparent(new BorderLayout(0, 4));
        t2.add(Ui.label("Riwayat poin", Font.BOLD, 13, Ui.TEXT), BorderLayout.NORTH);
        t2.add(Ui.scroll(Ui.table(ptModel)), BorderLayout.CENTER);
        tables.add(t1);
        tables.add(t2);
        right.add(tables, BorderLayout.CENTER);
        JPanel btns = Ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.add(btnUse);
        btns.add(btnEdit);
        right.add(btns, BorderLayout.SOUTH);
        add(right, BorderLayout.CENTER);

        search.getDocument().addDocumentListener(Ui.onChange(this::load));
        listTable.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) detail(); });
        add.addActionListener(e -> register());
        btnEdit.addActionListener(e -> edit());
        btnUse.addActionListener(e -> {
            Member m = selected();
            if (m != null) { nav.setMember(m); nav.go("pos"); }
        });
        load();
    }

    private JPanel box(String title, JLabel value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(new Color(0xF9FAFB));
        p.setBorder(BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(Ui.BORDER, 1, true),
                new javax.swing.border.EmptyBorder(6, 10, 6, 10)));
        p.add(Ui.muted(title));
        p.add(value);
        return p;
    }

    @Override String title() { return "Member"; }

    @Override String subtitle() { return "Pelanggan terdaftar: diskon member dan poin otomatis"; }

    @Override void refresh() { load(); }

    private Member selected() {
        int r = listTable.getSelectedRow();
        return r < 0 || r >= shown.size() ? null : shown.get(r);
    }

    private void load() {
        Member keep = selected();
        shown = db.searchMembers(search.getText());
        listModel.setRowCount(0);
        for (Member m : shown) listModel.addRow(new Object[]{m.name, m.no, m.points + " poin"});
        int idx = keep == null ? 0 : Math.max(0, shown.indexOf(keep));
        if (!shown.isEmpty()) listTable.setRowSelectionInterval(idx, idx);
        else detail();
    }

    private void detail() {
        Member m = selected();
        buyModel.setRowCount(0);
        ptModel.setRowCount(0);
        btnUse.setEnabled(m != null);
        btnEdit.setEnabled(m != null);
        if (m == null) {
            lblName.setText("Pilih member");
            lblSub.setText(" ");
            return;
        }
        lblName.setText(m.name);
        lblSub.setText(m.no + "  |  Anggota sejak " + m.since.format(Db.DF));
        lblDisc.setText(db.d.s.memberDisc + "%");
        lblPts.setText(m.points + " poin");
        lblRedeem.setText(db.d.s.redeemUnit + " poin = " + Ui.rp(db.d.s.redeemValue));
        int maxPts = (m.points / db.d.s.redeemUnit) * db.d.s.redeemUnit;
        lblMax.setText(maxPts + " poin = " + Ui.rp((long) maxPts / db.d.s.redeemUnit * db.d.s.redeemValue));
        lblSpent.setText(Ui.rp(db.memberSpent(m)));
        List<Sale> sales = new ArrayList<>();
        for (Sale s : db.d.sales) if (m.no.equals(s.memberNo)) sales.add(s);
        sales.sort((a, b) -> b.time.compareTo(a.time));
        for (Sale s : sales)
            buyModel.addRow(new Object[]{s.id, s.time.format(Db.DF), Ui.rp(s.total), "DIBATALKAN".equals(s.status) ? "batal" : "+" + s.pointsEarned});
        List<PointTx> pts = new ArrayList<>();
        for (PointTx p : db.d.pts) if (m.no.equals(p.memberNo)) pts.add(p);
        for (int i = pts.size() - 1; i >= 0; i--) {
            PointTx p = pts.get(i);
            ptModel.addRow(new Object[]{p.date.format(Db.DF), p.desc, (p.points > 0 ? "+" : "") + p.points});
        }
    }

    private void register() {
        String name = JOptionPane.showInputDialog(this, "Nama lengkap member baru:", "Daftarkan Member Baru", JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) { Ui.err(this, "Nama tidak boleh kosong."); return; }
        Member m = db.addMember(name);
        search.setText("");
        load();
        listTable.setRowSelectionInterval(Math.max(0, shown.indexOf(m)), Math.max(0, shown.indexOf(m)));
        Ui.info(this, "Member terdaftar: " + m.name + " (" + m.no + ").");
    }

    private void edit() {
        Member m = selected();
        if (m == null) return;
        String name = (String) JOptionPane.showInputDialog(this, "Ubah nama member " + m.no + ":", "Ubah Data",
                JOptionPane.PLAIN_MESSAGE, null, null, m.name);
        if (name == null || name.trim().isEmpty()) return;
        m.name = name.trim();
        db.log("Mengubah data member " + m.no);
        load();
    }
}
