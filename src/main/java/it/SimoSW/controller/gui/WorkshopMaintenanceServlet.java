package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WorkshopMaintenanceController;
import it.SimoSW.model.MaintenanceIntervention;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.MaterialConsumption;
import it.SimoSW.model.User;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.model.WorkshopMaterialUse;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/workshop-maintenance")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class WorkshopMaintenanceServlet extends HttpServlet {
    private WorkshopMaintenanceController controller;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getWorkshopMaintenanceController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        render(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String action = request.getParameter("action");
            if ("createMaterial".equals(action)) {
                controller.createMaterial(request.getParameter("name"), request.getParameter("category"),
                        request.getParameter("unit"), request.getParameter("location"),
                        decimal(request.getParameter("quantity")), decimal(request.getParameter("minimumQuantity")));
                response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view=materials&saved=1");
                return;
            }
            if ("adjustMaterial".equals(action)) {
                BigDecimal amount = decimal(request.getParameter("quantity"));
                if (amount.signum() <= 0) throw new IllegalArgumentException("Quantità da movimentare non valida");
                String direction = request.getParameter("direction");
                if (!"IN".equals(direction) && !"OUT".equals(direction))
                    throw new IllegalArgumentException("Tipo di movimentazione non valido");
                controller.adjustMaterialStock(longValue(request, "materialId"),
                        "OUT".equals(direction) ? amount.negate() : amount);
                response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view=materials&saved=1");
                return;
            }
            if ("createPlan".equals(action)) {
                LocalDate dueDate = date(request.getParameter("dueDate"));
                controller.createPlan(longValue(request, "machineId"), request.getParameter("description"), dueDate, null);
                response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view=calendar&month="
                        + YearMonth.from(dueDate) + "&day=" + dueDate + "&saved=1");
                return;
            }
            if ("deleteMachine".equals(action)) {
                long id = longValue(request, "machineId");
                String imageFilename = controller.deleteMachine(id);
                if (imageFilename != null && imageFilename.matches("[0-9a-fA-F-]{36}\\.jpg")) {
                    try {
                        Path directory = UploadPathResolver.resolveWorkshopMachineUploadBase(getServletContext()).resolve(String.valueOf(id));
                        Files.deleteIfExists(directory.resolve(imageFilename));
                        Files.deleteIfExists(directory);
                    } catch (IOException | IllegalStateException ignored) { }
                }
                response.sendRedirect(request.getContextPath() + "/workshop-maintenance?deleted=1");
                return;
            }
            long machineId;
            if ("createMachine".equals(action)) {
                Part image = checkedImage(request);
                machineId = controller.createMachine(request.getParameter("name"), request.getParameter("model"),
                        request.getParameter("serialNumber"), request.getParameter("location"),
                        date(request.getParameter("purchaseDate")), integer(request, "usageHours"));
                if (image != null && !saveImageOrRedirect(request, response, machineId, image, true)) return;
            } else if ("updateMachine".equals(action)) {
                Part image = checkedImage(request);
                machineId = longValue(request, "machineId");
                controller.updateMachine(machineId, request.getParameter("name"), request.getParameter("model"),
                        request.getParameter("serialNumber"), request.getParameter("location"),
                        date(request.getParameter("purchaseDate")), request.getParameter("status"), integer(request, "usageHours"));
                if (image != null && !saveImageOrRedirect(request, response, machineId, image, false)) return;
            } else if ("recordIntervention".equals(action)) {
                HttpSession session = request.getSession(false);
                User user = session == null ? null : (User) session.getAttribute("loggedUser");
                if (user == null) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
                machineId = longValue(request, "machineId");
                controller.recordIntervention(machineId, optionalLong(request, "planId"),
                        request.getParameter("type"), date(request.getParameter("performedOn")), request.getParameter("description"),
                        request.getParameter("parts"), user.getUsername(), materialConsumptions(request));
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view=detail&id=" + machineId + "&saved=1");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            String action = request.getParameter("action");
            request.setAttribute("view", "createMaterial".equals(action) || "adjustMaterial".equals(action) ? "materials"
                    : "createPlan".equals(action) ? "calendar"
                    : "createMachine".equals(action) ? "new"
                    : "updateMachine".equals(action) ? "edit"
                    : "deleteMachine".equals(action) ? "detail"
                    : "recordIntervention".equals(action) ? "intervention" : null);
            render(request, response);
        }
    }

    private static Part checkedImage(HttpServletRequest request) throws IOException, ServletException {
        Part image = request.getPart("image");
        if (image == null || image.getSize() == 0) return null;
        if (image.getContentType() == null || !image.getContentType().startsWith("image/"))
            throw new IllegalArgumentException("Seleziona un file immagine valido");
        try (java.io.InputStream input = image.getInputStream()) {
            if (ImageIO.read(input) == null) throw new IllegalArgumentException("Immagine non leggibile");
        }
        return image;
    }

    private boolean saveImageOrRedirect(HttpServletRequest request, HttpServletResponse response,
                                        long id, Part image, boolean created) throws IOException {
        Path directory = UploadPathResolver.resolveWorkshopMachineUploadBase(getServletContext()).resolve(String.valueOf(id));
        String filename = null;
        try {
            filename = ImageOptimizationUtil.storeOptimizedImage(image, directory);
            String previous = controller.updateMachineImage(id, filename);
            if (previous != null && previous.matches("[0-9a-fA-F-]{36}\\.jpg")) {
                try { Files.deleteIfExists(directory.resolve(previous)); }
                catch (IOException ignored) { }
            }
            return true;
        } catch (IOException | RuntimeException ex) {
            if (filename != null) {
                try { Files.deleteIfExists(directory.resolve(filename)); }
                catch (IOException ignored) { }
            }
            response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view="
                    + (created ? "detail" : "edit") + "&id=" + id + "&imageError=1"
                    + (created ? "&saved=1" : ""));
            return false;
        }
    }

    private void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String view = (String) request.getAttribute("view");
            if (view == null) view = request.getParameter("view");
            if (view == null && request.getParameter("edit") != null) view = "edit";
            if (view == null && "1".equals(request.getParameter("newMachine"))) view = "new";
            if ("plans".equals(view) || "new-plan".equals(view)) {
                response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view=calendar");
                return;
            }
            if ("calendar".equals(view)) {
                prepareCalendar(request);
                request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-calendar.jsp").forward(request, response);
                return;
            }
            if ("history".equals(view)) {
                request.setAttribute("interventions", controller.getInterventions());
                request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-history.jsp").forward(request, response);
                return;
            }
            if ("materials".equals(view)) {
                request.setAttribute("materials", controller.getMaterials());
                request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-materials.jsp").forward(request, response);
                return;
            }
            List<WorkshopMachine> machines = controller.getMachines();
            request.setAttribute("machines", machines);
            request.setAttribute("today", LocalDate.now());
            if ("new".equals(view)) {
                request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-machine-form.jsp").forward(request, response);
                return;
            }
            if ("detail".equals(view) || "edit".equals(view)) {
                String idValue = request.getParameter("id");
                if (idValue == null) idValue = request.getParameter("edit");
                if (idValue == null) idValue = request.getParameter("machineId");
                long id = Long.parseLong(idValue);
                WorkshopMachine machine = machines.stream().filter(item -> item.getId() == id).findFirst().orElse(null);
                if (machine == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                request.setAttribute("machine", machine);
                if ("edit".equals(view)) {
                    request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-machine-form.jsp").forward(request, response);
                } else {
                    LocalDate since = LocalDate.now().minusYears(1);
                    request.setAttribute("recentInterventions", controller.getRecentInterventions(id));
                    Map<Long, List<WorkshopMaterialUse>> uses = controller.getMaterialUses(id).stream()
                            .collect(Collectors.groupingBy(WorkshopMaterialUse::getInterventionId));
                    request.setAttribute("materialUses", uses);
                    request.setAttribute("since", since);
                    request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-machine-detail.jsp").forward(request, response);
                }
                return;
            }
            if ("intervention".equals(view)) {
                request.setAttribute("materials", controller.getMaterials());
                request.setAttribute("plans", controller.getPlans().stream().filter(plan -> !plan.isCompleted()).toList());
                String selected = request.getParameter("machineId");
                if (!blank(selected)) {
                    long id = Long.parseLong(selected);
                    if (machines.stream().noneMatch(machine -> machine.getId() == id)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    request.setAttribute("selectedMachineId", id);
                }
                request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-intervention-form.jsp").forward(request, response);
                return;
            }
            if (view != null && !view.isBlank()) { response.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
            List<MaintenancePlan> plans = controller.getPlans();
            request.setAttribute("machineCount", machines.size());
            request.setAttribute("outOfServiceCount", machines.stream().filter(m -> "OUT_OF_SERVICE".equals(m.getStatus())).count());
            request.setAttribute("overdueCount", plans.stream().filter(MaintenancePlan::isOverdue).count());
            request.setAttribute("dueSoonCount", plans.stream().filter(MaintenancePlan::isDueSoon).count());
            request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-maintenance.jsp").forward(request, response);
        } catch (NumberFormatException | DateTimeException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalStateException e) {
            request.setAttribute("error", "Impossibile caricare la manutenzione. Verifica che la migrazione del database sia stata applicata.");
            request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-maintenance.jsp").forward(request, response);
        }
    }

    private void prepareCalendar(HttpServletRequest request) {
        YearMonth month = blank(request.getParameter("month")) ? YearMonth.now()
                : YearMonth.parse(request.getParameter("month"));
        if (month.getYear() < 1900 || month.getYear() > 2100)
            throw new DateTimeException("Mese non valido");
        LocalDate selectedDate = blank(request.getParameter("day"))
                ? (month.equals(YearMonth.now()) ? LocalDate.now() : month.atDay(1))
                : LocalDate.parse(request.getParameter("day"));
        if (!YearMonth.from(selectedDate).equals(month))
            throw new DateTimeException("Giorno non valido");

        List<MaintenancePlan> plans = controller.getPlans();
        Map<LocalDate, List<MaintenancePlan>> byDate = plans.stream()
                .filter(plan -> plan.getDueDate() != null)
                .collect(Collectors.groupingBy(MaintenancePlan::getDueDate));
        Map<LocalDate, List<MaintenanceIntervention>> interventionsByDate = controller.getInterventions().stream()
                .collect(Collectors.groupingBy(MaintenanceIntervention::getPerformedOn));
        LocalDate firstCell = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastCell = month.atEndOfMonth().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Map<String, Object>> days = new ArrayList<>();
        for (LocalDate day = firstCell; !day.isAfter(lastCell); day = day.plusDays(1)) {
            List<MaintenancePlan> dailyPlans = byDate.getOrDefault(day, List.of());
            days.add(Map.of("date", day.toString(), "month", YearMonth.from(day).toString(), "number", day.getDayOfMonth(),
                    "inMonth", YearMonth.from(day).equals(month), "today", day.equals(LocalDate.now()),
                    "selected", day.equals(selectedDate), "count", dailyPlans.size()
                            + interventionsByDate.getOrDefault(day, List.of()).size(),
                    "overdue", dailyPlans.stream().anyMatch(MaintenancePlan::isOverdue)));
        }
        request.setAttribute("calendarMonth", month);
        request.setAttribute("calendarMonthTitle", month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.ITALIAN)));
        request.setAttribute("previousMonth", month.minusMonths(1));
        request.setAttribute("nextMonth", month.plusMonths(1));
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("selectedDayTitle", selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ITALIAN)));
        request.setAttribute("calendarDays", days);
        request.setAttribute("selectedPlans", byDate.getOrDefault(selectedDate, List.of()));
        request.setAttribute("selectedInterventions", interventionsByDate.getOrDefault(selectedDate, List.of()));
        request.setAttribute("hourPlans", plans.stream().filter(plan -> plan.getDueHours() != null && !plan.isCompleted()).toList());
        request.setAttribute("machines", controller.getMachines());
    }

    private static long longValue(HttpServletRequest request, String name) {
        try {
            long value = Long.parseLong(request.getParameter(name));
            if (value > 0) return value;
        } catch (NumberFormatException | NullPointerException ignored) { }
        throw new IllegalArgumentException("Identificativo non valido");
    }

    private static Long optionalLong(HttpServletRequest request, String name) {
        return blank(request.getParameter(name)) ? null : longValue(request, name);
    }

    private static int integer(HttpServletRequest request, String name) {
        try { return Integer.parseInt(request.getParameter(name)); }
        catch (NumberFormatException | NullPointerException e) { throw new IllegalArgumentException("Ore non valide"); }
    }

    private static LocalDate date(String value) {
        if (blank(value)) return null;
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Data non valida"); }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    private static BigDecimal decimal(String value) {
        if (blank(value)) throw new IllegalArgumentException("Quantità obbligatoria");
        try { return new BigDecimal(value.trim().replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Quantità non valida"); }
    }

    private static List<MaterialConsumption> materialConsumptions(HttpServletRequest request) {
        String[] ids = request.getParameterValues("materialId");
        String[] quantities = request.getParameterValues("materialQuantity");
        if (ids == null && quantities == null) return List.of();
        if (ids == null || quantities == null || ids.length != quantities.length || ids.length > 30)
            throw new IllegalArgumentException("Selezione materiali non valida");
        List<MaterialConsumption> result = new ArrayList<>();
        for (int index = 0; index < ids.length; index++) {
            if (blank(ids[index]) && blank(quantities[index])) continue;
            try {
                result.add(new MaterialConsumption(Long.parseLong(ids[index]), decimal(quantities[index])));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Materiale non valido");
            }
        }
        return result;
    }
}
