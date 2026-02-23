package com.oceanview.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * PasswordUtil — wraps BCrypt hashing and verification.
 * Uses cost factor 12 (good balance of security vs. performance).
 */
public final class PasswordUtil {

    private static final int COST = 12;

    private PasswordUtil() {
    } // utility class — no instantiation

    /**
     * Hashes a plain-text password.
     * 
     * @param plainPassword the raw password
     * @return BCrypt hash string (60 chars)
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     * 
     * @param plainPassword the plain-text attempt
     * @param hash          the stored hash
     * @return true if they match
     */
    public static boolean verify(String plainPassword, String hash) {
        if (plainPassword == null || hash == null)
            return false;
        BCrypt.Result result = BCrypt.verifyer().verify(
                plainPassword.toCharArray(), hash);
        return result.verified;
    }
}
