package com.oceanview.observer;

import com.oceanview.model.Reservation;
import com.oceanview.observer.ReservationEvent.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationSubjectTest {

    private ReservationSubject subject;
    private List<ReservationEvent> received;

    @BeforeEach
    void setUp() {
        subject = new ReservationSubject();
        received = new ArrayList<>();
        subject.addObserver(received::add); // lambda observer
    }

    @Test
    void notifyObservers_oneObserver_receivesEvent() {
        ReservationEvent event = makeEvent(EventType.CREATED);
        subject.notifyObservers(event);
        assertEquals(1, received.size());
        assertSame(event, received.get(0));
    }

    @Test
    void notifyObservers_multipleObservers_allReceive() {
        List<ReservationEvent> second = new ArrayList<>();
        subject.addObserver(second::add);

        ReservationEvent event = makeEvent(EventType.CONFIRMED);
        subject.notifyObservers(event);

        assertEquals(1, received.size());
        assertEquals(1, second.size());
    }

    @Test
    void addObserver_duplicate_notAddedTwice() {
        ReservationObserver obs = received::add;
        subject.addObserver(obs); // already added in setUp; this is a different lambda
        subject.notifyObservers(makeEvent(EventType.CANCELLED));
        // first observer (from setUp) + this new one = 2 notifications
        assertEquals(2, received.size());
    }

    @Test
    void removeObserver_removedObserver_doesNotReceive() {
        ReservationObserver obs = received::add;
        subject.addObserver(obs);
        subject.removeObserver(obs);

        subject.notifyObservers(makeEvent(EventType.CHECKED_OUT));
        // Original setUp observer still gets it once
        assertEquals(1, received.size());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ReservationEvent makeEvent(EventType type) {
        Reservation res = new Reservation();
        res.setReservationId(1);
        res.setStatus(com.oceanview.model.Reservation.Status.PENDING);
        return new ReservationEvent(type, res, "127.0.0.1");
    }
}
