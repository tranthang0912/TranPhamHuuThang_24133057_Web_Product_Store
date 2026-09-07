package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.baitap03.model.Category;
import com.baitap03.service.CategoryServiceImpl;
import com.baitap03.service.ICategoryService;
import com.baitap03.util.FormValidator;
import com.baitap03.util.ImageStorage;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = ImageStorage.MAX_FILE_SIZE,
        maxRequestSize = 6L * 1024 * 1024)
@WebServlet(urlPatterns = {"/admin/categories", "/admin/category/add", "/admin/category/insert",
        "/admin/category/edit", "/admin/category/update", "/admin/category/delete"})
public class CategoryController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ICategoryService cateService = new CategoryServiceImpl();
    private final ImageStorage images = new ImageStorage();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/admin/categories":
                String keyword = FormValidator.trim(req.getParameter("keyword"));
                req.setAttribute("listcate", keyword.isEmpty() ? cateService.findAll() : cateService.searchByName(keyword));
                show(req, resp, "category-list");
                break;
            case "/admin/category/add": show(req, resp, "category-add"); break;
            case "/admin/category/edit":
                Category category = cateService.findById(Integer.parseInt(req.getParameter("id").trim()));
                if (category == null) { resp.sendError(404); return; }
                req.setAttribute("cate", category);
                show(req, resp, "category-edit");
                break;
            default: resp.sendError(405);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if (path.endsWith("/delete")) {
            int id = Integer.parseInt(req.getParameter("id").trim());
            Category old = cateService.findById(id);
            if (old == null) { resp.sendError(404); return; }
            try {
                cateService.delete(id);
            } catch (Exception e) {
                getServletContext().log("Không xóa được danh mục", e);
                req.getSession().setAttribute("formError", "Không xóa được danh mục. Hãy chuyển hoặc xóa các sản phẩm thuộc danh mục trước.");
                resp.sendRedirect(req.getContextPath() + "/admin/categories");
                return;
            }
            cleanup(old.getImages());
            resp.sendRedirect(req.getContextPath() + "/admin/categories?deleted=1");
            return;
        }
        boolean edit = path.endsWith("/update");
        if (!edit && !path.endsWith("/insert")) { resp.sendError(405); return; }
        Category category = edit ? cateService.findById(Integer.parseInt(req.getParameter("categoryid").trim())) : new Category();
        if (category == null) { resp.sendError(404); return; }
        String oldImage = category.getImages();
        String uploaded = null;
        try {
            category.setCategoryname(FormValidator.trim(req.getParameter("categoryname")));
            category.setStatus(Integer.parseInt(req.getParameter("status").trim()));
            uploaded = images.save(req.getPart("images1"));
            String link = FormValidator.trim(req.getParameter("images"));
            category.setImages(uploaded != null ? uploaded : link.isEmpty() ? oldImage : link);
            if (edit) cateService.update(category); else cateService.insert(category);
        } catch (IllegalArgumentException e) {
            cleanup(uploaded);
            category.setImages(oldImage);
            req.setAttribute("cate", category);
            req.setAttribute("error", e.getMessage());
            resp.setStatus(400);
            show(req, resp, edit ? "category-edit" : "category-add");
            return;
        } catch (IOException | RuntimeException e) {
            cleanup(uploaded);
            getServletContext().log("Không lưu được danh mục", e);
            category.setImages(oldImage);
            req.setAttribute("cate", category);
            req.setAttribute("error", "Chưa lưu được danh mục. Vui lòng thử lại.");
            resp.setStatus(500);
            show(req, resp, edit ? "category-edit" : "category-add");
            return;
        }
        if (!java.util.Objects.equals(oldImage, category.getImages())) cleanup(oldImage);
        resp.sendRedirect(req.getContextPath() + "/admin/categories?" + (edit ? "updated=1" : "created=1"));
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, String view) throws ServletException, IOException {
        ViewRenderer.render(req, resp, "/views/admin/" + view + ".jsp");
    }

    private void cleanup(String filename) {
        try { images.delete(filename); }
        catch (IOException e) { getServletContext().log("Không xóa được ảnh cũ", e); }
    }
}
