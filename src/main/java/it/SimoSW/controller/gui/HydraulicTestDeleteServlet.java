package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.HydraulicTestController;
import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/hydraulic-test/delete")
public class HydraulicTestDeleteServlet extends HttpServlet {
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
        request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-delete-confirm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<HydraulicTest> test = findTest(request);
        if (test.isEmpty() || !"true".equals(request.getParameter("confirmDelete"))) {
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
            return;
        }
        hydraulicTestController.deleteHydraulicTest(test.get().getId());
        activityAuditLogger.logFromRequest(request, UserActivityActionType.DELETE,
                UserActivityEntityType.HYDRAULIC_TEST, String.valueOf(test.get().getId()),
                "eliminazione prova idraulica " + test.get().getEngineCode());
        response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
    }

    private Optional<HydraulicTest> findTest(HttpServletRequest request) {
        try {
            return hydraulicTestController.findHydraulicTestById(Long.parseLong(request.getParameter("id")));
        } catch (NumberFormatException | NullPointerException ex) {
            return Optional.empty();
        }
    }
}
