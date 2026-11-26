package smartmart;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Penyimpanan data + seluruh aturan bisnis SmartMart POS. */
final class Db {
    static final String[] RANK = {"AMAN", "PERHATIAN", "PRIORITAS", "SEGERA", "KEDALUWARSA"};
    static final Locale ID = Locale.forLanguageTag("id-ID");
    static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd MMM yyyy", ID);
    static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm", ID);
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", ID);

    private static Db inst;

    static synchronized Db get() {
        if (inst == null) inst = new Db();
        return inst;
    }

    Data d;
    User current;
    private final File file;

    private Db() {
        File dir = new File(System.getProperty("smartmart.dir", "data"));
        dir.mkdirs();
        file = new File(dir, "smartmart.dat");
        d = load();
        if (d == null) {
            d = Seed.create();
            save();
        }
    }

    private Data load() {
        if (!file.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            return (Data) in.readObject();
        } catch (Exception e) {
            file.renameTo(new File(file.getParentFile(), "smartmart.dat.bak"));
            return null;
        }
    }

    synchronized void save() {
        File tmp = new File(file.getParentFile(), "smartmart.tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tmp)))) {
            out.writeObject(d);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void resetDemo() {
        d = Seed.create();
        save();
    }

    // ---------------------------------------------------------------- akun
    static String newSalt() {
        byte[] b = new byte[8];
        new SecureRandom().nextBytes(b);
        return hex(b);
    }

    static String hash(String salt, String pw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest((salt + pw).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    User user(String username) {
        for (User u : d.users) if (u.username.equalsIgnoreCase(username)) return u;
        return null;
    }

    User login(String username, String pw) {
        User u = user(username == null ? "" : username.trim());
        if (u == null || !u.active || !u.hash.equals(hash(u.salt, pw))) {
            log("Login gagal untuk username '" + username + "'", "-");
            return null;
        }
        current = u;
        log("Login berhasil", u.name);
        return u;
    }

    /** Memeriksa password admin mana pun yang aktif (untuk persetujuan retur/void). */
    User verifyAdmin(String pw) {
        for (User u : d.users) if (u.isAdmin() && u.active && u.hash.equals(hash(u.salt, pw))) return u;
        log("Percobaan password admin salah (persetujuan)", actor());
        return null;
    }

    String actor() { return current == null ? "-" : current.name; }

    void log(String text) { log(text, actor()); }

    void log(String text, String actor) {
        ActLog a = new ActLog();
        a.time = LocalDateTime.now();
        a.actor = actor;
        a.text = text;
        d.acts.add(a);
        save();
    }

    User addUser(String name, String username, String role, String pw) {
        if (user(username) != null) throw new IllegalArgumentException("Username sudah dipakai.");
        User u = new User();
        u.name = name;
        u.username = username;
        u.role = role;
        u.salt = newSalt();
        u.hash = hash(u.salt, pw);
        d.users.add(u);
        log("Menambah pegawai " + username + " (" + role + ")");
        return u;
    }

    void setPassword(User u, String pw) {
        u.salt = newSalt();
        u.hash = hash(u.salt, pw);
        log("Mengubah password " + u.username);
    }

    // ------------------------------------------------------- produk & lot
    static double r3(double x) { return Math.round(x * 1000.0) / 1000.0; }

    Product product(String code) {
        for (Product p : d.products) if (p.code.equals(code)) return p;
        return null;
    }

    List<Lot> lotsOf(String code) {
        List<Lot> out = new ArrayList<>();
        for (Lot l : d.lots) if (l.productCode.equals(code) && l.qty > 1e-9) out.add(l);
        out.sort(Comparator.comparing((Lot l) -> l.expiry));
        return out;
    }

    Lot lot(String id) {
        for (Lot l : d.lots) if (l.id.equals(id)) return l;
        return null;
    }

    /** Lot asal item penjualan; bila lot sudah tidak ada, pakai lot terbaru produk tersebut. */
    Lot lotFor(SaleItem it) {
        Lot l = lot(it.lotId);
        if (l != null) return l;
        Lot best = null;
        for (Lot x : d.lots) if (x.productCode.equals(it.productCode) && (best == null || x.expiry.isAfter(best.expiry))) best = x;
        return best;
    }

    double stock(String code) {
        double s = 0;
        for (Lot l : lotsOf(code)) s += l.qty;
        return r3(s);
    }

    double sellable(String code) {
        double s = 0;
        for (Lot l : lotsOf(code)) if (days(l) > 0) s += l.qty;
        return r3(s);
    }

    static long days(Lot l) { return ChronoUnit.DAYS.between(LocalDate.now(), l.expiry); }

    String lotStatus(long days) {
        if (days <= 0) return "KEDALUWARSA";
        if (days == 1) return "SEGERA";
        if (days <= 3) return "PRIORITAS";
        if (days <= d.s.warnDays) return "PERHATIAN";
        return "AMAN";
    }

    static int rank(String status) {
        for (int i = 0; i < RANK.length; i++) if (RANK[i].equals(status)) return i;
        return 0;
    }

    String productStatus(Product p) {
        int worst = 0;
        for (Lot l : lotsOf(p.code)) worst = Math.max(worst, rank(lotStatus(days(l))));
        if (worst >= 2) return RANK[worst];
        double st = stock(p.code);
        if (st <= 0) return "HABIS";
        if (st < p.minStock) return "MENIPIS";
        return RANK[worst];
    }

    /** Persentase FreshSave untuk sisa masa simpan tertentu (0 bila tidak ada / diblokir). */
    int fsPct(long days) {
        if (days <= 0) return 0;
        int best = 0, bestDays = Integer.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            if (days <= d.s.fsDays[i] && d.s.fsDays[i] < bestDays) {
                best = d.s.fsPct[i];
                bestDays = d.s.fsDays[i];
            }
        }
        return best;
    }

    /** Diskon tidak ditumpuk: dipakai yang terbesar; sama besar => FRESHSAVE. */
    String[] pickDiscount(int fs, int mem) {
        if (fs <= 0 && mem <= 0) return new String[]{"", "0"};
        if (fs >= mem) return new String[]{"FRESHSAVE", String.valueOf(fs)};
        return new String[]{"MEMBER", String.valueOf(mem)};
    }

    // ------------------------------------------------------------ harga
    static class QLine {
        String code, name, unit, tag = "";
        double qty;
        long gross, net;
    }

    static class Quote {
        List<SaleItem> items = new ArrayList<>();
        List<QLine> lines = new ArrayList<>();
        long subtotal, memberDisc, fsDisc, discount, after;
        String error;
    }

    /** Alokasi lot terdekat kedaluwarsa (FEFO); null bila stok tidak cukup. */
    List<SaleItem> allocate(Product p, double qty, Member m) {
        double need = r3(qty);
        int mem = m != null ? d.s.memberDisc : 0;
        List<SaleItem> out = new ArrayList<>();
        for (Lot l : lotsOf(p.code)) {
            long dl = days(l);
            if (dl <= 0) continue;
            if (need <= 1e-9) break;
            double take = Math.min(need, l.qty);
            String[] pick = pickDiscount(fsPct(dl), mem);
            SaleItem it = new SaleItem();
            it.productCode = p.code;
            it.name = p.name;
            it.lotId = l.id;
            it.unit = p.unit;
            it.qty = r3(take);
            it.unitPrice = p.price;
            it.gross = Math.round(it.qty * p.price);
            it.source = pick[0];
            it.discPct = Integer.parseInt(pick[1]);
            it.discAmt = Math.round(it.gross * it.discPct / 100.0);
            it.net = it.gross - it.discAmt;
            out.add(it);
            need = r3(need - take);
        }
        return need > 1e-6 ? null : out;
    }

    Quote quote(Map<String, Double> cart, Member m) {
        Quote q = new Quote();
        for (Map.Entry<String, Double> e : cart.entrySet()) {
            Product p = product(e.getKey());
            if (p == null) continue;
            List<SaleItem> its = allocate(p, e.getValue(), m);
            if (its == null) {
                q.error = "Stok tidak cukup: " + p.name;
                continue;
            }
            QLine ql = new QLine();
            ql.code = p.code;
            ql.name = p.name;
            ql.unit = p.unit;
            for (SaleItem it : its) {
                q.items.add(it);
                ql.qty += it.qty;
                ql.gross += it.gross;
                ql.net += it.net;
                if (ql.tag.isEmpty() && it.discPct > 0)
                    ql.tag = ("FRESHSAVE".equals(it.source) ? "FreshSave" : "Member") + " -" + it.discPct + "%";
            }
            ql.qty = r3(ql.qty);
            q.lines.add(ql);
        }
        for (SaleItem it : q.items) {
            q.subtotal += it.gross;
            if ("MEMBER".equals(it.source)) q.memberDisc += it.discAmt;
            else if ("FRESHSAVE".equals(it.source)) q.fsDisc += it.discAmt;
        }
        q.discount = q.memberDisc + q.fsDisc;
        q.after = q.subtotal - q.discount;
        return q;
    }

    /** Poin maksimum yang boleh ditukar pada transaksi dengan total setelah diskon 'after'. */
    int maxRedeemPoints(Member m, long after) {
        if (m == null) return 0;
        long maxDisc = after * d.s.maxPointPct / 100;
        long units = Math.min(m.points / d.s.redeemUnit, maxDisc / d.s.redeemValue);
        return (int) (Math.max(units, 0) * d.s.redeemUnit);
    }

    // -------------------------------------------------------- transaksi
    String nextSaleId(LocalDate day) {
        String pre = "TRX-" + day.format(DateTimeFormatter.BASIC_ISO_DATE);
        int max = 0;
        for (Sale s : d.sales) {
            if (s.id.startsWith(pre)) {
                try { max = Math.max(max, Integer.parseInt(s.id.substring(pre.length() + 1))); }
                catch (NumberFormatException ignored) { }
            }
        }
        return pre + "-" + String.format("%04d", max + 1);
    }

    Sale sale(String id) {
        if (id == null) return null;
        for (Sale s : d.sales) if (s.id.equalsIgnoreCase(id.trim())) return s;
        return null;
    }

    Sale checkout(Map<String, Double> cart, Member m, int pointsUsed, String method, long paid, User cashier) {
        if (cart.isEmpty()) throw new IllegalArgumentException("Keranjang kosong.");
        Quote q = quote(cart, m);
        if (q.error != null) throw new IllegalArgumentException(q.error);
        if (pointsUsed < 0 || (pointsUsed > 0 && (m == null || pointsUsed % d.s.redeemUnit != 0
                || pointsUsed > maxRedeemPoints(m, q.after))))
            throw new IllegalArgumentException("Penukaran poin tidak valid.");
        long pointsDisc = (long) (pointsUsed / d.s.redeemUnit) * d.s.redeemValue;
        long total = q.after - pointsDisc;
        if ("Tunai".equals(method) && paid < total) throw new IllegalArgumentException("Uang diterima kurang.");
        Sale s = new Sale();
        s.time = LocalDateTime.now();
        s.id = nextSaleId(s.time.toLocalDate());
        s.cashier = cashier.name;
        if (m != null) {
            s.memberNo = m.no;
            s.memberName = m.name;
        }
        s.items = q.items;
        s.subtotal = q.subtotal;
        s.discount = q.discount;
        s.pointsUsed = pointsUsed;
        s.pointsDiscount = pointsDisc;
        s.total = total;
        s.method = method;
        s.paid = "Tunai".equals(method) ? paid : total;
        s.change = s.paid - total;
        s.pointsEarned = m != null ? (int) (total / d.s.pointPerRp) : 0;
        for (SaleItem it : s.items) {
            Lot l = lot(it.lotId);
            l.qty = r3(l.qty - it.qty);
            stockLog("TERJUAL", it.productCode, it.name, it.lotId, -it.qty, 0, cashier.name, s.id);
        }
        if (m != null) {
            m.points = m.points - pointsUsed + s.pointsEarned;
            if (pointsUsed > 0) pointTx(m, "Tukar poin " + s.id, -pointsUsed);
            if (s.pointsEarned > 0) pointTx(m, "Belanja " + s.id, s.pointsEarned);
        }
        d.sales.add(s);
        save();
        return s;
    }

    void stockLog(String type, String code, String name, String lotId, double delta, long loss, String actor, String reason) {
        StockLog g = new StockLog();
        g.time = LocalDateTime.now();
        g.type = type;
        g.productCode = code;
        g.product = name;
        g.lotId = lotId;
        g.delta = r3(delta);
        g.loss = loss;
        g.actor = actor;
        g.reason = reason;
        d.logs.add(g);
    }

    void pointTx(Member m, String desc, int pts) {
        PointTx p = new PointTx();
        p.memberNo = m.no;
        p.date = LocalDate.now();
        p.desc = desc;
        p.points = pts;
        d.pts.add(p);
    }

    // ----------------------------------------------------------- retur
    String saleReturnBlock(Sale s) {
        if (s == null) return "Masukkan ID transaksi asli untuk melanjutkan.";
        if ("DIBATALKAN".equals(s.status)) return "Transaksi sudah dibatalkan (void), tidak dapat diretur.";
        long age = ChronoUnit.DAYS.between(s.time.toLocalDate(), LocalDate.now());
        if (age > d.s.returnDays)
            return "Lewat " + d.s.returnDays + " hari. Pembelian " + s.time.format(DF) + ", batas retur sudah lewat.";
        return null;
    }

    String itemReturnBlock(SaleItem it) {
        Product p = product(it.productCode);
        if ("FRESHSAVE".equals(it.source)) return "Dibeli dengan diskon FreshSave";
        if (p != null && !p.returnable) return "Barang tidak dapat diretur";
        if (it.returned >= it.qty - 1e-9) return "Sudah diretur seluruhnya";
        return null;
    }

    static long itemRefund(SaleItem it, double qty) { return Math.round(it.net * qty / it.qty); }

    /** @return {refund, poin dikurangi} */
    long[] processReturn(Sale s, Map<Integer, Double> qtys, String reason, String condition, boolean restock,
                         User cashier, User approver) {
        String blk = saleReturnBlock(s);
        if (blk != null) throw new IllegalArgumentException(blk);
        long refund = 0;
        for (Map.Entry<Integer, Double> e : qtys.entrySet()) {
            SaleItem it = s.items.get(e.getKey());
            double q = r3(e.getValue());
            if (q <= 0) continue;
            String ib = itemReturnBlock(it);
            if (ib != null) throw new IllegalArgumentException(it.name + ": " + ib);
            double left = r3(it.qty - it.returned);
            if (q > left + 1e-9)
                throw new IllegalArgumentException("Jumlah melebihi pembelian. Dibeli " + Ui.qty(it.qty) + ", sudah diretur "
                        + Ui.qty(it.returned) + ", sisa maks. " + Ui.qty(left) + " (" + it.name + ").");
        }
        Member m = s.memberNo == null ? null : member(s.memberNo);
        for (Map.Entry<Integer, Double> e : qtys.entrySet()) {
            SaleItem it = s.items.get(e.getKey());
            double q = r3(e.getValue());
            if (q <= 0) continue;
            long rf = itemRefund(it, q);
            refund += rf;
            it.returned = r3(it.returned + q);
            ReturnRec r = new ReturnRec();
            r.id = "RET-" + String.format("%04d", ++d.retSeq);
            r.saleId = s.id;
            r.product = it.name;
            r.qty = q;
            r.refund = rf;
            r.reason = reason;
            r.condition = condition;
            r.cashier = cashier.name;
            r.approver = approver.name;
            r.time = LocalDateTime.now();
            d.returns.add(r);
            Lot l = lotFor(it);
            if (restock && l != null) {
                l.qty = r3(l.qty + q);
                stockLog("DIRETUR", it.productCode, it.name, l.id, q, 0, cashier.name, "Retur disetujui " + approver.name + " (" + s.id + ")");
            } else {
                stockLog("RUSAK", it.productCode, it.name, it.lotId, 0, Math.round(q * it.unitPrice), cashier.name,
                        "Retur tidak layak jual (" + s.id + ")");
            }
        }
        if (refund <= 0) throw new IllegalArgumentException("Pilih minimal satu barang untuk diretur.");
        s.hasReturn = true;
        int pts = (int) (refund / d.s.pointPerRp);
        if (m != null && pts > 0) {
            int ded = Math.min(pts, m.points);
            m.points -= ded;
            pointTx(m, "Retur " + s.id, -ded);
        }
        log("Retur " + s.id + " senilai " + Ui.rp(refund) + " disetujui " + approver.name, cashier.name);
        return new long[]{refund, pts};
    }

    // ------------------------------------------------------------ void
    String voidBlock(Sale s) {
        if (s == null) return "Masukkan ID transaksi.";
        if (!"SELESAI".equals(s.status)) return "Transaksi sudah dibatalkan.";
        if (!s.time.toLocalDate().equals(LocalDate.now())) return "Void setelah dibayar hanya pada hari yang sama.";
        if (s.hasReturn) return "Transaksi yang sudah diretur tidak dapat di-void.";
        return null;
    }

    void voidSale(Sale s, String reason, User cashier, User approver) {
        String blk = voidBlock(s);
        if (blk != null) throw new IllegalArgumentException(blk);
        for (SaleItem it : s.items) {
            Lot l = lotFor(it);
            if (l != null) l.qty = r3(l.qty + it.qty);
            stockLog("PENYESUAIAN", it.productCode, it.name, l == null ? it.lotId : l.id, it.qty, 0, cashier.name,
                    "Void " + s.id + " (" + reason + ")");
        }
        Member m = s.memberNo == null ? null : member(s.memberNo);
        if (m != null) {
            m.points = Math.max(0, m.points - s.pointsEarned + s.pointsUsed);
            pointTx(m, "Void " + s.id, s.pointsUsed - s.pointsEarned);
        }
        s.status = "DIBATALKAN";
        s.voidReason = reason;
        s.voidBy = approver.name;
        log("Void " + s.id + " (" + reason + ") disetujui " + approver.name, cashier.name);
    }

    // ------------------------------------------------- penyesuaian stok
    void adjust(Lot l, String type, double qty, String reason, User by) {
        qty = r3(qty);
        if (qty <= 0) throw new IllegalArgumentException("Jumlah harus lebih dari 0.");
        if (qty > l.qty + 1e-9) throw new IllegalArgumentException("Jumlah melebihi stok lot (" + Ui.qty(l.qty) + ").");
        if (reason == null || reason.trim().isEmpty()) throw new IllegalArgumentException("Alasan wajib diisi.");
        Product p = product(l.productCode);
        long loss = "PENYESUAIAN".equals(type) ? 0 : Math.round(qty * p.price);
        l.qty = r3(l.qty - qty);
        stockLog(type, p.code, p.name, l.id, -qty, loss, by.name, reason.trim());
        log("Penyesuaian stok " + type + " " + p.name + " lot " + l.id + " -" + Ui.qty(qty), by.name);
    }

    int disposeExpired(User by) {
        int n = 0;
        for (Lot l : new ArrayList<>(d.lots)) {
            if (l.qty > 1e-9 && days(l) <= 0) {
                adjust(l, "KEDALUWARSA", l.qty, "Lot dibuang", by);
                n++;
            }
        }
        return n;
    }

    // ------------------------------------------------------ penerimaan
    Recv receive(String supplier, String po, LocalDate date, User by, List<RecvLine> lines) {
        if (lines.isEmpty()) throw new IllegalArgumentException("Tambahkan minimal satu barang.");
        Recv r = new Recv();
        r.id = "RCV-" + String.format("%04d", ++d.recvSeq);
        r.supplier = supplier;
        r.po = po;
        r.date = date;
        r.receiver = by.name;
        for (RecvLine ln : lines) {
            Product p = product(ln.productCode);
            double entered = r3(ln.received - ln.damaged);
            if (entered < 0) throw new IllegalArgumentException(p.name + ": jumlah rusak melebihi jumlah diterima.");
            if (ln.expiry == null) throw new IllegalArgumentException(p.name + ": tanggal kedaluwarsa wajib diisi.");
            if (entered > 0) {
                Lot l = new Lot();
                l.id = "L" + String.format("%04d", ++d.lotSeq);
                l.productCode = p.code;
                l.expiry = ln.expiry;
                l.received = date;
                l.qty = entered;
                l.ref = po;
                d.lots.add(l);
                ln.lotId = l.id;
                stockLog("MASUK", p.code, p.name, l.id, entered, 0, by.name, "ReceiveCheck " + po);
            }
            if (ln.damaged > 0)
                stockLog("RUSAK", p.code, p.name, ln.lotId == null ? "-" : ln.lotId, -ln.damaged,
                        Math.round(ln.damaged * p.price), by.name, "Rusak saat diterima (" + po + ")");
            double diff = ln.ordered - ln.received;
            if (diff > 0) r.discrepancyValue += Math.round(diff * p.price);
            r.lines.add(ln);
        }
        d.recvs.add(r);
        log("Penerimaan barang " + po + " dari " + supplier + " (" + r.lines.size() + " lot)", by.name);
        return r;
    }

    // ---------------------------------------------------------- member
    Member member(String no) {
        for (Member m : d.members) if (m.no.equalsIgnoreCase(no)) return m;
        return null;
    }

    List<Member> searchMembers(String q) {
        String s = q == null ? "" : q.trim().toLowerCase();
        List<Member> out = new ArrayList<>();
        for (Member m : d.members) if (s.isEmpty() || m.no.toLowerCase().contains(s) || m.name.toLowerCase().contains(s)) out.add(m);
        return out;
    }

    Member addMember(String name) {
        int max = 0;
        for (Member m : d.members) {
            try { max = Math.max(max, Integer.parseInt(m.no.substring(2))); } catch (Exception ignored) { }
        }
        Member m = new Member();
        m.no = "M-" + String.format("%04d", max + 1);
        m.name = name;
        m.since = LocalDate.now();
        d.members.add(m);
        log("Mendaftarkan member " + m.no + " " + name);
        return m;
    }

    long memberSpent(Member m) {
        long t = 0;
        for (Sale s : d.sales) if (m.no.equals(s.memberNo) && "SELESAI".equals(s.status)) t += s.total;
        return t;
    }

    // --------------------------------------------------------- laporan
    List<Sale> sales(LocalDate from, LocalDate to) {
        List<Sale> out = new ArrayList<>();
        for (Sale s : d.sales) {
            LocalDate t = s.time.toLocalDate();
            if (!t.isBefore(from) && !t.isAfter(to)) out.add(s);
        }
        return out;
    }

    long refunds(LocalDate from, LocalDate to) {
        long r = 0;
        for (ReturnRec x : d.returns) {
            LocalDate t = x.time.toLocalDate();
            if (!t.isBefore(from) && !t.isAfter(to)) r += x.refund;
        }
        return r;
    }

    long revenue(LocalDate from, LocalDate to) {
        long r = 0;
        for (Sale s : sales(from, to)) if ("SELESAI".equals(s.status)) r += s.total;
        return r - refunds(from, to);
    }

    List<StockLog> logs(LocalDate from, LocalDate to) {
        List<StockLog> out = new ArrayList<>();
        for (StockLog g : d.logs) {
            LocalDate t = g.time.toLocalDate();
            if (!t.isBefore(from) && !t.isAfter(to)) out.add(g);
        }
        out.sort(Comparator.comparing((StockLog g) -> g.time).reversed());
        return out;
    }

    long lossBy(List<StockLog> logs, String type) {
        long t = 0;
        for (StockLog g : logs) if (type == null ? g.loss > 0 : type.equals(g.type)) t += g.loss;
        return t;
    }

    long discrepancy(LocalDate from, LocalDate to) {
        long t = 0;
        for (Recv r : d.recvs) if (!r.date.isBefore(from) && !r.date.isAfter(to)) t += r.discrepancyValue;
        return t;
    }

    int criticalCount() {
        int n = 0;
        for (Product p : d.products) {
            String st = productStatus(p);
            if (!st.equals("AMAN") && !st.equals("PERHATIAN")) n++;
        }
        return n;
    }
}
