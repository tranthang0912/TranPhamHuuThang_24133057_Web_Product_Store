package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.baitap03.service.IUserService;
import com.baitap03.service.UserServiceImpl;

@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final IUserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("email", req.getParameter("email"));
        ViewRenderer.render(req, resp, "/views/reset-password.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = trim(req.getParameter("email"));
        String otp = trim(req.getParameter("otp"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        req.setAttribute("email", email);

        if (email.isBlank() || !otp.matches("\\d{6}") || password == null
                || password.length() < 8 || password.length() > 128) {
            req.setAttribute("error", "Nhập đủ email, OTP 6 số và mật khẩu từ 8 đến 128 ký tự");
            ViewRenderer.render(req, resp, "/views/reset-password.jsp");
            return;
        }
        if (!password.equals(confirmPassword)) {
            req.setAttribute("error", "Mật khẩu xác nhận không khớp");
            ViewRenderer.render(req, resp, "/views/reset-password.jsp");
            return;
        }
        if (!userService.resetPassword(email, otp, password)) {
            req.setAttribute("error", "OTP không đúng hoặc đã hết hạn");
            ViewRenderer.render(req, resp, "/views/reset-password.jsp");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login?reset=success");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
