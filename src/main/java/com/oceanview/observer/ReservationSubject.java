package com.oceanview.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * ReservationSubject — Subject (Observable) in the Observer pattern.
 * ReservationService holds a reference to this and fires events on state
 * changes.
 */
public class ReservationSubject {

    private final List<ReservationObserver> observers = new ArrayList<>();

    public void addObserver(ReservationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(ReservationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ReservationEvent event) {
        for (ReservationObserver o : observers) {
            o.onReservationEvent(event);
        }
    }
}
