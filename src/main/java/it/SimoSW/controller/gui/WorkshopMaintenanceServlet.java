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
            if ("createMachine".equals(action)) {
                Part image = checkedImage(request);
                long id = controller.createMachine(request.getParameter("name"), request.getParameter("model"),
                        request.getParameter("serialNumber"), request.getParameter("location"), integer(request, "usageHours"));
                if (image != null && !saveImageOrRedirect(request, response, id, image)) return;
            } else if ("updateMachine".equals(action)) {
                Part image = checkedImage(request);
                long id = longValue(request, "machineId");
                controller.updateMachine(id, request.getParameter("name"), request.getParameter("model"),
                        request.getParameter("serialNumber"), request.getParameter("location"), request.getParameter("status"),
                        integer(request, "usageHours"));
                if (image != null && !saveImageOrRedirect(request, response, id, image)) return;
            } else if ("createPlan".equals(action)) {
                controller.createPlan(longValue(request, "machineId"), request.getParameter("description"),
                        date(request.getParameter("dueDate")), optionalInteger(request, "dueHours"));
            } else if ("recordIntervention".equals(action)) {
                HttpSession session = request.getSession(false);
                User user = session == null ? null : (User) session.getAttribute("loggedUser");
                if (user == null) { response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return; }
                controller.recordIntervention(longValue(request, "machineId"), optionalLong(request, "planId"),
                        request.getParameter("type"), date(request.getParameter("performedOn")), request.getParameter("description"),
                        request.getParameter("parts"), user.getUsername());
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            response.sendRedirect(request.getContextPath() + "/workshop-maintenance?saved=1");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            if ("createMachine".equals(request.getParameter("action")) || "updateMachine".equals(request.getParameter("action")))
                request.setAttribute("showMachineForm", true);
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
            response.sendRedirect(request.getContextPath() + "/workshop-maintenance?edit=" + id + "&imageError=1#machine-form");
            return false;
        }
    }

    private void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<WorkshopMachine> machines = controller.getMachines();
            List<MaintenancePlan> plans = controller.getPlans();
            request.setAttribute("machines", machines);
            request.setAttribute("plans", plans);
            request.setAttribute("interventions", controller.getInterventions());
            request.setAttribute("machineCount", machines.size());
            request.setAttribute("outOfServiceCount", machines.stream().filter(m -> "OUT_OF_SERVICE".equals(m.getStatus())).count());
            request.setAttribute("overdueCount", plans.stream().filter(MaintenancePlan::isOverdue).count());
            request.setAttribute("dueSoonCount", plans.stream().filter(MaintenancePlan::isDueSoon).count());
            request.setAttribute("today", LocalDate.now());
            String edit = request.getParameter("edit");
            if (edit == null && "updateMachine".equals(request.getParameter("action"))) edit = request.getParameter("machineId");
            if (edit != null) {
                long editId = Long.parseLong(edit);
                request.setAttribute("editMachine", machines.stream().filter(m -> m.getId() == editId).findFirst().orElse(null));
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (IllegalStateException e) {
            request.setAttribute("error", "Impossibile caricare la manutenzione. Verifica che la migrazione del database sia stata applicata.");
        }
        request.getRequestDispatcher("/WEB-INF/views/maintenance/workshop-maintenance.jsp").forward(request, response);
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
