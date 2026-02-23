package com.oceanview.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String password = "Admin@1234";
        String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        System.out.println("Hash for " + password + ": " + hash);
    }
}
