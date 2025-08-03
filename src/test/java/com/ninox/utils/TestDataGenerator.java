package com.ninox.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {
    
    public static String generateUniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
    
    public static String generateStrongPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        password.append("A"); // Uppercase letter
        password.append("a"); // Lowercase letter
        password.append("1"); // Digit
        password.append("!"); // Special character
        
        for (int i = 4; i < 12; i++) {
            password.append(characters.charAt(ThreadLocalRandom.current().nextInt(characters.length())));
        }
        
        return password.toString();
    }
    
    public static String generateWeakPassword() {
        return "123";
    }
    
    public static String generateInvalidEmail() {
        return "invalid-email";
    }
    
    public static String getKnownDuplicateEmail() {
        return "existing-user@ninox.com";
    }
    
    public static String generateUniqueEmailWithDomain(String domain) {
        String localPart = "test-" + UUID.randomUUID().toString().substring(0, 8);
        return localPart + domain;
    }
}