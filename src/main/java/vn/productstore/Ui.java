package vn.productstore;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component(value = "ui")
public class Ui {
  public String money(Object value) {
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
            .format(value == null ? BigDecimal.ZERO : value)
        + " ₫";
  }

  public String date(LocalDateTime value) {
    return value.plusHours(7L).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
  }
}
