package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/engine/delete")
public class EngineDeleteServlet extends HttpServlet {

    private EngineController engineController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
        this.activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String engineRef = request.getParameter("engineRef");
        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        EngineDetailBean detail = engineController.getEngineDetail(engineRef);
        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        request.setAttribute("detail", detail);
        request.getRequestDispatcher("/WEB-INF/views/engine/engine-delete-confirm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineRef = request.getParameter("engineRef");
        String confirmDelete = request.getParameter("confirmDelete");

        /* =========================
           Validazione parametro
           ========================= */

        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        if (!"true".equals(confirmDelete)) {
            response.sendRedirect(request.getContextPath() + "/engine/delete?engineRef=" + engineRef);
            return;
        }

        /* =========================
           Eliminazione
           ========================= */

        try {
            engineController.deleteEngine(engineRef);
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.DELETE,
                    UserActivityEntityType.MOTOR,
                    engineRef,
                    "eliminazione motore " + engineRef
            );
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

    private boolean isValidEngineRef(String engineRef) {
        if (engineRef == null || engineRef.isBlank()) {
            return false;
        }
        if (engineRef.length() > 50) {
            return false;
        }
        return engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$");
    }
}
