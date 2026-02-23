package com.oceanview.servlet;

import com.oceanview.dao.AuditLogDao;
import com.oceanview.dao.UserDao;
import com.oceanview.dao.GuestDao;
import com.oceanview.dao.RoomDao;
import com.oceanview.dao.ReservationDao;
import com.oceanview.dao.BillDao;
import com.oceanview.observer.AuditLogObserver;
import com.oceanview.observer.ReservationSubject;
import com.oceanview.service.*;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        AuthService authService = new AuthService(userDao);
        GuestService guestService = new GuestService(guestDao);
        RoomService roomService = new RoomService(roomDao);
        ReservationService reservationService = new ReservationService(reservationDao, roomDao, subject);
        BillingService billingService = new BillingService(billDao, reservationDao, roomDao);

        // ── Publish to context ────────────────────────────────────────────
        ctx.setAttribute("authService", authService);
        ctx.setAttribute("guestService", guestService);
        ctx.setAttribute("roomService", roomService);
        ctx.setAttribute("reservationService", reservationService);
        ctx.setAttribute("billingService", billingService);
        ctx.setAttribute("auditLogDao", auditLogDao);

        log.info("Application context ready.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Application shutting down.");
    }
}
