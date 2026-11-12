
package com.smartmart.ui.common;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.smartmart.model.Pegawai;
import com.smartmart.ui.auth.Login;

public class MainFrame extends JFrame {

    private final Pegawai pegawai;
    private final JPanel contentPanel;
    private final CardLayout cardLayout;
    private final JPanel sidebar;

    private boolean darkMode = false;

    private final Color SIDEBAR_COLOR = new Color(30, 58, 95);
    private final Color LIGHT_BACKGROUND = new Color(245, 247, 250);
    private final Color DARK_BACKGROUND = new Color(35, 39, 47);

    public MainFrame(Pegawai pegawai) {

        this.pegawai = pegawai;

        setTitle("SmartMart POS");
        setSize(1200, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);

        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(LIGHT_BACKGROUND);

        JPanel dashboard = createPage(
                "Dashboard",
                "Selamat datang di SmartMart POS"
        );

        dashboard.setName("Dashboard");
        contentPanel.add(dashboard, "Dashboard");

        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "Dashboard");
    }

    // Membuat header aplikasi
    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(SIDEBAR_COLOR);
        header.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        JLabel title = new JLabel("SMARTMART POS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));

        String nama = pegawai.getNama();
        String peran = pegawai.getPeran();

        JLabel userInfo = new JLabel(nama + " | " + peran);
        userInfo.setForeground(Color.WHITE);
        userInfo.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton themeButton = new JButton("Ganti Tema");
        themeButton.addActionListener(e -> toggleTheme());

        JButton logoutButton = new JButton("Logout");

        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(190, 65, 65));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(
                new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
        );

        logoutButton.addActionListener(e -> logout());

        JPanel rightPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

        rightPanel.setOpaque(false);
        rightPanel.add(userInfo);
        rightPanel.add(themeButton);
        rightPanel.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // Membuat sidebar navigasi
    private JPanel createSidebar() {

        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setPreferredSize(new Dimension(220, 0));

        panel.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        );

        JLabel menuTitle = new JLabel("MENU NAVIGASI");

        menuTitle.setForeground(Color.WHITE);
        menuTitle.setFont(new Font("Arial", Font.BOLD, 14));

        menuTitle.setBorder(
                BorderFactory.createEmptyBorder(0, 10, 15, 0)
        );

        panel.add(menuTitle);

        String peran = pegawai.getPeran();

        if ("ADMIN".equalsIgnoreCase(peran)) {

            addMenu(panel, "Dashboard");
            addMenu(panel, "Persediaan");
            addMenu(panel, "ReceiveCheck");
            addMenu(panel, "StockAlert");
            addMenu(panel, "LossGuard");
            addMenu(panel, "FreshSave");
            addMenu(panel, "Laporan");
            addMenu(panel, "Pegawai");
            addMenu(panel, "Pengaturan");

        } else if ("KASIR".equalsIgnoreCase(peran)) {

            addMenu(panel, "Dashboard");
            addMenu(panel, "Kasir (POS)");
            addMenu(panel, "Transaksi");
            addMenu(panel, "SmartReturn");
            addMenu(panel, "Member");

        } else {

            JLabel warning = new JLabel("Peran tidak dikenali");
            warning.setForeground(Color.WHITE);
            panel.add(warning);
        }

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // Membuat tombol menu
    private void addMenu(JPanel panel, String menuName) {

        JButton button = new JButton(menuName);

        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_COLOR);

        button.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 10)
        );

        button.addActionListener(e -> showPage(menuName));

        panel.add(button);
        panel.add(Box.createVerticalStrut(5));
    }

    // Menampilkan halaman berdasarkan menu
    private void showPage(String pageName) {

        boolean pageExists = false;

        for (Component component : contentPanel.getComponents()) {

            if (pageName.equals(component.getName())) {
                pageExists = true;
                break;
            }
        }

        if (!pageExists) {

            JPanel page;

            if ("SmartReturn".equals(pageName)) {

                page = createSmartReturnPage();

            } else {

                page = createPage(
                        pageName,
                        "Halaman " + pageName
                                + " sedang dalam pengembangan."
                );
            }

            page.setName(pageName);
            contentPanel.add(page, pageName);
        }

        cardLayout.show(contentPanel, pageName);
    }

    // Membuat halaman SmartReturn
    private JPanel createSmartReturnPage() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setName("SmartReturn");

        panel.setBackground(
                darkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND
        );

        JPanel information = new JPanel();

        information.setOpaque(false);

        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        JLabel titleLabel = new JLabel("SMART RETURN");

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(
                "Fitur pengembalian barang dan otorisasi Admin."
        );

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 16)
        );

        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton authorizationButton = new JButton(
                "Uji Otorisasi Admin"
        );

        authorizationButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        authorizationButton.addActionListener(e -> {

            Pegawai admin = AdminAuthorizationDialog
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

        information.add(titleLabel);
        information.add(Box.createVerticalStrut(10));
        information.add(descriptionLabel);
        information.add(Box.createVerticalStrut(25));
        information.add(authorizationButton);

        panel.add(information);

        return panel;
    }

    // Membuat tampilan halaman sementara
    private JPanel createPage(String title, String description) {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setName(title);

        panel.setBackground(
                darkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND
        );

        JPanel information = new JPanel();

        information.setOpaque(false);

        information.setLayout(
                new BoxLayout(information, BoxLayout.Y_AXIS)
        );

        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 28)
        );

        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 16)
        );

        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        information.add(titleLabel);
        information.add(Box.createVerticalStrut(10));
        information.add(descriptionLabel);

        panel.add(information);

        return panel;
    }

    // Mengganti tema terang dan gelap
    private void toggleTheme() {

        darkMode = !darkMode;

        Color background = darkMode
                ? DARK_BACKGROUND
                : LIGHT_BACKGROUND;

        contentPanel.setBackground(background);

        for (Component component : contentPanel.getComponents()) {

            component.setBackground(background);
        }

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    // Proses logout
    private void logout() {

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin keluar dari akun?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {

            Login login = new Login();
            login.setVisible(true);

            dispose();
        }
    }
}
