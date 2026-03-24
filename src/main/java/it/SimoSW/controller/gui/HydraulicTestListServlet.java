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
import java.util.List;

@WebServlet("/hydraulic-test/list")
public class HydraulicTestListServlet extends HttpServlet {

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

        String keyword = request.getParameter("keyword");

        List<HydraulicTest> hydraulicTests;
        if (keyword != null && !keyword.isBlank()) {
            hydraulicTests = hydraulicTestController.searchHydraulicTests(keyword);
            request.setAttribute("keyword", keyword.trim());
        } else {
            hydraulicTests = hydraulicTestController.getAllHydraulicTests();
        }

        request.setAttribute("hydraulicTests", hydraulicTests);
        request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-list.jsp")
                .forward(request, response);
    }
}
