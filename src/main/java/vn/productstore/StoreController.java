package vn.productstore;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.productstore.Forms;
import vn.productstore.Models;
import vn.productstore.StoreException;
import vn.productstore.StoreRepository;
import vn.productstore.StoreService;

@Controller
public class StoreController {
    private final StoreRepository repo;
    private final StoreService service;

    public StoreController(StoreRepository repo, StoreService service) {
        this.repo = repo;
        this.service = service;
    }

    private long userId(Principal p) {
        return this.repo.customer(p.getName()).id();
    }

    @GetMapping(value={"/"})
    public String home(Model model) {
        model.addAttribute("catalog", (Object)this.repo.catalog(null, null, "featured", 1, false, false, false, null));
        model.addAttribute("heroProduct", (Object)this.repo.categoryHighlight("laptop"));
        model.addAttribute("secondaryProduct", (Object)this.repo.categoryHighlight("man-hinh"));
        model.addAttribute("title", (Object)"Công nghệ cho góc học tập của bạn");
        return "index";
    }

    @GetMapping(value={"/products"})
    public String products(@RequestParam(defaultValue="") String q, @RequestParam(required=false) Long category, @RequestParam(defaultValue="featured") String sort, @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="false") boolean deals, @RequestParam(defaultValue="false") boolean inStock, @RequestParam(required=false) BigDecimal maxPrice, Model m) {
        m.addAttribute("catalog", (Object)this.repo.catalog(q, category, sort, page, false, deals, inStock, maxPrice));
        m.addAttribute("q", (Object)q);
        m.addAttribute("selectedCategory", (Object)category);
        m.addAttribute("sort", (Object)sort);
        m.addAttribute("deals", (Object)deals);
        m.addAttribute("inStock", (Object)inStock);
        m.addAttribute("maxPrice", (Object)maxPrice);
        m.addAttribute("title", (Object)(deals ? "Ưu đãi hôm nay" : "Khám phá sản phẩm"));
        return "products";
    }

    @GetMapping(value={"/products/{id}"})
    public String product(@PathVariable long id, Model m) {
        Models.Product p = this.repo.product(id);
        if (!p.active()) {
            throw new ResponseStatusException((HttpStatusCode)HttpStatus.NOT_FOUND);
        }
        m.addAttribute("product", (Object)p);
        m.addAttribute("related", this.repo.catalog(null, p.categoryId(), "featured", 1, false, false, false, null).products().stream().filter(x -> x.id() != id).limit(4L).toList());
        m.addAttribute("title", (Object)p.name());
        return "product";
    }

    @GetMapping(value={"/login"})
    public String login() {
        return "login";
    }

    @GetMapping(value={"/register"})
    public String register(Model m) {
        m.addAttribute("form", (Object)new Forms.Registration());
        return "register";
    }

    @PostMapping(value={"/register"})
    public String register(@Valid @ModelAttribute(value="form") Forms.Registration form, BindingResult errors, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "register";
        }
        try {
            this.service.register(form);
        }
        catch (StoreException e) {
            errors.reject("registration", e.getMessage());
            return "register";
        }
        flash.addFlashAttribute("success", (Object)"Đăng ký thành công. Hãy đăng nhập để bắt đầu mua sắm.");
        return "redirect:/login";
    }

    @GetMapping(value={"/cart"})
    public String cart(Principal p, Model m) {
        m.addAttribute("cart", (Object)this.repo.cart(this.userId(p)));
        return "cart";
    }

    @PostMapping(value={"/cart/add"})
    public String addCart(@RequestParam long productId, @RequestParam(defaultValue="1") int quantity, Principal p, RedirectAttributes flash) {
        try {
            this.service.updateCart(this.userId(p), productId, quantity, true);
            flash.addFlashAttribute("success", (Object)"Đã thêm sản phẩm vào giỏ hàng.");
        }
        catch (StoreException e) {
            flash.addFlashAttribute("error", (Object)e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping(value={"/cart/update"})
    public String updateCart(@RequestParam long productId, @RequestParam int quantity, Principal p, RedirectAttributes flash) {
        try {
            this.service.updateCart(this.userId(p), productId, quantity, false);
        }
        catch (StoreException e) {
            flash.addFlashAttribute("error", (Object)e.getMessage());
        }
        return "redirect:/cart";
    }

    @GetMapping(value={"/wishlist"})
    public String wishlist(Principal p, Model m) {
        m.addAttribute("products", this.repo.wishlist(this.userId(p)));
        return "wishlist";
    }

    @PostMapping(value={"/wishlist/toggle"})
    public String wishlist(@RequestParam long productId, Principal p, RedirectAttributes flash) {
        this.service.toggleWishlist(this.userId(p), productId);
        flash.addFlashAttribute("success", (Object)"Đã cập nhật danh sách yêu thích.");
        return "redirect:/wishlist";
    }

    @GetMapping(value={"/checkout"})
    public String checkout(Principal p, Model m, HttpSession session) {
        Models.Customer user = this.repo.customer(p.getName());
        Models.Cart cart = this.repo.cart(user.id());
        if (cart.items().isEmpty()) {
            return "redirect:/cart";
        }
        Forms.Checkout form = new Forms.Checkout();
        form.recipient = user.fullName();
        form.phone = user.phone();
        form.address = user.address();
        form.token = UUID.randomUUID().toString();
        session.setAttribute("checkoutToken", (Object)form.token);
        m.addAttribute("form", (Object)form);
        m.addAttribute("cart", (Object)cart);
        return "checkout";
    }

    @PostMapping(value={"/checkout"})
    public String checkout(@Valid @ModelAttribute(value="form") Forms.Checkout form, BindingResult errors, Principal p, Model m, HttpSession session, RedirectAttributes flash) {
        m.addAttribute("cart", (Object)this.repo.cart(this.userId(p)));
        if (errors.hasErrors()) {
            return "checkout";
        }
        if (!form.token.equals(session.getAttribute("checkoutToken"))) {
            throw new StoreException("Phiên đặt hàng đã thay đổi. Vui lòng mở lại trang thanh toán.");
        }
        try {
            long id = this.service.checkout(this.userId(p), form);
            flash.addFlashAttribute("success", (Object)"Đặt hàng thành công! Bạn sẽ thanh toán khi nhận hàng.");
            return "redirect:/orders/" + id;
        }
        catch (StoreException e) {
            errors.reject("checkout", e.getMessage());
            return "checkout";
        }
    }

    @GetMapping(value={"/orders"})
    public String orders(Principal p, Model m) {
        m.addAttribute("orders", this.repo.orders(this.userId(p), null));
        return "orders";
    }

    @GetMapping(value={"/orders/{id}"})
    public String order(@PathVariable long id, Principal p, Model m) {
        Models.Customer user = this.repo.customer(p.getName());
        Models.Order order = this.repo.order(id);
        if (order.userId() != user.id() && !user.role().equals("ADMIN")) {
            throw new ResponseStatusException((HttpStatusCode)HttpStatus.NOT_FOUND);
        }
        m.addAttribute("order", (Object)order);
        m.addAttribute("items", this.repo.orderItems(id));
        return "order";
    }

    @PostMapping(value={"/orders/{id}/cancel"})
    public String cancel(@PathVariable long id, Principal p, RedirectAttributes flash) {
        this.service.changeOrderStatus(id, this.userId(p), false, "CANCELLED");
        flash.addFlashAttribute("success", (Object)"Đã hủy đơn hàng và hoàn lại số lượng tồn kho.");
        return "redirect:/orders/" + id;
    }
}

