package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WorkshopMaintenanceController;
import it.SimoSW.model.MaintenancePlan;
import it.SimoSW.model.User;
import it.SimoSW.model.WorkshopMachine;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
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
            long machineId;
            if ("createMachine".equals(action)) {
                Part image = checkedImage(request);
                machineId = controller.createMachine(request.getParameter("name"), request.getParameter("model"),
                        request.getParameter("serialNumber"), request.getParameter("location"),
                        date(request.getParameter("purchaseDate")), integer(request, "usageHours"));
                if (image != null && !saveImageOrRedirect(request, response, machineId, image)) return;
            } else if ("updateMachine".equals(action)) {
                Part image = checkedImage(request);
                machineId = longValue(request, "machineId");
                controller.updateMachine(machineId, request.getParameter("name"), request.getParameter("model"),
                        request.getParameter("serialNumber"), request.getParameter("location"),
                        date(request.getParameter("purchaseDate")), request.getParameter("status"), integer(request, "usageHours"));
                if (image != null && !saveImageOrRedirect(request, response, machineId, image)) return;
            } else if ("createPlan".equals(action)) {
                machineId = longValue(request, "machineId");
                controller.createPlan(machineId, request.getParameter("description"),
                        date(request.getParameter("dueDate")), optionalInteger(request, "dueHours"));
            } else if ("recordIntervention".equals(action)) {
                HttpSession session = request.getSession(false);
                User user = session == null ? null : (User) session.getAttribute("loggedUser");
                if (user == null) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
                machineId = longValue(request, "machineId");
                controller.recordIntervention(machineId, optionalLong(request, "planId"),
                        request.getParameter("type"), date(request.getParameter("performedOn")), request.getParameter("description"),
                        request.getParameter("parts"), user.getUsername());
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            String destination = "createPlan".equals(action) ? "?view=plans&machineId=" : "?view=detail&id=";
            response.sendRedirect(request.getContextPath() + "/workshop-maintenance" + destination + machineId + "&saved=1");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            String action = request.getParameter("action");
            request.setAttribute("view", "createMachine".equals(action) ? "new"
                    : "updateMachine".equals(action) ? "edit"
                    : "recordIntervention".equals(action) ? "intervention"
                    : "createPlan".equals(action) ? "new-plan" : null);
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

    private boolean saveImageOrRedirect(HttpServletRequest request, HttpServletResponse response, long id, Part image) throws IOException {
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
            response.sendRedirect(request.getContextPath() + "/workshop-maintenance?view=edit&id=" + id + "&imageError=1");
            return false;
        }
    }

    private void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String view = (String) request.getAttribute("view");
            if (view == null) view = request.getParameter("view");
            if (view == null && request.getParameter("edit") != null) view = "edit";
            if (view == null && "1".equals(request.getParameter("newMachine"))) view = "new";
            List<WorkshopMachine> machines = controller.getMachines();
            request.setAttribute("machines", machines);
            request.setAttribute("today", LocalDate.now());
            if ("new".equals(view)) {
                request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-machine-form.jsp").forward(request, response);
                return;
            }
            if ("plans".equals(view) || "new-plan".equals(view)) {
                List<MaintenancePlan> plans = controller.getPlans();
                request.setAttribute("plans", plans);
                request.setAttribute("planCount", plans.size());
                request.setAttribute("overdueCount", plans.stream().filter(MaintenancePlan::isOverdue).count());
                request.setAttribute("dueSoonCount", plans.stream().filter(MaintenancePlan::isDueSoon).count());
                String selected = request.getParameter("machineId");
                if ("new-plan".equals(view) && blank(selected)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                if (!blank(selected)) {
                    long id = Long.parseLong(selected);
                    WorkshopMachine machine = machines.stream().filter(item -> item.getId() == id).findFirst().orElse(null);
                    if (machine == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                    request.setAttribute("selectedMachine", machine);
                    request.setAttribute("selectedPlans", plans.stream().filter(plan -> plan.getMachineId() == id).toList());
                    int index = machines.indexOf(machine);
                    if (index > 0) request.setAttribute("previousMachineId", machines.get(index - 1).getId());
                    if (index + 1 < machines.size()) request.setAttribute("nextMachineId", machines.get(index + 1).getId());
                }
                request.getRequestDispatcher("new-plan".equals(view)
                        ? "/WEB-INF/views/maintenance/workshop-plan-form.jsp"
                        : "/WEB-INF/views/maintenance/workshop-plans.jsp").forward(request, response);
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
                    request.setAttribute("since", since);
                    request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-machine-detail.jsp").forward(request, response);
                }
                return;
            }
            if ("intervention".equals(view)) {
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
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalStateException e) {
            request.setAttribute("error", "Impossibile caricare la manutenzione. Verifica che la migrazione del database sia stata applicata.");
            request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-maintenance.jsp").forward(request, response);
        }
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

    private static Integer optionalInteger(HttpServletRequest request, String name) {
        return blank(request.getParameter(name)) ? null : integer(request, name);
    }

    private static LocalDate date(String value) {
        if (blank(value)) return null;
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException e) { throw new IllegalArgumentException("Data non valida"); }
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
