# Framework Architecture

This document describes the architectural design of the UI Test Automation Framework.

## Overview
The framework is built using **Java 21**, **Selenium WebDriver**, and **TestNG**. It follows a multi-layered approach to ensure scalability, maintainability, and parallel execution safety.

## Layers

### 1. Driver Layer (`common.driver`)
- **WebDriverFactory**: Manages the lifecycle of WebDriver instances.
- **ThreadLocal Storage**: Ensures each thread has its own isolated driver instance, enabling safe parallel execution.
- **Support**: Supports Chrome, Firefox, Edge, and Safari, as well as Remote execution via Selenium Grid.

### 2. Configuration Layer (`common.config`)
- **OWNER Library**: Uses the OWNER library for type-safe configuration.
- **Multi-Environment**: Supports different profiles (QA, DEV) via `.properties` files.
- **Security**: Sensitive data (like passwords) are externalized via environment variables.

### 3. Page Object Model (`common.pageobject`)
- **BasePage**: The foundation for all page objects, providing common interaction methods and wait strategies.
- **Stateless Design**: Page Objects represent the UI state and actions but do not contain assertions (delegated to the test layer).
- **Component Model**: Complex UI elements (like Headers) are extracted into reusable components.
- **Source Set**: Framework and orchestration code lives under `src/main/java`; concrete TestNG scenarios stay under `src/test/java`.

### 4. Test Layer (`tests`)
- **BaseTestCase**: Handles setup (`BeforeMethod`) and teardown (`AfterMethod`) of the driver.
- **UITests**: Implementation of business scenarios.
- **AssertJ**: Used for fluent, descriptive assertions with business-level error messages.

### 5. Reporting Layer (`common.listener`)
- **Allure Reporting**: Integrated via a custom listener to capture screenshots, URL, page source, browser capabilities, console logs, and environment details on failure.
- **Step Annotations**: `@Step` used in Page Objects for detailed action tracking in reports.

## Design Principles
- **Fail-Fast**: Configuration and environment checks happen at startup.
- **Deterministic Waits**: Only explicit waits are used (no `Thread.sleep` or implicit waits).
- **Clean Code**: Code style is enforced via Checkstyle (Google Checks), Spotless, and Maven Enforcer during `verify`.
