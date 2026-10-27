package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.VehicleController;
import it.SimoSW.model.Vehicle;
import it.SimoSW.model.VehicleEvent;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/vehicles")
public class VehicleServlet extends HttpServlet {
    private VehicleController controller;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getVehicleController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        render(request, response, request.getParameter("view"));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("createVehicle".equals(action)) {
                long id = controller.createVehicle(request.getParameter("plate"), request.getParameter("make"),
                        request.getParameter("model"), request.getParameter("notes"));
                redirectDetail(request, response, id);
                return;
            }
            if ("updateVehicle".equals(action)) {
                long id = id(request, "id");
                controller.updateVehicle(id, request.getParameter("plate"), request.getParameter("make"),
                        request.getParameter("model"), request.getParameter("notes"));
                redirectDetail(request, response, id);
                return;
            }
            if ("deleteVehicle".equals(action)) {
                controller.deleteVehicle(id(request, "id"));
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
