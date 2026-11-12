package com.smartmart.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pembuat PDF satu halaman berisi teks monospasi (Courier), tanpa library tambahan.
 * Cukup untuk struk simulasi.
 */
public final class PdfSederhana {

    private PdfSederhana() {
    }

    public static void tulis(File berkas, List<String> baris) throws IOException {
        final int ukuranFont = 9;
        final int jarak = 12;
        final int margin = 24;
        int maksKarakter = 0;
        for (String b : baris) {
            maksKarakter = Math.max(maksKarakter, b.length());
        }
        int lebar = (int) Math.ceil(maksKarakter * ukuranFont * 0.6) + margin * 2;
        int tinggi = baris.size() * jarak + margin * 2;

        StringBuilder isi = new StringBuilder();
        isi.append("BT\n/F1 ").append(ukuranFont).append(" Tf\n").append(jarak).append(" TL\n");
        isi.append(margin).append(' ').append(tinggi - margin - ukuranFont).append(" Td\n");
        for (String b : baris) {
            isi.append('(').append(escape(b)).append(") Tj T*\n");
        }
        isi.append("ET\n");
        byte[] aliran = isi.toString().getBytes(StandardCharsets.ISO_8859_1);

        List<byte[]> objek = new ArrayList<>();
        objek.add(s("<< /Type /Catalog /Pages 2 0 R >>"));
        objek.add(s("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objek.add(s("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + lebar + " " + tinggi + "] "
                + "/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>"));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(s("<< /Length " + aliran.length + " >>\nstream\n"));
        stream.write(aliran);
        stream.write(s("\nendstream"));
        objek.add(stream.toByteArray());
        objek.add(s("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(s("%PDF-1.4\n"));
        int[] posisi = new int[objek.size()];
        for (int i = 0; i < objek.size(); i++) {
            posisi[i] = out.size();
            out.write(s((i + 1) + " 0 obj\n"));
            out.write(objek.get(i));
            out.write(s("\nendobj\n"));
        }
        int xref = out.size();
        out.write(s("xref\n0 " + (objek.size() + 1) + "\n0000000000 65535 f \n"));
        for (int p : posisi) {
            out.write(s(String.format("%010d 00000 n \n", p)));
        }
        out.write(s("trailer\n<< /Size " + (objek.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));

        try (FileOutputStream f = new FileOutputStream(berkas)) {
            f.write(out.toByteArray());
        }
    }

    private static byte[] s(String t) {
        return t.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String escape(String t) {
        StringBuilder sb = new StringBuilder();
        for (char ch : t.toCharArray()) {
            if (ch == '\\' || ch == '(' || ch == ')') {
                sb.append('\\').append(ch);
            } else if (ch < 32 || ch > 126) {
                sb.append('?');
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
