package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/engine/detail")
public class EngineDetailServlet extends HttpServlet {

    private EngineController engineController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String idParam = request.getParameter("id");

        // Controllo su parametro mancante
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // Controllo su parametro non numerico
        long engineId;
        try {
            engineId = Long.parseLong(idParam);
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // Recupero dettaglio completo
        EngineDetailBean detail = engineController.getEngineDetail(engineId);

        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // Passo UN SOLO oggetto alla JSP
        request.setAttribute("detail", detail);

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-detail.jsp").forward(request, response);
    }
}