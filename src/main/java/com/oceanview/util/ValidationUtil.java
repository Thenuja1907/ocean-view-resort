package com.oceanview.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil — centralised input validation.
 * All methods are static; throws IllegalArgumentException on failure.
 */
public final class ValidationUtil {

    // ─── Regex patterns ───────────────────────────────────────────────────────
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{7,15}$");

    private static final Pattern RESERVATION_NUMBER_PATTERN = Pattern.compile("^OVR-\\d{4}-\\d{6}$");

    private ValidationUtil() {
    }

    // ─── String validations ───────────────────────────────────────────────────

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return value.trim();
    }

    public static String requireMaxLength(String value, int maxLen, String fieldName) {
        requireNonBlank(value, fieldName);
        if (value.trim().length() > maxLen) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + maxLen + " characters.");
        }
        return value.trim();
    }

    // ─── Email ───────────────────────────────────────────────────────────────

    public static String validateEmail(String email) {
        requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return email.trim().toLowerCase();
    }

    // ─── Phone ───────────────────────────────────────────────────────────────

    public static String validatePhone(String phone) {
        requireNonBlank(phone, "Contact number");
        String cleaned = phone.trim().replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    "Invalid contact number. Must be 7-15 digits, optionally prefixed with +.");
        }
        return cleaned;
    }

    // ─── Date range ───────────────────────────────────────────────────────────

    public static void validateDateRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null)
            throw new IllegalArgumentException("Check-in date is required.");
        if (checkOut == null)
            throw new IllegalArgumentException("Check-out date is required.");
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Check-in date cannot be in the past.");
        }
    }

    // ─── Positive integer ────────────────────────────────────────────────────

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive integer.");
        }
        return value;
    }

    // ─── Reservation number ───────────────────────────────────────────────────

    public static boolean isValidReservationNumber(String number) {
        return number != null && RESERVATION_NUMBER_PATTERN.matcher(number).matches();
    }

    // ─── Password strength ────────────────────────────────────────────────────

    public static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUpper || !hasDigit) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter and one digit.");
        }
    }

    // ─── Number of guests ────────────────────────────────────────────────────

    public static void validateNumGuests(int numGuests, int roomCapacity) {
        if (numGuests < 1) {
            throw new IllegalArgumentException("Number of guests must be at least 1.");
        }
        if (numGuests > roomCapacity) {
            throw new IllegalArgumentException(
                    "Number of guests (" + numGuests + ") exceeds room capacity (" + roomCapacity + ").");
        }
    }
}
