package it.SimoSW.controller.app;

import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;
import it.SimoSW.model.UserActivityLog;
import it.SimoSW.model.User;
import it.SimoSW.model.WarehouseItem;
import it.SimoSW.model.WarehouseImage;
import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.Engine;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.model.dao.UserActivityLogDAO;
import it.SimoSW.model.dao.UserDAO;
import it.SimoSW.model.dao.WarehouseItemDAO;
import it.SimoSW.model.dao.WarehouseImageDAO;

import java.time.Duration;
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
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DashboardController {
    private static final LocalDate STATISTICS_START_DATE = LocalDate.of(2026, 1, 1);
    private static final Pattern STATUS_TRANSITION_PATTERN =
            Pattern.compile(".*->\\s*([A-Z_]+)\\s*$");

    private final EngineDAO engineDAO;
    private final CustomerDAO customerDAO;
    private final WarehouseItemDAO warehouseItemDAO;
    private final ImageDAO imageDAO;
    private final WarehouseImageDAO warehouseImageDAO;
    private final UserDAO userDAO;
    private final UserActivityLogDAO userActivityLogDAO;

    public DashboardController(EngineDAO engineDAO,
                               CustomerDAO customerDAO,
                               WarehouseItemDAO warehouseItemDAO,
                               ImageDAO imageDAO,
                               WarehouseImageDAO warehouseImageDAO,
                               UserDAO userDAO,
                               UserActivityLogDAO userActivityLogDAO) {
        this.engineDAO = engineDAO;
        this.customerDAO = customerDAO;
        this.warehouseItemDAO = warehouseItemDAO;
        this.imageDAO = imageDAO;
        this.warehouseImageDAO = warehouseImageDAO;
        this.userDAO = userDAO;
        this.userActivityLogDAO = userActivityLogDAO;
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
        return engineDAO.countByIntakeBetween(safeFrom, to);
    }

    public int getMotoriProntiNelPeriodo(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        LocalDate safeFrom = from.isBefore(STATISTICS_START_DATE) ? STATISTICS_START_DATE : from;
        if (safeFrom.isAfter(to)) {
            return 0;
        }
        return engineDAO.countByStatusAndIntakeBetween(EngineStatus.READY, safeFrom, to);
    }

    public int getTempoMedioLavorazioneNelPeriodo(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to non possono essere null");
        }
        LocalDate safeFrom = from.isBefore(STATISTICS_START_DATE) ? STATISTICS_START_DATE : from;
        if (safeFrom.isAfter(to)) {
            return 0;
        }
        return (int) Math.round(calculateAverageWorkInProgressDaysForDeliveredBetween(safeFrom, to));
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
        Map<YearMonth, Double> processingDaysSumByMonth = new HashMap<>();
        Map<YearMonth, Integer> processingDaysCountByMonth = new HashMap<>();

        List<Engine> allEngines = engineDAO.findAll();
        Map<String, List<StatusTransitionEvent>> transitionsByEngine = loadStatusTransitionsByEngineRef();
        for (Engine engine : allEngines) {
            YearMonth intakeMonth = YearMonth.from(engine.getIntakeDate());
            if (!intakeMonth.isBefore(firstMonth) && !intakeMonth.isAfter(currentMonth)) {
                insertedByMonth.merge(intakeMonth, 1, Integer::sum);
            }

            if (engine.getStatus() == EngineStatus.DELIVERED && engine.getDeliveryDate() != null) {
                YearMonth deliveryMonth = YearMonth.from(engine.getDeliveryDate());
                if (!deliveryMonth.isBefore(firstMonth) && !deliveryMonth.isAfter(currentMonth)) {
                    deliveredByMonth.merge(deliveryMonth, 1, Integer::sum);
                }
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (YearMonth ym = firstMonth; !ym.isAfter(currentMonth); ym = ym.plusMonths(1)) {
            MonthlyWorkInProgressAggregate aggregate = calculateMonthlyWorkInProgressAggregate(
                    ym,
                    allEngines,
                    transitionsByEngine,
                    now
            );
            if (aggregate.enginesWithOverlap() > 0) {
                processingDaysSumByMonth.put(ym, aggregate.totalDays());
                processingDaysCountByMonth.put(ym, aggregate.enginesWithOverlap());
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
                double totalDays = processingDaysSumByMonth.getOrDefault(ym, 0D);
                avgDays = (int) Math.round(totalDays / daysCount);
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

    private double calculateAverageWorkInProgressDaysForDeliveredBetween(LocalDate from, LocalDate to) {
        Map<String, List<StatusTransitionEvent>> transitionsByEngine = loadStatusTransitionsByEngineRef();
        double totalDays = 0;
        int countedEngines = 0;

        for (Engine engine : engineDAO.findAll()) {
            if (engine.getStatus() != EngineStatus.DELIVERED || engine.getDeliveryDate() == null) {
                continue;
            }
            if (engine.getDeliveryDate().isBefore(from) || engine.getDeliveryDate().isAfter(to)) {
                continue;
            }

            Optional<Double> workingDays = calculateWorkInProgressDays(
                    engine,
                    transitionsByEngine.getOrDefault(engine.getEngineRef(), List.of())
            );
            if (workingDays.isEmpty()) {
                continue;
            }

            totalDays += workingDays.get();
            countedEngines += 1;
        }

        return countedEngines == 0 ? 0 : totalDays / countedEngines;
    }

    private Map<String, List<StatusTransitionEvent>> loadStatusTransitionsByEngineRef() {
        Map<String, List<StatusTransitionEvent>> transitionsByEngine = new HashMap<>();
        List<UserActivityLog> logs = userActivityLogDAO.findByEntityTypeAndActionType("MOTOR", "STATUS_CHANGE");
        for (UserActivityLog log : logs) {
            if (log == null || log.getEntityId() == null || log.getEntityId().isBlank() || log.getCreatedAt() == null) {
                continue;
            }
            Optional<EngineStatus> toStatus = extractTargetStatus(log.getDescription());
            if (toStatus.isEmpty()) {
                continue;
            }
            transitionsByEngine
                    .computeIfAbsent(log.getEntityId(), ignored -> new ArrayList<>())
                    .add(new StatusTransitionEvent(log.getCreatedAt(), toStatus.get()));
        }
        return transitionsByEngine;
    }

    private static Optional<Double> calculateWorkInProgressDays(Engine engine, List<StatusTransitionEvent> transitions) {
        List<WorkInProgressInterval> intervals = buildWorkInProgressIntervals(engine, transitions);
        if (intervals.isEmpty()) {
            return Optional.empty();
        }

        Duration totalDuration = Duration.ZERO;
        boolean completedIntervalFound = false;

        for (WorkInProgressInterval interval : intervals) {
            if (interval.endExclusive() != null && !interval.endExclusive().isBefore(interval.startInclusive())) {
                totalDuration = totalDuration.plus(Duration.between(interval.startInclusive(), interval.endExclusive()));
                completedIntervalFound = true;
            }
        }

        if (!completedIntervalFound) {
            return Optional.empty();
        }

        return Optional.of(totalDuration.toMillis() / 86_400_000D);
    }

    private static MonthlyWorkInProgressAggregate calculateMonthlyWorkInProgressAggregate(YearMonth month,
                                                                                          List<Engine> engines,
                                                                                          Map<String, List<StatusTransitionEvent>> transitionsByEngine,
                                                                                          LocalDateTime now) {
        if (month == null || engines == null || transitionsByEngine == null || now == null) {
            return new MonthlyWorkInProgressAggregate(0D, 0);
        }

        LocalDateTime monthStart = month.atDay(1).atStartOfDay();
        LocalDateTime monthEndExclusive = month.plusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime openIntervalLimit = YearMonth.from(now).equals(month)
                ? minDateTime(now, monthEndExclusive)
                : monthEndExclusive;

        double totalDays = 0D;
        int enginesWithOverlap = 0;

        for (Engine engine : engines) {
            double engineDays = calculateOverlappingWorkInProgressDays(
                    buildWorkInProgressIntervals(engine, transitionsByEngine.getOrDefault(engine.getEngineRef(), List.of())),
                    monthStart,
                    monthEndExclusive,
                    openIntervalLimit
            );
            if (engineDays > 0D) {
                totalDays += engineDays;
                enginesWithOverlap += 1;
            }
        }

        return new MonthlyWorkInProgressAggregate(totalDays, enginesWithOverlap);
    }

    private static double calculateOverlappingWorkInProgressDays(List<WorkInProgressInterval> intervals,
                                                                 LocalDateTime periodStartInclusive,
                                                                 LocalDateTime periodEndExclusive,
                                                                 LocalDateTime openIntervalLimit) {
        if (intervals == null || intervals.isEmpty()) {
            return 0D;
        }

        Duration totalDuration = Duration.ZERO;
        for (WorkInProgressInterval interval : intervals) {
            LocalDateTime intervalEndExclusive = interval.endExclusive() != null ? interval.endExclusive() : openIntervalLimit;
            if (intervalEndExclusive == null) {
                continue;
            }
            LocalDateTime effectiveStart = maxDateTime(interval.startInclusive(), periodStartInclusive);
            LocalDateTime effectiveEnd = minDateTime(intervalEndExclusive, periodEndExclusive);
            if (effectiveEnd.isAfter(effectiveStart)) {
                totalDuration = totalDuration.plus(Duration.between(effectiveStart, effectiveEnd));
            }
        }

        return totalDuration.toMillis() / 86_400_000D;
    }

    private static List<WorkInProgressInterval> buildWorkInProgressIntervals(Engine engine, List<StatusTransitionEvent> transitions) {
        if (engine == null || transitions == null || transitions.isEmpty()) {
            return List.of();
        }

        List<StatusTransitionEvent> orderedTransitions = transitions.stream()
                .sorted(Comparator.comparing(StatusTransitionEvent::occurredAt))
                .toList();

        List<WorkInProgressInterval> intervals = new ArrayList<>();
        LocalDateTime openIntervalStart = null;
        EngineStatus currentStatus = null;

        for (StatusTransitionEvent transition : orderedTransitions) {
            EngineStatus nextStatus = transition.toStatus();
            if (nextStatus == currentStatus) {
                continue;
            }

            if (nextStatus == EngineStatus.WORK_IN_PROGRESS) {
                if (openIntervalStart == null) {
                    openIntervalStart = transition.occurredAt();
                }
            } else if (openIntervalStart != null && !transition.occurredAt().isBefore(openIntervalStart)) {
                intervals.add(new WorkInProgressInterval(openIntervalStart, transition.occurredAt()));
                openIntervalStart = null;
            }

            currentStatus = nextStatus;
        }

        if (openIntervalStart != null && engine.getStatus() == EngineStatus.WORK_IN_PROGRESS) {
            intervals.add(new WorkInProgressInterval(openIntervalStart, null));
        }

        return intervals;
    }

    private static LocalDateTime maxDateTime(LocalDateTime first, LocalDateTime second) {
        return first.isAfter(second) ? first : second;
    }

    private static LocalDateTime minDateTime(LocalDateTime first, LocalDateTime second) {
        return first.isBefore(second) ? first : second;
    }

    private static Optional<EngineStatus> extractTargetStatus(String description) {
        if (description == null || description.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = STATUS_TRANSITION_PATTERN.matcher(description.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            return Optional.of(EngineStatus.valueOf(matcher.group(1)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private record StatusTransitionEvent(LocalDateTime occurredAt, EngineStatus toStatus) {
    }

    private record WorkInProgressInterval(LocalDateTime startInclusive, LocalDateTime endExclusive) {
    }

    private record MonthlyWorkInProgressAggregate(double totalDays, int enginesWithOverlap) {
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
