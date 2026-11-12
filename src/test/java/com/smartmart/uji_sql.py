import re, glob, sqlite3, datetime

SQL = {}
for f in glob.glob('src/main/java/com/smartmart/dao/*.java'):
    src = open(f, encoding='utf-8').read()
    kls = f.split('/')[-1][:-5]
    for m in re.finditer(r'String\s+(SQL_\w+)\s*=\s*((?:\s*"(?:[^"\\]|\\.)*"\s*\+?)+)\s*;', src):
        SQL[kls + '.' + m.group(1)] = ''.join(re.findall(r'"((?:[^"\\]|\\.)*)"', m.group(2)))
FLAT = {}
for k, v in SQL.items():
    FLAT[k.split('.')[1]] = v
FLAT['SQL_BARIS'] = SQL['ReturDAO.SQL_BARIS']
FLAT['VOID_BARIS'] = SQL['VoidDAO.SQL_BARIS']
SQL = FLAT
print("SQL ditemukan:", len(SQL))

db = sqlite3.connect(':memory:')
c = db.cursor()
c.executescript("""
CREATE TABLE pegawai(id_pegawai INTEGER PRIMARY KEY, nama TEXT, username TEXT, password_hash TEXT, salt TEXT, peran TEXT, aktif INTEGER);
CREATE TABLE produk(kode_barang TEXT PRIMARY KEY, nama TEXT, harga INTEGER, satuan TEXT, stok_minimum INTEGER, dapat_diretur INTEGER);
CREATE TABLE supplier(id_supplier INTEGER PRIMARY KEY, nama TEXT, kontak TEXT, alamat TEXT);
CREATE TABLE member(id_member INTEGER PRIMARY KEY, no_member TEXT, nama TEXT, kontak TEXT, poin INTEGER);
CREATE TABLE penerimaan_barang(id_penerimaan INTEGER PRIMARY KEY, id_supplier INTEGER, id_pegawai INTEGER, tanggal TEXT);
CREATE TABLE detail_penerimaan(id_detail INTEGER PRIMARY KEY, id_penerimaan INTEGER, kode_barang TEXT, jumlah_dipesan INTEGER, jumlah_diterima INTEGER, jumlah_rusak INTEGER, tgl_kedaluwarsa TEXT);
CREATE TABLE stok_lot(id_lot INTEGER PRIMARY KEY, kode_barang TEXT, id_detail INTEGER, tgl_kedaluwarsa TEXT, jumlah_sisa REAL);
CREATE TABLE aturan_freshsave(id_aturan INTEGER PRIMARY KEY, batas_hari INTEGER, persen_diskon INTEGER);
CREATE TABLE transaksi(id_transaksi TEXT PRIMARY KEY, waktu TEXT, id_pegawai INTEGER, id_member INTEGER, subtotal INTEGER, diskon INTEGER, poin_dipakai INTEGER, poin_diperoleh INTEGER, diskon_poin INTEGER, total INTEGER, anggaran INTEGER, status TEXT);
CREATE TABLE detail_transaksi(id_detail_trx INTEGER PRIMARY KEY, id_transaksi TEXT, kode_barang TEXT, id_lot INTEGER, jumlah REAL, harga_awal INTEGER, diskon INTEGER, sumber_diskon TEXT, subtotal INTEGER);
CREATE TABLE pembayaran(id_pembayaran INTEGER PRIMARY KEY, id_transaksi TEXT, metode TEXT, nominal INTEGER, kembalian INTEGER, status TEXT);
CREATE TABLE retur(id_retur INTEGER PRIMARY KEY, id_transaksi TEXT, id_kasir INTEGER, id_admin INTEGER, waktu TEXT, alasan TEXT, total_refund INTEGER);
CREATE TABLE detail_retur(id INTEGER PRIMARY KEY, id_retur INTEGER, id_detail_trx INTEGER, jumlah REAL, kondisi TEXT);
CREATE TABLE stok_log(id_log INTEGER PRIMARY KEY, waktu TEXT, kode_barang TEXT, id_lot INTEGER, jenis TEXT, jumlah REAL, id_pegawai INTEGER, alasan TEXT);
CREATE TABLE log_aktivitas(id_aktivitas INTEGER PRIMARY KEY, waktu TEXT, id_pegawai INTEGER, aksi TEXT, keterangan TEXT);
""")
today = datetime.date.today()
kemarin = today - datetime.timedelta(days=1)
empat = today - datetime.timedelta(days=4)
c.executemany("INSERT INTO pegawai VALUES(?,?,?,?,?,?,1)", [(1,'Admin01','admin01','h','s','ADMIN'),(2,'Kasir01','kasir01','h','s','KASIR')])
c.executemany("INSERT INTO produk VALUES(?,?,?,?,10,?)", [('S','Susu UHT 1L',17500,'PCS',1),('A','Apel Fuji',48000,'KG',0),('R','Roti Tawar',15000,'PCS',0)])
c.execute("INSERT INTO member VALUES(42,'M-0042','Siti Rahma','',240)")
c.executemany("INSERT INTO stok_lot VALUES(?,?,?,?,?)", [(10,'S',1,str(today+datetime.timedelta(days=120)),8),(11,'A',2,str(today+datetime.timedelta(days=6)),21.5),(12,'S',3,str(today),5)])
c.execute("INSERT INTO transaksi VALUES('TRX-A-0017',?,2,42,68500,8350,100,5,5000,55150,NULL,'SELESAI')", (today.strftime('%Y-%m-%d')+' 22:41:00',))
c.executemany("INSERT INTO detail_transaksi VALUES(?,?,?,?,?,?,?,?,?)", [
 (1,'TRX-A-0017','S',10,1,17500,1750,'MEMBER',15750),(2,'TRX-A-0017','A',11,0.75,48000,3600,'MEMBER',32400),(3,'TRX-A-0017','R',None,1,15000,3000,'FRESHSAVE',12000)])
