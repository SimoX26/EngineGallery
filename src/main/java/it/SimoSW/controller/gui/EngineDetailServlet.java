package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

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
        request.setAttribute("statusUpdated", "1".equals(request.getParameter("statusUpdated")));
        request.setAttribute("statusUpdateError", request.getParameter("statusUpdateError"));
        request.getRequestDispatcher("/WEB-INF/views/engine/engine-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String engineRef = safeTrim(request.getParameter("ref"));
        String statusParam = safeTrim(request.getParameter("status"));

        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        if (statusParam == null || statusParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef
                    + "&statusUpdateError=" + encodeParam("Seleziona uno stato valido"));
            return;
        }

        EngineStatus newStatus;
        try {
            newStatus = EngineStatus.valueOf(statusParam);
        } catch (IllegalArgumentException ex) {
            response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef
                    + "&statusUpdateError=" + encodeParam("Stato non valido"));
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

            response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef + "&statusUpdated=1");
        } catch (RuntimeException ex) {
            response.sendRedirect(request.getContextPath() + "/engine/detail?ref=" + engineRef
                    + "&statusUpdateError=" + encodeParam("Errore durante l'aggiornamento rapido dello stato"));
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
