package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/warehouse/delete")
public class WarehouseDeleteServlet extends HttpServlet {

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

        Optional<WarehouseItem> itemOpt = warehouseController.findById(itemId);
        if (itemOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
            return;
        }

        request.setAttribute("item", itemOpt.get());
        request.getRequestDispatcher("/WEB-INF/views/warehouse/warehouse-delete-confirm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long itemId = parseId(request.getParameter("id"));
        String confirmDelete = request.getParameter("confirmDelete");

        if (itemId == null) {
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
            return;
        }

        if (!"true".equals(confirmDelete)) {
            response.sendRedirect(request.getContextPath() + "/warehouse/detail?id=" + itemId);
            return;
        }

        try {
            String itemName = warehouseController.findById(itemId).map(WarehouseItem::getName).orElse("ID " + itemId);
            warehouseController.deleteItem(itemId);
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.DELETE,
                    UserActivityEntityType.WAREHOUSE_ITEM,
                    String.valueOf(itemId),
                    "eliminazione articolo magazzino " + itemName
            );
            response.sendRedirect(request.getContextPath() + "/warehouse/list");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            response.sendRedirect(request.getContextPath() + "/warehouse/detail?id=" + itemId);
        } catch (RuntimeException ex) {
            response.sendRedirect(request.getContextPath() + "/warehouse/detail?id=" + itemId);
        }
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
