package com.oceanview.servlet;

import com.oceanview.model.Reservation;
import com.oceanview.model.Reservation.Status;
import com.oceanview.model.User;
import com.oceanview.service.ReservationService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ReservationServlet — REST-ish API for /api/reservations/*.
 *
 * GET /api/reservations → list all
 * GET /api/reservations?status=X → filter by status
 * GET /api/reservations/{id} → single reservation
 * POST /api/reservations → create
 * PUT /api/reservations/{id}/confirm → confirm
 * PUT /api/reservations/{id}/checkin → check-in
 * PUT /api/reservations/{id}/checkout → check-out
 * PUT /api/reservations/{id}/cancel → cancel
 */
@WebServlet("/api/reservations/*")
public class ReservationServlet extends HttpServlet {

    private ReservationService reservationService;

    @Override
    public void init() {
        reservationService = (ReservationService) getServletContext().getAttribute("reservationService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = normalise(req.getPathInfo());

        try {
            if (path.equals("/") || path.isEmpty()) {
                String statusParam = req.getParameter("status");
                List<Reservation> list = (statusParam != null && !statusParam.isBlank())
                        ? reservationService.findByStatus(Status.valueOf(statusParam.toUpperCase()))
                        : reservationService.findAll();
                resp.getWriter().write(JsonUtil.ok(list));
            } else {
                int id = Integer.parseInt(path.substring(1));
                Optional<Reservation> opt = reservationService.findById(id);
                if (opt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("Reservation not found."));
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
        User actor = currentUser(req);
        if (actor == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Not authenticated."));
            return;
        }
        try {
            int guestId = Integer.parseInt(req.getParameter("guestId"));
            int roomId = Integer.parseInt(req.getParameter("roomId"));
            LocalDate in = LocalDate.parse(req.getParameter("checkInDate"));
            LocalDate out = LocalDate.parse(req.getParameter("checkOutDate"));
            int numGuests = Integer.parseInt(req.getParameter("numGuests"));
            String notes = req.getParameter("specialRequests");

            Reservation res = reservationService.create(
                    guestId, roomId, in, out, numGuests, notes,
                    actor.getUserId(), req.getRemoteAddr());

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.ok("Reservation created.", res));

        } catch (IllegalArgumentException | IllegalStateException e) {
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
        User actor = currentUser(req);
        if (actor == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Not authenticated."));
            return;
        }
        try {
            // path: /{id}/confirm | /checkin | /checkout | /cancel
            String[] parts = normalise(req.getPathInfo()).split("/");
            int id = Integer.parseInt(parts[1]);
            String action = parts.length > 2 ? parts[2].toLowerCase() : "";
            String ip = req.getRemoteAddr();

            Reservation res = switch (action) {
                case "confirm" -> reservationService.confirm(id, ip);
                case "checkin" -> reservationService.checkIn(id, ip);
                case "checkout" -> reservationService.checkOut(id, ip);
                case "cancel" -> reservationService.cancel(id, ip);
                default -> throw new IllegalArgumentException("Unknown action: " + action);
            };
            resp.getWriter().write(JsonUtil.ok("Status updated.", res));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("currentUser");
    }

    private String normalise(String p) {
        return p == null ? "/" : p;
    }
}
