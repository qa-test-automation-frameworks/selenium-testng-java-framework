# Execution Guide

This guide explains how to run tests in different environments and configurations.

## Prerequisites
- **JDK 21**
- **Maven 3.9+** or the included Maven wrapper
- **Docker** (optional, for Selenium Grid)

## Local Execution
To run tests locally with the default configuration (Chrome, local):
```bash
./mvnw clean verify -DAPP_PASSWORD=your_password
```

### Parameters
- `-Denv`: Environment profile (`qa` default, `dev`).
- `-Dbrowser`: Browser type (`CHROME` default, `FIREFOX`, `EDGE`).
- `-Dheadless`: Run in headless mode (`true`, `false`).
- `-Dgroups`: Run a subset of TestNG groups, for example `smoke` or `login`.
- `-Dretry.enabled`: Enable retry analyzer when investigating infrastructure flakes.

### Group Execution
```bash
./mvnw clean verify -DAPP_PASSWORD=your_password -Dgroups=smoke
```

## Remote Execution (Docker & Selenium Grid)
The framework includes a `docker-compose.yml` to spin up a Selenium Grid.

1. **Start the Grid**:
   ```bash
   APP_PASSWORD=your_password docker compose up --build --exit-code-from test-runner
   ```
2. **Run tests on the Grid**:
   ```bash
   ./mvnw clean verify -Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub -DAPP_PASSWORD=your_password
   ```

## Allure Reporting
Allure results are generated in `target/allure-results`, and the generated report is written to `target/allure-report`.

To view the report:
```bash
./mvnw allure:serve
```

## Troubleshooting
- **Driver not found**: Ensure you have the corresponding browser installed. Selenium Manager will handle binary downloads automatically.
- **Config Error**: Ensure `APP_PASSWORD` is provided as a system property or environment variable.
