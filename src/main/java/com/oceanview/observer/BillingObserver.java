package com.oceanview.observer;

import com.oceanview.service.BillingService;
import com.oceanview.observer.ReservationEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * BillingObserver — automatically generates a bill when a reservation is
 * CREATED.
 */
public class BillingObserver implements ReservationObserver {

    private static final Logger log = LoggerFactory.getLogger(BillingObserver.class);
    private final BillingService billingService;

    public BillingObserver(BillingService billingService) {
        this.billingService = billingService;
    }

    @Override
    public void onReservationEvent(ReservationEvent event) {
        if (event.getType() == EventType.CREATED) {
            try {
                billingService.generateBill(event.getReservation().getReservationId());
                log.info("Automatic bill generated for reservation ID: {}", event.getReservation().getReservationId());
            } catch (SQLException e) {
                log.error("Failed to generate automatic bill for reservation {}: {}",
                        event.getReservation().getReservationId(), e.getMessage());
            } catch (IllegalStateException e) {
                log.warn("Could not generate bill for reservation {}: {}",
                        event.getReservation().getReservationId(), e.getMessage());
            }
        }
    }
}
