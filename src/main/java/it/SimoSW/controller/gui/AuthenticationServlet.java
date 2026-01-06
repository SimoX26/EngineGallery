package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.AuthenticationController;
import it.SimoSW.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {

    private AuthenticationController authenticationController;

    @Override
    public void init() {
        this.authenticationController = (AuthenticationController) getServletContext().getAttribute("authenticationController");
    }

    /* =========================
       GET: login / logout
       ========================= */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        // Logout
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Mostra pagina di login
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    /* =========================
       POST: login
       ========================= */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Optional<User> user = authenticationController.login(username, password);

        if (user.isPresent()) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user.get());

            // Redirect alla galleria dopo login
            response.sendRedirect(request.getContextPath() + "/gallery");
        } else {
            // Credenziali errate
            request.setAttribute("error", "Credenziali non valide");
            request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
        }
    }
}
