package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
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

        String engineRef = request.getParameter("ref");

        /* =========================
           Validazione parametro
           ========================= */

        // 1. Parametro mancante o vuoto
        if (engineRef == null || engineRef.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // 2. Lunghezza sospetta (difesa base)
        if (engineRef.length() > 50) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // 3. (OPZIONALE ma consigliato) Controllo formato
        if (!engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$")) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        /* =========================
           Recupero dati
           ========================= */

        EngineDetailBean detail = engineController.getEngineDetail(engineRef);

        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        /* =========================
           Forward alla JSP
           ========================= */

        request.setAttribute("detail", detail);
        request.setAttribute("updated", "1".equals(request.getParameter("updated")));
        request.getRequestDispatcher("/WEB-INF/views/engine/engine-detail.jsp").forward(request, response);
    }
}
