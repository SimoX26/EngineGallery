package it.SimoSW.controller.gui;

import it.SimoSW.util.UploadPathResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/uploads/hydraulic/*")
public class UploadHydraulicVideoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Filename invalido");
            return;
        }

        Path uploadBase = UploadPathResolver.resolveHydraulicUploadBase(getServletContext());
        Path videoPath = uploadBase.resolve(filename).normalize();
        if (!videoPath.startsWith(uploadBase)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Percorso file non valido");
            return;
        }

        File videoFile = videoPath.toFile();
        if (!videoFile.exists() || !videoFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = Files.probeContentType(videoFile.toPath());
        if (mimeType == null || !mimeType.startsWith("video/")) {
            mimeType = "video/mp4";
        }

        response.setContentType(mimeType);
        response.setContentLengthLong(videoFile.length());
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (FileInputStream fis = new FileInputStream(videoFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }
}
