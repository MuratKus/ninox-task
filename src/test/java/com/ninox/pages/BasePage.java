package com.ninox.pages;

import com.ninox.utils.ConfigManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.util.List;

public abstract class BasePage {
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    
    // Common cookie/privacy panel locators - shared across all pages
    protected final By cookieAcceptButton = By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll");
    protected final By cookieAcceptButtonAlt = By.xpath("//button[contains(text(), 'Allow all') or contains(text(), 'Accept') or contains(text(), 'OK') or contains(text(), 'Agree')]");
    protected final By cookieRejectButton = By.xpath("//button[contains(text(), 'Reject') or contains(text(), 'Decline') or contains(@id, 'reject')]");
    protected final By cookieCloseButton = By.xpath("//button[contains(@aria-label, 'close') or contains(@class, 'close') or text()='×']");
    protected final By cookiePanel = By.xpath("//*[contains(@id, 'CybotCookiebot') or contains(@class, 'cookie') or contains(@class, 'privacy') or contains(@class, 'consent')]");
    
    // Common navigation elements
    protected final By[] signInLinkSelectors = {
        By.xpath("//a[contains(text(), 'Sign in') or contains(text(), 'Login') or contains(text(), 'Log in')]"),
        By.xpath("//button[contains(text(), 'Sign in') or contains(text(), 'Login') or contains(text(), 'Log in')]"),
        By.xpath("//*[contains(@href, 'sign-in') or contains(@href, 'login')]")
    };
    
