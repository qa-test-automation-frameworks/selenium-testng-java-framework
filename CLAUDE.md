# CLAUDE.md

## Project

Java 21 Selenium WebDriver + TestNG UI automation framework with Allure reporting,
accessibility, visual suites, reliability policy, and portfolio evidence.

## Session Start

Refresh the local code graph before structural discovery:

`bash .agent/index-codebase-memory.sh`

Current MCP project name:

`home-vyaspc-Documents-Repo-selenium-testng-java-framework`

## Commands

- Install/use wrapper: `./mvnw --version`
- Main suite: `./mvnw test`
- Specific suite: `./mvnw test -Dtestng.suite.file=testng.xml`
- Accessibility suite: `./mvnw test -Dtestng.suite.file=testng-accessibility.xml`
- Visual suite: `./mvnw test -Dtestng.suite.file=testng-visual.xml`
- Framework unit suite: `./mvnw test -Dtestng.suite.file=testng-framework-unit.xml`
- Groups: `./mvnw test -Dgroups=smoke`
- Allure report: `./mvnw allure:report`

## Layout

- `src/main/java` - framework core, drivers, pages, listeners, config, and reporting code.
- `src/test/java` - TestNG suites and scenario coverage.
- `src/test/resources` - suite resources and Allure configuration.
- `testng*.xml` - suite definitions.
- `docs/` - architecture, execution, debugging, reliability, and writing-tests guides.
- `reliability/quarantine.yml` - quarantine policy and known exceptions.

## Codebase Memory MCP

Use graph tools before broad file reads:

1. `list_projects`
2. `get_architecture(project="home-vyaspc-Documents-Repo-selenium-testng-java-framework")`
3. `search_graph`
4. `trace_path`
5. `get_code_snippet`
6. `query_graph`

Fall back to `rg` for literals, configs, docs, generated files, scripts excluded from the graph,
or insufficient graph results.

## Agent Rules

- Cite `file:line` for code claims whenever practical.
- Prefer a TestNG suite, group, or class-level run over full-suite reruns.
- Preserve Allure, quarantine, driver lifecycle, and reliability-reporting conventions.
- Do not commit `.codebase-memory/`, `codebase-memory/`, or `.agent/index-codebase-memory.sh`.

