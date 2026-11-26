# SmartMart POS (Java Swing)

Aplikasi kasir supermarket sesuai mockup *SmartMart POS: Smart Retail Operations & Management System*.
Dibuat dengan Java Swing (JFrame), tanpa library tambahan.

## Cara menjalankan

Syarat: Java (JDK/JRE) 11 atau lebih baru.

- Jalankan langsung: `java -jar SmartMartPOS.jar` (atau klik `run.bat` di Windows / `./run.sh` di Linux/Mac).
- Kompilasi ulang: `build.bat` (Windows) atau `./build.sh` (Linux/Mac).
- Lewat IDE (NetBeans / IntelliJ / Eclipse): buat proyek Java dari folder `src`, main class `smartmart.Main`.

Data tersimpan otomatis di folder `data/` (dibuat saat pertama dijalankan, di folder tempat aplikasi dijalankan).
Hapus folder `data/` untuk mengembalikan data demo, atau pakai tombol **Reset Data Demo** di menu Pengaturan.

## Akun demo

| Peran | Username | Password |
|-------|----------|----------|
| Kasir | kasir01  | kasir123 |
| Kasir | kasir02  | kasir123 |
| Admin | admin01  | admin123 |

`kasir03` sengaja nonaktif (contoh akun nonaktif). Password disimpan sebagai hash SHA-256 dengan salt.

## Fitur (sesuai layar di mockup)

Kasir: Login, Transaksi (grid produk, kategori, scan barcode/ketik nama + Enter, input berat produk per kg,
keranjang, pilih member), Pembayaran (Tunai/QRIS/Debit, numpad, uang pas, kembalian, tukar poin, status PENDING > PAID),
Pratinjau Struk (cetak, simpan PDF), Riwayat Transaksi, SmartReturn, VoidGuard, Member (daftar, riwayat belanja, poin).

Admin (hanya fitur manajemen; fitur kasir tidak tersedia di akun Admin): Dashboard, Persediaan (tambah/ubah produk), ReceiveCheck (penerimaan jadi lot),
StockAlert (status lot, buang lot kedaluwarsa, kurangi stok manual), LossGuard, FreshSave, Laporan (ekspor CSV),
Pegawai (akun + log aktivitas), Pengaturan.

## Aturan bisnis yang diterapkan

- Stok dikelola per lot; penjualan memakai lot terdekat kedaluwarsa lebih dulu. Lot kedaluwarsa (0 hari) otomatis diblokir.
- FreshSave: sisa <= 7 hari 10%, <= 3 hari 20%, 1 hari 30% (dapat diubah admin).
- Diskon member 10%. Diskon tidak ditumpuk: tiap barang memakai yang terbesar; bila sama, sumber dicatat FRESHSAVE.
- Poin: 1 poin per Rp10.000 belanja bersih, diberikan setelah PAID. Tukar 100 poin = Rp5.000, kelipatan 100, maksimal 20% dari subtotal setelah diskon.
- Retur (SmartReturn): maksimal 3 hari, wajib ID transaksi, barang segar/cepat basi dan barang FreshSave tidak dapat diretur,
  jumlah tidak boleh melebihi pembelian, wajib password admin. Stok layak kembali ke lot asal; poin member dikurangi.
- Void (VoidGuard): setelah dibayar wajib alasan + password admin, hanya hari yang sama, transaksi yang sudah diretur tidak dapat di-void.
  Stok kembali ke lot asal dan poin disesuaikan. Tidak ada fitur edit transaksi.
- Setiap perubahan stok tercatat di LossGuard; percobaan password admin yang salah tercatat di log aktivitas.
- Pembayaran QRIS/debit hanya simulasi (tidak ada integrasi pembayaran sungguhan).

## Data contoh

Data demo dibuat relatif terhadap tanggal hari ini (lot dengan sisa 0, 1, 2, 3 hari, dst.), ditambah transaksi 7 hari terakhir.
Transaksi kemarin atas nama Siti Rahma dengan nomor berakhiran **-0042** bisa dipakai untuk mencoba SmartReturn
(Susu UHT 1L dapat diretur; Apel, Roti, Yoghurt ditolak otomatis).

## Struktur

```
src/smartmart/   kode sumber (Models, Db = aturan bisnis + penyimpanan, Seed, Ui, halaman-halaman, Main)
SmartMartPOS.jar hasil kompilasi siap jalan
build.* / run.*  skrip kompilasi dan menjalankan
```
