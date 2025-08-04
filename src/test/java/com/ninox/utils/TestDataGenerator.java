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
    
    public static String generateUniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    // Legitimate test users for different signup flows
    public static class TestUser {
        public final String email;
        public final String password;
        public final String type;
        
        public TestUser(String email, String password, String type) {
            this.email = email;
            this.password = password;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return String.format("TestUser{type='%s', email='%s'}", type, email);
        }
    }
    
    public static TestUser generateWorkTestUser() {
        return new TestUser(
            "work.test+" + generateUniqueId() + "@company.com",
            generateStrongPassword(),
            "work"
        );
    }
    
    public static TestUser generatePersonalTestUser() {
        return new TestUser(
            "personal.test+" + generateUniqueId() + "@gmail.com", 
            generateStrongPassword(),
            "personal"
        );
    }
    
    // Generate legitimate user that can be kept for future tests
    public static TestUser generateLegitimateTestUser() {
        return new TestUser(
            "qa.automation+" + System.currentTimeMillis() + "@ninox-test.com",
            "SecureTest123!@#",
            "legitimate"
        );
    }
    
    // Generate real test account using configured domain (e.g., me+123@murat-kus.com)
    public static TestUser generateRealTestUser() {
        String domain = ConfigManager.getTestEmailDomain();
        String prefix = ConfigManager.getTestEmailPrefix();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return new TestUser(
            prefix + "+" + timestamp + "@" + domain,
            generateStrongPassword(),
            "real-account"
        );
    }
    
    // Generate real personal test user
    public static TestUser generateRealPersonalTestUser() {
        String domain = ConfigManager.getTestEmailDomain();
        String prefix = ConfigManager.getTestEmailPrefix();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return new TestUser(
            prefix + "+personal+" + timestamp + "@" + domain,
            generateStrongPassword(),
            "real-personal"
        );
    }
    
    // Generate real work test user
    public static TestUser generateRealWorkTestUser() {
        String domain = ConfigManager.getTestEmailDomain();
        String prefix = ConfigManager.getTestEmailPrefix();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return new TestUser(
            prefix + "+work+" + timestamp + "@" + domain,
            generateStrongPassword(),
            "real-work"
        );
    }
}