package it.SimoSW.controller.gui;

import it.SimoSW.util.UploadPathResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/uploads/hydraulic/*")
public class UploadHydraulicVideoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String filename = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Filename invalido");
            return;
        }

        Path uploadBase = UploadPathResolver.resolveHydraulicUploadBase(getServletContext());
        Path videoPath = uploadBase.resolve(filename).normalize();
        if (!videoPath.startsWith(uploadBase)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Percorso file non valido");
            return;
        }

        File videoFile = videoPath.toFile();
        if (!videoFile.exists() || !videoFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = Files.probeContentType(videoFile.toPath());
        if (mimeType == null || !mimeType.startsWith("video/")) {
            mimeType = "video/mp4";
        }

        long fileLength = videoFile.length();
        ByteRange range;
        try {
            range = parseRange(request.getHeader("Range"), fileLength);
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            response.setHeader("Content-Range", "bytes */" + fileLength);
            response.setHeader("Accept-Ranges", "bytes");
            response.setContentLength(0);
            return;
        }

        response.setContentType(mimeType);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        long start = range == null ? 0 : range.start;
        long end = range == null ? fileLength - 1 : range.end;
        if (range != null) {
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        }
        response.setContentLengthLong(end - start + 1);

        try (FileInputStream fis = new FileInputStream(videoFile);
             OutputStream os = response.getOutputStream()) {
            fis.getChannel().position(start);
            byte[] buffer = new byte[8192];
            long remaining = end - start + 1;
            int bytesRead;
            while (remaining > 0 && (bytesRead = fis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                os.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            os.flush();
        }
    }

    static ByteRange parseRange(String header, long length) {
        if (header == null || header.isBlank()) return null;
        if (!header.startsWith("bytes=") || header.indexOf(',') >= 0 || length <= 0) {
            throw new IllegalArgumentException("Range non valido");
        }
        String[] bounds = header.substring(6).split("-", -1);
        if (bounds.length != 2 || (bounds[0].isEmpty() && bounds[1].isEmpty())) {
            throw new IllegalArgumentException("Range non valido");
        }
        try {
            if (bounds[0].isEmpty()) {
                long suffix = Long.parseLong(bounds[1]);
                if (suffix <= 0) throw new IllegalArgumentException("Range non valido");
                return new ByteRange(Math.max(0, length - suffix), length - 1);
            }
            long start = Long.parseLong(bounds[0]);
            long end = bounds[1].isEmpty() ? length - 1 : Math.min(Long.parseLong(bounds[1]), length - 1);
            if (start < 0 || start >= length || end < start) {
                throw new IllegalArgumentException("Range non valido");
            }
            return new ByteRange(start, end);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Range non valido", ex);
        }
    }

    static final class ByteRange {
        final long start;
        final long end;

        ByteRange(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
