package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;

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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@WebServlet("/warehouse/edit")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 100 * 1024 * 1024,
        maxRequestSize = 800 * 1024 * 1024
)
public class WarehouseEditServlet extends HttpServlet {

    private WarehouseController warehouseController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.warehouseController = initializer.getWarehouseController();
        this.activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long itemId = parseId(request.getParameter("id"));
        if (itemId == null) {
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
            return;
        }
        String formPath = "/warehouse/edit?id=" + itemId;
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, formPath)) {
            return;
        }

        Optional<WarehouseItem> itemOpt = warehouseController.findById(itemId);
        if (itemOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
            return;
        }

        bindFormData(request, itemOpt.get());
        bindImagesForEdit(request, itemId);
        request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long itemId = parseId(request.getParameter("id"));
        if (itemId == null) {
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
            return;
        }

        String name = request.getParameter("name");
        String sku = request.getParameter("sku");
        String quantityParam = request.getParameter("quantity");
        String location = request.getParameter("location");
        String notes = request.getParameter("notes");

        request.setAttribute("itemId", itemId);
        request.setAttribute("name", safeTrim(name));
        request.setAttribute("sku", safeTrim(sku));
        request.setAttribute("quantity", safeTrim(quantityParam));
        request.setAttribute("location", safeTrim(location));
        request.setAttribute("notes", safeTrim(notes));
        bindImagesForEdit(request, itemId);

        try {
            Integer quantity = parseQuantity(quantityParam);
            warehouseController.updateItem(itemId, name, sku, quantity, location, notes);
            applyImageDeletions(request, itemId);
            applyImageAdditions(request, itemId);
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.UPDATE,
                    UserActivityEntityType.WAREHOUSE_ITEM,
                    String.valueOf(itemId),
                    "modifica articolo magazzino " + safeTrim(name)
            );
            String formPath = "/warehouse/edit?id=" + itemId;
            String fallbackPath = "/warehouse/detail?id=" + itemId + "&updated=1&lockBack=1&navHome=1";
            PostSubmitNavigationGuard.blockFormPageOnce(request, formPath, fallbackPath);
            response.sendRedirect(request.getContextPath() + fallbackPath);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-edit.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            request.setAttribute("error", "Errore durante il salvataggio delle modifiche");
            request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-edit.jsp").forward(request, response);
        }
    }

    private static void bindFormData(HttpServletRequest request, WarehouseItem item) {
        request.setAttribute("itemId", item.getId());
        request.setAttribute("name", item.getName());
        request.setAttribute("sku", item.getSku());
        request.setAttribute("quantity", item.getQuantity());
        request.setAttribute("location", item.getLocation());
        request.setAttribute("notes", item.getNotes());
    }

    private static Integer parseQuantity(String quantityParam) {
        if (quantityParam == null || quantityParam.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(quantityParam.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Quantita non valida");
        }
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void bindImagesForEdit(HttpServletRequest request, Long itemId) {
        if (itemId == null || itemId <= 0) {
            return;
        }
        request.setAttribute("itemImages", warehouseController.findImagesByItemId(itemId));
    }

    private void applyImageDeletions(HttpServletRequest request, Long itemId) {
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

        Path uploadBase = UploadPathResolver.resolveWarehouseUploadBase(getServletContext());
        Path itemDir = uploadBase.resolve(String.valueOf(itemId)).normalize();

        for (String filename : uniqueFilenames) {
            boolean deleted = warehouseController.deleteImageByFilename(itemId, filename);
            if (!deleted) {
                continue;
            }
            Path imagePath = itemDir.resolve(filename).normalize();
            if (imagePath.startsWith(uploadBase)) {
                try {
                    Files.deleteIfExists(imagePath);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void applyImageAdditions(HttpServletRequest request, Long itemId) throws IOException, ServletException {
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

        Path uploadBase = UploadPathResolver.resolveWarehouseUploadBase(getServletContext());
        Path itemDir = uploadBase.resolve(String.valueOf(itemId)).normalize();
        if (!itemDir.startsWith(uploadBase)) {
            throw new IllegalArgumentException("Percorso non valido");
        }
        Files.createDirectories(itemDir);

        for (Part part : validImageParts) {
            String storedFilename = ImageOptimizationUtil.storeOptimizedImage(part, itemDir);
            warehouseController.addImage(itemId, storedFilename);
        }
    }
}
