
package com.smartmart.service;

import java.sql.SQLException;
import java.util.List;

import com.smartmart.dao.ProdukDAO;
import com.smartmart.model.Produk;

public class ProdukService {

    private final ProdukDAO produkDAO;

    public ProdukService() {
        this.produkDAO = new ProdukDAO();
    }

    // Mengambil seluruh produk.
    public List<Produk> getAllProduk() throws SQLException {
        return produkDAO.ambilSemua();
    }

    // Mencari produk berdasarkan kode barang.
    public Produk cariProduk(String kodeBarang) throws SQLException {
        if (kodeBarang == null || kodeBarang.isBlank()) {
            throw new IllegalArgumentException("Kode barang wajib diisi.");
        }

        return produkDAO.cariKode(kodeBarang.trim());
    }

    // Mencari produk berdasarkan nama.
    public List<Produk> cariNama(String kataKunci) throws SQLException {
        if (kataKunci == null || kataKunci.isBlank()) {
            return getAllProduk();
        }

        return produkDAO.cariNama(kataKunci.trim());
    }

    // Menambahkan produk baru.
    public void tambahProduk(
            String kodeBarang,
            String nama,
            long harga,
            String satuan,
            int stokMinimum,
            boolean dapatDiretur
    ) throws SQLException {

        validasiProduk(kodeBarang, nama, harga, satuan, stokMinimum);

        String kodeBersih = kodeBarang.trim();

        // Periksa juga produk nonaktif agar tidak terjadi benturan primary key.
        if (produkDAO.ambilByKode(kodeBersih) != null) {
            throw new IllegalArgumentException("Kode barang sudah digunakan.");
        }

        Produk produk = new Produk(
                kodeBersih,
                nama.trim(),
                Produk.KATEGORI_DEFAULT,
                harga,
                satuan.trim(),
                stokMinimum,
                dapatDiretur,
                true
        );

        produkDAO.tambah(produk);
    }

    // Mengubah data produk.
    public void ubahProduk(
            String kodeLama,
            String kodeBarang,
            String nama,
            long harga,
            String satuan,
            int stokMinimum,
            boolean dapatDiretur
    ) throws SQLException {

        if (kodeLama == null || kodeLama.isBlank()) {
            throw new IllegalArgumentException("Kode produk lama tidak valid.");
        }

        validasiProduk(kodeBarang, nama, harga, satuan, stokMinimum);

        String kodeSebelumnya = kodeLama.trim();
        String kodeBaru = kodeBarang.trim();

        if (!kodeBaru.equals(kodeSebelumnya)) {
            throw new IllegalArgumentException(
                    "Kode barang tidak dapat diubah."
            );
        }

        Produk ditemukan = produkDAO.ambilByKode(kodeSebelumnya);

        if (ditemukan == null || !ditemukan.isAktif()) {
            throw new IllegalArgumentException("Produk tidak ditemukan atau sudah nonaktif.");
        }

        // Kategori dan status aktif tetap dipertahankan.
        Produk produk = new Produk(
                kodeSebelumnya,
                nama.trim(),
                ditemukan.getKategori(),
                harga,
                satuan.trim(),
                stokMinimum,
                dapatDiretur,
                ditemukan.isAktif()
        );

        if (!produkDAO.ubah(produk)) {
            throw new IllegalArgumentException("Data produk gagal diperbarui.");
        }
    }

    // Menonaktifkan produk tanpa menghapus riwayatnya.
    public void hapusProduk(String kodeBarang) throws SQLException {

        if (kodeBarang == null || kodeBarang.isBlank()) {
            throw new IllegalArgumentException("Pilih produk yang ingin dihapus.");
        }

        String kodeBersih = kodeBarang.trim();

        Produk produk = produkDAO.cariKode(kodeBersih);

        if (produk == null) {
            throw new IllegalArgumentException("Produk tidak ditemukan.");
        }

        if (!produkDAO.nonaktifkan(kodeBersih)) {
            throw new IllegalArgumentException("Produk gagal dinonaktifkan.");
        }
    }

    // Validasi seluruh data produk.
    private void validasiProduk(
            String kodeBarang,
            String nama,
            long harga,
            String satuan,
            int stokMinimum
    ) {

        if (kodeBarang == null || kodeBarang.isBlank()) {
            throw new IllegalArgumentException("Kode barang wajib diisi.");
        }

        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException("Nama produk wajib diisi.");
        }

        if (harga < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif.");
        }

        if (satuan == null || satuan.isBlank()) {
            throw new IllegalArgumentException("Satuan produk wajib diisi.");
        }

        if (stokMinimum < 0) {
            throw new IllegalArgumentException("Stok minimum tidak boleh negatif.");
        }
    }


    // Mengambil stok aktual produk dari seluruh stok lot.
    public int getStokProduk(String kodeBarang) throws SQLException {
        return produkDAO.hitungStok(kodeBarang);
    }

}
