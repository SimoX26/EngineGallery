package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.VehicleController;
import it.SimoSW.model.Vehicle;
import it.SimoSW.model.VehicleEvent;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet("/vehicles")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 22 * 1024 * 1024)
public class VehicleServlet extends HttpServlet {
    private VehicleController controller;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getVehicleController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("photo".equals(request.getParameter("view"))) {
            servePhoto(request, response);
            return;
        }
        render(request, response, request.getParameter("view"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("createVehicle".equals(action)) {
                Part vehiclePhoto = checkedPhoto(request, "vehiclePhoto", "veicolo");
                Part registrationPhoto = checkedPhoto(request, "registrationPhoto", "libretto");
                long id = controller.createVehicle(request.getParameter("plate"), request.getParameter("make"),
                        request.getParameter("model"), request.getParameter("notes"));
                try {
                    storePhotos(id, vehiclePhoto, registrationPhoto);
                } catch (IllegalArgumentException ex) {
                    controller.deleteVehicle(id);
                    throw ex;
                }
                redirectDetail(request, response, id);
                return;
            }
            if ("updateVehicle".equals(action)) {
                Part vehiclePhoto = checkedPhoto(request, "vehiclePhoto", "veicolo");
                Part registrationPhoto = checkedPhoto(request, "registrationPhoto", "libretto");
                long id = id(request, "id");
                controller.updateVehicle(id, request.getParameter("plate"), request.getParameter("make"),
                        request.getParameter("model"), request.getParameter("notes"));
                storePhotos(id, vehiclePhoto, registrationPhoto);
                redirectDetail(request, response, id);
                return;
            }
            if ("deleteVehicle".equals(action)) {
                long id = id(request, "id");
                Vehicle vehicle = controller.getVehicle(id).orElse(null);
                controller.deleteVehicle(id);
                if (vehicle != null) deletePhotos(id, vehicle);
                response.sendRedirect(request.getContextPath() + "/vehicles?deleted=1");
                return;
            }
            long vehicleId = id(request, "id");
            if ("createEvent".equals(action)) {
                controller.createEvent(vehicleId, request.getParameter("type"), request.getParameter("title"),
                        date(request.getParameter("dueDate")), date(request.getParameter("completedOn")), request.getParameter("notes"));
            } else if ("updateEvent".equals(action)) {
                controller.updateEvent(vehicleId, id(request, "eventId"), request.getParameter("type"),
                        request.getParameter("title"), date(request.getParameter("dueDate")),
                        date(request.getParameter("completedOn")), request.getParameter("notes"));
            } else if ("deleteEvent".equals(action)) {
                controller.deleteEvent(vehicleId, id(request, "eventId"));
            } else if ("completeEvent".equals(action) || "reopenEvent".equals(action)) {
                VehicleEvent event = controller.getEvent(vehicleId, id(request, "eventId"))
                        .orElseThrow(() -> new IllegalArgumentException("Voce non trovata"));
                controller.updateEvent(vehicleId, event.getId(), event.getType(), event.getTitle(), event.getDueDate(),
                        "completeEvent".equals(action) ? LocalDate.now() : null, event.getNotes());
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            redirectDetail(request, response, vehicleId);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            String view = "createVehicle".equals(action) ? "new"
                    : "updateVehicle".equals(action) ? "edit"
                    : "createEvent".equals(action) ? "newEvent"
                    : "updateEvent".equals(action) ? "editEvent"
                    : "deleteVehicle".equals(action) ? "list" : "detail";
            render(request, response, view);
        }
    }

    private static Part checkedPhoto(HttpServletRequest request, String name, String label)
            throws IOException, ServletException {
        Part photo = request.getPart(name);
        if (photo == null || photo.getSize() == 0) return null;
        if (photo.getContentType() == null || !photo.getContentType().startsWith("image/"))
            throw new IllegalArgumentException("Seleziona una foto valida del " + label);
        try (java.io.InputStream input = photo.getInputStream()) {
            if (ImageIO.read(input) == null) throw new IllegalArgumentException("Foto del " + label + " non leggibile");
        }
        return photo;
    }

    private void storePhotos(long id, Part vehiclePhoto, Part registrationPhoto) {
        if (vehiclePhoto == null && registrationPhoto == null) return;
        Vehicle previous = controller.getVehicle(id).orElseThrow(() -> new IllegalArgumentException("Veicolo non trovato"));
        Path directory = UploadPathResolver.resolveVehicleUploadBase(getServletContext()).resolve(String.valueOf(id));
        String vehicleFilename = null;
        String registrationFilename = null;
        try {
            if (vehiclePhoto != null) vehicleFilename = ImageOptimizationUtil.storeOptimizedImage(vehiclePhoto, directory);
            if (registrationPhoto != null) registrationFilename = ImageOptimizationUtil.storeOptimizedImage(registrationPhoto, directory);
            controller.setPhotoFilenames(id, vehicleFilename, registrationFilename);
        } catch (IOException | RuntimeException ex) {
            deleteFile(directory, vehicleFilename);
            deleteFile(directory, registrationFilename);
            throw new IllegalArgumentException("Foto non salvate. Riprova.", ex);
        }
        if (vehicleFilename != null) deleteFile(directory, previous.getVehiclePhotoFilename());
        if (registrationFilename != null) deleteFile(directory, previous.getRegistrationPhotoFilename());
    }

    private void deletePhotos(long id, Vehicle vehicle) {
        Path directory = UploadPathResolver.resolveVehicleUploadBase(getServletContext()).resolve(String.valueOf(id));
        deleteFile(directory, vehicle.getVehiclePhotoFilename());
        deleteFile(directory, vehicle.getRegistrationPhotoFilename());
    }

    private static void deleteFile(Path directory, String filename) {
        if (filename == null || !filename.matches("[0-9a-fA-F-]{36}\\.jpg")) return;
        try { Files.deleteIfExists(directory.resolve(filename)); } catch (IOException ignored) { }
    }

    private void servePhoto(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long id;
        try { id = id(request, "id"); }
        catch (IllegalArgumentException ex) { response.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
        Vehicle vehicle = controller.getVehicle(id).orElse(null);
        if (vehicle == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        String kind = request.getParameter("kind");
        String filename = "vehicle".equals(kind) ? vehicle.getVehiclePhotoFilename()
                : "registration".equals(kind) ? vehicle.getRegistrationPhotoFilename() : null;
        if (filename == null || !filename.matches("[0-9a-fA-F-]{36}\\.jpg")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path base = UploadPathResolver.resolveVehicleUploadBase(getServletContext());
        Path photo = base.resolve(String.valueOf(id)).resolve(filename).normalize();
        if (!photo.startsWith(base) || !Files.isRegularFile(photo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("image/jpeg");
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Files.size(photo));
        Files.copy(photo, response.getOutputStream());
    }

    private void render(HttpServletRequest request, HttpServletResponse response, String view) throws ServletException, IOException {
        try {
            if (view == null || "list".equals(view)) {
                request.setAttribute("vehicles", controller.getVehicles());
                request.setAttribute("reminders", controller.getPendingEvents());
                request.getRequestDispatcher("/WEB-INF/views/vehicles/list.jsp").forward(request, response);
                return;
            }
            if ("new".equals(view)) {
                request.getRequestDispatcher("/WEB-INF/views/vehicles/vehicle-form.jsp").forward(request, response);
                return;
            }
            long id = id(request, "id");
            Vehicle vehicle = controller.getVehicle(id).orElse(null);
            if (vehicle == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            request.setAttribute("vehicle", vehicle);
            if ("edit".equals(view)) {
                request.getRequestDispatcher("/WEB-INF/views/vehicles/vehicle-form.jsp").forward(request, response);
            } else if ("newEvent".equals(view) || "editEvent".equals(view)) {
                if ("editEvent".equals(view)) {
                    VehicleEvent event = controller.getEvent(id, id(request, "eventId")).orElse(null);
                    if (event == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
                    request.setAttribute("event", event);
                }
                request.getRequestDispatcher("/WEB-INF/views/vehicles/event-form.jsp").forward(request, response);
            } else if ("detail".equals(view)) {
                request.setAttribute("events", controller.getEvents(id));
                request.getRequestDispatcher("/WEB-INF/views/vehicles/detail.jsp").forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (IllegalStateException ex) {
            throw new ServletException(ex);
        }
    }

    private static long id(HttpServletRequest request, String name) {
        try {
            long value = Long.parseLong(request.getParameter(name));
            if (value > 0) return value;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("ID non valido");
    }

    private static LocalDate date(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); }
        catch (DateTimeParseException ex) { throw new IllegalArgumentException("Data non valida", ex); }
    }

    private static void redirectDetail(HttpServletRequest request, HttpServletResponse response, long id) throws IOException {
        response.sendRedirect(request.getContextPath() + "/vehicles?view=detail&id=" + id + "&saved=1");
    }
}
