package com.ninox.utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRetryAnalyzer implements IRetryAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(TestRetryAnalyzer.class);
    private final int maxRetryCount;
    private int retryCount = 0;
    
    public TestRetryAnalyzer() {
        this.maxRetryCount = ConfigManager.isRetryEnabled() ? ConfigManager.getMaxRetries() : 0;
    }

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            logger.warn("Test '{}' failed. Retrying... (Attempt {}/{})", 
                       result.getMethod().getMethodName(), retryCount, maxRetryCount);
            return true;
        }
        if (maxRetryCount > 0) {
            logger.error("Test '{}' failed after {} retries", 
                        result.getMethod().getMethodName(), maxRetryCount);
        } else {
            logger.info("Test '{}' failed. Retry disabled.", 
                       result.getMethod().getMethodName());
        }
        return false;
    }
}