# Execution Guide

This guide explains how to run tests in different environments and configurations.

## Prerequisites
- **JDK 21**
- **Maven 3.9+** or the included Maven wrapper
- **Docker** (optional, for Selenium Grid)

## Local Execution
To run tests locally with the default configuration (Chrome, local):
```bash
APP_PASSWORD=your_password ./mvnw clean verify
```

### Parameters
- `-Denv`: Environment profile (`qa` default, `dev`).
- `-Dbrowser`: Browser type (`CHROME` default, `FIREFOX`, `EDGE`).
- `-Dheadless`: Run in headless mode (`true`, `false`).
- `-Dgroups`: Run a subset of TestNG groups, for example `smoke` or `login`.
- `-Dretry.enabled`: Enable retry analyzer when investigating infrastructure flakes.

### Group Execution
```bash
APP_PASSWORD=your_password ./mvnw clean verify -Dgroups=smoke
```

## Remote Execution (Docker & Selenium Grid)
The framework includes a `docker-compose.yml` to spin up a Selenium Grid.

1. **Start the Grid**:
   ```bash
   APP_PASSWORD=your_password docker compose up --build --exit-code-from test-runner
   ```
2. **Run tests on the Grid**:
   ```bash
   APP_PASSWORD=your_password ./mvnw clean verify -Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub
   ```

## Allure Reporting
Allure results are generated in `target/allure-results`, and the generated report is written to `target/allure-report`.

To view the report:
```bash
./mvnw allure:serve
```

## Retry Policy
Retries are disabled by default and should be used only while investigating infrastructure instability. Enable them with `-Dretry.enabled=true` and keep `retry.count` low. The framework validates that `retry.count` is not negative and records a retry summary in the execution log and Allure report when a retry is used.

## Diagnostics
Failure diagnostics include screenshots, current URL, browser capabilities, browser console logs, and page source. Optional Chrome/Edge performance logs can be enabled with `-Ddiagnostics.network.logs.enabled=true`. For Selenium Grid setups that publish videos, set `diagnostics.grid.video.base.url` to attach a session video link on failure.

## SBOM
Generate a CycloneDX software bill of materials with:

```bash
./mvnw -Psbom -DskipTests verify
```

## Troubleshooting
- **Driver not found**: Ensure you have the corresponding browser installed. Selenium Manager will handle binary downloads automatically.
- **Config Error**: Ensure `APP_PASSWORD` is provided as an environment variable or CI secret.
