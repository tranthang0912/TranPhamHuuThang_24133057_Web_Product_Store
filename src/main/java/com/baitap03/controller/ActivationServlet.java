package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;

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

@WebServlet(urlPatterns = {"/activate", "/activate/resend"})
public class ActivationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final IUserService userService = new UserServiceImpl();
    private final EmailService emailService = new EmailService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("email", req.getParameter("email"));
        ViewRenderer.render(req, resp, "/views/activate.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if ("/activate/resend".equals(req.getServletPath())) {
            resend(req, resp);
            return;
        }

        String email = trim(req.getParameter("email"));
        String otp = trim(req.getParameter("otp"));
        req.setAttribute("email", email);

        if (email.isBlank() || !otp.matches("\\d{6}")) {
            req.setAttribute("error", "Vui lòng nhập email và mã OTP gồm 6 chữ số");
            ViewRenderer.render(req, resp, "/views/activate.jsp");
            return;
        }

        if (!userService.activateAccount(email, otp)) {
            req.setAttribute("error", "OTP không đúng, đã hết hạn hoặc tài khoản đã kích hoạt");
            ViewRenderer.render(req, resp, "/views/activate.jsp");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login?activated=success");
    }

    private void resend(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = trim(req.getParameter("email"));
        req.setAttribute("email", email);
        User user = userService.findByEmail(email);
        String otp = OtpUtil.generate();

        if (user == null || user.isActive() || !userService.prepareActivationOtp(email, otp)) {
            req.setAttribute("error", "Không tìm thấy tài khoản đang chờ kích hoạt");
            ViewRenderer.render(req, resp, "/views/activate.jsp");
            return;
        }

        try {
            emailService.sendActivationOtp(user.getEmail(), user.getFullname(), otp);
            req.setAttribute("message", "Đã gửi một mã OTP mới tới email của bạn");
        } catch (IllegalStateException e) {
            req.setAttribute("error", "Không gửi được OTP. Vui lòng kiểm tra cấu hình SMTP");
        }
        ViewRenderer.render(req, resp, "/views/activate.jsp");
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
