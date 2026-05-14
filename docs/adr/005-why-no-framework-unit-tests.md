# ADR 005: Why No Framework Unit Tests

## Status
Accepted

## Context
This repository is intentionally a UI test automation framework. In the SDLC, it acts as the test layer that validates user-facing application behavior through browser automation. The framework code exists to support those UI tests with configuration, driver lifecycle, waits, page objects, reporting, diagnostics, and CI execution.

Adding framework-only tests creates a second validation strategy that increases maintenance cost, encourages testing implementation details instead of user behavior, and dilutes the repository's purpose as a browser-driven automation portfolio.

## Decision
Browser UI tests remain the only automated validation layer in this repository. Do not add framework unit tests or framework-only TestNG suites. Changes to framework code are validated through executable UI scenarios, static analysis, quality gates, and code review.

## Consequences
The repository stays focused on business-visible automation outcomes. Pure helper logic may take a little longer to validate, but the codebase avoids a growing side test harness that provides limited portfolio value. When framework code changes, prefer strengthening an existing UI scenario or adding a new user-observable browser scenario instead of adding isolated helper tests.
