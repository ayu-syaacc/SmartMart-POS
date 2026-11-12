
package com.smartmart.ui.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import com.smartmart.dao.ProdukDAO;
import com.smartmart.model.Pegawai;
import com.smartmart.model.SmartCart;
import com.smartmart.service.LogAktivitasService;
import com.smartmart.ui.admin.DashboardAdminPanel;
import com.smartmart.ui.admin.LogAktivitasPanel;
import com.smartmart.ui.admin.PegawaiPanel;
import com.smartmart.ui.admin.PersediaanPanel;
import com.smartmart.ui.auth.Login;
import com.smartmart.ui.kasir.TransaksiPanel;

public class MainFrame extends JFrame {

    private final Pegawai pegawai;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final JPanel sidebar;

    private boolean darkMode = false;
    private JButton activeButton;

    // Tombol sidebar menurut nama menu, dipakai untuk berpindah halaman dari dalam dashboard.
    private final Map<String, JButton> menuButtons = new HashMap<>();

    // ==========================================
    // PALET WARNA SMARTMART
    // ==========================================

    private final Color SIDEBAR_COLOR =
            new Color(23, 107, 69);

    private final Color SIDEBAR_HOVER =
            new Color(35, 130, 85);

    private final Color ACTIVE_COLOR =
            new Color(45, 145, 96);

    private final Color LIGHT_BACKGROUND =
            new Color(243, 246, 241);

    private final Color DARK_BACKGROUND =
            new Color(37, 45, 40);

    private final Color LIGHT_SURFACE =
            Color.WHITE;

    private final Color DARK_SURFACE =
            new Color(48, 59, 51);

    private final Color TEXT_COLOR =
            new Color(40, 50, 43);

    private final Color MUTED_COLOR =
            new Color(120, 130, 122);

