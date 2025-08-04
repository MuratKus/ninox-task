package com.ninox.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

public class LoginPage extends BasePage {
    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);
    
    // Primary locators for login form
    private final By[] emailFieldSelectors = {
        By.id("email"),
        By.name("email"),
        By.xpath("//input[@type='email']"),
        By.xpath("//input[@placeholder='Email' or @placeholder='Email address']")
    };
    
    private final By[] passwordFieldSelectors = {
        By.id("password"),
        By.name("password"),
        By.xpath("//input[@type='password']"),
        By.xpath("//input[@placeholder='Password']")
    };
    
    private final By[] loginButtonSelectors = {
        By.xpath("//button[@data-testid='login']"), // Primary selector - exact match
        By.xpath("//button[.//span[contains(text(), 'Log in')]]"), // Fallback - text in span
        By.xpath("//button[@title=' Log in']"), // Fallback - title attribute
        By.xpath("//button[@type='button' and .//span[contains(text(), 'Log in')]]"), // Fallback - type + text
        By.xpath("//button[contains(@class, 'Button') and .//span[contains(text(), 'Log in')]]") // Fallback - class + text
    };
    
    public LoginPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToLoginPage(String baseUrl) {
        navigateToLoginPage(baseUrl, false); // Default to fast navigation
    }
    
    public void navigateToLoginPage(String baseUrl, boolean useRealisticFlow) {
        if (useRealisticFlow) {
            logger.info("Navigating to login page via realistic user flow from homepage");
            navigateToSignInViaHomepage(baseUrl);
        } else {
            // Fast direct navigation for performance tests
            String loginUrl = baseUrl.contains("ninox.com") ? 
                baseUrl + "/en/sign-in" : 
                baseUrl + "/sign-in";
            
            logger.info("Navigating directly to login page: {}", loginUrl);
            driver.get(loginUrl);
            handleCookiePanel(); // Still handle cookies but on login page directly
        }
        waitForPageToLoad();
    }
    
    @Override
    public void waitForPageToLoad() {
        logger.info("Waiting for login page to load...");
        
        try {
            // Simple, fast check - wait for page title to contain "Sign in" or URL to contain "sign-in"
            wait.until(driver -> 
                driver.getTitle().toLowerCase().contains("sign") || 
                driver.getCurrentUrl().contains("sign-in") ||
                !driver.findElements(By.xpath("//input[@type='email' or @name='email']")).isEmpty()
            );
            logger.info("Login page loaded successfully");
        } catch (Exception e) {
            logger.error("Login page failed to load: {}", e.getMessage());
            throw e;
        }
    }
    
    public void enterEmail(String email) {
        logger.info("Entering email: {}", email);
        WebElement emailElement = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@type='email' or @name='email' or @id='email']")));
        emailElement.clear();
        emailElement.sendKeys(email);
    }
    
    public void enterPassword(String password) {
        logger.info("Entering password");
        WebElement passwordElement = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//input[@type='password' or @name='password' or @id='password']")));
        passwordElement.clear();
        passwordElement.sendKeys(password);
    }
    
    public void clickLogin() {
        logger.info("Clicking Login button");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[@data-testid='login' or .//span[contains(text(), 'Log in')]]")));
        button.click();
    }
    
    public boolean isLoginSuccessful() {
        try {
            // Check if redirected away from login page
            if (!urlContains("login", "sign-in")) {
                logger.info("Login successful - redirected to: {}", driver.getCurrentUrl());
                return true;
            }
            
            // Check for dashboard or app indicators
            if (urlContains("dashboard", "app", "home", "workspace")) {
                logger.info("Login successful - reached app: {}", driver.getCurrentUrl());
                return true;
            }
            
            logger.warn("Login success could not be determined");
            return false;
            
        } catch (Exception e) {
            logger.warn("Error checking login success: {}", e.getMessage());
            return false;
        }
    }
    
    
    public boolean isEmailFieldVisible() {
        try {
            WebElement emailElement = findElementWithFallback(emailFieldSelectors, "email field");
            return emailElement.isDisplayed();
        } catch (Exception e) {
            logger.warn("Email field not visible: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isPasswordFieldVisible() {
        try {
            WebElement passwordElement = findElementWithFallback(passwordFieldSelectors, "password field");
            return passwordElement.isDisplayed();
        } catch (Exception e) {
            logger.warn("Password field not visible: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isLoginButtonEnabled() {
        try {
            WebElement button = findElementWithFallback(loginButtonSelectors, "login button");
            boolean enabled = button.isEnabled() && button.isDisplayed();
            logger.info("Login button enabled and visible: {}", enabled);
            return enabled;
        } catch (Exception e) {
            logger.warn("Login button not found: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean areLoginFieldsVisible() {
        try {
            return isEmailFieldVisible() && isPasswordFieldVisible();
        } catch (Exception e) {
            logger.warn("Login fields not visible: {}", e.getMessage());
            return false;
        }
    }
}