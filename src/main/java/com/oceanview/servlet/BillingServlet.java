package com.oceanview.servlet;

import com.oceanview.model.Bill;
import com.oceanview.model.Bill.PaymentMethod;
import com.oceanview.model.User;
import com.oceanview.service.BillingService;
import com.oceanview.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * BillingServlet — REST-ish API for /api/bills/*.
 *
 * GET /api/bills → list all bills
 * GET /api/bills/{id} → single bill
 * GET /api/bills/reservation/{resId} → bill for a reservation
 * POST /api/bills/generate/{resId} → generate bill for reservation
 * PUT /api/bills/{id}/pay → record payment
 */
@WebServlet("/api/bills/*")
public class BillingServlet extends HttpServlet {

    private BillingService billingService;

    @Override
    public void init() {
        billingService = (BillingService) getServletContext().getAttribute("billingService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String path = normalise(req.getPathInfo());

        try {
            if (path.equals("/") || path.isEmpty()) {
                List<Bill> bills = billingService.findAll();
                resp.getWriter().write(JsonUtil.ok(bills));

            } else if (path.startsWith("/reservation/")) {
                int resId = Integer.parseInt(path.substring("/reservation/".length()));
                Optional<Bill> opt = billingService.findByReservationId(resId);
                if (opt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("No bill found for reservation."));
                } else {
                    resp.getWriter().write(JsonUtil.ok(opt.get()));
                }
            } else {
                int id = Integer.parseInt(path.substring(1));
                Optional<Bill> opt = billingService.findById(id);
                if (opt.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("Bill not found."));
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
        if (currentUser(req) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Not authenticated."));
            return;
        }
        try {
            // POST /api/bills/generate/{resId}
            String path = normalise(req.getPathInfo());
            int resId = Integer.parseInt(path.replace("/generate/", ""));
            Bill bill = billingService.generateBill(resId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.ok("Bill generated.", bill));
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
        if (currentUser(req) == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Not authenticated."));
            return;
        }
        try {
            // PUT /api/bills/{id}/pay?method=CASH
            String[] parts = normalise(req.getPathInfo()).split("/");
            int billId = Integer.parseInt(parts[1]);
            PaymentMethod method = PaymentMethod.valueOf(req.getParameter("method").toUpperCase());
            billingService.recordPayment(billId, method);
            resp.getWriter().write(JsonUtil.ok("Payment recorded.", null));
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
