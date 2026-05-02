# Selenium Java TestNG Automation Framework

A premium, containerized automation framework built with Java 21, Selenium 4, TestNG, and Allure Reports. Designed for scalability, parallel execution, and CI/CD readiness.

## 📚 Documentation
- [**Architecture Overview**](docs/ARCHITECTURE.md) - Deep dive into layers and design patterns.
- [**Execution Guide**](docs/EXECUTION_GUIDE.md) - Detailed instructions for local and remote runs.
- [**Test Writing Guide**](docs/TEST_WRITING_GUIDE.md) - Best practices for adding new tests and pages.

## ✨ Features
- **Java 21 & Maven**: Leverages modern Java features and robust dependency management.
- **Dockerized Execution**: Run tests in isolated containers using Docker Compose.
- **Selenium Grid Integration**: Native support for horizontal scaling via remote execution.
- **Environment Management**: Dynamic switching via [OWNER library](https://github.com/aeonbits/owner).
- **Stateless Page Object Model**: Clean separation of UI logic and test assertions.
- **Allure Reporting**: Rich, interactive reports with screenshots, environment info, and execution trends.
- **Static Analysis**: Integrated Checkstyle (Google) and Spotless formatting for code quality.

## 🚀 Getting Started

### Prerequisites
- **JDK 21**
- **Maven 3.8+**
- **Docker & Docker Compose** (for remote execution)

### Quick Run
To run tests locally with the default configuration:
```bash
mvn clean verify -DAPP_PASSWORD=your_secret_sauce
```

### Allure Dashboard
To generate and view the interactive report:
```bash
mvn allure:serve
```

## ⚙️ Configuration
Configurations are managed via `src/test/resources/config.properties`.

| Property | Description | Default |
|----------|-------------|---------|
| `browser` | Browser type (CHROME, FIREFOX, EDGE) | `CHROME` |
| `execution.type` | local or remote | `local` |
| `remote.url` | Selenium Grid URL | `http://localhost:4444/wd/hub` |
| `headless` | Run in background | `false` |

## 🛠️ Tech Stack
- **Languages**: Java 21
- **Test Runner**: TestNG
- **Reporting**: Allure
- **Assertions**: AssertJ
- **Logging**: Log4j 2 / Slf4j
- **Utilities**: Lombok, OWNER

---
