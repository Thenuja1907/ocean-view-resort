package com.oceanview.service;

import com.oceanview.dao.RoomDao;
import com.oceanview.model.Room;
import com.oceanview.model.Room.RoomType;
import com.oceanview.pattern.RoomFactory;
import com.oceanview.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * RoomService — business logic for room management.
 */
public class RoomService {

    private final RoomDao roomDao;

    public RoomService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    /** Creates a room using factory defaults for the given type. */
    public Room createRoom(String roomNumber, RoomType type, int floor) throws SQLException {
        ValidationUtil.requireNonBlank(roomNumber, "Room number");
        ValidationUtil.requirePositive(floor, "Floor number");

        if (roomDao.findByRoomNumber(roomNumber).isPresent()) {
            throw new IllegalArgumentException("Room number '" + roomNumber + "' already exists.");
        }

        Room room = RoomFactory.createRoom(roomNumber, type, floor);
        return roomDao.insert(room);
    }

    /** Creates a room with fully custom parameters. */
    public Room createRoom(String roomNumber, RoomType type, int floor,
            int capacity, BigDecimal rate,
            String description, String amenities) throws SQLException {
        ValidationUtil.requireNonBlank(roomNumber, "Room number");
        ValidationUtil.requirePositive(floor, "Floor number");
        ValidationUtil.requirePositive(capacity, "Capacity");

        if (roomDao.findByRoomNumber(roomNumber).isPresent()) {
            throw new IllegalArgumentException("Room number '" + roomNumber + "' already exists.");
        }

        Room room = RoomFactory.createRoom(roomNumber, type, floor, capacity, rate, description, amenities);
        return roomDao.insert(room);
    }

    public Optional<Room> findById(int id) throws SQLException {
        return roomDao.findById(id);
    }

    public List<Room> findAll() throws SQLException {
        return roomDao.findAll();
    }

    public List<Room> findAvailable() throws SQLException {
        return roomDao.findAvailable();
    }

    public void updateRoom(Room room) throws SQLException {
        roomDao.update(room);
    }

    public void setAvailability(int roomId, boolean available) throws SQLException {
        roomDao.setAvailability(roomId, available);
    }

    public void deleteRoom(int roomId) throws SQLException {
        roomDao.delete(roomId);
    }
}
