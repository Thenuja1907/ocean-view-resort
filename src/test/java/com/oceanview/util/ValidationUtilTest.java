package com.oceanview.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    // ── requireNonBlank ──────────────────────────────────────────────────────

    @Test
    void requireNonBlank_validValue_returnsTrimmed() {
        assertEquals("hello", ValidationUtil.requireNonBlank("  hello  ", "field"));
    }

    @Test
    void requireNonBlank_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireNonBlank(null, "field"));
    }

    @Test
    void requireNonBlank_blank_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.requireNonBlank("   ", "field"));
    }

    // ── validateEmail ────────────────────────────────────────────────────────

    @Test
    void validateEmail_valid() {
        assertEquals("user@example.com",
                ValidationUtil.validateEmail("User@Example.COM"));
    }

    @Test
    void validateEmail_noAt_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateEmail("notanemail"));
    }

    @Test
    void validateEmail_noDomain_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateEmail("user@"));
    }

    // ── validatePhone ────────────────────────────────────────────────────────

    @Test
    void validatePhone_valid() {
        assertEquals("+94771234567",
                ValidationUtil.validatePhone("+94771234567"));
    }

    @Test
    void validatePhone_withSpaces_cleaned() {
        assertEquals("0711234567",
                ValidationUtil.validatePhone("071 123 4567"));
    }

    @Test
    void validatePhone_tooShort_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePhone("123"));
    }

    // ── validateDateRange ────────────────────────────────────────────────────

    @Test
    void validateDateRange_sameDay_throws() {
        var today = java.time.LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateDateRange(today, today));
    }

    @Test
    void validateDateRange_pastCheckIn_throws() {
        var yesterday = java.time.LocalDate.now().minusDays(1);
        var tomorrow = java.time.LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateDateRange(yesterday, tomorrow));
    }

    @Test
    void validateDateRange_valid_noThrow() {
        var checkIn = java.time.LocalDate.now().plusDays(1);
        var checkOut = java.time.LocalDate.now().plusDays(3);
        assertDoesNotThrow(() -> ValidationUtil.validateDateRange(checkIn, checkOut));
    }

    // ── validatePassword ─────────────────────────────────────────────────────

    @Test
    void validatePassword_tooShort_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePassword("Ab1"));
    }

    @Test
    void validatePassword_noUppercase_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePassword("password1"));
    }

    @Test
    void validatePassword_noDigit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validatePassword("Password"));
    }

    @Test
    void validatePassword_valid_noThrow() {
        assertDoesNotThrow(() -> ValidationUtil.validatePassword("Admin@1234"));
    }

    // ── validateNumGuests ────────────────────────────────────────────────────

    @Test
    void validateNumGuests_exceeds_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateNumGuests(5, 4));
    }

    @Test
    void validateNumGuests_zero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtil.validateNumGuests(0, 2));
    }

    @Test
    void validateNumGuests_valid_noThrow() {
        assertDoesNotThrow(() -> ValidationUtil.validateNumGuests(2, 4));
    }
}
