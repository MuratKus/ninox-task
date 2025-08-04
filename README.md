# Ninox E2E Test Automation

End-to-end test automation for Ninox sign-up flow using Selenium WebDriver and TestNG.

## 🎯 What This Project Tests

This project provides comprehensive automated testing for the Ninox user registration flow, covering:

- **Valid sign-up scenarios** with unique emails and strong passwords
- **Input validation** for email format and password strength
- **Error handling** for duplicate emails and missing required fields
- **OAuth integration** testing for Google sign-in button
- **UI behavior** testing for marketing consent checkbox
- **Cross-browser compatibility** (Chrome, Firefox)
- **Flaky test mitigation** with automatic retry mechanisms

## 🏗️ Technology Stack

- **Java 17+**: Programming language
- **TestNG**: Test framework with parallel execution support
- **Selenium WebDriver**: Browser automation
- **WebDriverManager**: Automatic driver management
- **Allure**: Advanced test reporting with screenshots
- **Maven**: Build tool and dependency management
- **SLF4J**: Logging framework

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Chrome or Firefox browser

### Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/MuratKus/ninox-task.git
   cd ninox-task
   ```

2. **Install dependencies:**
   ```bash
   mvn clean install
   ```

3. **Run tests:**
   ```bash
   # Run all tests against staging environment
   mvn test -P staging
   
   # Run tests against production environment
   mvn test -P production
   
   # Run tests in headless mode (for CI)
   mvn test -P ci
   ```

## 🎛️ Configuration Options

### Real Account Creation Configuration

**Important:** This framework supports creating real accounts using configurable email domains.

```bash
# Configure your email domain for real account creation
mvn test -Dtest.email.domain=murat-kus.com       # Your email domain
mvn test -Dtest.email.prefix=me                  # Email prefix (creates me+123@murat-kus.com)

# Run tests that create real accounts
mvn test -Dgroups=real-accounts -Dtest.email.domain=murat-kus.com

# Example: Create real test accounts
mvn test -Dtest=SignUpTests#testRealAccountCreationReadiness \
  -Dtest.email.domain=murat-kus.com \
  -Dtest.email.prefix=me
```

**Email Pattern Examples:**
- `me+1734567890@murat-kus.com` (basic real account)
- `me+personal+1734567890@murat-kus.com` (personal flow)
- `me+work+1734567890@murat-kus.com` (work/team flow)

### Environment Variables & CI/CD Parameters

You can customize test execution using system properties. Perfect for CI/CD pipelines:

```bash
# Basic Configuration
mvn test -Dbase.url=https://custom.ninox.com     # Custom URL
mvn test -Dbrowser=firefox                       # Browser choice
mvn test -Dheadless=true                         # Headless mode
mvn test -Dtimeout=15                            # Wait timeout (seconds)

# Real Account Configuration
mvn test -Dtest.email.domain=your-domain.com     # Your email domain
mvn test -Dtest.email.prefix=testuser             # Email prefix

# Environment-based URLs (automatic URL selection)
mvn test -Denvironment=staging                   # Uses staging URL
mvn test -Denvironment=production                # Uses production URL

# Retry Configuration
mvn test -Dretry.enabled=false                   # Disable retries
mvn test -Dmax.retries=3                         # Custom retry count

# Test Group Selection
mvn test -Dgroups=smoke                          # Run smoke tests only
mvn test -Dgroups=real-accounts                  # Run real account creation tests
mvn test -Dgroups=navigation                     # Run navigation-dependent tests

# CI/CD Example - Complete parameterization
mvn test \
  -Denvironment=staging \
  -Dbrowser=chrome \
  -Dheadless=true \
  -Dtimeout=20 \
  -Dretry.enabled=true \
  -Dmax.retries=2 \
  -Dtest.email.domain=murat-kus.com \
  -Dtest.email.prefix=ci
```

### Maven Profiles

**Staging Environment (Default):**
```bash
mvn test -P staging
```
- Base URL: `https://q-www.ninox.com`
- Browser: Chrome
- Headless: false

**Production Environment:**
```bash
mvn test -P production
```
- Base URL: `https://ninox.com`
- Browser: Chrome
- Headless: false

**CI Environment:**
```bash
mvn test -P ci
```
- Base URL: `https://q-www.ninox.com`
- Browser: Chrome
- Headless: true

## 📊 Test Reports

### Allure Reports

Generate and view detailed test reports with screenshots:

```bash
# Generate Allure report
mvn allure:report

# Serve report locally
mvn allure:serve
```

Reports include:
- Test execution timeline
- Screenshots on failure
- Browser console logs
- Test step details
- Retry information

### TestNG Reports

Basic HTML reports are generated automatically:
```
target/surefire-reports/index.html
```

## 🧪 Test Scenarios

### Core Test Coverage

| Test Case | Description | Groups | Priority |
|-----------|-------------|---------|----------|
| `testPageAccessibility` | Basic page load and element visibility | smoke, critical | Critical |
| `testRealPersonalSignUpFlow` | Personal signup with real email domain | positive, personal, real-accounts | Critical |
| `testRealTeamWorkSignUpFlow` | Team/work signup with real email domain | positive, team, real-accounts | Critical |
| `testRealAccountCreationReadiness` | End-to-end real account creation readiness | positive, real-accounts | Critical |
| `testBookDemoVisibilityFromHomepage` | Book demo navigation from homepage | integration, demo, navigation | Normal |
| `testInvalidEmailFormat` | Email format validation | negative, validation | Normal |
| `testWeakPassword` | Password strength validation | negative, validation | Normal |
| `testGoogleOAuthButton` | Google OAuth button functionality | integration, oauth | Normal |

### Test Strategy Notes

