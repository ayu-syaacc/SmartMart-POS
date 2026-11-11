
-- =========================================
-- SMARTMART POS
-- DATABASE SCHEMA
-- =========================================

PRAGMA foreign_keys = ON;

-- 1. TABEL PEGAWAI
CREATE TABLE IF NOT EXISTS pegawai (
    id_pegawai INTEGER PRIMARY KEY AUTOINCREMENT,
    nama TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    salt TEXT NOT NULL,
    peran TEXT NOT NULL CHECK (peran IN ('ADMIN', 'KASIR')),
    aktif INTEGER NOT NULL DEFAULT 1 CHECK (aktif IN (0, 1))
);

-- 2. TABEL PRODUK
CREATE TABLE IF NOT EXISTS produk (
    kode_barang TEXT PRIMARY KEY,
    nama TEXT NOT NULL,
    harga INTEGER NOT NULL CHECK (harga >= 0),
    satuan TEXT NOT NULL,
    stok_minimum INTEGER NOT NULL DEFAULT 0,
    dapat_diretur INTEGER NOT NULL DEFAULT 1 CHECK (dapat_diretur IN (0, 1))
);

-- 3. TABEL SUPPLIER
CREATE TABLE IF NOT EXISTS supplier (
    id_supplier INTEGER PRIMARY KEY AUTOINCREMENT,
    nama TEXT NOT NULL,
    kontak TEXT,
    alamat TEXT
);

-- 4. TABEL MEMBER
CREATE TABLE IF NOT EXISTS member (
    id_member INTEGER PRIMARY KEY AUTOINCREMENT,
    no_member TEXT NOT NULL UNIQUE,
    nama TEXT NOT NULL,
    kontak TEXT,
    poin INTEGER NOT NULL DEFAULT 0 CHECK (poin >= 0)
);

-- 5. TABEL PENERIMAAN BARANG
CREATE TABLE IF NOT EXISTS penerimaan_barang (
    id_penerimaan INTEGER PRIMARY KEY AUTOINCREMENT,
    id_supplier INTEGER NOT NULL,
    id_pegawai INTEGER NOT NULL,
    tanggal TEXT NOT NULL,
    FOREIGN KEY (id_supplier) REFERENCES supplier(id_supplier),
    FOREIGN KEY (id_pegawai) REFERENCES pegawai(id_pegawai)
);

-- 6. TABEL DETAIL PENERIMAAN
CREATE TABLE IF NOT EXISTS detail_penerimaan (
    id_detail INTEGER PRIMARY KEY AUTOINCREMENT,
    id_penerimaan INTEGER NOT NULL,
    kode_barang TEXT NOT NULL,
    jumlah_dipesan INTEGER NOT NULL,
    jumlah_diterima INTEGER NOT NULL,
    jumlah_rusak INTEGER NOT NULL DEFAULT 0,
    tgl_kedaluwarsa TEXT,
    FOREIGN KEY (id_penerimaan) REFERENCES penerimaan_barang(id_penerimaan),
    FOREIGN KEY (kode_barang) REFERENCES produk(kode_barang)
);

-- 7. TABEL STOK LOT
CREATE TABLE IF NOT EXISTS stok_lot (
    id_lot INTEGER PRIMARY KEY AUTOINCREMENT,
    kode_barang TEXT NOT NULL,
    id_detail INTEGER,
    tgl_kedaluwarsa TEXT,
    jumlah_sisa INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (kode_barang) REFERENCES produk(kode_barang),
    FOREIGN KEY (id_detail) REFERENCES detail_penerimaan(id_detail)
);

-- 8. TABEL ATURAN FRESHSAVE
CREATE TABLE IF NOT EXISTS aturan_freshsave (
    id_aturan INTEGER PRIMARY KEY AUTOINCREMENT,
    batas_hari INTEGER NOT NULL,
    persen_diskon INTEGER NOT NULL
);

