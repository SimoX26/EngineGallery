package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.UserActivityLogController;
import it.SimoSW.model.User;
import it.SimoSW.model.UserActivityLog;
import it.SimoSW.model.UserRole;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/maintenance")
public class MaintenanceServlet extends HttpServlet {
    private UserActivityLogController userActivityLogController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.userActivityLogController = initializer.getUserActivityLogController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedUser = session != null ? (User) session.getAttribute("loggedUser") : null;
        UserRole role = loggedUser != null ? loggedUser.getRole() : null;

        if (role != UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        java.util.List<UserActivityLog> recentLogs = userActivityLogController.getRecentLogs(100);
        request.setAttribute("recentActivityLogs", recentLogs);
        request.getRequestDispatcher("/WEB-INF/views/maintenance/maintenance.jsp").forward(request, response);
    }
}
