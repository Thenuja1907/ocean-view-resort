package com.oceanview.service;

import com.oceanview.dao.GuestDao;
import com.oceanview.dao.UserDao;
import com.oceanview.model.Guest;
import com.oceanview.model.User;
import com.oceanview.util.PasswordUtil;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * AuthService — handles login / logout business logic for both Staff and
 * Guests.
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;
    private final GuestDao guestDao;

    public AuthService(UserDao userDao, GuestDao guestDao) {
        this.userDao = userDao;
        this.guestDao = guestDao;
    }

    /**
     * Staff Login
     */
    public User login(String username, String plainPassword) throws SQLException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireNonBlank(plainPassword, "Password");

        Optional<User> opt = userDao.findByUsername(username.trim());
        if (opt.isEmpty()) {
            throw new SecurityException("Invalid username or password.");
        }

        User user = opt.get();
        if (!user.isActive()) {
            throw new SecurityException("Account is disabled.");
        }

        if (!PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            throw new SecurityException("Invalid username or password.");
        }

        userDao.updateLastLogin(user.getUserId());
        log.info("Staff '{}' logged in.", username);
        return user;
    }

    /**
     * Guest Login
     */
    public Guest loginGuest(String email, String plainPassword) throws SQLException {
        ValidationUtil.requireNonBlank(email, "Email");
        ValidationUtil.requireNonBlank(plainPassword, "Password");

        Optional<Guest> opt = guestDao.findByEmail(email.trim());
        if (opt.isEmpty()) {
            throw new SecurityException("Invalid email or password.");
        }

        Guest guest = opt.get();
        if (!guest.isActive()) {
            throw new SecurityException("Account is disabled.");
        }

        if (guest.getPasswordHash() == null || !PasswordUtil.verify(plainPassword, guest.getPasswordHash())) {
            throw new SecurityException("Invalid email or password.");
        }

        guestDao.updateLastLogin(guest.getGuestId());
        log.info("Guest '{}' logged in.", email);
        return guest;
    }
}
