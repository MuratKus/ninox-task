package com.ninox.tests;

import com.ninox.utils.BrowserManager;
import com.ninox.utils.ConfigManager;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected WebDriver driver;
    protected String baseUrl;
    
    @BeforeClass
    @Step("Initialize test configuration")
    public void setUpClass() {
        ConfigManager.printConfiguration();
        baseUrl = ConfigManager.getBaseUrl();
        logger.info("Test suite initialized for base URL: {}", baseUrl);
    }
    
    @BeforeMethod
    @Step("Initialize WebDriver")
    public void setUp() {
        String browser = ConfigManager.getBrowser();
        boolean headless = ConfigManager.isHeadless();
        
        // Check if driver already exists and is still valid
        if (driver == null || !isDriverValid()) {
            logger.info("Starting new browser session: {} (headless: {})", browser, headless);
            BrowserManager.initializeDriver(browser, headless);
            driver = BrowserManager.getDriver();
        } else {
            logger.info("Reusing existing browser session");
        }
        
        // Hook for subclasses to add custom setup
        setupTest();
    }
    
    @AfterMethod
    @Step("Clean up WebDriver and capture artifacts on failure")
    public void tearDown(ITestResult result) {
        if (!result.isSuccess()) {
            logger.warn("Test failed: {}", result.getMethod().getMethodName());
            captureFailureArtifacts(result.getMethod().getMethodName());
            
            // For failed tests, start fresh browser session next time
            BrowserManager.quitDriver();
            driver = null;
        } else {
            // For successful tests, clear browser state but keep session
            clearBrowserState();
        }
        
        // Hook for subclasses to add custom teardown
        tearDownTest();
        
        logger.info("Test completed: {}", result.getMethod().getMethodName());
    }
    
    @AfterClass
    @Step("Close browser session after test class")
    public void tearDownClass() {
        // Always quit driver after all tests in class are complete
        BrowserManager.quitDriver();
        driver = null;
        logger.info("Browser session closed after test class completion");
    }
    
    private boolean isDriverValid() {
        try {
            if (driver != null) {
                driver.getCurrentUrl(); // Simple check to see if driver is responsive
                return true;
            }
        } catch (Exception e) {
            logger.debug("Driver is no longer valid: {}", e.getMessage());
        }
        return false;
    }
    
    private void clearBrowserState() {
        try {
            if (driver != null) {
                // Clear cookies and local storage for clean state
                driver.manage().deleteAllCookies();
                
                // Clear local/session storage if supported
                try {
                    ((JavascriptExecutor) driver).executeScript("localStorage.clear();");
                    ((JavascriptExecutor) driver).executeScript("sessionStorage.clear();");
                } catch (Exception e) {
                    logger.debug("Could not clear browser storage: {}", e.getMessage());
                }
                
                logger.debug("Browser state cleared for next test");
            }
        } catch (Exception e) {
            logger.warn("Error clearing browser state: {}", e.getMessage());
        }
    }
    
    /**
     * Hook method for subclasses to implement custom setup logic
     * Called after WebDriver initialization but before test execution
     */
    protected void setupTest() {
        // Default implementation does nothing
        // Subclasses can override this method
    }
    
    /**
     * Hook method for subclasses to implement custom teardown logic
     * Called before WebDriver cleanup
     */
    protected void tearDownTest() {
        // Default implementation does nothing
        // Subclasses can override this method
    }
    
    /**
     * Capture failure artifacts for debugging
     */
    private void captureFailureArtifacts(String testName) {
        try {
            captureScreenshot(testName);
            captureBrowserLogs();
            capturePageSource(testName);
        } catch (Exception e) {
            logger.error("Failed to capture failure artifacts: {}", e.getMessage());
        }
    }
    
    @Attachment(value = "Screenshot", type = "image/png")
    private byte[] captureScreenshot(String testName) {
        try {
            logger.info("Capturing screenshot for test: {}", testName);
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot: {}", e.getMessage());
            return new byte[0];
        }
    }
    
    @Attachment(value = "Browser Console Logs", type = "text/plain")
    private String captureBrowserLogs() {
        try {
            return driver.manage().logs().get("browser").getAll().toString();
        } catch (Exception e) {
            logger.error("Failed to capture console logs: {}", e.getMessage());
            return "Console logs not available: " + e.getMessage();
        }
    }
    
    @Attachment(value = "Page Source", type = "text/html")
    private String capturePageSource(String testName) {
        try {
            logger.debug("Capturing page source for test: {}", testName);
            return driver.getPageSource();
        } catch (Exception e) {
            logger.error("Failed to capture page source: {}", e.getMessage());
            return "<!-- Page source not available: " + e.getMessage() + " -->";
        }
    }
    
    /**
     * Utility method for subclasses to get current page URL
     */
    protected String getCurrentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (Exception e) {
            logger.warn("Could not get current URL: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Utility method for subclasses to get page title
     */
    protected String getPageTitle() {
        try {
            return driver.getTitle();
        } catch (Exception e) {
            logger.warn("Could not get page title: {}", e.getMessage());
            return "";
        }
    }
}