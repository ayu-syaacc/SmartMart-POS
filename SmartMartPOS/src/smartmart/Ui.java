package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/** Komponen & utilitas tampilan bersama. */
final class Ui {
    private Ui() { }

    static final Color PRIMARY = new Color(0x0F766E), PRIMARY_DK = new Color(0x115E59), PRIMARY_LT = new Color(0xCCFBF1),
            BG = new Color(0xF3F5F7), TEXT = new Color(0x1F2937), MUTED = new Color(0x6B7280),
            BORDER = new Color(0xE5E7EB), DANGER = new Color(0xDC2626), OK = new Color(0x16A34A),
            WARN = new Color(0xD97706), ORANGE = new Color(0xEA580C), SIDEBAR = new Color(0x0B3B3A);

    static Font f(int style, int size) { return new Font(Font.SANS_SERIF, style, size); }

    // ------------------------------------------------------------ format
    private static final DecimalFormatSymbols SYM = new DecimalFormatSymbols(Db.ID);

    static String num(long v) { return new DecimalFormat("#,##0", SYM).format(v); }

    static String rp(long v) { return v < 0 ? "-Rp" + num(-v) : "Rp" + num(v); }

    static String qty(double q) { return new DecimalFormat("0.###", SYM).format(q); }

    static double parseQty(String s) {
        try { return Double.parseDouble(s.trim().replace(',', '.')); } catch (Exception e) { return -1; }
    }

    static long parseMoney(String s) {
        String d = s == null ? "" : s.replaceAll("[^0-9]", "");
        if (d.isEmpty() || d.length() > 12) return 0;
        return Long.parseLong(d);
    }

