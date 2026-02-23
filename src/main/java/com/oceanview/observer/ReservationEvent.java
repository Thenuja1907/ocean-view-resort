package com.oceanview.observer;

import com.oceanview.model.Reservation;

/**
 * ReservationEvent — immutable data passed to every Observer.
 * Carries the type of event and the affected Reservation.
 */
public class ReservationEvent {

    public enum EventType {
        CREATED, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    }

    private final EventType type;
    private final Reservation reservation;
    private final String actorIp;

    public ReservationEvent(EventType type, Reservation reservation, String actorIp) {
        this.type = type;
        this.reservation = reservation;
        this.actorIp = actorIp;
    }

    public EventType getType() {
        return type;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getActorIp() {
        return actorIp;
    }

    @Override
    public String toString() {
        return "ReservationEvent{type=" + type +
                ", reservationId=" + reservation.getReservationId() + '}';
    }
}
