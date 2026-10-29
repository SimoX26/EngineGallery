package it.SimoSW.controller.gui;

import it.SimoSW.model.User;
import it.SimoSW.util.security.CsrfTokenUtil;
import org.junit.jupiter.api.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateFormSubmissionFilterTest {
    @Test
    void successfulHydraulicSubmissionIsCreatedOnlyOnce() throws Exception {
        HttpSession session = session();
        HttpServletRequest request = request(session, "/hydraulic-test/new", null);
        CreateFormSubmissionFilter filter = new CreateFormSubmissionFilter();
        AtomicInteger inserts = new AtomicInteger();
        FilterChain create = (req, res) -> {
            inserts.incrementAndGet();
            ((HttpServletResponse) res).sendRedirect("/app/hydraulic-test/list?lockBack=1");
        };

        FakeResponse first = new FakeResponse();
        filter.doFilter(request, first.response(), create);
        FakeResponse second = new FakeResponse();
        filter.doFilter(request, second.response(), create);

        assertEquals(1, inserts.get());
        assertTrue(first.body.toString().contains("\"saved\":true"));
        assertTrue(second.body.toString().contains("\"saved\":true"));
        assertTrue(second.body.toString().contains("/app/hydraulic-test/list?lockBack=1"));
    }

    @Test
    void failedValidationDoesNotConsumeSubmission() throws Exception {
        HttpSession session = session();
        HttpServletRequest request = request(session, "/workshop-maintenance", "createMachine");
        CreateFormSubmissionFilter filter = new CreateFormSubmissionFilter();
        AtomicInteger attempts = new AtomicInteger();
        FilterChain create = (req, res) -> {
            if (attempts.incrementAndGet() == 2) {
                ((HttpServletResponse) res).sendRedirect(
                        "/app/workshop-maintenance?view=detail&id=12&saved=1");
            }
        };

        filter.doFilter(request, new FakeResponse().response(), create);
        FakeResponse retry = new FakeResponse();
        filter.doFilter(request, retry.response(), create);
        filter.doFilter(request, new FakeResponse().response(), create);

        assertEquals(2, attempts.get());
        assertTrue(retry.body.toString().contains("\"saved\":true"));
    }

    @Test
    void repeatedNativeVehicleSubmissionRedirectsWithoutCreatingAgain() throws Exception {
        HttpServletRequest request = request(session(), "/vehicles", "createVehicle", false);
        CreateFormSubmissionFilter filter = new CreateFormSubmissionFilter();
        AtomicInteger inserts = new AtomicInteger();
        FilterChain create = (req, res) -> {
            inserts.incrementAndGet();
            ((HttpServletResponse) res).sendRedirect("/app/vehicles?view=detail&id=7&saved=1");
        };

        FakeResponse first = new FakeResponse();
        filter.doFilter(request, first.response(), create);
        FakeResponse second = new FakeResponse();
        filter.doFilter(request, second.response(), create);

        assertEquals(1, inserts.get());
        assertEquals(302, first.status);
        assertEquals(302, second.status);
        assertEquals("/app/vehicles?view=detail&id=7&saved=1", second.headers.get("Location"));
    }

    private static HttpSession session() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("loggedUser", new User());
        attributes.put(CsrfTokenUtil.SESSION_ATTR, "valid-token");
        return (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class}, (proxy, method, args) -> {
                    if ("getAttribute".equals(method.getName())) return attributes.get(args[0]);
                    if ("setAttribute".equals(method.getName())) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    return null;
                });
    }

    private static HttpServletRequest request(HttpSession session, String path, String action) {
        return request(session, path, action, true);
    }

    private static HttpServletRequest request(HttpSession session, String path, String action, boolean draft) {
        return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class}, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getMethod": return "POST";
                        case "getHeader": return draft && "X-Engine-Gallery-Submission".equals(args[0])
                                ? "draft-123" : null;
                        case "getServletPath": return path;
                        case "getSession": return session;
                        case "getContextPath": return "/app";
                        case "getRequestURL": return new StringBuffer("http://localhost/app" + path);
                        case "getParameter":
                            if (CsrfTokenUtil.PARAM.equals(args[0])) return "valid-token";
                            if ("action".equals(args[0])) return action;
                            if ("submissionToken".equals(args[0])) return "native-123";
                            return null;
                        default: return null;
                    }
                });
    }

    private static final class FakeResponse {
        private final Map<String, String> headers = new HashMap<>();
        private final StringWriter body = new StringWriter();
        private final PrintWriter writer = new PrintWriter(body);
        private int status = 200;

        private HttpServletResponse response() {
            return (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                    new Class<?>[]{HttpServletResponse.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "setHeader": headers.put((String) args[0], (String) args[1]); return null;
                            case "getHeader": return headers.get(args[0]);
                            case "getStatus": return status;
                            case "setContentType": headers.put("Content-Type", (String) args[0]); return null;
                            case "getWriter": return writer;
                            case "sendRedirect":
                                status = 302;
                                headers.put("Location", (String) args[0]);
                                return null;
                            default: return null;
                        }
                    });
        }
    }
}
