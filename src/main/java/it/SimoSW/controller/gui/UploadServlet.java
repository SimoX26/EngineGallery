package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.UploadPathResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

/**
 * Servlet responsabile del caricamento delle immagini del motore
 * insieme ai metadati associati (cliente, codice motore, note).
 */
@WebServlet("/upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,   // 1 MB
        maxFileSize = 5 * 1024 * 1024,      // 5 MB per file
        maxRequestSize = 20 * 1024 * 1024   // 20 MB totali
)
public class UploadServlet extends HttpServlet {
    private EngineController engineController;
    private CustomerController customerController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
        this.customerController = initializer.getCustomerController();
    }

    /**
     * Mostra la pagina di caricamento immagini.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1) riferimento per NUOVO motore
        String newEngineRef = engineController.generateEngineRef();
        request.setAttribute("newEngineRef", newEngineRef);

        // 2) riferimento per motore ESISTENTE
        String selectedRef = request.getParameter("ref");
        String existingEngineRef = (selectedRef != null && !selectedRef.isBlank()) ? selectedRef : "?";
        request.setAttribute("existingEngineRef", existingEngineRef);

        // 3) modalità selezionata
        String engineMode = (selectedRef != null && !selectedRef.isBlank()) ? "existing" : "new";
        request.setAttribute("engineMode", engineMode);

        // 4) engineRef effettivo
        String engineRef;

        if ("existing".equals(engineMode)) {
            engineRef = existingEngineRef;

            // QUI RECUPERI I DATI DAL DB
            EngineDetailBean detail = engineController.getEngineDetail(engineRef);

            if (detail != null) {
                request.setAttribute("customer",
                        customerController.findNameById(detail.getEngine().getCustomerId()));

                request.setAttribute("engineCode",
                        detail.getEngine().getEngineCode());

                request.setAttribute("note",
                        detail.getEngine().getNotes());

                request.setAttribute("status",
                        detail.getEngine().getStatus());
            }

        } else {
            engineRef = newEngineRef;
            request.setAttribute("status", EngineStatus.WAITING.name());
        }

        request.setAttribute("engineRef", engineRef);

        request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
    }

    /**
     * Gestisce il salvataggio di una o più immagini insieme ai metadati.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    /* =========================
       1. LETTURA PARAMETRI BASE
       ========================= */
        String engineMode = request.getParameter("engineMode");
        String engineRef  = request.getParameter("engineRef");

        if (engineRef == null || engineRef.isBlank() || "?".equals(engineRef)) {
            throw new ServletException("Riferimento motore non valido");
        }

    /* =========================
       2. VALIDAZIONE METADATI
       (solo se nuovo motore)
       ========================= */
        String cliente = null;
        String codiceMotore = null;
        String note = null;
        String statusParam = null;
        EngineStatus status = null;

        if ("new".equals(engineMode)) {

            cliente = request.getParameter("customer");
            codiceMotore = request.getParameter("engineCode");
            note = request.getParameter("note");
            statusParam = request.getParameter("status");

            if (cliente == null || cliente.isBlank() || codiceMotore == null || codiceMotore.isBlank()) {

                request.setAttribute("error", "Nome cliente e codice motore sono obbligatori");
                request.setAttribute("status", statusParam);

                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }

            if (statusParam == null || statusParam.isBlank()) {
                request.setAttribute("error", "Seleziona uno stato di lavorazione");
                request.setAttribute("status", statusParam);

                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }

            try {
                status = EngineStatus.valueOf(statusParam.trim());
            } catch (IllegalArgumentException ex) {
                request.setAttribute("error", "Stato non valido: " + statusParam);
                request.setAttribute("status", statusParam);

                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }
        }

    /* =========================
       3. PREPARAZIONE DIRECTORY
       ========================= */
        Path uploadRoot = UploadPathResolver.resolveUploadBase(getServletContext());
        Path engineDir = uploadRoot.resolve(engineRef);
        Files.createDirectories(engineDir);

    /* =========================
       4. UPLOAD IMMAGINI
       ========================= */
        List<String> uploadedFiles = new ArrayList<>();

        for (Part part : request.getParts()) {

            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }

            String contentType = part.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                continue;
            }

            String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();

            String safeFileName = UUID.randomUUID() + "_" + originalName;

            Path destination = engineDir.resolve(safeFileName);
            part.write(destination.toString());

            uploadedFiles.add(safeFileName);
        }

        if (uploadedFiles.isEmpty()) {
            request.setAttribute("error", "Devi caricare almeno un'immagine valida");

            request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
            return;
        }

    /* =========================
       5. APPLICATION LAYER
       ========================= */

        // 5a. Se nuovo motore → creazione
        if ("new".equals(engineMode)) {

            EngineBean engineBean = new EngineBean();
            engineBean.setEngineRef(engineRef);
            engineBean.setEngineCode(codiceMotore);
            Long customerId = customerController.findOrCreateCustomerId(cliente);
            engineBean.setCustomerId(customerId);
            engineBean.setNotes(note);
            engineBean.setStatus(status.name());
            engineBean.setIntakeDate(LocalDate.now().toString());

            engineController.createEngine(engineBean);
        }

        // 5b. Immagini (sempre)
        for (String filename : uploadedFiles) {
            engineController.addImage(engineRef, filename);
        }

    /* =========================
       6. REDIRECT
       ========================= */
        response.sendRedirect( request.getContextPath()  + "/dashboard");
    }
}
