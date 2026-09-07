package vn.productstore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class SqlServerConnectionTest {
  private static final Path CONFIG = Path.of(".local", "application.properties");

  public static Connection connect() throws IOException, SQLException {
    Properties config = new Properties();
    try (InputStream input = Files.newInputStream(CONFIG, new OpenOption[0]); ) {
      config.load(input);
    }
    String url = SqlServerConnectionTest.required(config, "spring.datasource.url");
    Properties credentials = new Properties();
    credentials.setProperty(
        "user", SqlServerConnectionTest.required(config, "spring.datasource.username"));
    credentials.setProperty(
        "password", SqlServerConnectionTest.required(config, "spring.datasource.password"));
    credentials.setProperty("loginTimeout", "10");
    return DriverManager.getConnection(url, credentials);
  }

  private static String required(Properties config, String key) throws IOException {
    String value = config.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IOException("Chưa điền " + key + " trong " + String.valueOf(CONFIG));
    }
    return value;
  }

  public static void main(String[] args) {
    System.setOut(new PrintStream((OutputStream) System.out, true, StandardCharsets.UTF_8));
    System.setErr(new PrintStream((OutputStream) System.err, true, StandardCharsets.UTF_8));
    try (Connection connection = SqlServerConnectionTest.connect();
        Statement statement = connection.createStatement();
        ResultSet result =
            statement.executeQuery(
                "SELECT DB_NAME() database_name, SUSER_SNAME() login_name,"
                    + " CAST(SERVERPROPERTY('ServerName') AS NVARCHAR(128)) server_name"); ) {
      result.next();
      String database = result.getString("database_name");
      if (!"ProductStore".equalsIgnoreCase(database)) {
        throw new SQLException(
            "Database phải là ProductStore. Hãy sửa file cấu hình.");
      }
      System.out.println("KẾT NỐI SQL SERVER THÀNH CÔNG!");
      System.out.println("Máy chủ: " + result.getString("server_name"));
      System.out.println("Database: " + database);
      System.out.println("Tài khoản SQL: " + result.getString("login_name"));
      try (Statement counts = connection.createStatement();
          ResultSet totals =
              counts.executeQuery(
                  "SELECT (SELECT COUNT(*) FROM dbo.Products) products, (SELECT COUNT(*) FROM"
                      + " dbo.Users) users"); ) {
        totals.next();
        System.out.println(
            "Sản phẩm đã lưu trong SQL Server: "
                + totals.getInt("products"));
        System.out.println(
            "Tài khoản website đã lưu trong SQL Server: "
                + totals.getInt("users"));
      }
      System.out.println(
          "Mật khẩu được đọc tự động từ "
              + String.valueOf(CONFIG)
              + ". Không cần nhập lại.");
    } catch (IOException | SQLException error) {
      System.err.println("KIỂM TRA SQL SERVER THẤT BẠI.");
      if (error instanceof SQLException) {
        SQLException sql = (SQLException) error;
        System.err.println("Mã lỗi SQL Server: " + sql.getErrorCode());
      }
      System.err.println(
          "Kiểm tra SQL Server/cổng 1433, database ProductStore và tài"
              + " khoản,");
      System.err.println(
          "mật khẩu đã lưu trong "
              + String.valueOf(CONFIG.toAbsolutePath()));
      System.err.println(
          "Trong Spring Tools, chọn working directory là thư mục gốc"
              + " dự án.");
      System.exit(1);
    }
  }
}
