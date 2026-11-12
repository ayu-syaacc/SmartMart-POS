
package com.smartmart.ui.auth;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.smartmart.model.Pegawai;
import com.smartmart.service.AuthService;
import com.smartmart.service.LogAktivitasService;
import com.smartmart.ui.common.MainFrame;

public class Login extends JFrame {

    private static final Color GREEN = new Color(13, 145, 91);
    private static final Color DARK_GREEN = new Color(8, 111, 75);
    private static final Color MINT = new Color(235, 246, 240);
    private static final Color DARK_TEXT = new Color(29, 52, 42);
    private static final Color MUTED = new Color(120, 135, 128);

    private final AuthService authService;
    private final Preferences preferences;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberCheckBox;

    private JToggleButton kasirButton;
    private JToggleButton adminButton;

    private JButton loginButton;

    private String selectedRole = "KASIR";

    public Login() {

        authService = new AuthService();
        preferences = Preferences.userNodeForPackage(Login.class);

        setTitle("SmartMart POS - Login");
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(MINT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.weightx = 1.05;
        mainPanel.add(createBrandPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.95;
        mainPanel.add(createLoginPanel(), gbc);

        setContentPane(mainPanel);
    }

    // =====================================================
    // PANEL KIRI: BRANDING DAN INFORMASI SMARTMART
    // =====================================================

    private JPanel createBrandPanel() {

        JPanel panel = new GradientPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
                35, 45, 35, 45
        ));

        JPanel brandContent = new JPanel();
        brandContent.setOpaque(false);
        brandContent.setLayout(new BorderLayout(0, 35));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🛒 SmartMart POS");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel taglineLabel = new JLabel(
                "Smart Retail Operations & Management System"
        );
        taglineLabel.setForeground(new Color(220, 245, 230));
        taglineLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        logoPanel.add(logoLabel, BorderLayout.NORTH);
        logoPanel.add(taglineLabel, BorderLayout.SOUTH);

        JPanel sloganPanel = new JPanel();
        sloganPanel.setOpaque(false);
        sloganPanel.setLayout(new BorderLayout(0, 12));

        JLabel slogan = new JLabel(
                "<html>Kasir cepat,<br><span style='color:#F1ED69'>"
                + "stok terjaga.</span></html>"
        );
        slogan.setForeground(Color.WHITE);
        slogan.setFont(new Font("SansSerif", Font.BOLD, 39));

        JLabel description = new JLabel(
                "Satu aplikasi untuk kasir dan admin supermarket."
        );
        description.setForeground(Color.WHITE);
        description.setFont(new Font("SansSerif", Font.PLAIN, 14));

        sloganPanel.add(slogan, BorderLayout.NORTH);
        sloganPanel.add(description, BorderLayout.CENTER);

        JPanel featurePanel = new JPanel(new GridBagLayout());
        featurePanel.setOpaque(false);

        addFeatureCard(
                featurePanel, "▦", "Fast Checkout",
                "Transaksi cepat lewat barcode", 0, 0
        );

        addFeatureCard(
                featurePanel, "▤", "Accountable Transactions",
                "Riwayat, retur, dan void terlacak", 1, 0
        );

        addFeatureCard(
                featurePanel, "◈", "Loss Prevention",
                "Pantau stok, penerimaan, kedaluwarsa", 0, 1
        );

        addFeatureCard(
                featurePanel, "▣", "Smart Shopping",
                "Member, poin, dan diskon", 1, 1
        );

        JPanel centerPanel = new JPanel(new BorderLayout(0, 35));
        centerPanel.setOpaque(false);
        centerPanel.add(sloganPanel, BorderLayout.NORTH);
        centerPanel.add(featurePanel, BorderLayout.CENTER);

        brandContent.add(logoPanel, BorderLayout.NORTH);
        brandContent.add(centerPanel, BorderLayout.CENTER);

        panel.add(brandContent, BorderLayout.CENTER);

