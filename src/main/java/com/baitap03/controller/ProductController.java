package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import java.math.BigDecimal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.baitap03.model.Category;
import com.baitap03.model.Product;
import com.baitap03.service.CategoryServiceImpl;
import com.baitap03.service.ICategoryService;
import com.baitap03.service.IProductService;
import com.baitap03.service.ProductServiceImpl;
import com.baitap03.util.FormValidator;
import com.baitap03.util.ImageStorage;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = ImageStorage.MAX_FILE_SIZE,
        maxRequestSize = 6L * 1024 * 1024)
@WebServlet(urlPatterns = {"/admin/products", "/admin/product/add", "/admin/product/insert",
        "/admin/product/edit", "/admin/product/update", "/admin/product/delete"})
public class ProductController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final IProductService productService = new ProductServiceImpl();
    private final ICategoryService categoryService = new CategoryServiceImpl();
    private final ImageStorage images = new ImageStorage();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/admin/products":
                req.setAttribute("products", productService.findAll());
                ViewRenderer.render(req, resp, "/views/admin/product-list.jsp");
                break;
            case "/admin/product/add": showForm(req, resp, false); break;
            case "/admin/product/edit":
                Product product = productService.findById(Integer.parseInt(req.getParameter("id").trim()));
                if (product == null) { resp.sendError(404); return; }
                req.setAttribute("product", product);
                showForm(req, resp, true);
                break;
            default: resp.sendError(405);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if (path.endsWith("/delete")) {
            Product old = productService.findById(Integer.parseInt(req.getParameter("id").trim()));
            if (old == null) { resp.sendError(404); return; }
            try { productService.delete(old.getProductid()); }
            catch (RuntimeException e) {
                getServletContext().log("Không xóa được sản phẩm", e);
                req.getSession().setAttribute("formError", "Không xóa được sản phẩm. Vui lòng thử lại.");
                resp.sendRedirect(req.getContextPath() + "/admin/products");
                return;
            }
            cleanup(old.getImages());
            resp.sendRedirect(req.getContextPath() + "/admin/products?deleted=1");
            return;
        }
        boolean edit = path.endsWith("/update");
        if (!edit && !path.endsWith("/insert")) { resp.sendError(405); return; }
        Product product = edit ? productService.findById(Integer.parseInt(req.getParameter("productid").trim())) : new Product();
        if (product == null) { resp.sendError(404); return; }
        String oldImage = product.getImages();
        String uploaded = null;
        try {
            Category category = categoryService.findById(Integer.parseInt(req.getParameter("categoryid").trim()));
            if (category == null) throw new IllegalArgumentException("Danh mục không tồn tại.");
            product.setProductname(FormValidator.trim(req.getParameter("productname")));
            product.setDescription(FormValidator.trim(req.getParameter("description")));
            product.setPrice(new BigDecimal(req.getParameter("price").trim()));
            product.setQuantity(Integer.parseInt(req.getParameter("quantity").trim()));
            product.setStatus(Integer.parseInt(req.getParameter("status").trim()));
            product.setCategory(category);
            uploaded = images.save(req.getPart("images1"));
            String link = FormValidator.trim(req.getParameter("images"));
            product.setImages(uploaded != null ? uploaded : link.isEmpty() ? oldImage : link);
            if (edit) productService.update(product); else productService.insert(product);
        } catch (IllegalArgumentException e) {
            cleanup(uploaded);
            product.setImages(oldImage);
            req.setAttribute("product", product);
            req.setAttribute("error", e.getMessage());
            resp.setStatus(400);
            showForm(req, resp, edit);
            return;
        } catch (IOException | RuntimeException e) {
            cleanup(uploaded);
            getServletContext().log("Không lưu được sản phẩm", e);
            product.setImages(oldImage);
            req.setAttribute("product", product);
            req.setAttribute("error", "Chưa lưu được sản phẩm. Vui lòng thử lại.");
            resp.setStatus(500);
            showForm(req, resp, edit);
            return;
        }
        if (!java.util.Objects.equals(oldImage, product.getImages())) cleanup(oldImage);
        resp.sendRedirect(req.getContextPath() + "/admin/products?" + (edit ? "updated=1" : "created=1"));
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, boolean edit) throws ServletException, IOException {
        req.setAttribute("categories", categoryService.findAll());
        ViewRenderer.render(req, resp, "/views/admin/product-" + (edit ? "edit" : "add") + ".jsp");
    }

    private void cleanup(String filename) {
        try { images.delete(filename); }
        catch (IOException e) { getServletContext().log("Không xóa được ảnh cũ", e); }
    }
}
