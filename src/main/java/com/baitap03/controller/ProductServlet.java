package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import jakarta.servlet.ServletException;
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

@WebServlet(urlPatterns = {"/product", "/product/detail"})
public class ProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 6;
    private final IProductService productService = new ProductServiceImpl();
    private final ICategoryService categoryService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/product/detail".equals(req.getServletPath())) {
            showDetail(req, resp);
        } else {
            showList(req, resp);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Category> categories = categoryService.findAll().stream()
                .filter(category -> category.getStatus() == 1)
                .sorted(Comparator.comparing(
                        Category::getCategoryname,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        int requestedCategoryId = parsePositiveInt(
                req.getParameter("category"), 0);
        Category selectedCategory = categories.stream()
                .filter(category -> category.getCategoryid() == requestedCategoryId)
                .findFirst()
                .orElse(null);
        int selectedCategoryId = selectedCategory == null ? 0 : requestedCategoryId;

        long totalItems = selectedCategoryId > 0
                ? productService.countActiveByCategory(selectedCategoryId)
                : productService.countActive();
        int totalPages = (int) Math.ceil(totalItems / (double) PAGE_SIZE);
        int currentPage = parsePositiveInt(req.getParameter("page"), 1);
        if (totalPages > 0 && currentPage > totalPages) {
            currentPage = totalPages;
        }

        List<Product> products = selectedCategoryId > 0
                ? productService.findActivePageByCategory(
                        selectedCategoryId, currentPage - 1, PAGE_SIZE)
                : productService.findActivePage(currentPage - 1, PAGE_SIZE);

        req.setAttribute("products", products);
        req.setAttribute("categories", categories);
        req.setAttribute("selectedCategoryId", selectedCategoryId);
        req.setAttribute("selectedCategory", selectedCategory);
        req.setAttribute("currentPage", currentPage);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalItems", totalItems);
        ViewRenderer.render(req, resp, "/views/product-list.jsp");
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int productId = parsePositiveInt(req.getParameter("id"), -1);
        if (productId < 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã sản phẩm không hợp lệ");
            return;
        }
        Product product = productService.findActiveById(productId);
        if (product == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm");
            return;
        }
        req.setAttribute("product", product);
        ViewRenderer.render(req, resp, "/views/product-detail.jsp");
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
