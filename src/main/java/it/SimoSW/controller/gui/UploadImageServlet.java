package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bean.ImageBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
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
public class UploadImageServlet extends HttpServlet {
    private EngineController engineController;

    private static final String UPLOAD_DIR = "uploads";

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
    }

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
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;

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
           6. COSTRUZIONE BEAN
           ========================= */
        EngineBean engineBean = new EngineBean();
        engineBean.setEngineCode(codiceMotore);
        engineBean.setCustomerId(Long.parseLong(cliente));
        engineBean.setNotes(note);
        engineBean.setStatus(EngineStatus.WAITING.name());
        engineBean.setIntakeDate(LocalDate.now().toString());

        List<ImageBean> imageBeans = new ArrayList<>();
        for (String filename : uploadedFiles) {
            ImageBean ib = new ImageBean();
            ib.setFilename(filename);
            imageBeans.add(ib);
        }

        EngineDetailBean detailBean = new EngineDetailBean();
        detailBean.setEngine(engineBean);
        detailBean.setImages(imageBeans);

        /* =========================
           7. APPLICATION LAYER
           ========================= */
        engineController.setEngineDetail(detailBean);

        /* =========================
           8. REDIRECT
           ========================= */
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
