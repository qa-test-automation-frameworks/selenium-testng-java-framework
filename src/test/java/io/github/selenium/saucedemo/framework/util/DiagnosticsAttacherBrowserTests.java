package io.github.selenium.saucedemo.framework.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.selenium.saucedemo.framework.config.ConfigFactory;
import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import io.github.selenium.saucedemo.tests.BaseTestCase;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/** Runs one synthetic protected capture on the existing remote Chrome runner. */
public class DiagnosticsAttacherBrowserTests extends BaseTestCase {

  @Test
  public void chromeCapturesOnlyAfterMaskingSyntheticSensitiveDom() throws IOException {
    WebDriver driver = getDriver();
    driver.get("about:blank");
    ((JavascriptExecutor) driver)
        .executeScript(
            "document.body.innerHTML = arguments[0];",
            "<main>"
                + "<div id='sensitive-text' data-sensitive='true'>"
                + "synthetic-dom-canary</div>"
                + "<input id='sensitive-input' data-sensitive='true' "
                + "value='synthetic-input-canary'>"
                + "</main>");

    RecordingSink sink = new RecordingSink();
    new DiagnosticsAttacher(syntheticConfig(), sink).attach("Synthetic DOM control", driver);

    boolean domWasMasked =
        "<redacted>".equals(driver.findElement(By.id("sensitive-text")).getText())
            && "<redacted>"
                .equals(driver.findElement(By.id("sensitive-input")).getDomProperty("value"));
    assertThat(domWasMasked).as("the real Chrome DOM was masked before capture").isTrue();
    assertThat(sink.screenshot).isNotNull().isNotEmpty();
    assertThat(ImageIO.read(new ByteArrayInputStream(sink.screenshot))).isNotNull();
    assertThat(sink.text("Sensitive DOM Masking")).contains("masking applied");
    boolean textAttachmentsWereMasked =
        !sink.allText().contains("synthetic-dom-canary")
            && !sink.allText().contains("synthetic-input-canary");
    assertThat(textAttachmentsWereMasked)
        .as("synthetic sensitive values stay out of captured text artifacts")
        .isTrue();
  }

  private static FrameworkConfig syntheticConfig() {
    FrameworkConfig delegate = ConfigFactory.getConfig();
    InvocationHandler handler =
        (proxy, method, arguments) -> {
          return switch (method.getName()) {
            case "sensitiveDomSelectors" -> "[data-sensitive='true']";
            case "attachScreenshotsOnFailure", "attachPageSourceOnFailure" -> true;
            case "attachBrowserLogsOnFailure", "attachFrameworkLogsOnFailure",
                "networkLogsEnabled" -> false;
            case "gridVideoBaseUrl" -> "";
            default -> method.invoke(delegate, arguments);
          };
        };
    return (FrameworkConfig)
        Proxy.newProxyInstance(
            FrameworkConfig.class.getClassLoader(), new Class<?>[] {FrameworkConfig.class}, handler);
  }

  private static final class RecordingSink implements DiagnosticsAttacher.AttachmentSink {

    private final List<CapturedAttachment> attachments = new ArrayList<>();
    private byte[] screenshot;

    @Override
    public void addText(String name, String text) {
      attachments.add(new CapturedAttachment(name, text));
    }

    @Override
    public void addText(String name, String contentType, String text, String fileExtension) {
      attachments.add(new CapturedAttachment(name, text));
    }

    @Override
    public void addBytes(String name, String contentType, byte[] bytes, String fileExtension) {
      if (name.equals("Screenshot")) screenshot = bytes.clone();
    }

    private String text(String name) {
      return attachments.stream()
          .filter(attachment -> attachment.name().equals(name))
          .map(CapturedAttachment::text)
          .findFirst()
          .orElse("");
    }

    private String allText() {
      return attachments.stream()
          .map(CapturedAttachment::text)
          .collect(java.util.stream.Collectors.joining("\n"));
    }
  }

  private record CapturedAttachment(String name, String text) {}
}
