package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.model.Customer;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/customer/detail")
public class CustomerDetailServlet extends HttpServlet {

    private CustomerController customerController;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.customerController = initializer.getCustomerController();
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
        request.setAttribute("updated", "1".equals(request.getParameter("updated")));
        request.setAttribute("deletedError", request.getParameter("deletedError"));
        request.getRequestDispatcher("/WEB-INF/views/customer/customer-detail.jsp").forward(request, response);
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
