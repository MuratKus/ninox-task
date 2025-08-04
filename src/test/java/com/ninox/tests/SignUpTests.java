package com.ninox.tests;

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
    
    @Test(description = "Smoke test: Verify sign-up page loads and basic elements are present",
          priority = 1)
    @Story("Page Accessibility")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify that the sign-up page loads correctly and essential elements are accessible")
    public void testPageAccessibility() {
        logger.info("Running smoke test for page accessibility");
        
        // Verify page title or URL contains expected content
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("create-account") || currentUrl.contains("signup") || currentUrl.contains("sign-up"),
                         "URL should contain sign-up related path. Current URL: " + currentUrl);
        
        // Verify essential elements are present (already done in setUp via waitForPageToLoad)
        Assert.assertTrue(signUpPage.isEmailFieldVisible(), "Email field should be visible");
        Assert.assertTrue(signUpPage.isPasswordFieldVisible(), "Password field should be visible");
        
        logger.info("Smoke test passed - page is accessible and basic elements are present");
    }

    @Test(description = "Valid sign-up with unique email and strong password", 
          dependsOnMethods = {"testPageAccessibility"},
          retryAnalyzer = TestRetryAnalyzer.class)
    @Story("Valid User Registration")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test successful user registration with valid email and strong password")
    public void testValidSignUp() {
        String email = TestDataGenerator.generateUniqueEmail();
        String password = TestDataGenerator.generateStrongPassword();
        
        logger.info("Testing valid sign-up with email: {}", email);
        
        signUpPage.enterEmail(email);
        signUpPage.enterPassword(password);
        signUpPage.toggleMarketingCheckbox(true);
        signUpPage.clickCreateAccount();
        
        // Note: This may need adjustment based on actual behavior
        // The page might redirect to email verification or dashboard
        boolean redirected = signUpPage.isRedirectedAfterSignUp();
        Assert.assertTrue(redirected, "User should be redirected after successful sign-up");
    }
    
    @Test(description = "Sign-up with invalid email format", 
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
        
        // Wait a moment for any client-side validation to appear
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        signUpPage.clickCreateAccount();
        
        // Wait a moment for server-side validation response
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
    
    @Test(description = "Verify Google OAuth button is present and clickable", 
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
    
    @Test(description = "Marketing checkbox opt-in behavior", 
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
    
}