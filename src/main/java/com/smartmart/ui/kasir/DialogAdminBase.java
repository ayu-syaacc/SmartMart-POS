package com.smartmart.ui.kasir;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class DialogAdminBase extends JDialog {

    public interface Pemeriksa {
        Integer verifikasi(char[] passwordAdmin);
    }

    public static final int ID_ADMIN_CONTOH = 1;

    private final PanelPersetujuanAdmin konten;
    private final Pemeriksa pemeriksa;
    private final boolean modeContoh;
    private Integer idAdmin;

    protected DialogAdminBase(Window pemilik, PanelPersetujuanAdmin konten, Pemeriksa pemeriksa) {
        this(pemilik, konten, pemeriksa, false);
    }

    protected DialogAdminBase(Window pemilik, PanelPersetujuanAdmin konten, Pemeriksa pemeriksa, boolean modeContoh) {
        super(pemilik, ModalityType.APPLICATION_MODAL);
        this.konten = konten;
        this.pemeriksa = pemeriksa;
        this.modeContoh = modeContoh;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        JPanel luar = new JPanel(new java.awt.BorderLayout());
        luar.setOpaque(false);
        luar.setBorder(new EmptyBorder(8, 12, 12, 12));
        luar.add(konten, java.awt.BorderLayout.CENTER);
        setContentPane(luar);
        pack();
        setLocationRelativeTo(pemilik);

        konten.getTombolBatal().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { tutup(null); }
        });
        java.awt.event.ActionListener setuju = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { proses(); }
        };
        konten.getTombolOk().addActionListener(setuju);
        konten.getFieldPassword().addActionListener(setuju);
        getRootPane().registerKeyboardAction(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { tutup(null); }
        }, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void proses() {
        char[] pw = konten.getFieldPassword().getPassword();
        if (pw.length == 0) {
            konten.getLabelGalat().setText("Password admin wajib diisi.");
            return;
        }
        Integer id;
        if (pemeriksa != null) {
            id = pemeriksa.verifikasi(pw);
        } else if (modeContoh) {
            id = Integer.valueOf(ID_ADMIN_CONTOH);
        } else {
            java.util.Arrays.fill(pw, '\0');
            konten.getLabelGalat().setText("Pemeriksa password admin belum terpasang. Persetujuan ditolak.");
            konten.getFieldPassword().setText("");
            konten.getFieldPassword().requestFocusInWindow();
            return;
        }
        java.util.Arrays.fill(pw, '\0');
        if (id != null) {
            tutup(id);
        } else {
            konten.getLabelGalat().setText("Password admin salah. Percobaan tercatat di log aktivitas.");
            konten.getFieldPassword().setText("");
            konten.getFieldPassword().requestFocusInWindow();
        }
    }

    private void tutup(Integer hasil) {
        idAdmin = hasil;
        dispose();
    }

    public Integer tampilkanDanTunggu() {
        idAdmin = null;
        javax.swing.JRootPane akar = null;
        java.awt.Component kacaLama = null;
        if (getOwner() instanceof javax.swing.RootPaneContainer) {
            akar = ((javax.swing.RootPaneContainer) getOwner()).getRootPane();
            kacaLama = akar.getGlassPane();
            JPanel kaca = new JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    g.setColor(new Color(8, 60, 35, 110));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            kaca.setOpaque(false);
            akar.setGlassPane(kaca);
            kaca.setVisible(true);
        }
        try {
            setVisible(true);
        } finally {
            if (akar != null) {
                akar.setGlassPane(kacaLama);
                kacaLama.setVisible(false);
            }
        }
        return idAdmin;
    }

    public static Window jendelaDari(Component c) {
        return c == null ? null : javax.swing.SwingUtilities.getWindowAncestor(c);
    }
}
