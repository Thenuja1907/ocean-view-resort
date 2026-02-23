package com.oceanview.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hash_returnsNonNullString() {
        String hash = PasswordUtil.hash("Admin@1234");
        assertNotNull(hash);
        assertFalse(hash.isBlank());
    }

    @Test
    void hash_producesValidBcryptHash() {
        String hash = PasswordUtil.hash("Admin@1234");
        assertTrue(hash.startsWith("$2"), "BCrypt hashes start with $2");
    }

    @Test
    void hash_blankPassword_throws() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash("   "));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hash(null));
    }

    @Test
    void verify_correctPassword_returnsTrue() {
        String hash = PasswordUtil.hash("Secret1!");
        assertTrue(PasswordUtil.verify("Secret1!", hash));
    }

    @Test
    void verify_wrongPassword_returnsFalse() {
        String hash = PasswordUtil.hash("Secret1!");
        assertFalse(PasswordUtil.verify("WrongPass1!", hash));
    }

    @Test
    void verify_nullArgs_returnsFalse() {
        assertFalse(PasswordUtil.verify(null, "$2a$12$dummy"));
        assertFalse(PasswordUtil.verify("Password1", null));
    }

    @Test
    void hash_uniqueForSamePlaintext() {
        // BCrypt uses a random salt — two hashes of the same plain-text must differ
        String h1 = PasswordUtil.hash("Admin@1234");
        String h2 = PasswordUtil.hash("Admin@1234");
        assertNotEquals(h1, h2, "BCrypt should produce different salts");
        // but both should verify
        assertTrue(PasswordUtil.verify("Admin@1234", h1));
        assertTrue(PasswordUtil.verify("Admin@1234", h2));
    }
}
