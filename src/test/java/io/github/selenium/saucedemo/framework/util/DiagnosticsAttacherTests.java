package io.github.selenium.saucedemo.framework.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.selenium.saucedemo.framework.config.ConfigLoader;
import io.github.selenium.saucedemo.framework.config.ConfigSources;
import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.Test;

public class DiagnosticsAttacherTests {

  private static final byte[] SCREENSHOT_BYTES =
      "screenshot-canary".getBytes(StandardCharsets.UTF_8);

  @Test
  public void appliedMaskIsConfirmedBeforeScreenshotBytesAreAttached() {
    FakeDriver fakeDriver = new FakeDriver(true, Map.of("status", "applied", "selectedCount", 1));
    RecordingSink sink = new RecordingSink();

    attacher("#secret-canary", true, true, sink)
        .attach("Synthetic failure", fakeDriver.webDriver());

    assertThat(fakeDriver.calls).containsSubsequence("mask", "screenshot");
    assertThat(fakeDriver.screenshotCalls).isEqualTo(1);
    assertThat(sink.named("Screenshot")).hasSize(1);
    assertThat(sink.named("Screenshot").getFirst().bytes()).containsExactly(SCREENSHOT_BYTES);
    assertThat(sink.text("Sensitive DOM Masking"))
        .isEqualTo("Sensitive DOM masking applied before browser artifacts were captured.");
    assertThat(sink.named("Page Source")).hasSize(1);
    assertThat(!sink.allText().contains("page-source-canary"))
        .as("page source reflects the masked DOM")
        .isTrue();
  }

  @Test
  public void scriptFailureOmitsScreenshotAndPageSourceWithoutLeakingFailureDetails() {
    FakeDriver fakeDriver = new FakeDriver(true, null);
    fakeDriver.failScript = true;
    RecordingSink sink = new RecordingSink();

    attacher("#secret-selector-canary", true, true, sink)
        .attach("Synthetic failure", fakeDriver.webDriver());

    assertThat(fakeDriver.screenshotCalls).isZero();
    assertThat(fakeDriver.calls).doesNotContain("screenshot", "page-source");
    assertThat(sink.text("Sensitive DOM Masking"))
        .isEqualTo("Sensitive DOM masking failed; screenshot and page source are omitted.");
    assertThat(sink.text("Page Source"))
        .isEqualTo("Page source omitted because sensitive DOM masking did not succeed.");
    boolean failureDetailsWereExcluded =
        !sink.allText().contains("secret-selector-canary")
            && !sink.allText().contains("script-error-canary")
            && !sink.allText().contains("page-source-canary")
            && !sink.allBytesAsText().contains("screenshot-canary");
    assertThat(failureDetailsWereExcluded)
        .as("selectors, raw errors, DOM and screenshot bytes stay out of failure diagnostics")
        .isTrue();
  }

  @Test
  public void malformedOrFailedScriptResponseIsNotAcceptedAsProtection() {
    for (Object response : List.of(Map.of("status", "failed"), Map.of("status", "applied"))) {
      FakeDriver fakeDriver = new FakeDriver(true, response);
      RecordingSink sink = new RecordingSink();

      attacher("#secret-selector-canary", true, true, sink)
          .attach("Synthetic failure", fakeDriver.webDriver());

      assertThat(fakeDriver.screenshotCalls).isZero();
      assertThat(fakeDriver.calls).doesNotContain("screenshot", "page-source");
      assertThat(sink.text("Sensitive DOM Masking"))
          .isEqualTo("Sensitive DOM masking failed; screenshot and page source are omitted.");
    }
  }

