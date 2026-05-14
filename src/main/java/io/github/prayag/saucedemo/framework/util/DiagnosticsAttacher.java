package io.github.prayag.saucedemo.framework.util;

import io.github.prayag.saucedemo.framework.config.ConfigFactory;
import io.github.prayag.saucedemo.framework.config.FrameworkConfig;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;

@Slf4j
public final class DiagnosticsAttacher {

  private static final Path FRAMEWORK_LOG_PATH = Path.of("target", "logs", "framework.log");
  private static final int MAX_LOG_LINES = 250;

  public void attach(String label, WebDriver driver) {
    if (driver == null) {
      log.info("Skipping {} diagnostics because no WebDriver instance is available", label);
      return;
    }

    log.info("Attaching {} diagnostics to Allure report", label);
    FrameworkConfig config = ConfigFactory.getConfig();
    safeAttach("Execution Environment", () -> attachExecutionEnvironment(driver, config));
    safeAttach("Sensitive DOM Masking", () -> maskSensitiveDom(driver, config));
    if (config.attachScreenshotsOnFailure()) {
      safeAttach("Screenshot", () -> attachScreenshot(driver));
    }
    safeAttach(
        "Current URL",
        () -> Allure.addAttachment("Current URL", safeAttachmentText(driver.getCurrentUrl())));
    safeAttach("Browser Capabilities", () -> attachCapabilities(driver));
    if (config.attachBrowserLogsOnFailure()) {
      safeAttach("Browser Console Logs", () -> attachBrowserLogs(driver));
    }
    safeAttach("BiDi Diagnostics", this::attachBiDiStatus);
    safeAttach("Browser Network Logs", () -> attachNetworkLogs(driver));
    safeAttach("Selenium Grid Video", () -> attachGridVideoLink(driver));
    if (config.attachFrameworkLogsOnFailure()) {
      safeAttach("Framework Log Excerpt", this::attachLogExcerptUnchecked);
    }
    if (config.attachPageSourceOnFailure()) {
      safeAttach(
          "Page Source",
          () ->
              Allure.addAttachment(
                  "Page Source", "text/html", safeAttachmentText(driver.getPageSource()), ".html"));
    }
  }

  private void attachScreenshot(WebDriver driver) {
    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
  }

  private void maskSensitiveDom(WebDriver driver, FrameworkConfig config) {
    if (!(driver instanceof JavascriptExecutor javascriptExecutor)) {
      return;
    }
    javascriptExecutor.executeScript(
        "document.querySelectorAll(arguments[0]).forEach(function(element) {"
            + " if ('value' in element) { element.value = '<redacted>'; }"
            + " element.setAttribute('value', '<redacted>');"
            + " if (element.children.length === 0) { element.textContent = '<redacted>'; }"
            + "});",
        config.sensitiveDomSelectors());
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
          "Browser Capabilities", safeAttachmentText(hasCapabilities.getCapabilities().toString()));
    }
  }

  private void attachBrowserLogs(WebDriver driver) {
    List<String> bidiConsoleLogs = DiagnosticsCollector.consoleLogs();
    if (!bidiConsoleLogs.isEmpty()) {
      Allure.addAttachment(
          "Browser Console Logs",
          safeAttachmentText(String.join(System.lineSeparator(), bidiConsoleLogs)));
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
      StringBuilder builder = new StringBuilder();
      logs.forEach(entry -> builder.append(entry).append(System.lineSeparator()));
      Allure.addAttachment("Browser Console Logs", safeAttachmentText(builder.toString()));
    } catch (Exception e) {
      Allure.addAttachment(
          "Browser Console Logs", "Console logs unavailable for this browser/session");
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
          safeAttachmentText(String.join(System.lineSeparator(), bidiNetworkLogs)),
          ".json");
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
      String payload =
          logs.getAll().stream()
              .map(Object::toString)
              .collect(Collectors.joining(System.lineSeparator()));
      Allure.addAttachment(
          "Browser Network Logs", "application/json", safeAttachmentText(payload), ".json");
    } catch (Exception e) {
      Allure.addAttachment(
          "Browser Network Logs", "Network logs unavailable for this browser/session");
      log.debug("Browser network logs are not available for this driver", e);
    }
  }

  private void attachBiDiStatus() {
    if (!DiagnosticsCollector.isBiDiActive()) {
      Allure.addAttachment("BiDi Diagnostics", "Unavailable; legacy Selenium logs used.");
    }
  }

  private void attachGridVideoLink(WebDriver driver) {
    String baseUrl = ConfigFactory.getConfig().gridVideoBaseUrl();
    if (baseUrl == null || baseUrl.isBlank() || !(driver instanceof RemoteWebDriver remoteDriver)) {
      return;
    }
    String videoUrl = baseUrl.replaceAll("/$", "") + "/" + remoteDriver.getSessionId() + ".mp4";
    Allure.addAttachment("Selenium Grid Video", safeAttachmentText(videoUrl));
  }

  private void attachLogExcerptUnchecked() {
    try {
      attachLogExcerpt();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to attach framework log excerpt", e);
    }
  }

  private void attachLogExcerpt() throws IOException {
    if (!Files.exists(FRAMEWORK_LOG_PATH)) {
      log.debug("Framework log file not found at {}", FRAMEWORK_LOG_PATH.toAbsolutePath());
      return;
    }

    String threadMarker = "[" + Thread.currentThread().getName() + "]";
    Deque<String> recentLines = new ArrayDeque<>(MAX_LOG_LINES);
    Deque<String> recentThreadLines = new ArrayDeque<>(MAX_LOG_LINES);

    try (Stream<String> lines = Files.lines(FRAMEWORK_LOG_PATH)) {
      lines.forEach(
          line -> {
            appendBounded(recentLines, line);
            if (line.contains(threadMarker)) {
              appendBounded(recentThreadLines, line);
            }
          });
    }

    Deque<String> excerptSource = recentThreadLines.isEmpty() ? recentLines : recentThreadLines;
    String excerpt = String.join(System.lineSeparator(), excerptSource);
    if (!excerpt.isBlank()) {
      Allure.addAttachment("Framework Log Excerpt", "text/plain", redact(excerpt), ".log");
    }
  }

  private void appendBounded(Deque<String> lines, String line) {
    if (lines.size() == MAX_LOG_LINES) {
      lines.removeFirst();
    }
    lines.addLast(line);
  }

  private void attachExecutionEnvironment(WebDriver driver, FrameworkConfig config) {
    String environmentSummary =
        String.join(
            System.lineSeparator(),
            "Environment: " + config.environment(),
            "Browser: " + config.browser(),
            "Execution type: " + config.executionType(),
            "Headless: " + config.headless(),
            "Thread: " + Thread.currentThread().getName(),
            "Current URL: " + driver.getCurrentUrl(),
            "Page title: " + driver.getTitle());
    Allure.addAttachment("Execution Environment", safeAttachmentText(environmentSummary));
  }

  private String redact(String rawValue) {
    return DiagnosticRedactor.redact(rawValue);
  }

  private String safeAttachmentText(String rawValue) {
    return java.util.Objects.toString(redact(rawValue), "");
  }
}
