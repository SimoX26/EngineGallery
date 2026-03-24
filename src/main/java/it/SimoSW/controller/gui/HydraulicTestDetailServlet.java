package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.HydraulicTestController;
import it.SimoSW.model.HydraulicTest;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/hydraulic-test/detail")
public class HydraulicTestDetailServlet extends HttpServlet {

    private HydraulicTestController hydraulicTestController;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.hydraulicTestController = initializer.getHydraulicTestController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
            return;
        }

        long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException ex) {
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
            return;
        }

        Optional<HydraulicTest> hydraulicTestOpt = hydraulicTestController.findHydraulicTestById(id);
        if (hydraulicTestOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list");
            return;
        }

        request.setAttribute("hydraulicTest", hydraulicTestOpt.get());
        request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-detail.jsp")
                .forward(request, response);
    }
}
