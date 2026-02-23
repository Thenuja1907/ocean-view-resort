package com.oceanview.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {
    @Test
    public void testHash() {
        String password = "Admin@1234";
        String hash = PasswordUtil.hash(password);
        System.out.println("GENERATED_HASH: " + hash);
        assertTrue(PasswordUtil.verify(password, hash));
    }
}
