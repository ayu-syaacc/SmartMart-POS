
package com.smartmart.ui.kasir;

import com.smartmart.dao.MemberDAO;
import com.smartmart.model.Member;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MemberPanel extends JPanel {

    private MemberDAO memberDAO;

    private JTable tabelMember;
    private DefaultTableModel modelTabel;
    private JTextField txtCari;

    public MemberPanel(MemberDAO memberDAO) {

        this.memberDAO = memberDAO;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Judul halaman
        JLabel judul = new JLabel("Member Management");
        judul.setFont(new Font("Arial", Font.BOLD, 24));
        judul.setForeground(new Color(30, 41, 59));

        // Panel bagian atas
        JPanel panelAtas = new JPanel(new BorderLayout(10, 10));
        panelAtas.setOpaque(false);

        txtCari = new JTextField();
        txtCari.setPreferredSize(new Dimension(250, 35));

        JButton btnCari = new JButton("Cari");
        JButton btnTambah = new JButton("+ Tambah Member");
        btnTambah.setBackground(new Color(37, 99, 235));
        btnTambah.setForeground(Color.WHITE);

        JPanel panelCari = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelCari.setOpaque(false);

        panelCari.add(txtCari);
        panelCari.add(btnCari);
        panelCari.add(btnTambah);

        panelAtas.add(judul, BorderLayout.WEST);
        panelAtas.add(panelCari, BorderLayout.EAST);

        add(panelAtas, BorderLayout.NORTH);

        // Tabel member
        String[] kolom = {
            "ID", "Kode Member", "Nama",
            "Nomor Telepon", "Poin", "Tanggal Daftar", "Status"
        };

        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelMember = new JTable(modelTabel);
        tabelMember.setRowHeight(30);
        tabelMember.setFont(new Font("Arial", Font.PLAIN, 12));
        tabelMember.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12));
        tabelMember.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(modelTabel);

        tabelMember.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(tabelMember);
        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 230)));

        add(scrollPane, BorderLayout.CENTER);

        // Panel bawah
        JPanel panelBawah = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        panelBawah.setOpaque(false);

        JButton btnRefresh = new JButton("Refresh");
        JButton btnTukarPoin = new JButton("Tukar Poin");

        panelBawah.add(btnRefresh);
        panelBawah.add(btnTukarPoin);

        add(panelBawah, BorderLayout.SOUTH);

        // Event tombol
        btnCari.addActionListener(e ->
                sorter.setRowFilter(
                        RowFilter.regexFilter(
                                "(?i)" + txtCari.getText().trim()
                        )
                )
        );

        txtCari.addActionListener(e -> btnCari.doClick());

        btnTambah.addActionListener(e -> tambahMember());

        btnRefresh.addActionListener(e -> {
            txtCari.setText("");
            sorter.setRowFilter(null);
            tampilkanMember();
        });

        btnTukarPoin.addActionListener(e -> tukarPoin());

        // Menampilkan data awal
        tampilkanMember();
    }

    // Menampilkan seluruh data member
    public void tampilkanMember() {

        modelTabel.setRowCount(0);

        List<Member> daftar = memberDAO.ambilSemua();

        for (Member member : daftar) {

            Object[] data = {
                member.getIdMember(),
                member.getKodeMember(),
                member.getNamaMember(),
                member.getNomorTelepon(),
                member.getPoin(),
                member.getTanggalDaftar(),
                member.getStatus()
            };

            modelTabel.addRow(data);
        }
    }

    // Menambahkan member baru
    private void tambahMember() {

        JTextField txtKode = new JTextField();
        JTextField txtNama = new JTextField();
        JTextField txtTelepon = new JTextField();
        JTextField txtAlamat = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));

        form.add(new JLabel("Kode Member:"));
        form.add(txtKode);

        form.add(new JLabel("Nama Member:"));
        form.add(txtNama);

        form.add(new JLabel("Nomor Telepon:"));
        form.add(txtTelepon);

        form.add(new JLabel("Alamat:"));
        form.add(txtAlamat);

        int pilihan = JOptionPane.showConfirmDialog(
                this,
                form,
                "Tambah Member Baru",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (pilihan == JOptionPane.OK_OPTION) {

            String kode = txtKode.getText().trim();
            String nama = txtNama.getText().trim();
            String telepon = txtTelepon.getText().trim();
            String alamat = txtAlamat.getText().trim();

            if (kode.isEmpty() || nama.isEmpty()
                    || telepon.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Kode, nama, dan nomor telepon wajib diisi."
                );

                return;
            }

            if (memberDAO.cariByKode(kode) != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Kode member sudah digunakan."
                );
                return;
            }

            Member member = new Member(
                    kode,
                    nama,
                    telepon,
                    alamat,
                    0,
                    LocalDate.now(),
                    "AKTIF"
            );

            if (memberDAO.tambah(member)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Member berhasil ditambahkan."
                );

                tampilkanMember();

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Gagal menambahkan member."
                );
            }
        }
    }

    // Membuka dialog penukaran poin
    private void tukarPoin() {

        int baris = tabelMember.getSelectedRow();

        if (baris == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Pilih member terlebih dahulu."
            );
            return;
        }

        int barisModel =
                tabelMember.convertRowIndexToModel(baris);

        int idMember = Integer.parseInt(
                modelTabel.getValueAt(barisModel, 0).toString()
        );

        String kodeMember =
                modelTabel.getValueAt(barisModel, 1).toString();

        String namaMember =
                modelTabel.getValueAt(barisModel, 2).toString();

        int poin = Integer.parseInt(
                modelTabel.getValueAt(barisModel, 4).toString()
        );

        Member member = memberDAO.cariById(idMember);

//        if (member != null) {
//
//            TukarPoinDialog dialog = new TukarPoinDialog(
//                    (JFrame) SwingUtilities.getWindowAncestor(this),
//                    member,
//                    memberDAO
//            );
//
//            dialog.setVisible(true);
//
//            tampilkanMember();
//        }
    }
}
