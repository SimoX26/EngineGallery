package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.DashboardController;
import it.SimoSW.controller.app.CustomerController;
import it.SimoSW.controller.app.EngineController;
import it.SimoSW.controller.app.HydraulicTestController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.HydraulicTest;
import it.SimoSW.model.WarehouseItem;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private DashboardController dashboardController;
    private EngineController engineController;
    private CustomerController customerController;
    private HydraulicTestController hydraulicTestController;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.dashboardController = initializer.getDashboardController();
        this.engineController = initializer.getEngineController();
        this.customerController = initializer.getCustomerController();
        this.hydraulicTestController = initializer.getHydraulicTestController();
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
        request.setAttribute("motoriTotali", engineController.getAllEngines().size());
        request.setAttribute("motoriInAttesa", dashboardController.getMotoriByStatus(EngineStatus.WAITING));
        request.setAttribute("workInProgressEngines", dashboardController.getWorkInProgressEngines());
        request.setAttribute("motoriPronti", dashboardController.getMotoriByStatus(EngineStatus.READY));
        request.setAttribute("motoriConsegnatiTotali", dashboardController.getMotoriByStatus(EngineStatus.DELIVERED));
        request.setAttribute("motoriInseritiMese", dashboardController.getMotoriInseritiNelPeriodo(monthStart, monthEnd));
        request.setAttribute("motoriConsegnatiMese", dashboardController.getMotoriConsegnatiNelPeriodo(monthStart, monthEnd));
        request.setAttribute("tempoMedioLavorazioneMese", dashboardController.getTempoMedioLavorazioneNelPeriodo(monthStart, monthEnd));
        request.setAttribute("clientiTotali", customerController.findAll().size());
        request.setAttribute("articoliMagazzinoTotali", dashboardController.getWarehouseItemCount());
        request.setAttribute("quantitaMagazzinoTotale", dashboardController.getWarehouseTotalQuantity());
        request.setAttribute("articoliEsauriti", dashboardController.getWarehouseOutOfStockCount());

        List<Engine> ultimiMotori = dashboardController.listaUltimiMotori(8);
        request.setAttribute("ultimiMotori", ultimiMotori);

        List<WarehouseItem> ultimiArticoliMagazzino = dashboardController.listaUltimiArticoliMagazzino(6);
        request.setAttribute("ultimiArticoliMagazzino", ultimiArticoliMagazzino);
        List<HydraulicTest> allHydraulicTests = hydraulicTestController.getAllHydraulicTests();
        List<HydraulicTest> recentHydraulicTests = allHydraulicTests.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) {
                        return 0;
                    }
                    if (a.getCreatedAt() == null) {
                        return 1;
                    }
                    if (b.getCreatedAt() == null) {
                        return -1;
                    }
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(6)
                .toList();
        request.setAttribute("proveIdraulicheTotali", allHydraulicTests.size());
        request.setAttribute("proveIdraulicheRecenti", recentHydraulicTests);
        request.setAttribute("clientiRecenti", customerController.findAll().stream().limit(6).toList());

        Map<Long, String> coverImages = new HashMap<>();
        for (Engine engine : ultimiMotori) {
            engineController
                    .getCoverFilenameForEngine(engine.getId())
                    .ifPresent(filename -> coverImages.put(engine.getId(), filename));
        }
        request.setAttribute("coverImages", coverImages);

        Map<Long, String> customerNames = new HashMap<>();
        for (Engine engine : ultimiMotori) {
            long customerId = engine.getCustomerId();
            if (!customerNames.containsKey(customerId)) {
                customerNames.put(customerId, customerController.findNameById(customerId));
            }
        }
        request.setAttribute("customerNames", customerNames);


        // Forward alla view
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp");
        dispatcher.forward(request, response);
    }
}
