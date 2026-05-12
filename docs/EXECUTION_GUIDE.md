# Execution Guide

This guide explains how to run tests in different environments and configurations.

This repository is intentionally focused on UI automation execution. The `src/test/java` source set contains TestNG UI suites, test data, and supporting orchestration rather than a separate framework unit-test layer.

## Prerequisites
- **JDK 21**
- **Maven 3.9+** or the included Maven wrapper
- **Docker** (optional, for Selenium Grid)

## Local Execution
To run tests locally with the default configuration (Chrome, local):
```bash
export APP_PASSWORD="<set-outside-repository>"
./mvnw clean verify
```

To run no-secret UI smoke coverage only (`inventory`, `cart`):
```bash
./mvnw clean test -Dgroups=inventory,cart -Dheadless=true
```

### Parameters
- `-Denv`: Environment profile (`qa` default, `dev`). This takes precedence over environment variables `ENV` and `env`.
- `-Dbrowser`: Browser type (`CHROME` default, `FIREFOX`, `EDGE`).
- `-Dheadless`: Run in headless mode (`true`, `false`).
- `-Dgroups`: Run a subset of TestNG groups, for example `smoke` or `login`.
- `-Dconfig.file`: Optional external properties file for private local overrides.
- `-Dretry.enabled`: Enable retry analyzer for tests explicitly marked with `@Retryable` when investigating infrastructure flakes.

### Group Execution
```bash
export APP_PASSWORD="<set-outside-repository>"
./mvnw clean verify -Dgroups=smoke
```

## Remote Execution (Docker & Selenium Grid)
The framework includes a `docker-compose.yml` to spin up a Selenium Grid.

1. **Start the Grid**:
   ```bash
   export APP_PASSWORD="<set-outside-repository>"
   docker compose up --build --exit-code-from test-runner
   ```
2. **Run tests on the Grid**:
   ```bash
   export APP_PASSWORD="<set-outside-repository>"
   ./mvnw clean verify -Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub
   ```

To include Edge in the local Grid, enable the optional profile:

```bash
export APP_PASSWORD="<set-outside-repository>"
docker compose --profile edge up --build --exit-code-from test-runner
```

The Docker `test-runner` waits for Selenium Grid readiness before invoking Maven.

## Jenkins Execution
The Jenkins pipeline expects a secret text credential with ID `sauce-demo-password` and passes it to Docker Compose as `APP_PASSWORD`.

## Allure Reporting
Allure results are generated in `target/allure-results`, and the generated report is written to `target/allure-report`.

To view the report:
```bash
./mvnw allure:serve
```

To preserve report generation after a failing run, use one of the helper scripts:
```bash
./scripts/run-ui-tests-with-allure-report.sh
pwsh ./scripts/run-ui-tests-with-allure-report.ps1
```

## Retry Policy
Retries are disabled by default and should be used only while investigating infrastructure instability. Enable them with `-Dretry.enabled=true`, mark only eligible tests with `@Retryable`, and keep `retry.count` low. The framework validates that `retry.count` is not negative and records the retry failure type, retry summary, and Allure retry labels when a retry is used.

## Diagnostics
Failure diagnostics include current URL, browser capabilities, screenshots, browser console logs, page source, and a framework log excerpt. Text-based attachments are redacted before they are written to Allure. Screenshot, page source, browser log, and framework log attachments can be disabled with the `diagnostics.attach.*.on.failure` properties. Optional Chrome/Edge performance logs can be enabled with `-Ddiagnostics.network.logs.enabled=true`; unsupported browsers/sessions add an explicit "network logs unavailable" attachment. For Selenium Grid setups that publish videos, set `diagnostics.grid.video.base.url` to attach a session video link on failure. The provided Docker Compose file does not record videos by itself.

## Quality Gates
To run the same non-UI quality checks used in CI:

```bash
./mvnw -DskipTests validate spotless:check checkstyle:check pmd:check spotbugs:check
```

## SBOM
Generate a CycloneDX software bill of materials with:

```bash
./mvnw -Psbom -DskipTests verify
```

## Troubleshooting
- **Driver not found**: Ensure you have the corresponding browser installed. Selenium Manager will handle binary downloads automatically.
- **Edge Grid not available**: Start Docker Compose with `--profile edge` before running `-Dbrowser=EDGE` against Selenium Grid.
- **Config Error**: Ensure `APP_PASSWORD` is provided as an environment variable or CI secret for login scenarios. Non-login smoke groups can run without it.
- **Wrong environment selected**: Confirm whether `-Denv`, `ENV`, or `env` is set. The framework uses that precedence order.
