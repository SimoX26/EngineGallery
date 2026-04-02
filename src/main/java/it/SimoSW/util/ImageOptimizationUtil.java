package it.SimoSW.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.Part;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

public final class ImageOptimizationUtil {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final float JPEG_QUALITY = 0.82f;

    private ImageOptimizationUtil() {
    }

    public static String storeOptimizedImage(Part part, Path targetDir) throws IOException {
        String submittedName = part.getSubmittedFileName();
        String originalName = submittedName == null ? "image.jpg" : Paths.get(submittedName).getFileName().toString();
        String sanitizedOriginal = sanitizeFilename(originalName);

        BufferedImage source;
        try (InputStream is = part.getInputStream()) {
            source = ImageIO.read(is);
        }

        if (source == null) {
            String fallbackFilename = UUID.randomUUID() + "_" + sanitizedOriginal;
            Path fallbackDestination = targetDir.resolve(fallbackFilename).normalize();
            part.write(fallbackDestination.toString());
            return fallbackFilename;
        }

        BufferedImage resized = resizeIfNeeded(source, MAX_WIDTH, MAX_HEIGHT);
        BufferedImage rgbImage = ensureRgb(resized);

        String outputFilename = UUID.randomUUID() + ".jpg";
        Path outputPath = targetDir.resolve(outputFilename).normalize();
        writeJpeg(rgbImage, outputPath, JPEG_QUALITY);
        return outputFilename;
    }

    private static BufferedImage resizeIfNeeded(BufferedImage source, int maxWidth, int maxHeight) {
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();

        if (srcWidth <= maxWidth && srcHeight <= maxHeight) {
            return source;
        }

        double widthRatio = (double) maxWidth / (double) srcWidth;
        double heightRatio = (double) maxHeight / (double) srcHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = Math.max(1, (int) Math.round(srcWidth * ratio));
        int newHeight = Math.max(1, (int) Math.round(srcHeight * ratio));

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resized.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(source, 0, 0, newWidth, newHeight, null);
        } finally {
            g2.dispose();
        }
        return resized;
    }

    private static BufferedImage ensureRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }

        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = rgb.createGraphics();
        try {
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            g2.drawImage(source, 0, 0, null);
        } finally {
            g2.dispose();
        }
        return rgb;
    }

    private static void writeJpeg(BufferedImage image, Path destination, float quality) throws IOException {
        Files.createDirectories(destination.getParent());
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("Nessun writer JPEG disponibile");
        }

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(Files.newOutputStream(destination))) {
            writer.setOutput(ios);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(quality);
            }
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
    }

    private static String sanitizeFilename(String filename) {
        String value = (filename == null || filename.isBlank()) ? "image.jpg" : filename;
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
            return "image.jpg";
        }
        String lower = sanitized.toLowerCase(Locale.ROOT);
        if (!lower.contains(".")) {
            return sanitized + ".jpg";
        }
        return sanitized;
    }
}
