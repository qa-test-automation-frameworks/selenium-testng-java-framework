# ADR 005: Do Not Add a Separate Framework Unit-Test Layer

## Status
Accepted

## Context
This repository is intentionally a UI test automation framework. In the SDLC, it acts as the test layer that validates user-facing application behavior through browser automation. The framework code exists to support those UI tests with configuration, driver lifecycle, waits, page objects, reporting, diagnostics, and CI execution.

Adding a second test layer whose purpose is to test the test framework itself would increase maintenance cost without improving application confidence for this portfolio scope. It is not a recommended practice for this UI-only automation repository because it would reward testing helper implementation details instead of strengthening application-facing coverage. It would also dilute the repository's stated focus: showing mature, reliable UI automation design rather than building a general-purpose production library.

## Decision
Do not add JUnit, framework unit tests, or framework-internal test classes. Quality is enforced through meaningful browser-level scenarios, static analysis, formatting, dependency governance, runtime diagnostics, and CI execution.

## Consequences
The repository keeps a pure UI automation scope and avoids testing tests for the sake of metrics. Changes to framework internals must be validated through relevant UI flows, quality gates, and code review. If this framework later becomes a reusable production library shared across multiple products, this ADR should be revisited.
