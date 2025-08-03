package com.ninox.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    
    // Default values
    private static final String DEFAULT_BASE_URL = "https://q-www.ninox.com";
    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_HEADLESS = "false";
    private static final String DEFAULT_TIMEOUT = "10";
    private static final String DEFAULT_ENVIRONMENT = "staging";
    
    public static String getBaseUrl() {
        String baseUrl = System.getProperty("base.url");
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = getEnvironmentUrl();
        }
        return baseUrl;
    }
    
    public static String getBrowser() {
        String browser = System.getProperty("browser");
        if (browser == null || browser.trim().isEmpty()) {
            browser = DEFAULT_BROWSER;
        }
        return browser.toLowerCase();
    }
    
    public static boolean isHeadless() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", DEFAULT_HEADLESS));
        return headless;
    }
    
    public static int getTimeout() {
        String timeoutStr = System.getProperty("timeout", DEFAULT_TIMEOUT);
        try {
            int timeout = Integer.parseInt(timeoutStr);
            return timeout;
        } catch (NumberFormatException e) {
            logger.warn("Invalid timeout value '{}', using default: {}", timeoutStr, DEFAULT_TIMEOUT);
            return Integer.parseInt(DEFAULT_TIMEOUT);
        }
    }
    
    public static String getEnvironment() {
        String env = System.getProperty("environment", DEFAULT_ENVIRONMENT);
        return env.toLowerCase();
    }
    
    private static String getEnvironmentUrl() {
        String environment = getEnvironment();
        switch (environment) {
            case "production":
            case "prod":
                return "https://ninox.com";
            case "staging":
            case "stage":
            default:
                return DEFAULT_BASE_URL;
        }
    }
    
    // Additional configuration getters
    public static boolean isParallelExecution() {
        return Boolean.parseBoolean(System.getProperty("parallel", "false"));
    }
    
    public static int getThreadCount() {
        String threadStr = System.getProperty("thread.count", "1");
        try {
            return Integer.parseInt(threadStr);
        } catch (NumberFormatException e) {
            logger.warn("Invalid thread count '{}', using default: 1", threadStr);
            return 1;
        }
    }
    
    public static boolean isRetryEnabled() {
        return Boolean.parseBoolean(System.getProperty("retry.enabled", "true"));
    }
    
    public static int getMaxRetries() {
        String retryStr = System.getProperty("max.retries", "2");
        try {
            return Integer.parseInt(retryStr);
        } catch (NumberFormatException e) {
            logger.warn("Invalid retry count '{}', using default: 2", retryStr);
            return 2;
        }
    }
    
    // Debug method to print all configuration
    public static void printConfiguration() {
        logger.info("=== Test Configuration ===");
        logger.info("Base URL: {}", getBaseUrl());
        logger.info("Browser: {}", getBrowser());
        logger.info("Headless: {}", isHeadless());
        logger.info("Timeout: {} seconds", getTimeout());
        logger.info("Environment: {}", getEnvironment());
        logger.info("Parallel Execution: {}", isParallelExecution());
        logger.info("Thread Count: {}", getThreadCount());
        logger.info("Retry Enabled: {}", isRetryEnabled());
        logger.info("Max Retries: {}", getMaxRetries());
        logger.info("========================");
    }
}