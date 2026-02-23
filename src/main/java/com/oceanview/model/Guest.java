package com.oceanview.model;

import java.time.LocalDateTime;

/**
 * Guest entity — maps to the 'guests' table.
 */
public class Guest {

    public enum IdType {
        NIC, PASSPORT, DRIVING_LICENSE
    }

    private int guestId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private boolean active = true;
    private LocalDateTime lastLogin;
    private String contactNumber;
    private String address;
    private IdType idType;
    private String idNumber;
    private String nationality;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Guest() {
    }

    public Guest(String firstName, String lastName, String email,
            String contactNumber, String address,
            IdType idType, String idNumber, String nationality) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.address = address;
        this.idType = idType;
        this.idNumber = idNumber;
        this.nationality = nationality;
    }

    // ─── Convenience ─────────────────────────────────────────────────────────

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String hash) {
        this.passwordHash = hash;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime t) {
        this.lastLogin = t;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String c) {
        this.contactNumber = c;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime t) {
        this.createdAt = t;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime t) {
        this.updatedAt = t;
    }

    @Override
    public String toString() {
        return "Guest{guestId=" + guestId + ", name='" + getFullName() +
                "', email='" + email + "'}";
    }
}
