package io.github.selenium.saucedemo.framework.driver;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.selenium.saucedemo.framework.config.ConfigFactory;
import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

/** One manual hosted worker-interruption and exact-session recovery control. */
public class GridRecoveryDrillTests {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String PROBE_HTML =
      "<!doctype html><html><head><title>grid-recovery-probe</title></head>"
          + "<body><div aria-label=\"masked-canary\">Protected element</div></body></html>";

  @Test(timeOut = 390_000)
  public void runsOneBoundedGridRecoveryWorkerMode() throws Exception {
    String mode = requiredProperty("grid.recovery.mode");
    String nonce = requiredProperty("grid.recovery.nonce");
    FrameworkConfig config = ConfigFactory.getConfig();
    Map<String, Object> observedCapabilities = new LinkedHashMap<>();
    boolean cleanupCalled;
    boolean passed = false;
    String failureCategory = "UNKNOWN";

    try {
      WebDriverFactory.initThreadLocalDriver(config);
      WebDriver driver = WebDriverFactory.getThreadLocalWebDriver();
      assertThat(driver).isNotNull();
      driver.get(probeUrl());
      assertThat(driver.getTitle()).isEqualTo("grid-recovery-probe");
      assertThat(driver.findElement(By.cssSelector("[aria-label='masked-canary']")).getText())
          .isEqualTo("Protected element");

      if (!(driver instanceof RemoteWebDriver remoteDriver)) {
        throw new IllegalStateException("The hosted drill did not create a remote driver.");
      }
      Capabilities capabilities = remoteDriver.getCapabilities();
      observedCapabilities = safeCapabilities(capabilities);
      assertThat(observedCapabilities.get("browserName")).isEqualTo("chrome");

      if ("interrupted".equals(mode)) {
        writePrivateReceipt(
            Path.of(requiredProperty("grid.recovery.receipt")),
            remoteDriver,
            nonce,
            requiredProperty("grid.recovery.source.sha"),
            config.remoteUrl());
        System.out.println("GRID_RECOVERY_READY");
        // The owning Node harness kills this whole child process group after it sees the receipt
        // and independently observes this exact session in the pinned Grid inventory.
        Thread.sleep(300_000);
        throw new IllegalStateException("The worker reached its bounded wait without interruption.");
      }

      if (!"control".equals(mode)) {
        throw new IllegalArgumentException("Unsupported Grid recovery worker mode.");
      }
      passed = true;
    } catch (AssertionError failure) {
      failureCategory = "APPLICATION_ASSERTION";
      throw failure;
    } catch (Exception failure) {
      failureCategory =
          failure instanceof IllegalArgumentException ? "UNKNOWN" : "APPLICATION_ASSERTION";
      throw failure;
    } finally {
      if ("control".equals(mode)) {
        WebDriverFactory.cleanUpDriver();
        cleanupCalled = true;
        writeControlResult(
            Path.of(requiredProperty("grid.recovery.control")),
            passed,
            cleanupCalled,
            observedCapabilities,
            failureCategory);
      }
    }
  }

  private static String probeUrl() {
    String encoded = Base64.getEncoder().encodeToString(PROBE_HTML.getBytes(StandardCharsets.UTF_8));
    return "data:text/html;charset=utf-8;base64," + encoded;
  }

  private static Map<String, Object> safeCapabilities(Capabilities capabilities) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("browserName", safeText(capabilities.getBrowserName(), 32));
    result.put("browserVersion", safeText(capabilities.getBrowserVersion(), 32));
    Object platform = capabilities.getCapability("platformName");
    result.put("platformName", safeText(platform == null ? "" : platform.toString(), 32));
    return result;
  }

  private static void writePrivateReceipt(
      Path receiptPath,
      RemoteWebDriver driver,
      String nonce,
      String sourceSha,
      String remoteUrl)
      throws IOException {
    Path directory =
        Objects.requireNonNull(receiptPath.getParent(), "Receipt path must have a parent directory.");
    Files.createDirectories(directory);
    setPermissions(
        directory,
        EnumSet.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE));
    Map<String, Object> receipt = new LinkedHashMap<>();
    receipt.put("schemaVersion", 1);
    receipt.put("runNonce", nonce);
    receipt.put("sourceSha", sourceSha);
    java.net.URI remoteUri = java.net.URI.create(remoteUrl);
    receipt.put("gridOrigin", remoteUri.getScheme() + "://" + remoteUri.getAuthority());
    String sessionId =
        Objects.requireNonNull(driver.getSessionId(), "Created Grid session must have an ID.")
            .toString();
    receipt.put("sessionId", sessionId);
    receipt.put("createdAt", Instant.now().toString());
    receipt.put("requestedCapabilities", Map.of(
        "browserName", "chrome",
        "se:name", "grid-recovery:" + nonce));
    receipt.put("observedCapabilities", safeCapabilities(driver.getCapabilities()));

    Path temporary = Files.createTempFile(directory, ".owned-session-", ".tmp");
    try {
      setPermissions(
          temporary,
          EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
      byte[] bytes = JSON.writeValueAsBytes(receipt);
      try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
        channel.write(java.nio.ByteBuffer.wrap(bytes));
        channel.force(true);
      }
      try {
        Files.move(temporary, receiptPath, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException unsupported) {
        throw new IOException("Private receipt storage does not support atomic rename.", unsupported);
      }
      setPermissions(receiptPath, EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  private static void writeControlResult(
      Path path,
      boolean passed,
      boolean cleanupCalled,
      Map<String, Object> observedCapabilities,
      String failureCategory)
      throws IOException {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("schemaVersion", 1);
    result.put("status", passed ? "passed" : "failed");
    result.put("frameworkCleanupCalled", cleanupCalled);
    result.put("requestedCapabilities", Map.of("browserName", "chrome"));
    result.put("observedCapabilities", observedCapabilities);
    result.put("failureCategory", failureCategory);
    Path parent =
        Objects.requireNonNull(path.getParent(), "Control result path must have a parent directory.");
    Path temporary = parent.resolve(path.getFileName() + ".tmp");
    Files.createDirectories(parent);
    Files.write(
        temporary,
        JSON.writeValueAsBytes(result),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
    setPermissions(
        temporary,
        EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
    try {
      Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException unsupported) {
      Files.deleteIfExists(temporary);
      throw new IOException("Control result storage does not support atomic rename.", unsupported);
    }
  }

  private static void setPermissions(Path path, Set<PosixFilePermission> permissions)
      throws IOException {
    Files.setPosixFilePermissions(path, permissions);
  }

  private static String requiredProperty(String name) {
    String value = System.getProperty(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required Grid recovery property: " + name);
    }
    return value;
  }

  private static String safeText(String value, int maxLength) {
    if (value == null) return "";
    String sanitized = value.replaceAll("[^A-Za-z0-9._ -]", "");
    return sanitized.substring(0, Math.min(sanitized.length(), maxLength));
  }
}
