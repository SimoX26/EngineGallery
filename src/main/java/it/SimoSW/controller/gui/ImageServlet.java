package it.SimoSW.controller.gui;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Servlet responsabile del caricamento delle immagini del motore
 * insieme ai metadati associati (cliente, codice motore, note).
 */
@WebServlet("/image-upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,   // 1 MB
        maxFileSize = 5 * 1024 * 1024,      // 5 MB per file
        maxRequestSize = 20 * 1024 * 1024   // 20 MB totali
)
public class ImageServlet extends HttpServlet {

    private static final String TEMP_UPLOAD_DIR = "temp_uploads";

    /**
     * Mostra la pagina di caricamento immagini.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/views/image/image-upload.jsp").forward(request, response);
    }

    /**
     * Gestisce il salvataggio di una o più immagini insieme ai metadati.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        /* =========================
           1. LETTURA METADATI
           ========================= */
        String cliente = request.getParameter("customer");
        String codiceMotore = request.getParameter("engineCode");
        String note = request.getParameter("note");

        /* =========================
           2. VALIDAZIONE METADATI
           ========================= */
        if (cliente == null || cliente.isBlank() || codiceMotore == null || codiceMotore.isBlank()) {
            request.setAttribute("error", "Nome cliente e codice motore sono obbligatori");
            request.getRequestDispatcher("/WEB-INF/views/image/image-upload.jsp").forward(request, response);
            return;
        }

        /* =========================
           3. PREPARAZIONE DIRECTORY
           ========================= */
        String uploadPath = getServletContext().getRealPath("") + File.separator + TEMP_UPLOAD_DIR;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        /* =========================
           4. GESTIONE MULTI-UPLOAD
           ========================= */
        List<String> uploadedFiles = new ArrayList<>();

        Collection<Part> parts = request.getParts();

        for (Part part : parts) {

            // Considera solo il campo file "images"
            if (!"images".equals(part.getName())) {
                continue;
            }

            // File vuoto
            if (part.getSize() == 0) {
                continue;
            }

            // Validazione MIME
            String contentType = part.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                continue;
            }

            // Nome file originale
            String originalFileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();

            // Nome sicuro
            String safeFileName = UUID.randomUUID() + "_" + originalFileName;

            // Salvataggio file
            File savedFile = new File(uploadDir, safeFileName);
            part.write(savedFile.getAbsolutePath());

            uploadedFiles.add(safeFileName);
        }

        /* =========================
           5. VERIFICA RISULTATO
           ========================= */
        if (uploadedFiles.isEmpty()) {
            request.setAttribute("error", "Devi caricare almeno un'immagine valida");
            request.getRequestDispatcher("/WEB-INF/views/image/image-upload.jsp").forward(request, response);
            return;
        }

        /* =========================
           6. PASSAGGIO DATI ALLA VIEW
           ========================= */
        request.setAttribute("customer", cliente);
        request.setAttribute("engineCode", codiceMotore);
        request.setAttribute("note", note);
        request.setAttribute("uploadedImages", uploadedFiles);

        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isBlank()) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }

    }
}
