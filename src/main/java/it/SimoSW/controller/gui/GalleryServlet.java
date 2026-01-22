package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.GalleryController;
import it.SimoSW.model.Engine;
import it.SimoSW.model.EngineStatus;
import it.SimoSW.model.Image;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet("/gallery")
public class GalleryServlet extends HttpServlet {

    private GalleryController galleryController;

    @Override
    public void init() {
        // Recupero del controller applicativo dal contesto
        this.galleryController = (GalleryController) getServletContext().getAttribute("galleryController");
    }

    /* =========================
       GET: visualizzazione
       ========================= */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineIdParam = request.getParameter("engineId");

        // Caso 1: nessun motore selezionato → pagina ricerca
        if (engineIdParam == null) {
            request.getRequestDispatcher("/WEB-INF/views/gallery/engine-gallery.jsp").forward(request, response);
            return;
        }

        // Caso 2: motore selezionato → mostra immagini
        long engineId = Long.parseLong(engineIdParam);
        List<Image> images = galleryController.getImagesForEngine(engineId);

        request.setAttribute("images", images);
        request.setAttribute("engineId", engineId);

        request.getRequestDispatcher("/WEB-INF/views/gallery/engine-detail.jsp").forward(request, response);
    }

    /* =========================
       POST: ricerca motori
       ========================= */

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String engineCode = request.getParameter("engineCode");
        String statusParam = request.getParameter("status");
        String keyword = request.getParameter("keyword");

        List<Engine> engines;

        // Priorità: codice → stato → keyword
        if (engineCode != null && !engineCode.isBlank()) {
            Optional<Engine> engine = galleryController.findEngineByCode(engineCode);

            engines = engine.map(List::of).orElse(List.of());

        } else if (statusParam != null && !statusParam.isBlank()) {
            EngineStatus status = EngineStatus.valueOf(statusParam);
            engines = galleryController.findEnginesByStatus(status);

        } else if (keyword != null && !keyword.isBlank()) {
            engines = galleryController.findEnginesByKeyword(keyword);

        } else {
            engines = galleryController.getAllEngines();
        }

        request.setAttribute("engines", engines);

        request.getRequestDispatcher("/WEB-INF/views/gallery/engineList.jsp").forward(request, response);
    }
}
