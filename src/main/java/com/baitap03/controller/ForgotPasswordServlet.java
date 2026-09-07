package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.baitap03.model.User;
import com.baitap03.service.EmailService;
import com.baitap03.service.IUserService;
import com.baitap03.service.UserServiceImpl;
import com.baitap03.util.OtpUtil;

@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final IUserService userService = new UserServiceImpl();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ViewRenderer.render(req, resp, "/views/forgot-password.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email");
        email = email == null ? "" : email.trim();

        if (email.isBlank()) {
            req.setAttribute("error", "Vui lòng nhập email");
            ViewRenderer.render(req, resp, "/views/forgot-password.jsp");
            return;
        }

        User user = userService.findByEmail(email);
        String otp = OtpUtil.generate();
        if (user != null && userService.preparePasswordResetOtp(email, otp)) {
            try {
                emailService.sendPasswordResetOtp(user.getEmail(), user.getFullname(), otp);
            } catch (IllegalStateException e) {
                req.setAttribute("error", "Không gửi được OTP. Vui lòng kiểm tra cấu hình SMTP");
                req.setAttribute("email", email);
                ViewRenderer.render(req, resp, "/views/forgot-password.jsp");
                return;
            }
        }

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/reset-password?sent=1&email=" + encodedEmail);
    }
}