  @Test
  public void configuredSelectorsOnNonJavascriptDriverOmitProtectedArtifacts() {
    FakeDriver fakeDriver = new FakeDriver(false, null);
    RecordingSink sink = new RecordingSink();

    attacher("#secret-selector-canary", true, true, sink)
        .attach("Synthetic failure", fakeDriver.webDriver());

    assertThat(fakeDriver.screenshotCalls).isZero();
    assertThat(fakeDriver.calls).doesNotContain("screenshot", "page-source", "mask");
    assertThat(sink.text("Sensitive DOM Masking"))
        .isEqualTo(
            "Sensitive DOM masking is unsupported by this driver; "
                + "screenshot and page source are omitted.");
    boolean sensitiveDetailsWereExcluded =
        !sink.allText().contains("secret-selector-canary")
            && !sink.allText().contains("page-source-canary")
            && !sink.allBytesAsText().contains("screenshot-canary");
    assertThat(sensitiveDetailsWereExcluded)
        .as("unsupported protection leaves sensitive details out of failure diagnostics")
        .isTrue();
  }

  @Test
  public void screenshotFailureAfterMaskingAddsSafeStatusAndContinuesOtherDiagnostics() {
    FakeDriver fakeDriver = new FakeDriver(true, Map.of("status", "applied", "selectedCount", 1));
    fakeDriver.failScreenshot = true;
    RecordingSink sink = new RecordingSink();

    attacher("#secret-canary", true, true, sink)
        .attach("Synthetic failure", fakeDriver.webDriver());

    assertThat(fakeDriver.screenshotCalls).isEqualTo(1);
    assertThat(sink.named("Screenshot")).hasSize(1);
    assertThat(sink.text("Screenshot")).isEqualTo("Screenshot capture failed after DOM masking.");
    assertThat(sink.named("Current URL")).hasSize(1);
    assertThat(sink.named("Page Source")).hasSize(1);
    assertThat(!sink.allText().contains("screenshot-error-canary"))
        .as("the raw screenshot exception stays out of the report")
        .isTrue();
  }

  @Test
  public void explicitlyBlankSelectorsAllowCaptureAndLabelTheUnmaskedBoundary() {
    FakeDriver fakeDriver = new FakeDriver(false, null);
    RecordingSink sink = new RecordingSink();

    attacher("", true, true, sink).attach("Synthetic failure", fakeDriver.webDriver());

    assertThat(fakeDriver.screenshotCalls).isEqualTo(1);
    assertThat(sink.named("Screenshot")).hasSize(1);
    assertThat(sink.text("Sensitive DOM Masking"))
        .contains("not configured", "unmasked");
  }

  @Test
  public void explicitBlankSelectorConfigurationIsAccepted() {
    Properties overrides = new Properties();
    overrides.setProperty("diagnostics.sensitive.dom.selectors", "");

    FrameworkConfig config =
        new ConfigLoader()
            .load(
                new ConfigSources(
                    Map.of(), overrides, ConfigSources.class.getClassLoader()));

    assertThat(config.sensitiveDomSelectors()).isEmpty();
  }

  private static DiagnosticsAttacher attacher(
      String selectors, boolean attachScreenshot, boolean attachPageSource, RecordingSink sink) {
    return new DiagnosticsAttacher(
        config(selectors, attachScreenshot, attachPageSource), sink);
  }

  private static FrameworkConfig config(
      String selectors, boolean attachScreenshot, boolean attachPageSource) {
    return (FrameworkConfig)
        Proxy.newProxyInstance(
            FrameworkConfig.class.getClassLoader(),
            new Class<?>[] {FrameworkConfig.class},
            (proxy, method, arguments) -> {
              return switch (method.getName()) {
                case "sensitiveDomSelectors" -> selectors;
                case "attachScreenshotsOnFailure" -> attachScreenshot;
                case "attachPageSourceOnFailure" -> attachPageSource;
                case "attachBrowserLogsOnFailure", "attachFrameworkLogsOnFailure",
                    "networkLogsEnabled" -> false;
                case "gridVideoBaseUrl" -> "";
                case "environment" -> "synthetic";
                case "browser" -> "Chrome";
                case "executionType" -> "local";
                case "headless" -> true;
                case "toString" -> "synthetic diagnostic configuration";
                default -> defaultValue(method.getReturnType());
              };
            });
  }

