# ADR 001: Use Custom Typed Configuration

## Status
Accepted

## Context
The framework needs typed access to browser, execution, timeout, retry, and application settings while supporting system properties, environment variables, and profile-specific files. Depending on an unmaintained configuration library creates avoidable maintenance risk for a small framework.

## Decision
Use a small internal `FrameworkConfig` interface and `ConfigFactory` implementation. The loader reads defaults, `config.properties`, `${env}.properties`, environment variables, and system properties in that override order.

## Consequences
Configuration stays typed and readable without relying on a stale external library. The project owns parsing and validation logic, so new config keys require an explicit typed accessor and default.
