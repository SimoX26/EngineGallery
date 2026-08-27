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
import java.util.List;

@WebServlet("/catalog")
public class CatalogListServlet extends HttpServlet {

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
        try {
            List<CatalogItem> catalogItems = catalogController.getAllItems();
            request.setAttribute("catalogItems", catalogItems);
        } catch (Exception ex) {
            request.setAttribute("error", "Errore durante il caricamento del catalogo");
        }

        request.getRequestDispatcher("/WEB-INF/views/catalog/catalog-list.jsp").forward(request, response);
    }
}
