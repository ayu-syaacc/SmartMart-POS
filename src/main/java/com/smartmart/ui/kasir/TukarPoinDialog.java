
package com.smartmart.ui.kasir;

import com.smartmart.dao.MemberDAO;
import com.smartmart.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TukarPoinDialog extends JDialog {

    private Member member;
    private MemberDAO memberDAO;

    private JLabel lblNama;
    private JLabel lblPoin;
    private JLabel lblNilai;

    private JSpinner spnPoin;
    private JButton btnTukar;
    private JButton btnBatal;

    private final int POIN_MINIMUM = 100;
    private final int NILAI_POIN = 5000;

    public TukarPoinDialog(
            JFrame parent,
            Member member,
            MemberDAO memberDAO) {

        super(parent, "Tukar Poin Member", true);

        this.member = member;
        this.memberDAO = memberDAO;

        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Panel utama
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel judul = new JLabel("Penukaran Poin");
        judul.setFont(new Font("Arial", Font.BOLD, 22));

        lblNama = new JLabel(
                "Nama Member: " + member.getNamaMember());

        lblPoin = new JLabel(
                "Poin Tersedia: " + member.getPoin());

        lblNilai = new JLabel(
                "Nilai Penukaran: Rp0");

        JLabel labelPoin = new JLabel(
                "Jumlah Poin yang Ditukar:");

        int poinTersedia = member.getPoin();

        spnPoin = new JSpinner(
                new SpinnerNumberModel(
                        100,
                        100,
                        Math.max(100, poinTersedia / 100 * 100),
                        100
                )
        );

        // Memperbarui nilai penukaran
        spnPoin.addChangeListener(e -> hitungNilai());

        btnTukar = new JButton("Tukar Poin");
        btnBatal = new JButton("Batal");

        btnTukar.setBackground(new Color(37, 99, 235));
        btnTukar.setForeground(Color.WHITE);

        panel.add(judul);
        panel.add(lblNama);
        panel.add(lblPoin);
        panel.add(labelPoin);
        panel.add(spnPoin);
        panel.add(lblNilai);

        JPanel panelTombol = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        panelTombol.setBackground(Color.WHITE);
        panelTombol.add(btnBatal);
        panelTombol.add(btnTukar);

        add(panel, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);

        btnTukar.addActionListener(e -> prosesTukar());
        btnBatal.addActionListener(e -> dispose());

        // Jika poin kurang dari 100
        if (poinTersedia < POIN_MINIMUM) {
            btnTukar.setEnabled(false);
        }

        hitungNilai();
    }

    // Menghitung nilai rupiah poin
    private void hitungNilai() {

        int jumlahPoin =
                (Integer) spnPoin.getValue();

        int nilai =
                (jumlahPoin / 100) * NILAI_POIN;

        lblNilai.setText(
                String.format(
                        "Nilai Penukaran: Rp%,d",
                        nilai
                )
        );
    }

    // Memproses penukaran poin
    private void prosesTukar() {

        int jumlahPoin =
                (Integer) spnPoin.getValue();

        if (jumlahPoin < POIN_MINIMUM) {

            JOptionPane.showMessageDialog(
                    this,
                    "Minimal penukaran adalah 100 poin."
            );

            return;
        }

        // Mengambil data terbaru dari database
        Member dataTerbaru =
                memberDAO.cariById(member.getIdMember());

        if (dataTerbaru == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Data member tidak ditemukan."
            );

            return;
        }

        if (jumlahPoin > dataTerbaru.getPoin()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Poin member tidak mencukupi."
            );

            return;
        }

        int nilai =
                (jumlahPoin / 100) * NILAI_POIN;

        int konfirmasi = JOptionPane.showConfirmDialog(
                this,
                "Tukar " + jumlahPoin
                + " poin menjadi potongan Rp"
                + String.format("%,d", nilai) + "?",
                "Konfirmasi Penukaran",
                JOptionPane.YES_NO_OPTION
        );

        if (konfirmasi != JOptionPane.YES_OPTION) {
            return;
        }

        // Mengurangi poin
        int sisaPoin =
                dataTerbaru.getPoin() - jumlahPoin;

        boolean berhasil = memberDAO.ubahPoin(
                dataTerbaru.getIdMember(),
                sisaPoin
        );

        if (berhasil) {

            member.setPoin(sisaPoin);

            JOptionPane.showMessageDialog(
                    this,
                    "Penukaran poin berhasil!\n"
                    + "Potongan harga: Rp"
                    + String.format("%,d", nilai)
                    + "\nSisa poin: " + sisaPoin
            );

            dispose();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Penukaran poin gagal."
            );
        }
    }
}
