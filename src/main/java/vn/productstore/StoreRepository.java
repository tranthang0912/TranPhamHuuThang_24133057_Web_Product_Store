package vn.productstore;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StoreRepository {
  final JdbcTemplate db;
  private static final String PRODUCT_SELECT =
      "SELECT p.*,c.name category_name,COALESCE((SELECT SUM(oi.quantity) FROM OrderItems oi JOIN"
          + " Orders o ON o.id=oi.order_id WHERE oi.product_id=p.id AND o.status <> 'CANCELLED'),0)"
          + " sold FROM Products p JOIN Categories c ON c.id=p.category_id ";

  public StoreRepository(JdbcTemplate db) {
    this.db = db;
  }

  Models.Product productRow(ResultSet r, int n) throws SQLException {
    return new Models.Product(
        r.getLong("id"),
        r.getString("sku"),
        r.getLong("category_id"),
        r.getString("category_name"),
        r.getString("name"),
        r.getString("brand"),
        r.getString("description"),
        r.getBigDecimal("price"),
        r.getBigDecimal("original_price"),
        r.getInt("stock"),
        r.getString("image_url"),
        r.getBoolean("active"),
        r.getBoolean("featured"),
        r.getLong("sold"),
        Base64.getEncoder().encodeToString(r.getBytes("version")),
        r.getString("source_url"),
        r.getString("source_retailer"),
        r.getDate("price_checked_at") == null ? null : r.getDate("price_checked_at").toLocalDate());
  }

  public Models.Product categoryHighlight(String slug) {
    return this.db
        .query(
            "SELECT p.*,c.name category_name,COALESCE((SELECT SUM(oi.quantity) FROM OrderItems oi"
                + " JOIN Orders o ON o.id=oi.order_id WHERE oi.product_id=p.id AND o.status <>"
                + " 'CANCELLED'),0) sold FROM Products p JOIN Categories c ON c.id=p.category_id "
                + " WHERE p.active=1 AND c.slug=? ORDER BY p.featured DESC,p.id OFFSET 0 ROWS FETCH"
                + " NEXT 1 ROWS ONLY",
            this::productRow,
            new Object[] {slug})
        .stream()
        .findFirst()
        .orElse(null);
  }

  Models.Customer customerRow(ResultSet r, int n) throws SQLException {
    return new Models.Customer(
        r.getLong("id"),
        r.getString("full_name"),
        r.getString("email"),
        r.getString("phone"),
        r.getString("address"),
        r.getString("role"),
        r.getTimestamp("created_at").toLocalDateTime());
  }

  Models.Order orderRow(ResultSet r, int n) throws SQLException {
    return new Models.Order(
        r.getLong("id"),
        r.getLong("user_id"),
        r.getString("recipient"),
        r.getString("phone"),
        r.getString("address"),
        r.getString("note"),
        r.getString("status"),
        r.getBigDecimal("subtotal"),
        r.getBigDecimal("shipping_fee"),
        r.getBigDecimal("total"),
        r.getTimestamp("created_at").toLocalDateTime());
  }

  public List<Models.Category> categories() {
    return this.db.query(
        "SELECT c.*,COUNT(p.id) product_count,COALESCE(SUM(p.stock),0) stock FROM Categories c LEFT"
            + " JOIN Products p ON p.category_id=c.id AND p.active=1 GROUP BY"
            + " c.id,c.name,c.slug,c.icon ORDER BY c.id",
        (r, n) ->
            new Models.Category(
                r.getLong("id"),
                r.getString("name"),
                r.getString("slug"),
                r.getString("icon"),
                r.getInt("product_count"),
                r.getInt("stock")));
  }

  public Models.Catalog catalog(
      String q,
      Long category,
      String sort,
      int page,
      boolean includeHidden,
      boolean deals,
      boolean inStock,
      BigDecimal maxPrice) {
    StringBuilder where = new StringBuilder(" WHERE 1=1 ");
    ArrayList<Object> args = new ArrayList<Object>();
    if (!includeHidden) {
      where.append(" AND p.active=1 ");
    }
    if (q != null && !q.isBlank()) {
      where.append(
          " AND (p.name LIKE ? ESCAPE '\\' OR p.brand LIKE ? ESCAPE '\\' OR p.sku LIKE ? ESCAPE"
              + " '\\') ");
      String pattern =
          "%"
              + q.trim()
                  .replace("\\", "\\\\")
                  .replace("%", "\\%")
                  .replace("_", "\\_")
                  .replace("[", "\\[")
              + "%";
      args.add(pattern);
      args.add(pattern);
      args.add(pattern);
    }
    if (category != null) {
      where.append(" AND p.category_id=? ");
      args.add(category);
    }
    if (deals) {
      where.append(" AND p.original_price>p.price ");
    }
    if (inStock) {
      where.append(" AND p.stock>0 ");
    }
    if (maxPrice != null) {
      where.append(" AND p.price<=? ");
      args.add(maxPrice);
    }
    int total =
        (Integer)
            this.db.queryForObject(
                "SELECT COUNT(*) FROM Products p" + String.valueOf(where),
                Integer.class,
                args.toArray());
    int pages = Math.max(1, (total + 11) / 12);
    page = Math.max(1, Math.min(page, pages));
    String order =
        switch (sort == null ? "" : sort) {
          case "price-asc" -> "p.price ASC,p.id";
          case "price-desc" -> "p.price DESC,p.id";
          case "newest" -> "p.id DESC";
          case "popular" -> "sold DESC,p.id";
          default -> "p.featured DESC,p.id";
        };
    args.add((page - 1) * 12);
    List products =
        this.db.query(
            PRODUCT_SELECT
                + String.valueOf(where)
                + " ORDER BY "
                + order
                + " OFFSET ? ROWS FETCH NEXT 12 ROWS ONLY",
            this::productRow,
            args.toArray());
    return new Models.Catalog(products, total, page, pages);
  }

  public Models.Product product(long id) {
    return (Models.Product)
        this.db
            .query(
                "SELECT p.*,c.name category_name,COALESCE((SELECT SUM(oi.quantity) FROM OrderItems"
                    + " oi JOIN Orders o ON o.id=oi.order_id WHERE oi.product_id=p.id AND o.status"
                    + " <> 'CANCELLED'),0) sold FROM Products p JOIN Categories c ON"
                    + " c.id=p.category_id  WHERE p.id=?",
                this::productRow,
                new Object[] {id})
            .stream()
            .findFirst()
            .orElseThrow(
                () -> new StoreException("Không tìm thấy sản phẩm."));
  }

  public Models.Customer customer(String email) {
    return (Models.Customer)
        this.db
            .query("SELECT * FROM Users WHERE email=?", this::customerRow, new Object[] {email})
            .stream()
            .findFirst()
            .orElseThrow(
                () -> new StoreException("Vui lòng đăng nhập lại."));
  }

  public List<Models.Customer> customers() {
    return this.db.query("SELECT * FROM Users ORDER BY created_at DESC", this::customerRow);
  }

  public Set<Long> wishlistIds(long userId) {
    return new HashSet<Long>(
        this.db.queryForList(
            "SELECT product_id FROM Wishlists WHERE user_id=?", Long.class, new Object[] {userId}));
  }

  public List<Models.Product> wishlist(long userId) {
    return this.db.query(
        "SELECT p.*,c.name category_name,COALESCE((SELECT SUM(oi.quantity) FROM OrderItems oi JOIN"
            + " Orders o ON o.id=oi.order_id WHERE oi.product_id=p.id AND o.status <>"
            + " 'CANCELLED'),0) sold FROM Products p JOIN Categories c ON c.id=p.category_id  WHERE"
            + " p.active=1 AND p.id IN (SELECT product_id FROM Wishlists WHERE user_id=?) ORDER BY"
            + " p.id DESC",
        this::productRow,
        new Object[] {userId});
  }

  public Models.Cart cart(long userId) {
    List<Map<String, Object>> quantities =
        this.db.queryForList(
            "SELECT product_id,quantity FROM CartItems WHERE user_id=? ORDER BY product_id",
            new Object[] {userId});
    ArrayList<Models.CartItem> items = new ArrayList<Models.CartItem>();
    for (Map<String, Object> item : quantities) {
      items.add(
          new Models.CartItem(
              this.product(((Number) item.get("product_id")).longValue()),
              ((Number) item.get("quantity")).intValue()));
    }
    BigDecimal subtotal =
        items.stream().map(Models.CartItem::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal shipping =
        items.isEmpty() || subtotal.compareTo(BigDecimal.valueOf(500000L)) >= 0
            ? BigDecimal.ZERO
            : BigDecimal.valueOf(30000L);
    return new Models.Cart(
        items,
        subtotal,
        shipping,
        subtotal.add(shipping),
        items.stream().mapToInt(Models.CartItem::quantity).sum());
  }

  public int cartCount(long userId) {
    return (Integer)
        this.db.queryForObject(
            "SELECT COALESCE(SUM(quantity),0) FROM CartItems WHERE user_id=?",
            Integer.class,
            new Object[] {userId});
  }

  public List<Models.Order> orders(Long userId, String status) {
    Object sql = "SELECT * FROM Orders WHERE 1=1";
    ArrayList<Object> args = new ArrayList<Object>();
    if (userId != null) {
      sql = (String) sql + " AND user_id=?";
      args.add(userId);
    }
    if (status != null && !status.isBlank()) {
      sql = (String) sql + " AND status=?";
      args.add(status);
    }
    return this.db.query(
        (String) sql + " ORDER BY created_at DESC", this::orderRow, args.toArray());
  }

  public Models.Order order(long id) {
    return (Models.Order)
        this.db.query("SELECT * FROM Orders WHERE id=?", this::orderRow, new Object[] {id}).stream()
            .findFirst()
            .orElseThrow(
                () -> new StoreException("Không tìm thấy đơn hàng."));
  }

  public List<Models.OrderItem> orderItems(long id) {
    return this.db.query(
        "SELECT * FROM OrderItems WHERE order_id=? ORDER BY id",
        (r, n) ->
            new Models.OrderItem(
                r.getLong("product_id"),
                r.getString("product_name"),
                r.getString("image_url"),
                r.getBigDecimal("unit_price"),
                r.getInt("quantity")),
        new Object[] {id});
  }

  public Map<String, Object> stats() {
    HashMap<String, Object> s = new HashMap<String, Object>();
    s.put("database", this.db.queryForObject("SELECT DB_NAME()", String.class));
    s.put("orders", this.db.queryForObject("SELECT COUNT(*) FROM Orders", Integer.class));
    s.put(
        "pending",
        this.db.queryForObject(
            "SELECT COUNT(*) FROM Orders WHERE status='PENDING'", Integer.class));
    s.put(
        "products",
        this.db.queryForObject("SELECT COUNT(*) FROM Products WHERE active=1", Integer.class));
    s.put(
        "stock",
        this.db.queryForObject(
            "SELECT COALESCE(SUM(stock),0) FROM Products WHERE active=1", Integer.class));
    s.put(
        "customers",
        this.db.queryForObject("SELECT COUNT(*) FROM Users WHERE role='CUSTOMER'", Integer.class));
    s.put(
        "sold",
        this.db.queryForObject(
            "SELECT COALESCE(SUM(i.quantity),0) FROM OrderItems i JOIN Orders o ON o.id=i.order_id"
                + " WHERE o.status='COMPLETED'",
            Integer.class));
    s.put(
        "lowStock",
        this.db.query(
            "SELECT p.*,c.name category_name,COALESCE((SELECT SUM(oi.quantity) FROM OrderItems oi"
                + " JOIN Orders o ON o.id=oi.order_id WHERE oi.product_id=p.id AND o.status <>"
                + " 'CANCELLED'),0) sold FROM Products p JOIN Categories c ON c.id=p.category_id "
                + " WHERE p.active=1 AND p.stock<=10 ORDER BY p.stock,p.id",
            this::productRow));
    s.put(
        "bestSellers",
        this.db.query(
            "SELECT p.*,c.name category_name,COALESCE((SELECT SUM(oi.quantity) FROM OrderItems oi"
                + " JOIN Orders o ON o.id=oi.order_id WHERE oi.product_id=p.id AND o.status <>"
                + " 'CANCELLED'),0) sold FROM Products p JOIN Categories c ON c.id=p.category_id "
                + " WHERE p.active=1 ORDER BY sold DESC,p.id OFFSET 0 ROWS FETCH NEXT 5 ROWS ONLY",
            this::productRow));
    return s;
  }

  public Map<String, Object> quantityStatistics() {
    return this.db.queryForMap(
        "SELECT COUNT(*) AS productCount, COALESCE(SUM(CAST(stock AS BIGINT)), 0) AS totalStock,"
            + " COALESCE(SUM(CASE WHEN stock = 0 THEN 1 ELSE 0 END), 0) AS outOfStock,"
            + " COALESCE(SUM(CASE WHEN stock BETWEEN 1 AND 10 THEN 1 ELSE 0 END), 0) AS lowStock"
            + " FROM Products WHERE active = 1");
  }
}
