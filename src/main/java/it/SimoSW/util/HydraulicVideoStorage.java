package it.SimoSW.util;

import javax.servlet.ServletContext;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class HydraulicVideoStorage {

    private static final long FFMPEG_TIMEOUT_SECONDS = 600;
    private static final long MIN_COMPRESSIBLE_BYTES = 1024L * 1024L;
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            ".mp4", ".mov", ".m4v", ".webm", ".avi", ".mkv", ".3gp", ".mpeg", ".mpg"
    );

    private HydraulicVideoStorage() {
    }

    public static String store(ServletContext context, Part videoPart) throws IOException {
        if (videoPart == null || videoPart.getSize() <= 0) {
            throw new IllegalArgumentException("Carica un video valido.");
        }

        String submittedName = videoPart.getSubmittedFileName();
        String originalFilename = submittedName != null
                ? Paths.get(submittedName).getFileName().toString()
                : "video.mp4";
        String sanitizedOriginal = sanitizeFilename(originalFilename);
        String inputExtension = extensionOf(sanitizedOriginal);
        if (!isVideo(videoPart.getContentType(), inputExtension)) {
            throw new IllegalArgumentException("Il file caricato non è un video.");
        }

        Path uploadBase = UploadPathResolver.resolveHydraulicUploadBase(context);
        Files.createDirectories(uploadBase);
        Path temporaryFile = Files.createTempFile("hydraulic-upload-", inputExtension);
        try {
            videoPart.write(temporaryFile.toString());

            String compressedFilename = UUID.randomUUID() + ".mp4";
            Path compressedDestination = resolveLocalFile(uploadBase, compressedFilename);
            if (Files.size(temporaryFile) >= MIN_COMPRESSIBLE_BYTES
                    && compressVideo(temporaryFile, compressedDestination)) {
                Files.deleteIfExists(temporaryFile);
                return compressedFilename;
            }

            Files.deleteIfExists(compressedDestination);
            String finalFilename = UUID.randomUUID() + "_" + sanitizedOriginal;
            Path destination = resolveLocalFile(uploadBase, finalFilename);
            Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
            return finalFilename;
        } catch (IOException | RuntimeException ex) {
            Files.deleteIfExists(temporaryFile);
            throw ex;
        }
    }

    public static void deleteIfLocal(ServletContext context, String videoReference) throws IOException {
        if (videoReference == null || videoReference.isBlank()
                || videoReference.startsWith("http://") || videoReference.startsWith("https://")) {
            return;
        }
        Path uploadBase = UploadPathResolver.resolveHydraulicUploadBase(context);
        Files.deleteIfExists(resolveLocalFile(uploadBase, videoReference));
    }

    private static Path resolveLocalFile(Path uploadBase, String filename) {
        if (filename == null || filename.isBlank()
                || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Nome file non valido.");
        }
        Path resolved = uploadBase.resolve(filename).normalize();
        if (!resolved.startsWith(uploadBase)) {
            throw new IllegalArgumentException("Nome file non valido.");
        }
        return resolved;
    }

    private static boolean compressVideo(Path inputFile, Path outputFile) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-y", "-i", inputFile.toString(),
                "-c:v", "libx264", "-preset", "veryfast", "-crf", "28",
                "-c:a", "aac", "-b:a", "96k", "-movflags", "+faststart", outputFile.toString()
        );
        processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
        try {
            Process process = processBuilder.start();
            if (!process.waitFor(FFMPEG_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                Files.deleteIfExists(outputFile);
                return false;
            }
            if (process.exitValue() != 0 || !Files.exists(outputFile) || Files.size(outputFile) == 0) {
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

    private static String sanitizeFilename(String filename) {
        String value = (filename == null || filename.isBlank()) ? "video.mp4" : filename;
        String normalized = Paths.get(value).getFileName().toString().replace(' ', '_');
        StringBuilder builder = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            builder.append(Character.isLetterOrDigit(ch) || ch == '.' || ch == '_' || ch == '-' ? ch : '_');
        }
        return builder.length() == 0 ? "video.mp4" : builder.toString();
    }

    private static String extensionOf(String filename) {
        int index = filename == null ? -1 : filename.lastIndexOf('.');
        if (index <= 0 || index == filename.length() - 1) {
            return ".tmp";
        }
        String extension = filename.substring(index).toLowerCase(Locale.ROOT);
        return extension.length() <= 10 ? extension : ".tmp";
    }

    private static boolean isVideo(String contentType, String extension) {
        return (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("video/"))
                || VIDEO_EXTENSIONS.contains(extension);
    }
}
