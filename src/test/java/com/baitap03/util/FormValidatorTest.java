package com.baitap03.util;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

class FormValidatorTest {
    static Stream<Arguments> invalidForms() {
        return Stream.of(
            Arguments.of("/login", Map.of("identifier", " ", "password", ""), "identifier"),
            Arguments.of("/register", Map.of("username", "<script>"), "username"),
            Arguments.of("/register", Map.of("password", "abcdefgh", "confirmPassword", "different"), "confirmPassword"),
            Arguments.of("/forgot-password", Map.of("email", "bad@"), "email"),
            Arguments.of("/activate/resend", Map.of("email", "bad"), "email"),
            Arguments.of("/activate", Map.of("email", "a@b.com", "otp", "12345"), "otp"),
            Arguments.of("/reset-password", Map.of("email", "a@b.com", "otp", "123456", "password", "        "), "password"),
            Arguments.of("/profile", Map.of("fullname", " ", "phone", "abc"), "fullname"),
            Arguments.of("/profile", Map.of("fullname", "An", "phone", "0912 345 678"), "phone"),
            Arguments.of("/admin/category/insert", Map.of("categoryname", "x".repeat(51), "status", "3"), "categoryname"),
            Arguments.of("/admin/category/update", Map.of("categoryid", "-1"), "categoryid"),
            Arguments.of("/admin/product/insert", Map.of("price", "1e9"), "price"),
            Arguments.of("/admin/product/insert", Map.of("price", "1.001"), "price"),
            Arguments.of("/admin/product/insert", Map.of("price", "10000000000000000"), "price"),
            Arguments.of("/admin/product/update", Map.of("quantity", "2147483648"), "quantity"),
            Arguments.of("/admin/product/update", Map.of("quantity", "1.5"), "quantity"),
            Arguments.of("/admin/product/update", Map.of("images", "javascript:alert(1)"), "images"),
            Arguments.of("/admin/category/delete", Map.of("id", "abc"), "id"),
            Arguments.of("/admin/product/delete", Map.of("id", "0"), "id"),
            Arguments.of("/admin/categories", Map.of("keyword", "x".repeat(51)), "keyword"),
            Arguments.of("/product", Map.of("category", "bad"), "category")
        );
    }

    @ParameterizedTest @MethodSource("invalidForms")
    void rejectsMalformedFormValues(String route, Map<String, String> values, String field) {
        assertTrue(FormValidator.validate(route, values::get).containsKey(field));
    }

    @Test void acceptsValidBoundaryValues() {
        Map<String, String> product = new HashMap<>(Map.of("productname", "Máy tính", "price", "9999999999999999.99",
                "quantity", "2147483647", "categoryid", "1", "status", "0", "images", "https://example.com/a.png"));
        assertTrue(FormValidator.validate("/admin/product/insert", product::get).isEmpty());
        Map<String, String> profile = Map.of("fullname", " Nguyễn Văn An ", "phone", "+84912345678");
        assertTrue(FormValidator.validate("/profile", profile::get).isEmpty());
    }
}
