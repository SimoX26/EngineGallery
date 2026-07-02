package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@WebServlet("/engine/detail")
public class EngineDetailServlet extends HttpServlet {

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String engineRef = safeTrim(request.getParameter("ref"));
        String statusParam = safeTrim(request.getParameter("status"));
        String redirectTo = safeTrim(request.getParameter("redirectTo"));
        boolean backToList = "list".equalsIgnoreCase(redirectTo);
        boolean backToArchive = "archive".equalsIgnoreCase(redirectTo);

        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        if (statusParam == null || statusParam.isBlank()) {
            if (backToList) {
                response.sendRedirect(request.getContextPath() + "/engine/list?statusUpdateError="
                        + encodeParam("Seleziona uno stato valido"));
            } else if (backToArchive) {
                response.sendRedirect(request.getContextPath() + "/engine/archive?statusUpdateError="
                        + encodeParam("Seleziona uno stato valido"));
            } else {
                response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef
                        + "&statusUpdateError=" + encodeParam("Seleziona uno stato valido"));
            }
            return;
        }

        EngineStatus newStatus;
        try {
            newStatus = EngineStatus.valueOf(statusParam);
        } catch (IllegalArgumentException ex) {
            if (backToList) {
                response.sendRedirect(request.getContextPath() + "/engine/list?statusUpdateError="
                        + encodeParam("Stato non valido"));
            } else if (backToArchive) {
                response.sendRedirect(request.getContextPath() + "/engine/archive?statusUpdateError="
                        + encodeParam("Stato non valido"));
            } else {
                response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef
                        + "&statusUpdateError=" + encodeParam("Stato non valido"));
            }
            return;
        }

        EngineDetailBean detail = engineController.getEngineDetail(engineRef);
        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        try {
            EngineBean currentEngine = detail.getEngine();
            EngineBean updateBean = new EngineBean();
            updateBean.setEngineRef(currentEngine.getEngineRef());
            updateBean.setEngineCode(currentEngine.getEngineCode());
            updateBean.setCustomerId(currentEngine.getCustomerId());
            updateBean.setIntakeDate(currentEngine.getIntakeDate());
            updateBean.setNotes(currentEngine.getNotes());
            updateBean.setStatus(newStatus.name());

            if (newStatus == EngineStatus.DELIVERED) {
                String currentDeliveryDate = currentEngine.getDeliveryDate();
                updateBean.setDeliveryDate(
                        currentDeliveryDate != null && !currentDeliveryDate.isBlank()
                                ? currentDeliveryDate
                                : LocalDate.now().toString()
                );
            } else {
                updateBean.setDeliveryDate(null);
            }

            engineController.updateEngine(updateBean);
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.STATUS_CHANGE,
                    UserActivityEntityType.MOTOR,
                    engineRef,
                    "cambio stato motore " + engineRef + ": " + currentEngine.getStatus() + " -> " + newStatus.name()
            );

            if (backToList) {
                response.sendRedirect(request.getContextPath() + "/engine/list?statusUpdated=1");
            } else if (backToArchive) {
                response.sendRedirect(request.getContextPath() + "/engine/archive?statusUpdated=1");
            } else {
                response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef + "&statusUpdated=1");
            }
        } catch (RuntimeException ex) {
            if (backToList) {
                response.sendRedirect(request.getContextPath() + "/engine/list?statusUpdateError="
                        + encodeParam("Errore durante l'aggiornamento rapido dello stato"));
            } else if (backToArchive) {
                response.sendRedirect(request.getContextPath() + "/engine/archive?statusUpdateError="
                        + encodeParam("Errore durante l'aggiornamento rapido dello stato"));
            } else {
                response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef
                        + "&statusUpdateError=" + encodeParam("Errore durante l'aggiornamento rapido dello stato"));
            }
        }
    }

    private static boolean isValidEngineRef(String engineRef) {
        return engineRef != null
                && !engineRef.isBlank()
                && engineRef.length() <= 50
                && engineRef.matches("^RML-[0-9]{4}-[0-9]{5}$");
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private static String encodeParam(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
