package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet responsabile del caricamento delle immagini del motore
 * insieme ai metadati associati (cliente, codice motore, note).
 */
@WebServlet("/upload")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,     // 2 MB
        maxFileSize = 100 * 1024 * 1024,         // 100 MB per file
        maxRequestSize = 800 * 1024 * 1024       // 800 MB totali
)
public class UploadServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(UploadServlet.class.getName());
    private static final String SESSION_PENDING_NEW_ENGINE_REF = "pendingNewEngineRef";
    private EngineController engineController;
    private CustomerController customerController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
        this.customerController = initializer.getCustomerController();
        this.activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    /**
     * Mostra la pagina di caricamento immagini.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, "/upload")) {
            return;
        }

        HttpSession session = request.getSession();
        populateCustomerOptions(request);
        request.setAttribute("engineMode", "new");

        String pendingRef = (String) session.getAttribute(SESSION_PENDING_NEW_ENGINE_REF);
        if (pendingRef == null || pendingRef.isBlank()) {
            pendingRef = engineController.generateEngineRef();
            session.setAttribute(SESSION_PENDING_NEW_ENGINE_REF, pendingRef);
        }

        request.setAttribute("newEngineRef", pendingRef);
        request.setAttribute("engineRef", pendingRef);
        request.setAttribute("status", EngineStatus.WAITING.name());

        request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
    }

    /**
     * Gestisce il salvataggio di una o più immagini insieme ai metadati.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            doPostSafe(request, response);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio upload motore", ex);
            request.setAttribute("error", "Errore durante il salvataggio. Riprova.");
            request.setAttribute("engineMode", Optional.ofNullable(request.getParameter("engineMode")).orElse("new"));
            request.setAttribute("engineRef", Optional.ofNullable(request.getParameter("engineRef")).orElse(""));
            request.setAttribute("newEngineRef", Optional.ofNullable(request.getParameter("engineRef")).orElse(""));
            request.setAttribute("existingEngineRef", Optional.ofNullable(request.getParameter("engineRef")).orElse("?"));
            request.setAttribute("customer", request.getParameter("customer"));
            request.setAttribute("engineCode", request.getParameter("engineCode"));
            request.setAttribute("note", request.getParameter("note"));
            request.setAttribute("status", request.getParameter("status"));
            populateCustomerOptions(request);
            request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
        }
    }

    private void doPostSafe(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

    /* =========================
       1. LETTURA PARAMETRI BASE
       ========================= */
        String engineMode = request.getParameter("engineMode");
        if (engineMode == null || engineMode.isBlank()) {
            engineMode = "new";
        }
        String engineRef  = request.getParameter("engineRef");

        if ("new".equals(engineMode) && (engineRef == null || engineRef.isBlank())) {
            engineRef = (String) session.getAttribute(SESSION_PENDING_NEW_ENGINE_REF);
        }

        if ("new".equals(engineMode) && (engineRef == null || engineRef.isBlank())) {
            engineRef = engineController.generateEngineRef();
            session.setAttribute(SESSION_PENDING_NEW_ENGINE_REF, engineRef);
        }

        if (engineRef == null || engineRef.isBlank() || "?".equals(engineRef)) {
            request.setAttribute("error", "Riferimento motore non valido");
            request.setAttribute("engineMode", engineMode);
            request.setAttribute("engineRef", "");
            request.setAttribute("newEngineRef", "");
            request.setAttribute("existingEngineRef", "?");
            request.setAttribute("customer", request.getParameter("customer"));
            request.setAttribute("engineCode", request.getParameter("engineCode"));
            request.setAttribute("note", request.getParameter("note"));
            request.setAttribute("status", request.getParameter("status"));
            populateCustomerOptions(request);
            request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
            return;
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
                request.setAttribute("engineRef", engineRef);
                request.setAttribute("newEngineRef", engineRef);
                request.setAttribute("engineMode", "new");
                request.setAttribute("customer", request.getParameter("customer"));
                request.setAttribute("engineCode", codiceMotore);
                request.setAttribute("note", note);
                populateCustomerOptions(request);

                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }

            if (statusParam == null || statusParam.isBlank()) {
                request.setAttribute("error", "Seleziona uno stato di lavorazione");
                request.setAttribute("status", statusParam);
                request.setAttribute("engineRef", engineRef);
                request.setAttribute("newEngineRef", engineRef);
                request.setAttribute("engineMode", "new");
                request.setAttribute("customer", request.getParameter("customer"));
                request.setAttribute("engineCode", codiceMotore);
                request.setAttribute("note", note);
                populateCustomerOptions(request);

                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }

            try {
                status = EngineStatus.valueOf(statusParam.trim());
            } catch (IllegalArgumentException ex) {
                request.setAttribute("error", "Stato non valido: " + statusParam);
                request.setAttribute("status", statusParam);
                request.setAttribute("engineRef", engineRef);
                request.setAttribute("newEngineRef", engineRef);
                request.setAttribute("engineMode", "new");
                request.setAttribute("customer", request.getParameter("customer"));
                request.setAttribute("engineCode", codiceMotore);
                request.setAttribute("note", note);
                populateCustomerOptions(request);

                request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
                return;
            }
        }

    /* =========================
       4. UPLOAD IMMAGINI
       ========================= */
        List<PendingImage> pendingImages = new ArrayList<>();

        for (Part part : request.getParts()) {

            if (!"images".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }

            String contentType = part.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                continue;
            }

            String submittedName = part.getSubmittedFileName();
            if (submittedName == null || submittedName.isBlank()) {
                continue;
            }
            pendingImages.add(new PendingImage(part));
        }

        if (pendingImages.isEmpty() && "existing".equals(engineMode)) {
            request.setAttribute("error", "Devi caricare almeno un'immagine valida");
            request.setAttribute("engineRef", engineRef);
            request.setAttribute("newEngineRef", engineRef);
            request.setAttribute("engineMode", engineMode);
            request.setAttribute("customer", request.getParameter("customer"));
            request.setAttribute("engineCode", request.getParameter("engineCode"));
            request.setAttribute("note", request.getParameter("note"));
            request.setAttribute("status", request.getParameter("status"));
            populateCustomerOptions(request);

            request.getRequestDispatcher("/WEB-INF/views/image/upload.jsp").forward(request, response);
            return;
        }

    /* =========================
       5. APPLICATION LAYER
       ========================= */

        // 5a. Se nuovo motore → creazione
        if ("new".equals(engineMode)) {
            Long existingCustomerId = customerController.findCustomerIdByName(cliente);

            EngineBean engineBean = new EngineBean();
            engineBean.setEngineRef(engineRef);
            engineBean.setEngineCode(codiceMotore);
            Long customerId = customerController.findOrCreateCustomerId(cliente);
            engineBean.setCustomerId(customerId);
            engineBean.setNotes(note);
            engineBean.setStatus(status.name());
            engineBean.setIntakeDate(LocalDate.now().toString());

            try {
                engineController.createEngine(engineBean);
            } catch (IllegalStateException ex) {
                // Collisione rara: il riferimento mostrato era solo "provvisorio".
                // Rigenero e ritento una volta.
                String retryRef = engineController.generateEngineRef();
                engineBean.setEngineRef(retryRef);
                engineController.createEngine(engineBean);
                engineRef = retryRef;
            }
            session.removeAttribute(SESSION_PENDING_NEW_ENGINE_REF);

            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.CREATE,
                    UserActivityEntityType.MOTOR,
                    engineRef,
                    "aggiunta motore " + engineRef
            );
            if (existingCustomerId == null) {
                activityAuditLogger.logFromRequest(
                        request,
                        UserActivityActionType.CREATE,
                        UserActivityEntityType.CUSTOMER,
                        String.valueOf(customerId),
                        "aggiunta cliente " + cliente
                );
            }
        }

        // 5b. Immagini (opzionali per nuovo motore)
        if (!pendingImages.isEmpty()) {
            Path uploadRoot = UploadPathResolver.resolveUploadBase(getServletContext());
            Path engineDir = uploadRoot.resolve(engineRef);
            Files.createDirectories(engineDir);

            for (PendingImage pendingImage : pendingImages) {
                String storedFilename = ImageOptimizationUtil.storeOptimizedImage(pendingImage.part, engineDir);
                engineController.addImage(engineRef, storedFilename);
            }
        }

    /* =========================
       6. REDIRECT
       ========================= */
        PostSubmitNavigationGuard.blockFormPageOnce(
                request,
                "/upload",
                "/dashboard?lockBack=1&navHome=1"
        );
        response.sendRedirect(request.getContextPath() + "/dashboard?lockBack=1&navHome=1");
    }

    private void populateCustomerOptions(HttpServletRequest request) {
        request.setAttribute("customers", customerController.getAllCustomers());
    }

    private static final class PendingImage {
        private final Part part;

        private PendingImage(Part part) {
            this.part = part;
        }
    }
}
