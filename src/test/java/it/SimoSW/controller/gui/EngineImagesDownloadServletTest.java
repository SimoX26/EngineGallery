package it.SimoSW.controller.gui;

import it.SimoSW.util.bean.ImageBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EngineImagesDownloadServletTest {
    @TempDir
    Path tempDir;

    @Test
    void archiveContainsAllPhotosEvenWhenNamesMatch() throws Exception {
        Path first = Files.write(tempDir.resolve("first.jpg"), "prima".getBytes(StandardCharsets.UTF_8));
        Path second = Files.write(tempDir.resolve("second.jpg"), "seconda".getBytes(StandardCharsets.UTF_8));
        ImageBean firstImage = new ImageBean();
        firstImage.setFilename("foto.jpg");
        ImageBean secondImage = new ImageBean();
        secondImage.setFilename("foto.jpg");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EngineImagesDownloadServlet.writeArchive(List.of(first, second), List.of(firstImage, secondImage), output);

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(output.toByteArray()))) {
            ZipEntry firstEntry = zip.getNextEntry();
            assertEquals("01-foto.jpg", firstEntry.getName());
            assertArrayEquals("prima".getBytes(StandardCharsets.UTF_8), zip.readAllBytes());
            ZipEntry secondEntry = zip.getNextEntry();
            assertEquals("02-foto.jpg", secondEntry.getName());
            assertArrayEquals("seconda".getBytes(StandardCharsets.UTF_8), zip.readAllBytes());
            assertNull(zip.getNextEntry());
        }
    }
}
