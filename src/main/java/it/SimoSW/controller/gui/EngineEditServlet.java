package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;

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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/engine/edit")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 100 * 1024 * 1024,
        maxRequestSize = 800 * 1024 * 1024
)
public class EngineEditServlet extends HttpServlet {

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String engineRef = request.getParameter("ref");

        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }
        String formPath = "/engine/edit?ref=" + engineRef;
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, formPath)) {
            return;
        }

        EngineDetailBean detail = engineController.getEngineDetail(engineRef);
        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        bindFormData(request,
                detail.getEngine().getEngineRef(),
                detail.getEngine().getCustomerName(),
                detail.getEngine().getEngineCode(),
                detail.getEngine().getStatus(),
                detail.getEngine().getIntakeDate(),
                detail.getEngine().getDeliveryDate(),
                detail.getEngine().getNotes());
        request.setAttribute("engineImages", detail.getImages());

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String engineRef = safeTrim(request.getParameter("ref"));
        String customerName = safeTrim(request.getParameter("customer"));
        String engineCode = safeTrim(request.getParameter("engineCode"));
        String statusParam = safeTrim(request.getParameter("status"));
        String intakeDate = safeTrim(request.getParameter("intakeDate"));
        String deliveryDate = safeTrim(request.getParameter("deliveryDate"));
        String note = safeTrim(request.getParameter("note"));

        bindFormData(request, engineRef, customerName, engineCode, statusParam, intakeDate, deliveryDate, note);
        bindImagesForEdit(request, engineRef);

        if (!isValidEngineRef(engineRef)) {
            request.setAttribute("error", "Riferimento motore non valido");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        if (customerName == null || customerName.isBlank() || engineCode == null || engineCode.isBlank()) {
            request.setAttribute("error", "Nome cliente e codice motore sono obbligatori");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        if (statusParam == null || statusParam.isBlank()) {
            request.setAttribute("error", "Seleziona uno stato di lavorazione");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        if (intakeDate == null || intakeDate.isBlank()) {
            request.setAttribute("error", "La data ingresso è obbligatoria");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        EngineStatus status;
        try {
            status = EngineStatus.valueOf(statusParam);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Stato non valido: " + statusParam);
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        LocalDate parsedIntakeDate;
        try {
            parsedIntakeDate = LocalDate.parse(intakeDate);
        } catch (Exception ex) {
            request.setAttribute("error", "Data ingresso non valida");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        String normalizedDeliveryDate = null;
        if (status == EngineStatus.DELIVERED) {
            if (deliveryDate == null || deliveryDate.isBlank()) {
                normalizedDeliveryDate = LocalDate.now().toString();
            } else {
                try {
                    normalizedDeliveryDate = LocalDate.parse(deliveryDate).toString();
                } catch (Exception ex) {
                    request.setAttribute("error", "Data consegna non valida");
                    request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
                    return;
                }
            }
        }

        try {
            Long existingCustomerId = customerController.findCustomerIdByName(customerName);
            Long customerId = customerController.findOrCreateCustomerId(customerName);

            EngineBean bean = new EngineBean();
            bean.setEngineRef(engineRef);
            bean.setEngineCode(engineCode);
            bean.setCustomerId(customerId);
            bean.setStatus(status.name());
            bean.setIntakeDate(parsedIntakeDate.toString());
            bean.setDeliveryDate(normalizedDeliveryDate);
            bean.setNotes(note);

            engineController.updateEngine(bean);
            applyImageDeletions(request, engineRef);
            applyImageAdditions(request, engineRef);
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.UPDATE,
                    UserActivityEntityType.MOTOR,
                    engineRef,
                    "modifica motore " + engineRef
            );
            if (existingCustomerId == null) {
                activityAuditLogger.logFromRequest(
                        request,
                        UserActivityActionType.CREATE,
                        UserActivityEntityType.CUSTOMER,
                        String.valueOf(customerId),
                        "aggiunta cliente " + customerName
                );
            }
            String formPath = "/engine/edit?ref=" + engineRef;
            String fallbackPath = "/engine/detail?ref=" + engineRef + "&updated=1&lockBack=1&navHome=1";
            PostSubmitNavigationGuard.blockFormPageOnce(request, formPath, fallbackPath);
            response.sendRedirect(request.getContextPath() + fallbackPath);
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
        } catch (RuntimeException e) {
            request.setAttribute("error", "Errore durante il salvataggio delle modifiche");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
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

    private static void bindFormData(HttpServletRequest request,
                                     String engineRef,
                                     String customer,
                                     String engineCode,
                                     String status,
                                     String intakeDate,
                                     String deliveryDate,
                                     String note) {
        request.setAttribute("engineRef", engineRef);
        request.setAttribute("customer", customer);
        request.setAttribute("engineCode", engineCode);
        request.setAttribute("status", status);
        request.setAttribute("intakeDate", intakeDate);
        request.setAttribute("deliveryDate", deliveryDate);
        request.setAttribute("note", note);
    }

    private void bindImagesForEdit(HttpServletRequest request, String engineRef) {
        if (!isValidEngineRef(engineRef)) {
            return;
        }
        EngineDetailBean detail = engineController.getEngineDetail(engineRef);
        if (detail != null) {
            request.setAttribute("engineImages", detail.getImages());
        }
    }

    private void applyImageDeletions(HttpServletRequest request, String engineRef) {
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
                try {
                    Files.deleteIfExists(imagePath);
                } catch (IOException ignored) {
                }
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
            String storedFilename = ImageOptimizationUtil.storeOptimizedImage(part, engineDir);
            engineController.addImage(engineRef, storedFilename);
        }
    }
}
