package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.baitap03.model.User;
import com.baitap03.service.IUserService;
import com.baitap03.service.UserServiceImpl;

@WebServlet("/login")

public class LoginServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public IUserService userService =
            new UserServiceImpl();


    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        ViewRenderer.render(req, resp, "/views/login.jsp");
    }


    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String identifier =
                req.getParameter("identifier");

        String password =
                req.getParameter("password");


        User user =
                userService.login(
                        identifier,
                        password
                );


        if (user == null) {

            User existing = userService.findByIdentifier(identifier);

            if (existing != null && !existing.isActive()) {
                req.setAttribute("inactiveEmail", existing.getEmail());
                req.setAttribute("error", "Tài khoản chưa được kích hoạt");
                ViewRenderer.render(req, resp, "/views/login.jsp");
                return;
            }

            req.setAttribute(
                    "error",
                    "Sai tên đăng nhập/email hoặc mật khẩu"
            );

            ViewRenderer.render(req, resp, "/views/login.jsp");

            return;
        }


        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = req.getSession(true);

        session.setAttribute(
                "account",
                user
        );


        resp.sendRedirect(
                req.getContextPath()
                + "/home"
        );
    }
}