    private final Color DARK_MUTED =
            new Color(190, 200, 192);

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public MainFrame(Pegawai pegawai) {

        this.pegawai = pegawai;

        setTitle("SmartMart POS");
        setSize(1200, 750);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();

        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(LIGHT_BACKGROUND);

        JPanel dashboard;

        if ("ADMIN".equalsIgnoreCase(pegawai.getPeran())) {

            dashboard = new DashboardAdminPanel(this::navigateTo);

        } else {

            dashboard = createPage(
                    "Dashboard",
                    "Selamat datang di SmartMart POS"
            );
        }

        dashboard.setName("Dashboard");

        contentPanel.add(dashboard, "Dashboard");

        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "Dashboard");
    }

    // ==========================================
    // HEADER
    // ==========================================

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(SIDEBAR_COLOR);

        header.setBorder(
                BorderFactory.createEmptyBorder(
                        13, 24, 13, 24
                )
        );

        // Identitas aplikasi
        JPanel brandPanel = new JPanel();

        brandPanel.setOpaque(false);

        brandPanel.setLayout(
                new BoxLayout(
                        brandPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title = new JLabel(
                "  SMARTMART POS",
                FontIcon.of(
                        FontAwesomeSolid.SHOPPING_CART,
                        24,
                        Color.WHITE
                ),
                SwingConstants.LEFT
        );

        title.setForeground(Color.WHITE);

        title.setFont(
                new Font("Arial", Font.BOLD, 23)
        );

        JLabel subtitle = new JLabel(
                "POINT OF SALE MANAGEMENT SYSTEM"
        );

        subtitle.setForeground(
                new Color(210, 235, 218)
        );

        subtitle.setFont(
                new Font("Arial", Font.PLAIN, 10)
        );

        brandPanel.add(title);
        brandPanel.add(Box.createVerticalStrut(3));
        brandPanel.add(subtitle);

        // Informasi pengguna
        String nama = pegawai.getNama();
        String peran = pegawai.getPeran();

        JPanel rightPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        10,
                        5
                )
        );

        rightPanel.setOpaque(false);

        JLabel userInfo = new JLabel(
                nama + "  |  " + peran,
                FontIcon.of(
                        FontAwesomeSolid.USER_CIRCLE,
                        17,
                        Color.WHITE
                ),
                SwingConstants.LEFT
        );

        userInfo.setForeground(Color.WHITE);

        userInfo.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        // Tombol tema
        JButton themeButton = new JButton();

        updateThemeButton(themeButton);
        styleHeaderButton(themeButton);

        themeButton.addActionListener(e -> {

            toggleTheme();

            updateThemeButton(themeButton);
        });

        // Tombol logout
        JButton logoutButton = new JButton(
                "Logout",
                FontIcon.of(
                        FontAwesomeSolid.SIGN_OUT_ALT,
                        16,
                        Color.WHITE
                )
        );

        logoutButton.setForeground(Color.WHITE);

        logoutButton.setBackground(
                new Color(190, 65, 65)
        );

        logoutButton.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        logoutButton.setBorder(
                BorderFactory.createEmptyBorder(
                        9, 15, 9, 15
                )
        );

        logoutButton.setIconTextGap(8);

        logoutButton.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        logoutButton.setBackground(
                                new Color(165, 48, 48)
                        );
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        logoutButton.setBackground(
                                new Color(190, 65, 65)
                        );
                    }
                }
        );

        logoutButton.addActionListener(
                e -> logout()
        );

        rightPanel.add(userInfo);
        rightPanel.add(themeButton);
        rightPanel.add(logoutButton);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private void updateThemeButton(JButton button) {

        if (darkMode) {

            button.setText("Tema Terang");

            button.setIcon(
                    FontIcon.of(
                            FontAwesomeSolid.SUN,
                            16,
                            Color.WHITE
                    )
            );

        } else {

            button.setText("Tema Gelap");

            button.setIcon(
                    FontIcon.of(
                            FontAwesomeSolid.MOON,
                            16,
                            Color.WHITE
                    )
            );
        }

        button.setIconTextGap(8);
    }

    private void styleHeaderButton(JButton button) {

        button.setForeground(Color.WHITE);

        button.setBackground(
                new Color(35, 125, 82)
        );

        button.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        9, 13, 9, 13
                )
        );

        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setBackground(
                                new Color(48, 145, 96)
                        );
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setBackground(
                                new Color(35, 125, 82)
                        );
                    }
                }
        );
    }

    // ==========================================
    // SIDEBAR
    // ==========================================

    private JPanel createSidebar() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setBackground(SIDEBAR_COLOR);

        panel.setPreferredSize(
                new Dimension(235, 0)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        20, 12, 15, 12
                )
        );

        JLabel menuTitle = new JLabel(
                "MENU NAVIGASI",
                FontIcon.of(
                        FontAwesomeSolid.TH_LIST,
                        13,
                        new Color(205, 230, 211)
                ),
                SwingConstants.LEFT
        );

        menuTitle.setForeground(
                new Color(205, 230, 211)
        );

        menuTitle.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        menuTitle.setIconTextGap(8);

        menuTitle.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.add(menuTitle);
        panel.add(Box.createVerticalStrut(15));

        String peran = pegawai.getPeran();

        if ("ADMIN".equalsIgnoreCase(peran)) {

            addMenu(panel, "Dashboard");
            addMenu(panel, "Persediaan");
            addMenu(panel, "ReceiveCheck");
            addMenu(panel, "StockAlert");
            addMenu(panel, "LossGuard");
            addMenu(panel, "FreshSave");
            addMenu(panel, "Laporan");

            addSidebarSeparator(panel);

            addMenu(panel, "Pegawai");
            addMenu(panel, "Log Aktivitas");
            addMenu(panel, "Pengaturan");

        } else if ("KASIR".equalsIgnoreCase(peran)) {

            addMenu(panel, "Dashboard");
            addMenu(panel, "Kasir (POS)");
            addMenu(panel, "Transaksi");
            addMenu(panel, "SmartReturn");
            addMenu(panel, "Member");

        } else {

            JLabel warning = new JLabel(
                    "Peran tidak dikenali"
            );

            warning.setForeground(Color.WHITE);
            panel.add(warning);
        }

        panel.add(Box.createVerticalGlue());

        JPanel footerPanel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        0,
                        0
                )
        );

        footerPanel.setOpaque(false);
        footerPanel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JLabel footerIcon = new JLabel(
                FontIcon.of(
                        FontAwesomeSolid.SHIELD_ALT,
                        12,
                        new Color(190, 220, 198)
                )
        );

        JLabel footer = new JLabel(
                " SMARTMART  |  SYSTEM"
        );

        footer.setForeground(
                new Color(190, 220, 198)
        );

        footer.setFont(
                new Font("Arial", Font.PLAIN, 10)
        );

        footerPanel.add(footerIcon);
        footerPanel.add(footer);

        panel.add(footerPanel);

        return panel;
    }

    private void addSidebarSeparator(JPanel panel) {

        JPanel separator = new JPanel();

        separator.setBackground(
                new Color(75, 145, 105)
        );

        separator.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        1
                )
        );

        separator.setPreferredSize(
                new Dimension(200, 1)
        );

        separator.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        panel.add(Box.createVerticalStrut(12));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(12));
    }

    // ==========================================
    // IKON FONTAWESOME
    // ==========================================

    private FontAwesomeSolid getMenuIconType(
            String menuName
    ) {

        switch (menuName) {

            case "Dashboard":
                return FontAwesomeSolid.HOME;

            case "Persediaan":
                return FontAwesomeSolid.BOXES;

            case "ReceiveCheck":
                return FontAwesomeSolid.TRUCK;

            case "StockAlert":
                return FontAwesomeSolid.EXCLAMATION_TRIANGLE;

            case "LossGuard":
                return FontAwesomeSolid.SHIELD_ALT;

            case "FreshSave":
                return FontAwesomeSolid.LEAF;

            case "Laporan":
                return FontAwesomeSolid.CHART_BAR;

            case "Pegawai":
                return FontAwesomeSolid.USERS;

            case "Log Aktivitas":
                return FontAwesomeSolid.HISTORY;

            case "Pengaturan":
                return FontAwesomeSolid.COG;

            case "Kasir (POS)":
                return FontAwesomeSolid.CASH_REGISTER;

            case "Transaksi":
                return FontAwesomeSolid.EXCHANGE_ALT;

            case "SmartReturn":
                return FontAwesomeSolid.UNDO;

            case "Member":
                return FontAwesomeSolid.USER;

            default:
                return FontAwesomeSolid.CIRCLE;
        }
    }

    private FontIcon getMenuIcon(
            String menuName,
            int size,
            Color color
    ) {

        Ikon ikon = getMenuIconType(menuName);

        return FontIcon.of(ikon, size, color);
    }

    // ==========================================
    // TOMBOL MENU
    // ==========================================

    private void addMenu(
            JPanel panel,
            String menuName
    ) {

        JButton button = new JButton(
                menuName,
                getMenuIcon(
                        menuName,
                        18,
                        Color.WHITE
                )
        );

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        43
                )
        );

        button.setPreferredSize(
                new Dimension(205, 43)
        );

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_COLOR);

        button.setIconTextGap(15);

        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setBorder(
                BorderFactory.createEmptyBorder(
                        8, 10, 8, 8
                )
        );

        // Efek hover
        button.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {

                        if (button != activeButton) {
                            button.setBackground(
                                    SIDEBAR_HOVER
                            );
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {

                        if (button != activeButton) {
                            button.setBackground(
                                    SIDEBAR_COLOR
                            );
                        }
                    }
                }
        );

        menuButtons.put(menuName, button);

        // Navigasi dan menu aktif
        button.addActionListener(e -> {

            if (activeButton != null) {
                activeButton.setBackground(
                        SIDEBAR_COLOR
                );
            }

            activeButton = button;

            activeButton.setBackground(
                    ACTIVE_COLOR
            );

            showPage(menuName);
        });

        // Dashboard aktif saat aplikasi dibuka
        if ("Dashboard".equals(menuName)) {

            activeButton = button;

            button.setBackground(
                    ACTIVE_COLOR
            );
        }

        panel.add(button);
        panel.add(Box.createVerticalStrut(5));
    }

    // ==========================================
    // NAVIGASI HALAMAN
    // ==========================================

    private void showPage(String pageName) {

        // Menu "Transaksi" dan "Kasir (POS)" memakai layar kasir yang sama.
        if ("Transaksi".equals(pageName)) {
            pageName = "Kasir (POS)";
        }

        boolean pageExists = false;

        for (Component component :
                contentPanel.getComponents()) {

            if (pageName.equals(component.getName())) {
                pageExists = true;
                break;
            }
        }

        if (!pageExists) {

            JPanel page;

            if ("SmartReturn".equals(pageName)) {

                page = createSmartReturnPage();

            } else if ("Persediaan".equals(pageName)) {

                page = new PersediaanPanel(pegawai);

            } else if ("Kasir (POS)".equals(pageName)) {

                page = new TransaksiPanel(
                        new ProdukDAO(),
                        SmartCart.tanpaLot()
                );

            } else if ("Pegawai".equals(pageName)) {

                page = new PegawaiPanel(pegawai);
                } else if ("Log Aktivitas".equals(pageName)) {

                page = new LogAktivitasPanel(pegawai);

            } else {

                page = createPage(
                        pageName,
                        "Halaman " + pageName
                                + " sedang dalam pengembangan."
                );
            }

            page.setName(pageName);

            contentPanel.add(
                    page,
                    pageName
            );
        }

        cardLayout.show(
                contentPanel,
                pageName
        );
    }

    // Berpindah halaman sekaligus menandai menu sidebar yang aktif.
    private void navigateTo(String menuName) {

        JButton button = menuButtons.get(menuName);

        if (button != null) {

            button.doClick();

        } else {

            showPage(menuName);
        }
    }

    // ==========================================
    // PANEL KARTU / SURFACE
    // ==========================================

    private JPanel createSurfacePanel() {

        JPanel surface = new JPanel();

        surface.setLayout(
                new BoxLayout(
                        surface,
                        BoxLayout.Y_AXIS
                )
        );

        surface.setOpaque(true);

        surface.setBackground(
                darkMode ? DARK_SURFACE : LIGHT_SURFACE
        );

        surface.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                darkMode
                                        ? new Color(70, 85, 74)
                                        : new Color(225, 233, 225)
                        ),
                        BorderFactory.createEmptyBorder(
                                35, 45, 35, 45
                        )
                )
        );

        surface.putClientProperty(
                "smartmart.surface",
                Boolean.TRUE
        );

        return surface;
    }

    // ==========================================
    // HALAMAN SMART RETURN
    // ==========================================

    private JPanel createSmartReturnPage() {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        panel.setName("SmartReturn");

        panel.setBackground(
                darkMode
                        ? DARK_BACKGROUND
                        : LIGHT_BACKGROUND
        );

        JPanel information = createSurfacePanel();

        JLabel iconLabel = new JLabel(
                FontIcon.of(
                        FontAwesomeSolid.UNDO,
                        48,
                        new Color(35, 145, 91)
                )
        );

        iconLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel titleLabel = new JLabel(
                "SMART RETURN"
        );

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        titleLabel.setForeground(
                darkMode ? Color.WHITE : TEXT_COLOR
        );

        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel descriptionLabel = new JLabel(
                "Fitur pengembalian barang dan otorisasi Admin."
        );

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        descriptionLabel.setForeground(
                darkMode ? DARK_MUTED : MUTED_COLOR
        );

        descriptionLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        // Tombol otorisasi
        JButton authorizationButton = new JButton(
                "Uji Otorisasi Admin"
        );

        authorizationButton.setIcon(
                FontIcon.of(
                        FontAwesomeSolid.KEY,
                        16,
                        Color.WHITE
                )
        );

        authorizationButton.setForeground(Color.WHITE);
        authorizationButton.setBackground(SIDEBAR_COLOR);

        authorizationButton.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        authorizationButton.setFocusPainted(false);
        authorizationButton.setBorderPainted(false);
        authorizationButton.setOpaque(true);

        authorizationButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        authorizationButton.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 20, 12, 20
                )
        );

        authorizationButton.setIconTextGap(10);

        authorizationButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        authorizationButton.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        authorizationButton.setBackground(
                                SIDEBAR_HOVER
                        );
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        authorizationButton.setBackground(
                                SIDEBAR_COLOR
                        );
                    }
                }
        );

        authorizationButton.addActionListener(e -> {

            Pegawai admin =
                    AdminAuthorizationDialog
                            .mintaOtorisasi(this);

            if (admin != null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Otorisasi berhasil diberikan oleh Admin: "
                                + admin.getNama(),
                        "Otorisasi Berhasil",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Otorisasi dibatalkan atau ditolak.",
                        "Otorisasi Gagal",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        information.add(iconLabel);
        information.add(Box.createVerticalStrut(18));
        information.add(titleLabel);
        information.add(Box.createVerticalStrut(10));
        information.add(descriptionLabel);
        information.add(Box.createVerticalStrut(28));
        information.add(authorizationButton);

        panel.add(information);

        return panel;
    }

    // ==========================================
    // HALAMAN UMUM
    // ==========================================

    private JPanel createPage(
            String title,
            String description
    ) {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        panel.setName(title);

        panel.setBackground(
                darkMode
                        ? DARK_BACKGROUND
                        : LIGHT_BACKGROUND
        );

        JPanel information = createSurfacePanel();

        JLabel iconLabel = new JLabel(
                getMenuIcon(
                        title,
                        48,
                        new Color(35, 145, 91)
                )
        );

        iconLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        titleLabel.setForeground(
                darkMode ? Color.WHITE : TEXT_COLOR
        );

        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 15)
        );

        descriptionLabel.setForeground(
                darkMode ? DARK_MUTED : MUTED_COLOR
        );

        descriptionLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        information.add(iconLabel);
        information.add(Box.createVerticalStrut(18));
        information.add(titleLabel);
        information.add(Box.createVerticalStrut(10));
        information.add(descriptionLabel);

        panel.add(information);

        return panel;
    }

    // ==========================================
    // GANTI TEMA
    // ==========================================

    private void toggleTheme() {

        darkMode = !darkMode;

        Color background = darkMode
                ? DARK_BACKGROUND
                : LIGHT_BACKGROUND;

        contentPanel.setBackground(background);

        for (Component component :
                contentPanel.getComponents()) {

            String pageName = component.getName();

            // Dashboard Admin memiliki desain internal sendiri.
            if (component instanceof DashboardAdminPanel) {
                continue;
            }

            // Panel Pegawai dan Log Aktivitas
            // memiliki desain internal sendiri.
            if ("Pegawai".equals(pageName)
                    || "Log Aktivitas".equals(pageName)
                    || "Kasir (POS)".equals(pageName)) {
                continue;
            }

            updatePageTheme(component, background);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void updatePageTheme(
            Component component,
            Color background
    ) {

        if (component instanceof JPanel) {

            JPanel panel = (JPanel) component;

            if (Boolean.TRUE.equals(
                    panel.getClientProperty("smartmart.surface")
            )) {

                panel.setBackground(
                        darkMode
                                ? DARK_SURFACE
                                : LIGHT_SURFACE
                );

                panel.setBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(
                                        darkMode
                                                ? new Color(70, 85, 74)
                                                : new Color(225, 233, 225)
                                ),
                                BorderFactory.createEmptyBorder(
                                        35, 45, 35, 45
                                )
                        )
                );

            } else if (panel.isOpaque()) {

                panel.setBackground(background);
            }
        }

        if (component instanceof JLabel) {

            JLabel label = (JLabel) component;

            // Label ikon tidak perlu diubah warnanya.
            if (label.getIcon() == null) {

                String text = label.getText();

                if (text != null
                        && !text.equals("SMART RETURN")
                        && !text.equals("Dashboard")
                        && !text.equals("Persediaan")) {

                    label.setForeground(
                            darkMode ? DARK_MUTED : MUTED_COLOR
                    );

                } else {

                    label.setForeground(
                            darkMode ? Color.WHITE : TEXT_COLOR
                    );
                }
            }
        }

        if (component instanceof Container) {

            Container container = (Container) component;

            for (Component child :
                    container.getComponents()) {

                updatePageTheme(child, background);
            }
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    private void logout() {

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin keluar dari akun?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {

            try {

                new LogAktivitasService().catatAktivitas(
                        pegawai.getIdPegawai(),
                        "LOGOUT",
                        "Pegawai berhasil logout dari sistem."
                );

            } catch (Exception logError) {

                System.err.println(
                        "Gagal mencatat aktivitas logout: "
                                + logError.getMessage()
                );
            }

            Login login = new Login();

            login.setVisible(true);

            dispose();
        }
    }
}
