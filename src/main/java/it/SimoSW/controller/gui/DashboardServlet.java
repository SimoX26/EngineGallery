package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.DashboardController;
import it.SimoSW.model.User;
import it.SimoSW.util.bean.UserSessionBean;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private DashboardController dashboardController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.dashboardController = initializer.getDashboardController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Controllo login
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("loggedUser");

        // Recupero dati AGGREGATI
        request.setAttribute("loggedUser", user);

        request.setAttribute("clientiConMotoriAttivi", dashboardController.getClientiConMotoriInOfficina());

        request.setAttribute("motoriInOfficina", dashboardController.getMotoriInOfficina());

        request.setAttribute("workInProgressEngines", dashboardController.getWorkInProgressEngines());

        request.setAttribute("motoriConsegnatiUltimaSettimana", dashboardController.getMotoriConsegnatiUltimaSettimana());


       // request.setAttribute("ultimiMotori", dashboardController.listaUtlimiMotori());


        // Forward alla view
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}