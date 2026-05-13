package io.github.prayag.saucedemo.framework.listener;

import io.github.prayag.saucedemo.framework.config.FrameworkConfig;
import io.github.prayag.saucedemo.framework.config.TestRunContext;
import io.github.prayag.saucedemo.framework.util.DiagnosticRedactor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

@Slf4j
public class SuiteConfigurationListener implements IAlterSuiteListener {

  @Override
  public void alter(List<XmlSuite> suites) {
    TestRunContext runContext = TestRunContext.load();
    FrameworkConfig config = runContext.config();
    int threadCount = config.threadCount();
    suites.forEach(suite -> suite.setThreadCount(threadCount));
    generateAllureEnvironmentFile(runContext);
    logResolvedConfiguration(config, runContext, suites);
  }

  private void logResolvedConfiguration(
      FrameworkConfig config, TestRunContext runContext, List<XmlSuite> suites) {
    log.info("=== Resolved Framework Configuration ===");
    log.info("Run ID: {}", runContext.runId());
    log.info("Started At: {}", runContext.startedAt());
    log.info("Environment: {}", config.environment());
    log.info("Browser: {}", config.browser());
    log.info("Headless: {}", config.headless());
    log.info("Execution Type: {}", config.executionType());
    log.info("Thread Count: {}", config.threadCount());
    log.info("App URL: {}", DiagnosticRedactor.redact(config.appUrl()));
    log.info("Retry Enabled: {}", config.retryEnabled());
    log.info("Retry Count: {}", config.retryCount());
    log.info("Explicit Wait Seconds: {}", config.explicitWaitSeconds());
    log.info("Polling Interval Ms: {}", config.pollingIntervalMs());
    log.info("Page Load Timeout Seconds: {}", config.pageLoadTimeoutSeconds());
    log.info("Script Timeout Seconds: {}", config.scriptTimeoutSeconds());
    log.info("Network Logs Enabled: {}", config.networkLogsEnabled());
    log.info("Grid Video Base URL Configured: {}", !config.gridVideoBaseUrl().isBlank());
    log.info("Suites Updated: {}", suites.stream().map(XmlSuite::getName).toList());
    log.info("========================================");
  }

  private void generateAllureEnvironmentFile(TestRunContext runContext) {
    FrameworkConfig config = runContext.config();
    Properties props = new Properties();
    props.setProperty("Run ID", runContext.runId());
    props.setProperty("Run Started At", runContext.startedAt().toString());
    props.setProperty("Environment", config.environment().toUpperCase());
    props.setProperty("App URL", DiagnosticRedactor.redact(config.appUrl()));
    props.setProperty("Browser Requested", config.browser());
    props.setProperty("Execution Type", config.executionType());
    props.setProperty("OS", System.getProperty("os.name"));
    props.setProperty("Java Version", System.getProperty("java.version"));

    try {
      Files.createDirectories(runContext.artifactDirectory());
      try (FileOutputStream fos =
          new FileOutputStream(
              Paths.get(runContext.artifactDirectory().toString(), "environment.properties")
                  .toString())) {
        props.store(fos, "Allure Environment Properties");
        log.info(
            "Allure environment.properties generated successfully in {}",
            runContext.artifactDirectory());
      }
    } catch (IOException e) {
      log.error("Failed to generate Allure environment.properties", e);
    }
  }
}
