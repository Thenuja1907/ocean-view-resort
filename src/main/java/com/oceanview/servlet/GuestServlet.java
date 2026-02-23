package com.oceanview.servlet;

import com.oceanview.model.Guest;
import com.oceanview.model.Guest.IdType;
import com.oceanview.service.GuestService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * GuestServlet — REST-ish API for /api/guests/*.
 *
 * GET /api/guests → list all guests
 * GET /api/guests?q=... → search guests
 * GET /api/guests/{id} → get one guest
 * POST /api/guests → register new guest
 * PUT /api/guests/{id} → update guest
 * DELETE /api/guests/{id} → delete guest (admin)
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
        requireSession(req, resp);
        String path = normalise(req.getPathInfo());

        try {
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
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        requireSession(req, resp);
        try {
            Guest guest = buildFromRequest(req, new Guest());
            guestService.registerGuest(guest);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.ok("Guest registered.", guest));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        requireSession(req, resp);
        try {
            int id = Integer.parseInt(normalise(req.getPathInfo()).substring(1));
            Guest guest = guestService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + id));
            buildFromRequest(req, guest);
            guestService.updateGuest(guest);
            resp.getWriter().write(JsonUtil.ok("Guest updated.", guest));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        requireSession(req, resp);
        try {
            int id = Integer.parseInt(normalise(req.getPathInfo()).substring(1));
            guestService.deleteGuest(id);
            resp.getWriter().write(JsonUtil.ok("Guest deleted.", null));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Guest buildFromRequest(HttpServletRequest req, Guest g) {
        g.setFirstName(req.getParameter("firstName"));
        g.setLastName(req.getParameter("lastName"));
        g.setEmail(req.getParameter("email"));
        g.setContactNumber(req.getParameter("contactNumber"));
        g.setAddress(req.getParameter("address"));
        g.setIdType(IdType.valueOf(req.getParameter("idType")));
        g.setIdNumber(req.getParameter("idNumber"));
        g.setNationality(req.getParameter("nationality"));
        return g;
    }

    private String normalise(String p) {
        return p == null ? "/" : p;
    }

    private void requireSession(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            try {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(JsonUtil.error("Not authenticated."));
            } catch (IOException ignored) {
            }
        }
    }
}