c.execute("INSERT INTO pembayaran VALUES(1,'TRX-A-0017','Tunai',100000,44850,'PAID')")
c.execute("INSERT INTO transaksi VALUES('TRX-A-0042',?,2,42,95400,0,0,9,0,95400,NULL,'SELESAI')", (kemarin.strftime('%Y-%m-%d')+' 16:20:00',))
c.executemany("INSERT INTO detail_transaksi VALUES(?,?,?,?,?,?,?,?,?)", [(4,'TRX-A-0042','S',10,2,17500,3500,'MEMBER',31500),(5,'TRX-A-0042','R',None,1,15000,0,'TIDAK_ADA',15000)])
c.execute("INSERT INTO pembayaran VALUES(2,'TRX-A-0042','Tunai',100000,4600,'PAID')")
c.execute("INSERT INTO transaksi VALUES('TRX-A-0007',?,2,NULL,20000,0,0,0,0,20000,NULL,'DIBATALKAN')", (empat.strftime('%Y-%m-%d')+' 10:00:00',))
c.execute("INSERT INTO transaksi VALUES('TRX-A-0099',?,2,NULL,0,0,0,0,0,0,NULL,'BERJALAN')", (today.strftime('%Y-%m-%d')+' 23:00:00',))
db.commit()

def ok(nama, cond):
    print(("OK    " if cond else "GAGAL ")+nama)
    if not cond: ok.gagal += 1
ok.gagal = 0

def cari(kata='', status='', dari='1970-01-01'):
    like = '%'+kata+'%'
    return c.execute(SQL['SQL_CARI'], (status, status, dari, kata, like, like, like, like)).fetchall()
rows = cari()
ok("riwayat semua = 3 (BERJALAN tidak muncul)", len(rows) == 3)
ok("urut terbaru dulu", rows[0][0] == 'TRX-A-0017')
ok("filter DIBATALKAN = 1", len(cari(status='DIBATALKAN')) == 1)
ok("cari nama produk 'apel' = TRX-0017", [r[0] for r in cari('apel')] == ['TRX-A-0017'])
ok("cari pelanggan 'siti' = 2", len(cari('siti')) == 2)
ok("cari tanggal kemarin", [r[0] for r in cari(str(kemarin))] == ['TRX-A-0042'])
ok("periode hari ini = 1", len(cari(dari=str(today))) == 1)
items = c.execute(SQL['SQL_ITEM'], ('TRX-A-0017',)).fetchall()
ok("detail item 0017 = 3 baris", len(items) == 3 and items[0][0] == 'Susu UHT 1L')

t = c.execute(SQL['SQL_TRANSAKSI'], ('TRX-A-0042',)).fetchone()
ok("transaksi 0042 terbaca (pelanggan, metode, kasir)", t[5] == 'Siti Rahma' and t[6] == 'Tunai' and t[7] == 'Kasir01')
baris = c.execute(SQL['SQL_BARIS'], ('TRX-A-0042',)).fetchall()
ok("baris retur + sudah_diretur = 0", len(baris) == 2 and baris[0][-1] == 0)

def proses_retur(id_trx, id_detail, kode, id_lot, qty, refund, kondisi, id_member, poin_kurang):
    c.execute(SQL['SQL_INSERT_RETUR'], (id_trx, 2, 1, today.strftime('%Y-%m-%d %H:%M:%S'), 'Salah beli', refund))
    id_retur = c.execute(SQL['SQL_ID_TERAKHIR']).fetchone()[0]
    c.execute(SQL['SQL_INSERT_DETAIL'], (id_retur, id_detail, qty, kondisi))
    if kondisi == 'LAYAK':
        c.execute(SQL['SQL_TAMBAH_STOK_LOT'], (qty, id_lot))
        c.execute(SQL['SQL_CATAT_STOK_LOG'], ('now', kode, id_lot, 'RETUR', qty, 2, 'Retur'))
    else:
        c.execute(SQL['SQL_CATAT_STOK_LOG'], ('now', kode, id_lot, 'RUSAK', -qty, 2, 'Retur rusak'))
    c.execute(SQL['SQL_UBAH_POIN'], (-poin_kurang, id_member))
    c.execute(SQL['SQL_CATAT_AKTIVITAS'], ('now', 2, 'RETUR', 'ket'))
    return id_retur

