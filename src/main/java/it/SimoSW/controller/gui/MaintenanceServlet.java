package it.SimoSW.controller.gui;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/maintenance")
public class MaintenanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedUser = session != null ? (User) session.getAttribute("loggedUser") : null;
        UserRole role = loggedUser != null ? loggedUser.getRole() : null;

        if (role != UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/maintenance/maintenance.jsp").forward(request, response);
    }
}

