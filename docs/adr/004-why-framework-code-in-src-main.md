# ADR 004: Keep Framework Code in src/main

## Status
Accepted

## Context
Reusable automation framework code is separate from concrete TestNG scenarios.

## Decision
Place driver management, configuration, listeners, utilities, page objects, and components under `src/main/java`. Keep test classes and test data under `src/test/java`.

## Consequences
The project structure clearly separates reusable framework code from scenario implementation and makes the framework package easier to reuse or extend.
