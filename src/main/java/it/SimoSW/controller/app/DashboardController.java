package it.SimoSW.controller.app;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.User;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.WarehouseImage;
import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.Engine;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.model.dao.UserDAO;
import it.SimoSW.model.dao.WarehouseItemDAO;
import it.SimoSW.model.dao.WarehouseImageDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.format.DateTimeFormatter;

public class DashboardController {
    private static final LocalDate STATISTICS_START_DATE = LocalDate.of(2026, 1, 1);

    private final EngineDAO engineDAO;
    private final CustomerDAO customerDAO;
    private final WarehouseItemDAO warehouseItemDAO;
    private final ImageDAO imageDAO;
    private final WarehouseImageDAO warehouseImageDAO;
    private final UserDAO userDAO;

    public DashboardController(EngineDAO engineDAO,
                               CustomerDAO customerDAO,
                               WarehouseItemDAO warehouseItemDAO,
                               ImageDAO imageDAO,
                               WarehouseImageDAO warehouseImageDAO,
                               UserDAO userDAO) {
        this.engineDAO = engineDAO;
        this.customerDAO = customerDAO;
        this.warehouseItemDAO = warehouseItemDAO;
        this.imageDAO = imageDAO;
        this.warehouseImageDAO = warehouseImageDAO;
        this.userDAO = userDAO;
    }

    /* =========================
       KPI Clienti
       ========================= */

    public int getClientiConMotoriInOfficina() {
        // questo metodo deve esistere nel CustomerDAO (o lo aggiorniamo dopo)
        return customerDAO.countClientiConMotoriInOfficina();
    }

    /* =========================
       KPI Motori
       ========================= */

    public int getMotoriInOfficina() {
        return engineDAO.countInWorkshop();
    }

    public int getWorkInProgressEngines() {
        return engineDAO.countByStatus(EngineStatus.WORK_IN_PROGRESS);
    }

    public int getMotoriByStatus(EngineStatus status) {
        if (status == null) {
            return 0;
        }
        return engineDAO.countByStatus(status);
    }

