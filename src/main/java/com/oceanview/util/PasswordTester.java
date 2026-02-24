package com.oceanview.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordTester {
    public static void main(String[] args) {
        String p = "Admin@1234";
        String h = BCrypt.withDefaults().hashToString(12, p.toCharArray());
        System.out.println("HASH_START:" + h + ":HASH_END");
        System.out.println("VERIFY:" + BCrypt.verifyer().verify(p.toCharArray(), h).verified);
    }
}
