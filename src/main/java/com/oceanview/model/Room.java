package com.oceanview.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Room entity — maps to the 'rooms' table.
 * Factory pattern: RoomFactory creates concrete instances.
 */
public class Room {

    public enum RoomType {
        STANDARD("Standard Room",   8500.00),
        DELUXE("Deluxe Room",      14000.00),
        OCEAN_VIEW("Ocean View",   20000.00),
        SUITE("Suite",             35000.00),
        PENTHOUSE("Penthouse",     75000.00);

        private final String displayName;
        private final double defaultRate;

        RoomType(String displayName, double defaultRate) {
            this.displayName = displayName;
            this.defaultRate = defaultRate;
        }
        public String getDisplayName() { return displayName; }
        public double getDefaultRate() { return defaultRate; }
    }

    private int         roomId;
    private String      roomNumber;
    private RoomType    roomType;
    private int         floorNumber;
    private int         capacity;
    private BigDecimal  ratePerNight;
    private String      description;
    private String      amenities;
    private boolean     available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Room() {}

    public Room(String roomNumber, RoomType roomType, int floorNumber,
                int capacity, BigDecimal ratePerNight, String description, String amenities) {
        this.roomNumber   = roomNumber;
        this.roomType     = roomType;
        this.floorNumber  = floorNumber;
        this.capacity     = capacity;
        this.ratePerNight = ratePerNight;
        this.description  = description;
        this.amenities    = amenities;
        this.available    = true;
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getRoomId()                        { return roomId; }
    public void setRoomId(int roomId)             { this.roomId = roomId; }

    public String getRoomNumber()                 { return roomNumber; }
    public void setRoomNumber(String roomNumber)  { this.roomNumber = roomNumber; }

    public RoomType getRoomType()                 { return roomType; }
    public void setRoomType(RoomType roomType)    { this.roomType = roomType; }

    public int getFloorNumber()                   { return floorNumber; }
    public void setFloorNumber(int floorNumber)   { this.floorNumber = floorNumber; }

    public int getCapacity()                      { return capacity; }
    public void setCapacity(int capacity)         { this.capacity = capacity; }

    public BigDecimal getRatePerNight()           { return ratePerNight; }
    public void setRatePerNight(BigDecimal rate)  { this.ratePerNight = rate; }

    public String getDescription()               { return description; }
    public void setDescription(String desc)      { this.description = desc; }

    public String getAmenities()                 { return amenities; }
    public void setAmenities(String amenities)   { this.amenities = amenities; }

    public boolean isAvailable()                 { return available; }
    public void setAvailable(boolean available)  { this.available = available; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)    { this.updatedAt = t; }

    @Override
    public String toString() {
        return "Room{roomId=" + roomId + ", roomNumber='" + roomNumber +
               "', type=" + roomType + ", rate=" + ratePerNight + ", available=" + available + '}';
    }
}
