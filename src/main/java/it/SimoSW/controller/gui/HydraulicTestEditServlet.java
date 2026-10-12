package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.HydraulicTestController;
import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.HydraulicVideoStorage;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/hydraulic-test/edit")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 500 * 1024 * 1024,
        maxRequestSize = 700 * 1024 * 1024
)
public class HydraulicTestEditServlet extends HttpServlet {
    private HydraulicTestController hydraulicTestController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        hydraulicTestController = initializer.getHydraulicTestController();
        activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<HydraulicTest> test = findTest(request);
        if (test.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
            return;
        }
        request.setAttribute("hydraulicTest", test.get());
        request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Optional<HydraulicTest> existing = findTest(request);
        if (existing.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
            return;
        }
        String replacementVideoUrl = null;
        try {
            Part videoPart = request.getPart("videoFile");
            if (videoPart != null && videoPart.getSize() > 0) {
                replacementVideoUrl = HydraulicVideoStorage.store(getServletContext(), videoPart);
            }
            hydraulicTestController.updateHydraulicTest(
                    existing.get().getId(),
                    request.getParameter("customerName"),
                    request.getParameter("engineCode"),
                    request.getParameter("testDate"),
                    request.getParameter("notes"),
                    replacementVideoUrl
            );
            if (replacementVideoUrl != null) {
                try {
                    HydraulicVideoStorage.deleteIfLocal(getServletContext(), existing.get().getVideoUrl());
                } catch (IOException ignored) {
                    // Il nuovo video è già associato alla prova; un file precedente residuo non deve annullare la modifica.
                }
            }
            activityAuditLogger.logFromRequest(request, UserActivityActionType.UPDATE,
                    UserActivityEntityType.HYDRAULIC_TEST, String.valueOf(existing.get().getId()),
                    "modifica prova idraulica " + existing.get().getEngineCode());
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/detail?id=" + existing.get().getId() + "&updated=1");
        } catch (IllegalArgumentException | IllegalStateException | IOException | ServletException ex) {
            if (replacementVideoUrl != null) {
                try {
                    HydraulicVideoStorage.deleteIfLocal(getServletContext(), replacementVideoUrl);
                } catch (IOException ignored) {
                    // Il salvataggio è già fallito; il messaggio d'errore deve comunque raggiungere l'utente.
                }
            }
            request.setAttribute("error", ex.getMessage());
            request.setAttribute("hydraulicTest", existing.get());
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-edit.jsp").forward(request, response);
        }
    }

    private Optional<HydraulicTest> findTest(HttpServletRequest request) {
        try {
            return hydraulicTestController.findHydraulicTestById(Long.parseLong(request.getParameter("id")));
        } catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }
}
