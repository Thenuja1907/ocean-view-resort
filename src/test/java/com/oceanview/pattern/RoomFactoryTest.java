package com.oceanview.pattern;

import com.oceanview.model.Room;
import com.oceanview.model.Room.RoomType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RoomFactoryTest {

    @ParameterizedTest
    @EnumSource(RoomType.class)
    void createRoom_allTypes_returnPopulatedRoom(RoomType type) {
        Room room = RoomFactory.createRoom("101", type, 1);
        assertNotNull(room);
        assertEquals("101", room.getRoomNumber());
        assertEquals(type, room.getRoomType());
        assertEquals(1, room.getFloorNumber());
        assertTrue(room.isAvailable());
        assertNotNull(room.getDescription());
        assertNotNull(room.getAmenities());
        assertNotNull(room.getRatePerNight());
        assertTrue(room.getRatePerNight().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(room.getCapacity() >= 2);
    }

    @Test
    void createRoom_standard_defaultRate8500() {
        Room room = RoomFactory.createRoom("101", RoomType.STANDARD, 1);
        assertEquals(0, room.getRatePerNight().compareTo(new BigDecimal("8500.0")));
    }

    @Test
    void createRoom_penthouse_rateAndCapacity() {
        Room room = RoomFactory.createRoom("501", RoomType.PENTHOUSE, 5);
        assertEquals(0, room.getRatePerNight().compareTo(new BigDecimal("75000.0")));
        assertEquals(6, room.getCapacity());
    }

    @Test
    void createRoom_custom_overridesDefaults() {
        BigDecimal customRate = new BigDecimal("99999.00");
        Room room = RoomFactory.createRoom("999", RoomType.SUITE, 9,
                8, customRate, "Custom suite", "Custom amenities");
        assertEquals(8, room.getCapacity());
        assertEquals(0, room.getRatePerNight().compareTo(customRate));
        assertEquals("Custom suite", room.getDescription());
        assertEquals("Custom amenities", room.getAmenities());
    }
}
