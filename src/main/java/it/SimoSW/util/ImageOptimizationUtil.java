package it.SimoSW.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.Part;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

public final class ImageOptimizationUtil {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final float JPEG_QUALITY = 0.82f;
    private static final int EXIF_ORIENTATION_TAG = 0x0112;

    private ImageOptimizationUtil() {
    }

    public static String storeOptimizedImage(Part part, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);
        String submittedName = part.getSubmittedFileName();
        String originalName = submittedName == null ? "image.jpg" : Paths.get(submittedName).getFileName().toString();
        String sanitizedOriginal = sanitizeFilename(originalName);
        byte[] sourceBytes;

        try (InputStream is = part.getInputStream()) {
            sourceBytes = is.readAllBytes();
        }

        BufferedImage source;
        try (ByteArrayInputStream is = new ByteArrayInputStream(sourceBytes)) {
            source = ImageIO.read(is);
        }

        if (source == null) {
            String fallbackFilename = UUID.randomUUID() + "_" + sanitizedOriginal;
            Path fallbackDestination = safeResolve(targetDir, fallbackFilename);
            Files.write(fallbackDestination, sourceBytes);
            return fallbackFilename;
        }

        int exifOrientation = readExifOrientation(sourceBytes);
        BufferedImage oriented = applyExifOrientation(source, exifOrientation);
        BufferedImage resized = resizeIfNeeded(oriented, MAX_WIDTH, MAX_HEIGHT);
        BufferedImage rgbImage = ensureRgb(resized);

        String outputFilename = UUID.randomUUID() + ".jpg";
        Path outputPath = safeResolve(targetDir, outputFilename);
        writeJpeg(rgbImage, outputPath, JPEG_QUALITY);
        return outputFilename;
    }

    private static Path safeResolve(Path targetDir, String filename) {
        Path baseDir = targetDir.toAbsolutePath().normalize();
        Path destination = baseDir.resolve(filename).normalize();
        if (!destination.startsWith(baseDir)) {
            throw new IllegalArgumentException("Percorso destinazione non valido");
        }
        return destination;
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

    private static BufferedImage applyExifOrientation(BufferedImage source, int orientation) {
        if (orientation == 1) {
            return source;
        }

        int width = source.getWidth();
        int height = source.getHeight();
        AffineTransform transform = new AffineTransform();
        int targetWidth = width;
        int targetHeight = height;

        switch (orientation) {
            case 3:
                transform.translate(width, height);
                transform.rotate(Math.PI);
                break;
            case 6:
                transform.translate(height, 0);
                transform.rotate(Math.PI / 2);
                targetWidth = height;
                targetHeight = width;
                break;
            case 8:
                transform.translate(0, width);
                transform.rotate(-Math.PI / 2);
                targetWidth = height;
                targetHeight = width;
                break;
            default:
                return source;
        }

        int imageType = source.getType() == BufferedImage.TYPE_CUSTOM ? BufferedImage.TYPE_INT_ARGB : source.getType();
        BufferedImage destination = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D g2 = destination.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(source, new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC), 0, 0);
        } finally {
            g2.dispose();
        }
        return destination;
    }

    private static int readExifOrientation(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 4 || !isJpeg(imageBytes)) {
            return 1;
        }

        int offset = 2;
        while (offset + 4 <= imageBytes.length) {
            if ((imageBytes[offset] & 0xFF) != 0xFF) {
                break;
            }

            int marker = imageBytes[offset + 1] & 0xFF;
            offset += 2;

            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }
            if (offset + 2 > imageBytes.length) {
                break;
            }

            int segmentLength = readUnsignedShortBigEndian(imageBytes, offset);
            if (segmentLength < 2 || offset + segmentLength > imageBytes.length) {
                break;
            }

            if (marker == 0xE1 && segmentLength >= 8 && hasExifHeader(imageBytes, offset + 2)) {
                int orientation = parseExifOrientation(imageBytes, offset + 2 + 6, segmentLength - 8);
                if (orientation == 3 || orientation == 6 || orientation == 8) {
                    return orientation;
                }
                return 1;
            }

            offset += segmentLength;
        }
        return 1;
    }

    private static int parseExifOrientation(byte[] imageBytes, int tiffStart, int availableLength) {
        if (availableLength < 8 || tiffStart + 8 > imageBytes.length) {
            return 1;
        }

        ByteOrder byteOrder;
        int byteOrderMarker = readUnsignedShortBigEndian(imageBytes, tiffStart);
        if (byteOrderMarker == 0x4949) {
            byteOrder = ByteOrder.LITTLE_ENDIAN;
        } else if (byteOrderMarker == 0x4D4D) {
            byteOrder = ByteOrder.BIG_ENDIAN;
        } else {
            return 1;
        }

        int tiffTagMarker = readUnsignedShort(imageBytes, tiffStart + 2, byteOrder);
        if (tiffTagMarker != 0x002A) {
            return 1;
        }

        int ifdOffset = readInt(imageBytes, tiffStart + 4, byteOrder);
        int ifdStart = tiffStart + ifdOffset;
        if (ifdOffset < 0 || ifdStart + 2 > imageBytes.length) {
            return 1;
        }

        int entryCount = readUnsignedShort(imageBytes, ifdStart, byteOrder);
        int entryOffset = ifdStart + 2;
        for (int i = 0; i < entryCount; i++) {
            int entryStart = entryOffset + i * 12;
            if (entryStart + 12 > imageBytes.length) {
                return 1;
            }

            int tag = readUnsignedShort(imageBytes, entryStart, byteOrder);
            if (tag != EXIF_ORIENTATION_TAG) {
                continue;
            }

            int type = readUnsignedShort(imageBytes, entryStart + 2, byteOrder);
            int count = readInt(imageBytes, entryStart + 4, byteOrder);
            if (type != 3 || count < 1) {
                return 1;
            }

            return readUnsignedShort(imageBytes, entryStart + 8, byteOrder);
        }
        return 1;
    }

    private static boolean isJpeg(byte[] imageBytes) {
        return (imageBytes[0] & 0xFF) == 0xFF && (imageBytes[1] & 0xFF) == 0xD8;
    }

    private static boolean hasExifHeader(byte[] imageBytes, int offset) {
        return offset + 6 <= imageBytes.length
                && imageBytes[offset] == 'E'
                && imageBytes[offset + 1] == 'x'
                && imageBytes[offset + 2] == 'i'
                && imageBytes[offset + 3] == 'f'
                && imageBytes[offset + 4] == 0
                && imageBytes[offset + 5] == 0;
    }

    private static int readUnsignedShortBigEndian(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 8) | (bytes[offset + 1] & 0xFF);
    }

    private static int readUnsignedShort(byte[] bytes, int offset, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bytes, offset, 2).order(byteOrder).getShort() & 0xFFFF;
    }

    private static int readInt(byte[] bytes, int offset, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bytes, offset, 4).order(byteOrder).getInt();
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
