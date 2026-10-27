package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.ReadyDeliveryController;
import it.SimoSW.model.User;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.ImageOptimizationUtil;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/ready-delivery")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class ReadyDeliveryServlet extends HttpServlet {
    private ReadyDeliveryController controller;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer = (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        controller = initializer.getReadyDeliveryController();
        activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String view = request.getParameter("view");
        if ("new".equals(view)) {
            request.getRequestDispatcher("/WEB-INF/views/warehouse/ready-delivery-new.jsp").forward(request, response);
        } else if (view == null || view.isBlank()) {
            try {
                request.setAttribute("engines", controller.getEngines());
            } catch (IllegalStateException ex) {
                request.setAttribute("error", ex.getMessage());
            }
            request.getRequestDispatcher("/WEB-INF/views/warehouse/ready-delivery-list.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!"create".equals(request.getParameter("action"))) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Part image = checkedImage(request);
            int quantity;
            try {
                quantity = Integer.parseInt(request.getParameter("quantity"));
            } catch (NumberFormatException | NullPointerException ex) {
                throw new IllegalArgumentException("Quantità non valida");
            }
            HttpSession session = request.getSession(false);
            User user = session == null ? null : (User) session.getAttribute("loggedUser");
            long id = controller.create(request.getParameter("engineCode"), request.getParameter("model"),
                    quantity, request.getParameter("notes"), user == null ? null : user.getUsername());
            if (image != null) storeImage(id, image);
            activityAuditLogger.logFromRequest(request, UserActivityActionType.CREATE,
                    UserActivityEntityType.READY_DELIVERY_ENGINE, String.valueOf(id),
                    "aggiunta motore pronta consegna " + request.getParameter("engineCode").trim());
            response.sendRedirect(request.getContextPath() + "/ready-delivery?saved=1");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/warehouse/ready-delivery-new.jsp").forward(request, response);
        }
    }

    private static Part checkedImage(HttpServletRequest request) throws IOException, ServletException {
        Part image = request.getPart("image");
        if (image == null || image.getSize() == 0) return null;
        if (image.getContentType() == null || !image.getContentType().startsWith("image/"))
            throw new IllegalArgumentException("Seleziona una foto valida");
        try (java.io.InputStream input = image.getInputStream()) {
            if (ImageIO.read(input) == null) throw new IllegalArgumentException("Foto non leggibile");
        }
        return image;
    }

    private void storeImage(long id, Part image) {
        Path directory = UploadPathResolver.resolveReadyDeliveryUploadBase(getServletContext()).resolve(String.valueOf(id));
        String filename = null;
        try {
            filename = ImageOptimizationUtil.storeOptimizedImage(image, directory);
            controller.setImageFilename(id, filename);
        } catch (IOException | RuntimeException ex) {
            if (filename != null) {
                try { Files.deleteIfExists(directory.resolve(filename)); } catch (IOException ignored) { }
            }
            try { controller.delete(id); } catch (RuntimeException ignored) { }
            throw new IllegalStateException("Foto non salvata. Riprova l'inserimento.", ex);
        }
    }
}
