package com.smartmart.ui.kasir;

import com.smartmart.model.Produk;
import com.smartmart.util.FormatRupiah;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.SwingUtilities;
import java.awt.Window;

/**
 * Dialog untuk memasukkan berat produk per kg (branch: feature/scan-search-product).
 * Pemakaian:
 *   Double kg = InputBeratDialog.tampilkan(this, produk);
 *   if (kg != null) { cart.tambah(produk, kg); }
 *
 * ASUMSI: util.FormatRupiah (milik Epi) punya method static String format(long).
 */
public class InputBeratDialog extends JDialog {

    private static final double BERAT_MAKS_KG = 100.0;

    private final Produk produk;
    private final JTextField txtBerat = new JTextField(8);
    private final JLabel lblEstimasi = new JLabel("Rp0");
    private final JLabel lblError = new JLabel(" ");
    private Double hasil = null;

    private InputBeratDialog(Window pemilik, Produk produk) {
        super(pemilik, "Input Berat", ModalityType.APPLICATION_MODAL);
        this.produk = produk;
        bangunTampilan();
        pack();
        setLocationRelativeTo(pemilik);
        setResizable(false);
    }

    /** Menampilkan dialog. Mengembalikan berat dalam kg, atau null bila dibatalkan. */
    public static Double tampilkan(Component induk, Produk produk) {
        Window pemilik = induk == null ? null : SwingUtilities.getWindowAncestor(induk);
        InputBeratDialog dialog = new InputBeratDialog(pemilik, produk);
        dialog.setVisible(true);
        return dialog.hasil;
    }

    private void bangunTampilan() {
        JPanel isi = new JPanel(new BorderLayout(0, 10));
        isi.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        JLabel judul = new JLabel("<html><b>" + produk.getNama() + "</b><br>"
                + FormatRupiah.format(produk.getHarga()) + " / kg</html>");
        isi.add(judul, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Berat (kg)"));
        form.add(txtBerat);
        form.add(new JLabel("Perkiraan harga"));
        form.add(lblEstimasi);
        lblError.setForeground(new Color(0xB00020));

        JPanel tengah = new JPanel(new BorderLayout(0, 6));
        tengah.add(form, BorderLayout.CENTER);
        tengah.add(lblError, BorderLayout.SOUTH);
        isi.add(tengah, BorderLayout.CENTER);

        JButton btnOk = new JButton("Tambah");
        JButton btnBatal = new JButton("Batal");
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tombol.add(btnBatal);
        tombol.add(btnOk);
        isi.add(tombol, BorderLayout.SOUTH);

        setContentPane(isi);
        getRootPane().setDefaultButton(btnOk);

        txtBerat.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { perbaruiEstimasi(); }
            @Override public void removeUpdate(DocumentEvent e) { perbaruiEstimasi(); }
            @Override public void changedUpdate(DocumentEvent e) { perbaruiEstimasi(); }
        });
        btnOk.addActionListener(e -> konfirmasi());
        btnBatal.addActionListener(e -> dispose());
    }

    private void perbaruiEstimasi() {
        Double berat = bacaBerat();
        lblEstimasi.setText(berat == null
                ? "Rp0" : FormatRupiah.format(Math.round(produk.getHarga() * berat)));
        lblError.setText(" ");
    }

    private void konfirmasi() {
        Double berat = bacaBerat();
        if (berat == null || berat <= 0) {
            lblError.setText("Masukkan berat yang valid (contoh: 0,75)");
            return;
        }
        if (berat > BERAT_MAKS_KG) {
            lblError.setText("Berat melebihi " + (int) BERAT_MAKS_KG + " kg");
            return;
        }
        hasil = berat;
        dispose();
    }

    /** Menerima koma atau titik sebagai pemisah desimal. Null bila kosong atau tidak valid. */
    private Double bacaBerat() {
        String teks = txtBerat.getText().trim().replace(',', '.');
        if (teks.isEmpty()) {
            return null;
        }
        try {
            double nilai = Double.parseDouble(teks);
            return Math.round(nilai * 1000.0) / 1000.0;   // presisi gram
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
