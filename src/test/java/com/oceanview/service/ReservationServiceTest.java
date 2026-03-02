package com.oceanview.service;

import com.oceanview.dao.ReservationDao;
import com.oceanview.dao.RoomDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.observer.ReservationEvent;
import com.oceanview.observer.ReservationSubject;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Test
    void create_successful_insertsAndNotifies() throws SQLException {
        ReservationDao rDao = mock(ReservationDao.class);
        RoomDao roomDao = mock(RoomDao.class);
        ReservationSubject subject = mock(ReservationSubject.class);

        Room rm = new Room(); rm.setRoomId(5); rm.setRoomNumber("101"); rm.setCapacity(2);
        when(roomDao.findById(5)).thenReturn(Optional.of(rm));
        when(rDao.isRoomAvailable(eq(5), any(), any(), eq(0))).thenReturn(true);

        // Emulate current reservations count
        when(rDao.findAll()).thenReturn(List.of());

        // insert should return the passed reservation
        doAnswer(invocation -> {
            Reservation arg = invocation.getArgument(0);
            arg.setReservationId(42);
            return arg;
        }).when(rDao).insert(any());

        Reservation detailed = new Reservation(); detailed.setReservationId(42);
        when(rDao.findById(42)).thenReturn(Optional.of(detailed));

        ReservationService svc = new ReservationService(rDao, roomDao, subject);
        LocalDate in = LocalDate.now().plusDays(1);
        LocalDate out = in.plusDays(2);

        Reservation res = svc.create(10, 5, in, out, 2, "", 1, "127.0.0.1");

        assertEquals(42, res.getReservationId());
        verify(rDao).insert(any());
        verify(roomDao).setAvailability(5, false);
        verify(subject).notifyObservers(any(ReservationEvent.class));
    }
}
