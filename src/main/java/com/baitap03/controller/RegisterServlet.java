package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.baitap03.model.User;
import com.baitap03.service.IUserService;
import com.baitap03.service.EmailService;
import com.baitap03.service.UserServiceImpl;
import com.baitap03.util.Constants;
import com.baitap03.util.OtpUtil;

@WebServlet("/register")

public class RegisterServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public IUserService userService =
            new UserServiceImpl();

    private final EmailService emailService = new EmailService();

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );


    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        ViewRenderer.render(req, resp, "/views/register.jsp");
    }


    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String username =
                req.getParameter("username");

        String fullname =
                req.getParameter("fullname");

        String email =
                req.getParameter("email");

        String password =
                req.getParameter("password");

        String confirmPassword =
                req.getParameter(
                        "confirmPassword"
                );


        if (username == null
                || username.isBlank()
                || fullname == null
                || fullname.isBlank()
                || email == null
                || email.isBlank()
                || password == null
                || password.isBlank()) {

            req.setAttribute(
                    "error",
                    "Vui lòng nhập đầy đủ thông tin"
            );

            ViewRenderer.render(req, resp, "/views/register.jsp");

            return;
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            req.setAttribute("error", "Địa chỉ email không hợp lệ");
            ViewRenderer.render(req, resp, "/views/register.jsp");
            return;
        }

        if (username.trim().length() > 50
                || fullname.trim().length() > 100
                || email.trim().length() > 150) {
            req.setAttribute("error", "Thông tin đăng ký vượt quá độ dài cho phép");
            ViewRenderer.render(req, resp, "/views/register.jsp");
            return;
        }

        if (password.length() < 8 || password.length() > 128) {
            req.setAttribute("error", "Mật khẩu phải có từ 8 đến 128 ký tự");
            ViewRenderer.render(req, resp, "/views/register.jsp");
            return;
        }


        if (!password.equals(
                confirmPassword)) {

            req.setAttribute(
                    "error",
                    "Mật khẩu xác nhận không khớp"
            );

            ViewRenderer.render(req, resp, "/views/register.jsp");

            return;
        }


        User user = new User();

        user.setUsername(
                username.trim()
        );

        user.setFullname(
                fullname.trim()
        );

        user.setEmail(email.trim().toLowerCase(Locale.ROOT));

        user.setPassword(password);

        String otp = OtpUtil.generate();
        user.setActivationOtpHash(OtpUtil.hash(otp));
        user.setActivationOtpExpiresAt(
                LocalDateTime.now().plusMinutes(Constants.OTP_EXPIRE_MINUTES)
        );


        boolean result =
                userService.register(user);


        if (!result) {

            req.setAttribute(
                    "error",
                    "Username hoặc email đã tồn tại"
            );

            ViewRenderer.render(req, resp, "/views/register.jsp");

            return;
        }

        try {
            emailService.sendActivationOtp(user.getEmail(), user.getFullname(), otp);
        } catch (IllegalStateException e) {
            req.setAttribute(
                    "error",
                    "Tài khoản đã được tạo nhưng chưa gửi được OTP. Hãy kiểm tra cấu hình email và bấm gửi lại."
            );
            req.setAttribute("email", user.getEmail());
            ViewRenderer.render(req, resp, "/views/activate.jsp");
            return;
        }

        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/activate?sent=1&email=" + encodedEmail);
    }
}
