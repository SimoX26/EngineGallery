package it.SimoSW.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

public final class UploadPathResolver {
    public static final String SYS_PROP = "enginegallery.upload.dir";
    public static final String ENV_VAR = "ENGINE_GALLERY_UPLOAD_DIR";
    private static final Path DEFAULT_UPLOAD_ROOT = Paths.get("/var/lib/EngineGallery/uploads").toAbsolutePath().normalize();
    private static final Logger LOGGER = Logger.getLogger(UploadPathResolver.class.getName());
    private static volatile Path cachedUploadRoot;

    private UploadPathResolver() {
    }

    public static Path resolveUploadBase(ServletContext context) {
        return resolveUploadRoot(context).resolve("engines").normalize();
    }

    public static Path resolveHydraulicUploadBase(ServletContext context) {
        return resolveUploadRoot(context).resolve("hydraulic").normalize();
    }

    public static Path resolveWarehouseUploadBase(ServletContext context) {
        return resolveUploadRoot(context).resolve("warehouse").normalize();
    }

    private static Path resolveUploadRoot(ServletContext context) {
        Path cached = cachedUploadRoot;
        if (cached != null) {
            return cached;
        }

        synchronized (UploadPathResolver.class) {
            cached = cachedUploadRoot;
            if (cached != null) {
                return cached;
            }

            Path resolved = resolveUploadRootInternal(context);
            cachedUploadRoot = resolved;
            return resolved;
        }
    }

    private static Path resolveUploadRootInternal(ServletContext context) {
        List<PathCandidate> candidates = new ArrayList<>();

        String configured = System.getProperty(SYS_PROP);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(ENV_VAR);
        }
        if (configured != null && !configured.isBlank()) {
            Path configuredPath = Paths.get(configured).toAbsolutePath().normalize();
            String lastSegment = configuredPath.getFileName() != null
                    ? configuredPath.getFileName().toString()
                    : "";
            if ("engines".equalsIgnoreCase(lastSegment)
                    || "hydraulic".equalsIgnoreCase(lastSegment)
                    || "warehouse".equalsIgnoreCase(lastSegment)) {
                configuredPath = configuredPath.getParent() != null ? configuredPath.getParent() : configuredPath;
            }
            candidates.add(new PathCandidate(configuredPath, "configured"));
        }

        candidates.add(new PathCandidate(DEFAULT_UPLOAD_ROOT, "default(/var/lib/enginegallery/uploads)"));

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            candidates.add(new PathCandidate(Paths.get(userHome, "EngineGallery", "uploads")
                    .toAbsolutePath()
                    .normalize(), "user.home"));
        }

        String javaTmp = System.getProperty("java.io.tmpdir");
        if (javaTmp != null && !javaTmp.isBlank()) {
            candidates.add(new PathCandidate(Paths.get(javaTmp, "EngineGallery", "uploads")
                    .toAbsolutePath()
                    .normalize(), "java.io.tmpdir"));
        }

        if (context != null) {
            Object tempDirAttr = context.getAttribute("javax.servlet.context.tempdir");
            if (tempDirAttr instanceof File) {
                Path servletTmp = ((File) tempDirAttr).toPath().resolve("EngineGallery").resolve("uploads")
                        .toAbsolutePath()
                        .normalize();
                candidates.add(new PathCandidate(servletTmp, "servlet.tempdir"));
            }
        }

        if (context != null) {
            String realPath = context.getRealPath("/uploads");
            if (realPath != null) {
                candidates.add(new PathCandidate(Paths.get(realPath).toAbsolutePath().normalize(), "webapp.realpath"));
            }
        }

        candidates.add(new PathCandidate(Paths.get("uploads").toAbsolutePath().normalize(), "cwd"));

        for (PathCandidate candidate : candidates) {
            Path writable = ensureWritable(candidate.path);
            if (writable != null) {
                LOGGER.info("[EngineGallery] Upload root: " + writable + " (source=" + candidate.source + ")");
                return writable;
            }
        }

        throw new IllegalStateException("Nessun percorso upload scrivibile disponibile. Verifica permessi filesystem.");
    }

    private static Path ensureWritable(Path path) {
        if (path == null) {
            return null;
        }
        try {
            Files.createDirectories(path);
            if (!Files.isDirectory(path) || !Files.isWritable(path)) {
                return null;
            }
            return path;
        } catch (IOException | SecurityException ex) {
            LOGGER.log(Level.WARNING, "[EngineGallery] Upload path non utilizzabile: " + path, ex);
            return null;
        }
    }

    private static final class PathCandidate {
        private final Path path;
        private final String source;

        private PathCandidate(Path path, String source) {
            this.path = path;
            this.source = source;
        }
    }
}
