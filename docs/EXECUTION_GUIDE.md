# Execution Guide

This guide explains how to run tests in different environments and configurations.

This repository is intentionally focused on UI automation execution. The `src/test/java` source set contains TestNG UI suites, test data, supporting orchestration, and narrow fast checks for pure framework logic.

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

PowerShell users should set `$env:APP_PASSWORD = "<set-outside-repository>"` before running password-backed tests. Local Maven runs do not auto-load `.env`; copy `.env.example` only for Docker Compose or export the values in your shell.

To run no-secret UI smoke coverage only (`inventory`, `cart`):
```bash
./mvnw clean test -Dgroups=inventory,cart -Dheadless=true
```

PowerShell examples must quote comma-separated groups and dotted Maven properties:

```powershell
.\mvnw.cmd test '-Dgroups=inventory,cart' -Dheadless=true -Dbrowser=CHROME '-Dthread.count=2'
```

### Parameters
- `-Denv`: Environment profile (`qa` default, `dev`). This takes precedence over environment variables `ENV` and `env`. The default `qa` environment can run from built-in safe defaults; non-default environments require a matching classpath profile or `-Dconfig.file`.
- `-Dbrowser`: Browser type (`CHROME` default, `FIREFOX`, `EDGE`).
- `-Dheadless`: Run in headless mode (`true`, `false`).
- `-Dgroups`: Run a subset of TestNG groups, for example `smoke` or `login`.
- `-Dconfig.file`: Optional external properties file for private local overrides.
- `-Dretry.enabled`: Enable retry analyzer for tests explicitly marked with `@Retryable(reason = "...")` when investigating infrastructure flakes.
- `-Dallow.passwordless.skips`: Allow password-backed tests to skip when `APP_PASSWORD` is missing. Keep this `false` for full regression; public smoke runs should use non-login groups instead.
- `-Ddiagnostics.sensitive.dom.selectors`: CSS selectors masked before screenshots and page source are attached.
- `-Dallure.issue.pattern`: Override the Allure issue-link pattern, for example `https://github.com/<owner>/<repo>/issues/{}`.

### Group Execution
```bash
export APP_PASSWORD="<set-outside-repository>"
./mvnw clean verify -Dgroups=smoke
```

## Remote Execution (Docker & Selenium Grid)
The framework includes a `docker-compose.yml` to spin up a Selenium Grid.

1. **Start the Grid**:
   ```bash
   export APP_PASSWORD="<set-outside-repository>" # required for full regression
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

Docker Compose also accepts `GROUPS`, `THREAD_COUNT`, `HEADLESS`, `RETRY_ENABLED`, and `ALLOW_PASSWORDLESS_SKIPS` environment variables:

```bash
GROUPS=inventory,cart THREAD_COUNT=2 docker compose up --build --exit-code-from test-runner
```

Docker and Selenium Grid images are pinned by both readable version tag and digest. Refresh digests whenever Dependabot updates Docker image tags.

## Jenkins Execution
The Jenkins pipeline expects a secret text credential with ID `sauce-demo-password` and passes it to Docker Compose as `APP_PASSWORD`. Jenkins parameters mirror the main Docker controls: browser, groups, thread count, headless mode, retry enablement, and passwordless skip mode.

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
Retries are disabled by default and should be used only while investigating infrastructure instability. Enable them with `-Dretry.enabled=true`, mark only eligible tests with `@Retryable(reason = "...")`, and keep `retry.count` low. Every retryable test must declare why retry is acceptable. The framework validates that `retry.count` is not negative and records the retry failure type, retry reason, Allure retry labels, and one suite-level retry summary when a retry is used.

## Diagnostics
Failure diagnostics include current URL, browser capabilities, screenshots, browser console logs, page source, and a framework log excerpt. Text-based attachments are redacted before they are written to Allure. Sensitive DOM fields matched by `diagnostics.sensitive.dom.selectors` are masked before browser artifacts are captured. Screenshot, page source, browser log, and framework log attachments can be disabled with the `diagnostics.attach.*.on.failure` properties. Optional Chrome/Edge performance logs can be enabled with `-Ddiagnostics.network.logs.enabled=true`; unsupported browsers/sessions add explicit unavailable attachments. For Selenium Grid setups that publish videos, set `diagnostics.grid.video.base.url` to attach a session video link on failure. The provided Docker Compose file does not record videos by itself.

To exercise optional network diagnostics locally:

```bash
./mvnw clean test -Dgroups=inventory,cart -Dheadless=true -Ddiagnostics.network.logs.enabled=true
```

## Quality Gates
To run the same non-UI quality checks used in CI:

```bash
./mvnw -DskipTests validate spotless:check checkstyle:check pmd:check spotbugs:check
```

Run the fast framework checks without browser startup:

```bash
./mvnw test -Dgroups=framework
```

## SBOM
Generate a CycloneDX software bill of materials with:

```bash
./mvnw -Psbom -DskipTests verify
```

The scheduled GitHub Actions dependency-governance job also uploads Maven dependency/plugin update reports and this SBOM for review.

## Troubleshooting
- **Driver not found**: Ensure you have the corresponding browser installed. Selenium Manager will handle binary downloads automatically.
- **Edge Grid not available**: Start Docker Compose with `--profile edge` before running `-Dbrowser=EDGE` against Selenium Grid.
- **Config Error**: Ensure `APP_PASSWORD` is provided as an environment variable or CI secret for full regression and login/persona/journey scenarios. Non-login smoke groups can run without it. Use `-Dallow.passwordless.skips=true` only when intentionally demonstrating public no-secret behavior.
- **Wrong environment selected**: Confirm whether `-Denv`, `ENV`, or `env` is set. The framework uses that precedence order. If a non-default environment is selected without a matching profile or external config file, startup fails intentionally.
- **CDP/BiDi diagnostic warning**: Local evergreen Chrome/Edge versions can be newer than Selenium's packaged DevTools artifact, and some sessions may fall back from BiDi to legacy logs. Prefer the digest-pinned Docker Grid images for reproducible browser diagnostics, or update Selenium when matching DevTools support becomes available.
