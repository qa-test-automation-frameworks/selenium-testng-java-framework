package io.github.selenium.saucedemo.framework.util;

import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.HasBiDi;
import org.openqa.selenium.bidi.module.LogInspector;
import org.openqa.selenium.bidi.module.Network;

/**
 * Captures browser diagnostics using WebDriver BiDi where supported and keeps them available for
 * failure reporting. Legacy Selenium log APIs remain the fallback in the listener.
 */
@Slf4j
public final class DiagnosticsCollector {

  private static final ThreadLocal<BiDiSession> SESSION = new ThreadLocal<>();

  private DiagnosticsCollector() {}

  public static void start(WebDriver driver, FrameworkConfig config) {
    stop();
    if (!(driver instanceof HasBiDi)) {
      log.debug(
          "BiDi diagnostics are not available for driver type {}", driver.getClass().getName());
      return;
    }

    try {
      SESSION.set(BiDiSession.create(driver, config.networkLogsEnabled()));
      log.debug("BiDi diagnostics collector initialized for {}", driver.getClass().getSimpleName());
    } catch (RuntimeException e) {
      log.info("BiDi diagnostics are unavailable for this session; using legacy log collection");
      log.debug("BiDi diagnostics initialization failure", e);
      SESSION.remove();
    }
  }

  public static boolean isBiDiActive() {
    return SESSION.get() != null;
  }

  @SuppressWarnings("PMD.CloseResource")
  public static List<String> consoleLogs() {
    BiDiSession session = SESSION.get();
    if (session == null) {
      return List.of();
    }
    return session.snapshotConsoleLogs();
  }

  @SuppressWarnings("PMD.CloseResource")
  public static List<String> networkLogs() {
    BiDiSession session = SESSION.get();
    if (session == null) {
      return List.of();
    }
    return session.snapshotNetworkLogs();
  }

  @SuppressWarnings("PMD.CloseResource")
  public static void stop() {
    BiDiSession session = SESSION.get();
    if (session == null) {
      return;
    }
    try {
      session.close();
    } catch (RuntimeException e) {
      log.warn("Failed to close BiDi diagnostics collector cleanly", e);
    } finally {
      SESSION.remove();
    }
  }

  private record BiDiSession(
      LogInspector logInspector,
      Network network,
      List<String> consoleLogs,
      List<String> networkLogs)
      implements AutoCloseable {

    @SuppressWarnings("PMD.CloseResource")
    private static BiDiSession create(WebDriver driver, boolean networkLoggingEnabled) {
      List<String> consoleLogs = Collections.synchronizedList(new ArrayList<>());
      LogInspector logInspector = new LogInspector(driver);
      logInspector.onConsoleEntry(entry -> consoleLogs.add(entry.toString()));
      logInspector.onJavaScriptLog(entry -> consoleLogs.add(entry.toString()));

      List<String> networkLogs = Collections.synchronizedList(new ArrayList<>());
      Network network = null;
      if (networkLoggingEnabled) {
        network = new Network(driver);
        Network activeNetwork = network;
        activeNetwork.onBeforeRequestSent(entry -> networkLogs.add(entry.toString()));
        activeNetwork.onResponseCompleted(entry -> networkLogs.add(entry.toString()));
        activeNetwork.onFetchError(entry -> networkLogs.add(entry.toString()));
      }
      return new BiDiSession(logInspector, network, consoleLogs, networkLogs);
    }

    @Override
    public void close() {
      logInspector.close();
      if (network != null) {
        network.close();
      }
    }

    private List<String> snapshotConsoleLogs() {
      return List.copyOf(consoleLogs);
    }

    private List<String> snapshotNetworkLogs() {
      return List.copyOf(networkLogs);
    }
  }
}
