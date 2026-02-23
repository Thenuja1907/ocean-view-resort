package com.oceanview.dao;

import com.oceanview.model.Room;
import com.oceanview.model.Room.RoomType;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RoomDao — persistence for the 'rooms' table.
 */
public class RoomDao {

    // ── CREATE ──────────────────────────────────────────────────────────────

    public Room insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, floor_number, capacity, " +
                "rate_per_night, description, amenities, is_available) VALUES (?,?,?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getFloorNumber());
            ps.setInt(4, room.getCapacity());
            ps.setBigDecimal(5, room.getRatePerNight());
            ps.setString(6, room.getDescription());
            ps.setString(7, room.getAmenities());
            ps.setBoolean(8, room.isAvailable());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    room.setRoomId(keys.getInt(1));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return room;
    }

    // ── READ ────────────────────────────────────────────────────────────────

    public Optional<Room> findById(int id) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
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

    public Optional<Room> findByRoomNumber(String number) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
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

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        List<Room> list = new ArrayList<>();
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

    public List<Room> findAvailable() throws SQLException {
        String sql = "SELECT * FROM rooms WHERE is_available = TRUE ORDER BY room_type, room_number";
        List<Room> list = new ArrayList<>();
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

    public void update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, floor_number=?, capacity=?, " +
                "rate_per_night=?, description=?, amenities=?, is_available=? WHERE room_id=?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setInt(3, room.getFloorNumber());
            ps.setInt(4, room.getCapacity());
            ps.setBigDecimal(5, room.getRatePerNight());
            ps.setString(6, room.getDescription());
            ps.setString(7, room.getAmenities());
            ps.setBoolean(8, room.isAvailable());
            ps.setInt(9, room.getRoomId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public void setAvailability(int roomId, boolean available) throws SQLException {
        String sql = "UPDATE rooms SET is_available = ? WHERE room_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    public void delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── MAPPING ─────────────────────────────────────────────────────────────

    private Room mapRow(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        r.setFloorNumber(rs.getInt("floor_number"));
        r.setCapacity(rs.getInt("capacity"));
        r.setRatePerNight(rs.getBigDecimal("rate_per_night"));
        r.setDescription(rs.getString("description"));
        r.setAmenities(rs.getString("amenities"));
        r.setAvailable(rs.getBoolean("is_available"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null)
            r.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null)
            r.setUpdatedAt(ua.toLocalDateTime());
        return r;
    }
}