        return panel;
    }

    private void addFeatureCard(
            JPanel parent,
            String icon,
            String title,
            String description,
            int x,
            int y
    ) {

        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 255, 225, 180), 1
                ),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setForeground(new Color(245, 240, 90));
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(
                new Color(230, 245, 235)
        );
        descriptionLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 10)
        );

        JPanel textPanel = new JPanel(new BorderLayout(0, 4));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(descriptionLabel, BorderLayout.CENTER);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);

        parent.add(card, gbc);
    }

    // =====================================================
    // PANEL KANAN: FORM LOGIN
    // =====================================================

    private JPanel createLoginPanel() {

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(MINT);
        background.setBorder(BorderFactory.createEmptyBorder(
                35, 30, 35, 30
        ));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 235, 228), 1
                ),
                BorderFactory.createEmptyBorder(
                        35, 30, 30, 30
                )
        ));

        card.setPreferredSize(new Dimension(400, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel welcomeLabel = new JLabel("Selamat datang");
        welcomeLabel.setFont(
                new Font("SansSerif", Font.BOLD, 29)
        );
        welcomeLabel.setForeground(DARK_TEXT);

        JLabel subtitleLabel = new JLabel(
                "Masuk untuk memulai shift Anda"
        );
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );

        gbc.gridy = 0;
        card.add(welcomeLabel, gbc);

        gbc.gridy = 1;
        card.add(subtitleLabel, gbc);

        JLabel roleLabel = new JLabel("Masuk sebagai");
        roleLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        roleLabel.setForeground(MUTED);

        gbc.gridy = 2;
        gbc.insets = new Insets(25, 0, 7, 0);
        card.add(roleLabel, gbc);

        JPanel rolePanel = new JPanel(new GridBagLayout());
        rolePanel.setBackground(new Color(238, 244, 240));
        rolePanel.setBorder(BorderFactory.createEmptyBorder(
                4, 4, 4, 4
        ));

        kasirButton = createRoleButton("🛒  Kasir");
        adminButton = createRoleButton("◈  Admin");

        kasirButton.setSelected(true);

        kasirButton.addActionListener(e -> {
            selectedRole = "KASIR";
            kasirButton.setSelected(true);
            adminButton.setSelected(false);
            updateRoleButtons();
        });

        adminButton.addActionListener(e -> {
            selectedRole = "ADMIN";
            adminButton.setSelected(true);
            kasirButton.setSelected(false);
            updateRoleButtons();
        });

        GridBagConstraints roleGbc = new GridBagConstraints();
        roleGbc.gridy = 0;
        roleGbc.weightx = 1;
        roleGbc.fill = GridBagConstraints.HORIZONTAL;

        roleGbc.gridx = 0;
        rolePanel.add(kasirButton, roleGbc);

        roleGbc.gridx = 1;
        rolePanel.add(adminButton, roleGbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 15, 0);
        card.add(rolePanel, gbc);

        JLabel usernameLabel = createFieldLabel("Username");

        gbc.gridy = 4;
        card.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        usernameField.setPreferredSize(
                new Dimension(300, 42)
        );
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 230, 223)
                ),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 12, 0);
        card.add(usernameField, gbc);

        JLabel passwordLabel = createFieldLabel("Password");

        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 5, 0);
        card.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );
        passwordField.setPreferredSize(
                new Dimension(300, 42)
        );
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 230, 223)
                ),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 10, 0);
        card.add(passwordField, gbc);

        rememberCheckBox = new JCheckBox(
                "Ingat saya di perangkat ini"
        );
        rememberCheckBox.setBackground(Color.WHITE);
        rememberCheckBox.setForeground(MUTED);
        rememberCheckBox.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        rememberCheckBox.setFocusPainted(false);

        gbc.gridy = 8;
        gbc.insets = new Insets(5, 0, 15, 0);
        card.add(rememberCheckBox, gbc);

        loginButton = new JButton("Masuk");
        loginButton.setFont(
                new Font("SansSerif", Font.BOLD, 15)
        );
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(GREEN);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setPreferredSize(
                new Dimension(300, 48)
        );
        loginButton.setCursor(
                new java.awt.Cursor(
                        java.awt.Cursor.HAND_CURSOR
                )
        );
        loginButton.addActionListener(e -> handleLogin());

        gbc.gridy = 9;
        gbc.insets = new Insets(8, 0, 22, 0);
        card.add(loginButton, gbc);

        JLabel securityLabel = new JLabel(
                "Password tersimpan aman sebagai hash",
                SwingConstants.CENTER
        );
        securityLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 11)
        );
        securityLabel.setForeground(MUTED);

        JLabel versionLabel = new JLabel(
                "SmartMart POS v1.0 | Kasir diarahkan ke layar transaksi",
                SwingConstants.CENTER
        );
        versionLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 10)
        );
        versionLabel.setForeground(MUTED);

        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(securityLabel, gbc);

        gbc.gridy = 11;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(versionLabel, gbc);

        loadRememberedUsername();

        background.add(card);

        return background;
    }

    private JToggleButton createRoleButton(String text) {

        JToggleButton button = new JToggleButton(text);
        button.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(
                new Dimension(150, 38)
        );

        return button;
    }

    private void updateRoleButtons() {

        kasirButton.setBackground(
                selectedRole.equals("KASIR")
                        ? GREEN : new Color(238, 244, 240)
        );

        kasirButton.setForeground(
                selectedRole.equals("KASIR")
                        ? Color.WHITE : MUTED
        );

        adminButton.setBackground(
                selectedRole.equals("ADMIN")
                        ? GREEN : new Color(238, 244, 240)
        );

        adminButton.setForeground(
                selectedRole.equals("ADMIN")
                        ? Color.WHITE : MUTED
        );
    }

    private JLabel createFieldLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        label.setForeground(DARK_TEXT);

        return label;
    }

    // =====================================================
    // PROSES LOGIN
    // =====================================================

    private void handleLogin() {

        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();

        if (username.isEmpty() || passwordChars.length == 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Username dan password harus diisi.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
            );

            Arrays.fill(passwordChars, '\0');
            return;
        }

        loginButton.setEnabled(false);

        try {

            String password = new String(passwordChars);

            Optional<Pegawai> result =
                    authService.login(username, password);

            if (result.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Username atau password salah, atau akun tidak aktif.",
                        "Login Gagal",
                        JOptionPane.ERROR_MESSAGE
                );

                passwordField.setText("");
                passwordField.requestFocus();

                return;
            }

            Pegawai pegawai = result.get();

            // Mencatat aktivitas login
        try {
        new LogAktivitasService().catatAktivitas(
                pegawai.getIdPegawai(),
                "LOGIN",
                "Pegawai berhasil login ke sistem."
        );
        } catch (Exception logError) {
        System.err.println(
                "Gagal mencatat aktivitas login: "
                        + logError.getMessage()
        );
        }

            // Memastikan peran sesuai pilihan pada form login
            if (!selectedRole.equalsIgnoreCase(pegawai.getPeran())) {

                JOptionPane.showMessageDialog(
                        this,
                        "Akun ini tidak memiliki peran "
                                + selectedRole + ".",
                        "Akses Ditolak",
                        JOptionPane.WARNING_MESSAGE
                );

                passwordField.setText("");
                passwordField.requestFocus();

                return;
            }

            saveRememberedUsername(username);

            JOptionPane.showMessageDialog(
                    this,
                    "Selamat datang, " + pegawai.getNama() + "!",
                    "Login Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
            );

            MainFrame mainFrame = new MainFrame(pegawai);
            mainFrame.setVisible(true);

            dispose();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Terjadi kesalahan saat login:\n" + e.getMessage(),
                    "Kesalahan",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();

        } finally {

            Arrays.fill(passwordChars, '\0');
            loginButton.setEnabled(true);
        }
    }

    // =====================================================
    // INGAT USERNAME (TIDAK MENYIMPAN PASSWORD)
    // =====================================================

    private void loadRememberedUsername() {

        String savedUsername =
                preferences.get("remembered_username", "");

        if (!savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberCheckBox.setSelected(true);
        }
    }

    private void saveRememberedUsername(String username) {

        if (rememberCheckBox.isSelected()) {

            preferences.put("remembered_username", username);

        } else {

            preferences.remove("remembered_username");
        }
    }

    // =====================================================
    // PANEL GRADASI
    // =====================================================

    private static class GradientPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            GradientPaint gradient = new GradientPaint(
                    0, 0, DARK_GREEN,
                    getWidth(), getHeight(), GREEN
            );

            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Lingkaran dekoratif
            g2.setColor(new Color(160, 230, 90, 75));
            g2.fillOval(
                    getWidth() - 220, -120, 300, 300
            );

            g2.setColor(new Color(120, 210, 100, 45));
            g2.fillOval(-150, getHeight() - 180, 300, 300);

            g2.dispose();
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Login login = new Login();
            login.setVisible(true);
        });
    }
}
