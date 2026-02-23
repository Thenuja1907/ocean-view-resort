package com.oceanview.pattern;

import com.oceanview.model.Room;
import com.oceanview.model.Room.RoomType;

import java.math.BigDecimal;

/**
 * RoomFactory — Factory Pattern.
 *
 * Creates pre-configured Room objects based on RoomType,
 * applying default rates and descriptions from the enum.
 * Custom overrides are supported via the overloaded method.
 */
public final class RoomFactory {

    private RoomFactory() {}   // static factory only

    /**
     * Creates a Room with all default values for the given type.
     *
     * @param roomNumber unique room number (e.g. "301")
     * @param roomType   the enum type
     * @param floor      floor number
     * @return fully constructed Room
     */
    public static Room createRoom(String roomNumber, RoomType roomType, int floor) {
        return switch (roomType) {
            case STANDARD   -> buildRoom(roomNumber, roomType, floor, 2,
                    BigDecimal.valueOf(8500.00),
                    "Cosy standard room with garden view",
                    "WiFi, AC, TV, Mini-bar");

            case DELUXE     -> buildRoom(roomNumber, roomType, floor, 2,
                    BigDecimal.valueOf(14000.00),
                    "Spacious deluxe room with partial ocean view",
                    "WiFi, AC, Smart TV, Mini-bar, Bathtub");

            case OCEAN_VIEW -> buildRoom(roomNumber, roomType, floor, 2,
                    BigDecimal.valueOf(20000.00),
                    "Stunning panoramic ocean view room",
                    "WiFi, AC, Smart TV, Mini-bar, Jacuzzi, Balcony");

            case SUITE      -> buildRoom(roomNumber, roomType, floor, 4,
                    BigDecimal.valueOf(35000.00),
                    "Luxurious suite with separate living area",
                    "WiFi, AC, Smart TV, Mini-bar, Jacuzzi, Kitchenette, Balcony");

            case PENTHOUSE  -> buildRoom(roomNumber, roomType, floor, 6,
                    BigDecimal.valueOf(75000.00),
                    "Exclusive penthouse with private rooftop terrace",
                    "WiFi, AC, Smart TV, Full Bar, Private Pool, Butler, Private Chef");
        };
    }

    /**
     * Creates a Room with fully custom parameters.
     */
    public static Room createRoom(String roomNumber, RoomType roomType, int floor,
                                  int capacity, BigDecimal rate,
                                  String description, String amenities) {
        return buildRoom(roomNumber, roomType, floor, capacity, rate, description, amenities);
    }

    // ─── Private builder ──────────────────────────────────────────────────────

    private static Room buildRoom(String roomNumber, RoomType roomType,
                                  int floor, int capacity,
                                  BigDecimal rate,
                                  String description, String amenities) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setFloorNumber(floor);
        room.setCapacity(capacity);
        room.setRatePerNight(rate);
        room.setDescription(description);
        room.setAmenities(amenities);
        room.setAvailable(true);
        return room;
    }
}
