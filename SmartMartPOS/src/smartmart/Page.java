package smartmart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Navigasi antar halaman. */
interface Nav {
    void go(String page);
    void goReturn(String saleId);
    void goVoid(String saleId);
    void setMember(Member m);
    User user();
}

/** Dasar semua halaman konten. */
abstract class Page extends JPanel {
    final Nav nav;
    final Db db = Db.get();

    Page(Nav nav) {
        this.nav = nav;
        setLayout(new BorderLayout(0, 12));
        setBackground(Ui.BG);
        setBorder(new EmptyBorder(16, 20, 16, 20));
    }

    abstract String title();

    abstract String subtitle();

    /** Dipanggil setiap halaman ditampilkan. */
    void refresh() { }
}
