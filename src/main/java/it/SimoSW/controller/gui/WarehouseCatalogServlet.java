package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WarehouseController;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/warehouse/catalog")
public class WarehouseCatalogServlet extends HttpServlet {
    private WarehouseController controller;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getWarehouseController();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if ("addCategory".equals(action)) controller.addCategory(request.getParameter("department"), request.getParameter("name"),
                    "on".equals(request.getParameter("injectorFields")));
            else if ("renameCategory".equals(action)) controller.renameCategory(parseId(request.getParameter("id")), request.getParameter("name"));
            else if ("deleteCategory".equals(action)) controller.deleteCategory(parseId(request.getParameter("id")));
            else throw new IllegalArgumentException("Operazione non valida");
            request.getSession().setAttribute("warehouseCatalogMessage", "Configurazione aggiornata.");
        } catch (IllegalArgumentException ex) {
            request.getSession().setAttribute("warehouseCatalogError", ex.getMessage());
        } catch (RuntimeException ex) {
            request.getSession().setAttribute("warehouseCatalogError", "Impossibile aggiornare la configurazione del magazzino.");
        }
        response.sendRedirect(request.getContextPath() + "/warehouse/list#warehouse-config");
    }

    private static Long parseId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id > 0) return id;
        } catch (NumberFormatException | NullPointerException ignored) { }
        throw new IllegalArgumentException("ID non valido");
    }
}
