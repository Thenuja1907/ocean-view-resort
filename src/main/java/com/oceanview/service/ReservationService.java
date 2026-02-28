package com.oceanview.service;

import com.oceanview.dao.ReservationDao;
import com.oceanview.dao.RoomDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.Reservation.Status;
import com.oceanview.observer.ReservationEvent;
import com.oceanview.observer.ReservationEvent.EventType;
import com.oceanview.observer.ReservationSubject;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * ReservationService — orchestrates the full reservation lifecycle.
 *
 * Design patterns used:
 * • Observer — fires ReservationEvent to all registered observers on state
 * changes
 * • Factory — delegates room creation to RoomFactory (via RoomService)
 */
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private final ReservationSubject subject;

    public ReservationService(ReservationDao reservationDao,
            RoomDao roomDao,
            ReservationSubject subject) {
        this.reservationDao = reservationDao;
        this.roomDao = roomDao;
        this.subject = subject;
    }

    // ── CREATE ──────────────────────────────────────────────────────────────

    /**
     * Creates a new PENDING reservation after full validation.
     */
    public Reservation create(int guestId, int roomId,
            LocalDate checkIn, LocalDate checkOut,
            int numGuests, String specialRequests,
            int bookedBy, String actorIp) throws SQLException {

        ValidationUtil.validateDateRange(checkIn, checkOut);
        ValidationUtil.requirePositive(guestId, "Guest ID");
        ValidationUtil.requirePositive(roomId, "Room ID");

        // Check room exists and is available for dates
        var roomOpt = roomDao.findById(roomId);
        if (roomOpt.isEmpty())
            throw new IllegalArgumentException("Room not found: " + roomId);
        var room = roomOpt.get();

        ValidationUtil.validateNumGuests(numGuests, room.getCapacity());

        boolean available = reservationDao.isRoomAvailable(roomId, checkIn, checkOut, 0);
        if (!available) {
            throw new IllegalStateException(
                    "Room " + room.getRoomNumber() + " is not available for the selected dates.");
        }

        String number = generateReservationNumber();
        Reservation res = new Reservation(number, guestId, roomId,
                checkIn, checkOut, numGuests, specialRequests, bookedBy);

        reservationDao.insert(res);
        roomDao.setAvailability(roomId, false);

        // Reload to get joined details (Guest name, Room #, etc.) for the
        // Observer/AuditLog
        Reservation detailedRes = reservationDao.findById(res.getReservationId())
                .orElse(res);

        subject.notifyObservers(new ReservationEvent(EventType.CREATED, detailedRes, actorIp));
        log.info("Reservation {} created.", number);
        return detailedRes;
    }

    // ── STATUS TRANSITIONS ──────────────────────────────────────────────────

    public Reservation confirm(int reservationId, String actorIp) throws SQLException {
        return transition(reservationId, Status.CONFIRMED, EventType.CONFIRMED, actorIp);
    }

    public Reservation checkIn(int reservationId, String actorIp) throws SQLException {
        return transition(reservationId, Status.CHECKED_IN, EventType.CHECKED_IN, actorIp);
    }

    public Reservation checkOut(int reservationId, String actorIp) throws SQLException {
        Reservation res = transition(reservationId, Status.CHECKED_OUT, EventType.CHECKED_OUT, actorIp);
        roomDao.setAvailability(res.getRoomId(), true); // room becomes available again
        return res;
    }

    public Reservation cancel(int reservationId, String actorIp) throws SQLException {
        Reservation res = transition(reservationId, Status.CANCELLED, EventType.CANCELLED, actorIp);
        roomDao.setAvailability(res.getRoomId(), true); // free the room
        return res;
    }

    private Reservation transition(int reservationId, Status newStatus,
            EventType eventType, String actorIp) throws SQLException {
        Optional<Reservation> opt = reservationDao.findById(reservationId);
        if (opt.isEmpty())
            throw new IllegalArgumentException("Reservation not found: " + reservationId);

        Reservation res = opt.get();
        String oldStatus = res.getStatus().name();

        res.setStatus(newStatus);
        reservationDao.updateStatus(reservationId, newStatus);

        subject.notifyObservers(new ReservationEvent(eventType, res, actorIp));
        log.info("Reservation {} transition: {} -> {}", res.getReservationNumber(), oldStatus, newStatus);
        return res;
    }

    // ── QUERIES ─────────────────────────────────────────────────────────────

    public Optional<Reservation> findById(int id) throws SQLException {
        return reservationDao.findById(id);
    }

    public Optional<Reservation> findByNumber(String number) throws SQLException {
        return reservationDao.findByNumber(number);
    }

    public List<Reservation> findAll() throws SQLException {
        return reservationDao.findAll();
    }

    public List<Reservation> findByStatus(Status status) throws SQLException {
        return reservationDao.findByStatus(status);
    }

    // ── HELPERS ─────────────────────────────────────────────────────────────

    /**
     * Generates a unique reservation number in format OVR-YYYY-NNNNNN.
     * Uses the current count of all reservations as a simple sequence.
     */
    private String generateReservationNumber() throws SQLException {
        int count = reservationDao.findAll().size() + 1;
        return String.format("OVR-%d-%06d", Year.now().getValue(), count);
    }
}
