package com.oceanview.dao;

import com.oceanview.model.Bill;
import com.oceanview.model.Bill.PaymentMethod;
import com.oceanview.model.Bill.PaymentStatus;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BillDao — CRUD for the 'bills' table.
 */
public class BillDao {

    // ── CREATE ──────────────────────────────────────────────────────────────

    public Bill insert(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (bill_number, reservation_id, num_nights, num_guests, room_rate, " +
                "room_charges, tax_percentage, tax_amount, service_charge, discount_amount, " +
                "total_amount, payment_status, payment_method, notes) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bill.getBillNumber());
            ps.setInt(2, bill.getReservationId());
            ps.setInt(3, bill.getNumNights());
            ps.setInt(4, bill.getNumGuests());
            ps.setBigDecimal(5, bill.getRoomRate());
            ps.setBigDecimal(6, bill.getRoomCharges());
            ps.setBigDecimal(7, bill.getTaxPercentage());
            ps.setBigDecimal(8, bill.getTaxAmount());
            ps.setBigDecimal(9, bill.getServiceCharge());
            ps.setBigDecimal(10, bill.getDiscountAmount());
            ps.setBigDecimal(11, bill.getTotalAmount());
            ps.setString(12, bill.getPaymentStatus().name());
            if (bill.getPaymentMethod() != null)
                ps.setString(13, bill.getPaymentMethod().name());
            else
                ps.setNull(13, Types.VARCHAR);
            ps.setString(14, bill.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    bill.setBillId(keys.getInt(1));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return bill;
    }

    // ── READ ────────────────────────────────────────────────────────────────

    public Optional<Bill> findById(int id) throws SQLException {
        String sql = "SELECT b.*, r.reservation_number, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number "
                +
                "FROM bills b " +
                "LEFT JOIN reservations r ON b.reservation_id = r.reservation_id " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE b.bill_id = ?";
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

    public Optional<Bill> findByReservationId(int reservationId) throws SQLException {
        String sql = "SELECT b.*, r.reservation_number, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number "
                +
                "FROM bills b " +
                "LEFT JOIN reservations r ON b.reservation_id = r.reservation_id " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE b.reservation_id = ? ORDER BY b.issued_at DESC LIMIT 1";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public Optional<Bill> findByNumber(String billNumber) throws SQLException {
        String sql = "SELECT b.*, r.reservation_number, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number "
                +
                "FROM bills b " +
                "LEFT JOIN reservations r ON b.reservation_id = r.reservation_id " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE b.bill_number = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, billNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public List<Bill> findByGuestId(int guestId) throws SQLException {
        String sql = "SELECT b.*, r.reservation_number, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number "
                +
                "FROM bills b " +
                "LEFT JOIN reservations r ON b.reservation_id = r.reservation_id " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "WHERE r.guest_id = ? " +
                "ORDER BY b.issued_at DESC";
        List<Bill> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<Bill> findAll() throws SQLException {
        String sql = "SELECT b.*, r.reservation_number, g.first_name, g.last_name, g.email, g.contact_number, rm.room_number "
                +
                "FROM bills b " +
                "LEFT JOIN reservations r ON b.reservation_id = r.reservation_id " +
                "LEFT JOIN guests g ON r.guest_id = g.guest_id " +
                "LEFT JOIN rooms rm ON r.room_id = rm.room_id " +
                "ORDER BY b.issued_at DESC";
        List<Bill> list = new ArrayList<>();
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

    // ── UPDATE ──────────────────────────────────────────────────────────────

    public void updatePayment(int billId, PaymentStatus status, PaymentMethod method) throws SQLException {
        String sql = "UPDATE bills SET payment_status=?, payment_method=?, paid_at=IF(?='PAID',NOW(),paid_at) " +
                "WHERE bill_id=?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (method != null)
                ps.setString(2, method.name());
            else
                ps.setNull(2, Types.VARCHAR);
            ps.setString(3, status.name());
            ps.setInt(4, billId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── MAPPING ─────────────────────────────────────────────────────────────

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setBillNumber(rs.getString("bill_number"));
        b.setReservationId(rs.getInt("reservation_id"));
        b.setNumNights(rs.getInt("num_nights"));
        // Try to read num_guests, default to 1 if not exists/null
        try {
            b.setNumGuests(rs.getInt("num_guests"));
        } catch (Exception e) {
            b.setNumGuests(1);
        }
        b.setRoomRate(rs.getBigDecimal("room_rate"));
        b.setRoomCharges(rs.getBigDecimal("room_charges"));
        b.setTaxPercentage(rs.getBigDecimal("tax_percentage"));
        b.setTaxAmount(rs.getBigDecimal("tax_amount"));
        b.setServiceCharge(rs.getBigDecimal("service_charge"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        String pm = rs.getString("payment_method");
        if (pm != null)
            b.setPaymentMethod(PaymentMethod.valueOf(pm));
        Timestamp ia = rs.getTimestamp("issued_at");
        if (ia != null)
            b.setIssuedAt(ia.toLocalDateTime());
        Timestamp pa = rs.getTimestamp("paid_at");
        if (pa != null)
            b.setPaidAt(pa.toLocalDateTime());
        b.setNotes(rs.getString("notes"));

        // Attempt to populate Reservation hierarchy if columns exist
        try {
            com.oceanview.model.Reservation res = new com.oceanview.model.Reservation();
            res.setReservationId(b.getReservationId());
            res.setReservationNumber(rs.getString("reservation_number"));

            com.oceanview.model.Guest g = new com.oceanview.model.Guest();
            g.setFirstName(rs.getString("first_name"));
            g.setLastName(rs.getString("last_name"));
            try {
                g.setEmail(rs.getString("email"));
                g.setContactNumber(rs.getString("contact_number"));
            } catch (SQLException ignored) {
            }
            res.setGuest(g);

            com.oceanview.model.Room rm = new com.oceanview.model.Room();
            rm.setRoomNumber(rs.getString("room_number"));
            res.setRoom(rm);

            b.setReservation(res);
        } catch (SQLException ignored) {
            // Join columns not present
        }

        return b;
    }
}
