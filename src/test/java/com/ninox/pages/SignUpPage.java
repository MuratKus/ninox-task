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
    
    // Primary locators with essential fallbacks
    private final By[] emailFieldSelectors = {
        By.id("email"),
        By.xpath("//input[@type='email']")
    };
    
    private final By[] passwordFieldSelectors = {
        By.id("password"),
        By.xpath("//input[@type='password']")
    };
    
    private final By[] marketingCheckboxSelectors = {
        By.id("idxufwc"),
        By.xpath("//input[@type='checkbox']")
    };
    
    private final By[] createAccountButtonSelectors = {
        By.xpath("//button[@data-testid='create-account']"),
        By.xpath("//button[.//span[contains(text(), 'Create account')]]"),
        By.xpath("//button[contains(text(), 'Create Account')]"),
        By.xpath("//button[@type='submit']")
    };
    
    private final By[] continueWithGoogleButtonSelectors = {
        By.xpath("//button[.//span[contains(text(), 'Continue with Google')]]"),
        By.xpath("//button[contains(text(), 'Continue with Google')]")
    };
    
    private final By[] errorMessageSelectors = {
        By.xpath("//*[@role='alert']"),
        By.xpath("//*[contains(@class, 'error') and contains(text(), '.')]")
    };
    
    // User type selection locators
    private final By[] personalSignupSelectors = {
        By.xpath("//button[contains(text(), 'Personal') or contains(text(), 'personal')]"),
        By.xpath("//*[contains(@data-testid, 'personal') or contains(@id, 'personal')]"),
        By.xpath("//button[contains(@class, 'personal')]"),
        By.xpath("//*[contains(text(), 'For personal use')]"),
        By.xpath("//*[contains(text(), 'Individual')]")  
    };
    
    private final By[] teamWorkSignupSelectors = {
        By.xpath("//button[contains(text(), 'Team') or contains(text(), 'team')]"),
        By.xpath("//button[contains(text(), 'Work') or contains(text(), 'work')]"),
        By.xpath("//button[contains(text(), 'Business') or contains(text(), 'business')]"),
        By.xpath("//*[contains(@data-testid, 'team') or contains(@id, 'team')]"),
        By.xpath("//*[contains(text(), 'For work')]"),
        By.xpath("//*[contains(text(), 'Organization')]")  
    };
    
    private final By[] bookDemoSelectors = {
        By.xpath("//button[contains(text(), 'Book a demo') or contains(text(), 'Book demo')]"),
        By.xpath("//a[contains(text(), 'Book a demo') or contains(text(), 'Book demo')]"),
        By.xpath("//*[contains(@href, 'demo') or contains(@data-testid, 'demo')]"),
        By.xpath("//*[contains(text(), 'Schedule demo')]"),
        By.xpath("//*[contains(text(), 'Request demo')]")
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
            // Wait longer for cookie panel to appear (it might load after page)
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            
            // Check multiple times as panel might appear with delay
            for (int attempt = 0; attempt < 3; attempt++) {
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
                    // Wait for panel to potentially appear with explicit wait
                    WebDriverWait veryShortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                    try {
                        veryShortWait.until(ExpectedConditions.presenceOfElementLocated(cookiePanel));
                    } catch (Exception ignored) {
                        // Panel didn't appear, continue
                    }
                }
            }
            
            // Final check if panel is still there
            if (!driver.findElements(cookiePanel).isEmpty()) {
                logger.warn("Cookie panel still present, trying last resort methods");
                
                // Last resort: try pressing Escape key
                try {
                    driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                    WebDriverWait veryShortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                    veryShortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
                    logger.info("Successfully dismissed cookie panel with Escape key");
                } catch (Exception e) {
                    logger.warn("Escape key method failed: {}", e.getMessage());
                }
                
                // Try clicking outside the panel
                try {
                    driver.findElement(By.tagName("body")).click();
                    WebDriverWait veryShortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                    veryShortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
                    logger.info("Successfully dismissed cookie panel by clicking body");
                } catch (Exception e) {
                    logger.warn("Body click method failed: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.debug("Error handling cookie panel: {}", e.getMessage());
        }
    }
    
    private void dismissPasswordVerificationPane() {
        try {
            // Look for password verification overlay/pane that might block the button
            By[] passwordPaneSelectors = {
                By.xpath("//*[contains(@class, 'password') and contains(@class, 'verification')]"),
                By.xpath("//*[contains(@class, 'password') and contains(@class, 'strength')]"),
                By.xpath("//*[contains(@class, 'overlay') or contains(@class, 'modal')]"),
                By.xpath("//*[@role='dialog' or @role='tooltip']"),
                By.xpath("//*[contains(@class, 'popover') or contains(@class, 'dropdown')]"),
                By.xpath("//*[contains(@class, 'tooltip') or contains(@class, 'hint')]"),
                By.xpath("//*[contains(@style, 'z-index') and contains(@style, 'position')]")
            };
            
            for (By selector : passwordPaneSelectors) {
                List<WebElement> panes = driver.findElements(selector);
                for (WebElement pane : panes) {
                    if (pane.isDisplayed()) {
                        logger.info("Found password verification pane, attempting to dismiss");
                        
                        // Try clicking on a safe empty area to dismiss
                        try {
                            // Click on page margin area (top-left corner, away from any elements)
                            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                                "document.elementFromPoint(50, 50).click();"
                            );
                            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                            shortWait.until(ExpectedConditions.invisibilityOf(pane));
                            logger.info("Password verification pane dismissed by clicking safe area");
                            return;
                        } catch (Exception e) {
                            logger.debug("Could not dismiss pane by clicking safe area: {}", e.getMessage());
                        }
                        
                        // Try clicking on empty space near the form
                        try {
                            // Click at coordinates outside the form but within viewport
                            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("document.elementFromPoint(100, 100).click();");
                            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                            shortWait.until(ExpectedConditions.invisibilityOf(pane));
                            logger.info("Password verification pane dismissed by clicking elsewhere");
                            return;
                        } catch (Exception e) {
                            logger.debug("Could not dismiss pane by clicking elsewhere: {}", e.getMessage());
                        }
                        
                        // Try pressing Escape
                        try {
                            driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
                            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
                            shortWait.until(ExpectedConditions.invisibilityOf(pane));
                            logger.info("Password verification pane dismissed with Escape");
                            return;
                        } catch (Exception e) {
                            logger.debug("Could not dismiss pane with Escape: {}", e.getMessage());
                        }
                    }
                }
            }
            
            logger.debug("No password verification pane found or already dismissed");
            
        } catch (Exception e) {
            logger.debug("Error handling password verification pane: {}", e.getMessage());
        }
    }
    
    private void waitForCookiePanelToDisappear() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(cookiePanel));
            logger.info("Cookie panel successfully dismissed");
        } catch (Exception e) {
            logger.debug("Cookie panel might still be visible: {}", e.getMessage());
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
        
        // First, dismiss any password verification pane that might be blocking the button
        dismissPasswordVerificationPane();
        
        WebElement button = findElementWithFallback(createAccountButtonSelectors, "create account button");
        
        // Log button state before clicking
        logger.info("Button enabled: {}, displayed: {}, text: '{}'", 
                   button.isEnabled(), button.isDisplayed(), button.getText());
        
        // Scroll to button to ensure it's visible
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
        
        // Wait for button to be clickable after scroll
        wait.until(ExpectedConditions.elementToBeClickable(button));
        
        // Log current URL before clicking
        String urlBeforeClick = driver.getCurrentUrl();
        logger.info("URL before button click: {}", urlBeforeClick);
        
        // Try multiple click strategies
        boolean clicked = false;
        
        // Strategy 1: Regular click
        try {
            button.click();
            clicked = true;
            logger.info("Create Account button clicked (regular click)");
        } catch (Exception e) {
            logger.debug("Regular click failed: {}", e.getMessage());
        }
        
        // Strategy 2: JavaScript click if regular click failed
        if (!clicked) {
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                clicked = true;
                logger.info("Create Account button clicked (JavaScript click)");
            } catch (Exception e) {
                logger.debug("JavaScript click failed: {}", e.getMessage());
            }
        }
        
        // Strategy 3: Actions click if others failed
        if (!clicked) {
            try {
                new org.openqa.selenium.interactions.Actions(driver)
                    .moveToElement(button)
                    .click()
                    .perform();
                logger.info("Create Account button clicked (Actions click)");
            } catch (Exception e) {
                logger.error("All click strategies failed: {}", e.getMessage());
                throw new RuntimeException("Could not click Create Account button", e);
            }
        }
        
        // Wait a moment and check if URL changed or if we're still on the same page
        try {
            Thread.sleep(3000); // Give time for any submission to process
            String urlAfterClick = driver.getCurrentUrl();
            logger.info("URL after button click: {}", urlAfterClick);
            
            if (urlBeforeClick.equals(urlAfterClick)) {
                logger.warn("⚠️ URL unchanged after button click - investigating form state");
                
                // Check for validation errors with more thorough search
                String emailError = getEmailErrorMessage();
                String passwordError = getPasswordErrorMessage();
                String generalError = getGeneralErrorMessage();
                
                // Check console for JavaScript errors
                try {
                    List<org.openqa.selenium.logging.LogEntry> logs = driver.manage().logs().get("browser").getAll();
                    for (org.openqa.selenium.logging.LogEntry log : logs) {
                        if (log.getLevel().toString().equals("SEVERE")) {
                            logger.warn("Browser console error: {}", log.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Could not retrieve console logs: {}", e.getMessage());
                }
                
                // Check if form fields are still filled (to see if form was reset)
                try {
                    WebElement emailField = findElementWithFallback(emailFieldSelectors, "email field");
                    WebElement passwordField = findElementWithFallback(passwordFieldSelectors, "password field");
                    
                    String currentEmailValue = emailField.getAttribute("value");
                    String currentPasswordValue = passwordField.getAttribute("value");
                    
                    logger.info("Email field value after click: '{}'", currentEmailValue);
                    logger.info("Password field value after click: '{}'", currentPasswordValue.isEmpty() ? "empty" : "filled");
                    
                    if (currentEmailValue.isEmpty() && currentPasswordValue.isEmpty()) {
                        logger.info("Form fields were cleared - may indicate submission attempt");
                    }
                } catch (Exception e) {
                    logger.debug("Could not check form field values: {}", e.getMessage());
                }
                
                // Log any found errors
                if (!emailError.isEmpty()) logger.warn("Email error: {}", emailError);
                if (!passwordError.isEmpty()) logger.warn("Password error: {}", passwordError);
                if (!generalError.isEmpty()) logger.warn("General error: {}", generalError);
                
                // Check if button became disabled (indicating processing)
                try {
                    WebElement buttonAfterClick = findElementWithFallback(createAccountButtonSelectors, "create account button");
                    logger.info("Button enabled after click: {}", buttonAfterClick.isEnabled());
                } catch (Exception e) {
                    logger.debug("Could not check button state after click: {}", e.getMessage());
                }
                
            } else {
                logger.info("✅ URL changed - form submission detected");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
            boolean enabled = button.isEnabled() && button.isDisplayed();
            logger.info("Create Account button enabled and visible: {}", enabled);
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
    
    // User type selection methods
    public void selectPersonalSignup() {
        logger.info("Selecting Personal signup option");
        try {
            WebElement personalButton = findElementWithFallback(personalSignupSelectors, "personal signup button");
            wait.until(ExpectedConditions.elementToBeClickable(personalButton));
            personalButton.click();
            logger.info("Personal signup selected");
        } catch (Exception e) {
            logger.warn("Personal signup option not found: {}", e.getMessage());
        }
    }
    
    public void selectTeamWorkSignup() {
        logger.info("Selecting Team/Work signup option");
        try {
            WebElement teamButton = findElementWithFallback(teamWorkSignupSelectors, "team/work signup button");
            wait.until(ExpectedConditions.elementToBeClickable(teamButton));
            teamButton.click();
            logger.info("Team/Work signup selected");
        } catch (Exception e) {
            logger.warn("Team/Work signup option not found: {}", e.getMessage());
        }
    }
    
    public boolean isPersonalSignupOptionAvailable() {
        return elementExistsWithFallback(personalSignupSelectors, "personal signup option");
    }
    
    public boolean isTeamWorkSignupOptionAvailable() {
        return elementExistsWithFallback(teamWorkSignupSelectors, "team/work signup option");
    }
    
    public void clickBookDemo() {
        logger.info("Clicking Book Demo button");
        try {
            WebElement demoButton = findElementWithFallback(bookDemoSelectors, "book demo button");
            wait.until(ExpectedConditions.elementToBeClickable(demoButton));
            demoButton.click();
            logger.info("Book Demo button clicked");
        } catch (Exception e) {
            logger.warn("Book Demo button not found: {}", e.getMessage());
            throw new RuntimeException("Could not find Book Demo button", e);
        }
    }
    
    public boolean isBookDemoAvailable() {
        return elementExistsWithFallback(bookDemoSelectors, "book demo button");
    }
    
    public boolean isDemoPageLoaded() {
        try {
            // Wait for URL to contain demo-related keywords or specific demo page elements
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            // Check URL for demo indicators
            String currentUrl = driver.getCurrentUrl().toLowerCase();
            if (currentUrl.contains("demo") || currentUrl.contains("schedule") || currentUrl.contains("meeting")) {
                logger.info("Demo page loaded - URL contains demo keywords: {}", currentUrl);
                return true;
            }
            
            // Check for demo page elements
            By[] demoPageSelectors = {
                By.xpath("//*[contains(text(), 'Schedule') and contains(text(), 'demo')]"),
                By.xpath("//*[contains(text(), 'Book') and contains(text(), 'meeting')]"),
                By.xpath("//*[contains(@class, 'calendar') or contains(@class, 'scheduler')]"),
                By.xpath("//*[contains(text(), 'Choose a time')]"),
                By.xpath("//*[contains(text(), 'Select date')]"),
                By.xpath("//*[contains(@class, 'calendly') or contains(@class, 'hubspot')]") // Common demo booking tools
            };
            
            for (By selector : demoPageSelectors) {
                try {
                    shortWait.until(ExpectedConditions.presenceOfElementLocated(selector));
                    logger.info("Demo page loaded - found demo element");
                    return true;
                } catch (Exception ignored) {
                    // Continue checking other selectors
                }
            }
            
            logger.warn("Demo page not detected");
            return false;
            
        } catch (Exception e) {
            logger.warn("Error checking if demo page loaded: {}", e.getMessage());
            return false;
        }
    }
}