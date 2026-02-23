package com.oceanview.servlet;

import com.oceanview.dao.UserDao;
import com.oceanview.model.User;
import com.oceanview.service.AuthService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Optional;

/**
 * AuthServlet — handles POST /api/auth/login and GET /api/auth/logout.
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() {
        authService = (AuthService) getServletContext().getAttribute("authService");
    }

    // ── POST /api/auth/login ─────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String path = req.getPathInfo();
        if (!"/login".equals(path)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.error("Endpoint not found."));
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            // Debug check
            Optional<User> opt = (new UserDao()).findByUsername(username);
            if (opt.isPresent()) {
                System.out.println("DEBUG: Found user hash from DB: [" + opt.get().getPasswordHash() + "]");
            } else {
                System.out.println("DEBUG: User not found in DB: " + username);
            }

            User user = authService.login(username, password);

            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);
            session.setMaxInactiveInterval(1800); // 30 min

            resp.getWriter().write(JsonUtil.ok("Login successful.", user));

        } catch (SecurityException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Log stack trace for unexpected errors
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error("An unexpected error occurred."));
        }
    }

    // ── GET /api/auth/logout ─────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session != null)
            session.invalidate();
        resp.getWriter().write(JsonUtil.ok("Logged out.", null));
    }
}
