package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@WebServlet("/engine/edit/images")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 100 * 1024 * 1024,
        maxRequestSize = 800 * 1024 * 1024
)
public class EngineImageEditServlet extends HttpServlet {

    private EngineController engineController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String engineRef = safeTrim(request.getParameter("engineRef"));
        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        try {
            applyImageDeletions(request, engineRef);
            applyImageAdditions(request, engineRef);
            response.sendRedirect(request.getContextPath() + "/engine/edit?ref=" + engineRef + "&imagesUpdated=1&lockBack=1");
        } catch (IllegalArgumentException ex) {
            response.sendRedirect(request.getContextPath() + "/engine/edit?ref=" + engineRef + "&imageError=1");
        } catch (RuntimeException ex) {
            response.sendRedirect(request.getContextPath() + "/engine/edit?ref=" + engineRef + "&imageError=1");
        }
    }

    private void applyImageDeletions(HttpServletRequest request, String engineRef) throws IOException, ServletException {
        String[] requestedDeletions = request.getParameterValues("deleteFilenames");
        if (requestedDeletions == null || requestedDeletions.length == 0) {
            return;
        }

        Set<String> uniqueFilenames = new LinkedHashSet<>();
        for (String raw : requestedDeletions) {
            String filename = safeTrim(raw);
            if (filename == null || filename.isBlank()) {
                continue;
            }
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                continue;
            }
            uniqueFilenames.add(filename);
        }

        if (uniqueFilenames.isEmpty()) {
            return;
        }

        Path uploadBase = UploadPathResolver.resolveUploadBase(getServletContext());
        Path engineDir = uploadBase.resolve(engineRef).normalize();

        for (String filename : uniqueFilenames) {
            boolean deleted = engineController.deleteImageByFilename(engineRef, filename);
            if (!deleted) {
                continue;
            }
            Path imagePath = engineDir.resolve(filename).normalize();
            if (imagePath.startsWith(uploadBase)) {
                Files.deleteIfExists(imagePath);
            }
        }
    }

    private void applyImageAdditions(HttpServletRequest request, String engineRef) throws IOException, ServletException {
        List<Part> validImageParts = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (!"newImages".equals(part.getName()) || part.getSize() <= 0) {
                continue;
            }
            String contentType = part.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                continue;
            }
            validImageParts.add(part);
        }

        if (validImageParts.isEmpty()) {
            return;
        }

        Path uploadBase = UploadPathResolver.resolveUploadBase(getServletContext());
        Path engineDir = uploadBase.resolve(engineRef).normalize();
        if (!engineDir.startsWith(uploadBase)) {
            throw new IllegalArgumentException("Percorso non valido");
        }
        Files.createDirectories(engineDir);

        for (Part part : validImageParts) {
            String submittedName = part.getSubmittedFileName();
            String originalName = submittedName == null ? "image.jpg" : Paths.get(submittedName).getFileName().toString();
            String sanitized = sanitizeFilename(originalName);
            String storedFilename = UUID.randomUUID() + "_" + sanitized;

            Path destination = engineDir.resolve(storedFilename).normalize();
            if (!destination.startsWith(engineDir)) {
                continue;
            }

            part.write(destination.toString());
            engineController.addImage(engineRef, storedFilename);
        }
    }

    private static boolean isValidEngineRef(String engineRef) {
        return engineRef != null
                && !engineRef.isBlank()
                && engineRef.length() <= 50
                && engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$");
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private static String sanitizeFilename(String filename) {
        String value = (filename == null || filename.isBlank()) ? "image.jpg" : filename;
        String normalized = Paths.get(value).getFileName().toString().replace(' ', '_');
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        String sanitized = builder.toString();
        if (sanitized.isBlank()) {
            return "image.jpg";
        }

        String lower = sanitized.toLowerCase(Locale.ROOT);
        if (!lower.contains(".")) {
            return sanitized + ".jpg";
        }
        return sanitized;
    }
}
