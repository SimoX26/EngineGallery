package it.SimoSW.controller.gui;

import it.SimoSW.model.User;
import it.SimoSW.model.UserRole;
import org.junit.jupiter.api.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeNavigationTest {

    @Test
    void legacyDashboardRedirectKeepsNavigationParameters() throws Exception {
        AtomicReference<String> destination = new AtomicReference<>();
        HttpServletRequest legacyRequest = proxy(HttpServletRequest.class, (method, args) -> {
            if ("getServletPath".equals(method)) return "/dashboard";
            if ("getQueryString".equals(method)) return "lockBack=1&navHome=1";
            if ("getContextPath".equals(method)) return "/app";
            return null;
        });
        HttpServletResponse response = response(destination);

        new HomeServlet().doGet(legacyRequest, response);

        assertEquals("/app/home?lockBack=1&navHome=1", destination.get());
    }

    @Test
    void authenticatedHomeReachesProtectedPageWithRoleAttributes() throws Exception {
        User user = new User("simone", "hash", UserRole.ADMIN);
        Map<String, Object> sessionValues = new HashMap<>();
        sessionValues.put("loggedUser", user);
        Map<String, Object> requestValues = new HashMap<>();
        AtomicBoolean passed = new AtomicBoolean();
        AtomicReference<String> destination = new AtomicReference<>();

        new AuthenticationFilter().doFilter(
                request("/app/home", sessionValues, requestValues, null),
                response(destination),
                (req, res) -> passed.set(true));

        assertTrue(passed.get());
        assertNull(destination.get());
        assertEquals(true, requestValues.get("canViewWarehouse"));
        assertEquals(true, requestValues.get("canViewStatistics"));
    }

    @Test
    void unauthenticatedHomeStillShowsLogin() throws Exception {
        AtomicReference<String> forwarded = new AtomicReference<>();
        AtomicBoolean passed = new AtomicBoolean();
        new AuthenticationFilter().doFilter(
                request("/app/home", new HashMap<>(), new HashMap<>(), forwarded),
                response(new AtomicReference<>()),
                (req, res) -> passed.set(true));

        assertEquals("/WEB-INF/views/auth/login.jsp", forwarded.get());
        assertFalse(passed.get());
    }

    @Test
    void authenticatedLoginEntryRedirectsToHome() throws Exception {
        User user = new User("simone", "hash", UserRole.ADMIN);
        Map<String, Object> sessionValues = new HashMap<>();
        sessionValues.put("loggedUser", user);
        AtomicReference<String> destination = new AtomicReference<>();
        AtomicBoolean passed = new AtomicBoolean();

        new AuthenticationFilter().doFilter(
                request("/app/auth", sessionValues, new HashMap<>(), null),
                response(destination),
                (req, res) -> passed.set(true));

        assertEquals("/app/home", destination.get());
        assertFalse(passed.get());
    }

    private static HttpServletRequest request(String uri, Map<String, Object> sessionValues,
                                              Map<String, Object> requestValues, AtomicReference<String> forwarded) {
        Map<String, Object> values = sessionValues == null ? new HashMap<>() : sessionValues;
        HttpSession session = proxy(HttpSession.class, (method, args) -> {
            if ("getAttribute".equals(method)) return values.get(args[0]);
            if ("setAttribute".equals(method)) values.put((String) args[0], args[1]);
            return null;
        });
        return proxy(HttpServletRequest.class, (method, args) -> {
            if ("getRequestURI".equals(method)) return uri;
            if ("getContextPath".equals(method)) return "/app";
            if ("getServletPath".equals(method)) return uri.substring(4);
            if ("getMethod".equals(method)) return "GET";
            if ("getSession".equals(method)) return session;
            if ("setAttribute".equals(method) && requestValues != null) {
                requestValues.put((String) args[0], args[1]);
            }
            if ("getRequestDispatcher".equals(method) && forwarded != null) {
                String path = (String) args[0];
                return proxy(RequestDispatcher.class, (dispatcherMethod, ignored) -> {
                    if ("forward".equals(dispatcherMethod)) forwarded.set(path);
                    return null;
                });
            }
            return null;
        });
    }

    private static HttpServletResponse response(AtomicReference<String> destination) {
        return proxy(HttpServletResponse.class, (method, args) -> {
            if ("sendRedirect".equals(method)) destination.set((String) args[0]);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Handler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (instance, method, args) -> handler.invoke(method.getName(), args));
    }

    @FunctionalInterface
    private interface Handler {
        Object invoke(String method, Object[] args) throws Exception;
    }
}
