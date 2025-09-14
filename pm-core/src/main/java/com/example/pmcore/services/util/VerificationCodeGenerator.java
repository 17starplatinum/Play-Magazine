package com.example.pmcore.services.util;

import java.security.SecureRandom;

public class VerificationCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generateVerificationCode() {
        return random.ints(CODE_LENGTH, 0, CHARACTERS.length())
                .mapToObj(CHARACTERS::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}