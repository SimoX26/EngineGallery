package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.ReadyDeliveryController;
import it.SimoSW.model.ReadyDeliveryEngine;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ready-delivery/image")
public class ReadyDeliveryImageServlet extends HttpServlet {
    private ReadyDeliveryController controller;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getReadyDeliveryController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long id;
        try {
            id = Long.parseLong(request.getParameter("id"));
        } catch (NumberFormatException | NullPointerException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ReadyDeliveryEngine engine = controller.findById(id).orElse(null);
        if (engine == null || engine.getImageFilename() == null
                || !engine.getImageFilename().matches("[0-9a-fA-F-]{36}\\.jpg")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path base = UploadPathResolver.resolveReadyDeliveryUploadBase(getServletContext());
        Path image = base.resolve(String.valueOf(id)).resolve(engine.getImageFilename()).normalize();
        if (!image.startsWith(base) || !Files.isRegularFile(image)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("image/jpeg");
        response.setContentLengthLong(Files.size(image));
        response.setHeader("Cache-Control", "private, max-age=3600");
        try (java.io.InputStream input = Files.newInputStream(image)) {
            input.transferTo(response.getOutputStream());
        }
    }
}