    // Common error message selectors
    protected final By[] errorMessageSelectors = {
        By.xpath("//*[@role='alert']"),
        By.xpath("//*[contains(@class, 'error') and contains(text(), '.')]"),
        By.xpath("//*[contains(@class, 'message') and contains(@class, 'error')]")
    };
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getTimeout()));
    }
    
    /**
     * Helper method to find element using multiple selector strategies with fallback
     * This is the core pattern used across all page objects to handle dynamic selectors
     */
    protected WebElement findElementWithFallback(By[] selectors, String elementName) {
        for (By selector : selectors) {
            try {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
                logger.debug("Found {} using selector: {}", elementName, selector);
                return element;
            } catch (Exception e) {
                logger.debug("Selector {} failed for {}: {}", selector, elementName, e.getMessage());
            }
        }
        throw new RuntimeException("Could not find " + elementName + " with any of the available selectors");
    }
    
    /**
     * Helper method to check if element exists using multiple strategies
     */
    protected boolean elementExistsWithFallback(By[] selectors, String elementName) {
        for (By selector : selectors) {
            try {
                if (!driver.findElements(selector).isEmpty()) {
                    logger.debug("Found {} using selector: {}", elementName, selector);
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Selector {} failed for {}: {}", selector, elementName, e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Handle cookie/privacy panels that appear across all Ninox pages
     * This is critical for test stability as these panels block interactions
     */
    protected void handleCookiePanel() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3)); // Reduced from 8 to 3
            
            // Check only twice instead of 3 times for faster execution  
            for (int attempt = 0; attempt < 2; attempt++) {
                if (!driver.findElements(cookiePanel).isEmpty()) {
                    logger.info("Cookie/privacy panel detected (attempt {}), attempting to dismiss", attempt + 1);
                    
                    // Try different dismiss strategies in order of preference
                    if (tryClickButton(cookieAcceptButton, "Accept cookies (Cookiebot)")) {
                        waitForCookiePanelToDisappear();
                        return;
                    }
                    if (tryClickButton(cookieAcceptButtonAlt, "Accept cookies (generic)")) {
                        waitForCookiePanelToDisappear();
                        return;
                    }
                    if (tryClickButton(cookieRejectButton, "Reject cookies")) {
                        waitForCookiePanelToDisappear();
                        return;
                    }
                    if (tryClickButton(cookieCloseButton, "Close cookie panel")) {
                        waitForCookiePanelToDisappear();
                        return;
                    }
                } else {
                    // Quick check if panel appears - reduced wait time
                    try {
                        Thread.sleep(500); // Quick 500ms wait instead of explicit wait
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            // Final check and last resort methods
            if (!driver.findElements(cookiePanel).isEmpty()) {
                logger.warn("Cookie panel still present, trying last resort methods");
                dismissPanelWithKeyboard();
            }
            
        } catch (Exception e) {
            logger.debug("Error handling cookie panel: {}", e.getMessage());
        }
    }
    
    /**
     * Try clicking a button with error handling and logging
     */
    protected boolean tryClickButton(By locator, String description) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            WebElement button = shortWait.until(ExpectedConditions.elementToBeClickable(locator));
            button.click();
            logger.info("Successfully clicked: {}", description);
            return true;
        } catch (Exception e) {
            logger.debug("Could not click {}: {}", description, e.getMessage());
            return false;
        }
    }
    
    /**
     * Wait for cookie panel to disappear after dismissal
     */
    private void waitForCookiePanelToDisappear() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
            logger.info("Cookie panel successfully dismissed");
        } catch (Exception e) {
            logger.debug("Cookie panel might still be visible: {}", e.getMessage());
        }
    }
    
    /**
     * Last resort method to dismiss panels using keyboard
     */
    private void dismissPanelWithKeyboard() {
        try {
            // Try pressing Escape key
            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
            WebDriverWait veryShortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
            veryShortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
            logger.info("Successfully dismissed panel with Escape key");
        } catch (Exception e) {
            logger.warn("Escape key method failed: {}", e.getMessage());
            
            // Try clicking outside the panel
            try {
                driver.findElement(By.tagName("body")).click();
                WebDriverWait veryShortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                veryShortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
                logger.info("Successfully dismissed panel by clicking body");
            } catch (Exception ex) {
                logger.warn("Body click method failed: {}", ex.getMessage());
            }
        }
    }
    
    /**
     * Multi-strategy click method used across all pages for reliable interactions
     */
    protected void clickElementWithStrategies(WebElement element, String elementName) {
        String urlBeforeClick = driver.getCurrentUrl();
        logger.info("URL before clicking {}: {}", elementName, urlBeforeClick);
        
        // Scroll to element to ensure it's visible
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        
        // Wait for element to be clickable
        wait.until(ExpectedConditions.elementToBeClickable(element));
        
        boolean clicked = false;
        
        // Strategy 1: Regular click
        try {
            element.click();
            clicked = true;
            logger.info("{} clicked (regular click)", elementName);
        } catch (Exception e) {
            logger.debug("Regular click failed for {}: {}", elementName, e.getMessage());
        }
        
        // Strategy 2: JavaScript click if regular click failed
        if (!clicked) {
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                clicked = true;
                logger.info("{} clicked (JavaScript click)", elementName);
            } catch (Exception e) {
                logger.debug("JavaScript click failed for {}: {}", elementName, e.getMessage());
            }
        }
        
        // Strategy 3: Actions click if others failed
        if (!clicked) {
            try {
                new org.openqa.selenium.interactions.Actions(driver)
                    .moveToElement(element)
                    .click()
                    .perform();
                logger.info("{} clicked (Actions click)", elementName);
            } catch (Exception e) {
                logger.error("All click strategies failed for {}: {}", elementName, e.getMessage());
                throw new RuntimeException("Could not click " + elementName, e);
            }
        }
        
        // Check for URL change or page state change  
        try {
            Thread.sleep(1000); // Reduced from 2000ms to 1000ms
            String urlAfterClick = driver.getCurrentUrl(); 
            logger.info("URL after clicking {}: {}", elementName, urlAfterClick);
            
            if (!urlBeforeClick.equals(urlAfterClick)) {
                logger.info("✅ URL changed after clicking {} - navigation detected", elementName);
            } else {
                logger.debug("URL unchanged after clicking {} - checking for other changes", elementName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Navigate to the homepage and find sign-in link
     * This provides realistic user navigation flow
     */
    public void navigateToSignInViaHomepage(String baseUrl) {
        logger.info("Navigating to homepage to find sign-in link: {}", baseUrl);
        driver.get(baseUrl);
        
        // Handle cookie panel on homepage
        handleCookiePanel();
        
        // Find and click sign-in link
        WebElement signInLink = findElementWithFallback(signInLinkSelectors, "sign-in link");
        clickElementWithStrategies(signInLink, "Sign-in link");
        
        logger.info("Successfully clicked sign-in link from homepage");
    }
    
    /**
     * Get any error message found on the page
     */
    public String getErrorMessage() {
        try {
            for (By selector : errorMessageSelectors) {
                List<WebElement> errors = driver.findElements(selector);
                if (!errors.isEmpty() && errors.get(0).isDisplayed()) {
                    String errorText = errors.get(0).getText();
                    logger.info("Error message found: {}", errorText);
                    return errorText;
                }
            }
            return "";
        } catch (Exception e) {
            logger.warn("Error finding error message: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Check if current URL contains any of the given path segments
     */
    protected boolean urlContains(String... pathSegments) {
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        for (String segment : pathSegments) {
            if (currentUrl.contains(segment.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Wait for URL to change from current URL (useful for form submissions)
     */
    protected boolean waitForUrlChange(String originalUrl, int timeoutSeconds) {
        try {
            WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            urlWait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(originalUrl)));
            return true;
        } catch (Exception e) {
            logger.debug("URL did not change from: {}", originalUrl);
            return false;
        }
    }
    
    /**
     * Abstract method that each page must implement for page-specific loading logic
     */
    public abstract void waitForPageToLoad();
}