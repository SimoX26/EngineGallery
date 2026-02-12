package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.model.Customer;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/customer/list")
public class CustomerListServlet extends HttpServlet {

    private CustomerController customerController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.customerController = initializer.getCustomerController();
    }


    /* =========================
       GET → Visualizzazione rubrica
       ========================= */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {

            // Livello applicativo
            List<Customer> customers = customerController.getAllCustomers();

            // Passaggio dati alla view
            request.setAttribute("customers", customers);

        } catch (Exception e) {

            request.setAttribute("error",
                    "Errore durante il caricamento della rubrica clienti");
        }

        // Forward (sempre alla fine)
        request.getRequestDispatcher("/WEB-INF/views/customer/customer-list.jsp").forward(request, response);
    }
}