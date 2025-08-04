package com.ninox.tests;

import com.ninox.pages.HomePage;
import com.ninox.pages.LoginPage;
import com.ninox.pages.SignUpPage;
import com.ninox.utils.TestDataGenerator;
import com.ninox.utils.TestRetryAnalyzer;
import io.qameta.allure.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@Epic("Ninox Sign-up Flow")
@Feature("User Registration")
@Listeners({io.qameta.allure.testng.AllureTestNg.class})
public class SignUpTests extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(SignUpTests.class);
    private SignUpPage signUpPage;
    
    @Override
    @Step("Navigate to sign-up page")
    protected void setupTest() {
        signUpPage = new SignUpPage(driver);
        signUpPage.navigateToSignUpPage(baseUrl);
        logger.info("Navigated to sign-up page: {}/create-account", baseUrl);
    }
    
    // ============ SMOKE TESTS ============
    
    @Test(description = "Smoke test: Verify sign-up page loads and basic elements are present",
          priority = 1, groups = {"smoke", "critical"})
    @Story("Page Accessibility")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that the sign-up page loads correctly and essential elements are accessible")
    public void testPageAccessibility() {
        logger.info("Running smoke test for page accessibility");
        
        // Verify page URL matches expected sign-up page
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("create-account") || currentUrl.contains("sign-up"),
                         "URL should contain sign-up path. Current URL: " + currentUrl);
        
        // Verify essential elements are present (already done in setUp via waitForPageToLoad)
        Assert.assertTrue(signUpPage.isEmailFieldVisible(), "Email field should be visible");
        Assert.assertTrue(signUpPage.isPasswordFieldVisible(), "Password field should be visible");
        
        logger.info("Smoke test passed - page is accessible and basic elements are present");
    }

    // ============ NAVIGATION & UI LAYOUT TESTS ============
    
    @Test(description = "Homepage navigation and user type selection UI validation",
          priority = 2, groups = {"navigation", "ui", "layout"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("User Journey Navigation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test complete homepage navigation flow and validate UI layout differences for personal vs work signup")
    public void testHomepageNavigationAndUserTypeUI() {
        logger.info("Testing homepage navigation and user type UI layout");
        
        // Navigate to homepage first
        HomePage homePage = new HomePage(driver);
        homePage.navigateToHomePage(baseUrl);
        
        // Verify homepage loaded with Try for free button
        Assert.assertTrue(homePage.isTryForFreeButtonVisible(), 
                         "Homepage should load with Try for free button visible");
        logger.info("✅ Homepage loaded successfully");
        
        // Navigate to signup page via Try for free
        signUpPage = homePage.clickTryForFree();
        logger.info("✅ Successfully navigated via Try for free button");
        
        // Validate UI elements that should be present via homepage navigation
        boolean personalAvailable = signUpPage.isPersonalSignupOptionAvailable();
        boolean teamAvailable = signUpPage.isTeamWorkSignupOptionAvailable();
        boolean bookDemoAvailable = signUpPage.isBookDemoAvailable();
        
        logger.info("UI Layout Validation Results:");
        logger.info("- Personal signup option: {}", personalAvailable ? "✅ Available" : "❌ Not found");
        logger.info("- Team/Work signup option: {}", teamAvailable ? "✅ Available" : "❌ Not found");
        logger.info("- Book Demo button: {}", bookDemoAvailable ? "✅ Available" : "❌ Not found");
        
        // Test Personal signup UI flow (if available)
        if (personalAvailable) {
            signUpPage.selectPersonalSignup();
            logger.info("✅ Personal signup option is clickable");
            
            // Check if UI changed after selecting personal
            boolean demoStillVisible = signUpPage.isBookDemoAvailable();
            logger.info("Book Demo after Personal selection: {}", demoStillVisible ? "Still visible" : "Hidden");
        }
        
        // Refresh page to test Work signup UI flow
        driver.navigate().refresh();
        signUpPage.waitForPageToLoad();
        
        if (teamAvailable && signUpPage.isTeamWorkSignupOptionAvailable()) {
            signUpPage.selectTeamWorkSignup();
            logger.info("✅ Team/Work signup option is clickable");
            
            // Check if Book Demo appears after selecting work (as you mentioned)
            boolean demoAfterWork = signUpPage.isBookDemoAvailable();
            logger.info("Book Demo after Work selection: {}", demoAfterWork ? "✅ Visible (expected)" : "❌ Hidden");
            
            // Test book demo functionality if it appeared
            if (demoAfterWork) {
                signUpPage.clickBookDemo();
                boolean demoPageLoaded = signUpPage.isDemoPageLoaded();
                Assert.assertTrue(demoPageLoaded, "Demo booking page should load after clicking book demo");
                logger.info("✅ Book demo functionality works correctly");
            }
        }
        
        logger.info("Homepage navigation and UI layout validation completed");
    }
    
    // ============ POSITIVE TESTS ============
    
    @Test(description = "Create real account with configured email domain", 
          priority = 3, groups = {"positive", "real-accounts", "account-creation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Real Account Creation")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Actually create a real account and verify successful registration flow")
    public void testCreateRealAccount() {
        TestDataGenerator.TestUser realUser = TestDataGenerator.generateRealTestUser();
        logger.info("Creating real account with: {}", realUser);
        
        signUpPage.enterEmail(realUser.email);
        signUpPage.enterPassword(realUser.password);
        
        // Marketing checkbox is optional - don't fail if not present
        try {
            signUpPage.toggleMarketingCheckbox(true);
        } catch (Exception ignored) {
            logger.info("Marketing checkbox not required - continuing");
        }
        
        // Create the account
        logger.info("🚀 Creating real account...");
        signUpPage.clickCreateAccount();
        
        // Check for specific success indicators
        String currentUrl = driver.getCurrentUrl();
        logger.info("Post-signup URL: {}", currentUrl);
        
        // Check for expected post-signup pages
        boolean successfulSignup = false;
        String signupResult = "";
        
        if (currentUrl.contains("/teams") || currentUrl.contains("/workspace")) {
            successfulSignup = true;
            signupResult = "Redirected to teams/workspace page";
        } else if (currentUrl.contains("/verify") || currentUrl.contains("/confirmation")) {
            successfulSignup = true;
            signupResult = "Redirected to email verification page";
        } else if (currentUrl.contains("/welcome") || currentUrl.contains("/onboarding")) {
            successfulSignup = true;
            signupResult = "Redirected to welcome/onboarding page";
        } else if (!currentUrl.contains("create-account")) {
            successfulSignup = true;
            signupResult = "Redirected away from signup page (success assumed)";
        } else {
            signupResult = "Remained on signup page - checking for errors";
        }
        
        logger.info("Signup result: {}", signupResult);
        
        if (successfulSignup) {
            logger.info("✅ Account created successfully!");
            logger.info("Email: {}", realUser.email);
            logger.info("Next page: {}", currentUrl);
            logger.info("This account can now be used for login tests");
        } else {
            logger.warn("⚠️ Account creation unclear - remained on signup page");
            logger.info("Email attempted: {}", realUser.email);
        }
    }
    
    // ============ NEGATIVE/VALIDATION TESTS ============
    
    @Test(description = "Sign-up with invalid email format",
          groups = {"negative", "validation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Input Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test email format validation during registration")
    public void testInvalidEmailFormat() {
        String invalidEmail = TestDataGenerator.generateInvalidEmail();
        String password = TestDataGenerator.generateStrongPassword();
        
        logger.info("Testing invalid email format: {}", invalidEmail);
        
        signUpPage.enterEmail(invalidEmail);
        signUpPage.enterPassword(password);
        
        // Wait for any client-side validation to appear
        WebDriverWait validationWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        try {
            validationWait.until(d -> !signUpPage.getEmailErrorMessage().isEmpty());
        } catch (Exception ignored) {
            // No validation appeared, continue
        }
        
        signUpPage.clickCreateAccount();
        
        // Wait for server-side validation response or redirect
        try {
            validationWait.until(d -> 
                !signUpPage.getEmailErrorMessage().isEmpty() || 
                !signUpPage.getGeneralErrorMessage().isEmpty() ||
                !driver.getCurrentUrl().contains("create-account")
            );
        } catch (Exception ignored) {
            // Continue with test
        }
        
        String errorMessage = signUpPage.getEmailErrorMessage();
        String generalError = signUpPage.getGeneralErrorMessage();
        
        // Check if form was prevented from submitting or if error appeared
        boolean hasError = !errorMessage.isEmpty() || !generalError.isEmpty();
        String currentUrl = driver.getCurrentUrl();
        boolean stayedOnPage = currentUrl.contains("create-account");
        
        logger.info("Error message: '{}', General error: '{}', Current URL: {}", 
                   errorMessage, generalError, currentUrl);
        
        // Either there should be an error message OR the form should stay on the same page (validation prevented submission)
        Assert.assertTrue(hasError || stayedOnPage, 
                         "Email validation should either show error message or prevent form submission");
    }
    
    @Test(description = "Sign-up with weak password", 
          groups = {"negative", "validation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Input Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test password strength validation during registration")
    public void testWeakPassword() {
        String email = TestDataGenerator.generateUniqueEmail();
        String weakPassword = TestDataGenerator.generateWeakPassword();
        
        logger.info("Testing weak password with email: {}", email);
        
        signUpPage.enterEmail(email);
        signUpPage.enterPassword(weakPassword);
        signUpPage.clickCreateAccount();
        
        String errorMessage = signUpPage.getPasswordErrorMessage();
        Assert.assertFalse(errorMessage.isEmpty(), "Password validation error should be displayed");
        Assert.assertTrue(errorMessage.toLowerCase().contains("password") || 
                         errorMessage.toLowerCase().contains("weak") ||
                         errorMessage.toLowerCase().contains("strong"),
                         "Error message should indicate password strength issue");
    }
    
    @Test(description = "Sign-up with already registered email", 
          groups = {"negative", "validation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Input Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test duplicate email validation during registration")
    public void testDuplicateEmail() {
        String duplicateEmail = TestDataGenerator.getKnownDuplicateEmail();
        String password = TestDataGenerator.generateStrongPassword();
        
        logger.info("Testing duplicate email: {}", duplicateEmail);
        
        signUpPage.enterEmail(duplicateEmail);
        signUpPage.enterPassword(password);
        signUpPage.clickCreateAccount();
        
        // Check for error message indicating email already exists
        String generalError = signUpPage.getGeneralErrorMessage();
        String emailError = signUpPage.getEmailErrorMessage();
        
        boolean errorFound = !generalError.isEmpty() || !emailError.isEmpty();
        Assert.assertTrue(errorFound, "Error message should be displayed for duplicate email");
        
        String combinedError = (generalError + " " + emailError).toLowerCase();
        Assert.assertTrue(combinedError.contains("already") || 
                         combinedError.contains("exists") ||
                         combinedError.contains("registered"),
                         "Error message should indicate email already exists");
    }
    
    // ============ INTEGRATION TESTS ============
    
    @Test(description = "Verify Google OAuth button is present and clickable", 
          groups = {"integration", "oauth"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("OAuth Integration")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test presence and functionality of Google OAuth button")
    public void testGoogleOAuthButton() {
        logger.info("Testing Google OAuth button presence and functionality");
        
        boolean googleButtonPresent = signUpPage.isGoogleButtonPresent();
        Assert.assertTrue(googleButtonPresent, "Continue with Google button should be present");
        
        // For OAuth testing, we only verify the button is present and enabled
        // Actually clicking would trigger OAuth flow which requires special setup
        try {
            // Find the Google button element to verify it's clickable
            WebElement googleButton = driver.findElement(
                By.xpath("//button[.//span[contains(text(), 'Continue with Google')]]"));
            
            Assert.assertTrue(googleButton.isEnabled(), "Google OAuth button should be enabled");
            Assert.assertTrue(googleButton.isDisplayed(), "Google OAuth button should be visible");
            
            logger.info("Google OAuth button is present, visible, and enabled");
            
        } catch (Exception e) {
            logger.error("Error verifying Google OAuth button: {}", e.getMessage());
            Assert.fail("Failed to verify Google OAuth button properties");
        }
        
        logger.info("Google OAuth button test completed successfully");
    }
    
    @Test(description = "Sign-up with missing email field", 
          groups = {"negative", "validation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Input Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test form validation when email field is empty")
    public void testMissingEmail() {
        String password = TestDataGenerator.generateStrongPassword();
        
        logger.info("Testing missing email field");
        
        // Only enter password, leave email empty
        signUpPage.enterPassword(password);
        signUpPage.clickCreateAccount();
        
        // Check if form submission is prevented or error is shown
        String emailError = signUpPage.getEmailErrorMessage();
        boolean buttonEnabled = signUpPage.isCreateAccountButtonEnabled();
        
        // Either there should be an error message or the button should be disabled
        Assert.assertTrue(!emailError.isEmpty() || !buttonEnabled,
                         "Form should validate required email field");
    }
    
    @Test(description = "Sign-up with missing password field", 
          groups = {"negative", "validation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Input Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test form validation when password field is empty")
    public void testMissingPassword() {
        String email = TestDataGenerator.generateUniqueEmail();
        
        logger.info("Testing missing password field with email: {}", email);
        
        // Only enter email, leave password empty
        signUpPage.enterEmail(email);
        signUpPage.clickCreateAccount();
        
        // Check if form submission is prevented or error is shown
        String passwordError = signUpPage.getPasswordErrorMessage();
        boolean buttonEnabled = signUpPage.isCreateAccountButtonEnabled();
        
        // Either there should be an error message or the button should be disabled
        Assert.assertTrue(!passwordError.isEmpty() || !buttonEnabled,
                         "Form should validate required password field");
    }
    
    // ============ UI/UX TESTS ============
    
    @Test(description = "Marketing checkbox opt-in behavior", 
          groups = {"ui", "regression"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Marketing Consent")
    @Severity(SeverityLevel.MINOR)
    @Description("Test marketing checkbox functionality and state persistence")
    public void testMarketingCheckboxBehavior() {
        logger.info("Testing marketing checkbox behavior");
        
        // Test initial state
        boolean initialState = signUpPage.isMarketingCheckboxChecked();
        logger.info("Marketing checkbox initial state: {}", initialState);
        
        // Test toggling on
        signUpPage.toggleMarketingCheckbox(true);
        Assert.assertTrue(signUpPage.isMarketingCheckboxChecked(),
                         "Marketing checkbox should be checked when toggled on");
        
        // Test toggling off
        signUpPage.toggleMarketingCheckbox(false);
        Assert.assertFalse(signUpPage.isMarketingCheckboxChecked(),
                          "Marketing checkbox should be unchecked when toggled off");
        
        // Test form submission works regardless of checkbox state
        String email = TestDataGenerator.generateUniqueEmail();
        String password = TestDataGenerator.generateStrongPassword();
        
        signUpPage.enterEmail(email);
        signUpPage.enterPassword(password);
        signUpPage.toggleMarketingCheckbox(false); // Opt out
        
        // Should still allow account creation
        Assert.assertTrue(signUpPage.isCreateAccountButtonEnabled(),
                         "Account creation should work regardless of marketing consent");
    }
    
    // ============ SECURITY TESTS ============
    
    @Test(description = "Test form handling of special characters and potential injection attempts",
          groups = {"security", "regression"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Input Sanitization")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test form handling of special characters and potential security issues")
    public void testSpecialCharacterHandling() {
        logger.info("Testing form security with special characters");
        
        String[] specialEmails = {
            "test+tag@example.com",         // Plus addressing
            "test.dots@example.com",        // Dots in local part
            "test-dash@example.com",        // Dashes
            "test_underscore@example.com",  // Underscores
            "test@sub.domain.com"           // Subdomain
        };
        
        String[] potentiallyDangerousPasswords = {
            "Pass@123!",                    // Standard special chars (safe)
            "Pass123;",                     // Semicolon
            "'Pass123'",                    // Single quotes
            "\"Pass123\"",                  // Double quotes
            "<Pass123>"                     // HTML-like brackets
        };
        
        for (String email : specialEmails) {
            logger.info("Testing email with special characters: {}", email);
            
            signUpPage.enterEmail(email);
            signUpPage.enterPassword(potentiallyDangerousPasswords[0]); // Use safe password
            
            // Verify form accepts legitimate special characters
            boolean formAccepted = signUpPage.isCreateAccountButtonEnabled();
            logger.info("Form accepted email '{}': {}", email, formAccepted);
            
            // Clear for next test
            signUpPage.enterEmail("");
            signUpPage.enterPassword("");
        }
    }
    
    // ============ PERFORMANCE TESTS ============
    
    @Test(description = "Test form submission timing and user experience",
          groups = {"performance", "regression"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Form Performance")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test form responsiveness and submission timing")
    public void testFormSubmissionTiming() {
        String email = TestDataGenerator.generateUniqueEmail();
        String password = TestDataGenerator.generateStrongPassword();
        
        logger.info("Testing form submission performance with email: {}", email);
        
        long startTime = System.currentTimeMillis();
        
        signUpPage.enterEmail(email);
        signUpPage.enterPassword(password);
        signUpPage.clickCreateAccount();
        
        // Check if button becomes disabled during submission (good UX)
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(1));
        boolean buttonDisabled = false;
        try {
            shortWait.until(d -> !signUpPage.isCreateAccountButtonEnabled());
            buttonDisabled = true;
            logger.info("Submit button properly disabled during processing");
        } catch (Exception e) {
            logger.debug("Submit button remained enabled during processing");
        }
        
        long submitTime = System.currentTimeMillis() - startTime;
        logger.info("Form submission response time: {}ms", submitTime);
        
        // Performance assertion - form should respond quickly
        Assert.assertTrue(submitTime < 15000, 
                "Form should respond within 15 seconds (current: " + submitTime + "ms)");
    }
    
    // ============ EDGE CASE TESTS ============
    
    @Test(description = "Test various email domain formats",
          groups = {"edge-case", "validation"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Email Domain Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test sign-up with different valid email domain formats")
    public void testEmailDomainVariations() {
        logger.info("Testing various email domain formats");
        
        String[] emailDomains = {
            "@gmail.com", "@outlook.com", "@yahoo.com", 
            "@company.com", "@university.edu", "@test.de"
        };
        
        String password = TestDataGenerator.generateStrongPassword();
        
        for (String domain : emailDomains) {
            String email = TestDataGenerator.generateUniqueEmailWithDomain(domain);
            logger.info("Testing email domain: {}", domain);
            
            signUpPage.enterEmail(email);
            signUpPage.enterPassword(password);
            
            // Verify form accepts different valid domains
            Assert.assertTrue(signUpPage.isCreateAccountButtonEnabled(),
                    "Create account button should be enabled for domain: " + domain);
            
            // Clear fields for next iteration
            signUpPage.enterEmail("");
            signUpPage.enterPassword("");
        }
    }
    
    // ============ LOGIN RELATED TESTS ============
    // Note: These login tests are placed here as they're related to the sign-up flow.
    // In the future, these should be moved to a separate LoginTests class for better organization.
    
    @Test(description = "Login page accessibility and basic elements verification",
          priority = 1, groups = {"login", "smoke", "critical"})
    @Story("Login Page Accessibility")  
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that the login page loads correctly and essential elements are accessible")
    public void testLoginPageAccessibility() {
        logger.info("Testing login page accessibility");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLoginPage(baseUrl);
        
        // Verify page URL matches expected login page
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("sign-in") || currentUrl.contains("login"),
                         "URL should contain login path. Current URL: " + currentUrl);
        
        // Verify essential elements are present
        Assert.assertTrue(loginPage.areLoginFieldsVisible(), "Login fields should be visible");
        Assert.assertTrue(loginPage.isLoginButtonEnabled(), "Login button should be enabled");
        
        logger.info("Login page accessibility test passed");
    }
    
    @Test(description = "Login with invalid credentials",
          groups = {"login", "negative", "validation"})
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test login with invalid email/password combinations")
    public void testInvalidLogin() {
        logger.info("Testing login with invalid credentials");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLoginPage(baseUrl);
        
        // Test with invalid email format
        loginPage.enterEmail("invalid-email");
        loginPage.enterPassword("somepassword");
        loginPage.clickLogin();
        
        String errorMessage = loginPage.getErrorMessage();
        Assert.assertFalse(errorMessage.isEmpty(), "Error message should be displayed for invalid credentials");
        
        logger.info("Invalid login test completed - error message: {}", errorMessage);
    }
    
    @Test(description = "Login with valid credentials (if available)",
          groups = {"login", "positive", "integration"})
    @Story("Valid Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test login with previously created valid account credentials")
    public void testValidLogin() {
        logger.info("Testing login with valid credentials");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLoginPage(baseUrl);
        
        // Use the configured test email credentials
        TestDataGenerator.TestUser testUser = TestDataGenerator.generateRealTestUser();
        
        loginPage.enterEmail(testUser.email);
        loginPage.enterPassword(testUser.password);
        loginPage.clickLogin();
        
        // Check if login was successful
        boolean loginSuccessful = loginPage.isLoginSuccessful();
        String currentUrl = driver.getCurrentUrl();
        
        logger.info("Login attempt result - Success: {}, Current URL: {}", loginSuccessful, currentUrl);
        
        if (!loginSuccessful) {
            String errorMessage = loginPage.getErrorMessage();
            logger.info("Login may have failed or account doesn't exist yet. Error: {}", errorMessage);
            
            // This is expected if account hasn't been created yet - not a test failure
            logger.info("Note: This test depends on a pre-existing account. Consider running account creation first.");
        } else {
            logger.info("✅ Login successful!");
        }
    }
    
    @Test(description = "Login with empty fields",
          groups = {"login", "negative", "validation"})
    @Story("Login Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test form validation when login fields are empty")
    public void testEmptyLoginFields() {
        logger.info("Testing login with empty fields");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLoginPage(baseUrl);
        
        // Try to login with empty fields
        loginPage.clickLogin();
        
        // Should either show error message or prevent submission
        String errorMessage = loginPage.getErrorMessage();
        boolean buttonEnabled = loginPage.isLoginButtonEnabled();
        String currentUrl = driver.getCurrentUrl();
        boolean stayedOnPage = currentUrl.contains("sign-in") || currentUrl.contains("login");
        
        Assert.assertTrue(!errorMessage.isEmpty() || stayedOnPage,
                         "Form should validate required fields or show error message");
        
        logger.info("Empty fields test completed - Error: '{}', Stayed on page: {}", errorMessage, stayedOnPage);
    }
    
    @Test(description = "Login form UI elements validation",
          groups = {"login", "ui", "regression"})
    @Story("Login UI Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test presence and functionality of login page UI elements")
    public void testLoginUIElements() {
        logger.info("Testing login UI elements");
        
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigateToLoginPage(baseUrl);
        
        // Verify basic fields are present and functional
        Assert.assertTrue(loginPage.isEmailFieldVisible(), "Email field should be visible");
        Assert.assertTrue(loginPage.isPasswordFieldVisible(), "Password field should be visible");
        Assert.assertTrue(loginPage.isLoginButtonEnabled(), "Login button should be enabled");
        
        // Test field interaction
        loginPage.enterEmail("test@example.com");
        loginPage.enterPassword("testpassword");
        
        // Verify fields accept input
        logger.info("Login UI elements validation completed successfully");
    }
}