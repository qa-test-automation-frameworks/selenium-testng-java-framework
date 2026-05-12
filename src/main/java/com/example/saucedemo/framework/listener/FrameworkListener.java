package com.example.saucedemo.framework.listener;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.driver.WebDriverFactory;
import com.example.saucedemo.framework.util.DiagnosticRedactor;
import com.example.saucedemo.framework.util.DiagnosticsCollector;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class FrameworkListener implements ITestListener, IConfigurationListener {

  @Override
  public void onStart(ITestContext context) {
    RetryRegistry.clear();
  }

  @Override
  public void onTestFailure(ITestResult result) {
    log.error("Test failed: {}", result.getName());
    attachDiagnostics("Test failure", WebDriverFactory.getThreadLocalWebDriver());
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
  public void onFinish(ITestContext context) {
    var retries = RetryRegistry.snapshot();
    if (retries.isEmpty()) {
      log.info("No tests required retry in this run");
      return;
    }
    String summary = String.join(System.lineSeparator(), retries);
    log.warn("Tests required retry: {}", summary);
    Allure.addAttachment("Retry Summary", summary);
  }

  @Override
  public void onConfigurationFailure(ITestResult result) {
    log.error("Configuration failed: {}", result.getName());
    attachDiagnostics("Configuration failure", WebDriverFactory.getThreadLocalWebDriver());
  }

  private void attachDiagnostics(String label, WebDriver driver) {
    if (driver == null) {
      log.info("Skipping {} diagnostics because no WebDriver instance is available", label);
      return;
    }

    log.info("Attaching {} diagnostics to Allure report", label);
    safeAttach("Screenshot", () -> attachScreenshot(driver));
    safeAttach(
        "Current URL", () -> Allure.addAttachment("Current URL", redact(driver.getCurrentUrl())));
    safeAttach("Browser Capabilities", () -> attachCapabilities(driver));
    safeAttach("Browser Console Logs", () -> attachBrowserLogs(driver));
    safeAttach("Browser Network Logs", () -> attachNetworkLogs(driver));
    safeAttach("Selenium Grid Video", () -> attachGridVideoLink(driver));
    safeAttach(
        "Page Source",
        () ->
            Allure.addAttachment(
                "Page Source", "text/html", redact(driver.getPageSource()), ".html"));
  }

  private void attachScreenshot(WebDriver driver) {
    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
  }

  private void safeAttach(String attachmentName, Runnable attachmentAction) {
    try {
      attachmentAction.run();
    } catch (Exception e) {
      log.warn("Failed to attach {} diagnostics", attachmentName, e);
    }
  }

  private void attachCapabilities(WebDriver driver) {
    if (driver instanceof HasCapabilities hasCapabilities) {
      Allure.addAttachment(
          "Browser Capabilities", redact(hasCapabilities.getCapabilities().toString()));
    }
  }

  private void attachBrowserLogs(WebDriver driver) {
    List<String> bidiConsoleLogs = DiagnosticsCollector.consoleLogs();
    if (!bidiConsoleLogs.isEmpty()) {
      Allure.addAttachment(
          "Browser Console Logs", redact(String.join(System.lineSeparator(), bidiConsoleLogs)));
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
      StringBuilder builder = new StringBuilder();
      logs.forEach(entry -> builder.append(entry).append(System.lineSeparator()));
      Allure.addAttachment("Browser Console Logs", redact(builder.toString()));
    } catch (Exception e) {
      log.debug("Browser console logs are not available for this driver", e);
    }
  }

  private void attachNetworkLogs(WebDriver driver) {
    if (!ConfigFactory.getConfig().networkLogsEnabled()) {
      return;
    }
    List<String> bidiNetworkLogs = DiagnosticsCollector.networkLogs();
    if (!bidiNetworkLogs.isEmpty()) {
      Allure.addAttachment(
          "Browser Network Logs",
          "application/json",
          redact(String.join(System.lineSeparator(), bidiNetworkLogs)),
          ".json");
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
      String payload =
          logs.getAll().stream()
              .map(Object::toString)
              .collect(Collectors.joining(System.lineSeparator()));
      Allure.addAttachment("Browser Network Logs", "application/json", redact(payload), ".json");
    } catch (Exception e) {
      log.debug("Browser network logs are not available for this driver", e);
    }
  }

  private void attachGridVideoLink(WebDriver driver) {
    String baseUrl = ConfigFactory.getConfig().gridVideoBaseUrl();
    if (baseUrl == null || baseUrl.isBlank() || !(driver instanceof RemoteWebDriver remoteDriver)) {
      return;
    }
    String videoUrl = baseUrl.replaceAll("/$", "") + "/" + remoteDriver.getSessionId() + ".mp4";
    Allure.addAttachment("Selenium Grid Video", redact(videoUrl));
  }

  private String redact(String rawValue) {
    return DiagnosticRedactor.redact(rawValue);
  }
}
