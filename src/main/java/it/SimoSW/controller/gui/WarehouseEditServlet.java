package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/warehouse/edit")
public class WarehouseEditServlet extends HttpServlet {

    private WarehouseController warehouseController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.warehouseController = initializer.getWarehouseController();
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

        try {
            Integer quantity = parseQuantity(quantityParam);
            warehouseController.updateItem(itemId, name, sku, quantity, location, notes);
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
}
