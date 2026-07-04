# Contributor Architecture Guide

## First Run

```bash
./mvnw test -Dtestng.suite.file=testng-framework-unit.xml
./mvnw test -Dgroups=smoke
```

The framework-unit suite exercises the framework's own classes without a browser, so it is the
fastest way to confirm the toolchain works before running the full Grid-backed UI suite.

## Project Map

| Area                         | Purpose                                                                   |
| ---------------------------- | ------------------------------------------------------------------------- |
| `src/main/java`              | Framework core: drivers, page objects, listeners, config, reporting code  |
| `src/test/java`              | TestNG suites and scenario coverage                                       |
| `src/test/resources`         | Suite resources and Allure configuration                                  |
| `testng*.xml`                | Suite definitions (default, accessibility, visual, framework-unit)        |
| `docs/`                      | Architecture, execution, debugging, reliability, and writing-tests guides |
| `reliability/quarantine.yml` | Quarantine policy and known exceptions                                    |

## Commands

- Main suite: `./mvnw test`
- Specific suite: `./mvnw test -Dtestng.suite.file=testng.xml`
- Accessibility suite: `./mvnw test -Dtestng.suite.file=testng-accessibility.xml`
- Visual suite: `./mvnw test -Dtestng.suite.file=testng-visual.xml`
- Framework unit suite: `./mvnw test -Dtestng.suite.file=testng-framework-unit.xml`
- Groups: `./mvnw test -Dgroups=smoke`
- Allure report: `./mvnw allure:report`

## Change Workflow

1. Prefer a TestNG suite, group, or class-level run over a full-suite rerun while iterating.
2. Preserve the Allure, quarantine, driver-lifecycle, and reliability-reporting conventions
   documented under `docs/`.
3. Run the quality gate (`./mvnw verify`) before opening a PR.
