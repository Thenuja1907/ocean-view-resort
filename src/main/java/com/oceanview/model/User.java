package com.oceanview.model;

import java.time.LocalDateTime;

/**
 * Represents a staff member who can log into the system.
 * Roles: ADMIN, STAFF, RECEPTIONIST
 */
public class User {

    public enum Role { ADMIN, STAFF, RECEPTIONIST }

    private int userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public User(String username, String passwordHash, String fullName,
                String email, Role role) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.email        = email;
        this.role         = role;
        this.active       = true;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getUserId()                    { return userId; }
    public void setUserId(int userId)         { this.userId = userId; }

    public String getUsername()               { return username; }
    public void setUsername(String username)  { this.username = username; }

    public String getPasswordHash()           { return passwordHash; }
    public void setPasswordHash(String h)     { this.passwordHash = h; }

    public String getFullName()               { return fullName; }
    public void setFullName(String fullName)  { this.fullName = fullName; }

    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }

    public Role getRole()                     { return role; }
    public void setRole(Role role)            { this.role = role; }

    public boolean isActive()                 { return active; }
    public void setActive(boolean active)     { this.active = active; }

    public LocalDateTime getLastLogin()            { return lastLogin; }
    public void setLastLogin(LocalDateTime t)      { this.lastLogin = t; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime t)      { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)      { this.updatedAt = t; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", username='" + username +
               "', role=" + role + ", active=" + active + '}';
    }
}
