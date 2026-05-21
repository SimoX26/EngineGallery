package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.model.Customer;
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

@WebServlet("/customer/delete")
public class CustomerDeleteServlet extends HttpServlet {

    private CustomerController customerController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.customerController = initializer.getCustomerController();
        this.activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long customerId = parseCustomerId(request.getParameter("id"));
        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/customer/list");
            return;
        }

        Optional<Customer> customerOpt = customerController.findById(customerId);
        if (customerOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/list");
            return;
        }

        request.setAttribute("customer", customerOpt.get());
        request.setAttribute("canDelete", !customerController.hasAssociatedEngines(customerId));
        request.getRequestDispatcher("/WEB-INF/views/customer/customer-delete-confirm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long customerId = parseCustomerId(request.getParameter("id"));
        String confirmDelete = request.getParameter("confirmDelete");

        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/customer/list");
            return;
        }

        if (!"true".equals(confirmDelete)) {
            response.sendRedirect(request.getContextPath() + "/customer/detail?id=" + customerId);
            return;
        }

        try {
            String customerName = customerController.findById(customerId).map(Customer::getName).orElse("ID " + customerId);
            customerController.deleteCustomer(customerId);
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.DELETE,
                    UserActivityEntityType.CUSTOMER,
                    String.valueOf(customerId),
                    "eliminazione cliente " + customerName
            );
            response.sendRedirect(request.getContextPath() + "/customer/list");
        } catch (IllegalStateException ex) {
            response.sendRedirect(request.getContextPath() + "/customer/detail?id=" + customerId + "&deletedError=1");
        } catch (RuntimeException ex) {
            response.sendRedirect(request.getContextPath() + "/customer/detail?id=" + customerId + "&deletedError=1");
        }
    }

    private Long parseCustomerId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
