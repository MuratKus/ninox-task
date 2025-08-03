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

public class SignUpPage {
    private static final Logger logger = LoggerFactory.getLogger(SignUpPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Flexible locators - multiple strategies for robustness
    private final By[] emailFieldSelectors = {
        By.id("email"),
        By.name("email"),
        By.xpath("//input[@type='email']"),
        By.xpath("//input[contains(@placeholder, 'email') or contains(@placeholder, 'Email')]")
    };
    
    private final By[] passwordFieldSelectors = {
        By.id("password"),
        By.name("password"),
        By.xpath("//input[@type='password']"),
        By.xpath("//input[contains(@placeholder, 'password') or contains(@placeholder, 'Password')]")
    };
    
    private final By[] marketingCheckboxSelectors = {
        By.id("marketing-consent"),
        By.name("marketing"),
        By.id("idxufwc"),
        By.xpath("//input[@type='checkbox' and @name='marketing']"),
        By.xpath("//input[@type='checkbox']"),
        By.xpath("//*[contains(text(), 'marketing') or contains(text(), 'newsletter')]/..//input[@type='checkbox']")
    };
    
    private final By[] createAccountButtonSelectors = {
        By.xpath("//button[contains(text(), 'Create Account')]"),
        By.xpath("//button[contains(text(), 'Sign up')]"),
        By.xpath("//button[contains(text(), 'Register')]"),
        By.xpath("//button[@type='submit']"),
        By.xpath("//input[@type='submit']")
    };
    
    private final By[] continueWithGoogleButtonSelectors = {
        By.xpath("//button[contains(text(), 'Continue with Google')]"),
        By.xpath("//button[contains(text(), 'Sign up with Google')]"),
        By.xpath("//button[contains(text(), 'Google')]"),
        By.xpath("//*[contains(@class, 'google') or contains(@class, 'oauth')]//button"),
        By.xpath("//button[contains(@aria-label, 'Google')]"),
        By.xpath("//button[contains(@class, 'Container-jFjATm')]"),
        By.xpath("//button[.//span[contains(text(), 'Continue with Google')]]"),
        By.xpath("//span[contains(text(), 'Continue with Google')]/parent::button")
    };
    
    private final By[] errorMessageSelectors = {
        By.xpath("//*[contains(@class, 'error') and contains(text(), '.')]"),
        By.xpath("//*[contains(@class, 'invalid') and contains(text(), '.')]"),
        By.xpath("//*[contains(@class, 'warning') and contains(text(), '.')]"),
        By.xpath("//*[@role='alert']"),
        By.xpath("//*[contains(@class, 'message') and contains(text(), '.')]")
    };
    
    // Cookie/Privacy panel locators - Specific for Ninox's Cookiebot implementation
    private final By cookieAcceptButton = By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll");
    private final By cookieAcceptButtonAlt = By.xpath("//button[contains(text(), 'Allow all') or contains(text(), 'Accept') or contains(text(), 'OK') or contains(text(), 'Agree')]");
    private final By cookieRejectButton = By.xpath("//button[contains(text(), 'Reject') or contains(text(), 'Decline') or contains(@id, 'reject')]");
    private final By cookieCloseButton = By.xpath("//button[contains(@aria-label, 'close') or contains(@class, 'close') or text()='×']");
    private final By cookiePanel = By.xpath("//*[contains(@id, 'CybotCookiebot') or contains(@class, 'cookie') or contains(@class, 'privacy') or contains(@class, 'consent')]");
    
    public SignUpPage(WebDriver driver) {
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
    
    // Helper method to check if element exists using multiple strategies
    private boolean elementExistsWithFallback(By[] selectors, String elementName) {
        for (By selector : selectors) {
            try {
                if (driver.findElements(selector).size() > 0) {
                    logger.debug("Found {} using selector: {}", elementName, selector);
                    return true;
                }
            } catch (Exception e) {
                logger.debug("Selector {} failed for {}: {}", selector, elementName, e.getMessage());
            }
        }
        return false;
    }
    
    public void navigateToSignUpPage(String baseUrl) {
        String signUpUrl = baseUrl.contains("ninox.com") ? 
            baseUrl + "/en/create-account" : 
            baseUrl + "/create-account";
        
        logger.info("Navigating to sign-up page: {}", signUpUrl);
        driver.get(signUpUrl);
        waitForPageToLoad();
    }
    
    public void waitForPageToLoad() {
        // First handle any cookie/privacy panels
        handleCookiePanel();
        
        // Then wait for main form elements using flexible selectors
        findElementWithFallback(emailFieldSelectors, "email field");
        findElementWithFallback(passwordFieldSelectors, "password field");
        logger.info("Sign-up page loaded successfully");
    }
    
    private void handleCookiePanel() {
        try {
            // Wait briefly for cookie panel to appear
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            
            if (driver.findElements(cookiePanel).size() > 0) {
                logger.info("Cookie/privacy panel detected, attempting to dismiss");
                
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
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            WebElement button = shortWait.until(ExpectedConditions.elementToBeClickable(locator));
            button.click();
            logger.info("Successfully clicked: {}", description);
            
            // Wait for panel to disappear by checking if cookie panel is no longer present
            try {
                shortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
                logger.debug("Cookie panel dismissed successfully");
            } catch (Exception e) {
                // Panel might not have been visible or might disappear immediately
                logger.debug("Cookie panel visibility check completed: {}", e.getMessage());
            }
            
            return true;
        } catch (Exception e) {
            logger.debug("Could not click {}: {}", description, e.getMessage());
            return false;
        }
    }
    
    public void enterEmail(String email) {
        logger.info("Entering email: {}", email);
        WebElement emailElement = findElementWithFallback(emailFieldSelectors, "email field");
        wait.until(ExpectedConditions.elementToBeClickable(emailElement));
        emailElement.clear();
        emailElement.sendKeys(email);
    }
    
    public void enterPassword(String password) {
        logger.info("Entering password");
        WebElement passwordElement = findElementWithFallback(passwordFieldSelectors, "password field");
        wait.until(ExpectedConditions.elementToBeClickable(passwordElement));
        passwordElement.clear();
        passwordElement.sendKeys(password);
    }
    
    public void toggleMarketingCheckbox(boolean check) {
        logger.info("Setting marketing checkbox to: {}", check);
        try {
            WebElement checkbox = findElementWithFallback(marketingCheckboxSelectors, "marketing checkbox");
            wait.until(ExpectedConditions.elementToBeClickable(checkbox));
            if (checkbox.isSelected() != check) {
                checkbox.click();
            }
        } catch (Exception e) {
            logger.warn("Marketing checkbox not found or not interactable: {}", e.getMessage());
        }
    }
    
    public void clickCreateAccount() {
        logger.info("Clicking Create Account button");
        WebElement button = findElementWithFallback(createAccountButtonSelectors, "create account button");
        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
    }
    
    public boolean isGoogleButtonPresent() {
        boolean present = elementExistsWithFallback(continueWithGoogleButtonSelectors, "Google OAuth button");
        logger.info("Google button present: {}", present);
        return present;
    }
    
    public void clickGoogleButton() {
        logger.info("Clicking Continue with Google button");
        WebElement button = findElementWithFallback(continueWithGoogleButtonSelectors, "Google OAuth button");
        wait.until(ExpectedConditions.elementToBeClickable(button));
        button.click();
    }
    
    public String getEmailErrorMessage() {
        try {
            // Look for error messages that might be related to email
            for (By selector : errorMessageSelectors) {
                List<WebElement> errors = driver.findElements(selector);
                for (WebElement error : errors) {
                    String text = error.getText().toLowerCase();
                    if (text.contains("email") || text.contains("invalid") || text.contains("format")) {
                        logger.info("Email error message: {}", error.getText());
                        return error.getText();
                    }
                }
            }
            logger.warn("No email-specific error message found");
            return "";
        } catch (Exception e) {
            logger.warn("Error finding email error message: {}", e.getMessage());
            return "";
        }
    }
    
    public String getPasswordErrorMessage() {
        try {
            // Look for error messages that might be related to password
            for (By selector : errorMessageSelectors) {
                List<WebElement> errors = driver.findElements(selector);
                for (WebElement error : errors) {
                    String text = error.getText().toLowerCase();
                    if (text.contains("password") || text.contains("weak") || text.contains("strong")) {
                        logger.info("Password error message: {}", error.getText());
                        return error.getText();
                    }
                }
            }
            logger.warn("No password-specific error message found");
            return "";
        } catch (Exception e) {
            logger.warn("Error finding password error message: {}", e.getMessage());
            return "";
        }
    }
    
    public String getGeneralErrorMessage() {
        try {
            // Get any error message found
            for (By selector : errorMessageSelectors) {
                List<WebElement> errors = driver.findElements(selector);
                if (!errors.isEmpty()) {
                    String errorText = errors.get(0).getText();
                    logger.info("General error message: {}", errorText);
                    return errorText;
                }
            }
            logger.warn("No general error message found");
            return "";
        } catch (Exception e) {
            logger.warn("Error finding general error message: {}", e.getMessage());
            return "";
        }
    }
    
    public boolean isMarketingCheckboxChecked() {
        try {
            WebElement checkbox = findElementWithFallback(marketingCheckboxSelectors, "marketing checkbox");
            boolean checked = checkbox.isSelected();
            logger.info("Marketing checkbox checked: {}", checked);
            return checked;
        } catch (Exception e) {
            logger.warn("Marketing checkbox not found: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isCreateAccountButtonEnabled() {
        try {
            WebElement button = findElementWithFallback(createAccountButtonSelectors, "create account button");
            boolean enabled = button.isEnabled();
            logger.info("Create Account button enabled: {}", enabled);
            return enabled;
        } catch (Exception e) {
            logger.warn("Create Account button not found: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isRedirectedAfterSignUp() {
        try {
            // Wait for URL change or specific success indicator with shorter timeout
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.not(ExpectedConditions.urlContains("create-account")));
            String currentUrl = driver.getCurrentUrl();
            logger.info("Redirected to: {}", currentUrl);
            return !currentUrl.contains("create-account");
        } catch (Exception e) {
            logger.warn("No redirect detected: {}", e.getMessage());
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
}