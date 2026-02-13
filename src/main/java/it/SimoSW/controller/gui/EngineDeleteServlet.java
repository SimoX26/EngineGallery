package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/engine/delete")
public class EngineDeleteServlet extends HttpServlet {

    private EngineController engineController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineRef = request.getParameter("engineRef");

        /* =========================
           Validazione parametro
           ========================= */

        // 1. Parametro mancante o vuoto
        if (engineRef == null || engineRef.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        // 2. Lunghezza sospetta (difesa base)
        if (engineRef.length() > 50) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        // 3. Controllo formato
        if (!engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$")) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        /* =========================
           Eliminazione
           ========================= */

        try {
            engineController.deleteEngine(engineRef);
            response.sendRedirect(request.getContextPath() + "/engine/list");
        } catch (IllegalStateException e) {
            // Motore non trovato
            response.sendRedirect(request.getContextPath() + "/engine/list");
        } catch (Exception e) {
            // Errore generico
            request.setAttribute("error", "Errore durante l'eliminazione del motore: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-list.jsp").forward(request, response);
        }
    }
}