    // ------------------------------------------------------- komponen dasar
    static JLabel label(String text, int style, int size, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f(style, size));
        l.setForeground(c);
        return l;
    }

    static JLabel muted(String text) { return label(text, Font.PLAIN, 12, MUTED); }

    static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));
        return p;
    }

    /** Panel transparan yang tidak melar vertikal di dalam BoxLayout. */
    static class Row extends JPanel {
        Row(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        @Override public Dimension getMaximumSize() {
            return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
        }
    }

    static JPanel transparent(LayoutManager lm) { return new Row(lm); }

    static Color shade(Color c, double f) {
        return new Color(Math.max(0, (int) (c.getRed() * f)), Math.max(0, (int) (c.getGreen() * f)), Math.max(0, (int) (c.getBlue() * f)));
    }

    static class Btn extends JButton {
        private final Color bg, fg;
        private final boolean outline;
        private boolean hov;

        Btn(String text, Color bg, Color fg, boolean outline) {
            super(text);
            this.bg = bg;
            this.fg = fg;
            this.outline = outline;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setForeground(fg);
            setFont(f(Font.BOLD, 13));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hov = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hov = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xE5E7EB) : (hov ? shade(bg, outline ? 0.95 : 0.88) : bg);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            if (outline) {
                g2.setColor(BORDER.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();
            setForeground(isEnabled() ? fg : new Color(0x9CA3AF));
            super.paintComponent(g);
        }
    }

    static Btn compact(Btn b) {
        b.setBorder(new EmptyBorder(9, 6, 9, 6));
        return b;
    }

    static Btn primary(String t) { return new Btn(t, PRIMARY, Color.WHITE, false); }
    static Btn danger(String t) { return new Btn(t, DANGER, Color.WHITE, false); }
    static Btn plain(String t) { return new Btn(t, Color.WHITE, TEXT, true); }
    static Btn orange(String t) { return new Btn(t, ORANGE, Color.WHITE, false); }

    static JToggleButton chip(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? PRIMARY : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                if (!isSelected()) {
                    g2.setColor(BORDER.darker());
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                }
                g2.dispose();
                setForeground(isSelected() ? Color.WHITE : TEXT);
                super.paintComponent(g);
            }
        };
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setFont(f(Font.BOLD, 12));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    static JLabel badge(String text, Color c) {
        JLabel l = new JLabel(text, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(f(Font.BOLD, 11));
        l.setForeground(c);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

    static JTextField field(int cols) {
        JTextField t = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Object hint = getClientProperty("JTextField.placeholderText");
                if (hint != null && getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(0x9CA3AF));
                    g2.setFont(getFont());
                    Insets in = getInsets();
                    g2.drawString(hint.toString(), in.left, in.top + g2.getFontMetrics().getAscent());
                    g2.dispose();
                }
            }
        };
        t.setFont(f(Font.PLAIN, 14));
        t.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER.darker(), 1, true), new EmptyBorder(6, 8, 6, 8)));
        return t;
    }

    static class StatCard extends JPanel {
        private final JLabel value = label("-", Font.BOLD, 24, TEXT);
        private final JLabel sub = label(" ", Font.PLAIN, 12, MUTED);

        StatCard(String title, Color accent) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));
            JLabel t = label(title, Font.PLAIN, 12, MUTED);
            value.setForeground(accent);
            add(t);
            add(Box.createVerticalStrut(4));
            add(value);
            add(Box.createVerticalStrut(2));
            add(sub);
        }

        void set(String v, String s) {
            value.setText(v);
            sub.setText(s);
        }
    }

    // ---------------------------------------------------------------- tabel
    static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    static Color statusColor(String s) {
        if (s == null) return null;
        switch (s) {
            case "SELESAI": case "AMAN": case "MASUK": case "DIRETUR": case "Dapat diretur": case "Aktif": case "PAID":
                return OK;
            case "DIBATALKAN": case "KEDALUWARSA": case "HILANG": case "BLOKIR": case "DIBLOKIR": case "HABIS": case "Nonaktif":
                return DANGER;
            case "SEGERA": case "PRIORITAS": case "FRESHSAVE": case "Diskon FreshSave":
                return ORANGE;
            case "PERHATIAN": case "MENIPIS": case "RUSAK": case "PENYESUAIAN": case "MEMBER":
                return WARN;
            case "ADMIN": case "KASIR": case "TERJUAL":
                return PRIMARY;
            default:
                return null;
        }
    }

    static class CellR extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, false, r, c);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            String s = v == null ? "" : v.toString();
            Color sc = statusColor(s);
            setFont(f(sc != null ? Font.BOLD : Font.PLAIN, 13));
            setForeground(sc != null ? sc : (s.startsWith("-") && s.length() > 1 && Character.isDigit(s.charAt(1)) ? DANGER : TEXT));
            boolean numeric = s.matches("[-+]?(Rp)?-?[0-9][0-9.,]*( kg)?");
            setHorizontalAlignment(numeric ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBackground(sel ? PRIMARY_LT : Color.WHITE);
            return this;
        }
    }

    static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(BORDER);
        t.setFont(f(Font.PLAIN, 13));
        t.setSelectionBackground(PRIMARY_LT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JTableHeader h = t.getTableHeader();
        h.setFont(f(Font.BOLD, 12));
        h.setBackground(new Color(0xF9FAFB));
        h.setForeground(MUTED);
        h.setPreferredSize(new Dimension(0, 32));
        h.setReorderingAllowed(false);
        t.setDefaultRenderer(Object.class, new CellR());
        return t;
    }

    static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
    }

    // --------------------------------------------------------------- dialog
    static void info(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "SmartMart POS", JOptionPane.INFORMATION_MESSAGE); }

    static void err(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Tidak dapat diproses", JOptionPane.WARNING_MESSAGE); }

    static boolean ask(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p, msg, "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Dialog persetujuan admin (dipakai retur, void, dll). Mengembalikan admin yang menyetujui atau null. */
    static User askAdmin(Component parent, String title, String detail) {
        JPasswordField pw = new JPasswordField(18);
        pw.setFont(f(Font.PLAIN, 14));
        JLabel err = label(" ", Font.PLAIN, 12, DANGER);
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.add(new JLabel("<html><b>" + title + "</b><br>" + detail + "</html>"), BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 4));
        mid.add(new JLabel("Password admin"), BorderLayout.NORTH);
        mid.add(pw, BorderLayout.CENTER);
        p.add(mid, BorderLayout.CENTER);
        p.add(err, BorderLayout.SOUTH);
        pw.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) { pw.requestFocusInWindow(); }
            @Override public void ancestorRemoved(AncestorEvent e) { }
            @Override public void ancestorMoved(AncestorEvent e) { }
        });
        while (true) {
            pw.setText("");
            int r = JOptionPane.showConfirmDialog(parent, p, "Persetujuan Admin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) return null;
            User u = Db.get().verifyAdmin(new String(pw.getPassword()));
            if (u != null) return u;
            err.setText("Password salah. Percobaan tercatat di log aktivitas.");
        }
    }

    /** Tombol angka (numpad) yang mengisi sebuah JTextField. */
    static JPanel numpad(JTextField target, boolean comma) {
        JPanel p = transparent(new GridLayout(4, 3, 6, 6));
        String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", comma ? "," : "00"};
        for (String k : keys) {
            Btn b = plain(k);
            b.setFont(f(Font.BOLD, 16));
            b.addActionListener(e -> {
                String t = target.getText();
                if (k.equals("C")) target.setText("");
                else if (k.equals(",")) { if (!t.contains(",")) target.setText(t.isEmpty() ? "0," : t + ","); }
                else target.setText(t + k);
            });
            p.add(b);
        }
        return p;
    }

    static javax.swing.event.DocumentListener onChange(Runnable r) {
        return new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        };
    }

    // -------------------------------------------------------------- PDF
    /** Menulis teks monospace ASCII ke PDF 1 halaman (tanpa library tambahan). */
    static void savePdf(File file, String text) throws IOException {
        String[] lines = text.split("\n");
        int lh = 12;
        int w = 260, h = Math.max(200, lines.length * lh + 50);
        StringBuilder cs = new StringBuilder("BT /F1 10 Tf " + lh + " TL 20 " + (h - 30) + " Td\n");
        for (String ln : lines) {
            StringBuilder e = new StringBuilder();
            for (char c : ln.toCharArray()) {
                if (c == '(' || c == ')' || c == '\\') e.append('\\').append(c);
                else if (c < 32 || c > 126) e.append('?');
                else e.append(c);
            }
            cs.append('(').append(e).append(") Tj T*\n");
        }
        cs.append("ET");
        byte[] content = cs.toString().getBytes(StandardCharsets.ISO_8859_1);
        String[] objs = {
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + w + " " + h + "] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
                null,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>"};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int[] off = new int[objs.length + 1];
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
        for (int i = 0; i < objs.length; i++) {
            off[i + 1] = out.size();
            out.write(((i + 1) + " 0 obj\n").getBytes(StandardCharsets.ISO_8859_1));
            if (objs[i] == null) {
                out.write(("<< /Length " + content.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
                out.write(content);
                out.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
            } else out.write(objs[i].getBytes(StandardCharsets.ISO_8859_1));
            out.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
        }
        int xref = out.size();
        StringBuilder x = new StringBuilder("xref\n0 " + (objs.length + 1) + "\n0000000000 65535 f \n");
        for (int i = 1; i <= objs.length; i++) x.append(String.format("%010d 00000 n \n", off[i]));
        x.append("trailer\n<< /Size ").append(objs.length + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF\n");
        out.write(x.toString().getBytes(StandardCharsets.ISO_8859_1));
        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(out.toByteArray());
        }
    }

    // ---------------------------------------------------------------- grafik
    static class BarChart extends JPanel {
        private String[] labels = new String[0];
        private double[] values = new double[0];

        BarChart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(300, 200));
        }

        void set(String[] l, double[] v) {
            labels = l;
            values = v;
            repaint();
        }

        static String fmt(double v) {
            if (v >= 1_000_000) return new DecimalFormat("0.0", SYM).format(v / 1_000_000) + "jt";
            if (v >= 1000) return new DecimalFormat("0", SYM).format(v / 1000) + "rb";
            return num(Math.round(v));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = values.length;
            if (n == 0) { g2.dispose(); return; }
            double max = 1;
            for (double v : values) max = Math.max(max, v);
            int padL = 10, padB = 24, padT = 22, w = getWidth() - padL * 2, h = getHeight() - padB - padT;
            int slot = w / n, bw = Math.min(46, slot - 12);
            for (int i = 0; i < n; i++) {
                int bh = (int) (h * values[i] / max);
                int x = padL + i * slot + (slot - bw) / 2, y = padT + h - bh;
                g2.setColor(i == n - 1 ? PRIMARY : new Color(0x99D5CF));
                g2.fillRoundRect(x, y, bw, Math.max(bh, 2), 8, 8);
                g2.setColor(TEXT);
                g2.setFont(f(Font.BOLD, 11));
                String v = fmt(values[i]);
                g2.drawString(v, x + (bw - g2.getFontMetrics().stringWidth(v)) / 2, y - 5);
                g2.setColor(MUTED);
                g2.setFont(f(Font.PLAIN, 11));
                g2.drawString(labels[i], x + (bw - g2.getFontMetrics().stringWidth(labels[i])) / 2, getHeight() - 7);
            }
            g2.dispose();
        }
    }
}
