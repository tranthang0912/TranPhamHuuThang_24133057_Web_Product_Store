package vn.productstore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {
  private final JdbcTemplate db;
  private final PasswordEncoder encoder;
  private final String email;
  private final String password;

  public AdminBootstrap(
      JdbcTemplate db,
      PasswordEncoder encoder,
      @Value(value = "${store.admin.email}") String email,
      @Value(value = "${store.admin.password}") String password) {
    this.db = db;
    this.encoder = encoder;
    this.email = email;
    this.password = password;
  }

  public void run(String... args) {
    if ((Integer)
            this.db.queryForObject("SELECT COUNT(*) FROM Users WHERE role='ADMIN'", Integer.class)
        == 0) {
      if (this.password.isBlank()) {
        throw new IllegalStateException(
            "Chưa cấu hình quản trị viên. Chạy scripts/setup-local.ps1 hoặc đặt"
                + " STORE_ADMIN_PASSWORD.");
      }
      this.db.update(
          "INSERT INTO Users(full_name,email,password_hash,role) VALUES(N'Quản trị"
              + " viên',?,?,'ADMIN')",
          new Object[] {this.email, this.encoder.encode((CharSequence) this.password)});
    }
  }
}
