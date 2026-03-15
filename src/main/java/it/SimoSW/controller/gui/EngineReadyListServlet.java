package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/engine/ready")
public class EngineReadyListServlet extends HttpServlet {

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

        List<Engine> engines = engineController.findEnginesByStatus(EngineStatus.READY);
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

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-ready-list.jsp").forward(request, response);
    }
}
