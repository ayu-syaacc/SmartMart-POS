
package com.smartmart.ui.kasir;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StrukDialog extends JDialog {

    private JTextArea areaStruk;
    private JButton btnTutup;
    private JButton btnCetak;

    private final NumberFormat rupiah =
            NumberFormat.getCurrencyInstance(
                    new Locale("id", "ID"));

    public StrukDialog(
            JFrame parent,
            String nomorTransaksi,
            String namaKasir,
            String metodePembayaran,
            String daftarProduk,
            double subtotal,
            double diskon,
            double total,
            double jumlahBayar,
            double kembalian) {

        super(parent, "Struk Pembelian", true);

        setSize(420, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Panel utama
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Area struk
        areaStruk = new JTextArea();
        areaStruk.setFont(
                new Font("Monospaced", Font.PLAIN, 12));
        areaStruk.setEditable(false);
        areaStruk.setBackground(Color.WHITE);

        areaStruk.setText(
                buatIsiStruk(
                        nomorTransaksi,
                        namaKasir,
                        metodePembayaran,
                        daftarProduk,
                        subtotal,
                        diskon,
                        total,
                        jumlahBayar,
                        kembalian
                )
        );

        JScrollPane scroll = new JScrollPane(areaStruk);

        panel.add(scroll, BorderLayout.CENTER);

        // Panel tombol
        JPanel panelTombol = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 5));

        btnCetak = new JButton("Cetak Struk");
        btnTutup = new JButton("Tutup");

        btnCetak.addActionListener(e -> cetakStruk());

        btnTutup.addActionListener(e -> dispose());

        panelTombol.add(btnCetak);
        panelTombol.add(btnTutup);

        panel.add(panelTombol, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);
    }

    // Membuat isi struk
    private String buatIsiStruk(
            String nomorTransaksi,
            String namaKasir,
            String metodePembayaran,
            String daftarProduk,
            double subtotal,
            double diskon,
            double total,
            double jumlahBayar,
            double kembalian) {

        StringBuilder struk = new StringBuilder();

        String tanggal = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy HH:mm:ss"));

        struk.append(
                "          SMARTMART POS\n");
        struk.append(
                "       STRUK PEMBELIAN\n");
        struk.append(
                "================================\n");

        struk.append("No. Transaksi : ")
                .append(nomorTransaksi).append("\n");

        struk.append("Tanggal       : ")
                .append(tanggal).append("\n");

        struk.append("Kasir         : ")
                .append(namaKasir).append("\n");

        struk.append(
                "================================\n");

        struk.append("DAFTAR PRODUK\n");
        struk.append(
                "--------------------------------\n");

        struk.append(daftarProduk).append("\n");

        struk.append(
                "================================\n");

        struk.append("Subtotal      : ")
                .append(rupiah.format(subtotal))
                .append("\n");

        struk.append("Diskon        : ")
                .append(rupiah.format(diskon))
                .append("\n");

        struk.append("Total         : ")
                .append(rupiah.format(total))
                .append("\n");

        struk.append(
                "--------------------------------\n");

        struk.append("Metode        : ")
                .append(metodePembayaran)
                .append("\n");

        struk.append("Dibayar       : ")
                .append(rupiah.format(jumlahBayar))
                .append("\n");

        struk.append("Kembalian     : ")
                .append(rupiah.format(kembalian))
                .append("\n");

        struk.append(
                "================================\n");

        struk.append(
                "       TERIMA KASIH\n");

        struk.append(
                "     SELAMAT BERBELANJA\n");

        struk.append(
                "================================\n");

        return struk.toString();
    }

    // Mencetak struk
    private void cetakStruk() {

        try {
            boolean berhasil = areaStruk.print();

            if (berhasil) {
                JOptionPane.showMessageDialog(
                        this,
                        "Struk berhasil dicetak.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Gagal mencetak struk: "
                    + e.getMessage());
        }
    }
}
