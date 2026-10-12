package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.HydraulicTestController;
import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.model.User;
import it.SimoSW.util.HydraulicVideoStorage;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@WebServlet("/hydraulic-test/new")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 500 * 1024 * 1024,
        maxRequestSize = 700 * 1024 * 1024
)
public class HydraulicTestCreateServlet extends HttpServlet {
    private HydraulicTestController hydraulicTestController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.hydraulicTestController = initializer.getHydraulicTestController();
        this.activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, "/hydraulic-test/new")) {
            return;
        }
        request.setAttribute("testDate", LocalDate.now().toString());
        request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String customerName = request.getParameter("customerName");
        String engineCode = request.getParameter("engineCode");
        String testDate = request.getParameter("testDate");
        String notes = request.getParameter("notes");
        Part videoPart;
        try {
            videoPart = request.getPart("videoFile");
        } catch (IllegalStateException ex) {
            request.setAttribute("error", "Il video supera il limite massimo consentito di 500 MB.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }

        request.setAttribute("customerName", customerName);
        request.setAttribute("engineCode", engineCode);
        request.setAttribute("testDate", testDate);
        request.setAttribute("notes", notes);

        if (isBlank(customerName) || isBlank(engineCode) || isBlank(testDate)) {
            request.setAttribute("error", "Compila tutti i campi obbligatori.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }
        try {
            LocalDate.parse(testDate.trim());
        } catch (DateTimeParseException ex) {
            request.setAttribute("error", "Data prova non valida.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }

        String finalFilename;
        try {
            finalFilename = HydraulicVideoStorage.store(getServletContext(), videoPart);
        } catch (IllegalArgumentException | IOException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }

        try {
            HydraulicTest createdTest = hydraulicTestController.createHydraulicTest(
                    customerName,
                    engineCode,
                    finalFilename,
                    testDate,
                    notes,
                    resolveLoggedUsername(request)
            );
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.CREATE,
                    UserActivityEntityType.HYDRAULIC_TEST,
                    String.valueOf(createdTest.getId()),
                    "aggiunta prova idraulica " + engineCode
            );
            PostSubmitNavigationGuard.blockFormPageOnce(
                    request,
                    "/hydraulic-test/new",
                    "/hydraulic-test/list?lockBack=1"
            );
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list?lockBack=1");
        } catch (RuntimeException ex) {
            try {
                HydraulicVideoStorage.deleteIfLocal(getServletContext(), finalFilename);
            } catch (IOException ignored) {
                // Il salvataggio DB è già fallito; il messaggio d'errore deve comunque raggiungere l'utente.
            }
            request.setAttribute("error", "Errore durante il salvataggio della prova idraulica.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String resolveLoggedUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session != null ? session.getAttribute("loggedUser") : null;
        if (!(value instanceof User)) {
            return null;
        }
        String username = ((User) value).getUsername();
        return username == null || username.isBlank() ? null : username.trim();
    }

}
