package com.smartmart.ui.kasir;

import java.awt.Component;

public class KonfirmasiReturDialog extends DialogAdminBase {

    public KonfirmasiReturDialog(Component induk, String ringkasan, Pemeriksa pemeriksa) {
        this(induk, ringkasan, pemeriksa, false);
    }

    public KonfirmasiReturDialog(Component induk, String ringkasan, Pemeriksa pemeriksa, boolean modeContoh) {
        super(jendelaDari(induk), PanelPersetujuanAdmin.untukRetur(ringkasan), pemeriksa, modeContoh);
    }

    public static Integer minta(Component induk, String ringkasan, Pemeriksa pemeriksa) {
        return minta(induk, ringkasan, pemeriksa, false);
    }

    public static Integer minta(Component induk, String ringkasan, Pemeriksa pemeriksa, boolean modeContoh) {
        return new KonfirmasiReturDialog(induk, ringkasan, pemeriksa, modeContoh).tampilkanDanTunggu();
    }
}
