package com.oceanview.service;

import com.oceanview.dao.GuestDao;
import com.oceanview.model.Guest;
import com.oceanview.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * GuestService — business logic for guest management.
 */
public class GuestService {

    private final GuestDao guestDao;

    public GuestService(GuestDao guestDao) {
        this.guestDao = guestDao;
    }

    public Guest registerGuest(Guest guest) throws SQLException {
        ValidationUtil.requireNonBlank(guest.getFirstName(), "First name");
        ValidationUtil.requireNonBlank(guest.getLastName(), "Last name");
        ValidationUtil.validateEmail(guest.getEmail());
        ValidationUtil.validatePhone(guest.getContactNumber());
        ValidationUtil.requireNonBlank(guest.getAddress(), "Address");
        ValidationUtil.requireNonBlank(guest.getIdNumber(), "ID number");

        if (guestDao.findByEmail(guest.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "A guest with email '" + guest.getEmail() + "' already exists.");
        }

        guest.setEmail(guest.getEmail().toLowerCase().trim());
        return guestDao.insert(guest);
    }

    public Optional<Guest> findById(int id) throws SQLException {
        return guestDao.findById(id);
    }

    public Optional<Guest> findByEmail(String email) throws SQLException {
        return guestDao.findByEmail(email);
    }

    public List<Guest> findAll() throws SQLException {
        return guestDao.findAll();
    }

    public List<Guest> search(String query) throws SQLException {
        ValidationUtil.requireNonBlank(query, "Search query");
        return guestDao.search(query.trim());
    }

    public void updateGuest(Guest guest) throws SQLException {
        ValidationUtil.requireNonBlank(guest.getFirstName(), "First name");
        ValidationUtil.requireNonBlank(guest.getLastName(), "Last name");
        ValidationUtil.validateEmail(guest.getEmail());
        ValidationUtil.validatePhone(guest.getContactNumber());
        guestDao.update(guest);
    }

    public void deleteGuest(int guestId) throws SQLException {
        guestDao.delete(guestId);
    }
}
