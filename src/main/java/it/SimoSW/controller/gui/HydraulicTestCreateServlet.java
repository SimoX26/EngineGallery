package it.SimoSW.controller.gui;

import it.SimoSW.controller.app.HydraulicTestController;
import it.SimoSW.model.UserActivityActionType;
import it.SimoSW.model.UserActivityEntityType;
import it.SimoSW.util.UploadPathResolver;
import it.SimoSW.util.audit.UserActivityAuditLogger;
import it.SimoSW.util.bootstrap.ApplicationInitializer;
import it.SimoSW.util.navigation.PostSubmitNavigationGuard;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@WebServlet("/hydraulic-test/new")
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,
        maxFileSize = 500 * 1024 * 1024,
        maxRequestSize = 700 * 1024 * 1024
)
public class HydraulicTestCreateServlet extends HttpServlet {
    private static final long FFMPEG_TIMEOUT_SECONDS = 600;
    private static final long MIN_COMPRESSIBLE_BYTES = 1024L * 1024L; // 1MB

    private HydraulicTestController hydraulicTestController;
    private UserActivityAuditLogger activityAuditLogger;

    @Override
    public void init() {
        ApplicationInitializer initializer =
                (ApplicationInitializer) getServletContext().getAttribute("appInitializer");
        this.hydraulicTestController = initializer.getHydraulicTestController();
        this.activityAuditLogger = new UserActivityAuditLogger(initializer.getUserActivityLogController());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (PostSubmitNavigationGuard.redirectIfBlocked(request, response, "/hydraulic-test/new")) {
            return;
        }
        request.setAttribute("testDate", LocalDate.now().toString());
        request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String customerName = request.getParameter("customerName");
        String engineCode = request.getParameter("engineCode");
        String testDate = request.getParameter("testDate");
        String notes = request.getParameter("notes");
        Part videoPart = request.getPart("videoFile");

        request.setAttribute("customerName", customerName);
        request.setAttribute("engineCode", engineCode);
        request.setAttribute("testDate", testDate);
        request.setAttribute("notes", notes);

        if (isBlank(customerName) || isBlank(engineCode) || isBlank(testDate)) {
            request.setAttribute("error", "Compila tutti i campi obbligatori.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }
        if (videoPart == null || videoPart.getSize() <= 0) {
            request.setAttribute("error", "Carica un video valido.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }
        String contentType = videoPart.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            request.setAttribute("error", "Il file caricato non è un video.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }

        try {
            LocalDate.parse(testDate.trim());
        } catch (DateTimeParseException ex) {
            request.setAttribute("error", "Data prova non valida.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }

        String submittedName = videoPart.getSubmittedFileName();
        String originalFilename = submittedName != null
                ? Paths.get(submittedName).getFileName().toString()
                : "video.mp4";
        String sanitizedOriginal = sanitizeFilename(originalFilename);
        String inputExtension = extensionOf(sanitizedOriginal);

        Path hydraulicUploadBase = UploadPathResolver.resolveHydraulicUploadBase(getServletContext());
        Files.createDirectories(hydraulicUploadBase);
        Path temporaryUploadedFile = Files.createTempFile("hydraulic-upload-", inputExtension);
        videoPart.write(temporaryUploadedFile.toString());

        String finalFilename;
        Path destination;

        String compressedFilename = UUID.randomUUID() + ".mp4";
        Path compressedDestination = hydraulicUploadBase.resolve(compressedFilename).normalize();
        if (!compressedDestination.startsWith(hydraulicUploadBase)) {
            Files.deleteIfExists(temporaryUploadedFile);
            request.setAttribute("error", "Nome file non valido.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
            return;
        }

        boolean compressionSucceeded = false;
        long uploadedSize = Files.size(temporaryUploadedFile);
        if (uploadedSize >= MIN_COMPRESSIBLE_BYTES) {
            compressionSucceeded = compressVideo(temporaryUploadedFile, compressedDestination);
        }

        if (compressionSucceeded) {
            finalFilename = compressedFilename;
            destination = compressedDestination;
            Files.deleteIfExists(temporaryUploadedFile);
        } else {
            finalFilename = UUID.randomUUID() + "_" + sanitizedOriginal;
            destination = hydraulicUploadBase.resolve(finalFilename).normalize();
            if (!destination.startsWith(hydraulicUploadBase)) {
                Files.deleteIfExists(temporaryUploadedFile);
                request.setAttribute("error", "Nome file non valido.");
                request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                        .forward(request, response);
                return;
            }
            Files.move(temporaryUploadedFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            hydraulicTestController.createHydraulicTest(
                    customerName,
                    engineCode,
                    finalFilename,
                    testDate,
                    notes
            );
            activityAuditLogger.logFromRequest(
                    request,
                    UserActivityActionType.CREATE,
                    UserActivityEntityType.HYDRAULIC_TEST,
                    null,
                    "aggiunta prova idraulica " + engineCode
            );
            PostSubmitNavigationGuard.blockFormPageOnce(
                    request,
                    "/hydraulic-test/new",
                    "/hydraulic-test/list?lockBack=1"
            );
            response.sendRedirect(request.getContextPath() + "/hydraulic-test/list?lockBack=1");
        } catch (RuntimeException ex) {
            Files.deleteIfExists(destination);
            request.setAttribute("error", "Errore durante il salvataggio della prova idraulica.");
            request.getRequestDispatcher("/WEB-INF/views/hydraulic/hydraulic-test-new.jsp")
                    .forward(request, response);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean compressVideo(Path inputFile, Path outputFile) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", inputFile.toString(),
                "-c:v", "libx264",
                "-preset", "veryfast",
                "-crf", "28",
                "-c:a", "aac",
                "-b:a", "96k",
                "-movflags", "+faststart",
                outputFile.toString()
        );
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(FFMPEG_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                Files.deleteIfExists(outputFile);
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0 || !Files.exists(outputFile) || Files.size(outputFile) == 0) {
                Files.deleteIfExists(outputFile);
                return false;
            }
            return true;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    private String sanitizeFilename(String filename) {
        String value = (filename == null || filename.isBlank()) ? "video.mp4" : filename;
        String normalized = Paths.get(value).getFileName().toString().replace(' ', '_');
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        String sanitized = builder.toString();
        if (sanitized.isBlank()) {
            return "video.mp4";
        }
        return sanitized;
    }

    private String extensionOf(String filename) {
        if (filename == null) {
            return ".tmp";
        }
        int index = filename.lastIndexOf('.');
        if (index <= 0 || index == filename.length() - 1) {
            return ".tmp";
        }
        String ext = filename.substring(index).toLowerCase(Locale.ROOT);
        if (ext.length() > 10) {
            return ".tmp";
        }
        return ext;
    }
}
