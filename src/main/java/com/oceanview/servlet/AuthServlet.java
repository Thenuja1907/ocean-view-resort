package com.oceanview.servlet;

import com.oceanview.model.Guest;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * AuthServlet — supports dual login endpoints for Staff and User portals.
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() {
        authService = (AuthService) getServletContext().getAttribute("authService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = normalise(req.getPathInfo());

        if ("/login".equals(path)) {
            handleStaffLogin(req, resp);
        } else if ("/guest-login".equals(path)) {
            handleUserLogin(req, resp);
        } else if ("/profile".equals(path)) {
            handleProfile(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.error("Endpoint not found: " + path));
        }
    }

    private void handleProfile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Not authenticated."));
            return;
        }
        Object user = session.getAttribute("currentUser");
        String type = (String) session.getAttribute("userType");

        // Return a wrapper with type and data
        resp.getWriter().write(JsonUtil.ok(type, user));
    }

    private String normalise(String path) {
        if (path == null || path.isEmpty() || "/".equals(path))
            return "/";
        if (!path.startsWith("/"))
            path = "/" + path;
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
    }

    private void handleStaffLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");
        try {
            User user = authService.login(u, p);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);
            session.setAttribute("userType", "staff");
            resp.getWriter().write(JsonUtil.ok("Login successful.", user));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    private void handleUserLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String p = req.getParameter("password");
        try {
            Guest guest = authService.loginGuest(email, p);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", guest);
            session.setAttribute("userType", "guest");
            resp.getWriter().write(JsonUtil.ok("Login successful.", guest));
        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session != null)
            session.invalidate();
        resp.getWriter().write(JsonUtil.ok("Logged out.", null));
    }
}
