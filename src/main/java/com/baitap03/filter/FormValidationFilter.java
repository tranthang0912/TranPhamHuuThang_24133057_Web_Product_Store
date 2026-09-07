package com.baitap03.filter;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.baitap03.service.CategoryServiceImpl;
import com.baitap03.service.ProductServiceImpl;
import com.baitap03.util.AuthUtil;
import com.baitap03.util.FormValidator;

public class FormValidationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        if (path.startsWith("/views/") || path.startsWith("/common/")) {
            resp.sendError(404);
            return;
        }
        if ((path.startsWith("/admin/") || path.equals("/profile")) && !AuthUtil.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (req.getSession(false) != null && req.getSession().getAttribute("formError") != null) {
            req.setAttribute("error", req.getSession().getAttribute("formError"));
            req.getSession().removeAttribute("formError");
        }
        boolean post = "POST".equals(req.getMethod());
        boolean queryForm = path.equals("/product") || path.equals("/admin/categories") || path.endsWith("/edit");
        if (!post && !queryForm) {
            chain.doFilter(req, resp);
            return;
        }
        Map<String, String> errors;
        try {
            if (post && (path.equals("/profile") || path.matches("/admin/(category|product)/(insert|update)"))) {
                if (req.getContentType() == null || !req.getContentType().toLowerCase(java.util.Locale.ROOT).startsWith("multipart/form-data")) {
                    resp.sendError(400, "Biểu mẫu này yêu cầu multipart/form-data.");
                    return;
                }
                req.getParts();
            }
            errors = FormValidator.validate(path, req::getParameter);
        } catch (IllegalStateException | ServletException | IOException e) {
            req.getSession().setAttribute("formError", "Không đọc được tệp tải lên. Ảnh tối đa 5 MB; vui lòng chọn lại ảnh.");
            String target = path.equals("/profile") ? "/profile"
                    : path.startsWith("/admin/category/") ? "/admin/categories" : "/admin/products";
            resp.sendRedirect(req.getContextPath() + target);
            return;
        }
        if (errors.isEmpty()) {
            chain.doFilter(req, resp);
            return;
        }
        if ((path.endsWith("/update") && errors.containsKey(path.contains("/category/") ? "categoryid" : "productid"))) {
            resp.sendError(400, "Mã bản ghi không hợp lệ.");
            return;
        }
        resp.setStatus(400);
        req.setAttribute("fieldErrors", errors);
        String view;
        switch (path) {
            case "/activate/resend": view = "activate"; break;
            case "/admin/category/insert": view = "admin/category-add"; break;
            case "/admin/category/update":
                view = "admin/category-edit";
                if (!errors.containsKey("categoryid")) {
                    req.setAttribute("cate", new CategoryServiceImpl().findById(Integer.parseInt(req.getParameter("categoryid").trim())));
                }
                break;
            case "/admin/product/insert":
            case "/admin/product/update":
                view = path.endsWith("insert") ? "admin/product-add" : "admin/product-edit";
                req.setAttribute("categories", new CategoryServiceImpl().findAll());
                if (path.endsWith("update") && !errors.containsKey("productid")) {
                    req.setAttribute("product", new ProductServiceImpl().findById(Integer.parseInt(req.getParameter("productid").trim())));
                }
                break;
            case "/admin/categories": view = "admin/category-list"; break;
            case "/product": view = "product-list"; break;
            case "/profile":
                req.setAttribute("fullname", req.getParameter("fullname"));
                req.setAttribute("phone", req.getParameter("phone"));
                view = "profile";
                break;
            case "/login": case "/register": case "/activate": case "/forgot-password": case "/reset-password":
                view = path.substring(1);
                req.setAttribute("email", req.getParameter("email"));
                break;
            default:
                resp.sendError(400, errors.values().iterator().next());
                return;
        }
        ViewRenderer.render(req, resp, "/views/" + view + ".jsp");
    }
}
