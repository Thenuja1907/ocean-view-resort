package com.oceanview.servlet;

import com.oceanview.model.Room;
import com.oceanview.model.Room.RoomType;
import com.oceanview.service.RoomService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * RoomServlet — REST-ish API for /api/rooms/*.
 *
 * GET /api/rooms → list all rooms
 * GET /api/rooms/available → list available rooms
 * GET /api/rooms/{id} → get one room
 * POST /api/rooms → create room (admin)
 * PUT /api/rooms/{id}/availability → toggle availability
 * DELETE /api/rooms/{id} → delete room (admin)
 */
@WebServlet("/api/rooms/*")
public class RoomServlet extends HttpServlet {

    private RoomService roomService;

    @Override
    public void init() {
        roomService = (RoomService) getServletContext().getAttribute("roomService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = normalise(req.getPathInfo());

        try {
            if (path.isEmpty() || path.equals("/")) {
                List<Room> rooms = roomService.findAll();
                resp.getWriter().write(JsonUtil.ok(rooms));

            } else if (path.equals("/available")) {
                List<Room> rooms = roomService.findAvailable();
                resp.getWriter().write(JsonUtil.ok(rooms));

            } else {
                int id = parseId(path);
                Optional<Room> opt = roomService.findById(id);
                if (opt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("Room not found."));
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
        try {
            requireAdmin(req);
            String roomNumber = req.getParameter("roomNumber");
            RoomType type = RoomType.valueOf(req.getParameter("roomType"));
            int floor = Integer.parseInt(req.getParameter("floorNumber"));

            Room room = roomService.createRoom(roomNumber, type, floor);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.ok("Room created.", room));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
            requireAdmin(req);
            String path = normalise(req.getPathInfo());
            // Expected: /{id}/availability
            String[] parts = path.split("/");
            int id = Integer.parseInt(parts[1]);
            boolean available = Boolean.parseBoolean(req.getParameter("available"));
            roomService.setAvailability(id, available);
            resp.getWriter().write(JsonUtil.ok("Availability updated.", null));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
            requireAdmin(req);
            int id = parseId(normalise(req.getPathInfo()));
            roomService.deleteRoom(id);
            resp.getWriter().write(JsonUtil.ok("Room deleted.", null));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String normalise(String pathInfo) {
        return pathInfo == null ? "/" : pathInfo;
    }

    private int parseId(String path) {
        // path is like /123 or /123/availability
        String[] parts = path.split("/");
        return Integer.parseInt(parts[1]);
    }

    private void requireAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            throw new SecurityException("Not authenticated.");
        }
        com.oceanview.model.User user = (com.oceanview.model.User) session.getAttribute("currentUser");
        if (user.getRole() != com.oceanview.model.User.Role.ADMIN) {
            throw new SecurityException("Admin access required.");
        }
    }
}
