package com.oceanview.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * DatabaseConnection — Singleton pattern with a minimal connection pool.
 *
 * Usage:
 * Connection conn = DatabaseConnection.getInstance().getConnection();
 * ...
 * DatabaseConnection.getInstance().releaseConnection(conn);
 */
public class DatabaseConnection {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnection.class);

    /** Thread-safe lazy holder for Singleton */
    private static class Holder {
        static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    // ─── Pool configuration ───────────────────────────────────────────────────
    private static final int POOL_SIZE = 10;

    private final String url;
    private final String username;
    private final String password;
    private final BlockingQueue<Connection> pool;

    /** Private constructor — reads db.properties from classpath */
    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            props.load(is);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load db.properties: " + e.getMessage());
        }

        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");

        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL driver not found: " + e.getMessage());
        }

        this.pool = new ArrayBlockingQueue<>(POOL_SIZE);
        initPool();
        log.info("DatabaseConnection pool initialized with {} connections.", POOL_SIZE);
    }

    private void initPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                pool.offer(createConnection());
            } catch (SQLException e) {
                log.error("Failed to create connection #{}: {}", i, e.getMessage());
            }
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /** Returns the Singleton instance. */
    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Borrows a connection from the pool (blocks up to 5 s if pool is empty).
     * Always call releaseConnection() in a finally block.
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.poll(5, java.util.concurrent.TimeUnit.SECONDS);
            if (conn == null || conn.isClosed()) {
                conn = createConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for DB connection", e);
        }
    }

    /**
     * Returns a connection back to the pool.
     * If the pool is full the connection is closed instead.
     */
    public void releaseConnection(Connection conn) {
        if (conn == null)
            return;
        try {
            if (conn.isClosed()) {
                log.warn("Attempted to release a closed connection — discarded.");
                return;
            }
            if (!pool.offer(conn)) {
                conn.close(); // pool full — discard
            }
        } catch (SQLException e) {
            log.error("Error releasing connection: {}", e.getMessage());
        }
    }

    /** Closes all pooled connections (call on application shutdown). */
    public void shutdown() {
        log.info("Shutting down database connection pool…");
        for (Connection conn : pool) {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
        pool.clear();
    }
}