-- 9. TABEL TRANSAKSI
CREATE TABLE IF NOT EXISTS transaksi (
    id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT,
    waktu TEXT NOT NULL,
    id_pegawai INTEGER NOT NULL,
    id_member INTEGER,
    subtotal INTEGER NOT NULL DEFAULT 0,
    diskon INTEGER NOT NULL DEFAULT 0,
    poin_dipakai INTEGER NOT NULL DEFAULT 0,
    poin_diperoleh INTEGER NOT NULL DEFAULT 0,
    diskon_poin INTEGER NOT NULL DEFAULT 0,
    total INTEGER NOT NULL DEFAULT 0,
    anggaran INTEGER,
    status TEXT NOT NULL DEFAULT 'BERJALAN',
    FOREIGN KEY (id_pegawai) REFERENCES pegawai(id_pegawai),
    FOREIGN KEY (id_member) REFERENCES member(id_member)
);

-- 10. TABEL DETAIL TRANSAKSI
CREATE TABLE IF NOT EXISTS detail_transaksi (
    id_detail_trx INTEGER PRIMARY KEY AUTOINCREMENT,
    id_transaksi INTEGER NOT NULL,
    kode_barang TEXT NOT NULL,
    id_lot INTEGER,
    jumlah REAL NOT NULL,
    harga_awal INTEGER NOT NULL,
    diskon INTEGER NOT NULL DEFAULT 0,
    sumber_diskon TEXT,
    subtotal INTEGER NOT NULL,
    FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi),
    FOREIGN KEY (kode_barang) REFERENCES produk(kode_barang),
    FOREIGN KEY (id_lot) REFERENCES stok_lot(id_lot)
);

-- 11. TABEL PEMBAYARAN
CREATE TABLE IF NOT EXISTS pembayaran (
    id_pembayaran INTEGER PRIMARY KEY AUTOINCREMENT,
    id_transaksi INTEGER NOT NULL,
    metode TEXT NOT NULL,
    nominal INTEGER NOT NULL,
    kembalian INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL,
    FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi)
);

-- 12. TABEL RETUR
CREATE TABLE IF NOT EXISTS retur (
    id_retur INTEGER PRIMARY KEY AUTOINCREMENT,
    id_transaksi INTEGER NOT NULL,
    id_kasir INTEGER NOT NULL,
    id_admin INTEGER NOT NULL,
    waktu TEXT NOT NULL,
    alasan TEXT NOT NULL,
    total_refund INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (id_transaksi) REFERENCES transaksi(id_transaksi),
    FOREIGN KEY (id_kasir) REFERENCES pegawai(id_pegawai),
    FOREIGN KEY (id_admin) REFERENCES pegawai(id_pegawai)
);

-- 13. TABEL DETAIL RETUR
CREATE TABLE IF NOT EXISTS detail_retur (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_retur INTEGER NOT NULL,
    id_detail_trx INTEGER NOT NULL,
    jumlah REAL NOT NULL,
    kondisi TEXT NOT NULL,
    FOREIGN KEY (id_retur) REFERENCES retur(id_retur),
    FOREIGN KEY (id_detail_trx) REFERENCES detail_transaksi(id_detail_trx)
);

-- 14. TABEL STOK LOG
CREATE TABLE IF NOT EXISTS stok_log (
    id_log INTEGER PRIMARY KEY AUTOINCREMENT,
    waktu TEXT NOT NULL,
    kode_barang TEXT NOT NULL,
    id_lot INTEGER,
    jenis TEXT NOT NULL,
    jumlah REAL NOT NULL,
    id_pegawai INTEGER NOT NULL,
    alasan TEXT,
    FOREIGN KEY (kode_barang) REFERENCES produk(kode_barang),
    FOREIGN KEY (id_lot) REFERENCES stok_lot(id_lot),
    FOREIGN KEY (id_pegawai) REFERENCES pegawai(id_pegawai)
);

-- 15. TABEL LOG AKTIVITAS
CREATE TABLE IF NOT EXISTS log_aktivitas (
    id_aktivitas INTEGER PRIMARY KEY AUTOINCREMENT,
    waktu TEXT NOT NULL,
    id_pegawai INTEGER NOT NULL,
    aksi TEXT NOT NULL,
    keterangan TEXT,
    FOREIGN KEY (id_pegawai) REFERENCES pegawai(id_pegawai)
);
