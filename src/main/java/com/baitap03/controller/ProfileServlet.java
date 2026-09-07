package com.baitap03.controller;

import com.baitap03.util.ViewRenderer;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.baitap03.model.User;
import com.baitap03.service.IUserService;
import com.baitap03.service.UserServiceImpl;
import com.baitap03.util.AuthUtil;
import com.baitap03.util.ImageStorage;

@WebServlet("/profile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = ImageStorage.MAX_FILE_SIZE, maxRequestSize = 6L * 1024 * 1024)
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final IUserService userService;
    private final ImageStorage imageStorage;

    public ProfileServlet() {
        this(new UserServiceImpl(), new ImageStorage());
    }

    ProfileServlet(IUserService userService, ImageStorage imageStorage) {
        this.userService = userService;
        this.imageStorage = imageStorage;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User account = requireAccount(req, resp);
        if (account == null) {
            return;
        }
        User profile = userService.findById(account.getUserid());
        if (profile == null || !profile.isActive()) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getSession().setAttribute("account", profile);
        req.setAttribute("fullname", profile.getFullname());
        req.setAttribute("phone", profile.getPhone());
        Object success = req.getSession().getAttribute("profileSuccess");
        req.getSession().removeAttribute("profileSuccess");
        req.setAttribute("success", success);
        showForm(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User account = requireAccount(req, resp);
        if (account == null) {
            return;
        }
        req.setCharacterEncoding("UTF-8");
        req.setAttribute("fullname", account.getFullname());
        req.setAttribute("phone", account.getPhone());
        String uploadedImage = null;
        User updated;
        try {
            String contentType = req.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data")) {
                throw new IllegalArgumentException("Biểu mẫu phải được gửi bằng multipart/form-data.");
            }
            // Parse within the try block so oversized multipart requests show a usable error.
            try {
                req.getParts();
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException("Ảnh tối đa 5 MB, tổng biểu mẫu tối đa 6 MB.", e);
            }
            String token = (String) req.getSession().getAttribute("profileCsrfToken");
            if (token == null || !token.equals(req.getParameter("csrfToken"))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Phiên biểu mẫu không hợp lệ. Vui lòng tải lại trang.");
                return;
            }
            String fullname = req.getParameter("fullname");
            String phone = req.getParameter("phone");
            req.setAttribute("fullname", fullname);
            req.setAttribute("phone", phone);
            uploadedImage = imageStorage.save(req.getPart("images"));
            // The user ID is always taken from the authenticated session, never the form.
            updated = userService.updateProfile(account.getUserid(), fullname, phone, uploadedImage);
        } catch (IllegalArgumentException e) {
            deleteImage(uploadedImage);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", e.getMessage());
            showForm(req, resp);
            return;
        } catch (ServletException e) {
            deleteImage(uploadedImage);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", "Không đọc được biểu mẫu tải lên. Vui lòng chọn lại ảnh.");
            showForm(req, resp);
            return;
        } catch (IOException | RuntimeException e) {
            deleteImage(uploadedImage);
            getServletContext().log("Không cập nhật được profile", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("error", "Chưa lưu được hồ sơ. Vui lòng thử lại sau.");
            showForm(req, resp);
            return;
        }
        // Database has committed: refresh the session before issuing the redirect.
        req.getSession().setAttribute("account", updated);
        req.getSession().setAttribute("profileSuccess", "Cập nhật hồ sơ thành công.");
        if (uploadedImage != null && !uploadedImage.equals(account.getImages())) {
            deleteImage(account.getImages());
        }
        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    private User requireAccount(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Cache-Control", "no-store");
        User account = AuthUtil.getCurrentUser(req);
        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
        return account;
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (session.getAttribute("profileCsrfToken") == null) {
            session.setAttribute("profileCsrfToken", UUID.randomUUID().toString());
        }
        ViewRenderer.render(req, resp, "/views/profile.jsp");
    }

    private void deleteImage(String filename) {
        try {
            imageStorage.delete(filename);
        } catch (IOException e) {
            getServletContext().log("Không xóa được ảnh profile cũ: " + filename, e);
        }
    }
}
