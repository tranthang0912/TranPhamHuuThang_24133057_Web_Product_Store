package com.baitap03;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import javax.imageio.ImageIO;
import org.apache.catalina.Context;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;
import com.baitap03.config.JPAConfig;
import com.baitap03.model.*;
import com.baitap03.repository.UserRepository;
import com.baitap03.service.UserServiceImpl;
import com.baitap03.util.OtpUtil;
import com.baitap03.util.PasswordUtil;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebIntegrationTest {
    private Tomcat tomcat;
    private String base;
    private Path uploads;
    private final UserRepository users = new UserRepository();
    private int aliceId;
    private int bobId;
    private int categoryId;
    private int productId;

    @BeforeAll void start() throws Exception {
        Path work = Path.of("target", "integration").toAbsolutePath();
        uploads = work.resolve("uploads");
        System.setProperty("APP_UPLOAD_DIR", uploads.toString());
        System.setProperty("APP_JDBC_URL", "jdbc:h2:mem:profiletest;MODE=MSSQLServer;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS dbo");
        System.setProperty("APP_JDBC_DRIVER", "org.h2.Driver");
        System.setProperty("APP_JDBC_USER", "sa");
        System.setProperty("APP_JDBC_PASSWORD", "test");
        System.setProperty("APP_DB_DIALECT", "org.hibernate.dialect.H2Dialect");
        User alice = account("alice", "Nguyễn Văn An");
        User bob = account("bob", "Trần Bình");
        users.insert(alice); users.insert(bob);
        aliceId = alice.getUserid(); bobId = bob.getUserid();
        var em = JPAConfig.getEntityManager();
        em.getTransaction().begin();
        Category category = new Category(); category.setCategoryname("Điện thoại"); category.setStatus(1); em.persist(category);
        Product product = new Product(); product.setProductname("Sản phẩm kiểm thử"); product.setPrice(new BigDecimal("1000.00"));
        product.setQuantity(3); product.setStatus(1); product.setCategory(category); em.persist(product);
        em.getTransaction().commit(); categoryId = category.getCategoryid(); productId = product.getProductid(); em.close();
        Path docBase = work.resolve("webapp");
        copyTree(Path.of("src/main/webapp"), docBase);
        copyTree(Path.of("target/classes"), docBase.resolve("WEB-INF/classes"));
        tomcat = new Tomcat();
        tomcat.setBaseDir(work.resolve("tomcat").toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        Context context = tomcat.addWebapp("/bai_tap_03", docBase.toString());
        context.setParentClassLoader(getClass().getClassLoader());
        WebappLoader loader = new WebappLoader(); loader.setDelegate(true); context.setLoader(loader);
        tomcat.start();
        base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/bai_tap_03";
    }

    @AfterAll void stop() throws Exception {
        if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
        JPAConfig.shutdown();
    }

    @Test void everyPageRendersOneSiteMeshLayoutWithLocalBootstrap() throws Exception {
        HttpClient client = login();
        for (String route : new String[]{"/home", "/product", "/product/detail?id=" + productId, "/login", "/register",
                "/activate", "/forgot-password", "/reset-password", "/profile", "/admin/categories", "/admin/category/add",
                "/admin/category/edit?id=" + categoryId, "/admin/products", "/admin/product/add", "/admin/product/edit?id=" + productId}) {
            var result = get(client, route);
            assertEquals(200, result.statusCode(), route + " " + result.body());
            assertEquals(1, result.body().split("id=\"wrapper\"", -1).length - 1, route);
            assertTrue(result.body().contains("assets/vendor/simple-sidebar/styles.css"), route);
            assertFalse(result.body().contains("<sitemesh:"), route);
        }
        assertEquals(200, get(client, "/assets/vendor/bootstrap/bootstrap.bundle.min.js").statusCode());
        assertEquals(404, get(client, "/views/profile.jsp").statusCode());
        assertEquals(404, get(client, "/common/admin/product-form.jsp").statusCode());
    }

    @Test void guestCannotViewOrUpdateProfileOrAdmin() throws Exception {
        HttpClient guest = client();
        assertEquals(302, get(guest, "/profile").statusCode());
        assertEquals(302, multipart(guest, "/profile", Map.of("fullname", "Hacked"), null, null).statusCode());
        assertEquals(302, get(guest, "/admin/categories").statusCode());
    }

    @Test void profilePersistsUnicodeAndMultipartImageAndPreservesSensitiveFields() throws Exception {
        HttpClient client = login();
        String token = csrf(get(client, "/profile").body());
        User before = users.findById(aliceId);
        Map<String,String> fields = Map.of("fullname", " Nguyễn Thị Ánh ", "phone", "0912345678", "csrfToken", token,
                "userid", String.valueOf(bobId), "password", "hacked", "email", "hacked@example.com");
        assertEquals(302, multipart(client, "/profile", fields, "avatar.png", png()).statusCode());
        User after = users.findById(aliceId);
        assertEquals("Nguyễn Thị Ánh", after.getFullname()); assertEquals("0912345678", after.getPhone());
        assertEquals(before.getPassword(), after.getPassword()); assertEquals(before.getEmail(), after.getEmail());
        assertEquals("Trần Bình", users.findById(bobId).getFullname());
        assertTrue(Files.isRegularFile(uploads.resolve(after.getImages())));
        assertEquals(200, get(client, "/image?fname=" + after.getImages()).statusCode());
        assertTrue(get(client, "/profile").body().contains("Cập nhật hồ sơ thành công"));
        assertEquals(302, multipart(client, "/profile", fields, null, null).statusCode());
        assertEquals(after.getImages(), users.findById(aliceId).getImages());
        assertEquals(302, multipart(client, "/profile", fields, "new.png", png()).statusCode());
        assertFalse(Files.exists(uploads.resolve(after.getImages())));
        assertNotEquals(after.getImages(), users.findById(aliceId).getImages());
    }

    @Test void rejectsBadUploadsAndValidationNeverChangesAccount() throws Exception {
        HttpClient client = login();
        String token = csrf(get(client, "/profile").body());
        Map<String,String> fields = Map.of("fullname", "An", "phone", "0912345678", "csrfToken", token);
        User before = users.findById(aliceId);
        var invalid = multipart(client, "/profile", Map.of("fullname", " ", "phone", "abc", "csrfToken", token), null, null);
        assertEquals(400, invalid.statusCode()); assertTrue(invalid.body().contains("Vui lòng kiểm tra lại thông tin"));
        assertEquals(400, multipart(client, "/profile", fields, "fake.png", "not an image".getBytes()).statusCode());
        assertEquals(400, multipart(client, "/profile", fields, "payload.jsp", png()).statusCode());
        assertEquals(400, multipart(client, "/profile", fields, "empty.png", new byte[0]).statusCode());
        assertEquals(403, multipart(client, "/profile", Map.of("fullname", "An", "phone", "0912345678"), null, null).statusCode());
        assertEquals(302, multipart(client, "/profile", fields, "large.png", new byte[5 * 1024 * 1024 + 1]).statusCode());
        assertTrue(get(client, "/profile").body().contains("Ảnh tối đa 5 MB"));
        assertEquals(before.getFullname(), users.findById(aliceId).getFullname());
        assertEquals(before.getImages(), users.findById(aliceId).getImages());
    }

    @Test void authFormsValidateBeforeDatabaseOrEmailAndOtpWorks() throws Exception {
        HttpClient client = client();
        for (String route : new String[]{"/login", "/register", "/activate", "/activate/resend", "/forgot-password", "/reset-password"}) {
            var result = post(client, route, Map.of("email", "invalid", "password", "x"));
            assertEquals(400, result.statusCode(), route + result.body());
            assertTrue(result.body().contains("alert-danger"), route);
        }
        User pending = account("pending", "Tài khoản chờ"); pending.setActive(false);
        pending.setActivationOtpHash(OtpUtil.hash("123456")); pending.setActivationOtpExpiresAt(LocalDateTime.now().plusMinutes(5)); users.insert(pending);
        assertEquals(302, post(client, "/activate", Map.of("email", pending.getEmail(), "otp", "123456")).statusCode());
        assertTrue(users.findById(pending.getUserid()).isActive());
        assertTrue(new UserServiceImpl().preparePasswordResetOtp(pending.getEmail(), "654321"));
        assertEquals(302, post(client, "/reset-password", Map.of("email", pending.getEmail(), "otp", "654321", "password", "NewPassword123", "confirmPassword", "NewPassword123")).statusCode());
        assertTrue(PasswordUtil.verify("NewPassword123", users.findById(pending.getUserid()).getPassword()));
    }

    @Test void removesNewUploadWhenAccountDisappearsBeforeCommit() throws Exception {
        User doomed = account("removed", "Tài khoản bị xóa"); users.insert(doomed);
        HttpClient client = client();
        assertEquals(302, post(client, "/login", Map.of("identifier", "removed", "password", "Password123")).statusCode());
        String token = csrf(get(client, "/profile").body());
        Files.createDirectories(uploads);
        long before;
        try (var files = Files.list(uploads)) { before = files.count(); }
        var em = JPAConfig.getEntityManager();
        em.getTransaction().begin(); em.remove(em.find(User.class, doomed.getUserid())); em.getTransaction().commit(); em.close();
        assertEquals(400, multipart(client, "/profile", Map.of("fullname", "An", "phone", "0912345678", "csrfToken", token), "new.png", png()).statusCode());
        try (var files = Files.list(uploads)) { assertEquals(before, files.count()); }
    }

    @Test void catalogFormsValidateAndCrudWorks() throws Exception {
        HttpClient client = login();
        assertEquals(400, multipart(client, "/admin/category/insert", Map.of("categoryname", " ", "status", "9"), null, null).statusCode());
        assertEquals(400, multipart(client, "/admin/product/insert", Map.of("price", "-1", "quantity", "abc"), null, null).statusCode());
        var malformedCategory = multipart(client, "/admin/product/insert", Map.of("productname", "Tên đã nhập", "price", "10", "quantity", "2", "categoryid", "abc", "status", "1"), null, null);
        assertEquals(400, malformedCategory.statusCode());
        assertTrue(malformedCategory.body().contains("Tên đã nhập"));
        assertEquals(400, post(client, "/admin/category/delete", Map.of("id", "abc")).statusCode());
        assertEquals(405, get(client, "/admin/category/delete?id=" + categoryId).statusCode());
        assertEquals(400, get(client, "/admin/category/edit?id=abc").statusCode());
        assertEquals(400, get(client, "/admin/categories?keyword=" + "x".repeat(51)).statusCode());
        assertEquals(400, get(client, "/product?category=bad").statusCode());
        Map<String,String> categoryFields = Map.of("categoryname", "Danh mục mới", "status", "1");
        assertEquals(302, multipart(client, "/admin/category/insert", categoryFields, "category.png", png()).statusCode());
        var categories = new com.baitap03.service.CategoryServiceImpl();
        Category added = categories.findByCategoryname("Danh mục mới"); assertNotNull(added);
        assertEquals(400, multipart(client, "/admin/category/insert", categoryFields, "duplicate.png", png()).statusCode());
        assertEquals(302, multipart(client, "/admin/category/update", Map.of("categoryid", "" + added.getCategoryid(), "categoryname", "Danh mục đã sửa", "status", "0"), null, null).statusCode());
        assertEquals(added.getImages(), categories.findById(added.getCategoryid()).getImages());
        assertEquals(400, multipart(client, "/admin/category/update", Map.of("categoryid", "" + added.getCategoryid(), "categoryname", "Điện thoại", "status", "1"), null, null).statusCode());
        Map<String,String> productFields = Map.of("productname", "Sản phẩm mới", "price", "123.45", "quantity", "2", "categoryid", "" + added.getCategoryid(), "status", "1");
        assertEquals(302, multipart(client, "/admin/product/insert", productFields, "product.png", png()).statusCode());
        var products = new com.baitap03.service.ProductServiceImpl();
        Product addedProduct = products.findAll().stream().filter(p -> p.getProductname().equals("Sản phẩm mới")).findFirst().orElseThrow();
        var update = new java.util.HashMap<>(productFields); update.put("productid", "" + addedProduct.getProductid()); update.put("price", "456.78");
        assertEquals(302, multipart(client, "/admin/product/update", update, null, null).statusCode());
        assertEquals(new BigDecimal("456.78"), products.findById(addedProduct.getProductid()).getPrice());
        assertEquals(302, post(client, "/admin/product/delete", Map.of("id", "" + addedProduct.getProductid())).statusCode());
        assertEquals(302, post(client, "/admin/category/delete", Map.of("id", "" + added.getCategoryid())).statusCode());
        assertNull(categories.findById(added.getCategoryid()));
        assertFalse(Files.exists(uploads.resolve(added.getImages())));
    }

    private User account(String username, String name) {
        User user = new User(); user.setUsername(username); user.setFullname(name); user.setEmail(username + "@example.com");
        user.setPassword(PasswordUtil.hash("Password123")); user.setActive(true); return user;
    }
    private HttpClient client() { return HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build(); }
    private HttpClient login() throws Exception {
        HttpClient client = client(); assertEquals(302, post(client, "/login", Map.of("identifier", "alice", "password", "Password123")).statusCode()); return client;
    }
    private HttpResponse<String> get(HttpClient client, String route) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + route)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> post(HttpClient client, String route, Map<String,String> fields) throws Exception {
        String body = fields.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create(base + route)).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> multipart(HttpClient client, String route, Map<String,String> fields, String filename, byte[] file) throws Exception {
        String boundary = "TestBoundary123";
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (var field : fields.entrySet()) body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n" + field.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        if (file != null) {
            String part = route.equals("/profile") ? "images" : "images1";
            body.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + part + "\"; filename=\"" + filename + "\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(file); body.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }
        body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return client.send(HttpRequest.newBuilder(URI.create(base + route)).header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build(), HttpResponse.BodyHandlers.ofString());
    }
    private byte[] png() throws Exception { ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ImageIO.write(new BufferedImage(4,4,BufferedImage.TYPE_INT_RGB), "png", bytes); return bytes.toByteArray(); }
    private String csrf(String html) { var matcher = Pattern.compile("name=\"csrfToken\" value=\"([^\"]+)\"").matcher(html); assertTrue(matcher.find(), html); return matcher.group(1); }
    private void copyTree(Path source, Path target) throws Exception {
        try (var files = Files.walk(source)) {
            for (Path file : files.toList()) {
                Path dest = target.resolve(source.relativize(file));
                if (Files.isDirectory(file)) Files.createDirectories(dest); else Files.copy(file, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
