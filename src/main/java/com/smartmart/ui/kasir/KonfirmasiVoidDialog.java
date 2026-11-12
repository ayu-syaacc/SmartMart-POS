package com.smartmart.ui.kasir;

import java.awt.Component;

public class KonfirmasiVoidDialog extends DialogAdminBase {

    public KonfirmasiVoidDialog(Component induk, String ringkasan, Pemeriksa pemeriksa) {
        this(induk, ringkasan, pemeriksa, false);
    }

    public KonfirmasiVoidDialog(Component induk, String ringkasan, Pemeriksa pemeriksa, boolean modeContoh) {
        super(jendelaDari(induk), PanelPersetujuanAdmin.untukVoid(ringkasan), pemeriksa, modeContoh);
    }

    public static Integer minta(Component induk, String ringkasan, Pemeriksa pemeriksa) {
        return minta(induk, ringkasan, pemeriksa, false);
    }

    public static Integer minta(Component induk, String ringkasan, Pemeriksa pemeriksa, boolean modeContoh) {
        return new KonfirmasiVoidDialog(induk, ringkasan, pemeriksa, modeContoh).tampilkanDanTunggu();
    }
}
