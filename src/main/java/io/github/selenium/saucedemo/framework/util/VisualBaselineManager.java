package io.github.selenium.saucedemo.framework.util;

import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Manages an opt-in visual baseline scaffold by comparing screenshot hashes to approved baselines.
 *
 * <p>This intentionally keeps the first version lightweight: it compares whole-image SHA-256 hashes
 * instead of introducing a heavier pixel-diff dependency into the default framework runtime.
 */
public final class VisualBaselineManager {

  private final Path baselineDirectory;
  private final boolean autoApprove;

  public VisualBaselineManager(FrameworkConfig config) {
    this(Path.of(config.visualBaselineDir()), config.visualAutoApprove());
  }

  public VisualBaselineManager(Path baselineDirectory, boolean autoApprove) {
    this.baselineDirectory = Objects.requireNonNull(baselineDirectory, "baselineDirectory");
    this.autoApprove = autoApprove;
  }

  public VisualComparisonResult compare(String baselineName, byte[] screenshotBytes) {
    Objects.requireNonNull(baselineName, "baselineName");
    Objects.requireNonNull(screenshotBytes, "screenshotBytes");

    Path baselinePath = baselineDirectory.resolve(baselineName + ".sha256");
    String actualHash = sha256Hex(screenshotBytes);

    try {
      if (Files.exists(baselinePath)) {
        String expectedHash = Files.readString(baselinePath, StandardCharsets.UTF_8).trim();
        if (expectedHash.equals(actualHash)) {
          return new VisualComparisonResult(
              true,
              false,
              baselinePath,
              expectedHash,
              actualHash,
              "Visual baseline matched approved screenshot hash.");
        }
        if (autoApprove) {
          writeBaseline(baselinePath, actualHash);
          return new VisualComparisonResult(
              true,
              true,
              baselinePath,
              expectedHash,
              actualHash,
              "Visual baseline hash changed and was auto-approved.");
        }
        return new VisualComparisonResult(
            false,
            false,
            baselinePath,
            expectedHash,
            actualHash,
            "Visual baseline hash mismatch. Re-run with -Dvisual.auto.approve=true after reviewing the screenshot.");
      }

      if (autoApprove) {
        writeBaseline(baselinePath, actualHash);
        return new VisualComparisonResult(
            true,
            true,
            baselinePath,
            null,
            actualHash,
            "Visual baseline was created and approved automatically.");
      }

      return new VisualComparisonResult(
          false,
          false,
          baselinePath,
          null,
          actualHash,
          "Visual baseline is missing. Run once with -Dvisual.auto.approve=true to approve a reviewed screenshot.");
    } catch (IOException e) {
      throw new IllegalStateException(
          "Unable to read or write visual baseline: " + baselinePath, e);
    }
  }

  private void writeBaseline(Path baselinePath, String actualHash) throws IOException {
    Path parent = baselinePath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.writeString(baselinePath, actualHash + System.lineSeparator(), StandardCharsets.UTF_8);
  }

  private String sha256Hex(byte[] screenshotBytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(screenshotBytes));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 digest is not available", e);
    }
  }

  public record VisualComparisonResult(
      boolean passed,
      boolean approved,
      Path baselinePath,
      String expectedHash,
      String actualHash,
      String message) {}
}
