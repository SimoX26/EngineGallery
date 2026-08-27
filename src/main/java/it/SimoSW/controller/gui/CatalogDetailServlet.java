package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CatalogController;
import it.SimoSW.model.CatalogItem;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/catalog/detail")
public class CatalogDetailServlet extends HttpServlet {

    private CatalogController catalogController;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.catalogController = initializer.getCatalogController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long catalogItemId = parseId(request.getParameter("id"));
        if (catalogItemId == null) {
            response.sendRedirect(request.getContextPath() + "/catalog");
            return;
        }

        Optional<CatalogItem> catalogItem = catalogController.findById(catalogItemId);
        if (catalogItem.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/catalog");
            return;
        }

        request.setAttribute("catalogItem", catalogItem.get());
        request.getRequestDispatcher("/WEB-INF/views/catalog/catalog-detail.jsp").forward(request, response);
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
