package com.oceanview.servlet;

import com.oceanview.model.Guest;
import com.oceanview.model.Guest.IdType;
import com.oceanview.service.GuestService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GuestServlet — REST-ish API for /api/guests/*.
 */
@WebServlet("/api/guests/*")
public class GuestServlet extends HttpServlet {

    private GuestService guestService;

    @Override
    public void init() {
        guestService = (GuestService) getServletContext().getAttribute("guestService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            requireSession(req);
            String path = normalise(req.getPathInfo());

            if (path.equals("/") || path.isEmpty()) {
                String q = req.getParameter("q");
                List<Guest> guests = (q != null && !q.isBlank())
                        ? guestService.search(q)
                        : guestService.findAll();
                resp.getWriter().write(JsonUtil.ok(guests));
            } else {
                int id = Integer.parseInt(path.substring(1));
                Optional<Guest> opt = guestService.findById(id);
                if (opt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("Guest not found."));
                } else {
                    resp.getWriter().write(JsonUtil.ok(opt.get()));
                }
            }
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            Guest guest = buildFromRequest(req, new Guest());
            Guest registered = guestService.registerGuest(guest);
            // Automatically log in the user after registration
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", registered);
            session.setAttribute("userType", "guest");

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.ok("Guest registered and logged in.", registered));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            requireSession(req);
            int id = Integer.parseInt(normalise(req.getPathInfo()).substring(1));
            Guest guest = guestService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + id));

            // Servlets don't parse body for PUT — read it manually
            Map<String, String> params = parseBodyParams(req);
            buildFromMap(params, guest);

            guestService.updateGuest(guest);
            resp.getWriter().write(JsonUtil.ok("Guest updated.", guest));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        try {
            requireSession(req);
            int id = Integer.parseInt(normalise(req.getPathInfo()).substring(1));
            guestService.deleteGuest(id);
            resp.getWriter().write(JsonUtil.ok("Guest deleted.", null));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    /**
     * Parses application/x-www-form-urlencoded body from a PUT request.
     * Servlet containers don't auto-parse the body for PUT (only POST).
     */
    private Map<String, String> parseBodyParams(HttpServletRequest req) throws IOException {
        Map<String, String> params = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }
        String body = sb.toString();
        if (body.isBlank())
            return params;
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else if (kv.length == 1) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    /**
     * Builds / updates Guest fields from a Map (used for PUT where body must be
     * parsed manually).
     */
    private Guest buildFromMap(Map<String, String> p, Guest g) {
        if (p.containsKey("firstName"))
            g.setFirstName(p.get("firstName"));
        if (p.containsKey("lastName"))
            g.setLastName(p.get("lastName"));
        if (p.containsKey("email"))
            g.setEmail(p.get("email"));
        if (p.containsKey("contactNumber"))
            g.setContactNumber(p.get("contactNumber"));
        if (p.containsKey("address"))
            g.setAddress(p.get("address"));
        if (p.containsKey("nationality"))
            g.setNationality(p.get("nationality"));
        if (p.containsKey("idType"))
            g.setIdType(IdType.valueOf(p.get("idType")));
        if (p.containsKey("idNumber"))
            g.setIdNumber(p.get("idNumber"));
        String pwd = p.get("password");
        if (pwd != null && !pwd.isBlank())
            g.setPasswordHash(pwd);
        return g;
    }

    private Guest buildFromRequest(HttpServletRequest req, Guest g) {
        g.setFirstName(req.getParameter("firstName"));
        g.setLastName(req.getParameter("lastName"));
        g.setEmail(req.getParameter("email"));
        g.setContactNumber(req.getParameter("contactNumber"));
        g.setAddress(req.getParameter("address"));
        g.setIdType(IdType.valueOf(req.getParameter("idType")));
        g.setIdNumber(req.getParameter("idNumber"));
        g.setNationality(req.getParameter("nationality"));

        String pwd = req.getParameter("password");
        if (pwd != null && !pwd.isBlank()) {
            g.setPasswordHash(pwd);
        }
        return g;
    }

    private String normalise(String p) {
        return p == null ? "/" : p;
    }

    private void requireSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            throw new SecurityException("Not authenticated.");
        }
    }
}
