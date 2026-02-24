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

@WebServlet("/uploads/engines/*")
public class UploadImageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String trimmed = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        int slashIndex = trimmed.indexOf('/');
        if (slashIndex <= 0 || slashIndex == trimmed.length() - 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Percorso immagine non valido");
            return;
        }

        String engineRef = trimmed.substring(0, slashIndex);
        String filename = trimmed.substring(slashIndex + 1);

        if (!engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "engineRef invalido");
            return;
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Filename invalido");
            return;
        }

        Path uploadBase = UploadPathResolver.resolveUploadBase(getServletContext());
        Path imagePath = uploadBase.resolve(engineRef).resolve(filename).normalize();
        if (!imagePath.startsWith(uploadBase)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Percorso file non valido");
            return;
        }

        File imageFile = imagePath.toFile();
        if (!imageFile.exists() || !imageFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = Files.probeContentType(imageFile.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);
        response.setContentLengthLong(imageFile.length());
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (FileInputStream fis = new FileInputStream(imageFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }
}
