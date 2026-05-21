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
import java.util.ArrayList;
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
        if (role != UserRole.ADMIN) {
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
        int readyNow = dashboardController.getMotoriByStatus(it.SimoSW.model.EngineStatus.READY);
        int waitingNow = dashboardController.getMotoriByStatus(it.SimoSW.model.EngineStatus.WAITING);
        int deliveredNow = dashboardController.getMotoriByStatus(it.SimoSW.model.EngineStatus.DELIVERED);
        int totalEngines = waitingNow + inProgressNow + readyNow + deliveredNow;
        int avgDaysThisMonth = dashboardController.getTempoMedioLavorazioneNelPeriodo(effectiveMonthStart, monthEnd);
        int avgDaysOverall = dashboardController.getTempoMedioLavorazioneNelPeriodo(STATISTICS_START_DATE, today);

        YearMonth startMonth = YearMonth.from(STATISTICS_START_DATE);
        long monthsBetween = ChronoUnit.MONTHS.between(startMonth, currentMonth) + 1;
        int maxHistoryMonths = (int) Math.max(1, monthsBetween);
        int selectedMonths = parseMonths(request.getParameter("months"), Math.min(12, maxHistoryMonths), maxHistoryMonths);
        List<Map<String, Object>> monthlyHistory = dashboardController.getStoricoMensileKpi(selectedMonths);
        String fromParam = safeTrim(request.getParameter("fromMonth"));
        String toParam = safeTrim(request.getParameter("toMonth"));
        if (!fromParam.isBlank() && !toParam.isBlank()) {
            monthlyHistory = filterHistoryByMonthRange(monthlyHistory, fromParam, toParam);
        }
        List<Map<String, Object>> userActions = dashboardController.getRecentUserActions(20);
        Forecast forecast = buildForecast(monthlyHistory);

        request.setAttribute("meseCorrenteLabel", meseCorrenteLabel);
        request.setAttribute("motoriTotali", totalEngines);
        request.setAttribute("motoriInseritiMese", insertedThisMonth);
        request.setAttribute("motoriConsegnatiMese", deliveredThisMonth);
        request.setAttribute("motoriInLavorazione", inProgressNow);
        request.setAttribute("motoriPronti", readyNow);
        request.setAttribute("motoriInAttesa", waitingNow);
        request.setAttribute("motoriConsegnatiTotali", deliveredNow);
        request.setAttribute("tempoMedioLavorazione", avgDaysOverall);
        request.setAttribute("tempoMedioLavorazioneMese", avgDaysThisMonth);
        request.setAttribute("monthlyHistory", monthlyHistory);
        request.setAttribute("userActions", userActions);
        request.setAttribute("selectedMonths", selectedMonths);
        request.setAttribute("fromMonth", fromParam);
        request.setAttribute("toMonth", toParam);
        request.setAttribute("forecastInsertedNextMonth", forecast.insertedForecast);
        request.setAttribute("forecastDeliveredNextMonth", forecast.deliveredForecast);
        request.setAttribute("forecastAvgDaysNextMonth", forecast.avgDaysForecast);
        request.setAttribute("forecastHasEnoughData", forecast.hasEnoughData);

        request.getRequestDispatcher("/WEB-INF/views/statistics/statistics.jsp").forward(request, response);
    }

    private static int parseMonths(String raw, int defaultValue, int maxValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            if (parsed < 1) {
                return defaultValue;
            }
            return Math.min(parsed, maxValue);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static List<Map<String, Object>> filterHistoryByMonthRange(List<Map<String, Object>> rows, String fromMonth, String toMonth) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        if (fromMonth.compareTo(toMonth) > 0) {
            return rows;
        }
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String monthKey = String.valueOf(row.getOrDefault("monthKey", ""));
            if (monthKey.isBlank()) {
                continue;
            }
            if (monthKey.compareTo(fromMonth) >= 0 && monthKey.compareTo(toMonth) <= 0) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static Forecast buildForecast(List<Map<String, Object>> rows) {
        if (rows == null || rows.size() < 3) {
            return new Forecast(false, 0, 0, 0);
        }
        int count = 0;
        double insertedSum = 0;
        double deliveredSum = 0;
        double avgDaysSum = 0;

        for (int i = rows.size() - 1; i >= 0 && count < 3; i--) {
            Map<String, Object> row = rows.get(i);
            insertedSum += toInt(row.get("inserted"));
            deliveredSum += toInt(row.get("delivered"));
            avgDaysSum += toInt(row.get("avgDays"));
            count += 1;
        }

        if (count < 3) {
            return new Forecast(false, 0, 0, 0);
        }

        int insertedForecast = (int) Math.round(insertedSum / 3.0);
        int deliveredForecast = (int) Math.round(deliveredSum / 3.0);
        int avgDaysForecast = (int) Math.round(avgDaysSum / 3.0);
        return new Forecast(true, insertedForecast, deliveredForecast, avgDaysForecast);
    }

    private static int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return 0;
        }
    }

    private static final class Forecast {
        private final boolean hasEnoughData;
        private final int insertedForecast;
        private final int deliveredForecast;
        private final int avgDaysForecast;

        private Forecast(boolean hasEnoughData, int insertedForecast, int deliveredForecast, int avgDaysForecast) {
            this.hasEnoughData = hasEnoughData;
            this.insertedForecast = insertedForecast;
            this.deliveredForecast = deliveredForecast;
            this.avgDaysForecast = avgDaysForecast;
        }
    }
}
