package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.ImageController;
import it.SimoSW.model.Image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/image")
public class ImageServlet extends HttpServlet {

    private ImageController imageController;

    @Override
    public void init() {
        this.imageController = (ImageController) getServletContext().getAttribute("imageController");
    }

    /* =========================
       POST: azioni sulle immagini
       ========================= */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("add".equals(action)) {
            handleAddImage(request, response);
            return;
        }

        if ("delete".equals(action)) {
            handleDeleteImage(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    /* =========================
       Gestione ADD
       ========================= */

    private void handleAddImage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        long engineId = Long.parseLong(request.getParameter("engineId"));
        String filePath = request.getParameter("filePath");
        String description = request.getParameter("description");
        String keywordsParam = request.getParameter("keywords");

        List<String> keywords = List.of();
        if (keywordsParam != null && !keywordsParam.isBlank()) {
            keywords = Arrays.stream(keywordsParam.split(","))
                    .map(String::trim)
                    .toList();
        }

        Image image = new Image(
                engineId,
                filePath,
                description,
                keywords
        );

        imageController.addImage(image);

        // Redirect al dettaglio del motore
        response.sendRedirect(request.getContextPath() + "/gallery?engineId=" + engineId);
    }

    /* =========================
       Gestione DELETE
       ========================= */

    private void handleDeleteImage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        long imageId = Long.parseLong(request.getParameter("imageId"));
        long engineId = Long.parseLong(request.getParameter("engineId"));

        imageController.deleteImage(imageId);

        // Redirect al dettaglio del motore
        response.sendRedirect(request.getContextPath() + "/gallery?engineId=" + engineId);
    }
}
