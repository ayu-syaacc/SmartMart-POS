package smartmart;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/** Data contoh awal (mengikuti mockup). Tanggal dibuat relatif terhadap hari ini. */
final class Seed {
    private Seed() { }

    static Data create() {
        Data d = new Data();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        user(d, "Admin01", "admin01", "ADMIN", "admin123", true);
        user(d, "Kasir01", "kasir01", "KASIR", "kasir123", true);
        user(d, "Kasir02", "kasir02", "KASIR", "kasir123", true);
        user(d, "Kasir03", "kasir03", "KASIR", "kasir123", false);

        prod(d, "899100101", "Susu UHT 1L", "Susu", 17500, "PCS", 10, true);
        prod(d, "899100102", "Apel Fuji", "Segar", 48000, "KG", 10, false);
        prod(d, "899100103", "Roti Tawar", "Makanan", 15000, "PCS", 10, false);
        prod(d, "899100104", "Yoghurt Plain 125g", "Susu", 10000, "PCS", 15, false);
        prod(d, "899100105", "Salad Sayur", "Segar", 12000, "PCS", 10, false);
        prod(d, "899100106", "Pisang", "Segar", 28000, "KG", 8, false);
        prod(d, "899100107", "Sabun Cair 250ml", "Lainnya", 21900, "PCS", 12, true);
        prod(d, "899100108", "Keju Slice", "Susu", 18500, "PCS", 10, true);
        prod(d, "899100109", "Daging Ayam Fillet", "Segar", 38000, "PCS", 10, false);
        prod(d, "899100110", "Bayam Ikat", "Segar", 5000, "PCS", 10, false);
        prod(d, "899100111", "Teh Celup 25", "Minuman", 12500, "PCS", 10, true);
        prod(d, "899100112", "Mie Instan Pedas", "Makanan", 3500, "PCS", 20, true);
        prod(d, "899100113", "Tepung Premium", "Lainnya", 14500, "PCS", 10, true);
        prod(d, "899100114", "Selai Kacang", "Makanan", 24500, "PCS", 10, true);
        prod(d, "899100115", "Air Mineral 600ml", "Minuman", 4000, "PCS", 24, true);

        lot(d, "A-231", "899100104", 30, today.plusDays(1));
        lot(d, "B-118", "899100103", 37, today.plusDays(2));
        lot(d, "C-077", "899100105", 18, today.plusDays(3));
        lot(d, "D-042", "899100109", 24, today.plusDays(5));
        lot(d, "C-081", "899100110", 15, today.plusDays(6));
        lot(d, "E-019", "899100108", 6, today);
        lot(d, "F-304", "899100101", 8, today.plusDays(120));
        lot(d, "G-210", "899100102", 21.5, today.plusDays(6));
        lot(d, "G-215", "899100106", 14.2, today.plusDays(9));
        lot(d, "H-101", "899100107", 46, today.plusDays(400));
        lot(d, "H-102", "899100111", 60, today.plusDays(300));
        lot(d, "H-103", "899100112", 80, today.plusDays(200));
        lot(d, "H-104", "899100113", 30, today.plusDays(250));
        lot(d, "H-105", "899100114", 25, today.plusDays(180));
        lot(d, "H-106", "899100115", 120, today.plusDays(365));

        member(d, "M-0042", "Siti Rahma", 240, today.minusMonths(8));
        member(d, "M-0038", "Budi Santoso", 90, today.minusMonths(9));
        member(d, "M-0031", "Rina Aprilia", 515, today.minusMonths(11));
        member(d, "M-0027", "Dewi Lestari", 60, today.minusMonths(13));
        member(d, "M-0019", "Agus Pratama", 130, today.minusMonths(16));
        member(d, "M-0012", "Maya Putri", 305, today.minusMonths(20));

        // riwayat poin contoh
        pt(d, "M-0042", today.minusDays(14), "Belanja TRX contoh", 11);
        pt(d, "M-0042", today.minusDays(21), "Belanja TRX contoh", 7);
        pt(d, "M-0042", today.minusDays(24), "Retur Susu UHT", -1);
        pt(d, "M-0042", today.minusDays(30), "Belanja TRX contoh", 9);

        // log stok contoh
        log(d, now.minusMinutes(35), "KEDALUWARSA", "899100108", "E-019", -6, 111000, "Admin01", "Lot dibuang (contoh)");
        log(d, now.minusMinutes(110), "RUSAK", "899100105", "C-077", -2, 24000, "Admin01", "Layu, tidak layak");
        log(d, now.minusMinutes(210), "MASUK", "899100101", "F-304", 43, 0, "Admin01", "ReceiveCheck PO-2611-014");
        log(d, now.minusMinutes(300), "HILANG", "899100110", "C-081", -3, 15000, "Admin01", "Tidak ditemukan saat cek");
        log(d, now.minusMinutes(380), "DIRETUR", "899100101", "F-304", 1, 0, "Kasir01", "Retur disetujui admin");
        log(d, now.minusMinutes(450), "RUSAK", "899100101", "F-304", -2, 35000, "Admin01", "Rusak saat diterima");

        ActLog al = new ActLog();
        al.time = now.minusHours(2);
        al.actor = "Sistem";
        al.text = "Data demo SmartMart dibuat";
        d.acts.add(al);

        // penerimaan contoh
        Recv r = new Recv();
        r.id = "RCV-0013";
        r.supplier = "PT Segar Makmur";
        r.po = "PO-2611-014";
        r.receiver = "Admin01";
        r.date = today;
        r.discrepancyValue = 5 * 17500;
        d.recvs.add(r);

        // transaksi contoh 7 hari terakhir
        Random rnd = new Random(42);
        String[] codes = {"899100101", "899100103", "899100107", "899100111", "899100112", "899100113", "899100114",
                "899100115", "899100106", "899100102"};
        String[] cashiers = {"Kasir01", "Kasir02"};
        String[] methods = {"Tunai", "QRIS", "Debit"};
        for (int back = 6; back >= 0; back--) {
            LocalDate day = today.minusDays(back);
            int n = 6 + rnd.nextInt(7);
            int counter = 0;
            for (int i = 0; i < n; i++) {
                LocalDateTime t = day.atTime(LocalTime.of(8 + rnd.nextInt(13), rnd.nextInt(60)));
                if (t.isAfter(now.minusMinutes(5))) continue;
                Member m = rnd.nextInt(3) == 0 ? d.members.get(rnd.nextInt(d.members.size())) : null;
                Sale s = new Sale();
                s.time = t;
                s.id = "TRX-" + day.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", ++counter);
                s.cashier = cashiers[rnd.nextInt(2)];
                s.method = methods[rnd.nextInt(3)];
                if (m != null) { s.memberNo = m.no; s.memberName = m.name; }
                int lines = 1 + rnd.nextInt(4);
                for (int k = 0; k < lines; k++) {
                    Product p = prod(d, codes[rnd.nextInt(codes.length)]);
                    double q = p.perKg() ? 0.25 * (1 + rnd.nextInt(6)) : 1 + rnd.nextInt(3);
                    s.items.add(item(p, q, m != null ? 10 : 0, m != null ? "MEMBER" : ""));
                }
                finish(s);
                d.sales.add(s);
            }
        }

        // transaksi contoh untuk uji retur (kemarin, Siti Rahma)
        LocalDate y = today.minusDays(1);
        Sale s = new Sale();
        s.time = y.atTime(16, 20);
        s.id = "TRX-" + y.format(DateTimeFormatter.BASIC_ISO_DATE) + "-0042";
        s.cashier = "Kasir01";
        s.memberNo = "M-0042";
        s.memberName = "Siti Rahma";
        s.items.add(item(prod(d, "899100101"), 2, 10, "MEMBER"));
        s.items.add(item(prod(d, "899100102"), 0.75, 10, "FRESHSAVE"));
        s.items.add(item(prod(d, "899100103"), 1, 20, "FRESHSAVE"));
        s.items.add(item(prod(d, "899100104"), 1, 30, "FRESHSAVE"));
        finish(s);
        d.sales.add(s);
        return d;
    }

