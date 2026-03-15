package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/warehouse/new")
public class WarehouseCreateServlet extends HttpServlet {

    private WarehouseController warehouseController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.warehouseController = initializer.getWarehouseController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            response.sendRedirect(request.getContextPath() + "/warehouse/detail?id=" + id);
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
}
