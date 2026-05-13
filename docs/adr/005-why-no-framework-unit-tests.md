# ADR 005: Allow Narrow Fast Framework Checks

## Status
Amended

## Context
This repository is intentionally a UI test automation framework. In the SDLC, it acts as the test layer that validates user-facing application behavior through browser automation. The framework code exists to support those UI tests with configuration, driver lifecycle, waits, page objects, reporting, diagnostics, and CI execution.

Broad framework unit-test coverage would increase maintenance cost without improving application confidence for this portfolio scope. However, the framework now contains pure, side-effect-light logic where narrow fast checks prevent expensive browser-only feedback: configuration validation, redaction behavior, and retry aggregation.

## Decision
Browser UI tests remain the primary validation layer. Narrow TestNG fast checks are allowed for pure framework logic such as configuration parsing, diagnostic redaction, and retry registry behavior. These checks use the `framework` group and must not initialize WebDriver or duplicate browser scenario coverage.

## Consequences
The repository keeps a UI automation focus while gaining cheap feedback for high-risk helper logic. Changes to page objects and user flows are still validated through browser scenarios, quality gates, and code review.
