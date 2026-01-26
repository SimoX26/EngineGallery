package it.SimoSW.controller.gui;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;


@WebServlet("/image-upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 MB
        maxFileSize = 5 * 1024 * 1024,         // 5 MB
        maxRequestSize = 10 * 1024 * 1024      // 10 MB
)
public class ImageUploadServlet extends HttpServlet {

    private static final String TEMP_UPLOAD_DIR = "temp_uploads";



    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/WEB-INF/views/image/image-upload.jsp")
                .forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Part imagePart = request.getPart("image");

        if (imagePart == null || imagePart.getSize() == 0) {
            request.setAttribute("error", "Nessun file selezionato");
            request.getRequestDispatcher("/WEB-INF/views/image/image-upload.jsp")
                    .forward(request, response);
            return;
        }

        // Validazione MIME
        String contentType = imagePart.getContentType();
        if (!contentType.startsWith("image/")) {
            request.setAttribute("error", "Il file caricato non è un'immagine valida");
            request.getRequestDispatcher("/WEB-INF/views/image/image-upload.jsp")
                    .forward(request, response);
            return;
        }

        // Nome file sicuro
        String originalFileName = Paths.get(imagePart.getSubmittedFileName())
                .getFileName().toString();

        String safeFileName = UUID.randomUUID() + "_" + originalFileName;

        // Directory temporanea
        String uploadPath = getServletContext().getRealPath("") + File.separator + TEMP_UPLOAD_DIR;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Salvataggio file
        File savedFile = new File(uploadDir, safeFileName);
        imagePart.write(savedFile.getAbsolutePath());

        // Passaggio dati alla schermata successiva
        request.setAttribute("tempImageName", safeFileName);
        request.setAttribute("originalFileName", originalFileName);

        request.getRequestDispatcher("/WEB-INF/views/image/image-save.jsp").forward(request, response);
    }
}