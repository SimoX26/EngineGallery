package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.DashboardController;
import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.User;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private DashboardController dashboardController;
    private EngineController engineController;
    private CustomerController customerController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.dashboardController = initializer.getDashboardController();
        this.engineController = initializer.getEngineController();
        this.customerController = initializer.getCustomerController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Controllo login
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        User user = (User) session.getAttribute("loggedUser");

        // Recupero dati AGGREGATI
        request.setAttribute("loggedUser", user);

        request.setAttribute("clientiConMotoriAttivi", dashboardController.getClientiConMotoriInOfficina());

        request.setAttribute("motoriInOfficina", dashboardController.getMotoriInOfficina());

        request.setAttribute("workInProgressEngines", dashboardController.getWorkInProgressEngines());

        request.setAttribute("motoriConsegnatiUltimaSettimana", dashboardController.getMotoriConsegnatiUltimaSettimana());

        List<Engine> ultimiMotori = dashboardController.listaUltimiMotori(8);
        request.setAttribute("ultimiMotori", ultimiMotori);

        request.setAttribute("warehouseItemCount", dashboardController.getWarehouseItemCount());
        request.setAttribute("warehouseOutOfStockCount", dashboardController.getWarehouseOutOfStockCount());

        List<WarehouseItem> ultimiArticoliMagazzino = dashboardController.listaUltimiArticoliMagazzino(6);
        request.setAttribute("ultimiArticoliMagazzino", ultimiArticoliMagazzino);

        Map<Long, String> coverImages = new HashMap<>();
        for (Engine engine : ultimiMotori) {
            engineController
                    .getCoverFilenameForEngine(engine.getId())
                    .ifPresent(filename -> coverImages.put(engine.getId(), filename));
        }
        request.setAttribute("coverImages", coverImages);

        Map<Long, String> customerNames = new HashMap<>();
        for (Engine engine : ultimiMotori) {
            long customerId = engine.getCustomerId();
            if (!customerNames.containsKey(customerId)) {
                customerNames.put(customerId, customerController.findNameById(customerId));
            }
        }
        request.setAttribute("customerNames", customerNames);


        // Forward alla view
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}
