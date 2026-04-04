package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
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
import java.util.List;

@WebServlet("/warehouse/new")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 100 * 1024 * 1024,
        maxRequestSize = 800 * 1024 * 1024
)
public class WarehouseCreateServlet extends HttpServlet {

    private WarehouseController warehouseController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.warehouseController = initializer.getWarehouseController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, "/warehouse/new")) {
            return;
        }
        request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-new.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");
        String sku = request.getParameter("sku");
        String quantityParam = request.getParameter("quantity");
        String location = request.getParameter("location");
        String notes = request.getParameter("notes");

        bindFormData(request, name, sku, quantityParam, location, notes);

        try {
            Integer quantity = parseQuantity(quantityParam);
            Long id = warehouseController.createItem(name, sku, quantity, location, notes);
            applyImageAdditions(request, id);
            PostSubmitNavigationGuard.blockFormPageOnce(
                    request,
                    "/warehouse/new",
                    "/warehouse/detail?id=" + id + "&lockBack=1&navHome=1"
            );
            response.sendRedirect(request.getContextPath() + "/warehouse/detail?id=" + id + "&lockBack=1&navHome=1");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-new.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            request.setAttribute("error", "Errore durante il salvataggio dell'articolo");
            request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-new.jsp").forward(request, response);
        }
    }

    private static void bindFormData(HttpServletRequest request,
                                     String name,
                                     String sku,
                                     String quantity,
                                     String location,
                                     String notes) {
        request.setAttribute("name", safeTrim(name));
        request.setAttribute("sku", safeTrim(sku));
        request.setAttribute("quantity", safeTrim(quantity));
        request.setAttribute("location", safeTrim(location));
        request.setAttribute("notes", safeTrim(notes));
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

    private void applyImageAdditions(HttpServletRequest request, Long itemId) throws IOException, ServletException {
        List<Part> validImageParts = new ArrayList<>();
        for (Part part : request.getParts()) {
            if (!"images".equals(part.getName()) || part.getSize() <= 0) {
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
