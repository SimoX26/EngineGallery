package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.WarehouseComponentType;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;

@WebServlet("/warehouse/list")
public class WarehouseListServlet extends HttpServlet {

    private WarehouseController warehouseController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.warehouseController = initializer.getWarehouseController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            List<WarehouseItem> items = warehouseController.getAllItems();
            String componentTypeParam = request.getParameter("componentType");
            WarehouseComponentType selectedComponentType = WarehouseComponentType.fromValue(componentTypeParam).orElse(null);
            String department = request.getParameter("department");
            String categoryId = request.getParameter("categoryId");
            items = items.stream().filter(item ->
                    (selectedComponentType == null || item.getComponentType() == selectedComponentType)
                    && matches(item.getDetails() == null ? null : item.getDetails().getDepartment(), department, true)
                    && (blank(categoryId) || item.getDetails() != null && item.getDetails().getCategoryId() != null
                        && item.getDetails().getCategoryId().toString().equals(categoryId))
                    && matches(item.getDetails() == null ? null : item.getDetails().getManufacturer(), request.getParameter("manufacturer"), false)
                    && matches(item.getSku(), request.getParameter("sku"), false)
                    && matches(item.getDetails() == null ? null : item.getDetails().getCondition(), request.getParameter("condition"), false)
                    && matches(item.getLocation(), request.getParameter("location"), false)
                    && matches(item.getDetails() == null ? null : item.getDetails().getLotCode(), request.getParameter("lotCode"), false)
                    && matchesEngine(item, request.getParameter("engineCode"))
            ).toList();
            request.setAttribute("items", items);
            request.setAttribute("categories", warehouseController.getCategories());
            request.setAttribute("locations", warehouseController.getLocations());
            request.setAttribute("previewImages", warehouseController.getPreviewFilenames());
            request.setAttribute("componentTypes", Arrays.stream(WarehouseComponentType.values())
                    .filter(type -> type != WarehouseComponentType.UNCLASSIFIED)
                    .toList());
            request.setAttribute("selectedComponentType", selectedComponentType == null ? "" : selectedComponentType.name());
            request.setAttribute("selectedComponentTypeLabel", selectedComponentType == null ? "" : selectedComponentType.getLabel());
            Object message = request.getSession().getAttribute("warehouseCatalogMessage");
            Object catalogError = request.getSession().getAttribute("warehouseCatalogError");
            request.setAttribute("catalogMessage", message);
            request.setAttribute("catalogError", catalogError);
            request.getSession().removeAttribute("warehouseCatalogMessage");
            request.getSession().removeAttribute("warehouseCatalogError");
        } catch (Exception e) {
            request.setAttribute("error", "Errore durante il caricamento del magazzino");
        }

        request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-list.jsp").forward(request, response);
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static boolean matches(String value, String filter, boolean exact) {
        if (blank(filter)) return true;
        if (value == null) return false;
        String candidate = value.toLowerCase(Locale.ROOT);
        String wanted = filter.trim().toLowerCase(Locale.ROOT);
        return exact ? candidate.equals(wanted) : candidate.contains(wanted);
    }

    private static boolean matchesEngine(WarehouseItem item, String filter) {
        if (blank(filter)) return true;
        if (item.getDetails() == null) return false;
        return matches(item.getDetails().getSourceEngineCode(), filter, false)
                || item.getDetails().getCompatibilities().stream().anyMatch(c -> matches(c.getEngineCode(), filter, false));
    }
}
