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

public class HomePage {
    private static final Logger logger = LoggerFactory.getLogger(HomePage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Try for free button selectors
    private final By[] tryForFreeButtonSelectors = {
        By.xpath("//a[contains(@class, 'nav_button primary w-button') and contains(@href, '/en/sign-up')]"),
        By.xpath("//a[contains(@class, 'nav_button') and contains(text(), 'Try for free')]"),
        By.xpath("//a[contains(@href, '/en/sign-up')]"),
        By.xpath("//a[contains(text(), 'Try for free')]"),
        By.linkText("Try for free")
    };
    
    // Cookie/Privacy panel locators - Same as SignUpPage for consistency
    private final By cookieAcceptButton = By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll");
    private final By cookieAcceptButtonAlt = By.xpath("//button[contains(text(), 'Allow all') or contains(text(), 'Accept') or contains(text(), 'OK') or contains(text(), 'Agree')]");
    private final By cookieRejectButton = By.xpath("//button[contains(text(), 'Reject') or contains(text(), 'Decline') or contains(@id, 'reject')]");
    private final By cookieCloseButton = By.xpath("//button[contains(@aria-label, 'close') or contains(@class, 'close') or text()='×']");
    private final By cookiePanel = By.xpath("//*[contains(@id, 'CybotCookiebot') or contains(@class, 'cookie') or contains(@class, 'privacy') or contains(@class, 'consent')]");
    
    public HomePage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver cannot be null");
        }
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigManager.getTimeout()));
    }
    
    // Helper method to find element using multiple selector strategies
    private WebElement findElementWithFallback(By[] selectors, String elementName) {
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
    
    public void navigateToHomePage(String baseUrl) {
        String homeUrl = baseUrl.contains("ninox.com") ? 
            baseUrl + "/en" : 
            baseUrl + "/en";
        
        logger.info("Navigating to home page: {}", homeUrl);
        driver.get(homeUrl);
        waitForPageToLoad();
    }
    
    public void waitForPageToLoad() {
        // Handle cookie panel first
        handleCookiePanel();
        
        // Wait for Try for free button to be present
        try {
            findElementWithFallback(tryForFreeButtonSelectors, "Try for free button");
            logger.info("Home page loaded successfully");
        } catch (Exception e) {
            logger.warn("Could not find Try for free button, but continuing: {}", e.getMessage());
        }
    }
    
    private void handleCookiePanel() {
        try {
            // Wait briefly for cookie panel to appear
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            if (driver.findElements(cookiePanel).size() > 0) {
                logger.info("Cookie/privacy panel detected on home page, attempting to dismiss");
                
                // Try different dismiss strategies in order of preference
                if (tryClickButton(cookieAcceptButton, "Accept cookies (Cookiebot)")) return;
                if (tryClickButton(cookieAcceptButtonAlt, "Accept cookies (generic)")) return;
                if (tryClickButton(cookieRejectButton, "Reject cookies")) return;
                if (tryClickButton(cookieCloseButton, "Close cookie panel")) return;
                
                logger.warn("Could not dismiss cookie panel with standard methods");
                
                // Last resort: try pressing Escape key
                try {
                    driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                    logger.info("Attempted to dismiss cookie panel with Escape key");
                } catch (Exception e) {
                    logger.warn("Escape key method failed: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.debug("No cookie panel found or error handling it: {}", e.getMessage());
        }
    }
    
    private boolean tryClickButton(By locator, String description) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement button = shortWait.until(ExpectedConditions.elementToBeClickable(locator));
            button.click();
            logger.info("Successfully clicked: {}", description);
            
            // Wait for panel to disappear
            try {
                shortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
                logger.debug("Cookie panel dismissed successfully");
            } catch (Exception e) {
                logger.debug("Cookie panel visibility check completed: {}", e.getMessage());
            }
            
            return true;
        } catch (Exception e) {
            logger.debug("Could not click {}: {}", description, e.getMessage());
            return false;
        }
    }
    
    public SignUpPage clickTryForFree() {
        logger.info("Clicking Try for free button");
        WebElement button = findElementWithFallback(tryForFreeButtonSelectors, "Try for free button");
        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
        
        // Wait for navigation to sign-up page
        wait.until(ExpectedConditions.urlContains("sign-up"));
        logger.info("Successfully navigated to sign-up page");
        
        return new SignUpPage(driver);
    }
    
    public boolean isTryForFreeButtonVisible() {
        try {
            WebElement button = findElementWithFallback(tryForFreeButtonSelectors, "Try for free button");
            return button.isDisplayed();
        } catch (Exception e) {
            logger.warn("Try for free button not visible: {}", e.getMessage());
            return false;
        }
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    public String getPageTitle() {
        return driver.getTitle();
    }
}