  private static Object defaultValue(Class<?> type) {
    if (type == boolean.class) return false;
    if (type == int.class) return 0;
    if (type == long.class) return 0L;
    if (type == double.class) return 0D;
    if (type == float.class) return 0F;
    if (type == short.class) return (short) 0;
    if (type == byte.class) return (byte) 0;
    if (type == char.class) return '\0';
    return null;
  }

  private static final class RecordingSink implements DiagnosticsAttacher.AttachmentSink {

    private final List<CapturedAttachment> attachments = new ArrayList<>();

    @Override
    public void addText(String name, String text) {
      attachments.add(
          new CapturedAttachment(
              name, "text/plain", text.getBytes(StandardCharsets.UTF_8), ".txt"));
    }

    @Override
    public void addText(String name, String contentType, String text, String fileExtension) {
      attachments.add(
          new CapturedAttachment(
              name, contentType, text.getBytes(StandardCharsets.UTF_8), fileExtension));
    }

    @Override
    public void addBytes(String name, String contentType, byte[] bytes, String fileExtension) {
      attachments.add(new CapturedAttachment(name, contentType, bytes.clone(), fileExtension));
    }

    private List<CapturedAttachment> named(String name) {
      return attachments.stream().filter(attachment -> attachment.name().equals(name)).toList();
    }

    private String text(String name) {
      return new String(named(name).getFirst().bytes(), StandardCharsets.UTF_8);
    }

    private String allText() {
      return attachments.stream()
          .filter(attachment -> attachment.contentType().startsWith("text/"))
          .map(attachment -> new String(attachment.bytes(), StandardCharsets.UTF_8))
          .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String allBytesAsText() {
      return attachments.stream()
          .map(attachment -> new String(attachment.bytes(), StandardCharsets.UTF_8))
          .collect(java.util.stream.Collectors.joining("\n"));
    }
  }

  private record CapturedAttachment(
      String name, String contentType, byte[] bytes, String fileExtension) {}

  private static final class FakeDriver {

    private final List<String> calls = new ArrayList<>();
    private final boolean supportsJavascript;
    private final Object scriptResult;
    private String pageSource = "<main>page-source-canary</main>";
    private int screenshotCalls;
    private boolean failScript;
    private boolean failScreenshot;

    private FakeDriver(boolean supportsJavascript, Object scriptResult) {
      this.supportsJavascript = supportsJavascript;
      this.scriptResult = scriptResult;
    }

    private WebDriver webDriver() {
      Class<?>[] interfaces =
          supportsJavascript
              ? new Class<?>[] {WebDriver.class, JavascriptExecutor.class, TakesScreenshot.class}
              : new Class<?>[] {WebDriver.class, TakesScreenshot.class};
      InvocationHandler handler = this::invoke;
      return (WebDriver)
          Proxy.newProxyInstance(WebDriver.class.getClassLoader(), interfaces, handler);
    }

    private Object invoke(Object proxy, java.lang.reflect.Method method, Object[] arguments) {
      return switch (method.getName()) {
        case "getCurrentUrl" -> "https://synthetic.invalid/form";
        case "getTitle" -> "Synthetic diagnostics page";
        case "getPageSource" -> {
          calls.add("page-source");
          yield pageSource;
        }
        case "executeScript" -> {
          calls.add("mask");
          if (failScript) throw new JavascriptException("script-error-canary");
          if (scriptResult instanceof Map<?, ?> result
              && "applied".equals(result.get("status"))) {
            pageSource = "<main><redacted></main>";
          }
          yield scriptResult;
        }
        case "getScreenshotAs" -> {
          calls.add("screenshot");
          screenshotCalls += 1;
          if (failScreenshot) throw new WebDriverException("screenshot-error-canary");
          yield SCREENSHOT_BYTES.clone();
        }
        case "toString" -> "synthetic diagnostics driver";
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == arguments[0]; // NOPMD - preserve proxy identity
        default -> defaultValue(method.getReturnType());
      };
    }
  }
}
