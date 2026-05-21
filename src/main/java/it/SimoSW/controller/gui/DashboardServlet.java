package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.DashboardController;
import it.SimoSW.model.User;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        User user = (User) session.getAttribute("loggedUser");

        // Recupero dati AGGREGATI
        request.setAttribute("loggedUser", user);

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALIAN);
        String meseCorrenteLabel = monthFormatter.format(today);
        if (!meseCorrenteLabel.isEmpty()) {
            meseCorrenteLabel = Character.toUpperCase(meseCorrenteLabel.charAt(0)) + meseCorrenteLabel.substring(1);
        }

        request.setAttribute("meseCorrenteLabel", meseCorrenteLabel);
        int motoriInseritiMese = dashboardController.getMotoriInseritiNelPeriodo(monthStart, monthEnd);
        int motoriConsegnatiMese = dashboardController.getMotoriConsegnatiNelPeriodo(monthStart, monthEnd);
        int tempoMedioLavorazioneMese = dashboardController.getTempoMedioLavorazioneNelPeriodo(monthStart, monthEnd);
        request.setAttribute("motoriInseritiMese", motoriInseritiMese);
        request.setAttribute("motoriConsegnatiMese", motoriConsegnatiMese);
        request.setAttribute("workInProgressEngines", dashboardController.getWorkInProgressEngines());
        request.setAttribute("tempoMedioLavorazioneMese", tempoMedioLavorazioneMese);
        request.setAttribute("tempoMedioDisponibile", motoriConsegnatiMese > 0);


        // Forward alla view
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}
