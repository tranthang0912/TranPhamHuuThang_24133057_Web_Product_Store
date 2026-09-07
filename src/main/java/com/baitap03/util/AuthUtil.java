package com.baitap03.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.baitap03.model.User;

public class AuthUtil {

    public static boolean isLoggedIn(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        return session != null
                && session.getAttribute("account")
                != null;
    }


    public static User getCurrentUser(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {

            return null;
        }

        return (User)
                session.getAttribute(
                        "account"
                );
    }
}