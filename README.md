
# Ninox Sign-Up QA Strategy & Implementation

## Strategic Overview

Sign-up flows are critical for any SaaS business as a broken registration process kills conversion rates. That's why this project focuses on building robust E2E UI testing as part of a comprehensive quality strategy.

**The complete testing approach should include:**

- **API Tests (70%)**: Direct endpoint testing, database validation, OAuth integration - fastest feedback
- **Integration Tests (20%)**: Cross-service communication, data consistency, email delivery  
- **E2E UI Tests (10%)**: Critical user journeys, browser compatibility - what users actually experience

**This implementation**: Delivers a solid foundation for the E2E layer with performance-optimized architecture and smart flaky test mitigation.

## Architecture Decisions

**Why TestNG**: Groups and parallel execution out of the box. Easy to organize tests by business priority (`smoke`, `critical`, `regression`) rather than just throwing everything in one bucket.

**Page Object Model with BasePage**: Common functionality lives in BasePage, specific page logic stays separated, no copy-pasting WebDriverWait code everywhere.

**Framework Adaptability**: 
- **Test Framework**: TestNG now, but easily adaptable to JUnit, pytest, NUnit
- **Reporting**: Allure currently, but designed to plug into whatever your org uses  
- **CI/CD**: Works with Jenkins, GitHub Actions, Azure DevOps - just change the Maven commands


## Installation & Quick Start

**Prerequisites**: Java 17+, Maven 3.6+, Chrome/Firefox

```bash
# Get started in 30 seconds
git clone https://github.com/MuratKus/ninox-task.git
cd ninox-task && mvn clean install

# Run what matters most
mvn test -Dgroups=smoke          # Critical path validation (2 minutes)
mvn test -Dgroups=login          # Authentication flows
mvn test -Dgroups=negative       # Input validation testing
mvn test                         # Full regression suite

# Production-ready execution
mvn test -Dheadless=true -Dbrowser=chrome -Dbase.url=https://ninox.com/en/create-account
```  

## Test Coverage & Capabilities

**What is tested?:**
- Sign-up form validation (email format, password strength, required fields)
- OAuth integration (Google sign-in button presence and functionality)
- Cross-browser compatibility (Chrome, Firefox)
- Error handling (duplicate emails, missing fields, weak passwords)
- Real account creation workflows (configurable email domains)

**Extra Capabilities:**
- **Real Account Testing**: `mvn test -Dtest.email.domain=yourcompany.com` creates actual accounts like `me+1734567890@yourcompany.com` 
- **Flaky Test Resistant**: `wait.until(ExpectedConditions.elementToBeClickable())` with 3-second timeouts instead of blind retries

**Test Organization by Business Priority:**
```bash
mvn test -Dgroups=smoke          # Business-critical paths first
mvn test -Dgroups=critical       # Core functionality  
mvn test -Dgroups=regression     # Full coverage
```

## Summary

This implementation demonstrates a strategic approach to E2E testing with performance-optimized architecture, smart flaky test mitigation, and business-priority test organization - providing a solid foundation that can scale with organizational needs.