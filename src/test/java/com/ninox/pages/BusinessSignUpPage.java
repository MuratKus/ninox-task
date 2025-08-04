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

public class BusinessSignUpPage {
    private static final Logger logger = LoggerFactory.getLogger(BusinessSignUpPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    // Primary locators for business signup form
    private final By[] fullNameFieldSelectors = {
        By.id("fullName"),
        By.xpath("//input[@id='fullName']"),
        By.xpath("//input[@placeholder='Full name' or @placeholder='Name']")
    };
    
    private final By[] companyFieldSelectors = {
        By.id("company"),
        By.xpath("//input[@id='company']"),
        By.xpath("//input[@placeholder='Company' or @placeholder='Company name']")
    };
    
    private final By[] industryFieldSelectors = {
        By.xpath("//fieldset[@id='industry']//input"),
        By.xpath("//input[@placeholder='Select' and preceding-sibling::*[contains(text(), 'Industry')]]"),
        By.xpath("//fieldset[contains(@class, 'industry')]//input"),
        By.xpath("//div[contains(@class, 'industry')]//input[@placeholder='Select']")
    };
    
    private final By[] companySizeFieldSelectors = {
        By.xpath("//fieldset[@id='companySize' or @id='company-size']//input"),
        By.xpath("//input[@placeholder='Select' and preceding-sibling::*[contains(text(), 'size')]]"),
        By.xpath("//fieldset[contains(@class, 'size')]//input"),
        By.xpath("//div[contains(@class, 'company-size')]//input[@placeholder='Select']")
    };
    
    private final By[] countryFieldSelectors = {
        By.xpath("//fieldset[@id='country']//input"),
        By.xpath("//input[@placeholder='Select' and preceding-sibling::*[contains(text(), 'Country')]]"),
        By.xpath("//fieldset[contains(@class, 'country')]//input")
    };
    
    private final By[] telephoneFieldSelectors = {
        By.id("telephone"),
        By.xpath("//input[@id='telephone']"),
        By.xpath("//input[@type='tel']"),
        By.xpath("//input[@placeholder='Phone' or @placeholder='Telephone']")
    };
    
    private final By[] saveProfileButtonSelectors = {
        By.xpath("//button[contains(text(), 'Save profile')]"),
        By.xpath("//button[@type='submit' and contains(text(), 'Save')]"),
        By.xpath("//button[contains(@class, 'submit') or contains(@class, 'save')]")
    };
    
    // Dropdown option selectors
    private final By industryDropdownOptions = By.xpath("//div[contains(@class, 'option') or contains(@role, 'option')]");
    private final By companySizeDropdownOptions = By.xpath("//div[contains(@class, 'option') or contains(@role, 'option')]");
    private final By countryDropdownOptions = By.xpath("//div[contains(@class, 'option') or contains(@role, 'option')]");
    
    public BusinessSignUpPage(WebDriver driver) {
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
    
    public void waitForPageToLoad() {
        logger.info("Waiting for business signup page to load...");
        try {
            // Wait for key form elements to be present
            findElementWithFallback(fullNameFieldSelectors, "full name field");
            findElementWithFallback(companyFieldSelectors, "company field");
            logger.info("Business signup page loaded successfully");
        } catch (Exception e) {
            logger.error("Business signup page failed to load: {}", e.getMessage());
            throw e;
        }
    }
    
    public void enterFullName(String fullName) {
        logger.info("Entering full name: {}", fullName);
        WebElement fullNameElement = findElementWithFallback(fullNameFieldSelectors, "full name field");
        wait.until(ExpectedConditions.elementToBeClickable(fullNameElement));
        fullNameElement.clear();
        fullNameElement.sendKeys(fullName);
    }
    
    public void enterCompanyName(String companyName) {
        logger.info("Entering company name: {}", companyName);
        WebElement companyElement = findElementWithFallback(companyFieldSelectors, "company field");
        wait.until(ExpectedConditions.elementToBeClickable(companyElement));
        companyElement.clear();
        companyElement.sendKeys(companyName);
    }
    
    public void selectIndustry(String industry) {
        logger.info("Selecting industry: {}", industry);
        try {
            WebElement industryField = findElementWithFallback(industryFieldSelectors, "industry field");
            wait.until(ExpectedConditions.elementToBeClickable(industryField));
            industryField.click();
            
            // Wait for dropdown to open and select option
            Thread.sleep(1000);
            List<WebElement> options = driver.findElements(industryDropdownOptions);
            
            for (WebElement option : options) {
                if (option.isDisplayed() && option.getText().toLowerCase().contains(industry.toLowerCase())) {
                    option.click();
                    logger.info("Selected industry option: {}", option.getText());
                    return;
                }
            }
            
            // If exact match not found, select first available option
            if (!options.isEmpty()) {
                options.get(0).click();
                logger.info("Selected first available industry option: {}", options.get(0).getText());
            }
            
        } catch (Exception e) {
            logger.warn("Could not select industry: {}", e.getMessage());
        }
    }
    
    public void selectCompanySize(String size) {
        logger.info("Selecting company size: {}", size);
        try {
            WebElement sizeField = findElementWithFallback(companySizeFieldSelectors, "company size field");
            wait.until(ExpectedConditions.elementToBeClickable(sizeField));
            sizeField.click();
            
            // Wait for dropdown to open and select option
            Thread.sleep(1000);
            List<WebElement> options = driver.findElements(companySizeDropdownOptions);
            
            for (WebElement option : options) {
                if (option.isDisplayed() && option.getText().toLowerCase().contains(size.toLowerCase())) {
                    option.click();
                    logger.info("Selected company size option: {}", option.getText());
                    return;
                }
            }
            
            // If exact match not found, select first available option
            if (!options.isEmpty()) {
                options.get(0).click();
                logger.info("Selected first available company size option: {}", options.get(0).getText());
            }
            
        } catch (Exception e) {
            logger.warn("Could not select company size: {}", e.getMessage());
        }
    }
    
    public void selectCountry(String country) {
        logger.info("Selecting country: {}", country);
        try {
            WebElement countryField = findElementWithFallback(countryFieldSelectors, "country field");
            wait.until(ExpectedConditions.elementToBeClickable(countryField));
            countryField.click();
            
            // Wait for dropdown to open and select option
            Thread.sleep(1000);
            List<WebElement> options = driver.findElements(countryDropdownOptions);
            
            for (WebElement option : options) {
                if (option.isDisplayed() && option.getText().toLowerCase().contains(country.toLowerCase())) {
                    option.click();
                    logger.info("Selected country option: {}", option.getText());
                    return;
                }
            }
            
            // If exact match not found, select first available option
            if (!options.isEmpty()) {
                options.get(0).click();
                logger.info("Selected first available country option: {}", options.get(0).getText());
            }
            
        } catch (Exception e) {
            logger.warn("Could not select country: {}", e.getMessage());
        }
    }
    
    public void enterTelephone(String telephone) {
        logger.info("Entering telephone: {}", telephone);
        try {
            WebElement telephoneElement = findElementWithFallback(telephoneFieldSelectors, "telephone field");
            wait.until(ExpectedConditions.elementToBeClickable(telephoneElement));
            telephoneElement.clear();
            telephoneElement.sendKeys(telephone);
        } catch (Exception e) {
            logger.warn("Could not enter telephone: {}", e.getMessage());
        }
    }
    
    public void clickSaveProfile() {
        logger.info("Clicking Save Profile button");
        WebElement button = findElementWithFallback(saveProfileButtonSelectors, "save profile button");
        
        // Scroll to button to ensure it's visible
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", button);
        
        // Wait for button to be clickable
        wait.until(ExpectedConditions.elementToBeClickable(button));
        
        // Log current URL before clicking
        String urlBeforeClick = driver.getCurrentUrl();
        logger.info("URL before Save Profile click: {}", urlBeforeClick);
        
        // Try multiple click strategies
        boolean clicked = false;
        
        // Strategy 1: Regular click
        try {
            button.click();
            clicked = true;
            logger.info("Save Profile button clicked (regular click)");
        } catch (Exception e) {
            logger.debug("Regular click failed: {}", e.getMessage());
        }
        
        // Strategy 2: JavaScript click if regular click failed
        if (!clicked) {
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                clicked = true;
                logger.info("Save Profile button clicked (JavaScript click)");
            } catch (Exception e) {
                logger.debug("JavaScript click failed: {}", e.getMessage());
            }
        }
        
        if (!clicked) {
            throw new RuntimeException("Could not click Save Profile button");
        }
        
        // Wait and check result
        try {
            Thread.sleep(3000);
            String urlAfterClick = driver.getCurrentUrl();
            logger.info("URL after Save Profile click: {}", urlAfterClick);
            
            if (!urlBeforeClick.equals(urlAfterClick)) {
                logger.info("✅ URL changed - business profile submission detected");
            } else {
                logger.warn("⚠️ URL unchanged after button click");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public boolean isRedirectedAfterSaveProfile() {
        try {
            // Wait for URL change or completion indicator
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            shortWait.until(ExpectedConditions.not(ExpectedConditions.urlContains("business-signup")));
            String currentUrl = driver.getCurrentUrl();
            logger.info("Redirected to: {}", currentUrl);
            return !currentUrl.contains("business-signup");
        } catch (Exception e) {
            logger.warn("No redirect detected: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean isSaveProfileButtonEnabled() {
        try {
            WebElement button = findElementWithFallback(saveProfileButtonSelectors, "save profile button");
            boolean enabled = button.isEnabled() && button.isDisplayed();
            logger.info("Save Profile button enabled and visible: {}", enabled);
            return enabled;
        } catch (Exception e) {
            logger.warn("Save Profile button not found: {}", e.getMessage());
            return false;
        }
    }
}