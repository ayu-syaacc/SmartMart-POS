package smartmart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Pegawai: akun kasir/admin, hak akses, keamanan password, log aktivitas. */
final class StaffPage extends Page {
    private final DefaultTableModel model = Ui.model("Nama", "Username", "Peran", "Status");
    private final JTable table = Ui.table(model);
    private final DefaultTableModel logModel = Ui.model("Waktu", "Pelaku", "Aktivitas");

    StaffPage(Nav nav) {
        super(nav);
        JPanel left = Ui.card();
        left.setLayout(new BorderLayout(0, 8));
        JPanel head = Ui.transparent(new BorderLayout());
        head.add(Ui.label("Daftar pegawai", Font.BOLD, 14, Ui.TEXT), BorderLayout.WEST);
        JPanel btns = Ui.transparent(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        Ui.Btn add = Ui.primary("Tambah");
        Ui.Btn edit = Ui.plain("Ubah");
        Ui.Btn toggle = Ui.plain("Aktif / Nonaktif");
        Ui.Btn pw = Ui.plain("Reset Password");
        btns.add(add);
        btns.add(edit);
        btns.add(toggle);
        btns.add(pw);
        head.add(btns, BorderLayout.EAST);
        left.add(head, BorderLayout.NORTH);
        JPanel body = Ui.transparent(new GridLayout(2, 1, 0, 10));
        body.add(Ui.scroll(table));
        JPanel lg = Ui.transparent(new BorderLayout(0, 4));
        lg.add(Ui.label("Log aktivitas", Font.BOLD, 13, Ui.TEXT), BorderLayout.NORTH);
        JTable lt = Ui.table(logModel);
        Ui.widths(lt, 130, 90, 420);
        lg.add(Ui.scroll(lt), BorderLayout.CENTER);
        body.add(lg);
        left.add(body, BorderLayout.CENTER);
        add(left, BorderLayout.CENTER);

        JPanel right = Ui.card();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(290, 10));
        right.add(left(Ui.label("Keamanan password", Font.BOLD, 14, Ui.TEXT)));
        right.add(left(Ui.muted("<html>Password disimpan sebagai hash (SHA-256 dengan salt), tidak pernah sebagai teks biasa.</html>")));
        right.add(Box.createVerticalStrut(14));
        right.add(left(Ui.label("Hak akses", Font.BOLD, 14, Ui.TEXT)));
        right.add(Box.createVerticalStrut(6));
        right.add(left(Ui.label("Kasir", Font.BOLD, 13, Ui.PRIMARY)));
        right.add(left(Ui.muted("<html>- Transaksi dan riwayat<br>- Ajukan retur dan void<br>- Member dan poin</html>")));
        right.add(Box.createVerticalStrut(8));
        right.add(left(Ui.label("Admin", Font.BOLD, 13, Ui.PRIMARY)));
        right.add(left(Ui.muted("<html>- Semua fitur kasir<br>- Produk, harga, supplier<br>- ReceiveCheck dan StockAlert<br>"
                + "- FreshSave dan laporan<br>- Pegawai dan log aktivitas</html>")));
        right.add(Box.createVerticalGlue());
        add(right, BorderLayout.EAST);

        add.addActionListener(e -> form(null));
        edit.addActionListener(e -> { User u = selected(); if (u != null) form(u); });
        toggle.addActionListener(e -> toggle());
        pw.addActionListener(e -> resetPw());
        refresh();
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    @Override String title() { return "Pegawai"; }

    @Override String subtitle() { return "Akun kasir dan admin dengan hak akses berbeda"; }

    private User selected() {
        int r = table.getSelectedRow();
        if (r < 0) { Ui.info(this, "Pilih pegawai terlebih dahulu."); return null; }
        return db.d.users.get(r);
    }

    @Override void refresh() {
        int keep = table.getSelectedRow();
        model.setRowCount(0);
        for (User u : db.d.users) model.addRow(new Object[]{u.name, u.username, u.role, u.active ? "Aktif" : "Nonaktif"});
        if (keep >= 0 && keep < model.getRowCount()) table.setRowSelectionInterval(keep, keep);
        logModel.setRowCount(0);
        List<ActLog> a = new ArrayList<>(db.d.acts);
        for (int i = a.size() - 1; i >= Math.max(0, a.size() - 300); i--) {
            ActLog g = a.get(i);
            logModel.addRow(new Object[]{g.time.format(Db.DTF), g.actor, g.text});
        }
    }

    private void form(User u) {
        boolean isNew = u == null;
        JTextField name = Ui.field(16), user = Ui.field(16);
        JPasswordField pw = new JPasswordField(16);
        JComboBox<String> role = new JComboBox<>(new String[]{"KASIR", "ADMIN"});
        if (!isNew) {
            name.setText(u.name);
            user.setText(u.username);
            user.setEditable(false);
            role.setSelectedItem(u.role);
        }
        JPanel f = new JPanel(new GridLayout(0, 2, 8, 8));
        f.add(new JLabel("Nama"));
        f.add(name);
        f.add(new JLabel("Username"));
        f.add(user);
        f.add(new JLabel("Peran"));
        f.add(role);
        if (isNew) {
            f.add(new JLabel("Password"));
            f.add(pw);
        }
        if (JOptionPane.showConfirmDialog(this, f, isNew ? "Tambah Pegawai" : "Ubah Pegawai", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        if (name.getText().trim().isEmpty() || user.getText().trim().isEmpty()) { Ui.err(this, "Nama dan username wajib diisi."); return; }
        try {
            if (isNew) {
                if (pw.getPassword().length < 4) { Ui.err(this, "Password minimal 4 karakter."); return; }
                db.addUser(name.getText().trim(), user.getText().trim(), (String) role.getSelectedItem(), new String(pw.getPassword()));
            } else {
                if (u.isAdmin() && "KASIR".equals(role.getSelectedItem()) && activeAdmins() <= 1) {
                    Ui.err(this, "Minimal harus ada satu admin aktif.");
                    return;
                }
                u.name = name.getText().trim();
                u.role = (String) role.getSelectedItem();
                db.log("Mengubah pegawai " + u.username);
            }
        } catch (IllegalArgumentException ex) {
            Ui.err(this, ex.getMessage());
        }
        refresh();
    }

    private int activeAdmins() {
        int n = 0;
        for (User x : db.d.users) if (x.isAdmin() && x.active) n++;
        return n;
    }

    private void toggle() {
        User u = selected();
        if (u == null) return;
        if (u == nav.user()) { Ui.err(this, "Anda tidak dapat menonaktifkan akun yang sedang dipakai."); return; }
        if (u.active && u.isAdmin() && activeAdmins() <= 1) { Ui.err(this, "Minimal harus ada satu admin aktif."); return; }
        u.active = !u.active;
        db.log((u.active ? "Mengaktifkan " : "Menonaktifkan ") + u.username);
        refresh();
    }

    private void resetPw() {
        User u = selected();
        if (u == null) return;
        JPasswordField pw = new JPasswordField(16);
        if (JOptionPane.showConfirmDialog(this, new Object[]{"Password baru untuk " + u.username + ":", pw}, "Reset Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        if (pw.getPassword().length < 4) { Ui.err(this, "Password minimal 4 karakter."); return; }
        db.setPassword(u, new String(pw.getPassword()));
        Ui.info(this, "Password " + u.username + " diperbarui.");
        refresh();
    }
}
