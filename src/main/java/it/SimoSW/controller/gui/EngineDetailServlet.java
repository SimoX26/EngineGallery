package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.EngineController;
import it.SimoSW.util.bean.EngineDetailBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/engine/detail")
public class EngineDetailServlet extends HttpServlet {

    private EngineController engineController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.engineController = initializer.getGalleryController();
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Controllo login
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Lettura parametro
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        long engineId;
        try {
            engineId = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // Recupero dati (bean di view)
        EngineDetailBean engine = engineController.getEngineDetail(engineId);

        if (engine == null) {
            response.sendRedirect(request.getContextPath() + "/engine");
            return;
        }

        // 4️⃣ Passaggio dati
        request.setAttribute("engine", engine);

        // 5️⃣ Forward
        request.getRequestDispatcher("/WEB-INF/views/engine/engine-detail.jsp")
                .forward(request, response);
    }

}
