# Product Store — Spring Boot, Tomcat và SQL Server

Website bán thiết bị công nghệ phục vụ học tập. Database riêng là **ProductStore**, không dùng hoặc sửa `webst2`.

## Chạy trong Spring Tools

1. Import → Maven → Existing Maven Projects, chọn thư mục chứa `pom.xml`.
2. Chọn JDK 21 trở lên, nhấn F5 rồi Maven → Update Project.
3. Bật dịch vụ **SQL Server (MSSQLSERVER)**. Cấu hình SQL cố định nằm trong `.local/application.properties`.
4. Run As → Spring Boot App; hoặc Run As → Run on Server → Tomcat 11.
5. Spring Boot: http://localhost:8080/. Tomcat ngoài: http://localhost:8080/product-store/.

Tài khoản quản trị học tập: **admin / admin**. Khách hàng đăng ký bằng email. Mật khẩu website được mã hóa BCrypt trong SQL Server.

## Dữ liệu và chức năng

Có 80 sản phẩm công nghệ thực tế, ảnh cục bộ, danh mục, tìm kiếm, giỏ hàng, yêu thích, đặt hàng COD, tài khoản, phân quyền khách/admin, CRUD sản phẩm và danh mục, quản lý đơn, thống kê số lượng và CSV. Không có thống kê doanh thu.

Mỗi sản phẩm có mã SKU duy nhất. SQL Server chặn mã trùng. `database/schema.sql` tạo cấu trúc; `database/SeedProducts.java` nạp mẫu và có thể xóa sau khi nạp; `database/product-sources.json` giữ nguồn sản phẩm.

Chạy Tomcat riêng:

```powershell
./scripts/run-tomcat.ps1 -TomcatHome 'E:\apache-tomcat-11.0.25' -Port 8080
```

Nếu cổng 8080 bận, dừng bản Java/Tomcat khác hoặc dùng cổng 8081. Database và thông tin kết nối không bị thay đổi khi đóng Tomcat.