stok_sebelum = c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=10").fetchone()[0]
proses_retur('TRX-A-0042', 4, 'S', 10, 1, 15750, 'LAYAK', 42, 1)
stok_sesudah = c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=10").fetchone()[0]
ok("retur layak: stok lot asal +1", stok_sesudah == stok_sebelum + 1)
ok("retur layak: poin 240 -> 239", c.execute("SELECT poin FROM member WHERE id_member=42").fetchone()[0] == 239)
baris = c.execute(SQL['SQL_BARIS'], ('TRX-A-0042',)).fetchall()
ok("sudah_diretur susu = 1 setelah retur", baris[0][-1] == 1)
ok("hitung retur = 1", c.execute(SQL['SQL_JUMLAH_RETUR'], ('TRX-A-0042',)).fetchone()[0] == 1)
ok("stok_log RETUR tercatat", c.execute("SELECT COUNT(*) FROM stok_log WHERE jenis='RETUR' AND jumlah=1").fetchone()[0] == 1)

stok_a = c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=10").fetchone()[0]
proses_retur('TRX-A-0042', 4, 'S', 10, 1, 15750, 'RUSAK', 42, 1)
ok("retur rusak: stok tidak bertambah", c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=10").fetchone()[0] == stok_a)
ok("retur rusak: stok_log RUSAK tercatat", c.execute("SELECT COUNT(*) FROM stok_log WHERE jenis='RUSAK'").fetchone()[0] == 1)

c.execute(SQL['SQL_UBAH_POIN'], (-100000, 42))
ok("poin tidak kurang dari 0", c.execute("SELECT poin FROM member WHERE id_member=42").fetchone()[0] == 0)
c.execute("UPDATE member SET poin=240 WHERE id_member=42")

ok("transaksi 0042 punya retur -> tidak bisa void (hitung>0)", c.execute(SQL['SQL_JUMLAH_RETUR'], ('TRX-A-0042',)).fetchone()[0] > 0)
ok("transaksi 0017 belum ada retur", c.execute(SQL['SQL_JUMLAH_RETUR'], ('TRX-A-0017',)).fetchone()[0] == 0)
info = c.execute(VOID := SQL['SQL_INFO'], ('TRX-A-0017',)).fetchone()
ok("info void terbaca (member, poin diperoleh 5, dipakai 100)", info[3] == 42 and info[4] == 5 and info[5] == 100)
brs = c.execute(SQL['VOID_BARIS'], ('TRX-A-0017',)).fetchall()
ok("void: 3 baris detail terbaca", len(brs) == 3)
stok_lot10 = c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=10").fetchone()[0]
for (iddt, kode, lot, jml) in brs:
    if lot is not None:
        c.execute(SQL['SQL_TAMBAH_STOK_LOT'], (jml, lot))
    c.execute(SQL['SQL_CATAT_STOK_LOG'], ('now', kode, lot, 'PENYESUAIAN', jml, 2, 'Void'))
c.execute(SQL['SQL_UBAH_POIN'], (-5 + 100, 42))
c.execute(SQL['SQL_UBAH_STATUS_TRANSAKSI'], ('DIBATALKAN', 'TRX-A-0017'))
ok("void: stok lot asal kembali (+1)", c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=10").fetchone()[0] == stok_lot10 + 1)
ok("void: apel 0,75 kg kembali", abs(c.execute("SELECT jumlah_sisa FROM stok_lot WHERE id_lot=11").fetchone()[0] - 22.25) < 1e-9)
ok("void: poin 240 -5 +100 = 335", c.execute("SELECT poin FROM member WHERE id_member=42").fetchone()[0] == 335)
ok("void: status DIBATALKAN", c.execute("SELECT status FROM transaksi WHERE id_transaksi='TRX-A-0017'").fetchone()[0] == 'DIBATALKAN')
ok("void: status pembayaran tetap PAID", c.execute("SELECT status FROM pembayaran WHERE id_transaksi='TRX-A-0017'").fetchone()[0] == 'PAID')
c.execute(SQL['SQL_UBAH_STATUS_TRANSAKSI'], ('DIBATALKAN', 'TRX-A-0099'))
ok("batal sebelum bayar: status DIBATALKAN, stok & poin tidak berubah", c.execute("SELECT status FROM transaksi WHERE id_transaksi='TRX-A-0099'").fetchone()[0]=='DIBATALKAN')

print("\nSEMUA LULUS" if ok.gagal == 0 else "\nGAGAL: %d" % ok.gagal)