    private static Product prod(Data d, String code) {
        for (Product p : d.products) if (p.code.equals(code)) return p;
        return null;
    }

    private static SaleItem item(Product p, double qty, int pct, String src) {
        SaleItem it = new SaleItem();
        it.productCode = p.code;
        it.name = p.name;
        it.lotId = "-";
        it.unit = p.unit;
        it.qty = qty;
        it.unitPrice = p.price;
        it.gross = Math.round(qty * p.price);
        it.discPct = pct;
        it.source = pct > 0 ? src : "";
        it.discAmt = Math.round(it.gross * pct / 100.0);
        it.net = it.gross - it.discAmt;
        return it;
    }

    private static void finish(Sale s) {
        for (SaleItem it : s.items) {
            s.subtotal += it.gross;
            s.discount += it.discAmt;
        }
        s.total = s.subtotal - s.discount;
        s.paid = "Tunai".equals(s.method) ? ((s.total + 49999) / 50000) * 50000 : s.total;
        s.change = s.paid - s.total;
        s.pointsEarned = s.memberNo != null ? (int) (s.total / 10000) : 0;
    }

    private static void user(Data d, String name, String username, String role, String pw, boolean active) {
        User u = new User();
        u.name = name;
        u.username = username;
        u.role = role;
        u.salt = Db.newSalt();
        u.hash = Db.hash(u.salt, pw);
        u.active = active;
        d.users.add(u);
    }

    private static void prod(Data d, String code, String name, String cat, long price, String unit, int min, boolean ret) {
        Product p = new Product();
        p.code = code;
        p.name = name;
        p.category = cat;
        p.price = price;
        p.unit = unit;
        p.minStock = min;
        p.returnable = ret;
        d.products.add(p);
    }

    private static void lot(Data d, String id, String code, double qty, LocalDate exp) {
        Lot l = new Lot();
        l.id = id;
        l.productCode = code;
        l.qty = qty;
        l.expiry = exp;
        l.received = LocalDate.now().minusDays(5);
        l.ref = "Stok awal";
        d.lots.add(l);
    }

    private static void member(Data d, String no, String name, int pts, LocalDate since) {
        Member m = new Member();
        m.no = no;
        m.name = name;
        m.points = pts;
        m.since = since;
        d.members.add(m);
    }

    private static void pt(Data d, String no, LocalDate date, String desc, int pts) {
        PointTx p = new PointTx();
        p.memberNo = no;
        p.date = date;
        p.desc = desc;
        p.points = pts;
        d.pts.add(p);
    }

    private static void log(Data d, LocalDateTime t, String type, String code, String lot, double delta, long loss,
                            String actor, String reason) {
        StockLog g = new StockLog();
        g.time = t;
        g.type = type;
        g.productCode = code;
        g.product = prod(d, code).name;
        g.lotId = lot;
        g.delta = delta;
        g.loss = loss;
        g.actor = actor;
        g.reason = reason;
        d.logs.add(g);
    }
}
