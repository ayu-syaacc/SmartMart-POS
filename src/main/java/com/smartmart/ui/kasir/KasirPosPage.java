package com.smartmart.ui.kasir;

import com.smartmart.dao.ProdukDAO;
import com.smartmart.model.Pegawai;
import com.smartmart.model.SmartCart;
import com.smartmart.model.StrukData;
import com.smartmart.service.PenjualanService;
import com.smartmart.service.StokLotSource;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Window;
import java.sql.SQLException;

/**
 * Halaman Kasir (POS): layar kasir -> dialog pembayaran -> Pratinjau Struk -> Transaksi Baru.
 */
public class KasirPosPage extends JPanel {

    private static final String KARTU_KASIR = "kasir";
    private static final String KARTU_STRUK = "struk";

    private final Pegawai pegawai;
    private final CardLayout kartu = new CardLayout();
    private final TransaksiPanel layarKasir;
    private final PenjualanService service = new PenjualanService();
    private JScrollPane layarStruk;

    public KasirPosPage(Pegawai pegawai) {
        this.pegawai = pegawai;
        setLayout(kartu);
        layarKasir = new TransaksiPanel(new ProdukDAO(), new StokLotSource());
        layarKasir.setBayarListener(cart -> mulaiPembayaran());
        add(layarKasir, KARTU_KASIR);
        kartu.show(this, KARTU_KASIR);
    }

    private void mulaiPembayaran() {
        SmartCart cart = layarKasir.getCart();
        Window induk = SwingUtilities.getWindowAncestor(this);
        PenjualanService.Pembayaran p = PembayaranDialog.tampilkan(induk, cart);
        layarKasir.setPersenMember(cart.getPersenMember());   // segarkan tabel keranjang
        if (p == null) {
            return;
        }
        try {
            StrukData struk = service.bayar(pegawai.getIdPegawai(), pegawai.getNama(), cart, p);
            tampilkanStruk(struk);
        } catch (SQLException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this, "Transaksi gagal disimpan, tidak ada data yang berubah.\n"
                    + ex.getMessage(), "Pembayaran", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkanStruk(StrukData struk) {
        if (layarStruk != null) {
            remove(layarStruk);
        }
        StrukPanel panel = new StrukPanel(struk, this::transaksiBaru);
        layarStruk = UiKit.bungkusScroll(panel);
        layarStruk.getViewport().setBackground(UiKit.BG);
        add(layarStruk, KARTU_STRUK);
        kartu.show(this, KARTU_STRUK);
        revalidate();
        repaint();
    }

    private void transaksiBaru() {
        layarKasir.setPersenMember(0);
        layarKasir.resetTransaksi();
        kartu.show(this, KARTU_KASIR);
    }
}
