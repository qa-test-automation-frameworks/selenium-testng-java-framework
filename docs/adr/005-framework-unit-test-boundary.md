# ADR 005: Framework Unit-Test Boundary

## Status

Accepted. Supersedes the earlier decision to prohibit framework unit tests.

## Context

Browser scenarios are the primary product evidence, but some framework behavior is deterministic and does not need a browser: redaction, configuration value parsing, retry metadata, and similar pure logic. Requiring a browser to validate these controls makes feedback slower and leaves security-sensitive helpers without focused regression coverage.

## Decision

Keep browser-driven TestNG scenarios as the functional acceptance layer and add a narrow `testng-framework-unit.xml` suite for deterministic framework logic. The unit suite must not mock page objects or duplicate user journeys. CI runs it during Maven `verify` so compiled inputs, static analysis, unit checks, and reports share one lifecycle.

## Consequences

Framework regressions receive fast focused feedback, while UI behavior remains validated through the browser matrix. Maintainers must keep the unit boundary narrow and resist duplicating Selenium scenarios as implementation-detail tests.
