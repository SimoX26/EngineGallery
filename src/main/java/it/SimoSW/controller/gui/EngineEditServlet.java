package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bean.EngineBean;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/engine/edit")
public class EngineEditServlet extends HttpServlet {

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
        String engineRef = request.getParameter("ref");

        if (!isValidEngineRef(engineRef)) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }
        String formPath = "/engine/edit?ref=" + engineRef;
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, formPath)) {
            return;
        }

        EngineDetailBean detail = engineController.getEngineDetail(engineRef);
        if (detail == null) {
            response.sendRedirect(request.getContextPath() + "/engine/list");
            return;
        }

        bindFormData(request,
                detail.getEngine().getEngineRef(),
                detail.getEngine().getCustomerName(),
                detail.getEngine().getEngineCode(),
                detail.getEngine().getStatus(),
                detail.getEngine().getIntakeDate(),
                detail.getEngine().getDeliveryDate(),
                detail.getEngine().getNotes());

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String engineRef = safeTrim(request.getParameter("ref"));
        String customerName = safeTrim(request.getParameter("customer"));
        String engineCode = safeTrim(request.getParameter("engineCode"));
        String statusParam = safeTrim(request.getParameter("status"));
        String intakeDate = safeTrim(request.getParameter("intakeDate"));
        String deliveryDate = safeTrim(request.getParameter("deliveryDate"));
        String note = safeTrim(request.getParameter("note"));

        bindFormData(request, engineRef, customerName, engineCode, statusParam, intakeDate, deliveryDate, note);

        if (!isValidEngineRef(engineRef)) {
            request.setAttribute("error", "Riferimento motore non valido");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        if (customerName == null || customerName.isBlank() || engineCode == null || engineCode.isBlank()) {
            request.setAttribute("error", "Nome cliente e codice motore sono obbligatori");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        if (statusParam == null || statusParam.isBlank()) {
            request.setAttribute("error", "Seleziona uno stato di lavorazione");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        if (intakeDate == null || intakeDate.isBlank()) {
            request.setAttribute("error", "La data ingresso è obbligatoria");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        EngineStatus status;
        try {
            status = EngineStatus.valueOf(statusParam);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", "Stato non valido: " + statusParam);
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        LocalDate parsedIntakeDate;
        try {
            parsedIntakeDate = LocalDate.parse(intakeDate);
        } catch (Exception ex) {
            request.setAttribute("error", "Data ingresso non valida");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
            return;
        }

        String normalizedDeliveryDate = null;
        if (status == EngineStatus.DELIVERED) {
            if (deliveryDate == null || deliveryDate.isBlank()) {
                normalizedDeliveryDate = LocalDate.now().toString();
            } else {
                try {
                    normalizedDeliveryDate = LocalDate.parse(deliveryDate).toString();
                } catch (Exception ex) {
                    request.setAttribute("error", "Data consegna non valida");
                    request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
                    return;
                }
            }
        }

        try {
            Long customerId = customerController.findOrCreateCustomerId(customerName);

            EngineBean bean = new EngineBean();
            bean.setEngineRef(engineRef);
            bean.setEngineCode(engineCode);
            bean.setCustomerId(customerId);
            bean.setStatus(status.name());
            bean.setIntakeDate(parsedIntakeDate.toString());
            bean.setDeliveryDate(normalizedDeliveryDate);
            bean.setNotes(note);

            engineController.updateEngine(bean);
            String formPath = "/engine/edit?ref=" + engineRef;
            String fallbackPath = "/engine/detail?ref=" + engineRef + "&updated=1&lockBack=1&navHome=1";
            PostSubmitNavigationGuard.blockFormPageOnce(request, formPath, fallbackPath);
            response.sendRedirect(request.getContextPath() + fallbackPath);
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
        } catch (RuntimeException e) {
            request.setAttribute("error", "Errore durante il salvataggio delle modifiche");
            request.getRequestDispatcher("/WEB-INF/views/engine/engine-edit.jsp").forward(request, response);
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

    private static void bindFormData(HttpServletRequest request,
                                     String engineRef,
                                     String customer,
                                     String engineCode,
                                     String status,
                                     String intakeDate,
                                     String deliveryDate,
                                     String note) {
        request.setAttribute("engineRef", engineRef);
        request.setAttribute("customer", customer);
        request.setAttribute("engineCode", engineCode);
        request.setAttribute("status", status);
        request.setAttribute("intakeDate", intakeDate);
        request.setAttribute("deliveryDate", deliveryDate);
        request.setAttribute("note", note);
    }
}
