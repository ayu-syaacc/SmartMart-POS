
package com.smartmart.ui.kasir;

import com.smartmart.model.Tunai;
import com.smartmart.model.QRIS;
import com.smartmart.model.Debit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PembayaranPanel extends JPanel {

    private JTextField txtTotal;
    private JTextField txtJumlahBayar;
    private JTextField txtNomorReferensi;
    private JTextField txtNomorKartu;
    private JTextField txtNamaKartu;

    private JComboBox<String> cmbMetode;
    private JLabel lblKembalian;
    private JLabel lblStatus;

    private JButton btnProses;

    private int idTransaksi;
    private Tunai pembayaranTunai;
    private QRIS pembayaranQRIS;
    private Debit pembayaranDebit;

    public PembayaranPanel(int idTransaksi, double total) {

        this.idTransaksi = idTransaksi;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Judul halaman
        JLabel judul = new JLabel("Pembayaran");
        judul.setFont(new Font("Arial", Font.BOLD, 24));

        add(judul, BorderLayout.NORTH);

        // Panel utama
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        txtTotal = new JTextField(
                String.format("%.0f", total));
        txtTotal.setEditable(false);

        txtJumlahBayar = new JTextField();

        cmbMetode = new JComboBox<>(
                new String[]{"TUNAI", "QRIS", "DEBIT"});

        txtNomorReferensi = new JTextField();
        txtNomorKartu = new JTextField();
        txtNamaKartu = new JTextField();

        lblKembalian = new JLabel("Rp0");
        lblStatus = new JLabel("Menunggu pembayaran");

        btnProses = new JButton("Proses Pembayaran");

        panel.add(new JLabel("Total Tagihan"));
        panel.add(txtTotal);

        panel.add(new JLabel("Metode Pembayaran"));
        panel.add(cmbMetode);

        panel.add(new JLabel("Jumlah Bayar"));
        panel.add(txtJumlahBayar);

        panel.add(new JLabel("Nomor Referensi QRIS"));
        panel.add(txtNomorReferensi);

        panel.add(new JLabel("Nomor Kartu Debit (Demo)"));
        panel.add(txtNomorKartu);

        panel.add(new JLabel("Nama Pemegang Kartu"));
        panel.add(txtNamaKartu);

        panel.add(new JLabel("Uang Kembalian"));
        panel.add(lblKembalian);

        panel.add(btnProses);
        panel.add(lblStatus);

        add(panel, BorderLayout.CENTER);

        // Mengubah tampilan sesuai metode
        cmbMetode.addActionListener(e -> ubahMetode());

        // Tombol pembayaran
        btnProses.addActionListener(e -> prosesPembayaran());

        ubahMetode();
    }

    private void ubahMetode() {

        String metode =
                cmbMetode.getSelectedItem().toString();

        txtJumlahBayar.setVisible(
                metode.equals("TUNAI"));

        txtNomorReferensi.setVisible(
                metode.equals("QRIS"));

        txtNomorKartu.setVisible(
                metode.equals("DEBIT"));

        txtNamaKartu.setVisible(
                metode.equals("DEBIT"));

        lblKembalian.setText("Rp0");
        lblStatus.setText("Menunggu pembayaran");
    }

    private void prosesPembayaran() {

        try {

            double total =
                    Double.parseDouble(txtTotal.getText());

            String metode =
                    cmbMetode.getSelectedItem().toString();

            boolean berhasil = false;

            if (metode.equals("TUNAI")) {

                double uang = Double.parseDouble(
                        txtJumlahBayar.getText());

                pembayaranTunai =
                        new Tunai(idTransaksi, uang);

                berhasil =
                        pembayaranTunai.prosesPembayaran(total);

                if (berhasil) {
                    lblKembalian.setText(
                            String.format("Rp%.0f",
                            pembayaranTunai.getUangKembalian()));
                }

            } else if (metode.equals("QRIS")) {

                pembayaranQRIS =
                        new QRIS(idTransaksi, total);

                pembayaranQRIS.setNomorReferensi(
                        txtNomorReferensi.getText());

                int konfirmasi = JOptionPane.showConfirmDialog(
                        this,
                        "Apakah pembayaran QRIS sudah diterima?",
                        "Konfirmasi QRIS",
                        JOptionPane.YES_NO_OPTION
                );

                pembayaranQRIS.setTerkonfirmasi(
                        konfirmasi == JOptionPane.YES_OPTION);

                berhasil =
                        pembayaranQRIS.prosesPembayaran(total);

            } else if (metode.equals("DEBIT")) {

                pembayaranDebit = new Debit(
                        idTransaksi,
                        total,
                        txtNomorKartu.getText(),
                        txtNamaKartu.getText()
                );

                int konfirmasi = JOptionPane.showConfirmDialog(
                        this,
                        "Apakah pembayaran debit berhasil?",
                        "Konfirmasi Debit",
                        JOptionPane.YES_NO_OPTION
                );

                pembayaranDebit.setTerkonfirmasi(
                        konfirmasi == JOptionPane.YES_OPTION);

                berhasil =
                        pembayaranDebit.prosesPembayaran(total);
            }

            if (berhasil) {

                lblStatus.setText(
                        "Pembayaran berhasil!");

                lblStatus.setForeground(
                        new Color(22, 163, 74));

                JOptionPane.showMessageDialog(
                        this,
                        "Pembayaran berhasil dilakukan!"
                );

            } else {

                lblStatus.setText(
                        "Pembayaran belum berhasil.");

                lblStatus.setForeground(Color.RED);

                JOptionPane.showMessageDialog(
                        this,
                        "Pembayaran gagal atau belum dikonfirmasi."
                );
            }

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Masukkan jumlah pembayaran dengan benar."
            );
        }
    }
}
