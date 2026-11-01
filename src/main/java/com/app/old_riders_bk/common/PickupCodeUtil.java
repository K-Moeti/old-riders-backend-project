package com.app.old_riders_bk.common;

import java.security.SecureRandom;

public class PickupCodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Generates a code matching the pattern: ORD-8digit-2Letters
     * Example: ORD-82739104-XK
     */
    public static String generatePickupCode() {
        // Generate 8 random digits (10000000 to 99999999)
        int digits = 10000000 + RANDOM.nextInt(90000000);

        // Generate 2 uppercase random letters
        char l1 = LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));
        char l2 = LETTERS.charAt(RANDOM.nextInt(LETTERS.length()));

        return String.format("ORD-%08d-%c%c", digits, l1, l2);
    }
}