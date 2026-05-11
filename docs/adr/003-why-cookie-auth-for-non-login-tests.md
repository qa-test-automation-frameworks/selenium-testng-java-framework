# ADR 003: Use Cookie Authentication for Non-Login Tests

## Status
Accepted

## Context
Most tests do not need to validate the login flow. Repeating UI login in every test increases runtime and expands the failure surface.

## Decision
Use `AuthService` to inject the Sauce Demo session cookie for non-login scenarios, while keeping login-specific tests on the real login form.

## Consequences
Tests remain independent and faster. Authentication behavior is still covered by dedicated login tests.
