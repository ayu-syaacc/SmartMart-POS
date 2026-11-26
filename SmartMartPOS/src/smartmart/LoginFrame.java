package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.prefs.Preferences;

/** Layar login: Kasir / Admin. */
final class LoginFrame extends JFrame {
    private final JTextField username = Ui.field(18);
    private final JPasswordField password = new JPasswordField(18);
    private final JCheckBox remember = new JCheckBox("Ingat saya di perangkat ini");
    private final JLabel error = Ui.label(" ", Font.PLAIN, 12, Ui.DANGER);
    private final Preferences prefs = Preferences.userNodeForPackage(LoginFrame.class);
    private String role = "KASIR";

    LoginFrame() {
        super("SmartMart POS - Masuk");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(promo());
        root.add(form());
        setContentPane(root);
        String last = prefs.get("lastUser", "");
        if (!last.isEmpty()) {
            username.setText(last);
            remember.setSelected(true);
            SwingUtilities.invokeLater(password::requestFocusInWindow);
        }
        getRootPane().setDefaultButton(null);
    }

    private JPanel promo() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Ui.PRIMARY_DK, getWidth(), getHeight(), new Color(0x0B3B3A)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(48, 44, 40, 40));
        p.add(left(Ui.label("SmartMart POS", Font.BOLD, 30, Color.WHITE)));
        p.add(left(Ui.label("Smart Retail Operations & Management System", Font.PLAIN, 13, new Color(0x99F6E4))));
        p.add(Box.createVerticalStrut(34));
        p.add(left(Ui.label("Kasir cepat,", Font.BOLD, 34, Color.WHITE)));
        p.add(left(Ui.label("stok terjaga.", Font.BOLD, 34, new Color(0x5EEAD4))));
        p.add(Box.createVerticalStrut(10));
        p.add(left(Ui.label("Satu aplikasi untuk kasir dan admin supermarket.", Font.PLAIN, 14, new Color(0xCCFBF1))));
        p.add(Box.createVerticalStrut(30));
        String[][] f = {{"Fast Checkout", "Transaksi cepat lewat barcode"}, {"Accountable Transactions", "Riwayat, retur, dan void terlacak"},
                {"Loss Prevention", "Pantau stok, penerimaan, kedaluwarsa"}, {"Smart Shopping", "Member, poin, dan diskon"}};
        for (String[] x : f) {
            p.add(left(Ui.label(x[0], Font.BOLD, 14, Color.WHITE)));
            p.add(left(Ui.label(x[1], Font.PLAIN, 12, new Color(0x99F6E4))));
            p.add(Box.createVerticalStrut(10));
        }
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel form() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(60, 60, 40, 60));
        p.add(left(Ui.label("Selamat datang", Font.BOLD, 26, Ui.TEXT)));
        p.add(left(Ui.muted("Masuk untuk memulai shift Anda")));
        p.add(Box.createVerticalStrut(22));
        p.add(left(Ui.muted("Masuk sebagai")));
        JPanel roles = new JPanel(new GridLayout(1, 2, 8, 0));
        roles.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        for (String r : new String[]{"KASIR", "ADMIN"}) {
            JToggleButton b = Ui.chip(r.equals("KASIR") ? "Kasir" : "Admin");
            b.setSelected(r.equals(role));
            b.addActionListener(e -> role = r);
            bg.add(b);
            roles.add(b);
        }
        roles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(left(roles));
        p.add(Box.createVerticalStrut(14));
        p.add(left(Ui.muted("Username")));
        username.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(left(username));
        p.add(Box.createVerticalStrut(10));
        p.add(left(Ui.muted("Password")));
        password.setFont(Ui.f(Font.PLAIN, 14));
        password.setBorder(username.getBorder());
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(left(password));
        p.add(Box.createVerticalStrut(8));
        remember.setOpaque(false);
        p.add(left(remember));
        p.add(Box.createVerticalStrut(6));
        p.add(left(error));
        p.add(Box.createVerticalStrut(6));
        Ui.Btn go = Ui.primary("Masuk");
        go.setFont(Ui.f(Font.BOLD, 15));
        go.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        go.setAlignmentX(Component.LEFT_ALIGNMENT);
        go.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        username.addActionListener(e -> password.requestFocusInWindow());
        p.add(go);
        p.add(Box.createVerticalStrut(14));
        p.add(left(Ui.muted("Password tersimpan aman sebagai hash")));
        p.add(Box.createVerticalStrut(18));
        p.add(left(Ui.muted("<html>Akun demo: <b>kasir01</b> / kasir123 &nbsp;|&nbsp; <b>admin01</b> / admin123</html>")));
        p.add(Box.createVerticalGlue());
        p.add(left(Ui.muted("SmartMart POS v1.0 | Kasir diarahkan ke layar transaksi")));
        return p;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    private void doLogin() {
        User u = Db.get().login(username.getText(), new String(password.getPassword()));
        if (u == null) {
            error.setText("Username atau password salah, atau akun nonaktif.");
            password.setText("");
            return;
        }
        if (!u.role.equals(role)) {
            Db.get().current = null;
            error.setText("Akun ini bukan akun " + (role.equals("KASIR") ? "Kasir" : "Admin") + ". Pilih peran yang sesuai.");
            return;
        }
        if (remember.isSelected()) prefs.put("lastUser", u.username);
        else prefs.remove("lastUser");
        dispose();
        new MainFrame(u).setVisible(true);
    }
}
