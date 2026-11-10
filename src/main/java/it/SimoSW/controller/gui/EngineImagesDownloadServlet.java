package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bean.ImageBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/engine/images/download")
public class EngineImagesDownloadServlet extends HttpServlet {
    private EngineController engineController;

    @Override
    public void init() throws ServletException {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        if (initializer == null || initializer.getEngineController() == null) {
            throw new ServletException("EngineController non disponibile");
        }
        engineController = initializer.getEngineController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String engineRef = request.getParameter("ref");
        if (engineRef == null || !engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Riferimento motore non valido");
            return;
        }

        EngineDetailBean detail = engineController.getEngineDetail(engineRef);
        if (detail == null || detail.getImages() == null || detail.getImages().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Foto non disponibili");
            return;
        }

        Path uploadBase = UploadPathResolver.resolveUploadBase(getServletContext());
        Path engineDir = uploadBase.resolve(engineRef);
        if (!Files.isDirectory(engineDir) || !engineDir.toRealPath().startsWith(uploadBase.toRealPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Foto non disponibili");
            return;
        }
        List<Path> imagePaths = new ArrayList<>();
        for (ImageBean image : detail.getImages()) {
            String filename = image.getFilename();
            if (filename == null || filename.isBlank() || filename.contains("..")
                    || filename.contains("/") || filename.contains("\\")
                    || filename.indexOf('\r') >= 0 || filename.indexOf('\n') >= 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Nome foto non valido");
                return;
            }
            Path imagePath = engineDir.resolve(filename).normalize();
            if (!Files.isRegularFile(imagePath)
                    || !imagePath.toRealPath().startsWith(engineDir.toRealPath())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Una foto non è disponibile");
                return;
            }
            imagePaths.add(imagePath);
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"foto-motore-" + engineRef + ".zip\"");
        response.setHeader("Cache-Control", "private, no-store");
        writeArchive(imagePaths, detail.getImages(), response.getOutputStream());
    }

    static void writeArchive(List<Path> imagePaths, List<ImageBean> images, OutputStream output) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            zip.setLevel(Deflater.NO_COMPRESSION);
            for (int i = 0; i < imagePaths.size(); i++) {
                String entryName = String.format("%02d-%s", i + 1, images.get(i).getFilename());
                zip.putNextEntry(new ZipEntry(entryName));
                Files.copy(imagePaths.get(i), zip);
                zip.closeEntry();
            }
        }
    }
}
