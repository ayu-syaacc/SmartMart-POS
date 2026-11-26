
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

    // Mengambil seluruh produk beserta stoknya.
    public List<Produk> getAllProduk() throws SQLException {
        return produkDAO.ambilSemua();
    }

    // Mencari produk berdasarkan kode barang.
    public Produk cariProduk(String kodeBarang) throws SQLException {
        if (kodeBarang == null || kodeBarang.isBlank()) {
            throw new IllegalArgumentException(
                    "Kode barang wajib diisi."
            );
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

        validasiProduk(
                kodeBarang,
                nama,
                harga,
                satuan,
                stokMinimum
        );

        String kodeBersih = kodeBarang.trim();

        if (produkDAO.cariKode(kodeBersih) != null) {
            throw new IllegalArgumentException(
                    "Kode barang sudah digunakan."
            );
        }

        Produk produk = new Produk(
                kodeBersih,
                nama.trim(),
                harga,
                satuan.trim(),
                stokMinimum,
                dapatDiretur
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
            throw new IllegalArgumentException(
                    "Kode produk lama tidak valid."
            );
        }

        validasiProduk(
                kodeBarang,
                nama,
                harga,
                satuan,
                stokMinimum
        );

        String kodeBaru = kodeBarang.trim();
        String kodeSebelumnya = kodeLama.trim();

        Produk ditemukan = produkDAO.cariKode(kodeSebelumnya);

        if (ditemukan == null) {
            throw new IllegalArgumentException(
                    "Produk tidak ditemukan."
            );
        }

        if (!kodeBaru.equals(kodeSebelumnya)
                && produkDAO.cariKode(kodeBaru) != null) {
            throw new IllegalArgumentException(
                    "Kode barang sudah digunakan produk lain."
            );
        }

        Produk produk = new Produk(
                kodeBaru,
                nama.trim(),
                harga,
                satuan.trim(),
                stokMinimum,
                dapatDiretur
        );

        produkDAO.ubah(kodeSebelumnya, produk);
    }

    // Menghapus produk.
    public void hapusProduk(String kodeBarang) throws SQLException {

        if (kodeBarang == null || kodeBarang.isBlank()) {
            throw new IllegalArgumentException(
                    "Pilih produk yang ingin dihapus."
            );
        }

        Produk produk = produkDAO.cariKode(kodeBarang.trim());

        if (produk == null) {
            throw new IllegalArgumentException(
                    "Produk tidak ditemukan."
            );
        }

        produkDAO.hapus(kodeBarang.trim());
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
            throw new IllegalArgumentException(
                    "Kode barang wajib diisi."
            );
        }

        if (nama == null || nama.isBlank()) {
            throw new IllegalArgumentException(
                    "Nama produk wajib diisi."
            );
        }

        if (harga < 0) {
            throw new IllegalArgumentException(
                    "Harga tidak boleh negatif."
            );
        }

        if (satuan == null || satuan.isBlank()) {
            throw new IllegalArgumentException(
                    "Satuan produk wajib diisi."
            );
        }

        if (stokMinimum < 0) {
            throw new IllegalArgumentException(
                    "Stok minimum tidak boleh negatif."
            );
        }
    }
}
