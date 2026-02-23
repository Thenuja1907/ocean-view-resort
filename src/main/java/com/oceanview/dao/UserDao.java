package com.oceanview.dao;

import com.oceanview.model.User;
import com.oceanview.model.User.Role;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UserDao — CRUD for the 'users' table.
 */
public class UserDao {

    // ── CREATE ──────────────────────────────────────────────────────────────

    public User insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, email, role, is_active) " +
                "VALUES (?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    user.setUserId(keys.getInt(1));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return user;
    }

    // ── READ ────────────────────────────────────────────────────────────────

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
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

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY full_name";
        List<User> list = new ArrayList<>();
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

    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username=?, full_name=?, email=?, role=?, is_active=? WHERE user_id=?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getUserId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public void updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── DELETE ──────────────────────────────────────────────────────────────

    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── MAPPING ─────────────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("is_active"));
        Timestamp ll = rs.getTimestamp("last_login");
        if (ll != null)
            u.setLastLogin(ll.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null)
            u.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null)
            u.setUpdatedAt(ua.toLocalDateTime());
        return u;
    }
}