    public int getMotoriConsegnatiUltimaSettimana() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(7);
        return engineDAO.countDeliveredBetween(from, to);
    }

    public int getClientiServitiNelPeriodo(LocalDate from, LocalDate to) {
        return engineDAO.countDistinctCustomersDeliveredBetween(from, to);
    }

    public int getMotoriConsegnatiNelPeriodo(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        LocalDate safeFrom = from.isBefore(STATISTICS_START_DATE) ? STATISTICS_START_DATE : from;
        if (safeFrom.isAfter(to)) {
            return 0;
        }
        return engineDAO.countDeliveredBetween(safeFrom, to);
    }

    public int getMotoriInseritiNelPeriodo(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        LocalDate safeFrom = from.isBefore(STATISTICS_START_DATE) ? STATISTICS_START_DATE : from;
        if (safeFrom.isAfter(to)) {
            return 0;
        }
        return (int) engineDAO.findAll().stream()
                .filter(engine -> !engine.getIntakeDate().isBefore(safeFrom) && !engine.getIntakeDate().isAfter(to))
                .count();
    }

    public int getTempoMedioLavorazioneNelPeriodo(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        LocalDate safeFrom = from.isBefore(STATISTICS_START_DATE) ? STATISTICS_START_DATE : from;
        if (safeFrom.isAfter(to)) {
            return 0;
        }
        return (int) Math.round(engineDAO.averageProcessingDaysForDeliveredBetween(safeFrom, to));
    }

    public List<Map<String, Object>> getStoricoMensileKpi(int months) {
        int safeMonths = Math.max(1, months);
        YearMonth currentMonth = YearMonth.now();
        YearMonth requestedFirstMonth = currentMonth.minusMonths(safeMonths - 1L);
        YearMonth startMonth = YearMonth.from(STATISTICS_START_DATE);
        YearMonth firstMonth = requestedFirstMonth.isBefore(startMonth) ? startMonth : requestedFirstMonth;
        if (firstMonth.isAfter(currentMonth)) {
            return List.of();
        }

        Map<YearMonth, Integer> insertedByMonth = new HashMap<>();
        Map<YearMonth, Integer> deliveredByMonth = new HashMap<>();
        Map<YearMonth, Long> processingDaysSumByMonth = new HashMap<>();
        Map<YearMonth, Integer> processingDaysCountByMonth = new HashMap<>();

        List<Engine> allEngines = engineDAO.findAll();
        for (Engine engine : allEngines) {
            YearMonth intakeMonth = YearMonth.from(engine.getIntakeDate());
            if (!intakeMonth.isBefore(firstMonth) && !intakeMonth.isAfter(currentMonth)) {
                insertedByMonth.merge(intakeMonth, 1, Integer::sum);
            }

            if (engine.getStatus() == EngineStatus.DELIVERED
                    && engine.getDeliveryDate() != null
                    && !engine.getDeliveryDate().isBefore(engine.getIntakeDate())) {
                YearMonth deliveryMonth = YearMonth.from(engine.getDeliveryDate());
                if (!deliveryMonth.isBefore(firstMonth) && !deliveryMonth.isAfter(currentMonth)) {
                    deliveredByMonth.merge(deliveryMonth, 1, Integer::sum);
                    long days = ChronoUnit.DAYS.between(engine.getIntakeDate(), engine.getDeliveryDate());
                    processingDaysSumByMonth.merge(deliveryMonth, days, Long::sum);
                    processingDaysCountByMonth.merge(deliveryMonth, 1, Integer::sum);
                }
            }
        }

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ITALIAN);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (int i = 0; i < safeMonths; i++) {
            YearMonth ym = firstMonth.plusMonths(i);
            int inserted = insertedByMonth.getOrDefault(ym, 0);
            int delivered = deliveredByMonth.getOrDefault(ym, 0);
            int avgDays = 0;
            int inProgress = 0;

            int daysCount = processingDaysCountByMonth.getOrDefault(ym, 0);
            if (daysCount > 0) {
                long totalDays = processingDaysSumByMonth.getOrDefault(ym, 0L);
                avgDays = (int) Math.round((double) totalDays / daysCount);
            }

            LocalDate monthEnd = ym.atEndOfMonth();
            for (Engine engine : allEngines) {
                boolean enteredBeforeEnd = !engine.getIntakeDate().isAfter(monthEnd);
                boolean deliveredByMonthEnd = engine.getDeliveryDate() != null
                        && !engine.getDeliveryDate().isAfter(monthEnd);
                if (enteredBeforeEnd && !deliveredByMonthEnd) {
                    inProgress += 1;
                }
            }

            String label = monthFormatter.format(ym.atDay(1));
            if (!label.isBlank()) {
                label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            }

            Map<String, Object> row = new HashMap<>();
            row.put("monthLabel", label);
            row.put("monthKey", ym.toString());
            row.put("inserted", inserted);
            row.put("delivered", delivered);
            row.put("inProgress", inProgress);
            row.put("avgDays", avgDays);
            rows.add(row);
        }

        return rows;
    }

    public List<Map<String, Object>> getRecentUserActions(int limit) {
        int safeLimit = Math.max(1, limit);
        DateTimeFormatter actionTsFormatter = DateTimeFormatter.ofPattern("dd / MM / yyyy HH:mm");

        List<Map<String, Object>> actions = new ArrayList<>();

        for (Image image : imageDAO.findLatest(safeLimit)) {
            if (image.getUploadedBy() == null || image.getUploadDate() == null) {
                continue;
            }

            String username = resolveUsername(image.getUploadedBy());
            String entity = engineDAO.findById(image.getEngineId())
                    .map(engine -> engine.getEngineRef())
                    .orElse("ID " + image.getEngineId());

            Map<String, Object> action = new HashMap<>();
            action.put("username", username);
            action.put("action", "Upload immagine motore");
            action.put("timestamp", image.getUploadDate());
            action.put("timestampLabel", actionTsFormatter.format(image.getUploadDate()));
            action.put("entity", "Motore " + entity);
            action.put("description", image.getFilename());
            actions.add(action);
        }

        for (WarehouseImage image : warehouseImageDAO.findLatest(safeLimit)) {
            if (image.getUploadedBy() == null || image.getUploadDate() == null) {
                continue;
            }

            String username = resolveUsername(image.getUploadedBy());
            String entity = warehouseItemDAO.findById(image.getWarehouseItemId())
                    .map(WarehouseItem::getName)
                    .orElse("ID " + image.getWarehouseItemId());

            Map<String, Object> action = new HashMap<>();
            action.put("username", username);
            action.put("action", "Upload immagine magazzino");
            action.put("timestamp", image.getUploadDate());
            action.put("timestampLabel", actionTsFormatter.format(image.getUploadDate()));
            action.put("entity", "Articolo " + entity);
            action.put("description", image.getFilename());
            actions.add(action);
        }

        actions.sort(Comparator.<Map<String, Object>, LocalDateTime>comparing(
                row -> (LocalDateTime) row.get("timestamp"),
                Comparator.nullsLast(Comparator.naturalOrder())
        ).reversed());

        if (actions.size() > safeLimit) {
            return new ArrayList<>(actions.subList(0, safeLimit));
        }

        return actions;
    }

    private String resolveUsername(Long userId) {
        if (userId == null) {
            return "Utente sconosciuto";
        }
        User user = userDAO.findById(userId);
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return "Utente #" + userId;
        }
        return user.getUsername();
    }

    /* =========================
       Lista ultimi motori (TODO)
       ========================= */
    public List<Engine> listaUltimiMotori(int limit) {
        return engineDAO.findLatest(limit);
    }

    /* =========================
       KPI Magazzino
       ========================= */

    public int getWarehouseItemCount() {
        return warehouseItemDAO.countAll();
    }

    public int getWarehouseTotalQuantity() {
        return warehouseItemDAO.sumTotalQuantity();
    }

    public int getWarehouseOutOfStockCount() {
        return warehouseItemDAO.countOutOfStock();
    }

    public List<WarehouseItem> listaUltimiArticoliMagazzino(int limit) {
        return warehouseItemDAO.findLatest(limit);
    }
}
