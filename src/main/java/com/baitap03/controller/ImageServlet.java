package com.baitap03.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.baitap03.util.Constants;

@WebServlet("/image")

public class ImageServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;


    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws IOException {

        String fname =
                req.getParameter("fname");


        if (fname == null || !fname.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,254}")) {

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }


        fname =
                Paths.get(fname)
                        .getFileName()
                        .toString();


        Path path =
                Paths.get(
                        Constants.UPLOAD_DIRECTORY,
                        fname
                );


        if (!Files.isRegularFile(path, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }


        String contentType =
                Files.probeContentType(
                        path
                );


        if (contentType != null) {

            resp.setContentType(
                    contentType
            );
        }
        resp.setHeader("X-Content-Type-Options", "nosniff");


        Files.copy(
                path,
                resp.getOutputStream()
        );
    }
}
