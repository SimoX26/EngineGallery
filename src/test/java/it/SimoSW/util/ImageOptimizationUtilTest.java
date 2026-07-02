package it.SimoSW.util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageOptimizationUtilTest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void keepsOrientationOneUnchanged() throws Exception {
        BufferedImage image = storeWithOrientation(1);
        assertEquals(40, image.getWidth());
        assertEquals(20, image.getHeight());
        assertLooksRed(image.getRGB(10, 10));
        assertLooksGreen(image.getRGB(30, 10));
    }

    @Test
    void rotatesOrientationThree() throws Exception {
        BufferedImage image = storeWithOrientation(3);
        assertEquals(40, image.getWidth());
        assertEquals(20, image.getHeight());
        assertLooksGreen(image.getRGB(10, 10));
        assertLooksRed(image.getRGB(30, 10));
    }

    @Test
    void rotatesOrientationSix() throws Exception {
        BufferedImage image = storeWithOrientation(6);
        assertEquals(20, image.getWidth());
        assertEquals(40, image.getHeight());
        assertLooksRed(image.getRGB(10, 10));
        assertLooksGreen(image.getRGB(10, 30));
    }

    @Test
    void rotatesOrientationEight() throws Exception {
        BufferedImage image = storeWithOrientation(8);
        assertEquals(20, image.getWidth());
        assertEquals(40, image.getHeight());
        assertLooksGreen(image.getRGB(10, 10));
        assertLooksRed(image.getRGB(10, 30));
    }

    private static BufferedImage storeWithOrientation(int orientation) throws Exception {
        byte[] jpegBytes = createJpegWithExifOrientation(orientation);
        Part part = new InMemoryPart("test.jpg", "image/jpeg", jpegBytes);

        Path targetDir = Files.createTempDirectory("enginegallery-image-test");
        String storedFilename = ImageOptimizationUtil.storeOptimizedImage(part, targetDir);
        assertTrue(storedFilename.toLowerCase(Locale.ROOT).endsWith(".jpg"));

        BufferedImage result = ImageIO.read(targetDir.resolve(storedFilename).toFile());
        assertTrue(result != null, "Stored image should be readable");
        return result;
    }

    private static byte[] createJpegWithExifOrientation(int orientation) throws IOException {
        BufferedImage source = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                source.setRGB(x, y, x < 20 ? 0x00FF0000 : 0x0000FF00);
            }
        }

        ByteArrayOutputStream jpegOut = new ByteArrayOutputStream();
        ImageIO.write(source, "jpg", jpegOut);
        byte[] jpeg = jpegOut.toByteArray();

        byte[] exifPayload = buildExifPayload(orientation);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.write(jpeg, 0, 2);
        result.write(0xFF);
        result.write(0xE1);
        int app1Length = exifPayload.length + 2;
        result.write((app1Length >>> 8) & 0xFF);
        result.write(app1Length & 0xFF);
        result.write(exifPayload);
        result.write(jpeg, 2, jpeg.length - 2);
        return result.toByteArray();
    }

    private static byte[] buildExifPayload(int orientation) {
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) 'E');
        buffer.put((byte) 'x');
        buffer.put((byte) 'i');
        buffer.put((byte) 'f');
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 'M');
        buffer.put((byte) 'M');
        buffer.putShort((short) 0x002A);
        buffer.putInt(8);
        buffer.putShort((short) 1);
        buffer.putShort((short) 0x0112);
        buffer.putShort((short) 3);
        buffer.putInt(1);
        buffer.putShort((short) orientation);
        buffer.putShort((short) 0);
        buffer.putInt(0);
        return buffer.array();
    }

    private static void assertLooksRed(int actualRgb) {
        int red = (actualRgb >> 16) & 0xFF;
        int green = (actualRgb >> 8) & 0xFF;
        int blue = actualRgb & 0xFF;
        assertTrue(red > green + 40, "Pixel should remain predominantly red");
        assertTrue(red > blue + 40, "Pixel should remain predominantly red");
    }

    private static void assertLooksGreen(int actualRgb) {
        int red = (actualRgb >> 16) & 0xFF;
        int green = (actualRgb >> 8) & 0xFF;
        int blue = actualRgb & 0xFF;
        assertTrue(green > red + 20, "Pixel should remain predominantly green");
        assertTrue(green > blue + 20, "Pixel should remain predominantly green");
    }

    private static final class InMemoryPart implements Part {
        private final String fileName;
        private final String contentType;
        private final byte[] bytes;

        private InMemoryPart(String fileName, String contentType, byte[] bytes) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return "images";
        }

        @Override
        public String getSubmittedFileName() {
            return fileName;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public void write(String fileName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getHeaderNames() {
            return Collections.emptyList();
        }
    }
}
