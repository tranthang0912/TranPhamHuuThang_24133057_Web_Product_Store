import json
import pathlib
import shutil

root = pathlib.Path(__file__).resolve().parents[1]
asset_root = pathlib.Path('C:/Users/ADMIN/.codex/visualizations/2026/09/04/01a06b02-ccda-75f1-adbb-391b1f5dd0b0')
items = json.loads((asset_root/'tech-assets/manifest-40.json').read_text(encoding='utf-8-sig')) + json.loads((asset_root/'hardware-assets/manifest.json').read_text(encoding='utf-8-sig'))
assert len(items) == 80 and len({x['sourceUrl'] for x in items}) == 80
categories = {'Bàn phím':('ban-phim','⌨'), 'Chuột':('chuot','◉'), 'Tai nghe & loa':('tai-nghe-loa','♫'), 'Màn hình':('man-hinh','▣'), 'Laptop':('laptop','▱'), 'Phụ kiện':('phu-kien','⌁')}
literal = lambda value: json.dumps(value, ensure_ascii=False)
java_items = []
for i, item in enumerate(items):
    file = pathlib.Path(item['file'])
    assert file.is_file() and file.stat().st_size > 1000
    assert 0 < item['currentPriceVnd'] < 999999999
    assert item['originalPriceVnd'] == 0 or item['originalPriceVnd'] >= item['currentPriceVnd']
    assert item['sourceUrl'].startswith('https://')
    name = file.name
    shutil.copyfile(file, root/'src/main/resources/static/images'/name)
    item['localImage'] = '/images/' + name
    item['categorySlug'] = categories[item['category']][0]
    if not item.get('segment'):
        limit = 20000000 if item['category']=='Laptop' else 5000000 if item['category']=='Màn hình' else 2000000
        item['segment'] = 'Tầm trung' if item['currentPriceVnd'] <= limit else 'Cao cấp'
    # Only source-backed description selected by researchers, no generated specifications.
    description = item['description']
    item['stockFixture'] = 7 if i%9==0 else 15 + (i*7)%46
    featured = i in [0,3,13,16,17,18,32,36,40,42,52,64]
    fields=[literal(item['category']),literal(item['categorySlug']),literal(categories[item['category']][1]),literal(item['name']),literal(item['brand']),literal(description),str(item['currentPriceVnd'])+'L',str(item['originalPriceVnd'])+'L',str(item['stockFixture']),literal(item['localImage']),str(featured).lower(),literal(item['sourceUrl']),literal(item['retailer']),literal(item['date'])]
    java_items.append('        new Product(' + ', '.join(fields) + ')')
    item.pop('file',None); item.pop('imagePath',None)
