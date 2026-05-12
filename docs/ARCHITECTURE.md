# Framework Architecture

This document describes the architectural design of the UI Test Automation Framework.

## Overview
The framework is built using **Java 21**, **Selenium WebDriver**, and **TestNG**. It follows a multi-layered approach to ensure scalability, maintainability, and parallel execution safety.

## Test Lifecycle Sequence

```mermaid
sequenceDiagram
	participant CI as GitHub Actions / Local Maven
	participant TN as TestNG
	participant BT as BaseTestCase
	participant WF as WebDriverFactory
	participant PO as Page Objects
	participant RL as Reporting Listener

	CI->>TN: mvnw clean verify
	TN->>BT: @BeforeMethod
	BT->>WF: initThreadLocalDriver()
	WF-->>BT: WebDriver
	TN->>PO: Execute test steps
	PO-->>TN: UI actions + page state
	TN->>RL: Pass / fail event
	RL-->>CI: Allure attachments and metadata
	TN->>BT: @AfterMethod
	BT->>WF: cleanUpDriver()
```

## Configuration Override Flow

```mermaid
graph LR
	Defaults[Default properties] --> BaseConfig[config.properties]
	BaseConfig --> EnvProfile[env-specific properties]
	EnvProfile --> ExternalFile[-Dconfig.file optional override]
	ExternalFile --> Environment[Environment variables]
	Environment --> SystemProps[Maven / system properties]
	SystemProps --> FinalConfig[Validated FrameworkConfig]
```

## Layers

### 1. Driver Layer (`com.example.saucedemo.framework.driver`)
- **WebDriverFactory**: Manages the lifecycle of WebDriver instances.
- **ThreadLocal Storage**: Ensures each thread has its own isolated driver instance, enabling safe parallel execution.
- **Support**: Supports Chrome, Firefox, and Edge locally. Chrome and Firefox are configured for Selenium Grid and CI. Safari is local macOS-only experimental support.

### 2. Configuration Layer (`com.example.saucedemo.framework.config`)
- **Custom Typed Loader**: Uses `FrameworkConfig` plus `ConfigFactory` for typed configuration without depending on an unmaintained external library.
- **Multi-Environment**: Supports different profiles (QA, DEV) via `.properties` files.
- **Security**: Sensitive data (like passwords) are externalized via environment variables.
- **Override Order**: Defaults are loaded first, then `config.properties`, then `${env}.properties`, then environment variables, then Maven/system properties.

### 3. Page Object Model (`com.example.saucedemo.framework.pageobject`)
- **BasePage**: The foundation for all page objects, providing common interaction methods and wait strategies.
- **Stateless Design**: Page Objects represent the UI state and actions but do not contain assertions (delegated to the test layer).
- **Component Model**: Complex UI elements (like Headers) are extracted into reusable components.
- **Source Set**: Framework and orchestration code lives under `src/main/java`; concrete TestNG scenarios stay under `src/test/java`.

### 4. Test Layer (`tests`)
- **BaseTestCase**: Handles setup (`BeforeMethod`) and teardown (`AfterMethod`) of the driver.
- **UITests**: Implementation of business scenarios.
- **AssertJ**: Used for fluent, descriptive assertions with business-level error messages.

### 5. Reporting Layer (`com.example.saucedemo.framework.listener`)
- **Allure Reporting**: Integrated via a custom listener to capture screenshots, URL, page source, browser capabilities, console logs, and environment details on failure.
- **Step Annotations**: `@Step` used in Page Objects for detailed action tracking in reports.

### 6. CI and Quality Gates
- **GitHub Actions**: Runs formatting checks, Checkstyle, PMD, browser-matrix UI execution, artifact upload, and GitHub Pages deployment for Allure on `main`.
- **Dependabot**: Keeps Maven, Docker, and GitHub Actions dependencies on an automated weekly update cadence.

## Design Principles
- **Fail-Fast**: Configuration and environment checks happen at startup.
- **Deterministic Waits**: Only explicit waits are used (no `Thread.sleep` or implicit waits).
- **Clean Code**: Code style and bug-pattern detection are enforced via Checkstyle (Google Checks), Spotless, PMD, and Maven Enforcer during `verify`.

## Docker Grid Troubleshooting
- Ensure Docker Desktop is running before `docker compose up`.
- If Selenium Grid is slow to become healthy, rerun the command after the hub and browser nodes finish starting.
- Use `-Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub` for local grid runs.
- Keep browser names aligned with supported values: `CHROME`, `FIREFOX`, `EDGE`, or `SAFARI`.



