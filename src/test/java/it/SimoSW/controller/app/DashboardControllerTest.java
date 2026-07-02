package it.SimoSW.controller.app;

import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.UserActivityLog;
import it.SimoSW.model.dao.CustomerDAO;
import it.SimoSW.model.dao.EngineDAO;
import it.SimoSW.model.dao.ImageDAO;
import it.SimoSW.model.dao.UserActivityLogDAO;
import it.SimoSW.model.dao.UserDAO;
import it.SimoSW.model.dao.WarehouseImageDAO;
import it.SimoSW.model.dao.WarehouseItemDAO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardControllerTest {

    @Test
    void singleCompleteWorkIntervalIsAveragedCorrectly() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00001", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5))),
                List.of(
                        statusChange("RML-2026-00001", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 2, 2, 10, 0)),
                        statusChange("RML-2026-00001", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 2, 4, 10, 0))
                )
        );

        assertEquals(2, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        ));
    }

    @Test
    void engineStillInProgressIsExcludedFromDeliveredAverage() {
        DashboardController controller = controllerWith(
                List.of(inProgressEngine("RML-2026-00002", LocalDate.of(2026, 2, 1))),
                List.of(
                        statusChange("RML-2026-00002", "INITIAL -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 2, 1, 9, 0))
                )
        );

        assertEquals(0, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        ));
    }

    @Test
    void multipleEntriesAndExitsAreSummedPerEngine() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00003", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10))),
                List.of(
                        statusChange("RML-2026-00003", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 1, 8, 0)),
                        statusChange("RML-2026-00003", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 2, 8, 0)),
                        statusChange("RML-2026-00003", "READY -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 3, 8, 0)),
                        statusChange("RML-2026-00003", "WORK_IN_PROGRESS -> DELIVERED", LocalDateTime.of(2026, 3, 5, 8, 0))
                )
        );

        assertEquals(3, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        ));
    }

    @Test
    void engineNeverEnteredWorkInProgressDoesNotAffectAverage() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00004", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5))),
                List.of(
                        statusChange("RML-2026-00004", "WAITING -> READY", LocalDateTime.of(2026, 3, 2, 8, 0)),
                        statusChange("RML-2026-00004", "READY -> DELIVERED", LocalDateTime.of(2026, 3, 5, 8, 0))
                )
        );

        assertEquals(0, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        ));
    }

    @Test
    void duplicateAndOutOfOrderTransitionsDoNotDoubleCount() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00005", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 10))),
                List.of(
                        statusChange("RML-2026-00005", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 4, 5, 8, 0)),
                        statusChange("RML-2026-00005", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 4, 3, 8, 0)),
                        statusChange("RML-2026-00005", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 4, 3, 9, 0))
                )
        );

        assertEquals(2, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        ));
    }

    @Test
    void intervalCrossingPeriodBoundaryKeepsFullDurationForDeliveredMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00006", LocalDate.of(2026, 2, 20), LocalDate.of(2026, 3, 2))),
                List.of(
                        statusChange("RML-2026-00006", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 2, 28, 12, 0)),
                        statusChange("RML-2026-00006", "WORK_IN_PROGRESS -> DELIVERED", LocalDateTime.of(2026, 3, 2, 12, 0))
                )
        );

        assertEquals(2, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        ));
    }

    @Test
    void multipleEnginesUseAverageOfPerEngineDurations() {
        DashboardController controller = controllerWith(
                List.of(
                        deliveredEngine("RML-2026-00007", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 5)),
                        deliveredEngine("RML-2026-00008", LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 8))
                ),
                List.of(
                        statusChange("RML-2026-00007", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 5, 1, 8, 0)),
                        statusChange("RML-2026-00007", "WORK_IN_PROGRESS -> DELIVERED", LocalDateTime.of(2026, 5, 3, 8, 0)),
                        statusChange("RML-2026-00008", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 5, 2, 8, 0)),
                        statusChange("RML-2026-00008", "WORK_IN_PROGRESS -> DELIVERED", LocalDateTime.of(2026, 5, 6, 8, 0))
                )
        );

        assertEquals(3, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        ));
    }

    @Test
    void crossingDayBoundaryUsesTransitionTimestamps() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00009", LocalDate.of(2026, 5, 30), LocalDate.of(2026, 6, 2))),
                List.of(
                        statusChange("RML-2026-00009", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 5, 31, 23, 0)),
                        statusChange("RML-2026-00009", "WORK_IN_PROGRESS -> DELIVERED", LocalDateTime.of(2026, 6, 2, 23, 0))
                )
        );

        assertEquals(2, controller.getTempoMedioLavorazioneNelPeriodo(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30)
        ));
    }

    @Test
    void monthlyHistoryUsesSameAverageForDeliveryMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00010", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 5))),
                List.of(
                        statusChange("RML-2026-00010", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 6, 2, 9, 0)),
                        statusChange("RML-2026-00010", "WORK_IN_PROGRESS -> DELIVERED", LocalDateTime.of(2026, 6, 4, 9, 0))
                )
        );

        Map<String, Object> juneRow = controller.getStoricoMensileKpi(monthsIncluding(YearMonth.of(2026, 6))).stream()
                .filter(row -> "2026-06".equals(row.get("monthKey")))
                .findFirst()
                .orElseThrow();

        assertEquals(2, juneRow.get("avgDays"));
    }

    @Test
    void monthlyHistoryCountsIntervalFullyContainedInPastMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00011", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 10))),
                List.of(
                        statusChange("RML-2026-00011", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 10, 8, 0)),
                        statusChange("RML-2026-00011", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 12, 8, 0))
                )
        );

        assertEquals(2, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryCountsIntervalStartedPreviousMonthAndEndedInSelectedMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00012", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 10))),
                List.of(
                        statusChange("RML-2026-00012", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 2, 28, 12, 0)),
                        statusChange("RML-2026-00012", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 2, 12, 0))
                )
        );

        assertEquals(2, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryCountsIntervalStartedInSelectedMonthAndEndedInNextMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00013", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 10))),
                List.of(
                        statusChange("RML-2026-00013", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 30, 0, 0)),
                        statusChange("RML-2026-00013", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 4, 2, 0, 0))
                )
        );

        assertEquals(2, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryCountsIntervalCoveringEntireMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00014", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1))),
                List.of(
                        statusChange("RML-2026-00014", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 2, 15, 0, 0)),
                        statusChange("RML-2026-00014", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 4, 15, 0, 0))
                )
        );

        assertEquals(31, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryCountsHistoricalIntervalEvenIfEngineIsNoLongerInProgress() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00015", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1))),
                List.of(
                        statusChange("RML-2026-00015", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 5, 8, 0)),
                        statusChange("RML-2026-00015", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 7, 8, 0)),
                        statusChange("RML-2026-00015", "READY -> DELIVERED", LocalDateTime.of(2026, 5, 1, 8, 0))
                )
        );

        assertEquals(2, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryLimitsOpenIntervalToEndOfPastMonth() {
        DashboardController controller = controllerWith(
                List.of(inProgressEngine("RML-2026-00016", LocalDate.of(2026, 2, 1))),
                List.of(
                        statusChange("RML-2026-00016", "INITIAL -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 20, 0, 0))
                )
        );

        assertEquals(12, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistorySumsMultipleIntervalsWithinSameMonth() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00017", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1))),
                List.of(
                        statusChange("RML-2026-00017", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 1, 0, 0)),
                        statusChange("RML-2026-00017", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 3, 0, 0)),
                        statusChange("RML-2026-00017", "READY -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 10, 0, 0)),
                        statusChange("RML-2026-00017", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 12, 0, 0))
                )
        );

        assertEquals(4, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryReturnsZeroWhenThereIsNoOverlap() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00018", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1))),
                List.of(
                        statusChange("RML-2026-00018", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 2, 1, 0, 0)),
                        statusChange("RML-2026-00018", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 2, 3, 0, 0))
                )
        );

        assertEquals(0, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryHandlesExactMonthBoundaries() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00019", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1))),
                List.of(
                        statusChange("RML-2026-00019", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 1, 0, 0)),
                        statusChange("RML-2026-00019", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 4, 1, 0, 0))
                )
        );

        assertEquals(31, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthlyHistoryAveragesKnownDurationsAcrossMultipleMotors() {
        DashboardController controller = controllerWith(
                List.of(
                        deliveredEngine("RML-2026-00020", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1)),
                        deliveredEngine("RML-2026-00021", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 1))
                ),
                List.of(
                        statusChange("RML-2026-00020", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 1, 0, 0)),
                        statusChange("RML-2026-00020", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 3, 0, 0)),
                        statusChange("RML-2026-00021", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 3, 10, 0, 0)),
                        statusChange("RML-2026-00021", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 3, 14, 0, 0))
                )
        );

        assertEquals(3, monthlyAvgDays(controller, YearMonth.of(2026, 3)));
    }

    @Test
    void monthWithInsufficientHistoryReturnsNullInsteadOfZero() {
        DashboardController controller = controllerWith(
                List.of(deliveredEngine("RML-2026-00022", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 4, 10))),
                List.of(
                        statusChange("RML-2026-00022", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 5, 23, 10, 0)),
                        statusChange("RML-2026-00022", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 5, 25, 10, 0))
                )
        );

        assertEquals(null, monthlyAvgDaysValue(controller, YearMonth.of(2026, 4)));
    }

    @Test
    void monthValueRemainsAlignedWithItsMonthKeyWhenHistoryContainsNullAndNumbers() {
        DashboardController controller = controllerWith(
                List.of(
                        deliveredEngine("RML-2026-00023", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10)),
                        deliveredEngine("RML-2026-00024", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 6, 10))
                ),
                List.of(
                        statusChange("RML-2026-00023", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 5, 23, 10, 0)),
                        statusChange("RML-2026-00023", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 5, 25, 10, 0)),
                        statusChange("RML-2026-00024", "WAITING -> WORK_IN_PROGRESS", LocalDateTime.of(2026, 6, 1, 0, 0)),
                        statusChange("RML-2026-00024", "WORK_IN_PROGRESS -> READY", LocalDateTime.of(2026, 6, 4, 0, 0))
                )
        );

        List<Map<String, Object>> rows = controller.getStoricoMensileKpi(monthsIncluding(YearMonth.of(2026, 5)));
        Map<String, Object> mayRow = rows.stream()
                .filter(row -> "2026-05".equals(row.get("monthKey")))
                .findFirst()
                .orElseThrow();
        Map<String, Object> juneRow = rows.stream()
                .filter(row -> "2026-06".equals(row.get("monthKey")))
                .findFirst()
                .orElseThrow();

        assertEquals(2, mayRow.get("avgDays"));
        assertEquals(3, juneRow.get("avgDays"));
    }

    private static DashboardController controllerWith(List<Engine> engines, List<UserActivityLog> logs) {
        return new DashboardController(
                new FakeEngineDAO(engines),
                unsupportedDao(CustomerDAO.class),
                unsupportedDao(WarehouseItemDAO.class),
                unsupportedDao(ImageDAO.class),
                unsupportedDao(WarehouseImageDAO.class),
                unsupportedDao(UserDAO.class),
                new FakeUserActivityLogDAO(logs)
        );
    }

    private static int monthsIncluding(YearMonth targetMonth) {
        YearMonth currentMonth = YearMonth.now();
        return Math.max(1, (int) (java.time.temporal.ChronoUnit.MONTHS.between(targetMonth, currentMonth) + 1));
    }

    private static int monthlyAvgDays(DashboardController controller, YearMonth month) {
        return ((Number) monthlyAvgDaysValue(controller, month)).intValue();
    }

    private static Object monthlyAvgDaysValue(DashboardController controller, YearMonth month) {
        return controller.getStoricoMensileKpi(monthsIncluding(month)).stream()
                .filter(row -> month.toString().equals(row.get("monthKey")))
                .findFirst()
                .orElseThrow()
                .get("avgDays");
    }

    private static Engine deliveredEngine(String ref, LocalDate intakeDate, LocalDate deliveryDate) {
        return new Engine(1L, ref, "CODE-" + ref, 1L, intakeDate, EngineStatus.DELIVERED, deliveryDate, "");
    }

    private static Engine inProgressEngine(String ref, LocalDate intakeDate) {
        return new Engine(1L, ref, "CODE-" + ref, 1L, intakeDate, EngineStatus.WORK_IN_PROGRESS, null, "");
    }

    private static UserActivityLog statusChange(String engineRef, String transition, LocalDateTime occurredAt) {
        UserActivityLog log = new UserActivityLog();
        log.setEntityType("MOTOR");
        log.setActionType("STATUS_CHANGE");
        log.setEntityId(engineRef);
        log.setDescription("cambio stato motore " + engineRef + ": " + transition);
        log.setCreatedAt(occurredAt);
        return log;
    }

    @SuppressWarnings("unchecked")
    private static <T> T unsupportedDao(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                (proxy, method, args) -> {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    if (returnType == long.class) {
                        return 0L;
                    }
                    if (returnType == double.class) {
                        return 0D;
                    }
                    if (List.class.isAssignableFrom(returnType)) {
                        return List.of();
                    }
                    if (Optional.class.isAssignableFrom(returnType)) {
                        return Optional.empty();
                    }
                    return null;
                }
        );
    }

    private static final class FakeEngineDAO implements EngineDAO {
        private final List<Engine> engines;

        private FakeEngineDAO(List<Engine> engines) {
            this.engines = new ArrayList<>(engines);
        }

        @Override
        public Engine save(Engine engine) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Engine update(Engine engine) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean delete(String engineRef) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Engine> findById(long id) {
            return engines.stream().filter(engine -> engine.getId() == id).findFirst();
        }

        @Override
        public Optional<Engine> findByEngineRef(String engineRef) {
            return engines.stream().filter(engine -> engine.getEngineRef().equals(engineRef)).findFirst();
        }

        @Override
        public List<Engine> findByEngineCode(String engineCode) {
            return List.of();
        }

        @Override
        public List<Engine> findByStatus(EngineStatus status) {
            return engines.stream().filter(engine -> engine.getStatus() == status).toList();
        }

        @Override
        public List<Engine> findAll() {
            return List.copyOf(engines);
        }

        @Override
        public List<Engine> findLatest(int limit) {
            return engines.stream()
                    .sorted(Comparator.comparing(Engine::getIntakeDate).reversed())
                    .limit(Math.max(1, limit))
                    .toList();
        }

        @Override
        public List<Engine> search(String keyword) {
            return List.of();
        }

        @Override
        public int countByStatus(EngineStatus status) {
            return (int) engines.stream().filter(engine -> engine.getStatus() == status).count();
        }

        @Override
        public int countByIntakeBetween(LocalDate from, LocalDate to) {
            return (int) engines.stream()
                    .filter(engine -> !engine.getIntakeDate().isBefore(from) && !engine.getIntakeDate().isAfter(to))
                    .count();
        }

        @Override
        public int countByStatusAndIntakeBetween(EngineStatus status, LocalDate from, LocalDate to) {
            return (int) engines.stream()
                    .filter(engine -> engine.getStatus() == status)
                    .filter(engine -> !engine.getIntakeDate().isBefore(from) && !engine.getIntakeDate().isAfter(to))
                    .count();
        }

        @Override
        public int countInWorkshop() {
            return (int) engines.stream().filter(Engine::isInWorkshop).count();
        }

        @Override
        public int countDeliveredBetween(LocalDate from, LocalDate to) {
            return (int) engines.stream()
                    .filter(Engine::isDelivered)
                    .filter(engine -> engine.getDeliveryDate() != null)
                    .filter(engine -> !engine.getDeliveryDate().isBefore(from) && !engine.getDeliveryDate().isAfter(to))
                    .count();
        }

        @Override
        public int countDistinctCustomersDeliveredBetween(LocalDate from, LocalDate to) {
            return 0;
        }

        @Override
        public double averageProcessingDaysForDeliveredBetween(LocalDate from, LocalDate to) {
            return 0;
        }

        @Override
        public Optional<Engine> findByCustomerAndEngineCode(String customer, String engineCode) {
            return Optional.empty();
        }

        @Override
        public int getNextSequenceForYear(int year) {
            return 1;
        }
    }

    private static final class FakeUserActivityLogDAO implements UserActivityLogDAO {
        private final List<UserActivityLog> logs;

        private FakeUserActivityLogDAO(List<UserActivityLog> logs) {
            this.logs = new ArrayList<>(logs);
        }

        @Override
        public void save(UserActivityLog log) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UserActivityLog> findRecent(int limit) {
            return List.of();
        }

        @Override
        public List<UserActivityLog> findByUsername(String username, int limit) {
            return List.of();
        }

        @Override
        public List<UserActivityLog> findByDate(LocalDate date, int limit) {
            return List.of();
        }

        @Override
        public List<UserActivityLog> findByUsernameAndDate(String username, LocalDate date, int limit) {
            return List.of();
        }

        @Override
        public List<UserActivityLog> findByEntityTypeAndActionType(String entityType, String actionType) {
            return logs.stream()
                    .filter(log -> entityType.equals(log.getEntityType()))
                    .filter(log -> actionType.equals(log.getActionType()))
                    .sorted(Comparator.comparing(UserActivityLog::getCreatedAt))
                    .toList();
        }
    }
}
