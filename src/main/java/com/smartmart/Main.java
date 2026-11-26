package com.smartmart;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.smartmart.db.DatabaseInitializer;
import com.smartmart.ui.auth.Login;

public class Main {

    public static void main(String[] args) {

        try {
            // Inisialisasi database
            DatabaseInitializer.initialize();

            // Menampilkan halaman login
            SwingUtilities.invokeLater(() -> {
                Login login = new Login();
                login.setVisible(true);
            });

        } catch (Exception e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Gagal menjalankan SmartMart POS:\n" + e.getMessage(),
                    "Kesalahan Sistem",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
