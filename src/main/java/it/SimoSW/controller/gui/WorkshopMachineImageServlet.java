package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.WorkshopMaintenanceController;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/uploads/workshop-machines/*")
public class WorkshopMachineImageServlet extends HttpServlet {
    private WorkshopMaintenanceController controller;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getWorkshopMaintenanceController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]*/[0-9a-fA-F-]{36}\\.jpg")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String[] segments = path.substring(1).split("/");
        long id;
        try { id = Long.parseLong(segments[0]); }
        catch (NumberFormatException ex) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        String filename = segments[1];
        if (!filename.equals(controller.getMachineImageFilename(id))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path file = UploadPathResolver.resolveWorkshopMachineUploadBase(getServletContext())
                .resolve(segments[0]).resolve(filename).normalize();
        if (!Files.isRegularFile(file)) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        response.setContentType("image/jpeg");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Files.size(file));
        Files.copy(file, response.getOutputStream());
    }
}
