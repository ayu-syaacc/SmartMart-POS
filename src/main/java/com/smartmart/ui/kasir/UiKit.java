package com.smartmart.ui.kasir;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public final class UiKit {

    private UiKit() { }

    public static final Color BG = new Color(0xEDF3EF);
    public static final Color GREEN = new Color(0x0F9158);
    public static final Color GREEN_DARK = new Color(0x0A6E50);
    public static final Color GREEN_SOFT = new Color(0xE5F7EB);
    public static final Color GREEN_ROW = new Color(0xE5F7EB);
    public static final Color GRAY_SOFT = new Color(0xF2F6F3);
    public static final Color ROW_BG = new Color(0xF7FAF8);
    public static final Color FIELD_BG = new Color(0xF3F7F4);
    public static final Color TEXT = new Color(0x14281F);
    public static final Color MUTED = new Color(0x76847C);
    public static final Color RED = new Color(0xE11D48);
    public static final Color RED_SOFT = new Color(0xFDE6EC);
    public static final Color RED_TEXT = new Color(0xC0143C);
    public static final Color AMBER_BG = new Color(0xFFF7E6);
    public static final Color AMBER_TEXT = new Color(0x92400E);
    public static final Color ORANGE_BG = new Color(0xFFE9D6);
    public static final Color ORANGE_TEXT = new Color(0xC45D15);
    public static final Color BLUE_BG = new Color(0xE3F0FB);
    public static final Color BLUE_TEXT = new Color(0x1D6FB8);
    public static final Color HEADER_BG = new Color(0xEEF3F0);
    public static final Color SELECTED = new Color(0xF0FAF3);
    public static final Color LINE = new Color(0xE3E9E5);

    public static final String FONT = "Poppins";
    private static boolean fontTerpasang;

    public static synchronized void pasangFont() {
        if (fontTerpasang) return;
        fontTerpasang = true;
        String[] berkas = {"/fonts/Poppins-Regular.ttf", "/fonts/Poppins-Bold.ttf"};
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String b : berkas) {
            java.io.InputStream in = UiKit.class.getResourceAsStream(b);
            if (in == null) continue;
            try {
                try {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, in));
                } finally {
                    in.close();
                }
            } catch (java.awt.FontFormatException e) {
            } catch (java.io.IOException e) {
            }
        }
    }

    public static Font font(int style, int size) {
        pasangFont();
        return new Font(FONT, style, size);
    }

    public static String rp(int n) {
        return "Rp" + String.format(Locale.GERMANY, "%,d", n);
    }

    public static JLabel lbl(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static GridBagConstraints gbc(int x, int y, double wx, double wy, int fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = wx;
        c.weighty = wy;
        c.fill = fill;
        return c;
    }

    private static void quality(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public enum Ikon { DOC, UNDO, SHIELD, TRASH, LOCK, BOX, CARD, CHECK, WARN, SEARCH, CALENDAR, BELL }

    public static void paintIkon(Graphics2D g, Ikon t, double x, double y, double s, Color c) {
        Graphics2D g2 = (Graphics2D) g.create();
        quality(g2);
        g2.translate(x, y);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float) (s * 0.09), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (t) {
            case DOC:
                g2.draw(new RoundRectangle2D.Double(s * .2, s * .1, s * .6, s * .8, s * .14, s * .14));
                g2.draw(new Line2D.Double(s * .32, s * .35, s * .68, s * .35));
                g2.draw(new Line2D.Double(s * .32, s * .52, s * .68, s * .52));
                g2.draw(new Line2D.Double(s * .32, s * .69, s * .56, s * .69));
                break;
            case UNDO: {
                Path2D p = new Path2D.Double();
                p.moveTo(s * .34, s * .14); p.lineTo(s * .1, s * .4); p.lineTo(s * .34, s * .66);
                g2.draw(p);
                Path2D q = new Path2D.Double();
                q.moveTo(s * .1, s * .4); q.lineTo(s * .58, s * .4);
                q.curveTo(s * .95, s * .4, s * .95, s * .88, s * .58, s * .88);
                q.lineTo(s * .3, s * .88);
                g2.draw(q);
                break;
            }
            case SHIELD: {
                Path2D p = new Path2D.Double();
                p.moveTo(s * .5, s * .08); p.lineTo(s * .86, s * .22); p.lineTo(s * .86, s * .5);
                p.curveTo(s * .86, s * .75, s * .65, s * .88, s * .5, s * .94);
                p.curveTo(s * .35, s * .88, s * .14, s * .75, s * .14, s * .5);
                p.lineTo(s * .14, s * .22); p.closePath();
                g2.draw(p);
                Path2D k = new Path2D.Double();
                k.moveTo(s * .34, s * .5); k.lineTo(s * .46, s * .62); k.lineTo(s * .68, s * .38);
                g2.draw(k);
                break;
            }
            case TRASH: {
                g2.draw(new Line2D.Double(s * .18, s * .26, s * .82, s * .26));
                Path2D h = new Path2D.Double();
                h.moveTo(s * .4, s * .26); h.lineTo(s * .4, s * .14); h.lineTo(s * .6, s * .14); h.lineTo(s * .6, s * .26);
                g2.draw(h);
                Path2D b = new Path2D.Double();
                b.moveTo(s * .27, s * .32); b.lineTo(s * .32, s * .88); b.lineTo(s * .68, s * .88); b.lineTo(s * .73, s * .32);
                g2.draw(b);
                g2.draw(new Line2D.Double(s * .43, s * .46, s * .43, s * .74));
                g2.draw(new Line2D.Double(s * .57, s * .46, s * .57, s * .74));
                break;
            }
            case LOCK:
                g2.draw(new RoundRectangle2D.Double(s * .2, s * .44, s * .6, s * .46, s * .12, s * .12));
                g2.draw(new Arc2D.Double(s * .3, s * .1, s * .4, s * .7, 0, 180, Arc2D.OPEN));
                g2.fill(new Ellipse2D.Double(s * .46, s * .62, s * .08, s * .08));
                break;
            case BOX: {
                Path2D p = new Path2D.Double();
                p.moveTo(s * .5, s * .08); p.lineTo(s * .88, s * .28); p.lineTo(s * .88, s * .72);
                p.lineTo(s * .5, s * .92); p.lineTo(s * .12, s * .72); p.lineTo(s * .12, s * .28); p.closePath();
                g2.draw(p);
                Path2D q = new Path2D.Double();
                q.moveTo(s * .12, s * .28); q.lineTo(s * .5, s * .48); q.lineTo(s * .88, s * .28);
                g2.draw(q);
                g2.draw(new Line2D.Double(s * .5, s * .48, s * .5, s * .92));
                break;
            }
            case CARD:
                g2.draw(new RoundRectangle2D.Double(s * .1, s * .22, s * .8, s * .56, s * .12, s * .12));
                g2.draw(new Line2D.Double(s * .1, s * .42, s * .9, s * .42));
                g2.draw(new Line2D.Double(s * .22, s * .64, s * .42, s * .64));
                break;
            case CHECK: {
                Path2D p = new Path2D.Double();
                p.moveTo(s * .2, s * .52); p.lineTo(s * .42, s * .72); p.lineTo(s * .8, s * .3);
                g2.setStroke(new BasicStroke((float) (s * 0.13), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(p);
                break;
            }
            case WARN: {
                Path2D p = new Path2D.Double();
                p.moveTo(s * .5, s * .1); p.lineTo(s * .92, s * .86); p.lineTo(s * .08, s * .86); p.closePath();
                g2.draw(p);
                g2.draw(new Line2D.Double(s * .5, s * .4, s * .5, s * .6));
                g2.fill(new Ellipse2D.Double(s * .46, s * .7, s * .08, s * .08));
                break;
            }
            case CALENDAR:
                g2.draw(new RoundRectangle2D.Double(s * .12, s * .2, s * .76, s * .68, s * .16, s * .16));
                g2.draw(new Line2D.Double(s * .12, s * .44, s * .88, s * .44));
                g2.draw(new Line2D.Double(s * .32, s * .1, s * .32, s * .3));
                g2.draw(new Line2D.Double(s * .68, s * .1, s * .68, s * .3));
                break;
            case BELL: {
                Path2D b = new Path2D.Double();
                b.moveTo(s * .2, s * .72); b.curveTo(s * .3, s * .64, s * .3, s * .5, s * .3, s * .42);
                b.curveTo(s * .3, s * .12, s * .7, s * .12, s * .7, s * .42);
                b.curveTo(s * .7, s * .5, s * .7, s * .64, s * .8, s * .72); b.closePath();
                g2.draw(b);
                g2.draw(new Line2D.Double(s * .42, s * .84, s * .58, s * .84));
                break;
            }
            case SEARCH:
                g2.draw(new Ellipse2D.Double(s * .12, s * .12, s * .52, s * .52));
                g2.draw(new Line2D.Double(s * .56, s * .56, s * .88, s * .88));
                break;
            default:
                break;
        }
        g2.dispose();
    }

    public static JComponent ikon(final Ikon t, final int size, final Color c) {
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                paintIkon((Graphics2D) g, t, (getWidth() - size) / 2.0, (getHeight() - size) / 2.0, size, c);
            }
        };
        comp.setPreferredSize(new Dimension(size, size));
        comp.setMinimumSize(new Dimension(size, size));
        return comp;
    }

    public static JComponent ikonBulat(final Ikon t, final int d, final Color bg, final Color fg) {
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(bg);
                g2.fill(new Ellipse2D.Double(0, 0, d, d));
                g2.dispose();
                paintIkon((Graphics2D) g, t, d * .25, d * .25, d * .5, fg);
            }
        };
        comp.setPreferredSize(new Dimension(d, d));
        comp.setMinimumSize(new Dimension(d, d));
        comp.setMaximumSize(new Dimension(d, d));
        return comp;
    }

    public static JComponent ikonKotak(final Ikon t, final int d, final Color bg, final Color fg) {
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, d, d, d * .3, d * .3));
                g2.dispose();
                paintIkon((Graphics2D) g, t, d * .25, d * .25, d * .5, fg);
            }
        };
        comp.setPreferredSize(new Dimension(d, d));
        comp.setMinimumSize(new Dimension(d, d));
        comp.setMaximumSize(new Dimension(d, d));
        return comp;
    }

    public enum Produk { SUSU, APEL, ROTI, YOGHURT, UMUM }

    public static JComponent ikonProduk(final Produk p, final int d) {
        JComponent comp = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                Color bg;
                switch (p) {
                    case APEL: bg = new Color(0xFFE3E6); break;
                    case ROTI: bg = new Color(0xFFF0D6); break;
                    case UMUM: bg = new Color(0xE9EEEB); break;
                    default: bg = new Color(0xE3EEFB); break;
                }
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, d, d, d * .3, d * .3));
                switch (p) {
                    case SUSU:
                        g2.setColor(new Color(0x2F6FD0));
                        g2.fill(new RoundRectangle2D.Double(d * .32, d * .24, d * .36, d * .52, d * .06, d * .06));
                        g2.setColor(Color.WHITE);
                        g2.fill(new RoundRectangle2D.Double(d * .4, d * .42, d * .2, d * .16, d * .04, d * .04));
                        break;
                    case APEL:
                        g2.setColor(new Color(0xD92D20));
                        g2.fill(new Ellipse2D.Double(d * .26, d * .3, d * .48, d * .46));
                        g2.setColor(new Color(0x3C8F3C));
                        g2.fill(new Ellipse2D.Double(d * .5, d * .2, d * .14, d * .1));
                        break;
                    case ROTI:
                        g2.setColor(new Color(0xD9A441));
                        g2.fill(new RoundRectangle2D.Double(d * .24, d * .36, d * .52, d * .34, d * .1, d * .1));
                        g2.setColor(new Color(0xF0C46A));
                        g2.fill(new RoundRectangle2D.Double(d * .24, d * .3, d * .52, d * .16, d * .1, d * .1));
                        break;
                    case UMUM:
                        g2.setColor(new Color(0x9AA8A0));
                        g2.fill(new RoundRectangle2D.Double(d * .28, d * .3, d * .44, d * .4, d * .08, d * .08));
                        g2.setColor(Color.WHITE);
                        g2.fill(new RoundRectangle2D.Double(d * .4, d * .4, d * .2, d * .08, d * .04, d * .04));
                        break;
                    default: {
                        Path2D cup = new Path2D.Double();
                        cup.moveTo(d * .3, d * .3); cup.lineTo(d * .7, d * .3); cup.lineTo(d * .64, d * .74);
                        cup.lineTo(d * .36, d * .74); cup.closePath();
                        g2.setColor(Color.WHITE);
                        g2.fill(cup);
                        g2.setColor(new Color(0x2F6FD0));
                        g2.fill(new RoundRectangle2D.Double(d * .32, d * .3, d * .36, d * .12, d * .04, d * .04));
                        break;
                    }
                }
                g2.dispose();
            }
        };
        comp.setPreferredSize(new Dimension(d, d));
        comp.setMinimumSize(new Dimension(d, d));
        comp.setMaximumSize(new Dimension(d, d));
        return comp;
    }

    public static Produk produkDariNama(String nama) {
        String n = nama == null ? "" : nama.toLowerCase();
        if (n.contains("susu")) return Produk.SUSU;
        if (n.contains("apel") || n.contains("pisang") || n.contains("jeruk")) return Produk.APEL;
        if (n.contains("roti")) return Produk.ROTI;
        if (n.contains("yoghurt") || n.contains("yogurt")) return Produk.YOGHURT;
        return Produk.UMUM;
    }

    public static class Card extends JPanel {
        private final int radius;

        public Card() { this(30); }

        public Card(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(22, 28, 24, 28));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0, 70, 40, 10));
            g2.fill(new RoundRectangle2D.Double(0, 8, w, h - 8, radius, radius));
            g2.setColor(new Color(0, 70, 40, 14));
            g2.fill(new RoundRectangle2D.Double(0, 5, w, h - 5, radius, radius));
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h - 8, radius, radius));
            g2.dispose();
        }
    }

    public static class Box extends JPanel {
        private Color fill;
        private Color border;
        private float stroke = 2f;
        private Color bar;
        private final int radius;

        public Box(Color fill, int radius) {
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }

        public void setFill(Color c) { fill = c; repaint(); }

        @Override
        public Dimension getMinimumSize() {
            Dimension d = super.getMinimumSize();
            return new Dimension(d.width, Math.max(d.height, getPreferredSize().height));
        }

        public void setBorderColor(Color c, float width) { border = c; stroke = width; repaint(); }

        public void setBar(Color c) { bar = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            int w = getWidth(), h = getHeight();
            Shape rr = new RoundRectangle2D.Double(0, 0, w, h, radius, radius);
            g2.setColor(fill);
            g2.fill(rr);
            if (bar != null) {
                g2.setClip(rr);
                g2.setColor(bar);
                g2.fillRect(0, 0, 6, h);
                g2.setClip(null);
            }
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(stroke));
                float o = stroke / 2f;
                g2.draw(new RoundRectangle2D.Double(o, o, w - stroke, h - stroke, radius, radius));
            }
            g2.dispose();
        }
    }

    public static void tipisScroll(javax.swing.JScrollPane sp) {
        javax.swing.JScrollBar sb = sp.getVerticalScrollBar();
        sb.setPreferredSize(new Dimension(10, 0));
        sb.setOpaque(false);
        sb.setBackground(Color.WHITE);
        sb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0xC9D3CD);
                trackColor = new Color(0, 0, 0, 0);
            }

            @Override
            protected JButton createDecreaseButton(int o) { return nol(); }

            @Override
            protected JButton createIncreaseButton(int o) { return nol(); }

            private JButton nol() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) { }

            @Override
            protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 6, 6);
                g2.dispose();
            }
        });
    }

    public static JPanel transparan() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    public static class Badge extends JLabel {
        private Color bg;

        public Badge(String text, Color bg, Color fg) {
            super(text, SwingConstants.CENTER);
            setFont(font(Font.BOLD, 10));
            setBorder(new EmptyBorder(4, 12, 4, 12));
            set(text, bg, fg);
        }

        public final void set(String text, Color bg, Color fg) {
            this.bg = bg;
            setText(text);
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Badge badgeStatus(String status) {
        Badge b = new Badge(status, GREEN_SOFT, GREEN);
        terapkanStatus(b, status);
        return b;
    }

    public static void terapkanStatus(Badge b, String status) {
        if ("DIBATALKAN".equals(status)) {
            b.set(status, RED_SOFT, RED_TEXT);
        } else {
            b.set(status, GREEN_SOFT, GREEN);
        }
    }

    public static class Chip extends JToggleButton {
        private final Color aktif;

        public Chip(String text, Color aktif) {
            super(text);
            this.aktif = aktif;
            setFont(font(Font.BOLD, 11));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 28, 28);
        }

        @Override
        public Dimension getMinimumSize() { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            g2.setColor(isSelected() ? aktif : GRAY_SOFT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setFont(getFont());
            g2.setColor(isSelected() ? Color.WHITE : TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    public static class RoundButton extends JButton {
        private Color fill;
        private Color fg;
        private Color border;
        private final Ikon ikon;
        private boolean hover;

        public RoundButton(String text, Ikon ikon, Color fill, Color fg, Color border) {
            super(text);
            this.ikon = ikon;
            this.fill = fill;
            this.fg = fg;
            this.border = border;
            setFont(font(Font.BOLD, 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            return isPreferredSizeSet() ? super.getPreferredSize() : new Dimension(120, 48);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(60, getPreferredSize().height);
        }

        public void setWarna(Color c) { fill = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            int w = getWidth(), h = getHeight();
            Color f = fill;
            Color t = fg;
            if (!isEnabled()) {
                f = new Color(f.getRed(), f.getGreen(), f.getBlue(), 90);
                t = new Color(t.getRed(), t.getGreen(), t.getBlue(), 120);
            } else if (hover && f.getAlpha() > 0) {
                f = f.darker();
            } else if (hover) {
                f = new Color(border != null ? border.getRed() : 0, border != null ? border.getGreen() : 0,
                        border != null ? border.getBlue() : 0, 18);
            }
            g2.setColor(f);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 18, 18));
            if (border != null) {
                g2.setColor(isEnabled() ? border : new Color(border.getRed(), border.getGreen(), border.getBlue(), 90));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 18, 18));
            }
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(getText());
            int ik = ikon != null ? 18 + 10 : 0;
            int x = (w - tw - ik) / 2;
            if (ikon != null) {
                paintIkon(g2, ikon, x, (h - 18) / 2.0, 18, t);
            }
            g2.setColor(t);
            g2.drawString(getText(), x + ik, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    public static RoundButton tombolHijau(String text, Ikon ikon) {
        return new RoundButton(text, ikon, GREEN, Color.WHITE, null);
    }

    public static RoundButton tombolAbu(String text) {
        return new RoundButton(text, null, GRAY_SOFT, MUTED, null);
    }

    public static RoundButton tombolMerah(String text, Ikon ikon) {
        return new RoundButton(text, ikon, RED, Color.WHITE, null);
    }

    public static RoundButton tombolOutlineMerah(String text, Ikon ikon) {
        return new RoundButton(text, ikon, new Color(255, 255, 255, 0), RED, RED);
    }

    public static class HintField extends JTextField {
        private final String hint;

        public HintField(String hint) {
            this.hint = hint;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setFont(font(Font.PLAIN, 14));
            setForeground(TEXT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }

    public static class SearchField extends JPanel {
        private final HintField field;
        private boolean aktif;
        private final Color warnaAktif;

        public SearchField(String hint, Ikon ikon, Color warnaAktif) {
            this.warnaAktif = warnaAktif;
            field = new HintField(hint);
            setOpaque(false);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new java.awt.Insets(0, 18, 0, 10);
            add(ikon(ikon, 18, MUTED), c);
            c = gbc(1, 0, 1, 1, GridBagConstraints.HORIZONTAL);
            c.insets = new java.awt.Insets(0, 0, 0, 16);
            add(field, c);
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { aktif = true; repaint(); }

                @Override
                public void focusLost(FocusEvent e) { aktif = false; repaint(); }
            });
            setPreferredSize(new Dimension(300, 44));
        }

        public JTextField getField() { return field; }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(63, getPreferredSize().height);
        }

        public void setAktif(boolean a) { aktif = a; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(FIELD_BG);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, h, h));
            if (aktif) {
                g2.setColor(warnaAktif);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, h - 2, h - 2));
            }
            g2.dispose();
        }
    }

    public static class PasswordBox extends JPanel {
        private final JPasswordField field = new JPasswordField();
        private Color garis;

        public PasswordBox(Color garis) {
            this.garis = garis;
            setOpaque(false);
            setLayout(new GridBagLayout());
            field.setOpaque(false);
            field.setBorder(BorderFactory.createEmptyBorder());
            field.setFont(font(Font.BOLD, 16));
            field.setForeground(TEXT);
            field.setEchoChar('\u25CF');
            GridBagConstraints c = gbc(0, 0, 1, 1, GridBagConstraints.HORIZONTAL);
            c.insets = new java.awt.Insets(0, 18, 0, 18);
            add(field, c);
            setPreferredSize(new Dimension(300, 50));
        }

        public JPasswordField getField() { return field; }

        public void setGaris(Color c) { garis = c; repaint(); }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(63, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(FIELD_BG);
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 18, 18));
            g2.setColor(garis);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 18, 18));
            g2.dispose();
        }
    }

    public static class CentangBox extends JToggleButton {
        private final int d;

        public CentangBox(int d, boolean terpilih) {
            this.d = d;
            setSelected(terpilih);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(d, d));
            setMinimumSize(new Dimension(d, d));
            setMaximumSize(new Dimension(d, d));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            quality(g2);
            double r = d * .3;
            if (isSelected()) {
                g2.setColor(GREEN);
                g2.fill(new RoundRectangle2D.Double(1, 1, d - 2, d - 2, r, r));
                paintIkon(g2, Ikon.CHECK, d * .12, d * .12, d * .76, Color.WHITE);
            } else {
                g2.setColor(isEnabled() ? Color.WHITE : new Color(0xF4F7F5));
                g2.fill(new RoundRectangle2D.Double(1, 1, d - 2, d - 2, r, r));
                g2.setColor(isEnabled() ? GREEN : new Color(0xD5DDD8));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1.5, 1.5, d - 3, d - 3, r, r));
            }
            g2.dispose();
        }
    }

    public static javax.swing.Icon ikonIcon(final Ikon t, final int size, final Color c) {
        return new javax.swing.Icon() {
            public void paintIcon(java.awt.Component comp, Graphics g, int x, int y) {
                paintIkon((Graphics2D) g, t, x, y, size, c);
            }

            public int getIconWidth() { return size; }

            public int getIconHeight() { return size; }
        };
    }

    public static javax.swing.Icon ikonBulatIcon(final Ikon t, final int d, final Color bg, final Color fg) {
        return new javax.swing.Icon() {
            public void paintIcon(java.awt.Component comp, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                quality(g2);
                g2.setColor(bg);
                g2.fill(new Ellipse2D.Double(x, y, d, d));
                g2.dispose();
                paintIkon((Graphics2D) g, t, x + d * .25, y + d * .25, d * .5, fg);
            }

            public int getIconWidth() { return d; }

            public int getIconHeight() { return d; }
        };
    }

    public static void susunVertikal(JPanel box) {
        for (java.awt.Component k : box.getComponents()) {
            if (k instanceof JComponent) {
                JComponent jc = (JComponent) k;
                jc.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
                jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, jc.getPreferredSize().height));
            }
        }
        box.revalidate();
        box.repaint();
    }

    public static void jarakAtas(JComponent c, int px) {
        java.awt.Insets lama = c.getBorder() == null ? new java.awt.Insets(0, 0, 0, 0) : c.getBorder().getBorderInsets(c);
        c.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(px, 0, 0, 0), c.getBorder()));
    }

    public static JLabel cariLabel(java.awt.Container root, String teks) {
        for (java.awt.Component k : root.getComponents()) {
            if (k instanceof JLabel && teks.equals(((JLabel) k).getText())) return (JLabel) k;
            if (k instanceof java.awt.Container) {
                JLabel r = cariLabel((java.awt.Container) k, teks);
                if (r != null) return r;
            }
        }
        return null;
    }

    public static class BarisValid extends Box {
        private final JLabel judul = new JLabel();
        private final JLabel ket = new JLabel();
        private final JLabel ikonLabel = new JLabel();

        public BarisValid(String judulTeks, String ketTeks) {
            super(GREEN_ROW, 18);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.gridheight = 2;
            c.insets = new java.awt.Insets(0, 14, 0, 12);
            add(ikonLabel, c);
            c = new GridBagConstraints();
            c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
            c.insets = new java.awt.Insets(12, 0, 0, 14);
            add(judul, c);
            c = new GridBagConstraints();
            c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
            c.insets = new java.awt.Insets(0, 0, 12, 14);
            add(ket, c);
            judul.setFont(font(Font.BOLD, 13));
            ket.setFont(font(Font.PLAIN, 11));
            ket.setForeground(MUTED);
            set(judulTeks, ketTeks, true);
        }

        public final void set(String judulTeks, String ketTeks, boolean lolos) {
            judul.setText(judulTeks);
            ket.setText(ketTeks);
            judul.setForeground(lolos ? GREEN_DARK : RED_TEXT);
            setFill(lolos ? GREEN_ROW : RED_SOFT);
            ikonLabel.setIcon(lolos ? ikonBulatIcon(Ikon.CHECK, 28, GREEN, Color.WHITE)
                    : ikonBulatIcon(Ikon.WARN, 28, RED, Color.WHITE));
        }
    }

    public static Box kartuTolak(String judulTeks, String isi) {
        Box b = new Box(RED_SOFT, 16);
        b.setBar(RED);
        b.setLayout(new GridBagLayout());
        b.setPreferredSize(new Dimension(100, 78));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridheight = 2; c.insets = new java.awt.Insets(0, 26, 0, 14);
        JLabel ik = new JLabel(ikonIcon(Ikon.WARN, 22, RED));
        b.add(ik, c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 0; c.weightx = 1; c.anchor = GridBagConstraints.SOUTHWEST;
        b.add(lbl(judulTeks, Font.BOLD, 14, RED_TEXT), c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1; c.weightx = 1; c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new java.awt.Insets(2, 0, 0, 10);
        b.add(lbl(isi, Font.PLAIN, 12, TEXT), c);
        return b;
    }

    public static class Dampak extends Box {
        private final JLabel sub;

        public Dampak(Ikon ikon, String judulTeks, String subTeks) {
            super(ROW_BG, 18);
            setLayout(new GridBagLayout());
            setPreferredSize(new Dimension(100, 76));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.gridheight = 2; c.insets = new java.awt.Insets(0, 16, 0, 16);
            JLabel ik = new JLabel(new javax.swing.Icon() {
                public void paintIcon(java.awt.Component comp, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    quality(g2);
                    g2.setColor(GREEN_SOFT);
                    g2.fill(new RoundRectangle2D.Double(x, y, 46, 46, 14, 14));
                    g2.dispose();
                    paintIkon((Graphics2D) g, ikon, x + 11.5, y + 11.5, 23, GREEN);
                }

                public int getIconWidth() { return 46; }

                public int getIconHeight() { return 46; }
            });
            add(ik, c);
            c = new GridBagConstraints();
            c.gridx = 1; c.gridy = 0; c.weightx = 1; c.anchor = GridBagConstraints.SOUTHWEST;
            add(lbl(judulTeks, Font.BOLD, 14, TEXT), c);
            sub = lbl(subTeks, Font.PLAIN, 12, MUTED);
            c = new GridBagConstraints();
            c.gridx = 1; c.gridy = 1; c.weightx = 1; c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new java.awt.Insets(2, 0, 0, 8);
            add(sub, c);
        }

        public void setSub(String t) { sub.setText(t); }
    }

    public static void pintarLatar(Graphics2D g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        quality(g2);
        g2.setColor(new Color(0xCEEC9D));
        g2.fill(new Ellipse2D.Double(-5, -110, 200, 200));
        g2.setColor(new Color(0xE2F2CB));
        g2.fill(new Ellipse2D.Double(w - 5 - 88, -60, 176, 176));
        g2.setColor(new Color(0xD5EDE0));
        g2.fill(new Ellipse2D.Double(w - 611, h + 553 - 611 + 0.0, 1222, 1222));
        g2.dispose();
    }

    private static final String[] HARI = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
    private static final String[] BLN = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

    public static class BarInfo extends JPanel {
        private final JLabel tanggal = new JLabel();
        private final JLabel jam = new JLabel();

        public BarInfo() {
            setOpaque(false);
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 18, 0));
            tanggal.setFont(font(Font.BOLD, 14));
            tanggal.setForeground(TEXT);
            tanggal.setIcon(ikonIcon(Ikon.CALENDAR, 16, TEXT));
            tanggal.setIconTextGap(8);
            jam.setFont(font(Font.PLAIN, 14));
            jam.setForeground(MUTED);
            JLabel online = new JLabel("Online");
            online.setFont(font(Font.BOLD, 14));
            online.setForeground(TEXT);
            online.setIconTextGap(8);
            online.setIcon(new javax.swing.Icon() {
                public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    quality(g2);
                    g2.setColor(GREEN);
                    g2.fill(new Ellipse2D.Double(x, y, 10, 10));
                    g2.dispose();
                }

                public int getIconWidth() { return 10; }

                public int getIconHeight() { return 10; }
            });
            JLabel lonceng = new JLabel(new javax.swing.Icon() {
                public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
                    paintIkon((Graphics2D) g, Ikon.BELL, x, y + 2, 22, TEXT);
                    Graphics2D g2 = (Graphics2D) g.create();
                    quality(g2);
                    g2.setColor(RED);
                    g2.fill(new Ellipse2D.Double(x + 15, y + 2, 7, 7));
                    g2.dispose();
                }

                public int getIconWidth() { return 24; }

                public int getIconHeight() { return 24; }
            });
            add(tanggal);
            add(jam);
            add(online);
            add(lonceng);
            perbarui();
            new javax.swing.Timer(20000, new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) { perbarui(); }
            }).start();
        }

        private void perbarui() {
            java.time.LocalDateTime n = java.time.LocalDateTime.now();
            tanggal.setText(HARI[n.getDayOfWeek().getValue() - 1] + ", " + String.format("%02d", n.getDayOfMonth())
                    + " " + BLN[n.getMonthValue() - 1] + " " + n.getYear());
            jam.setText(String.format("%02d:%02d", n.getHour(), n.getMinute()));
        }
    }

    public static JPanel header(Ikon ikon, String judul, String subjudul, JLabel subRef) {
        JPanel p = transparan();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        c.insets = new java.awt.Insets(0, 0, 0, 12);
        p.add(ikon(ikon, 28, TEXT), c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.WEST;
        p.add(lbl(judul, Font.BOLD, 26, TEXT), c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST;
        subRef.setText(subjudul);
        subRef.setFont(font(Font.PLAIN, 13));
        subRef.setForeground(MUTED);
        p.add(subRef, c);
        c = new GridBagConstraints();
        c.gridx = 2; c.gridy = 0; c.weightx = 1;
        p.add(javax.swing.Box.createHorizontalGlue(), c);
        return p;
    }

    public static JPanel header(Ikon ikon, String judul, String subjudul) {
        return header(ikon, judul, subjudul, new JLabel());
    }

    public static Box barisValid(String judul, String ket) {
        Box b = new Box(GREEN_ROW, 18);
        b.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.gridheight = 2;
        c.insets = new java.awt.Insets(0, 14, 0, 12);
        b.add(ikonBulat(Ikon.CHECK, 28, GREEN, Color.WHITE), c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        c.insets = new java.awt.Insets(10, 0, 0, 14);
        b.add(lbl(judul, Font.BOLD, 13, GREEN_DARK), c);
        c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        c.insets = new java.awt.Insets(0, 0, 10, 14);
        b.add(lbl(ket, Font.PLAIN, 11, MUTED), c);
        return b;
    }

    public static javax.swing.JTextArea teksBungkus(String isi, int lebar, int style, int size, Color warna) {
        javax.swing.JTextArea ta = new javax.swing.JTextArea(isi);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setOpaque(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(font(style, size));
        ta.setForeground(warna);
        ta.setBorder(null);
        ta.setSize(new Dimension(lebar, 1000));
        int tinggi = ta.getPreferredSize().height;
        ta.setPreferredSize(new Dimension(lebar, tinggi));
        ta.setMinimumSize(new Dimension(40, tinggi));
        return ta;
    }

    public static Box catatanKuning(String judul, String isi, boolean denganIkon) {
        return catatanKuning(judul, isi, denganIkon, 240);
    }

    public static Box catatanKuning(String judul, String isi, boolean denganIkon, int lebar) {
        Box b = new Box(AMBER_BG, 18);
        b.setLayout(new GridBagLayout());
        int x = 0;
        if (denganIkon) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.gridheight = 2;
            c.insets = new java.awt.Insets(0, 18, 0, 14);
            b.add(ikon(Ikon.WARN, 24, ORANGE_TEXT), c);
            x = 1;
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        c.insets = new java.awt.Insets(12, denganIkon ? 0 : 14, 0, 14);
        b.add(lbl(judul, Font.BOLD, denganIkon ? 14 : 12, AMBER_TEXT), c);
        c = new GridBagConstraints();
        c.gridx = x; c.gridy = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1;
        c.insets = new java.awt.Insets(2, denganIkon ? 0 : 14, 12, 14);
        b.add(teksBungkus(isi, lebar, Font.PLAIN, denganIkon ? 12 : 10, AMBER_TEXT), c);
        return b;
    }

    public static javax.swing.JScrollPane bungkusScroll(JComponent panel) {
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(new IsiScroll(panel),
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getHorizontalScrollBar().setUnitIncrement(24);
        return sp;
    }

    private static final class IsiScroll extends JPanel implements javax.swing.Scrollable {
        private final JComponent isi;

        IsiScroll(JComponent isi) {
            super(new java.awt.BorderLayout());
            this.isi = isi;
            setOpaque(false);
            add(isi, java.awt.BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            return isi.getMinimumSize();
        }

        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

        public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientation, int direction) { return 24; }

        public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? r.height - 24 : r.width - 24;
        }

        public boolean getScrollableTracksViewportWidth() {
            return getParent() instanceof javax.swing.JViewport
                    && ((javax.swing.JViewport) getParent()).getWidth() >= isi.getMinimumSize().width;
        }

        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof javax.swing.JViewport
                    && ((javax.swing.JViewport) getParent()).getHeight() >= isi.getMinimumSize().height;
        }
    }
}
