package vn.productstore;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {
  private final StoreRepository repo;
  private final JdbcTemplate db;
  private final PasswordEncoder encoder;

  public StoreService(StoreRepository repo, JdbcTemplate db, PasswordEncoder encoder) {
    this.repo = repo;
    this.db = db;
    this.encoder = encoder;
  }

  public void validatePassword(String password) {
    if (password == null
        || password.length() < 8
        || password.length() > 64
        || password.getBytes(StandardCharsets.UTF_8).length > 72
        || !password.matches(".*[A-Za-z].*")
        || !password.matches(".*[0-9].*")) {
      throw new StoreException(
          "Mật khẩu cần 8–64 ký tự, gồm chữ và"
              + " số, tối đa 72 byte UTF-8.");
    }
  }

  public void register(Forms.Registration form) {
    this.validatePassword(form.password);
    if (!form.password.equals(form.confirmPassword)) {
      throw new StoreException("Mật khẩu nhập lại chưa khớp.");
    }
    try {
      this.db.update(
          "INSERT INTO Users(full_name,email,password_hash,role) VALUES(?,?,?,'CUSTOMER')",
          new Object[] {
            form.fullName.strip(),
            form.email.strip().toLowerCase(Locale.ROOT),
            this.encoder.encode((CharSequence) form.password)
          });
    } catch (DuplicateKeyException e) {
      throw new StoreException(
          "Email đã được sử dụng. Vui lòng đăng"
              + " nhập hoặc dùng email khác.");
    }
  }

  private void lockCustomer(long userId) {
    this.db.queryForObject(
        "SELECT id FROM Users WITH(UPDLOCK,ROWLOCK) WHERE id=?", Long.class, new Object[] {userId});
  }

  @Transactional
  public void updateCart(long userId, long productId, int quantity, boolean add) {
    this.lockCustomer(userId);
    Models.Product p = this.repo.product(productId);
    if (quantity < 0 || quantity > 99 || add && quantity == 0) {
      throw new StoreException("Số lượng phải từ 1 đến 99.");
    }
    List existing =
        this.db.queryForList(
            "SELECT quantity FROM CartItems WHERE user_id=? AND product_id=?",
            Integer.class,
            new Object[] {userId, productId});
    int next = quantity + (add && !existing.isEmpty() ? (Integer) existing.getFirst() : 0);
    if (next == 0) {
      this.db.update(
          "DELETE FROM CartItems WHERE user_id=? AND product_id=?",
          new Object[] {userId, productId});
      return;
    }
    if (!p.active() || next > p.stock() || next > 99) {
      throw new StoreException(
          "Sản phẩm không đủ hàng. Hiện còn "
              + p.stock()
              + " sản phẩm; tối đa 99 sản phẩm mỗi"
              + " đơn.");
    }
    if (existing.isEmpty()) {
      this.db.update(
          "INSERT INTO CartItems(user_id,product_id,quantity) VALUES(?,?,?)",
          new Object[] {userId, productId, next});
    } else {
      this.db.update(
          "UPDATE CartItems SET quantity=? WHERE user_id=? AND product_id=?",
          new Object[] {next, userId, productId});
    }
  }

  @Transactional
  public void toggleWishlist(long userId, long productId) {
    this.lockCustomer(userId);
    this.repo.product(productId);
    int removed =
        this.db.update(
            "DELETE FROM Wishlists WHERE user_id=? AND product_id=?",
            new Object[] {userId, productId});
    if (removed == 0) {
      this.db.update(
          "INSERT INTO Wishlists(user_id,product_id) VALUES(?,?)",
          new Object[] {userId, productId});
    }
  }

  @Transactional
  public long checkout(long userId, Forms.Checkout form) {
    this.lockCustomer(userId);
    List previous =
        this.db.queryForList(
            "SELECT id FROM Orders WHERE user_id=? AND checkout_token=?",
            Long.class,
            new Object[] {userId, form.token});
    if (!previous.isEmpty()) {
      return (Long) previous.getFirst();
    }
    Models.Cart cart = this.repo.cart(userId);
    if (cart.items().isEmpty()) {
      throw new StoreException("Giỏ hàng đang trống.");
    }
    for (Models.CartItem item : cart.items()) {
      int changed =
          this.db.update(
              "UPDATE Products WITH(ROWLOCK) SET stock=stock-? WHERE id=? AND stock>=? AND active=1"
                  + " AND version=?",
              new Object[] {
                item.quantity(),
                item.product().id(),
                item.quantity(),
                Base64.getDecoder().decode(item.product().version())
              });
      if (changed == 1) continue;
      throw new StoreException(
          "Sản phẩm “"
              + item.product().name()
              + "” vừa thay đổi giá hoặc số lượng. Vui"
              + " lòng kiểm tra lại giỏ hàng.");
    }
    Long id =
        (Long)
            this.db.queryForObject(
                "INSERT INTO"
                    + " Orders(user_id,recipient,phone,address,note,subtotal,shipping_fee,total,checkout_token)"
                    + " OUTPUT INSERTED.id VALUES(?,?,?,?,?,?,?,?,?)",
                Long.class,
                new Object[] {
                  userId,
                  form.recipient.strip(),
                  form.phone,
                  form.address.strip(),
                  form.note,
                  cart.subtotal(),
                  cart.shipping(),
                  cart.total(),
                  form.token
                });
    for (Models.CartItem item : cart.items()) {
      this.db.update(
          "INSERT INTO OrderItems(order_id,product_id,product_name,image_url,unit_price,quantity)"
              + " VALUES(?,?,?,?,?,?)",
          new Object[] {
            id,
            item.product().id(),
            item.product().name(),
            item.product().imageUrl(),
            item.product().price(),
            item.quantity()
          });
    }
    this.db.update("DELETE FROM CartItems WHERE user_id=?", new Object[] {userId});
    this.db.update(
        "UPDATE Users SET phone=?,address=? WHERE id=?",
        new Object[] {form.phone, form.address.strip(), userId});
    return id;
  }

  @Transactional
  public void changeOrderStatus(long orderId, long actorId, boolean admin, String next) {
    Models.Order order =
        (Models.Order)
            this.db
                .query(
                    "SELECT * FROM Orders WITH(UPDLOCK,ROWLOCK) WHERE id=?",
                    this.repo::orderRow,
                    new Object[] {orderId})
                .stream()
                .findFirst()
                .orElseThrow(
                    () ->
                        new StoreException(
                            "Không tìm thấy đơn hàng."));
    if (!admin && order.userId() != actorId) {
      throw new StoreException(
          "Bạn không có quyền với đơn hàng này.");
    }
    if (!(admin || order.status().equals("PENDING") && next.equals("CANCELLED"))) {
      throw new StoreException(
          "Chỉ có thể hủy đơn đang chờ xác"
              + " nhận.");
    }
    List<String> allowed =
        switch (order.status()) {
          case "PENDING" -> List.of("CONFIRMED", "CANCELLED");
          case "CONFIRMED" -> List.of("SHIPPING", "CANCELLED");
          case "SHIPPING" -> List.of("COMPLETED");
          default -> List.of();
        };
    if (!allowed.contains(next)) {
      throw new StoreException(
          "Không thể chuyển đơn từ trạng thái hiện"
              + " tại sang trạng thái đã chọn.");
    }
    if (next.equals("CANCELLED")) {
      for (Models.OrderItem item :
          this.repo.orderItems(orderId).stream()
              .sorted(Comparator.comparingLong(Models.OrderItem::productId))
              .toList()) {
        this.db.update(
            "UPDATE Products SET stock=stock+? WHERE id=?",
            new Object[] {item.quantity(), item.productId()});
      }
    }
    this.db.update("UPDATE Orders SET status=? WHERE id=?", new Object[] {next, orderId});
  }

  @Transactional
  public void deleteProduct(long id) {
    this.repo.product(id);
    this.db.update("DELETE FROM CartItems WHERE product_id=?", new Object[] {id});
    this.db.update("DELETE FROM Wishlists WHERE product_id=?", new Object[] {id});
    if ((Integer)
            this.db.queryForObject(
                "SELECT COUNT(*) FROM OrderItems WHERE product_id=?",
                Integer.class,
                new Object[] {id})
        > 0) {
      this.db.update("UPDATE Products SET active=0 WHERE id=?", new Object[] {id});
    } else {
      this.db.update("DELETE FROM Products WHERE id=?", new Object[] {id});
    }
  }

  public void saveProduct(Forms.ProductForm f) {
    f.setSku(f.sku);
    if (!f.sku.matches("[A-Z0-9_-]{1,32}"))
      throw new StoreException("Mã sản phẩm chỉ gồm 1–32 ký tự chữ, số, dấu - hoặc _.");
    if (f.originalPrice.signum() != 0 && f.originalPrice.compareTo(f.price) < 0) {
      throw new StoreException(
          "Giá gốc phải bằng 0 hoặc không thấp hơn giá"
              + " bán.");
    }
    if ((Integer)
            this.db.queryForObject(
                "SELECT COUNT(*) FROM Categories WHERE id=?",
                Integer.class,
                new Object[] {f.categoryId})
        == 0) {
      throw new StoreException("Danh mục không tồn tại.");
    }
    try {
      if (f.id == null) {
        this.db.update(
            "INSERT INTO"
                + " Products(sku,category_id,name,brand,description,price,original_price,stock,image_url,active,featured,source_url,source_retailer,price_checked_at)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            new Object[] {
              f.sku,
              f.categoryId,
              f.name.strip(),
              f.brand.strip(),
              f.description.strip(),
              f.price,
              f.originalPrice,
              f.stock,
              f.imageUrl,
              f.active,
              f.featured,
              f.sourceUrl,
              f.sourceRetailer,
              f.priceCheckedAt
            });
      } else {
        byte[] version;
        try {
          version = Base64.getDecoder().decode(f.version);
        } catch (IllegalArgumentException e) {
          throw new StoreException(
              "Dữ liệu phiên bản không hợp lệ. Vui lòng"
                  + " tải lại trang.");
        }
        if (this.db.update(
                "UPDATE Products SET"
                    + " sku=?,category_id=?,name=?,brand=?,description=?,price=?,original_price=?,stock=?,image_url=?,active=?,featured=?,source_url=?,source_retailer=?,price_checked_at=?"
                    + " WHERE id=? AND version=?",
                new Object[] {
                  f.sku,
                  f.categoryId,
                  f.name.strip(),
                  f.brand.strip(),
                  f.description.strip(),
                  f.price,
                  f.originalPrice,
                  f.stock,
                  f.imageUrl,
                  f.active,
                  f.featured,
                  f.sourceUrl,
                  f.sourceRetailer,
                  f.priceCheckedAt,
                  f.id,
                  version
                })
            == 0) {
          throw new StoreException(
              "Sản phẩm vừa có thay đổi, có thể do"
                  + " khách đặt hàng. Tải lại trang trước"
                  + " khi sửa.");
        }
      }
    } catch (org.springframework.dao.DuplicateKeyException e) {
      throw new StoreException("Mã sản phẩm đã tồn tại. Vui lòng nhập mã khác.");
    }
  }
}
