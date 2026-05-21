package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.DashboardController;
import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import it.SimoSW.util.bootstrap.ApplicationInitializer;

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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/statistics")
public class StatisticsServlet extends HttpServlet {
    private static final LocalDate STATISTICS_START_DATE = LocalDate.of(2026, 1, 1);

    private DashboardController dashboardController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.dashboardController = initializer.getDashboardController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedUser = session != null ? (User) session.getAttribute("loggedUser") : null;
        UserRole role = loggedUser != null ? loggedUser.getRole() : null;
        if (role == null || !role.canAccessStatistics()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();
        LocalDate effectiveMonthStart = monthStart.isBefore(STATISTICS_START_DATE) ? STATISTICS_START_DATE : monthStart;

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ITALIAN);
        String meseCorrenteLabel = monthFormatter.format(today);
        if (!meseCorrenteLabel.isBlank()) {
            meseCorrenteLabel = Character.toUpperCase(meseCorrenteLabel.charAt(0)) + meseCorrenteLabel.substring(1);
        }

        int insertedThisMonth = dashboardController.getMotoriInseritiNelPeriodo(effectiveMonthStart, monthEnd);
        int deliveredThisMonth = dashboardController.getMotoriConsegnatiNelPeriodo(effectiveMonthStart, monthEnd);
        int inProgressNow = dashboardController.getWorkInProgressEngines();
        int avgDaysThisMonth = dashboardController.getTempoMedioLavorazioneNelPeriodo(effectiveMonthStart, monthEnd);

        YearMonth startMonth = YearMonth.from(STATISTICS_START_DATE);
        long monthsBetween = ChronoUnit.MONTHS.between(startMonth, currentMonth) + 1;
        int historyMonths = (int) Math.max(1, monthsBetween);
        List<Map<String, Object>> monthlyHistory = dashboardController.getStoricoMensileKpi(historyMonths);
        List<Map<String, Object>> userActions = dashboardController.getRecentUserActions(20);

        request.setAttribute("meseCorrenteLabel", meseCorrenteLabel);
        request.setAttribute("motoriInseritiMese", insertedThisMonth);
        request.setAttribute("motoriConsegnatiMese", deliveredThisMonth);
        request.setAttribute("motoriInLavorazione", inProgressNow);
        request.setAttribute("tempoMedioLavorazioneMese", avgDaysThisMonth);
        request.setAttribute("monthlyHistory", monthlyHistory);
        request.setAttribute("userActions", userActions);

        request.getRequestDispatcher("/WEB-INF/views/statistics/statistics.jsp").forward(request, response);
    }
}
