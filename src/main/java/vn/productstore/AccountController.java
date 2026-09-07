package vn.productstore;

import java.security.Principal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {
  private final JdbcTemplate db;
  private final StoreService service;
  private final PasswordEncoder encoder;

  public AccountController(JdbcTemplate db, StoreService service, PasswordEncoder encoder) {
    this.db = db;
    this.service = service;
    this.encoder = encoder;
  }

  @GetMapping(value = {"/account"})
  public String account() {
    return "account";
  }

  @PostMapping(value = {"/account"})
  public String update(
      @RequestParam String fullName,
      @RequestParam(defaultValue = "") String phone,
      @RequestParam(defaultValue = "") String address,
      Principal p,
      RedirectAttributes flash) {
    if (fullName.isBlank()
        || fullName.length() > 100
        || !phone.matches("(0[0-9]{9})?")
        || address.length() > 500) {
      throw new StoreException(
          "Vui lòng kiểm tra họ tên, số điện thoại và"
              + " địa chỉ.");
    }
    this.db.update(
        "UPDATE Users SET full_name=?,phone=?,address=? WHERE email=?",
        new Object[] {fullName.strip(), phone, address.strip(), p.getName()});
    flash.addFlashAttribute(
        "success", (Object) "Đã lưu thông tin tài khoản.");
    return "redirect:/account";
  }

  @PostMapping(value = {"/account/password"})
  public String password(
      @RequestParam String currentPassword,
      @RequestParam String newPassword,
      @RequestParam String confirmPassword,
      Principal p,
      RedirectAttributes flash) {
    String hash =
        (String)
            this.db.queryForObject(
                "SELECT password_hash FROM Users WHERE email=?",
                String.class,
                new Object[] {p.getName()});
    if (!this.encoder.matches((CharSequence) currentPassword, hash)) {
      throw new StoreException("Mật khẩu hiện tại không đúng.");
    }
    this.service.validatePassword(newPassword);
    if (!newPassword.equals(confirmPassword)) {
      throw new StoreException("Mật khẩu nhập lại chưa khớp.");
    }
    this.db.update(
        "UPDATE Users SET password_hash=? WHERE email=?",
        new Object[] {this.encoder.encode((CharSequence) newPassword), p.getName()});
    flash.addFlashAttribute("success", (Object) "Đã đổi mật khẩu.");
    return "redirect:/account";
  }
}
