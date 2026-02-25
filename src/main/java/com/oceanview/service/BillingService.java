package com.oceanview.service;

import com.oceanview.dao.BillDao;
import com.oceanview.dao.ReservationDao;
import com.oceanview.dao.RoomDao;
import com.oceanview.model.Bill;
import com.oceanview.model.Bill.PaymentMethod;
import com.oceanview.model.Bill.PaymentStatus;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * BillingService — Strategy-style billing logic.
 *
 * Calculates:
 * roomCharges = numNights × ratePerNight
 * taxAmount = roomCharges × taxPercentage / 100
 * totalAmount = roomCharges + taxAmount + serviceCharge − discountAmount
 */
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final BillDao billDao;
    private final ReservationDao reservationDao;
    private final RoomDao roomDao;

    public BillingService(BillDao billDao,
            ReservationDao reservationDao,
            RoomDao roomDao) {
        this.billDao = billDao;
        this.reservationDao = reservationDao;
        this.roomDao = roomDao;
    }

    /**
     * Generates and persists a Bill for the given reservation.
     * Uses default 10 % tax and zero service/discount.
     */
    public Bill generateBill(int reservationId) throws SQLException {
        return generateBill(reservationId, new BigDecimal("10.00"),
                BigDecimal.ZERO, BigDecimal.ZERO, null);
    }

    /**
     * Full overload — allows custom tax %, service charge, discount, and notes.
     */
    public Bill generateBill(int reservationId,
            BigDecimal taxPercentage,
            BigDecimal serviceCharge,
            BigDecimal discountAmount,
            String notes) throws SQLException {

        Reservation res = reservationDao.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found: " + reservationId));

        Room room = roomDao.findById(res.getRoomId())
                .orElseThrow(() -> new IllegalStateException(
                        "Room not found for reservation: " + reservationId));

        int nights = (int) res.getNumNights();
        // Allow same-day reservations by charging at least 1 night or handling 0 nights
        if (nights < 0)
            throw new IllegalStateException("Check-out cannot be before check-in.");
        if (nights == 0)
            nights = 1; // Default to 1 night for same-day stays

        BigDecimal rate = room.getRatePerNight();
        BigDecimal roomCharges = rate.multiply(BigDecimal.valueOf(nights));
        BigDecimal taxAmount = roomCharges.multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = roomCharges.add(taxAmount)
                .add(serviceCharge)
                .subtract(discountAmount);

        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber());
        bill.setReservationId(reservationId);
        bill.setNumNights(nights);
        bill.setRoomRate(rate);
        bill.setRoomCharges(roomCharges);
        bill.setTaxPercentage(taxPercentage);
        bill.setTaxAmount(taxAmount);
        bill.setServiceCharge(serviceCharge);
        bill.setDiscountAmount(discountAmount);
        bill.setTotalAmount(total);
        bill.setPaymentStatus(PaymentStatus.PENDING);
        bill.setNotes(notes);

        billDao.insert(bill);
        log.info("Bill {} generated for reservation {}, total={}",
                bill.getBillNumber(), reservationId, total);
        return bill;
    }

    /** Records a payment against an existing bill. */
    public void recordPayment(int billId, PaymentMethod method) throws SQLException {
        billDao.updatePayment(billId, PaymentStatus.PAID, method);
        log.info("Payment recorded for bill {} via {}", billId, method);
    }

    public Optional<Bill> findByReservationId(int reservationId) throws SQLException {
        return billDao.findByReservationId(reservationId);
    }

    public Optional<Bill> findByNumber(String number) throws SQLException {
        return billDao.findByNumber(number);
    }

    public Optional<Bill> findById(int id) throws SQLException {
        return billDao.findById(id);
    }

    public List<Bill> findAll() throws SQLException {
        return billDao.findAll();
    }

    public List<Bill> findByGuestId(int guestId) throws SQLException {
        return billDao.findByGuestId(guestId);
    }

    /**
     * Maintenance: Generates bills for all reservations that don't have one.
     * Useful for recovering from failed automatic generation or manual entries.
     */
    public void generateMissingBills() throws SQLException {
        List<Reservation> allRes = reservationDao.findAll();
        for (Reservation res : allRes) {
            if (billDao.findByReservationId(res.getReservationId()).isEmpty()) {
                try {
                    generateBill(res.getReservationId());
                    log.info("Recovered missing bill for reservation: {}", res.getReservationId());
                } catch (Exception e) {
                    log.warn("Failed to recover bill for reservation {}: {}", res.getReservationId(), e.getMessage());
                }
            }
        }
    }

    // ── HELPERS ─────────────────────────────────────────────────────────────

    private String generateBillNumber() throws SQLException {
        int seq = billDao.findAll().size() + 1;
        return String.format("BILL-%d-%06d", Year.now().getValue(), seq);
    }
}
