package com.oceanview.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Reservation entity — maps to the 'reservations' table.
 */
public class Reservation {

    public enum Status {
        PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    }

    private int reservationId;
    private String reservationNumber; // e.g. OVR-2024-000001
    private int guestId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private String specialRequests;
    private Status status;
    private int bookedBy; // FK → users.user_id
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ─── Joined fields (not stored in DB, populated by query JOINs) ──────────
    private Guest guest;
    private Room room;
    private User bookedByUser;

    public Reservation() {
    }

    /**
     * Core constructor used when creating new reservations.
     */
    public Reservation(String reservationNumber, int guestId, int roomId,
            LocalDate checkInDate, LocalDate checkOutDate,
            int numGuests, String specialRequests, int bookedBy) {
        this.reservationNumber = reservationNumber;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numGuests = numGuests;
        this.specialRequests = specialRequests;
        this.bookedBy = bookedBy;
        this.status = Status.PENDING;
    }

    // ─── Business logic ───────────────────────────────────────────────────────

    public long getNumNights() {
        if (checkInDate == null || checkOutDate == null)
            return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public boolean isActive() {
        return status == Status.PENDING ||
                status == Status.CONFIRMED ||
                status == Status.CHECKED_IN;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(String n) {
        this.reservationNumber = n;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNumGuests() {
        return numGuests;
    }

    public void setNumGuests(int numGuests) {
        this.numGuests = numGuests;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(int bookedBy) {
        this.bookedBy = bookedBy;
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

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getBookedByUser() {
        return bookedByUser;
    }

    public void setBookedByUser(User u) {
        this.bookedByUser = u;
    }

    @Override
    public String toString() {
        return "Reservation{id=" + reservationId +
                ", number='" + reservationNumber +
                "', status=" + status +
                ", checkIn=" + checkInDate +
                ", checkOut=" + checkOutDate +
                ", nights=" + getNumNights() + '}';
    }
}
