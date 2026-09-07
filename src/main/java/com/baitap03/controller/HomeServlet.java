package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.baitap03.service.IProductService;
import com.baitap03.service.ProductServiceImpl;

@WebServlet(urlPatterns = {"", "/home"})

public class HomeServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final IProductService productService = new ProductServiceImpl();


    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("latestProducts", productService.findLatestActive(10));
        ViewRenderer.render(req, resp, "/views/home.jsp");
    }
}
