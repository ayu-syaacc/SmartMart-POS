package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Jendela utama: sidebar menurut peran + area halaman. */
final class MainFrame extends JFrame implements Nav {
    private final User user;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, JToggleButton> navBtns = new LinkedHashMap<>();
    private final JLabel lblTitle = Ui.label("", Font.BOLD, 20, Ui.TEXT);
    private final JLabel lblSub = Ui.muted("");
    private final JLabel lblClock = Ui.label("", Font.PLAIN, 13, Ui.MUTED);
    private PosPage pos;
    private ReturnPage ret;
    private VoidPage voidPage;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm", Db.ID);

    MainFrame(User user) {
        super("SmartMart POS");
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1320, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Ui.BG);
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(Color.WHITE);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.BORDER), new EmptyBorder(12, 20, 12, 20)));
        JPanel t = new JPanel(new GridLayout(2, 1));
        t.setOpaque(false);
        t.add(lblTitle);
        t.add(lblSub);
        head.add(t, BorderLayout.WEST);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        r.setOpaque(false);
        r.add(lblClock);
        r.add(Ui.badge("Online", Ui.OK));
        head.add(r, BorderLayout.EAST);
        main.add(head, BorderLayout.NORTH);
        content.setBackground(Ui.BG);
        main.add(content, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);

        Timer clock = new Timer(1000, e -> lblClock.setText(LocalDateTime.now().format(CLOCK)));
        clock.setInitialDelay(0);
        clock.start();

        go(user.isAdmin() ? "dash" : "pos");
    }

    private JPanel sidebar() {
        JPanel s = new JPanel();
        s.setBackground(Ui.SIDEBAR);
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setPreferredSize(new Dimension(230, 10));
        s.setBorder(new EmptyBorder(16, 12, 16, 12));
        JLabel logo = Ui.label("SmartMart", Font.BOLD, 22, Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = Ui.label("POS", Font.BOLD, 12, new Color(0x5EEAD4));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.add(logo);
        s.add(sub);
        s.add(Box.createVerticalStrut(16));

        List<String[]> nav = new ArrayList<>();
        if (user.isAdmin()) {
            nav.add(new String[]{"dash", "Dashboard"});
            addPage("dash", new DashboardPage(this));
        } else {
            // Fitur kasir hanya dibuat untuk akun Kasir (admin tidak memilikinya).
            pos = new PosPage(this);
            ret = new ReturnPage(this);
            voidPage = new VoidPage(this);
            nav.add(new String[]{"pos", "Transaksi"});
            addPage("pos", pos);
            nav.add(new String[]{"history", "Riwayat"});
            addPage("history", new HistoryPage(this));
            nav.add(new String[]{"return", "SmartReturn"});
            addPage("return", ret);
            nav.add(new String[]{"void", "VoidGuard"});
            addPage("void", voidPage);
            nav.add(new String[]{"member", "Member"});
            addPage("member", new MemberPage(this));
        }
        if (user.isAdmin()) {
            String[][] admin = {{"inv", "Persediaan"}, {"recv", "ReceiveCheck"}, {"stock", "StockAlert"}, {"loss", "LossGuard"},
                    {"fresh", "FreshSave"}, {"report", "Laporan"}, {"staff", "Pegawai"}, {"settings", "Pengaturan"}};
            Page[] ps = {new InventoryPage(this), new ReceivePage(this), new StockPage(this), new LossPage(this),
                    new FreshPage(this), new ReportPage(this), new StaffPage(this), new SettingsPage(this)};
            for (int i = 0; i < admin.length; i++) {
                nav.add(admin[i]);
                addPage(admin[i][0], ps[i]);
            }
        }

        ButtonGroup bg = new ButtonGroup();
        boolean adminSection = false;
        for (String[] n : nav) {
            if (user.isAdmin() && n[0].equals("inv")) {
                s.add(Box.createVerticalStrut(8));
                JLabel l = Ui.label("ADMIN", Font.BOLD, 10, new Color(0x5EEAD4));
                l.setAlignmentX(Component.LEFT_ALIGNMENT);
                l.setBorder(new EmptyBorder(0, 6, 4, 0));
                s.add(l);
            }
            JToggleButton b = navButton(n[1]);
            b.addActionListener(e -> go(n[0]));
            bg.add(b);
            navBtns.put(n[0], b);
            s.add(b);
            s.add(Box.createVerticalStrut(2));
        }
        s.add(Box.createVerticalGlue());

        JPanel me = new JPanel(new GridLayout(2, 1));
        me.setOpaque(false);
        me.setAlignmentX(Component.LEFT_ALIGNMENT);
        me.add(Ui.label(user.name, Font.BOLD, 14, Color.WHITE));
        me.add(Ui.label(user.isAdmin() ? "Administrator" : "Petugas Kasir", Font.PLAIN, 12, new Color(0x99F6E4)));
        me.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        s.add(me);
        s.add(Box.createVerticalStrut(8));
        Ui.Btn out = new Ui.Btn("Logout", new Color(0x134E4A), Color.WHITE, false);
        out.setAlignmentX(Component.LEFT_ALIGNMENT);
        out.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        out.addActionListener(e -> logout());
        s.add(out);
        return s;
    }

    private JToggleButton navButton(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(Ui.PRIMARY);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                setForeground(isSelected() ? Color.WHITE : new Color(0xCCFBF1));
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(Ui.f(Font.BOLD, 13));
        b.setBorder(new EmptyBorder(9, 14, 9, 14));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void addPage(String key, Page p) {
        pages.put(key, p);
        content.add(p, key);
    }

    private void logout() {
        if (!Ui.ask(this, "Keluar dari akun " + user.name + "?")) return;
        Db.get().log("Logout", user.name);
        Db.get().current = null;
        dispose();
        new LoginFrame().setVisible(true);
    }

    // ------------------------------------------------------------ Nav
    @Override public void go(String page) {
        Page p = pages.get(page);
        if (p == null) return;
        p.refresh();
        lblTitle.setText(p.title());
        lblSub.setText(p.subtitle());
        cards.show(content, page);
        JToggleButton b = navBtns.get(page);
        if (b != null) b.setSelected(true);
    }

    @Override public void goReturn(String saleId) {
        if (ret == null) return;
        ret.load(saleId);
        go("return");
    }

    @Override public void goVoid(String saleId) {
        if (voidPage == null) return;
        voidPage.load(saleId);
        go("void");
    }

    @Override public void setMember(Member m) {
        if (pos != null) pos.setMember(m);
    }

    @Override public User user() { return user; }
}
