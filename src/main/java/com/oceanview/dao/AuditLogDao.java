package com.oceanview.dao;

import com.oceanview.model.AuditLog;
import com.oceanview.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditLogDao — CRUD for the audit_log table.
 */
public class AuditLogDao {

    // ── INSERT ──────────────────────────────────────────────────────────────

    public void insert(AuditLog entry) throws SQLException {
        String sql = "INSERT INTO audit_log (user_id, action, entity_type, entity_id, " +
                "old_value, new_value, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setNullableInt(ps, 1, entry.getUserId());
            ps.setString(2, entry.getAction());
            ps.setString(3, entry.getEntityType());
            setNullableInt(ps, 4, entry.getEntityId());
            ps.setString(5, entry.getOldValue());
            ps.setString(6, entry.getNewValue());
            ps.setString(7, entry.getIpAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    entry.setLogId(keys.getLong(1));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
    }

    // ── FIND ────────────────────────────────────────────────────────────────

    public List<AuditLog> findAll(int limit) throws SQLException {
        String sql = "SELECT * FROM audit_log ORDER BY created_at DESC LIMIT ?";
        List<AuditLog> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    public List<AuditLog> findByEntity(String entityType, int entityId) throws SQLException {
        String sql = "SELECT * FROM audit_log WHERE entity_type = ? AND entity_id = ? " +
                "ORDER BY created_at DESC";
        List<AuditLog> list = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entityType);
            ps.setInt(2, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        } finally {
            DatabaseConnection.getInstance().releaseConnection(conn);
        }
        return list;
    }

    // ── MAPPING ─────────────────────────────────────────────────────────────

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setLogId(rs.getLong("log_id"));
        int uid = rs.getInt("user_id");
        a.setUserId(rs.wasNull() ? null : uid);
        a.setAction(rs.getString("action"));
        a.setEntityType(rs.getString("entity_type"));
        int eid = rs.getInt("entity_id");
        a.setEntityId(rs.wasNull() ? null : eid);
        a.setOldValue(rs.getString("old_value"));
        a.setNewValue(rs.getString("new_value"));
        a.setIpAddress(rs.getString("ip_address"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null)
            a.setCreatedAt(ts.toLocalDateTime());
        return a;
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null)
            ps.setNull(idx, Types.INTEGER);
        else
            ps.setInt(idx, val);
    }
}
