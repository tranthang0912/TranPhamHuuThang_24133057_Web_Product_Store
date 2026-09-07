package vn.productstore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Models {
  private Models() {}

  public record Catalog(List<Product> products, int total, int page, int pages) {}

  public record OrderItem(
      long productId, String productName, String imageUrl, BigDecimal unitPrice, int quantity) {
    public BigDecimal total() {
      return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
  }

  public record Order(
      long id,
      long userId,
      String recipient,
      String phone,
      String address,
      String note,
      String status,
      BigDecimal subtotal,
      BigDecimal shippingFee,
      BigDecimal total,
      LocalDateTime createdAt) {
    public String code() {
      return "PS" + String.format("%06d", this.id);
    }

    public String statusName() {
      return switch (this.status) {
        case "PENDING" -> "Chờ xác nhận";
        case "CONFIRMED" -> "Đã xác nhận";
        case "SHIPPING" -> "Đang giao hàng";
        case "COMPLETED" -> "Hoàn thành";
        default -> "Đã hủy";
      };
    }
  }

  public record Cart(
      List<CartItem> items,
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal total,
      int count) {}

  public record CartItem(Product product, int quantity) {
    public BigDecimal total() {
      return this.product.price().multiply(BigDecimal.valueOf(this.quantity));
    }
  }

  public record Customer(
      long id,
      String fullName,
      String email,
      String phone,
      String address,
      String role,
      LocalDateTime createdAt) {}

  public record Product(
      long id,
      String sku,
      long categoryId,
      String categoryName,
      String name,
      String brand,
      String description,
      BigDecimal price,
      BigDecimal originalPrice,
      int stock,
      String imageUrl,
      boolean active,
      boolean featured,
      long sold,
      String version,
      String sourceUrl,
      String sourceRetailer,
      LocalDate priceCheckedAt) {
    public int discount() {
      return this.originalPrice.compareTo(this.price) > 0
          ? this.originalPrice
              .subtract(this.price)
              .multiply(BigDecimal.valueOf(100L))
              .divide(this.originalPrice, 0, RoundingMode.DOWN)
              .intValue()
          : 0;
    }
  }

  public record Category(
      long id, String name, String slug, String icon, int productCount, int stock) {}
}
