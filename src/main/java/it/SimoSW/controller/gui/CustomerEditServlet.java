package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.model.Customer;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/customer/edit")
public class CustomerEditServlet extends HttpServlet {

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
        String formPath = "/customer/edit?id=" + customerId;
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, formPath)) {
            return;
        }

        Optional<Customer> customerOpt = customerController.findById(customerId);
        if (customerOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/list");
            return;
        }

        bindFormData(request, customerOpt.get());
        request.getRequestDispatcher("/WEB-INF/views/customer/customer-edit.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long customerId = parseCustomerId(request.getParameter("id"));
        if (customerId == null) {
            response.sendRedirect(request.getContextPath() + "/customer/list");
            return;
        }

        String name = request.getParameter("name");
        String companyName = request.getParameter("companyName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String notes = request.getParameter("notes");

        request.setAttribute("customerId", customerId);
        request.setAttribute("name", safeTrim(name));
        request.setAttribute("companyName", safeTrim(companyName));
        request.setAttribute("phone", safeTrim(phone));
        request.setAttribute("email", safeTrim(email));
        request.setAttribute("notes", safeTrim(notes));

        try {
            customerController.updateCustomer(customerId, name, companyName, phone, email, notes);
            String formPath = "/customer/edit?id=" + customerId;
            String fallbackPath = "/customer/detail?id=" + customerId + "&updated=1&lockBack=1&navHome=1";
            PostSubmitNavigationGuard.blockFormPageOnce(request, formPath, fallbackPath);
            response.sendRedirect(request.getContextPath() + fallbackPath);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/customer/customer-edit.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            request.setAttribute("error", "Errore durante il salvataggio delle modifiche del cliente");
            request.getRequestDispatcher("/WEB-INF/views/customer/customer-edit.jsp").forward(request, response);
        }
    }

    private static void bindFormData(HttpServletRequest request, Customer customer) {
        request.setAttribute("customerId", customer.getId());
        request.setAttribute("name", customer.getName());
        request.setAttribute("companyName", customer.getCompanyName());
        request.setAttribute("phone", customer.getPhone());
        request.setAttribute("email", customer.getEmail());
        request.setAttribute("notes", customer.getNotes());
    }

    private static String safeTrim(String value) {
        return value == null ? null : value.trim();
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
