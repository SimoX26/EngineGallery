package it.SimoSW.controller.gui;

import it.SimoSW.util.UploadPathResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadHydraulicVideoServletTest {
    @TempDir Path uploadRoot;
    private final String previousUploadProperty = System.getProperty(UploadPathResolver.SYS_PROP);

    @AfterEach
    void resetUploadPath() throws Exception {
        if (previousUploadProperty == null) System.clearProperty(UploadPathResolver.SYS_PROP);
        else System.setProperty(UploadPathResolver.SYS_PROP, previousUploadProperty);
        resetCachedUploadRoot();
    }

    @Test
    void servesOnlyRequestedVideoBytesWithPartialContentHeaders() throws Exception {
        System.setProperty(UploadPathResolver.SYS_PROP, uploadRoot.toString());
        resetCachedUploadRoot();
        byte[] video = new byte[100];
        for (int index = 0; index < video.length; index++) video[index] = (byte) index;
        Path videoDir = Files.createDirectories(uploadRoot.resolve("hydraulic"));
        Files.write(videoDir.resolve("test.mp4"), video);

        VideoResponse response = new VideoResponse();
        UploadHydraulicVideoServlet servlet = new UploadHydraulicVideoServlet() {
            @Override public ServletContext getServletContext() { return null; }
        };

        servlet.doGet(request("bytes=10-19"), response.response());

        assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, response.status);
        assertEquals("bytes", response.headers.get("Accept-Ranges"));
        assertEquals("bytes 10-19/100", response.headers.get("Content-Range"));
        assertEquals(10L, response.contentLength);
        assertArrayEquals(Arrays.copyOfRange(video, 10, 20), response.body.toByteArray());

        VideoResponse invalid = new VideoResponse();
        servlet.doGet(request("bytes=100-"), invalid.response());
        assertEquals(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, invalid.status);
        assertEquals("bytes */100", invalid.headers.get("Content-Range"));
    }

    @Test
    void parsesPartialOpenAndSuffixRanges() {
        assertNull(UploadHydraulicVideoServlet.parseRange(null, 100));
        assertRange("bytes=10-19", 100, 10, 19);
        assertRange("bytes=90-", 100, 90, 99);
        assertRange("bytes=-15", 100, 85, 99);
        assertRange("bytes=0-999", 100, 0, 99);
    }

    @Test
    void rejectsUnsatisfiableAndMultipleRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> UploadHydraulicVideoServlet.parseRange("bytes=100-", 100));
        assertThrows(IllegalArgumentException.class,
                () -> UploadHydraulicVideoServlet.parseRange("bytes=20-10", 100));
        assertThrows(IllegalArgumentException.class,
                () -> UploadHydraulicVideoServlet.parseRange("bytes=0-1,5-6", 100));
        assertThrows(IllegalArgumentException.class,
                () -> UploadHydraulicVideoServlet.parseRange("bytes=-0", 100));
    }

    private static void assertRange(String header, long length, long start, long end) {
        UploadHydraulicVideoServlet.ByteRange range = UploadHydraulicVideoServlet.parseRange(header, length);
        assertEquals(start, range.start);
        assertEquals(end, range.end);
    }

    private static HttpServletRequest request(String range) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(), new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getPathInfo".equals(method.getName())) return "/test.mp4";
                    if ("getHeader".equals(method.getName())) return range;
                    return null;
                });
    }

    private static void resetCachedUploadRoot() throws Exception {
        Field field = UploadPathResolver.class.getDeclaredField("cachedUploadRoot");
        field.setAccessible(true);
        field.set(null, null);
    }

    private static final class VideoResponse {
        private final Map<String, String> headers = new HashMap<>();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private int status = 200;
        private long contentLength;

        private HttpServletResponse response() {
            ServletOutputStream output = new ServletOutputStream() {
                @Override public void write(int value) { body.write(value); }
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener listener) { }
            };
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(), new Class<?>[]{HttpServletResponse.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setStatus": status = (Integer) args[0]; return null;
                            case "setHeader": headers.put((String) args[0], (String) args[1]); return null;
                            case "setContentLengthLong": contentLength = (Long) args[0]; return null;
                            case "getOutputStream": return output;
                            default: return null;
                        }
                    });
        }
    }
}
