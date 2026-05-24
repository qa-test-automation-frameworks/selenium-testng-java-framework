# Portfolio Review Guide

Use this guide when reviewing the repository as a QA Automation, SDET, or test-framework portfolio project.

## Fast Review Path

1. Start with the [README](../README.md) for the feature summary, quick-start commands, supported browsers, and live Allure report link.
2. Review the [Architecture Overview](ARCHITECTURE.md) to understand the framework layers, driver lifecycle, configuration override flow, and CI design.
3. Open the [Execution Guide](EXECUTION_GUIDE.md) for local, Docker Grid, CI, and cloud-grid execution examples.
4. Read the [Test Writing Guide](TEST_WRITING_GUIDE.md) to see page-object, component, assertion, group, retry, visual, and accessibility conventions.
5. Check the ADRs under [docs/adr](adr) for explicit design tradeoffs.
6. Inspect the GitHub Actions workflows under [`.github/workflows`](../.github/workflows) for quality gates, browser matrix execution, secret-safe pull request behavior, Allure publication, and scheduled governance.

## What This Repository Demonstrates

- Java 21, Selenium 4, TestNG, Maven, AssertJ, Log4j2, and Allure in a cohesive UI automation framework.
- Page Object Model plus component objects for shared UI regions.
- Thread-isolated WebDriver lifecycle for parallel TestNG execution.
- Typed configuration loading with layered classpath, external-file, environment, and Maven property overrides.
- Explicit-only synchronization and fail-fast environment validation.
- Cookie authentication shortcuts for non-login scenarios, with separate UI login coverage.
- Docker Compose Selenium Grid execution for Chrome, Firefox, and Edge.
- CI quality gates with Spotless, Checkstyle, PMD, SpotBugs, Maven Enforcer, browser matrix tests, artifact uploads, Allure Pages deployment, secret scanning, dependency review, and SBOM generation.
- Opt-in accessibility and visual-regression extension points without destabilizing the default functional regression suite.

## Suggested Reviewer Checks

- Confirm the README badges and live Allure report are current.
- Review one functional test class, one page object, and one component class to evaluate naming, readability, and separation of responsibilities.
- Review `BaseTestCase`, `WebDriverFactory`, and `WaitUtils` for lifecycle and synchronization design.
- Review `ConfigFactory`, `ConfigLoader`, and `FrameworkConfig` for configuration behavior.
- Review `DiagnosticsAttacher` and `DiagnosticRedactor` for failure-debugging and secret-safety behavior.
- Review `.github/workflows/ui-tests.yml` for CI orchestration and fork-safe secret handling.

## Scope Boundaries

This repository is intentionally focused on UI automation framework design for Sauce Demo. It does not try to become a general automation platform, a packaged Maven library, or a production monitoring suite. API, mobile, database, and performance frameworks should live in separate portfolio repositories so each project has a clear purpose.

## Maintenance Notes

- Scheduled full UI governance runs quarterly to conserve free GitHub Actions minutes.
- Dependabot runs monthly and groups related Maven, Docker, and GitHub Actions updates.
- Java remains on the LTS baseline used by the framework and CI. Runtime upgrades should be handled as deliberate LTS migrations after validating Selenium, Maven plugins, Docker images, and GitHub Actions.
