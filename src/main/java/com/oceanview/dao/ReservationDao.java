package com.oceanview.dao;

import com.oceanview.model.Reservation;
import com.oceanview.model.Reservation.Status;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ReservationDao — CRUD for the 'reservations' table.
 */
public class ReservationDao {

    // ── CREATE ──────────────────────────────────────────────────────────────

    public Reservation insert(Reservation res) throws SQLException {
        String sql = "INSERT INTO reservations (reservation_number, guest_id, room_id, " +
                "check_in_date, check_out_date, num_guests, special_requests, status, booked_by) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, res.getReservationNumber());
            ps.setInt(2, res.getGuestId());
            ps.setInt(3, res.getRoomId());
            ps.setDate(4, Date.valueOf(res.getCheckInDate()));
            ps.setDate(5, Date.valueOf(res.getCheckOutDate()));
            ps.setInt(6, res.getNumGuests());
            ps.setString(7, res.getSpecialRequests());
            ps.setString(8, res.getStatus().name());
            ps.setInt(9, res.getBookedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    res.setReservationId(keys.getInt(1));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return res;
    }

    // ── READ ────────────────────────────────────────────────────────────────

    public Optional<Reservation> findById(int id) throws SQLException {
        String sql = "SELECT r.*, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number, rm.room_type " +
                "FROM reservations r " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.reservation_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public Optional<Reservation> findByNumber(String number) throws SQLException {
        String sql = "SELECT r.*, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number " +
                "FROM reservations r " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.reservation_number = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT r.*, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number " +
                "FROM reservations r " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "ORDER BY r.created_at DESC";
        List<Reservation> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<Reservation> findByStatus(Status status) throws SQLException {
        String sql = "SELECT r.*, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number " +
                "FROM reservations r " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.status = ? ORDER BY r.check_in_date";
        List<Reservation> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    /**
     * Checks if a room is available for the requested date range (excluding a given
     * reservation id).
     */
    public boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut,
            int excludeReservationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations " +
                "WHERE room_id = ? AND reservation_id != ? " +
                "AND status NOT IN ('CANCELLED','CHECKED_OUT') " +
                "AND check_in_date < ? AND check_out_date > ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setInt(2, excludeReservationId);
            ps.setDate(3, Date.valueOf(checkOut));
            ps.setDate(4, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    public void updateStatus(int reservationId, Status newStatus) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public void update(Reservation res) throws SQLException {
        String sql = "UPDATE reservations SET guest_id=?, room_id=?, check_in_date=?, " +
                "check_out_date=?, num_guests=?, special_requests=?, status=? " +
                "WHERE reservation_id=?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, res.getGuestId());
            ps.setInt(2, res.getRoomId());
            ps.setDate(3, Date.valueOf(res.getCheckInDate()));
            ps.setDate(4, Date.valueOf(res.getCheckOutDate()));
            ps.setInt(5, res.getNumGuests());
            ps.setString(6, res.getSpecialRequests());
            ps.setString(7, res.getStatus().name());
            ps.setInt(8, res.getReservationId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── MAPPING ─────────────────────────────────────────────────────────────

    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setReservationNumber(rs.getString("reservation_number"));
        r.setGuestId(rs.getInt("guest_id"));
        r.setRoomId(rs.getInt("room_id"));
        r.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        r.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        r.setNumGuests(rs.getInt("num_guests"));
        r.setSpecialRequests(rs.getString("special_requests"));
        r.setStatus(Status.valueOf(rs.getString("status")));
        r.setBookedBy(rs.getInt("booked_by"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null)
            r.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null)
            r.setUpdatedAt(ua.toLocalDateTime());

        // Attempt to populate Guest/Room objects if columns exist in projection
        try {
            com.oceanview.model.Guest g = new com.oceanview.model.Guest();
            g.setGuestId(r.getGuestId());
            g.setFirstName(rs.getString("first_name"));
            g.setLastName(rs.getString("last_name"));
            try {
                g.setEmail(rs.getString("email"));
                g.setContactNumber(rs.getString("contact_number"));
            } catch (SQLException ignored) {
            }
            r.setGuest(g);

            com.oceanview.model.Room rm = new com.oceanview.model.Room();
            rm.setRoomId(r.getRoomId());
            rm.setRoomNumber(rs.getString("room_number"));
            r.setRoom(rm);
        } catch (SQLException ignored) {
            // Columns not in result set (e.g. from findById with *)
        }
        return r;
    }
}
