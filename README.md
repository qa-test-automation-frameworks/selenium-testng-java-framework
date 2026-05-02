# Selenium Java TestNG Automation Framework

A premium, containerized automation framework built with Java 21, Selenium 4, TestNG, and Allure Reports.

## ✨ Features

- **Java 21 & Maven**: Modern Java features and dependency management.
- **Dockerized Execution**: Run tests in isolated containers using Docker Compose.
- **Selenium Grid Integration**: Built-in support for scaling tests on a remote grid.
- **Environment Profiles**: Switch between `dev`, `qa`, and other environments using the OWNER library.
- **Premium Logging**: Log4j 2 with color-coded, detailed output (Thread, Class, Method, Line).
- **Allure Reports**: Rich reporting with Categories and persistent History/Trends.
- **Jenkins Pipeline**: Declarative CI/CD pipeline with parameterized builds.
- **Type-Safe Config**: Managed by the [OWNER library](https://github.com/aeonbits/owner).

## 🚀 Getting Started

### Prerequisites
- JDK 21
- Maven
- Docker & Docker Compose (for containerized execution)

### Local Execution
To run tests on your local machine:
```bash
mvn clean verify
```
To run on a specific environment:
```bash
mvn verify -Denv=dev
```

### Containerized Execution
To spin up a Selenium Grid and run tests inside a container:
```bash
docker-compose up --build --exit-code-from test-runner
```

## ⚙️ Configuration

Configurations are managed via `src/main/resources/config.properties` and environment-specific files (`qa.properties`, `dev.properties`).

| Property | Description | Default |
|----------|-------------|---------|
| `driverType` | Browser to use (CHROME, FIREFOX, EDGE) | `CHROME` |
| `execution_type` | Where to run (local, remote) | `local` |
| `env` | Target environment profile | `qa` |
| `headless` | Run browser without UI | `false` |

## 📊 Reporting

After execution, generate and view the Allure report:
```bash
mvn allure:serve
```
The report includes **Trend analysis** and **Category classification** for easier debugging.
