# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Dedicated `CheckoutTests` coverage with a data-driven invalid-checkout `@DataProvider`.
- Postal-code-required checkout validation coverage.
- `CHANGELOG.md` and README links to the live Allure report and project history.
- ADR 005 documenting why this UI automation repository keeps only narrow fast checks for pure framework logic.
- Valid UI login, protected-route negative coverage, and a full end-to-end shopping journey.
- Checkout overview assertions for selected product, item total, tax, and final total.
- Debugging guide with real failed Allure report screenshots and artifact triage guidance.

### Changed
- Split checkout scenarios out of `CartTests` to keep cart and checkout responsibilities separate.
- Added missing Allure `@Step` annotations across shared page and component methods.
- Logged a safe resolved configuration summary at suite startup.
- Aligned `testng.xml` thread-count documentation with runtime configuration behavior.
- Replaced Selenium umbrella dependency usage with explicit Selenium driver/support modules.
- Expanded PMD rules to cover additional low-noise maintainability and error-prone checks.
- Updated the GitHub Actions browser matrix to include Edge.
- Clarified `LoginPage.clickLoginButton()` to return `void` instead of a stale page reference.
- Strengthened product catalog validation to compare full expected product details.
- Masked password-like fields before attaching failure screenshots.
- Split Allure Java adapter and report generator versions.
- Preserved scheduled dependency update reports as CI artifacts.

### Fixed
- Expanded diagnostic redaction checks for HTML, cookies, tokens, and configured credentials.
- Standardized unsupported-browser failures on `FrameworkConfigurationException`.
- Failed fast for missing non-default environment profiles unless an external config file is supplied.
- Removed static Docker Compose container names to avoid local and CI naming collisions.

## [1.0.0] - 2026-05-12

### Added
- Java 21 + Maven Wrapper UI automation framework for Sauce Demo.
- Selenium 4, TestNG, AssertJ, Allure, Log4j2/SLF4J, Docker Grid, and GitHub Actions support.
- Typed configuration with layered overrides from defaults, profiles, environment variables, and system properties.
- Thread-local WebDriver lifecycle management for parallel execution.
- Page Object Model with reusable page components and explicit-wait-only synchronization.
- Cookie-based authentication shortcut for non-login scenarios.
- Failure diagnostics with screenshots, page source, capabilities, logs, and redaction.
- Documentation set including architecture, execution guide, test-writing guide, ADRs, contributing guide, and GitHub templates.

### Changed
- Evolved from the initial bootstrap into a layered `src/main` framework + `src/test` test-suite structure.
- Introduced dedicated inventory, cart, and login test classes instead of a single monolithic suite.
- Added browser-specific options handling, retry infrastructure, Allure environment output, and CI publication flow.
- Strengthened repository hygiene with Checkstyle, PMD, Dependabot, issue templates, and sample report assets.

### Historical Notes
- **2026-05-02** - Bootstrapped the framework, then modernized the package layout, documentation, CI workflow, and page/component model.
- **2026-05-11** - Refined configuration handling, added ADRs, introduced auth utilities, and split the test suite into focused classes.
- **2026-05-12** - Migrated to the `io.github.prayag.saucedemo` namespace, added checkout flow pages, helper scripts, reporting assets, and portfolio polish.

