package io.github.selenium.saucedemo.framework.util;

import io.github.selenium.saucedemo.framework.config.ConfigFactory;
import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
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
  private static final String MASK_SENSITIVE_DOM_SCRIPT =
      "const selector = arguments[0];"
          + "try {"
          + " const elements = document.querySelectorAll(selector);"
          + " let selectedCount = 0;"
          + " for (const element of elements) {"
          + "  if ('value' in element) element.value = '<redacted>';"
          + "  element.setAttribute('value', '<redacted>');"
          + "  element.textContent = '<redacted>';"
          + "  selectedCount += 1;"
          + " }"
          + " return { status: 'applied', selectedCount: selectedCount };"
          + "} catch (_) {"
          + " return { status: 'failed' };"
          + "}";

  private final Supplier<FrameworkConfig> configSupplier;
  private final AttachmentSink attachmentSink;

  public DiagnosticsAttacher() {
    this(ConfigFactory::getConfig, new AllureAttachmentSink());
  }

  DiagnosticsAttacher(FrameworkConfig config, AttachmentSink attachmentSink) {
    this(() -> config, attachmentSink);
  }

  DiagnosticsAttacher(
      Supplier<FrameworkConfig> configSupplier, AttachmentSink attachmentSink) {
    this.configSupplier = Objects.requireNonNull(configSupplier);
    this.attachmentSink = Objects.requireNonNull(attachmentSink);
  }

  public void attach(String label, WebDriver driver) {
    if (driver == null) {
      log.info("Skipping {} diagnostics because no WebDriver instance is available", label);
      return;
    }

    log.info("Attaching {} diagnostics to Allure report", label);
    FrameworkConfig config = configSupplier.get();
    safeAttach("Execution Environment", () -> attachExecutionEnvironment(driver, config));

    ProtectionStatus protectionStatus = maskSensitiveDom(driver, config);
    safeAttach(
        "Sensitive DOM Masking",
        () -> attachmentSink.addText("Sensitive DOM Masking", protectionMessage(protectionStatus)));

    boolean protectedCaptureAllowed = protectionStatus.allowsCapture();
    if (config.attachScreenshotsOnFailure()) {
      if (protectedCaptureAllowed) {
        attachScreenshotSafely(driver);
      }
    }
    safeAttach(
        "Current URL",
        () ->
            attachmentSink.addText(
                "Current URL", safeAttachmentText(driver.getCurrentUrl(), config)));
    safeAttach("Browser Capabilities", () -> attachCapabilities(driver, config));
    if (config.attachBrowserLogsOnFailure()) {
      safeAttach("Browser Console Logs", () -> attachBrowserLogs(driver, config));
    }
    safeAttach("BiDi Diagnostics", this::attachBiDiStatus);
    safeAttach("Browser Network Logs", () -> attachNetworkLogs(driver, config));
    safeAttach("Selenium Grid Video", () -> attachGridVideoLink(driver, config));
    if (config.attachFrameworkLogsOnFailure()) {
      safeAttach("Framework Log Excerpt", () -> attachLogExcerptUnchecked(config));
    }
    if (config.attachPageSourceOnFailure()) {
      if (protectedCaptureAllowed) {
        safeAttach(
            "Page Source",
            () ->
                attachmentSink.addText(
                    "Page Source",
                    "text/html",
                    safeAttachmentText(driver.getPageSource(), config),
                    ".html"));
      } else {
        safeAttach(
            "Page Source omitted",
            () ->
                attachmentSink.addText(
                    "Page Source",
                    "Page source omitted because sensitive DOM masking did not succeed."));
      }
    }
  }

  private void attachScreenshotSafely(WebDriver driver) {
    try {
      byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      attachmentSink.addBytes("Screenshot", "image/png", screenshot, ".png");
    } catch (RuntimeException ignored) {
      log.warn("Screenshot capture failed after DOM protection; continuing with other diagnostics");
      safeAttach(
          "Screenshot failure status",
          () ->
              attachmentSink.addText(
                  "Screenshot", "Screenshot capture failed after DOM masking."));
    }
  }

  private ProtectionStatus maskSensitiveDom(WebDriver driver, FrameworkConfig config) {
    String selectors = config.sensitiveDomSelectors();
    if (selectors == null) {
      return ProtectionStatus.FAILED;
    }
    if (selectors.isBlank()) {
      return ProtectionStatus.NOT_CONFIGURED;
    }
    if (!(driver instanceof JavascriptExecutor javascriptExecutor)) {
      return ProtectionStatus.UNSUPPORTED;
    }

    try {
      Object response =
          javascriptExecutor.executeScript(MASK_SENSITIVE_DOM_SCRIPT, selectors);
      if (response instanceof Map<?, ?> result
          && "applied".equals(result.get("status"))
          && result.get("selectedCount") instanceof Number selectedCount
          && selectedCount.longValue() >= 0) {
        return ProtectionStatus.APPLIED;
      }
    } catch (RuntimeException ignored) {
      return ProtectionStatus.FAILED;
    }
    return ProtectionStatus.FAILED;
  }

  private String protectionMessage(ProtectionStatus status) {
    return switch (status) {
      case APPLIED -> "Sensitive DOM masking applied before browser artifacts were captured.";
      case NOT_CONFIGURED ->
          "Sensitive DOM masking is not configured; screenshots and page source are unmasked.";
      case FAILED ->
          "Sensitive DOM masking failed; screenshot and page source are omitted.";
      case UNSUPPORTED ->
          "Sensitive DOM masking is unsupported by this driver; "
              + "screenshot and page source are omitted.";
    };
  }

  private void attachCapabilities(WebDriver driver, FrameworkConfig config) {
    if (driver instanceof HasCapabilities hasCapabilities) {
      attachmentSink.addText(
          "Browser Capabilities",
          safeAttachmentText(hasCapabilities.getCapabilities().toString(), config));
    }
  }

  private void attachBrowserLogs(WebDriver driver, FrameworkConfig config) {
    List<String> bidiConsoleLogs = DiagnosticsCollector.consoleLogs();
    if (!bidiConsoleLogs.isEmpty()) {
      attachmentSink.addText(
          "Browser Console Logs",
          safeAttachmentText(String.join(System.lineSeparator(), bidiConsoleLogs), config));
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
      StringBuilder builder = new StringBuilder();
      logs.forEach(entry -> builder.append(entry).append(System.lineSeparator()));
      attachmentSink.addText(
          "Browser Console Logs", safeAttachmentText(builder.toString(), config));
    } catch (Exception e) {
      attachmentSink.addText(
          "Browser Console Logs", "Console logs unavailable for this browser/session");
      log.debug("Browser console logs are not available for this driver", e);
    }
  }

  private void attachNetworkLogs(WebDriver driver, FrameworkConfig config) {
    if (!config.networkLogsEnabled()) {
      return;
    }
    List<String> bidiNetworkLogs = DiagnosticsCollector.networkLogs();
    if (!bidiNetworkLogs.isEmpty()) {
      attachmentSink.addText(
          "Browser Network Logs",
          "application/json",
          safeAttachmentText(String.join(System.lineSeparator(), bidiNetworkLogs), config),
          ".json");
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
      String payload =
          logs.getAll().stream()
              .map(Object::toString)
              .collect(Collectors.joining(System.lineSeparator()));
      attachmentSink.addText(
          "Browser Network Logs",
          "application/json",
          safeAttachmentText(payload, config),
          ".json");
    } catch (Exception e) {
      attachmentSink.addText(
          "Browser Network Logs", "Network logs unavailable for this browser/session");
      log.debug("Browser network logs are not available for this driver", e);
    }
  }

  private void attachBiDiStatus() {
    if (!DiagnosticsCollector.isBiDiActive()) {
      attachmentSink.addText("BiDi Diagnostics", "Unavailable; legacy Selenium logs used.");
    }
  }

  private void attachGridVideoLink(WebDriver driver, FrameworkConfig config) {
    String baseUrl = config.gridVideoBaseUrl();
    if (baseUrl == null || baseUrl.isBlank() || !(driver instanceof RemoteWebDriver remoteDriver)) {
      return;
    }
    String videoUrl = baseUrl.replaceAll("/$", "") + "/" + remoteDriver.getSessionId() + ".mp4";
    attachmentSink.addText("Selenium Grid Video", safeAttachmentText(videoUrl, config));
  }

  private void attachLogExcerptUnchecked(FrameworkConfig config) {
    try {
      attachLogExcerpt(config);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to attach framework log excerpt", e);
    }
  }

  private void attachLogExcerpt(FrameworkConfig config) throws IOException {
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
      attachmentSink.addText(
          "Framework Log Excerpt", "text/plain", safeAttachmentText(excerpt, config), ".log");
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
    attachmentSink.addText(
        "Execution Environment", safeAttachmentText(environmentSummary, config));
  }

  private String safeAttachmentText(String rawValue, FrameworkConfig config) {
    return Objects.toString(DiagnosticRedactor.redact(rawValue, config), "");
  }

  private void safeAttach(String label, Runnable attachmentOperation) {
    try {
      attachmentOperation.run();
    } catch (RuntimeException exception) {
      log.warn("Unable to attach {} diagnostics; continuing with remaining diagnostics", label);
      log.debug("Diagnostic attachment failed for {}", label, exception);
    }
  }

  enum ProtectionStatus {
    APPLIED,
    NOT_CONFIGURED,
    FAILED,
    UNSUPPORTED;

    private boolean allowsCapture() {
      return this == APPLIED || this == NOT_CONFIGURED;
    }
  }

  interface AttachmentSink {
    void addText(String name, String text);

    void addText(String name, String contentType, String text, String fileExtension);

    void addBytes(String name, String contentType, byte[] bytes, String fileExtension);
  }

  private static final class AllureAttachmentSink implements AttachmentSink {

    @Override
    public void addText(String name, String text) {
      Allure.addAttachment(name, text);
    }

    @Override
    public void addText(String name, String contentType, String text, String fileExtension) {
      Allure.addAttachment(name, contentType, text, fileExtension);
    }

    @Override
    public void addBytes(String name, String contentType, byte[] bytes, String fileExtension) {
      Allure.addAttachment(
          name,
          contentType,
          new ByteArrayInputStream(bytes),
          fileExtension);
    }
  }
}