(root/'database/product-sources.json').write_text(json.dumps(items,ensure_ascii=False,indent=2),encoding='utf-8')
java = '''import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Properties;

/**
 * File nạp dữ liệu độc lập, KHÔNG thuộc ứng dụng Spring Boot.
 * Chạy từ thư mục dự án: ./scripts/seed-products.ps1
 * Cần chạy database/schema.sql một lần trước đó.
 * Có thể xóa file này sau khi nạp: mọi sản phẩm đã nằm trong SQL Server.
 * Chạy lại không ghi đè sản phẩm đã có hoặc đặt lại tồn kho.
 * Tên, ảnh, thông số và giá: nguồn nhà bán lẻ ngày 04/09/2026.
 * Số lượng tồn kho là dữ liệu bài tập, không phải tồn kho của nhà bán lẻ.
 */
public class SeedProducts {
    record Product(String category, String slug, String icon, String name,
            String brand, String description, long price, long originalPrice,
            int stock, String image, boolean featured, String sourceUrl,
            String retailer, String checkedDate) {}

    static final List<Product> PRODUCTS = List.of(
__PRODUCTS__
    );

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        Path configFile = Path.of(".local/application.properties");
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                config.load(reader);
            }
        }
        String url = setting(config, "DB_URL", "spring.datasource.url",
            "jdbc:sqlserver://localhost:1433;databaseName=ProductStore;encrypt=true;trustServerCertificate=true");
        String username = setting(config, "DB_USERNAME", "spring.datasource.username", "productstore_app");
        String password = setting(config, "DB_PASSWORD", "spring.datasource.password", "");
        if (password.isBlank()) throw new IllegalStateException("Chưa có mật khẩu SQL. Chạy scripts/setup-local.ps1 trước.");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            try (Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT DB_NAME() database_name, CAST(SERVERPROPERTY('ServerName') AS NVARCHAR(128)) server_name")) {
                result.next();
                String database = result.getString("database_name");
                if (!"ProductStore".equalsIgnoreCase(database)) {
                    throw new IllegalStateException("Dừng nạp: database phải là ProductStore, hiện tại là " + database);
                }
                System.out.println("[OK] ĐÃ KẾT NỐI SQL SERVER: " + result.getString("server_name"));
                System.out.println("[OK] DATABASE: " + database);
            }
            connection.setAutoCommit(false);
            int inserted = 0;
            try {
                for (Product product : PRODUCTS) {
                    long categoryId = categoryId(connection, product);
                    // Khóa theo nguồn sản phẩm giúp chạy lại không nạp trùng.
                    try (PreparedStatement check = connection.prepareStatement("SELECT id FROM Products WITH(UPDLOCK,HOLDLOCK) WHERE source_url=?")) {
                        check.setString(1, product.sourceUrl());
                        try (ResultSet result = check.executeQuery()) { if (result.next()) continue; }
                    }
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO Products(category_id,name,brand,description,price,original_price,stock,image_url,active,featured,source_url,source_retailer,price_checked_at) VALUES(?,?,?,?,?,?,?,?,1,?,?,?,?)")) {
                        insert.setLong(1, categoryId);
                        insert.setNString(2, product.name());
                        insert.setNString(3, product.brand());
                        insert.setNString(4, product.description());
                        insert.setBigDecimal(5, BigDecimal.valueOf(product.price()));
                        insert.setBigDecimal(6, BigDecimal.valueOf(product.originalPrice()));
                        insert.setInt(7, product.stock());
                        insert.setNString(8, product.image());
                        insert.setBoolean(9, product.featured());
                        insert.setNString(10, product.sourceUrl());
                        insert.setNString(11, product.retailer());
                        insert.setDate(12, Date.valueOf(product.checkedDate()));
                        inserted += insert.executeUpdate();
                    }
                }
                connection.commit();
                System.out.println("[OK] Đã thêm " + inserted + " sản phẩm; bỏ qua " + (PRODUCTS.size()-inserted) + " sản phẩm đã tồn tại.");
                try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM Products WHERE active=1")) {
                    result.next(); System.out.println("[OK] Tổng sản phẩm đang bán trong SQL Server: " + result.getInt(1));
                }
                System.out.println("Có thể xóa SeedProducts.java sau khi nạp. Website không cần file này để chạy.");
            } catch (Exception error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException error) {
            System.err.println("[LỖI] Chưa kết nối/nạp được SQL Server. Kiểm tra SQL Server, cổng 1433 và thông tin trong .local/application.properties.");
            throw error;
        }
    }

    static long categoryId(Connection connection, Product product) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT id FROM Categories WITH(UPDLOCK,HOLDLOCK) WHERE slug=?")) {
            query.setString(1, product.slug());
            try (ResultSet result = query.executeQuery()) { if (result.next()) return result.getLong(1); }
        }
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO Categories(name,slug,icon) OUTPUT INSERTED.id VALUES(?,?,?)")) {
            insert.setNString(1, product.category()); insert.setString(2, product.slug()); insert.setNString(3, product.icon());
            try (ResultSet result = insert.executeQuery()) { result.next(); return result.getLong(1); }
        }
    }

    static String setting(Properties config, String envName, String key, String fallback) {
        String environment = System.getenv(envName);
        return environment != null && !environment.isBlank() ? environment : config.getProperty(key, fallback);
    }
}
'''.replace('__PRODUCTS__',',\n'.join(java_items))
(root/'database/SeedProducts.java').write_text(java,encoding='utf-8')
print('Created standalone Java seeder for',len(items),'products.')
print('Category counts:', {c:sum(i['category']==c for i in items) for c in categories})
print('Midrange:',sum(i['segment']=='Tầm trung' for i in items))
