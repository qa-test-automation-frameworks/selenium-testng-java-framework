package io.github.selenium.saucedemo.framework.listener;

import io.github.selenium.saucedemo.framework.driver.WebDriverFactory;
import io.github.selenium.saucedemo.framework.util.DiagnosticsAttacher;
import io.qameta.allure.Allure;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class FrameworkListener implements ITestListener, IConfigurationListener, ISuiteListener {

  private static final DiagnosticsAttacher DIAGNOSTICS_ATTACHER = new DiagnosticsAttacher();
  private static final double MILLIS_PER_SECOND = 1000.0;

  @Override
  public void onStart(ISuite suite) {
    RetryRegistry.clear();
  }

  @Override
  public void onTestFailure(ITestResult result) {
    log.error("Test failed: {}", result.getName());
  }

  @Override
  public void onTestStart(ITestResult result) {
    log.info("Starting test: {}", result.getName());
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    log.info("Test passed: {}", result.getName());
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    log.warn("Test skipped: {}", result.getName());
  }

  @Override
  public void onFinish(ISuite suite) {
    var retries = RetryRegistry.snapshot();
    if (retries.isEmpty()) {
      log.info("No tests required retry in this run");
    } else {
      String summary = String.join(System.lineSeparator(), retries);
      log.warn("Tests required retry: {}", summary);
      Allure.addAttachment("Retry Summary", summary);
    }
    writePortfolioMetrics(suite, retries.size());
  }

  @Override
  public void onConfigurationFailure(ITestResult result) {
    log.error("Configuration failed: {}", result.getName());
    DIAGNOSTICS_ATTACHER.attach(
        "Configuration failure", WebDriverFactory.getThreadLocalWebDriver());
  }

  public static void attachTestFailureDiagnostics(WebDriver driver) {
    DIAGNOSTICS_ATTACHER.attach("Test failure", driver);
  }

  private static void writePortfolioMetrics(ISuite suite, int retries) {
    long passed =
        suite.getResults().values().stream()
            .mapToLong(result -> result.getTestContext().getPassedTests().size())
            .sum();
    long failed =
        suite.getResults().values().stream()
            .mapToLong(result -> result.getTestContext().getFailedTests().size())
            .sum();
    long skipped =
        suite.getResults().values().stream()
            .mapToLong(result -> result.getTestContext().getSkippedTests().size())
            .sum();
    long startedAt =
        suite.getResults().values().stream()
            .mapToLong(result -> result.getTestContext().getStartDate().getTime())
            .min()
            .orElse(0L);
    long finishedAt =
        suite.getResults().values().stream()
            .mapToLong(result -> result.getTestContext().getEndDate().getTime())
            .max()
            .orElse(startedAt);
    double durationSeconds = Math.max(0L, finishedAt - startedAt) / MILLIS_PER_SECOND;
    Path metricsFile =
        Path.of(System.getProperty("portfolio.metrics.file", "target/portfolio-metrics-v1.json"));
    String suiteName = Objects.toString(suite.getName(), "unknown").replace("\"", "\\\"");
    String duration = String.format(Locale.ROOT, "%.3f", durationSeconds);
    String json =
        "{\n"
            + "  \"schemaVersion\": 1,\n"
            + "  \"framework\": \"selenium-testng-java-framework\",\n"
            + "  \"generatedFrom\": \"TestNG suite results\",\n"
            + "  \"suite\": \""
            + suiteName
            + "\",\n"
            + "  \"tests\": "
            + (passed + failed + skipped)
            + ",\n"
            + "  \"passed\": "
            + passed
            + ",\n"
            + "  \"failures\": "
            + failed
            + ",\n"
            + "  \"skipped\": "
            + skipped
            + ",\n"
            + "  \"retries\": "
            + retries
            + ",\n"
            + "  \"durationSeconds\": "
            + duration
            + "\n"
            + "}\n";
    try {
      Path metricsDirectory =
          Objects.requireNonNull(
              metricsFile.toAbsolutePath().getParent(),
              "Metrics file must have a parent directory");
      Files.createDirectories(metricsDirectory);
      Files.writeString(metricsFile, json, StandardCharsets.UTF_8);
      log.info("Portfolio metrics written to {}", metricsFile);
    } catch (IOException exception) {
      throw new IllegalStateException(
          "Could not write portfolio metrics to " + metricsFile, exception);
    }
  }
}
