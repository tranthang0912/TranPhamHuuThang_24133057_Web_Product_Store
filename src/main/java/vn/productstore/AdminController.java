package vn.productstore;

import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.productstore.Forms;
import vn.productstore.Models;
import vn.productstore.StoreException;
import vn.productstore.StoreRepository;
import vn.productstore.StoreService;

@Controller
@RequestMapping(value={"/admin"})
public class AdminController {
    private final StoreRepository repo;
    private final StoreService service;
    private final JdbcTemplate db;

    public AdminController(StoreRepository repo, StoreService service, JdbcTemplate db) {
        this.repo = repo;
        this.service = service;
        this.db = db;
    }

    @GetMapping(value={"", "/"})
    public String dashboard(Model m) {
        m.addAttribute("stats", this.repo.stats());
        m.addAttribute("orders", this.repo.orders(null, null).stream().limit(6L).toList());
        return "admin/dashboard";
    }

    @GetMapping(value={"/products"})
    public String products(@RequestParam(defaultValue="") String q, @RequestParam(defaultValue="1") int page, Model m) {
        m.addAttribute("catalog", (Object)this.repo.catalog(q, null, "newest", page, true, false, false, null));
        m.addAttribute("q", (Object)q);
        return "admin/products";
    }

    @GetMapping(value={"/statistics"})
    public String statistics(Model model) {
        model.addAttribute("quantities", this.repo.quantityStatistics());
        return "admin/statistics";
    }

    @GetMapping(value={"/products/new"})
    public String newProduct(Model m) {
        m.addAttribute("form", (Object)new Forms.ProductForm());
        return "admin/product-form";
    }

    @GetMapping(value={"/products/{id}/edit"})
    public String edit(@PathVariable long id, Model m) {
        Models.Product p = this.repo.product(id);
        Forms.ProductForm f = new Forms.ProductForm();
        f.id = p.id();
        f.sku = p.sku();
        f.name = p.name();
        f.categoryId = p.categoryId();
        f.brand = p.brand();
        f.description = p.description();
        f.price = p.price();
        f.originalPrice = p.originalPrice();
        f.stock = p.stock();
        f.imageUrl = p.imageUrl();
        f.active = p.active();
        f.featured = p.featured();
        f.version = p.version();
        f.sourceUrl = p.sourceUrl();
        f.sourceRetailer = p.sourceRetailer();
        f.priceCheckedAt = p.priceCheckedAt();
        m.addAttribute("form", (Object)f);
        return "admin/product-form";
    }

    @PostMapping(value={"/products/save"})
    public String save(@Valid @ModelAttribute(value="form") Forms.ProductForm form, BindingResult errors, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            return "admin/product-form";
        }
        try {
            this.service.saveProduct(form);
        }
        catch (StoreException e) {
            errors.reject("product", e.getMessage());
            return "admin/product-form";
        }
        flash.addFlashAttribute("success", (Object)"Đã lưu sản phẩm.");
        return "redirect:/admin/products";
    }

    @PostMapping(value={"/products/{id}/delete"})
    public String deleteProduct(@PathVariable long id, RedirectAttributes flash) {
        this.service.deleteProduct(id);
        flash.addFlashAttribute("success", (Object)"Đã xóa sản phẩm khỏi cửa hàng. Sản phẩm đã có đơn hàng được lưu ẩn để giữ lịch sử.");
        return "redirect:/admin/products";
    }

    @GetMapping(value={"/categories"})
    public String categories() {
        return "admin/categories";
    }

    @PostMapping(value={"/categories/save"})
    public String category(@RequestParam(required=false) Long id, @RequestParam String name, @RequestParam String slug, RedirectAttributes flash) {
        if (name.isBlank() || name.length() > 80 || !slug.matches("[a-z0-9-]{1,80}")) {
            throw new StoreException("Tên danh mục tối đa 80 ký tự. Mã chỉ gồm chữ thường, số và dấu gạch ngang.");
        }
        try {
            if (id == null) {
                this.db.update("INSERT INTO Categories(name,slug) VALUES(?,?)", new Object[]{name.strip(), slug});
            } else if (this.db.update("UPDATE Categories SET name=?,slug=? WHERE id=?", new Object[]{name.strip(), slug, id}) == 0) {
                throw new StoreException("Danh mục không tồn tại.");
            }
        }
        catch (DuplicateKeyException e) {
            throw new StoreException("Tên hoặc mã danh mục đã tồn tại.");
        }
        flash.addFlashAttribute("success", (Object)"Đã lưu danh mục.");
        return "redirect:/admin/categories";
    }

    @PostMapping(value={"/categories/{id}/delete"})
    public String deleteCategory(@PathVariable long id, RedirectAttributes flash) {
        try {
            this.db.update("DELETE FROM Categories WHERE id=?", new Object[]{id});
        }
        catch (DataIntegrityViolationException e) {
            throw new StoreException("Danh mục còn sản phẩm. Chuyển sản phẩm sang danh mục khác trước khi xóa.");
        }
        flash.addFlashAttribute("success", (Object)"Đã xóa danh mục.");
        return "redirect:/admin/categories";
    }

    @GetMapping(value={"/orders"})
    public String orders(@RequestParam(defaultValue="") String status, Model m) {
        m.addAttribute("orders", this.repo.orders(null, status));
        m.addAttribute("selectedStatus", (Object)status);
        return "admin/orders";
    }

    @PostMapping(value={"/orders/{id}/status"})
    public String status(@PathVariable long id, @RequestParam String status, Principal p, RedirectAttributes flash) {
        this.service.changeOrderStatus(id, this.repo.customer(p.getName()).id(), true, status);
        flash.addFlashAttribute("success", (Object)"Đã cập nhật đơn hàng.");
        return "redirect:/orders/" + id;
    }

    @GetMapping(value={"/customers"})
    public String customers(Model m) {
        m.addAttribute("customers", this.repo.customers());
        return "admin/customers";
    }

    private String csv(String s) {
        if (s == null) {
            return "\"\"";
        }
        if (((String)s).matches("^[=+@\\-\\t\\r].*")) {
            s = "'" + (String)s;
        }
        return "\"" + ((String)s).replace("\"", "\"\"") + "\"";
    }

    @GetMapping(value={"/export"})
    public ResponseEntity<byte[]> export() {
        StringBuilder out = new StringBuilder("﻿Mã đơn,Người nhận,Điện thoại,Trạng thái,Tổng tiền,Ngày tạo (UTC)\r\n");
        for (Models.Order o : this.repo.orders(null, null)) {
            out.append(this.csv(o.code())).append(',').append(this.csv(o.recipient())).append(',').append(this.csv(o.phone())).append(',').append(this.csv(o.statusName())).append(',').append(o.total()).append(',').append(this.csv(o.createdAt().toString())).append("\r\n");
        }
        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=don-hang.csv").contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(out.toString().getBytes(StandardCharsets.UTF_8));
    }
}
