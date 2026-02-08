package it.SimoSW.controller.gui;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Recupera la sessione se esiste (senza crearne una nuova)
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // distrugge completamente la sessione
        }

        // Redirect alla landing page
        response.sendRedirect(request.getContextPath() + "/");
    }

    /**
     * Opzionale: se qualcuno prova ad accedere via GET,
     * lo trattiamo come un POST per sicurezza.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}