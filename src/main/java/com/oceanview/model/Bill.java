package com.oceanview.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bill entity — maps to the 'bills' table.
 * Automatically calculated by BillingService (Strategy-style logic).
 */
public class Bill {

    public enum PaymentStatus {
        PENDING, PARTIAL, PAID, REFUNDED
    }

    public enum PaymentMethod {
        CASH, CARD, BANK_TRANSFER, ONLINE
    }

    private int billId;
    private String billNumber; // e.g. BILL-2024-000001
    private int reservationId;
    private int numNights;
    private BigDecimal roomRate;
    private BigDecimal roomCharges; // numNights × roomRate
    private BigDecimal taxPercentage; // e.g. 10.00 = 10%
    private BigDecimal taxAmount;
    private BigDecimal serviceCharge;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime issuedAt;
    private LocalDateTime paidAt;
    private String notes;

    // ─── Joined fields ────────────────────────────────────────────────────────
    private Reservation reservation;

    public Bill() {
        this.taxPercentage = new BigDecimal("10.00");
        this.serviceCharge = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getNumNights() {
        return numNights;
    }

    public void setNumNights(int numNights) {
        this.numNights = numNights;
    }

    public BigDecimal getRoomRate() {
        return roomRate;
    }

    public void setRoomRate(BigDecimal roomRate) {
        this.roomRate = roomRate;
    }

    public BigDecimal getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(BigDecimal roomCharges) {
        this.roomCharges = roomCharges;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal t) {
        this.taxPercentage = t;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(BigDecimal s) {
        this.serviceCharge = s;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal d) {
        this.discountAmount = d;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus ps) {
        this.paymentStatus = ps;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod pm) {
        this.paymentMethod = pm;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime t) {
        this.issuedAt = t;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime t) {
        this.paidAt = t;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation r) {
        this.reservation = r;
    }

    @Override
    public String toString() {
        return "Bill{billId=" + billId + ", number='" + billNumber +
                "', nights=" + numNights + ", total=" + totalAmount +
                ", status=" + paymentStatus + '}';
    }
}