**🏠 Navigation-Dependent Features:**
- **Personal/Team signup options** only appear when navigating from homepage → "Try for free"
- **Book Demo button** visibility requires homepage navigation flow
- **Direct URL access** (`/create-account`) shows basic signup form without user type selection

**📧 Real Account Creation:**
- Tests generate real email addresses using configurable domains
- Emails follow pattern: `prefix+timestamp@domain.com`
- **Account creation is controlled via TODO comments** - uncomment to enable actual account creation
- Generated accounts can be used for subsequent login/post-signup testing

**🔄 Test Organization:**
- **Groups**: `smoke`, `real-accounts`, `navigation`, `positive`, `negative`, `integration`, `login`
- **Priority-based execution**: Critical tests run first
- **Strategic coverage**: Focus on core user journeys, not every edge case

**🔐 Login Testing:**
- Login tests are currently included in `SignUpTests.java` as they're related to the sign-up flow
- **Future improvement**: These should be moved to a separate `LoginTests.java` class for better organization
- Login tests include validation, accessibility, and integration scenarios

**🏗️ Architecture Improvements:**
- **API Testing Recommendation**: For higher confidence and better coverage, API-level testing should be implemented alongside UI tests
- **BasePage Pattern**: Consider implementing a BasePage class to reduce code duplication between SignUpPage and LoginPage
- **Test Data Management**: Current UUID-based approach works well, but consider external test data management for complex scenarios

## 🔧 Development & Debugging

### Running Specific Tests

```bash
# Run single test class
mvn test -Dtest=SignUpTests

# Run specific test method
mvn test -Dtest=SignUpTests#testValidSignUp

# Run tests by group/category (if using TestNG groups)
mvn test -Dgroups=smoke
```

### Debug Mode

For debugging test failures:

```bash
# Run with verbose logging
mvn test -Dheadless=false -Dbrowser=chrome

# Run single test with debug
mvn test -Dtest=SignUpTests#testValidSignUp -Dheadless=false
```

## 🏛️ Project Structure

```
ninox-e2e-automation/
├── src/test/java/com/ninox/
│   ├── tests/
│   │   ├── BaseTest.java             # Base test class with common setup/teardown
│   │   ├── SignUpTests.java          # Sign-up flow tests
│   │   └── LoginTests.java           # Example: Additional test class
│   ├── pages/
│   │   └── SignUpPage.java           # Page Object Model
│   └── utils/
│       ├── BrowserManager.java       # Browser/driver management
│       ├── ConfigManager.java        # Configuration management
│       ├── TestRetryAnalyzer.java    # Retry logic
│       └── TestDataGenerator.java    # Test data utilities
├── src/test/resources/
│   └── testng.xml                    # TestNG suite configuration
├── docs/
│   └── strategy.md                   # QA strategy documentation
├── pom.xml                           # Maven configuration
└── README.md                         # This file
```

## 🎯 QA Strategy Highlights

- **BaseTest Architecture**: Common setup/teardown with inheritance for easy test creation
- **Page Object Model**: Maintainable test structure with separated concerns
- **Data-Driven Testing**: UUID-based unique test data generation
- **Retry Mechanism**: Automatic retry for flaky tests (max 2 retries)
- **Cross-Browser Support**: Chrome and Firefox compatibility
- **CI/CD Ready**: Headless execution with comprehensive reporting
- **Automatic Artifacts**: Screenshots, console logs, and page source on failures
- **Comprehensive Logging**: Detailed logging with SLF4J for debugging
- **Cookie Panel Handling**: Automatic dismissal of privacy/cookie panels

## 🏗️ Architecture Benefits

### **BaseTest Pattern**
```java
public class MyNewTests extends BaseTest {
    @Override
    protected void setupTest() {
        // Custom page navigation
        driver.get(baseUrl + "/my-page");
    }
    
    @Test
    public void myTest() {
        // Test logic - setup/teardown handled automatically
        // Screenshots, logs captured on failure automatically
    }
}
```

**Benefits:**
- **DRY Principle**: No duplicate setup/teardown code
- **Consistent Artifacts**: All tests get screenshots/logs on failure
- **Easy Extension**: New test classes only need business logic
- **Centralized Configuration**: Browser management in one place

## 🔍 Troubleshooting

### Common Issues

**WebDriver Not Found:**
```bash
# WebDriverManager handles this automatically, but if issues persist:
mvn clean install -U
```

**Tests Failing Due to Timeouts:**
```bash
# Increase wait times or run in non-headless mode
mvn test -Dheadless=false
```

**Element Not Found Errors:**
- Check if page structure has changed
- Verify locators in `SignUpPage.java`
- Run tests in non-headless mode to see browser behavior

### Debugging Steps

1. **Run single test in visible browser:**
   ```bash
   mvn test -Dtest=SignUpTests#testValidSignUp -Dheadless=false
   ```

2. **Check Allure report for screenshots and logs:**
   ```bash
   mvn allure:serve
   ```

3. **Review console output for detailed error messages**

4. **Verify target environment is accessible:**
   ```bash
   curl -I https://q-www.ninox.com/create-account
   ```

## 📈 CI/CD Integration

For Jenkins, GitHub Actions, or other CI systems:

```bash
# Headless execution with XML output
mvn clean test -P ci -Dmaven.test.failure.ignore=true

# Generate reports
mvn allure:report
```

The project is configured to continue execution even if some tests fail, ensuring complete test suite execution and report generation.

## 🤝 Contributing

1. Follow existing code patterns and conventions
2. Add appropriate logging and error handling
3. Update tests when page structure changes
4. Maintain Page Object Model separation
5. Include Allure annotations for new tests

## 📄 License

This project is for educational and testing purposes.