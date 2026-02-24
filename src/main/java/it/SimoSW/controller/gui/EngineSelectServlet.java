package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/engine/select")
public class EngineSelectServlet extends HttpServlet {

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

        List<Engine> engines = engineController.getAllEngines();
        request.setAttribute("engines", engines);

        // Mappa engineId -> cover filename
        Map<Long, String> coverImages = new HashMap<>();

        for (Engine engine : engines) {
            engineController
                    .getCoverFilenameForEngine(engine.getId())
                    .ifPresent(filename -> coverImages.put(engine.getId(), filename));
        }

        request.setAttribute("coverImages", coverImages);

        Map<Long, String> customerNames = new HashMap<>();
        for (Engine engine : engines) {
            long customerId = engine.getCustomerId();
            if (!customerNames.containsKey(customerId)) {
                customerNames.put(customerId, customerController.findNameById(customerId));
            }
        }
        request.setAttribute("customerNames", customerNames);

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-select.jsp").forward(request, response);
    }


    // =================== RICERCA ===================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineCode = request.getParameter("engineCode");
        String statusParam = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        List<Engine> engines;

        // Priorità: codice → stato → keyword
        if (engineCode != null && !engineCode.isBlank()) {

            engines = engineController.findEnginesByCode(engineCode.trim());

        } else if (statusParam != null && !statusParam.isBlank()) {

            try {
                EngineStatus status = EngineStatus.valueOf(statusParam.trim());
                engines = engineController.findEnginesByStatus(status);
            } catch (IllegalArgumentException ex) {
                // valore non valido dal form -> fallback sensato
                engines = engineController.getAllEngines();
                request.setAttribute("error", "Stato non valido: " + statusParam);
            }

        } else if (keyword != null && !keyword.isBlank()) {

            engines = engineController.searchEngines(keyword.trim());

        } else {

            engines = engineController.getAllEngines();
        }

        request.setAttribute("engines", engines);

        Map<Long, String> coverImages = new HashMap<>();

        for (Engine engine : engines) {
            engineController
                    .getCoverFilenameForEngine(engine.getId())
                    .ifPresent(filename -> coverImages.put(engine.getId(), filename));
        }

        request.setAttribute("coverImages", coverImages);

        Map<Long, String> customerNames = new HashMap<>();
        for (Engine engine : engines) {
            long customerId = engine.getCustomerId();
            if (!customerNames.containsKey(customerId)) {
                customerNames.put(customerId, customerController.findNameById(customerId));
            }
        }
        request.setAttribute("customerNames", customerNames);

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-select.jsp").forward(request, response);
    }
}
