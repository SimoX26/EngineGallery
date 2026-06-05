package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet({"/engine/list", "/engine/archive"})
public class EngineListServlet extends HttpServlet {

    private EngineController engineController;
    private CustomerController customerController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
        this.customerController = initializer.getCustomerController();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean archiveMode = "/engine/archive".equals(request.getServletPath());
        if (archiveMode && !isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String engineCode = safeTrim(request.getParameter("engineCode"));
        String statusParam = safeTrim(request.getParameter("status"));
        String customerIdParam = safeTrim(request.getParameter("customerId"));
        String keyword = safeTrim(request.getParameter("keyword"));

        Long selectedCustomerId = parseLong(customerIdParam);
        EngineStatus selectedStatus = null;

        if (statusParam != null && !statusParam.isBlank()) {
            try {
                selectedStatus = EngineStatus.valueOf(statusParam);
            } catch (IllegalArgumentException ex) {
                request.setAttribute("error", "Stato non valido: " + statusParam);
            }
        }

        if (archiveMode) {
            selectedStatus = EngineStatus.DELIVERED;
            statusParam = EngineStatus.DELIVERED.name();
        }
        final EngineStatus effectiveStatus = selectedStatus;

        List<Engine> scopedEngines = engineController.getAllEngines().stream()
                .filter(engine -> archiveMode
                        ? engine.getStatus() == EngineStatus.DELIVERED
                        : engine.getStatus() != EngineStatus.DELIVERED)
                .collect(Collectors.toList());

        Map<Long, String> customerNames = buildCustomerNames(scopedEngines);

        List<Engine> filteredEngines = scopedEngines.stream()
                .filter(engine -> effectiveStatus == null || engine.getStatus() == effectiveStatus)
                .filter(engine -> engineCode == null || engineCode.isBlank() || engineCode.equalsIgnoreCase(engine.getEngineCode()))
                .filter(engine -> selectedCustomerId == null || engine.getCustomerId() == selectedCustomerId)
                .filter(engine -> keyword == null || keyword.isBlank() || matchesKeyword(engine, customerNames.get(engine.getCustomerId()), keyword))
                .collect(Collectors.toList());

        sortByMostRecentIntake(filteredEngines);
        request.setAttribute("engines", filteredEngines);

        Map<Long, String> coverImages = new HashMap<>();
        for (Engine engine : filteredEngines) {
            engineController
                    .getCoverFilenameForEngine(engine.getId())
                    .ifPresent(filename -> coverImages.put(engine.getId(), filename));
        }

        request.setAttribute("coverImages", coverImages);
        request.setAttribute("customerNames", customerNames);

        request.setAttribute("archiveMode", archiveMode);
        request.setAttribute("pageTitle", archiveMode ? "Archivio motori" : "Lista motori");

        request.setAttribute("filterEngineCode", engineCode == null ? "" : engineCode);
        request.setAttribute("filterStatus", statusParam == null ? "" : statusParam);
        request.setAttribute("filterCustomerId", selectedCustomerId == null ? "" : String.valueOf(selectedCustomerId));
        request.setAttribute("filterKeyword", keyword == null ? "" : keyword);

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-list.jsp").forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private void sortByMostRecentIntake(List<Engine> engines) {
        engines.sort(
                Comparator.comparing(Engine::getIntakeDate).reversed()
                        .thenComparing(Engine::getId, Comparator.reverseOrder())
        );
    }

    private Map<Long, String> buildCustomerNames(List<Engine> engines) {
        Map<Long, String> customerNames = new LinkedHashMap<>();
        for (Engine engine : engines) {
            long customerId = engine.getCustomerId();
            if (!customerNames.containsKey(customerId)) {
                customerNames.put(customerId, customerController.findNameById(customerId));
            }
        }
        return customerNames;
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loggedUser = session != null ? (User) session.getAttribute("loggedUser") : null;
        UserRole role = loggedUser != null ? loggedUser.getRole() : null;
        return role == UserRole.ADMIN;
    }

    private static boolean matchesKeyword(Engine engine, String customerName, String keyword) {
        String normalizedKeyword = normalizeText(keyword);
        if (normalizedKeyword.isBlank()) {
            return true;
        }

        String statusLabel = switch (engine.getStatus()) {
            case WAITING -> "in attesa";
            case WORK_IN_PROGRESS -> "in lavorazione";
            case READY -> "pronto";
            case DELIVERED -> "consegnato";
        };

        String haystack = String.join(" ",
                safeValue(engine.getEngineCode()),
                safeValue(engine.getEngineRef()),
                safeValue(customerName),
                safeValue(engine.getStatus().name()),
                statusLabel,
                safeValue(engine.getNotes())
        );

        return normalizeText(haystack).contains(normalizedKeyword);
    }

    private static String safeValue(String value) {
        return value == null ? "" : value;
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "")
                .toLowerCase()
                .trim();
    }
}
