package com.oceanview.observer;

/**
 * ReservationObserver — Observer interface (Observer pattern).
 * Implement this to react to reservation lifecycle events.
 */
public interface ReservationObserver {
    void onReservationEvent(ReservationEvent event);
}
