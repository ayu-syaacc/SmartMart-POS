package smartmart;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Seluruh kelas data aplikasi (disimpan dengan serialisasi Java ke data/smartmart.dat). */

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    String name, username, role, salt, hash;
    boolean active = true;

    boolean isAdmin() { return "ADMIN".equals(role); }
}

class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    String code, name, category, unit = "PCS";
    long price;
    int minStock = 10;
    boolean returnable = true;

    boolean perKg() { return "KG".equals(unit); }
}

class Lot implements Serializable {
    private static final long serialVersionUID = 1L;
    String id, productCode, ref;
    LocalDate expiry, received;
    double qty;
}

class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    String no, name;
    int points;
    LocalDate since;
}

class SaleItem implements Serializable {
    private static final long serialVersionUID = 1L;
    String productCode, name, lotId, unit, source = "";   // source: MEMBER / FRESHSAVE / ""
    double qty, returned;
    long unitPrice, gross, discAmt, net;
    int discPct;
}

class Sale implements Serializable {
    private static final long serialVersionUID = 1L;
    String id, cashier, memberNo, memberName, method = "Tunai", status = "SELESAI", voidReason, voidBy;
    LocalDateTime time;
    List<SaleItem> items = new ArrayList<>();
    long subtotal, discount, pointsDiscount, total, paid, change;
    int pointsUsed, pointsEarned;
    boolean hasReturn;

    long discountBy(String source) {
        long s = 0;
        for (SaleItem i : items) if (source.equals(i.source)) s += i.discAmt;
        return s;
    }
}

class ReturnRec implements Serializable {
    private static final long serialVersionUID = 1L;
    String id, saleId, product, reason, condition, cashier, approver;
    LocalDateTime time;
    double qty;
    long refund;
    int pointsDeducted;
}

class StockLog implements Serializable {
    private static final long serialVersionUID = 1L;
    LocalDateTime time;
    String type, productCode, product, lotId, actor, reason;
    double delta;
    long loss;
}

class ActLog implements Serializable {
    private static final long serialVersionUID = 1L;
    LocalDateTime time;
    String actor, text;
}

class PointTx implements Serializable {
    private static final long serialVersionUID = 1L;
    String memberNo, desc;
    LocalDate date;
    int points;
}

class RecvLine implements Serializable {
    private static final long serialVersionUID = 1L;
    String productCode, productName, lotId;
    double ordered, received, damaged;
    LocalDate expiry;
}

class Recv implements Serializable {
    private static final long serialVersionUID = 1L;
    String id, supplier, po, receiver;
    LocalDate date;
    List<RecvLine> lines = new ArrayList<>();
    long discrepancyValue;
}

class Settings implements Serializable {
    private static final long serialVersionUID = 1L;
    String storeName = "SMARTMART", address = "Jl. Contoh No. 1, Pekanbaru", phone = "Telp. 0761-000000";
    int minStock = 10, warnDays = 7, memberDisc = 10, pointPerRp = 10000, redeemUnit = 100,
        redeemValue = 5000, maxPointPct = 20, returnDays = 3;
    int[] fsDays = {7, 3, 1};
    int[] fsPct = {10, 20, 30};
}

class Data implements Serializable {
    private static final long serialVersionUID = 1L;
    List<User> users = new ArrayList<>();
    List<Product> products = new ArrayList<>();
    List<Lot> lots = new ArrayList<>();
    List<Member> members = new ArrayList<>();
    List<Sale> sales = new ArrayList<>();
    List<ReturnRec> returns = new ArrayList<>();
    List<StockLog> logs = new ArrayList<>();
    List<ActLog> acts = new ArrayList<>();
    List<PointTx> pts = new ArrayList<>();
    List<Recv> recvs = new ArrayList<>();
    Settings s = new Settings();
    int lotSeq = 400, retSeq = 0, recvSeq = 14;
}
