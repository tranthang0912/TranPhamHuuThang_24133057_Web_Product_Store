package vn.productstore;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class Forms {

  public static class ProductForm {
    public Long id;
    public String version = "";

    @NotBlank(message = "Vui lòng nhập mã sản phẩm.")
    @Pattern(
        regexp = "[A-Za-z0-9_-]{1,32}",
        message = "Mã sản phẩm tối đa 32 ký tự, chỉ gồm chữ, số, dấu - hoặc _.")
    public String sku = "";

    @NotBlank
    @Size(max = 160)
    public @NotBlank @Size(max = 160) String name = "";

    @Size(max = 80)
    public @Size(max = 80) String brand = "";

    @NotNull @Positive public Long categoryId;

    @NotBlank
    @Size(max = 3000)
    public @NotBlank @Size(max = 3000) String description = "";

    @NotNull
    @DecimalMin(value = "1000")
    @DecimalMax(value = "999999999")
    public @NotNull @DecimalMin(value = "1000") @DecimalMax(value = "999999999") BigDecimal price =
        BigDecimal.valueOf(100000L);

    @NotNull
    @DecimalMin(value = "0")
    @DecimalMax(value = "999999999")
    public @NotNull @DecimalMin(value = "0") @DecimalMax(value = "999999999") BigDecimal
        originalPrice = BigDecimal.ZERO;

    @Min(value = 0L)
    @Max(value = 1000000L)
    public @Min(value = 0L) @Max(value = 1000000L) int stock;

    @NotBlank
    @Size(max = 500)
    @Pattern(
        regexp = "(/images/[A-Za-z0-9._/-]+|https://[^\\s]+)",
        message =
            "Ảnh phải là đường dẫn /images/... hoặc URL"
                + " HTTPS.")
    public @NotBlank @Size(max = 500) @Pattern(
        regexp = "(/images/[A-Za-z0-9._/-]+|https://[^\\s]+)",
        message =
            "Ảnh phải là đường dẫn /images/... hoặc URL"
                + " HTTPS.")
    String imageUrl = "/images/product-placeholder.svg";

    public boolean active = true;
    public boolean featured = false;

    @Size(max = 500)
    @Pattern(regexp = "(|https://[^\\s]+)")
    public @Size(max = 500) @Pattern(regexp = "(|https://[^\\s]+)") String sourceUrl = "";

    @Size(max = 100)
    public @Size(max = 100) String sourceRetailer = "";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    public LocalDate priceCheckedAt;

    public String getSourceUrl() {
      return this.sourceUrl;
    }

    public void setSourceUrl(String v) {
      this.sourceUrl = v;
    }

    public String getSourceRetailer() {
      return this.sourceRetailer;
    }

    public void setSourceRetailer(String v) {
      this.sourceRetailer = v;
    }

    public LocalDate getPriceCheckedAt() {
      return this.priceCheckedAt;
    }

    public void setPriceCheckedAt(LocalDate v) {
      this.priceCheckedAt = v;
    }

    public Long getId() {
      return this.id;
    }

    public void setId(Long v) {
      this.id = v;
    }

    public String getSku() {
      return this.sku;
    }

    public void setSku(String value) {
      this.sku = value == null ? "" : value.strip().toUpperCase(java.util.Locale.ROOT);
    }

    public String getVersion() {
      return this.version;
    }

    public void setVersion(String v) {
      this.version = v;
    }

    public String getName() {
      return this.name;
    }

    public void setName(String v) {
      this.name = v;
    }

    public String getBrand() {
      return this.brand;
    }

    public void setBrand(String v) {
      this.brand = v;
    }

    public Long getCategoryId() {
      return this.categoryId;
    }

    public void setCategoryId(Long v) {
      this.categoryId = v;
    }

    public String getDescription() {
      return this.description;
    }

    public void setDescription(String v) {
      this.description = v;
    }

    public BigDecimal getPrice() {
      return this.price;
    }

    public void setPrice(BigDecimal v) {
      this.price = v;
    }

    public BigDecimal getOriginalPrice() {
      return this.originalPrice;
    }

    public void setOriginalPrice(BigDecimal v) {
      this.originalPrice = v;
    }

    public int getStock() {
      return this.stock;
    }

    public void setStock(int v) {
      this.stock = v;
    }

    public String getImageUrl() {
      return this.imageUrl;
    }

    public void setImageUrl(String v) {
      this.imageUrl = v;
    }

    public boolean isActive() {
      return this.active;
    }

    public void setActive(boolean v) {
      this.active = v;
    }

    public boolean isFeatured() {
      return this.featured;
    }

    public void setFeatured(boolean v) {
      this.featured = v;
    }
  }

  public static class Checkout {
    @NotBlank(message = "Vui lòng nhập người nhận.")
    @Size(max = 100)
    public @NotBlank(message = "Vui lòng nhập người nhận.") @Size(
        max = 100) String recipient = "";

    @Pattern(
        regexp = "0[0-9]{9}",
        message =
            "Số điện thoại gồm 10 chữ số, bắt đầu"
                + " bằng 0.")
    public @Pattern(
        regexp = "0[0-9]{9}",
        message =
            "Số điện thoại gồm 10 chữ số, bắt đầu"
                + " bằng 0.")
    String phone = "";

    @NotBlank
    @Size(
        min = 10,
        max = 500,
        message = "Địa chỉ cần từ 10 đến 500 ký tự.")
    public @NotBlank @Size(
        min = 10,
        max = 500,
        message = "Địa chỉ cần từ 10 đến 500 ký tự.")
    String address = "";

    @Size(max = 1000)
    public @Size(max = 1000) String note = "";

    @NotBlank
    @Pattern(regexp = "[a-f0-9-]{36}")
    public @NotBlank @Pattern(regexp = "[a-f0-9-]{36}") String token = "";

    public String getRecipient() {
      return this.recipient;
    }

    public void setRecipient(String v) {
      this.recipient = v;
    }

    public String getPhone() {
      return this.phone;
    }

    public void setPhone(String v) {
      this.phone = v;
    }

    public String getAddress() {
      return this.address;
    }

    public void setAddress(String v) {
      this.address = v;
    }

    public String getNote() {
      return this.note;
    }

    public void setNote(String v) {
      this.note = v;
    }

    public String getToken() {
      return this.token;
    }

    public void setToken(String v) {
      this.token = v;
    }
  }

  public static class Registration {
    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 100)
    public @NotBlank(message = "Vui lòng nhập họ tên.") @Size(max = 100) String
        fullName = "";

    @NotBlank
    @Email(message = "Email không hợp lệ.")
    @Size(max = 254)
    public @NotBlank @Email(message = "Email không hợp lệ.") @Size(max = 254) String
        email = "";

    @NotBlank
    @Size(
        min = 8,
        max = 64,
        message = "Mật khẩu cần từ 8 đến 64 ký tự.")
    public @NotBlank @Size(
        min = 8,
        max = 64,
        message = "Mật khẩu cần từ 8 đến 64 ký tự.") String
        password = "";

    @NotBlank public String confirmPassword = "";

    public String getFullName() {
      return this.fullName;
    }

    public void setFullName(String v) {
      this.fullName = v;
    }

    public String getEmail() {
      return this.email;
    }

    public void setEmail(String v) {
      this.email = v;
    }

    public String getPassword() {
      return this.password;
    }

    public void setPassword(String v) {
      this.password = v;
    }

    public String getConfirmPassword() {
      return this.confirmPassword;
    }

    public void setConfirmPassword(String v) {
      this.confirmPassword = v;
    }
  }
}
