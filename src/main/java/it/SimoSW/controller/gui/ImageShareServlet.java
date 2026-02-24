package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/image/share")
public class ImageShareServlet extends HttpServlet {

    private EngineController engineController;
    private static final String UPLOAD_DIR = "uploads/engines";

    @Override
    public void init() throws ServletException {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        if (initializer == null) {
            throw new ServletException("ApplicationInitializer non trovato nel contesto");
        }
        this.engineController = initializer.getEngineController();
        if (this.engineController == null) {
            throw new ServletException("EngineController non disponibile");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineRef = request.getParameter("engineRef");
        String indexStr = request.getParameter("imageIndex");

        System.out.println("[ImageShareServlet] Richiesta: engineRef=" + engineRef + ", imageIndex=" + indexStr);

        // Validazione parametri
        if (engineRef == null || engineRef.isBlank() || indexStr == null || indexStr.isBlank()) {
            System.err.println("[ImageShareServlet] Parametri mancanti");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametri mancanti");
            return;
        }

        if (engineRef.length() > 50) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "engineRef invalido");
            return;
        }

        if (!engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "engineRef invalido");
            return;
        }

        int imageIndex;
        try {
            imageIndex = Integer.parseInt(indexStr);
            if (imageIndex < 0) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "imageIndex invalido");
                return;
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "imageIndex non valido");
            return;
        }

        // Recupero filename dal controller
        var filenameOpt = engineController.getImageFilenameByEngineRefAndIndex(engineRef, imageIndex);

        if (filenameOpt.isEmpty()) {
            System.err.println("[ImageShareServlet] Immagine non trovata per: " + engineRef + " index " + imageIndex);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Immagine non trovata");
            return;
        }

        String filename = filenameOpt.get();
        System.out.println("[ImageShareServlet] Filename trovato: " + filename);

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Filename invalido");
            return;
        }

        try {
            Path uploadBase = UploadPathResolver.resolveUploadBase(getServletContext());
            Path imagePath = uploadBase.resolve(engineRef).resolve(filename).normalize();
            if (!imagePath.startsWith(uploadBase)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Percorso file non valido");
                return;
            }
            File imageFile = imagePath.toFile();

            System.out.println("[ImageShareServlet] Path assoluto: " + imagePath);
            System.out.println("[ImageShareServlet] File esiste: " + imageFile.exists());

            if (!imageFile.exists() || !imageFile.isFile()) {
                System.err.println("[ImageShareServlet] File non trovato: " + imagePath);
                serveFileViaResourceStream(response, engineRef, filename);
                return;
            }

            // Serve il file
            serveFile(response, imageFile, filename);

        } catch (IOException e) {
            System.err.println("[ImageShareServlet] Errore: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento");
        }
    }

    private void serveFile(HttpServletResponse response, File imageFile, String filename) throws IOException {
        String mimeType = Files.probeContentType(imageFile.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        response.setContentType(mimeType);
        response.setContentLength((int) imageFile.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (FileInputStream fis = new FileInputStream(imageFile);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            System.out.println("[ImageShareServlet] File servito con successo: " + filename);
        }
    }

    private void serveFileViaResourceStream(HttpServletResponse response, String engineRef, String filename) throws IOException {
        String resourcePath = "/" + UPLOAD_DIR + "/" + engineRef + "/" + filename;
        System.out.println("[ImageShareServlet] Usando getResourceAsStream: " + resourcePath);

        try (java.io.InputStream is = getServletContext().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[ImageShareServlet] Risorsa non trovata: " + resourcePath);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File non trovato");
                return;
            }

            String mimeType = getServletContext().getMimeType(filename);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                System.out.println("[ImageShareServlet] File servito via ResourceStream: " + filename);
            }
        }
    }
}
