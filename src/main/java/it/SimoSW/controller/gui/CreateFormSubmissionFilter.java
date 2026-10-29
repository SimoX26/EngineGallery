package it.SimoSW.controller.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.SimoSW.model.User;
import it.SimoSW.util.security.CsrfTokenUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@WebFilter(urlPatterns = {"/hydraulic-test/new", "/warehouse/new", "/upload",
        "/ready-delivery", "/workshop-maintenance", "/vehicles"})
public class CreateFormSubmissionFilter implements Filter {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String SESSION_KEY = CreateFormSubmissionFilter.class.getName() + ".completed";
    private static final String SUBMISSION_HEADER = "X-Engine-Gallery-Submission";
    private static final String SAVED_REDIRECT_HEADER = "X-Engine-Gallery-Saved-Redirect";
    private static final Set<String> WORKSHOP_CREATE_ACTIONS = Set.of(
            "createMaterial", "createPlan", "createMachine", "recordIntervention");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        boolean draftRequest = req.getHeader(SUBMISSION_HEADER) != null;
        String token = draftRequest ? req.getHeader(SUBMISSION_HEADER) : req.getParameter("submissionToken");
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        if (!"POST".equalsIgnoreCase(req.getMethod()) || token == null || token.isBlank()
                || token.length() > 200 || session == null
                || !(session.getAttribute("loggedUser") instanceof User)
                || !CsrfTokenUtil.isValid(req) || !isCreateRequest(req, path)) {
            chain.doFilter(request, response);
            return;
        }

        String submission = path + ':' + token;
        synchronized (session) {
            Map<String, String> completed = completed(session);
            String previousDestination = completed.get(submission);
            if (previousDestination != null) {
                if (draftRequest) sendSavedResponse(res, previousDestination);
                else res.sendRedirect(previousDestination);
                return;
            }
            String[] savedDestination = new String[1];
            HttpServletResponseWrapper wrapped = new HttpServletResponseWrapper(res) {
                @Override
                public void sendRedirect(String location) throws IOException {
                    if (isSuccessDestination(req, path, location)) {
                        savedDestination[0] = location;
                        setHeader(SAVED_REDIRECT_HEADER, location);
                        if (draftRequest) sendSavedResponse(this, location);
                        else super.sendRedirect(location);
                    } else {
                        super.sendRedirect(location);
                    }
                }
            };
            try {
                chain.doFilter(request, wrapped);
            } finally {
                String destination = savedDestination[0] != null
                        ? savedDestination[0] : res.getHeader(SAVED_REDIRECT_HEADER);
                if (isSuccessDestination(req, path, destination)) {
                    if (completed.size() >= 64) {
                        completed.remove(completed.keySet().iterator().next());
                    }
                    completed.put(submission, destination);
                }
            }
        }
    }

    private static void sendSavedResponse(HttpServletResponse response, String destination) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        JSON.writeValue(response.getWriter(), Map.of("saved", true, "redirect", destination));
    }

    private static boolean isCreateRequest(HttpServletRequest request, String path) {
        if ("/workshop-maintenance".equals(path)) {
            return WORKSHOP_CREATE_ACTIONS.contains(request.getParameter("action"));
        }
        if ("/ready-delivery".equals(path)) {
            return "create".equals(request.getParameter("action"));
        }
        if ("/vehicles".equals(path)) {
            return "createVehicle".equals(request.getParameter("action"))
                    || "createEvent".equals(request.getParameter("action"));
        }
        return !"/upload".equals(path) || "new".equals(request.getParameter("engineMode"));
    }

    private static boolean isSuccessDestination(HttpServletRequest request, String path, String destination) {
        if (destination == null || destination.isBlank()) return false;
        URI uri;
        try {
            uri = URI.create(destination);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        URI requestUri = URI.create(request.getRequestURL().toString());
        URI target = requestUri.resolve(uri);
        if (!requestUri.getScheme().equalsIgnoreCase(target.getScheme())
                || !requestUri.getAuthority().equalsIgnoreCase(target.getAuthority())) return false;
        String targetPath = target.getPath();
        String query = target.getRawQuery() == null ? "" : target.getRawQuery();
        String base = request.getContextPath();
        if ("/hydraulic-test/new".equals(path)) {
            return (base + "/hydraulic-test/list").equals(targetPath) && hasFlag(query, "lockBack");
        }
        if ("/warehouse/new".equals(path)) {
            return (base + "/warehouse/detail").equals(targetPath) && hasFlag(query, "lockBack");
        }
        if ("/upload".equals(path)) {
            return (base + "/home").equals(targetPath) && hasFlag(query, "lockBack");
        }
        if ("/ready-delivery".equals(path)) {
            return (base + "/ready-delivery").equals(targetPath) && hasFlag(query, "saved");
        }
        if ("/vehicles".equals(path)) {
            return (base + "/vehicles").equals(targetPath) && hasFlag(query, "saved");
        }
        return (base + "/workshop-maintenance").equals(targetPath) && hasFlag(query, "saved");
    }

    private static boolean hasFlag(String query, String name) {
        for (String part : query.split("&")) {
            if ((name + "=1").equals(part)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> completed(HttpSession session) {
        Map<String, String> entries = (Map<String, String>) session.getAttribute(SESSION_KEY);
        if (entries == null) {
            entries = new LinkedHashMap<>();
            session.setAttribute(SESSION_KEY, entries);
        }
        return entries;
    }
}
