package com.smartmart.ui.kasir;

import com.smartmart.model.Debit;
import com.smartmart.model.QRIS;
import com.smartmart.model.SmartCart;
import com.smartmart.model.Tunai;
import com.smartmart.service.PenjualanService;
import com.smartmart.service.PenjualanService.MemberInfo;
import com.smartmart.util.FormatRupiah;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;

/**
 * Dialog pembayaran kasir: member (opsional), tukar poin, dan metode Tunai / QRIS / Debit.
 * Pemeriksaan pembayaran memakai kelas model Tunai, QRIS, dan Debit.
 * Hanya mengembalikan data; penyimpanan dilakukan PenjualanService.
 */
public class PembayaranDialog extends JDialog {

    private final SmartCart cart;
    private final PenjualanService service = new PenjualanService();
    private final int persenAwal;

    private MemberInfo member;
    private PenjualanService.Pembayaran hasil;

    private final JLabel lblSubtotal = nilai();
    private final JLabel lblHemat = nilai();
    private final JLabel lblPoin = nilai();
    private final JLabel lblTotal = new JLabel();
    private final JLabel lblMember = UiKit.lbl(" ", Font.PLAIN, 12, UiKit.MUTED);
    private final JLabel lblKembalian = nilai();

    private final JTextField txtMember = new JTextField(12);
    private final JSpinner spnPoin = new JSpinner(new SpinnerNumberModel(0, 0, 0, PenjualanService.POIN_PER_TUKAR));
    private final JComboBox<String> cmbMetode = new JComboBox<>(new String[]{"Tunai", "QRIS", "Debit"});
    private final JTextField txtUang = new JTextField(14);
    private final JTextField txtReferensi = new JTextField(14);
    private final JTextField txtKartu = new JTextField(16);
    private final JTextField txtNamaKartu = new JTextField(16);
    private final JPanel kartuMetode = new JPanel(new CardLayout());

    private PembayaranDialog(Window parent, SmartCart cart) {
        super(parent, "Pembayaran", ModalityType.APPLICATION_MODAL);
        this.cart = cart;
        this.persenAwal = cart.getPersenMember();
        bangun();
        segarkan();
        pack();
        setLocationRelativeTo(parent);
    }

    /** Menampilkan dialog. Mengembalikan null bila dibatalkan (diskon member dikembalikan seperti awal). */
    public static PenjualanService.Pembayaran tampilkan(Window parent, SmartCart cart) {
        PembayaranDialog d = new PembayaranDialog(parent, cart);
        d.setVisible(true);
        if (d.hasil == null) {
            cart.setPersenMember(d.persenAwal);
        }
        return d.hasil;
    }

    private static JLabel nilai() {
        JLabel l = UiKit.lbl(" ", Font.PLAIN, 13, UiKit.TEXT);
        l.setHorizontalAlignment(JLabel.RIGHT);
        return l;
    }

