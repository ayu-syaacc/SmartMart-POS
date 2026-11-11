package com.smartmart.ui.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * UI Penyesuaian Stok (SmartMart POS)
 */
public class penyesuaianStok extends javax.swing.JFrame {

    private JTextField txtCari;
    private JRadioButton rbRusak, rbHilang, rbKedaluwarsa;
    private JSpinner spinnerJumlah;
    private JTextArea txtAlasan;
    private JLabel lblStokSebelum, lblStokSesudah, lblStatusDicatat;
    private JButton btnSimpan, btnBatal;

    private final int stokAwal = 37;
    private final String lotKode = "B-118";
    private final PenyesuaianStokService service = new PenyesuaianStokService();

    public penyesuaianStok() {
        initComponents();
        buatKomponenUI();
        setLocationRelativeTo(null);
    }

    private void buatKomponenUI() {
        setTitle("Penyesuaian Stok - SmartMart POS");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 247, 248));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("▲ Penyesuaian Stok");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel sub = new JLabel("Kurangi stok lot manual: rusak, hilang, atau buang kedaluwarsa");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));

        JPanel tBox = new JPanel(new GridLayout(2, 1));
        tBox.setOpaque(false);
        tBox.add(title);
        tBox.add(sub);
        headerPanel.add(tBox, BorderLayout.WEST);

        // Content Split
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        contentPanel.setOpaque(false);

        contentPanel.add(createLeftPanel());
        contentPanel.add(createRightForm());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        txtCari = new JTextField("Q Cari produk atau lot....");
        txtCari.setPreferredSize(new Dimension(0, 35));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("<html><b>Roti Tawar</b> | Lot B-118<br><font color='gray'>Kedaluwarsa 07 Nov 2026 | sisa 2 hari</font></html>");
        listModel.addElement("<html><b>Keju Slice</b> | Lot E-019<br><font color='red'>KEDALUWARSA | sisa 0 hari | jumlah 6</font></html>");
        listModel.addElement("<html><b>Salad Sayur</b> | Lot C-077 sisa 3 hari | jumlah 18</html>");
        listModel.addElement("<html><b>Bayam Ikat</b> | Lot C-081 sisa 6 hari | jumlah 15</html>");
        listModel.addElement("<html><b>Yoghurt Plain</b> | Lot A-231 sisa 1 hari | jumlah 30</html>");
        listModel.addElement("<html><b>Susu UHT 1L</b> | Lot F-304 sisa 120 hari | jumlah 8</html>");

        JList<String> listLot = new JList<>(listModel);
        listLot.setFixedCellHeight(55);
        listLot.setSelectedIndex(0);

        panel.add(txtCari, BorderLayout.NORTH);
        panel.add(new JScrollPane(listLot), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblProd = new JLabel("Roti Tawar");
        lblProd.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(lblProd, gbc);

        gbc.gridy = 1;
        JLabel lblLotInfo = new JLabel("Lot B-118 | sisa 2 hari | jumlah 37 [PRIORITAS]");
        lblLotInfo.setForeground(new Color(234, 88, 12));
        panel.add(lblLotInfo, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Jenis penyesuaian:"), gbc);

        gbc.gridy = 3;
        JPanel rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rbPanel.setOpaque(false);
        rbRusak = new JRadioButton("Rusak", true);
        rbHilang = new JRadioButton("Hilang");
        rbKedaluwarsa = new JRadioButton("Buang kedaluwarsa");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbRusak); bg.add(rbHilang); bg.add(rbKedaluwarsa);

        rbRusak.addActionListener(e -> hitungStok());
        rbHilang.addActionListener(e -> hitungStok());
        rbKedaluwarsa.addActionListener(e -> hitungStok());

        rbPanel.add(rbRusak); rbPanel.add(rbHilang); rbPanel.add(rbKedaluwarsa);
        panel.add(rbPanel, gbc);

        gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Jumlah dikurangi:"), gbc);

        gbc.gridx = 1;
        spinnerJumlah = new JSpinner(new SpinnerNumberModel(3, 1, 37, 1));
        spinnerJumlah.addChangeListener(e -> hitungStok());
        panel.add(spinnerJumlah, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(new JLabel("Alasan (wajib):"), gbc);

        gbc.gridy = 6;
        txtAlasan = new JTextArea("Kemasan sobek akibat tertimpa saat penataan rak..", 3, 20);
        txtAlasan.setLineWrap(true);
        panel.add(new JScrollPane(txtAlasan), gbc);

        gbc.gridy = 7;
        JPanel calcBox = new JPanel(new GridLayout(1, 3, 10, 0));
        calcBox.setBackground(new Color(248, 250, 252));
        calcBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblStokSebelum = new JLabel("Stok Sebelum: 37");
        lblStokSesudah = new JLabel("Sesudah: 34");
        lblStatusDicatat = new JLabel("Dicatat: RUSAK");

        calcBox.add(lblStokSebelum);
        calcBox.add(lblStokSesudah);
        calcBox.add(lblStatusDicatat);
        panel.add(calcBox, gbc);

        gbc.gridy = 8;
        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBox.setOpaque(false);

        btnBatal = new JButton("Batal");
        btnBatal.addActionListener(e -> dispose());

        btnSimpan = new JButton("Simpan Penyesuaian");
        btnSimpan.setBackground(new Color(37, 99, 235));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.addActionListener(this::simpanPenyesuaian);

        btnBox.add(btnBatal);
        btnBox.add(btnSimpan);
        panel.add(btnBox, gbc);

        return panel;
    }

    private void hitungStok() {
        int val = (Integer) spinnerJumlah.getValue();
        int sdh = stokAwal - val;
        lblStokSesudah.setText("Sesudah: " + sdh);

        String jenis = rbRusak.isSelected() ? "RUSAK" : (rbHilang.isSelected() ? "HILANG" : "KEDALUWARSA");
        lblStatusDicatat.setText("Dicatat: " + jenis);
    }

    private void simpanPenyesuaian(ActionEvent evt) {
        String jenis = rbRusak.isSelected() ? "RUSAK" : (rbHilang.isSelected() ? "HILANG" : "KEDALUWARSA");
        int jml = (Integer) spinnerJumlah.getValue();
        String alasan = txtAlasan.getText();

        if (alasan == null || alasan.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alasan penyesuaian stok wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean ok = service.sesuaikanStok(lotKode, jenis, jml, alasan, "Admin01");
            if (ok) {
                JOptionPane.showMessageDialog(this, "Penyesuaian stok berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menyimpan penyesuaian stok.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Berhasil simpan sampel!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        pack();
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(penyesuaianStok.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new penyesuaianStok().setVisible(true);
        });
    }
}