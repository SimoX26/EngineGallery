package it.SimoSW.util;

import javax.servlet.ServletContext;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPathResolver {
    public static final String SYS_PROP = "enginegallery.upload.dir";
    public static final String ENV_VAR = "ENGINE_GALLERY_UPLOAD_DIR";

    private UploadPathResolver() {
    }

    public static Path resolveUploadBase(ServletContext context) {
        return resolveUploadRoot(context).resolve("engines").normalize();
    }

    public static Path resolveHydraulicUploadBase(ServletContext context) {
        return resolveUploadRoot(context).resolve("hydraulic").normalize();
    }

    private static Path resolveUploadRoot(ServletContext context) {
        String configured = System.getProperty(SYS_PROP);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(ENV_VAR);
        }
        if (configured != null && !configured.isBlank()) {
            Path configuredPath = Paths.get(configured).toAbsolutePath().normalize();
            String lastSegment = configuredPath.getFileName() != null
                    ? configuredPath.getFileName().toString()
                    : "";
            if ("engines".equalsIgnoreCase(lastSegment) || "hydraulic".equalsIgnoreCase(lastSegment)) {
                return configuredPath.getParent() != null ? configuredPath.getParent() : configuredPath;
            }
            return configuredPath;
        }

        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            return Paths.get(userHome, "EngineGallery", "uploads")
                    .toAbsolutePath()
                    .normalize();
        }

        if (context != null) {
            String realPath = context.getRealPath("/uploads");
            if (realPath != null) {
                return Paths.get(realPath).toAbsolutePath().normalize();
            }
        }

        return Paths.get("uploads").toAbsolutePath().normalize();
    }
}
