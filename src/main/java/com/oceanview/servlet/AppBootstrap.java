package com.oceanview.servlet;

import com.oceanview.dao.AuditLogDao;
import com.oceanview.dao.UserDao;
import com.oceanview.dao.GuestDao;
import com.oceanview.dao.RoomDao;
import com.oceanview.dao.ReservationDao;
import com.oceanview.dao.BillDao;
import com.oceanview.observer.AuditLogObserver;
import com.oceanview.observer.BillingObserver;
import com.oceanview.observer.ReservationSubject;
import com.oceanview.service.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * AppBootstrap — initialises all services once at startup
 * and stores them in ServletContext so servlets can share them.
 *
 * Implements the Service-Locator bootstrapping approach:
 * servletContext.getAttribute("authService") → AuthService
 */
@WebListener
public class AppBootstrap implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppBootstrap.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Ocean View Resort — initialising application context …");
        ServletContext ctx = sce.getServletContext();

        // ── DAOs ──────────────────────────────────────────────────────────
        UserDao userDao = new UserDao();
        GuestDao guestDao = new GuestDao();
        RoomDao roomDao = new RoomDao();
        ReservationDao reservationDao = new ReservationDao();
        BillDao billDao = new BillDao();
        AuditLogDao auditLogDao = new AuditLogDao();

        // ── Observer setup ────────────────────────────────────────────────
        ReservationSubject subject = new ReservationSubject();
        subject.addObserver(new AuditLogObserver(auditLogDao));

        // ── Services ──────────────────────────────────────────────────────
        AuthService authService = new AuthService(userDao, guestDao);
        GuestService guestService = new GuestService(guestDao);
        RoomService roomService = new RoomService(roomDao);
        BillingService billingService = new BillingService(billDao, reservationDao, roomDao);
        ReservationService reservationService = new ReservationService(reservationDao, roomDao, subject);

        // Link dependencies
        billingService.setReservationService(reservationService);
        subject.addObserver(new BillingObserver(billingService));

        // ── Publish to context ────────────────────────────────────────────
        ctx.setAttribute("authService", authService);
        ctx.setAttribute("guestService", guestService);
        ctx.setAttribute("roomService", roomService);
        ctx.setAttribute("reservationService", reservationService);
        ctx.setAttribute("billingService", billingService);
        ctx.setAttribute("auditLogDao", auditLogDao);
        ctx.setAttribute("userDao", userDao); // Added for ID lookups

        // Maintenance: Generate missing bills & Seed Admin
        try {
            billingService.generateMissingBills();

            // Ensure the 'admin' user exists with the default password
            Optional<com.oceanview.model.User> adminOpt = userDao.findByUsername("admin");
            if (adminOpt.isEmpty()) {
                log.info("Admin user not found. Seeding default admin account...");
                com.oceanview.model.User admin = new com.oceanview.model.User();
                admin.setUsername("admin");
                admin.setFullName("System Administrator");
                admin.setEmail("admin@oceanviewresort.lk");
                admin.setRole(com.oceanview.model.User.Role.ADMIN);
                admin.setActive(true);
                // Password = Admin@1234
                admin.setPasswordHash(com.oceanview.util.PasswordUtil.hash("Admin@1234"));
                userDao.insert(admin);
                log.info("Default admin created: admin / Admin@1234");
            } else {
                // Force update password for the default admin to ensure it's correct
                // This is a safety measure for the USER's specific environment
                userDao.updatePassword(adminOpt.get().getUserId(),
                        com.oceanview.util.PasswordUtil.hash("Admin@1234"));
                log.info("Admin password synchronized: admin / Admin@1234");
            }

            // Manual Migration: Ensure bills table has required columns and proper types
            try (java.sql.Connection conn = com.oceanview.util.DatabaseConnection.getInstance().getConnection();
                    java.sql.Statement st = conn.createStatement()) {
                // Add num_guests if missing
                st.executeUpdate(
                        "ALTER TABLE bills ADD COLUMN IF NOT EXISTS num_guests INT DEFAULT 1 AFTER num_nights");
                // Fix payment_method truncation (ENUM to VARCHAR)
                st.executeUpdate("ALTER TABLE bills MODIFY COLUMN payment_method VARCHAR(50) NULL");
                log.info("Database migrations (num_guests, payment_method) applied successfully.");
            } catch (Exception e) {
                log.warn("Migration notice: {}", e.getMessage());
            }
        } catch (SQLException e) {
            log.error("Startup maintenance failed: {}", e.getMessage());
        }

        log.info("Application context ready.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Application shutting down.");
    }
}
