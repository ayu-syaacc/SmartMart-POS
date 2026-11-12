
-- =========================================
-- SMARTMART POS
-- INITIAL DATA (SEED)
-- =========================================

-- Aturan diskon FreshSave
INSERT INTO aturan_freshsave (batas_hari, persen_diskon)
SELECT 7, 10
WHERE NOT EXISTS (
    SELECT 1 FROM aturan_freshsave WHERE batas_hari = 7
);

INSERT INTO aturan_freshsave (batas_hari, persen_diskon)
SELECT 3, 20
WHERE NOT EXISTS (
    SELECT 1 FROM aturan_freshsave WHERE batas_hari = 3
);

INSERT INTO aturan_freshsave (batas_hari, persen_diskon)
SELECT 1, 30
WHERE NOT EXISTS (
    SELECT 1 FROM aturan_freshsave WHERE batas_hari = 1
);

-- Produk contoh untuk demo kasir
-- INSERT OR IGNORE: aman dijalankan berulang, produk yang sudah ada tidak diubah
INSERT OR IGNORE INTO produk
    (kode_barang, nama, harga, satuan, stok_minimum, dapat_diretur, kategori, aktif)
VALUES
    ('8990001000011', 'Air Mineral 600 ml',        3500, 'PCS', 24, 1, 'Minuman', 1),
    ('8990001000028', 'Teh Botol 350 ml',          5000, 'PCS', 24, 1, 'Minuman', 1),
    ('8990001000035', 'Kopi Susu Kaleng 240 ml',   8500, 'PCS', 12, 1, 'Minuman', 1),
    ('8990001000042', 'Jus Jeruk 250 ml',          7000, 'PCS', 12, 1, 'Minuman', 1),
    ('8990001000059', 'Minuman Isotonik 500 ml',   9000, 'PCS', 12, 1, 'Minuman', 1),
    ('8990002000018', 'Mi Instan Goreng',          3200, 'PCS', 40, 1, 'Makanan', 1),
    ('8990002000025', 'Biskuit Cokelat 120 g',     9500, 'PCS', 20, 1, 'Makanan', 1),
    ('8990002000032', 'Keripik Kentang 68 g',     11000, 'PCS', 15, 1, 'Makanan', 1),
    ('8990002000049', 'Roti Tawar',               16500, 'PCS', 10, 1, 'Makanan', 1),
    ('8990002000056', 'Beras Premium 5 kg',       72000, 'PCS',  8, 1, 'Makanan', 1),
    ('8990003000017', 'Apel Fuji',                38000, 'KG',   5, 0, 'Segar',   1),
    ('8990003000024', 'Jeruk Mandarin',           32000, 'KG',   5, 0, 'Segar',   1),
    ('8990003000031', 'Tomat',                    14000, 'KG',   5, 0, 'Segar',   1),
    ('8990003000048', 'Daging Ayam Fillet',       48000, 'KG',   5, 0, 'Segar',   1),
    ('8990003000055', 'Telur Ayam',               29000, 'KG',   5, 0, 'Segar',   1),
    ('8990004000016', 'Susu UHT Cokelat 200 ml',   6000, 'PCS', 24, 1, 'Susu',    1),
    ('8990004000023', 'Susu UHT Full Cream 1 L',  19500, 'PCS', 12, 1, 'Susu',    1),
    ('8990004000030', 'Yoghurt Plain 150 ml',      8000, 'PCS', 12, 0, 'Susu',    1),
    ('8990004000047', 'Keju Slice 10 lembar',     22000, 'PCS',  8, 1, 'Susu',    1),
    ('8990005000015', 'Sabun Mandi Cair 450 ml',  21000, 'PCS', 10, 1, 'Lainnya', 1),
    ('8990005000022', 'Tisu Wajah 250 lembar',    14500, 'PCS', 10, 1, 'Lainnya', 1);
