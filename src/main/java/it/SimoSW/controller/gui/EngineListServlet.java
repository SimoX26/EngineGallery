package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/engine")
public class EngineListServlet extends HttpServlet {

    private EngineController engineController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getEngineController();
    }



    /* =========================
       GET: visualizzazione
       ========================= */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        List<Engine> engines = engineController.getAllEngines();
        request.setAttribute("engines", engines);

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-list.jsp").forward(request, response);
    }



    /* =========================
       POST: ricerca motori
       ========================= */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineCode = request.getParameter("engineCode");
        String statusParam = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        List<Engine> engines;

        // Priorità: codice → stato → keyword
        if (engineCode != null && !engineCode.isBlank()) {
            Optional<Engine> engine = engineController.findEngineByCode(engineCode);

            engines = engine.map(List::of).orElse(List.of());

        } else if (statusParam != null && !statusParam.isBlank()) {
            EngineStatus status = EngineStatus.valueOf(statusParam);
            engines = engineController.findEnginesByStatus(status);

        } else if (keyword != null && !keyword.isBlank()) {
            engines = engineController.findEnginesByKeyword(keyword);

        } else {
            engines = engineController.getAllEngines();
        }

        request.setAttribute("engines", engines);

        request.getRequestDispatcher("/WEB-INF/views/engine/engine-list.jsp").forward(request, response);
    }
}
