package com.baitap03.util;

import java.math.BigDecimal;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Server-side rules shared by every form; never rely only on HTML attributes. */
public final class FormValidator {
    private FormValidator() { }

    public static Map<String, String> validate(String route, Function<String, String> values) {
        Map<String, String> errors = new LinkedHashMap<>();
        Function<String, String> text = key -> trim(values.apply(key));
        switch (route) {
            case "/login":
                required(errors, "identifier", text.apply("identifier"), 150, "Tên đăng nhập/email");
                String loginPassword = values.apply("password");
                if (loginPassword == null || loginPassword.isBlank() || loginPassword.length() > 128) {
                    errors.put("password", "Vui lòng nhập mật khẩu, tối đa 128 ký tự.");
                }
                break;
            case "/register":
                if (!text.apply("username").matches("[A-Za-z0-9_.-]{3,50}")) {
                    errors.put("username", "Tên đăng nhập gồm 3–50 ký tự: chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang.");
                }
                required(errors, "fullname", text.apply("fullname"), 100, "Họ tên");
                email(errors, text.apply("email"));
                password(errors, values);
                break;
            case "/forgot-password":
            case "/activate/resend":
                email(errors, text.apply("email"));
                break;
            case "/activate":
            case "/reset-password":
                email(errors, text.apply("email"));
                if (!text.apply("otp").matches("[0-9]{6}")) {
                    errors.put("otp", "OTP phải gồm đúng 6 chữ số.");
                }
                if (route.equals("/reset-password")) password(errors, values);
                break;
            case "/profile":
                required(errors, "fullname", text.apply("fullname"), 100, "Họ tên");
                if (!text.apply("phone").matches("\\+?[0-9]{9,15}")) {
                    errors.put("phone", "Số điện thoại gồm 9–15 chữ số, có thể bắt đầu bằng +.");
                }
                break;
            case "/admin/category/insert":
            case "/admin/category/update":
                required(errors, "categoryname", text.apply("categoryname"), 50, "Tên danh mục");
                status(errors, text.apply("status"));
                if (route.endsWith("update")) positiveInt(errors, "categoryid", text.apply("categoryid"));
                imageLink(errors, text.apply("images"));
                break;
            case "/admin/product/insert":
            case "/admin/product/update":
                required(errors, "productname", text.apply("productname"), 150, "Tên sản phẩm");
                if (text.apply("description").length() > 10000) errors.put("description", "Mô tả tối đa 10.000 ký tự.");
                String price = text.apply("price");
                if (!price.matches("[0-9]{1,16}(\\.[0-9]{1,2})?")
                        || new BigDecimal(price).compareTo(new BigDecimal("9999999999999999.99")) > 0) {
                    errors.put("price", "Giá phải không âm, tối đa 16 chữ số phần nguyên và 2 chữ số thập phân.");
                }
                integer(errors, "quantity", text.apply("quantity"), 0);
                positiveInt(errors, "categoryid", text.apply("categoryid"));
                status(errors, text.apply("status"));
                if (route.endsWith("update")) positiveInt(errors, "productid", text.apply("productid"));
                imageLink(errors, text.apply("images"));
                break;
            case "/admin/category/delete":
            case "/admin/product/delete":
            case "/admin/category/edit":
            case "/admin/product/edit":
                positiveInt(errors, "id", text.apply("id"));
                break;
            case "/admin/categories":
                if (text.apply("keyword").length() > 50) errors.put("keyword", "Từ khóa tối đa 50 ký tự.");
                break;
            case "/product":
                if (!text.apply("category").isEmpty()) integer(errors, "category", text.apply("category"), 0);
                if (!text.apply("page").isEmpty()) positiveInt(errors, "page", text.apply("page"));
                break;
            default:
                break;
        }
        return errors;
    }

    private static void required(Map<String, String> errors, String field, String value, int max, String label) {
        if (value.isEmpty() || value.length() > max) errors.put(field, label + " phải có từ 1 đến " + max + " ký tự.");
    }

    private static void email(Map<String, String> errors, String email) {
        if (email.length() > 150 || !email.matches("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")) {
            errors.put("email", "Email không hợp lệ hoặc dài quá 150 ký tự.");
        }
    }

    private static void password(Map<String, String> errors, Function<String, String> values) {
        String password = values.apply("password");
        if (password == null || password.isBlank() || password.length() < 8 || password.length() > 128) {
            errors.put("password", "Mật khẩu phải có từ 8 đến 128 ký tự và không chỉ gồm khoảng trắng.");
        }
        if (password == null || !password.equals(values.apply("confirmPassword"))) {
            errors.put("confirmPassword", "Mật khẩu xác nhận không khớp.");
        }
    }

    private static void positiveInt(Map<String, String> errors, String field, String value) {
        integer(errors, field, value, 1);
    }

    private static void integer(Map<String, String> errors, String field, String value, int min) {
        try {
            if (!value.matches("[0-9]{1,10}") || Integer.parseInt(value) < min) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            String label = switch (field) {
                case "quantity" -> "Số lượng";
                case "page" -> "Số trang";
                case "category", "categoryid" -> "Mã danh mục";
                case "productid" -> "Mã sản phẩm";
                default -> "Mã bản ghi";
            };
            errors.put(field, label + " phải là số nguyên từ " + min + " đến 2147483647.");
        }
    }

    private static void status(Map<String, String> errors, String status) {
        if (!status.matches("[01]")) errors.put("status", "Vui lòng chọn trạng thái hoạt động hoặc tạm khóa.");
    }

    private static void imageLink(Map<String, String> errors, String link) {
        if (link.isEmpty()) return;
        try {
            URI uri = URI.create(link);
            if (link.length() > 500 || uri.getHost() == null
                    || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                    || uri.getUserInfo() != null) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            errors.put("images", "Link ảnh phải là URL http:// hoặc https:// hợp lệ, tối đa 500 ký tự.");
        }
    }

    public static String trim(String value) { return value == null ? "" : value.trim(); }
}
