package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/warehouse/detail")
public class WarehouseDetailServlet extends HttpServlet {

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

        Optional<WarehouseItem> itemOpt = warehouseController.findById(itemId);
        if (itemOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
            return;
        }

        request.setAttribute("item", itemOpt.get());
        request.setAttribute("updated", "1".equals(request.getParameter("updated")));
        request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-detail.jsp").forward(request, response);
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
