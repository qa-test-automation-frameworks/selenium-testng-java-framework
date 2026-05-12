# Contributing

## Prerequisites

- Java 21
- Maven 3.9.x or the included Maven wrapper
- Chrome, Firefox, Edge, or Safari installed for local browser runs

## Local Setup

This repository is intentionally focused on UI automation. Keep `src/test/java` dedicated to TestNG UI scenarios, test data, and supporting orchestration rather than adding a separate framework unit-test package.

1. Set the Sauce Demo password outside the repository:

   ```powershell
   $env:APP_PASSWORD = "<sauce-demo-password>"
   ```

2. Run the default regression suite:

   ```powershell
   .\mvnw.cmd clean verify
   ```

3. Override browser or execution settings with Maven properties:

   ```powershell
   .\mvnw.cmd clean verify -Dbrowser=FIREFOX -Dheadless=true -Dthread.count=2
   ```

4. Run no-secret contributor smoke coverage when you only need non-login UI validation:

   ```powershell
   .\mvnw.cmd clean test -Dgroups=inventory,cart -Dheadless=true
   ```

## Code Style

- Keep framework code in `src/main/java` and tests/test data in `src/test/java`.
- Use page objects and components for UI interactions.
- Call `waitUntilLoaded()` explicitly from tests or navigation methods when page readiness must be asserted.
- Use explicit waits only. Do not add implicit waits or fixed sleeps.
- Run `\.\mvnw.cmd spotless:apply` before opening a pull request when formatting changes are needed.
- Run `\.\mvnw.cmd -DskipTests validate spotless:check checkstyle:check pmd:check spotbugs:check` when changing framework code, build logic, or CI configuration.

## Pull Requests

- Include focused changes that map to one feature, fix, or refactor.
- Add or update UI automation coverage when behavioral changes require it.
- Keep credentials, tokens, browser profiles, and IDE workspace files out of the repository.
- Confirm `.\mvnw.cmd clean verify` passes locally or explain any environment-specific limitation.

## Commit Messages

Use concise, imperative commit messages, for example:

- `test: add checkout happy path coverage`
- `fix: wait for cart badge before reading count`
- `chore: expand checkstyle rules`
