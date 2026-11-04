**SmartMart-POS**
Java-based supermarket Point of Sale (POS) system featuring intelligent budgeting, automated discounts, inventory management, and transaction reporting.
	

**Fitur Utama**
1. Front office (kasir): ScanBarcode \& Search Product, POS Transaction, Transaction History, SmartReturn, SmartBudget, Member
2. Back office (admin): ReceiveCheck (StokLot), LossGuard, FreshSave, StockAlert
4. Pengendalian transaksi: VoidGuard
5. Manajemen: Login \& Role, SalesReport

**Cara Menjalankan**
1. Pastikan JDK 21 dan NetBeans sudah terpasang.
2. Clone repo:
		git clone <url-repo>
3. Pindah ke branch develop:
		git checkout develop
4. Buka NetBeans, pilih File > Open Project, lalu pilih folder hasil clone.
5. Klik kanan project > Reload POM agar dependency terunduh.
6. Klik Run. File smartmart.db dibuat otomatis saat pertama kali aplikasi berjalan.

###### **Aturan Kerja Tim**
1. Sebelum mulai bekerja, jalankan
   git pull origin develop
2. Satu fitur dikerjakan satu orang. Hindari mengedit form Swing yang sama secara bersamaan.
3. Perubahan struktur tabel (database/schema.sql) dilakukan lewat Pull Request tersendiri.
4. File database (\*.db) tidak di-commit.



