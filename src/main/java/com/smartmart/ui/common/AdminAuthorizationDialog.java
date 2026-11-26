
package com.smartmart.ui.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.smartmart.model.Pegawai;
import com.smartmart.service.AuthService;

public class AdminAuthorizationDialog extends JDialog {

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    private final AuthService authService;

    private Pegawai adminTerotorisasi;

    public AdminAuthorizationDialog(Window owner) {
        super(owner, "Otorisasi Admin", Dialog.ModalityType.APPLICATION_MODAL);

        authService = new AuthService();

        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);

        setLayout(new BorderLayout(10, 10));

        // Panel informasi.
        JLabel title = new JLabel(
                "Masukkan akun Admin untuk memberikan otorisasi."
        );

        JPanel informationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        informationPanel.add(title);

        // Panel input.
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        inputPanel.add(new JLabel("Username Admin:"));
        inputPanel.add(usernameField);

        inputPanel.add(new JLabel("Password Admin:"));
        inputPanel.add(passwordField);

        // Panel tombol.
        JButton authorizeButton = new JButton("Verifikasi");
        JButton cancelButton = new JButton("Batal");

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttonPanel.add(cancelButton);
        buttonPanel.add(authorizeButton);

        add(informationPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Aksi tombol.
        authorizeButton.addActionListener(e -> verifyAdmin());

        cancelButton.addActionListener(e -> {
            adminTerotorisasi = null;
            dispose();
        });

        getRootPane().setDefaultButton(authorizeButton);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setSize(430, 190);
        setLocationRelativeTo(owner);
    }

    /**
     * Menampilkan dialog dan mengembalikan data Admin
     * apabila proses otorisasi berhasil.
     *
     * Mengembalikan null jika dibatalkan atau gagal.
     */
    public static Pegawai mintaOtorisasi(Component parent) {

        Window owner = parent == null
                ? null
                : SwingUtilities.getWindowAncestor(parent);

        AdminAuthorizationDialog dialog =
                new AdminAuthorizationDialog(owner);

        dialog.setVisible(true);

        return dialog.adminTerotorisasi;
    }

    /**
     * Memverifikasi username dan password Admin.
     */
    private void verifyAdmin() {

        String username = usernameField.getText().trim();
        char[] passwordChars = passwordField.getPassword();

        if (username.isEmpty() || passwordChars.length == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username dan password wajib diisi.",
                    "Validasi",
                    JOptionPane.WARNING_MESSAGE
            );

            Arrays.fill(passwordChars, '\0');
            return;
        }

        String password = new String(passwordChars);
        Arrays.fill(passwordChars, '\0');

        try {

            Optional<Pegawai> hasil =
                    authService.authorizeAdmin(username, password);

            if (hasil.isPresent()) {

                adminTerotorisasi = hasil.get();

                JOptionPane.showMessageDialog(
                        this,
                        "Otorisasi Admin berhasil.",
                        "Berhasil",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Username atau password salah, "
                                + "atau akun bukan Admin yang aktif.",
                        "Otorisasi Ditolak",
                        JOptionPane.ERROR_MESSAGE
                );

                passwordField.setText("");
                passwordField.requestFocus();

            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Terjadi kesalahan saat memverifikasi Admin.",
                    "Kesalahan",
                    JOptionPane.ERROR_MESSAGE
            );

            ex.printStackTrace();

            passwordField.setText("");

        }
    }
}
