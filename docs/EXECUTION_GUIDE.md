# Execution Guide

This guide explains how to run tests in different environments and configurations.

## Prerequisites
- **JDK 21**
- **Maven 3.8+**
- **Docker** (optional, for Selenium Grid)

## Local Execution
To run tests locally with the default configuration (Chrome, local):
```bash
mvn clean verify -DAPP_PASSWORD=your_secret_sauce
```

### Parameters
- `-Denv`: Environment profile (`qa` default, `dev`).
- `-Dbrowser`: Browser type (`CHROME` default, `FIREFOX`, `EDGE`).
- `-Dheadless`: Run in headless mode (`true`, `false`).

## Remote Execution (Docker & Selenium Grid)
The framework includes a `docker-compose.yml` to spin up a Selenium Grid.

1. **Start the Grid**:
   ```bash
   docker-compose up -d
   ```
2. **Run tests on the Grid**:
   ```bash
   mvn clean verify -Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub -DAPP_PASSWORD=your_secret_sauce
   ```

## Allure Reporting
Reports are generated in the `allure-report` directory at the project root.

To view the report:
```bash
mvn allure:serve
```

## Troubleshooting
- **Driver not found**: Ensure you have the corresponding browser installed. Selenium Manager will handle binary downloads automatically.
- **Config Error**: Ensure `APP_PASSWORD` is provided as a system property or environment variable.
