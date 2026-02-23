package com.oceanview.dao;

import com.oceanview.model.Guest;
import com.oceanview.model.Guest.IdType;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GuestDao — CRUD for the 'guests' table.
 */
public class GuestDao {

    // ── CREATE ──────────────────────────────────────────────────────────────

    public Guest insert(Guest guest) throws SQLException {
        String sql = "INSERT INTO guests (first_name, last_name, email, contact_number, " +
                "address, id_type, id_number, nationality) VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getEmail());
            ps.setString(4, guest.getContactNumber());
            ps.setString(5, guest.getAddress());
            ps.setString(6, guest.getIdType().name());
            ps.setString(7, guest.getIdNumber());
            ps.setString(8, guest.getNationality());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    guest.setGuestId(keys.getInt(1));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return guest;
    }

    // ── READ ────────────────────────────────────────────────────────────────

    public Optional<Guest> findById(int id) throws SQLException {
        String sql = "SELECT * FROM guests WHERE guest_id = ?";
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

    public Optional<Guest> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM guests WHERE email = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public List<Guest> findAll() throws SQLException {
        String sql = "SELECT * FROM guests ORDER BY last_name, first_name";
        List<Guest> list = new ArrayList<>();
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

    public List<Guest> search(String query) throws SQLException {
        String q = "%" + query.toLowerCase() + "%";
        String sql = "SELECT * FROM guests WHERE LOWER(first_name) LIKE ? " +
                "OR LOWER(last_name) LIKE ? OR LOWER(email) LIKE ? " +
                "OR contact_number LIKE ? ORDER BY last_name";
        List<Guest> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            ps.setString(4, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    // ── UPDATE ──────────────────────────────────────────────────────────────

    public void update(Guest guest) throws SQLException {
        String sql = "UPDATE guests SET first_name=?, last_name=?, email=?, contact_number=?, " +
                "address=?, id_type=?, id_number=?, nationality=? WHERE guest_id=?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getEmail());
            ps.setString(4, guest.getContactNumber());
            ps.setString(5, guest.getAddress());
            ps.setString(6, guest.getIdType().name());
            ps.setString(7, guest.getIdNumber());
            ps.setString(8, guest.getNationality());
            ps.setInt(9, guest.getGuestId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    public void delete(int guestId) throws SQLException {
        String sql = "DELETE FROM guests WHERE guest_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guestId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── MAPPING ─────────────────────────────────────────────────────────────

    private Guest mapRow(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setGuestId(rs.getInt("guest_id"));
        g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name"));
        g.setEmail(rs.getString("email"));
        g.setContactNumber(rs.getString("contact_number"));
        g.setAddress(rs.getString("address"));
        g.setIdType(IdType.valueOf(rs.getString("id_type")));
        g.setIdNumber(rs.getString("id_number"));
        g.setNationality(rs.getString("nationality"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null)
            g.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null)
            g.setUpdatedAt(ua.toLocalDateTime());
        return g;
    }
}
