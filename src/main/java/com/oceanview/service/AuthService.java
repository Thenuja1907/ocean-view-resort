package com.oceanview.service;

import com.oceanview.dao.UserDao;
import com.oceanview.model.User;
import com.oceanview.util.PasswordUtil;
import com.oceanview.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * AuthService — handles login / logout business logic.
 */
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Validates username + password and returns the authenticated User.
     *
     * @throws IllegalArgumentException if credentials are blank
     * @throws SecurityException        if credentials are wrong or account is
     *                                  inactive
     * @throws SQLException             on DB error
     */
    public User login(String username, String plainPassword) throws SQLException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireNonBlank(plainPassword, "Password");

        Optional<User> opt = userDao.findByUsername(username.trim());
        if (opt.isEmpty()) {
            log.warn("Login failed — unknown username: {}", username);
            throw new SecurityException("Invalid username or password.");
        }

        User user = opt.get();

        if (!user.isActive()) {
            log.warn("Login rejected — inactive account: {}", username);
            throw new SecurityException("Account is disabled. Contact your administrator.");
        }

        if (!PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            log.warn("Login failed — wrong password for: {}", username);
            throw new SecurityException("Invalid username or password.");
        }

        userDao.updateLastLogin(user.getUserId());
        log.info("User '{}' logged in successfully.", username);
        return user;
    }
}
