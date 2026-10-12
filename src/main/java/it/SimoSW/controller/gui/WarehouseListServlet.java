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
            if (selectedComponentType != null) {
                items = items.stream()
                        .filter(item -> item.getComponentType() == selectedComponentType)
                        .toList();
            }
            request.setAttribute("items", items);
            request.setAttribute("componentTypes", Arrays.stream(WarehouseComponentType.values())
                    .filter(type -> type != WarehouseComponentType.UNCLASSIFIED)
                    .toList());
            request.setAttribute("selectedComponentType", selectedComponentType == null ? "" : selectedComponentType.name());
            request.setAttribute("selectedComponentTypeLabel", selectedComponentType == null ? "" : selectedComponentType.getLabel());
        } catch (Exception e) {
            request.setAttribute("error", "Errore durante il caricamento del magazzino");
        }

        request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-list.jsp").forward(request, response);
    }
}
