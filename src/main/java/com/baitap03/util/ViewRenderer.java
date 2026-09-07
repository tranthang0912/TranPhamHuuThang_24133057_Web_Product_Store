package com.baitap03.util;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class ViewRenderer {
    private ViewRenderer() { }

    public static void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        // An included JSP cannot change headers; set the HTML content type first.
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher(view).include(request, response);
    }
}