    private void bangun() {
        JPanel isi = new JPanel(new GridBagLayout());
        isi.setBackground(Color.WHITE);
        isi.setBorder(BorderFactory.createEmptyBorder(22, 26, 18, 26));
        int y = 0;

        isi.add(UiKit.lbl("Pembayaran", Font.BOLD, 22, UiKit.TEXT), kolom(0, y++, 2, 0, 12));

        isi.add(UiKit.lbl("Subtotal", Font.PLAIN, 13, UiKit.MUTED), kolom(0, y, 1, 0, 2));
        isi.add(lblSubtotal, kolom(1, y++, 1, 0, 2));
        isi.add(UiKit.lbl("Hemat (member / FreshSave)", Font.PLAIN, 13, UiKit.MUTED), kolom(0, y, 1, 0, 2));
        isi.add(lblHemat, kolom(1, y++, 1, 0, 2));
        isi.add(UiKit.lbl("Tukar poin", Font.PLAIN, 13, UiKit.MUTED), kolom(0, y, 1, 0, 2));
        isi.add(lblPoin, kolom(1, y++, 1, 0, 2));
        isi.add(UiKit.lbl("TOTAL", Font.BOLD, 16, UiKit.TEXT), kolom(0, y, 1, 0, 14));
        lblTotal.setFont(UiKit.font(Font.BOLD, 20));
        lblTotal.setForeground(UiKit.GREEN);
        lblTotal.setHorizontalAlignment(JLabel.RIGHT);
        isi.add(lblTotal, kolom(1, y++, 1, 0, 14));

        isi.add(UiKit.lbl("Nomor member (opsional)", Font.BOLD, 12, UiKit.TEXT), kolom(0, y++, 2, 0, 2));
        JPanel barisMember = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        barisMember.setOpaque(false);
        barisMember.add(txtMember);
        UiKit.RoundButton cek = UiKit.tombolAbu("Cek");
        cek.setPreferredSize(new Dimension(70, 32));
        cek.setFont(UiKit.font(Font.BOLD, 12));
        cek.addActionListener(e -> cekMember());
        txtMember.addActionListener(e -> cekMember());
        barisMember.add(cek);
        isi.add(barisMember, kolom(0, y++, 2, 0, 0));
        isi.add(lblMember, kolom(0, y++, 2, 0, 6));

        isi.add(UiKit.lbl("Tukar poin (kelipatan 100 = Rp5.000)", Font.BOLD, 12, UiKit.TEXT), kolom(0, y, 1, 0, 2));
        isi.add(spnPoin, kolom(1, y++, 1, 0, 2));
        spnPoin.setEnabled(false);
        spnPoin.addChangeListener(e -> segarkan());

        isi.add(UiKit.lbl("Metode pembayaran", Font.BOLD, 12, UiKit.TEXT), kolom(0, y, 1, 0, 12));
        isi.add(cmbMetode, kolom(1, y++, 1, 0, 12));

        kartuMetode.setOpaque(false);
        kartuMetode.add(baris("Uang diterima (Rp)", txtUang, "Kembalian", lblKembalian), "Tunai");
        kartuMetode.add(baris("Nomor referensi QRIS", txtReferensi, null, null), "QRIS");
        kartuMetode.add(baris("Nomor kartu (16 digit)", txtKartu, "Nama pemegang kartu", txtNamaKartu), "Debit");
        isi.add(kartuMetode, kolom(0, y++, 2, 0, 12));
        cmbMetode.addActionListener(e -> {
            ((CardLayout) kartuMetode.getLayout()).show(kartuMetode, (String) cmbMetode.getSelectedItem());
            pack();
            segarkan();
        });
        txtUang.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { segarkan(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { segarkan(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { segarkan(); }
        });

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        UiKit.RoundButton batal = UiKit.tombolAbu("Batal");
        batal.setPreferredSize(new Dimension(110, 42));
        batal.addActionListener(e -> dispose());
        UiKit.RoundButton proses = UiKit.tombolHijau("Proses Pembayaran", null);
        proses.setPreferredSize(new Dimension(190, 42));
        proses.addActionListener(e -> proses());
        tombol.add(batal);
        tombol.add(proses);
        isi.add(tombol, kolom(0, y++, 2, 0, 0));

        setContentPane(isi);
        getRootPane().setDefaultButton(null);
    }

    private JPanel baris(String judul1, JComponent f1, String judul2, JComponent f2) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(UiKit.lbl(judul1, Font.BOLD, 12, UiKit.TEXT), kolom(0, 0, 1, 0, 2));
        p.add(f1, kolom(1, 0, 1, 0, 2));
        if (judul2 != null) {
            p.add(UiKit.lbl(judul2, Font.BOLD, 12, UiKit.TEXT), kolom(0, 1, 1, 0, 2));
            p.add(f2, kolom(1, 1, 1, 0, 2));
        }
        return p;
    }

    private static GridBagConstraints kolom(int x, int y, int lebar, double wx, int bawah) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = lebar;
        c.weightx = lebar == 2 ? 1 : (x == 0 ? 1 : 0);
        c.fill = lebar == 2 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.anchor = x == 1 ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, bawah, x == 0 && lebar == 1 ? 14 : 0);
        return c;
    }

    // ----- logika -----

    private void cekMember() {
        String no = txtMember.getText().trim();
        if (no.isEmpty()) {
            member = null;
            cart.setPersenMember(persenAwal);
            lblMember.setText(" ");
            spnPoin.setValue(0);
            aturBatasPoin();
            segarkan();
            return;
        }
        try {
            MemberInfo m = service.cariMember(no);
            if (m == null) {
                member = null;
                cart.setPersenMember(persenAwal);
                lblMember.setForeground(UiKit.RED_TEXT);
                lblMember.setText("Member \"" + no + "\" tidak ditemukan.");
            } else {
                member = m;
                cart.setPersenMember(PenjualanService.PERSEN_MEMBER);
                lblMember.setForeground(UiKit.GREEN);
                lblMember.setText(m.nama + " (" + m.noMember + ") | Poin " + m.poin
                        + " | Diskon member " + PenjualanService.PERSEN_MEMBER + "%");
            }
        } catch (SQLException ex) {
            lblMember.setForeground(UiKit.RED_TEXT);
            lblMember.setText("Gagal membaca data member: " + ex.getMessage());
            member = null;
        }
        spnPoin.setValue(0);
        aturBatasPoin();
        segarkan();
    }

    private void aturBatasPoin() {
        int maks = PenjualanService.poinMaksimum(member, cart.getTotal());
        SpinnerNumberModel m = (SpinnerNumberModel) spnPoin.getModel();
        m.setMaximum(maks);
        spnPoin.setEnabled(maks > 0);
    }

    private int poinDitukar() {
        return (Integer) spnPoin.getValue();
    }

    private long diskonPoin() {
        return (poinDitukar() / PenjualanService.POIN_PER_TUKAR) * PenjualanService.NILAI_TUKAR;
    }

    private long totalAkhir() {
        return cart.getTotal() - diskonPoin();
    }

    private void segarkan() {
        lblSubtotal.setText(FormatRupiah.format(cart.getSubtotal()));
        lblHemat.setText("-" + FormatRupiah.format(cart.getTotalHemat()));
        lblPoin.setText("-" + FormatRupiah.format(diskonPoin()));
        lblTotal.setText(FormatRupiah.format(totalAkhir()));
        long uang = uangDiterima();
        lblKembalian.setText(uang >= totalAkhir() && uang > 0 ? FormatRupiah.format(uang - totalAkhir()) : "-");
    }

    private long uangDiterima() {
        try {
            return FormatRupiah.parse(txtUang.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void proses() {
        long total = totalAkhir();
        String metode = (String) cmbMetode.getSelectedItem();
        PenjualanService.Pembayaran p = new PenjualanService.Pembayaran();
        p.metode = metode;
        p.member = member;
        p.poinDitukar = poinDitukar();

        boolean berhasil;
        String gagal;
        if ("Tunai".equals(metode)) {
            long uang = uangDiterima();
            if (uang <= 0) {
                peringatan("Masukkan jumlah uang yang diterima, contoh: 100000");
                return;
            }
            Tunai tunai = new Tunai(0, uang);
            berhasil = tunai.prosesPembayaran(total);
            gagal = "Uang yang diterima kurang dari total " + FormatRupiah.format(total) + ".";
            p.dibayar = uang;
        } else if ("QRIS".equals(metode)) {
            QRIS qris = new QRIS(0, total);
            qris.setNomorReferensi(txtReferensi.getText());
            if (txtReferensi.getText().trim().isEmpty()) {
                peringatan("Nomor referensi QRIS wajib diisi.");
                return;
            }
            qris.setTerkonfirmasi(konfirmasi("Apakah pembayaran QRIS sudah diterima?", "Konfirmasi QRIS"));
            berhasil = qris.prosesPembayaran(total);
            gagal = "Pembayaran QRIS belum dikonfirmasi.";
            p.dibayar = total;
            p.referensi = txtReferensi.getText().trim();
        } else {
            Debit debit = new Debit(0, total, txtKartu.getText().trim(), txtNamaKartu.getText().trim());
            if (!txtKartu.getText().trim().matches("\\d{16}") || txtNamaKartu.getText().trim().isEmpty()) {
                peringatan("Isi nomor kartu 16 digit dan nama pemegang kartu.");
                return;
            }
            debit.setTerkonfirmasi(konfirmasi("Apakah pembayaran debit berhasil?", "Konfirmasi Debit"));
            berhasil = debit.prosesPembayaran(total);
            gagal = "Pembayaran debit belum dikonfirmasi.";
            p.dibayar = total;
        }

        if (!berhasil) {
            peringatan(gagal);
            return;
        }
        hasil = p;
        dispose();
    }

    private boolean konfirmasi(String pesan, String judul) {
        return JOptionPane.showConfirmDialog(this, pesan, judul, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void peringatan(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Pembayaran", JOptionPane.WARNING_MESSAGE);
    }
}
