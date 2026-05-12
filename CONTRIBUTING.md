# Contributing

## Prerequisites

- Java 21
- Maven 3.9.x or the included Maven wrapper
- Chrome, Firefox, Edge, or Safari installed for local browser runs

## Local Setup

1. Set the Sauce Demo password outside the repository:

   ```powershell
   $env:APP_PASSWORD = "secret_sauce"
   ```

2. Run the default regression suite:

   ```powershell
   .\mvnw.cmd clean verify
   ```

3. Override browser or execution settings with Maven properties:

   ```powershell
   .\mvnw.cmd clean verify -Dbrowser=FIREFOX -Dheadless=true -Dthread.count=2
   ```

## Code Style

- Keep framework code in `src/main/java` and tests/test data in `src/test/java`.
- Use page objects and components for UI interactions.
- Use explicit waits only. Do not add implicit waits or fixed sleeps.
- Run `.\mvnw.cmd spotless:apply` before opening a pull request when formatting changes are needed.

## Pull Requests

- Include focused changes that map to one feature, fix, or refactor.
- Add or update tests for behavioral changes.
- Keep credentials, tokens, browser profiles, and IDE workspace files out of the repository.
- Confirm `.\mvnw.cmd clean verify` passes locally or explain any environment-specific limitation.

## Commit Messages

Use concise, imperative commit messages, for example:

- `test: add checkout happy path coverage`
- `fix: wait for cart badge before reading count`
- `chore: expand checkstyle rules`
