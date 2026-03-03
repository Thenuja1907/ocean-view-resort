package com.oceanview.observer;

import com.oceanview.dao.AuditLogDao;
import com.oceanview.model.AuditLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * AuditLogObserver — writes an audit_log row on every reservation event.
 * Concrete Observer in the Observer pattern.
 */
public class AuditLogObserver implements ReservationObserver {

    private static final Logger log = LoggerFactory.getLogger(AuditLogObserver.class);

    private final AuditLogDao auditLogDao;

    public AuditLogObserver(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    @Override
    public void onReservationEvent(ReservationEvent event) {
        try {
            String guestName = (event.getReservation().getGuest() != null)
                    ? event.getReservation().getGuest().getFirstName() + " "
                            + event.getReservation().getGuest().getLastName()
                    : "Unknown Guest";
            String guestEmail = (event.getReservation().getGuest() != null)
                    ? event.getReservation().getGuest().getEmail()
                    : "";
            String roomNum = (event.getReservation().getRoom() != null)
                    ? event.getReservation().getRoom().getRoomNumber()
                    : String.valueOf(event.getReservation().getRoomId());

            String details = String.format("Res: %s | Guest: %s (%s) | Room: %s | Status: %s",
                    event.getReservation().getReservationNumber(),
                    guestName,
                    guestEmail,
                    roomNum,
                    event.getReservation().getStatus());

            AuditLog entry = new AuditLog(
                    event.getReservation().getBookedBy(),
                    event.getType().name(),
                    "RESERVATION",
                    event.getReservation().getReservationId(),
                    null, // Could be improved if we pass oldStatus
                    details,
                    event.getActorIp());
            auditLogDao.insert(entry);
            log.info("Audit log written: {}", event);
        } catch (SQLException e) {
            log.error("Failed to write audit log for event {}: {}", event, e.getMessage());
        }
    }
}
