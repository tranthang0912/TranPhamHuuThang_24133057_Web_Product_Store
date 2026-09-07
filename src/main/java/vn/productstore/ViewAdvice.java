package vn.productstore;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ViewAdvice {
  private final StoreRepository repo;

  public ViewAdvice(StoreRepository repo) {
    this.repo = repo;
  }

  @ModelAttribute
  public void shared(Model model, Principal principal, HttpServletRequest request) {
    model.addAttribute("categories", this.repo.categories());
    model.addAttribute(
        "currentPath",
        (Object) request.getRequestURI().substring(request.getContextPath().length()));
    model.addAttribute("cartCount", (Object) 0);
    model.addAttribute("wishlistIds", Set.of());
    model.addAttribute(
        "priceOptions",
        List.of(
            Integer.valueOf(200000),
            Integer.valueOf(500000),
            Integer.valueOf(1000000),
            Integer.valueOf(5000000),
            Integer.valueOf(20000000)));
    if (principal != null) {
      Models.Customer user = this.repo.customer(principal.getName());
      model.addAttribute("currentUser", (Object) user);
      model.addAttribute("cartCount", (Object) this.repo.cartCount(user.id()));
      model.addAttribute("wishlistIds", this.repo.wishlistIds(user.id()));
    }
  }

  @ExceptionHandler(value = {StoreException.class})
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public String problem(StoreException error, Model model) {
    model.addAttribute("problem", (Object) error.getMessage());
    return "error";
  }
}
