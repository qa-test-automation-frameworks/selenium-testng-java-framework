package io.github.prayag.saucedemo.framework.driver;

import io.github.prayag.saucedemo.framework.config.FrameworkConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

/** Builds browser-specific options without launching browser instances. */
public final class BrowserOptionsFactory {

  public ChromeOptions chromeOptions(FrameworkConfig config) {
    ChromeOptions chromeOptions = new ChromeOptions();
    Map<String, Object> prefs = new HashMap<>();
    prefs.put("credentials_enable_service", false);
    prefs.put("profile.password_manager_enabled", false);
    prefs.put("profile.password_manager_leak_detection", false);
    chromeOptions.setExperimentalOption("prefs", prefs);
    applyNetworkLogging(chromeOptions, config);

    if (config.headless()) {
      chromeOptions.addArguments("--headless=new");
      chromeOptions.addArguments("--disable-gpu");
      chromeOptions.addArguments("--no-sandbox");
      chromeOptions.addArguments("--disable-dev-shm-usage");
      chromeOptions.addArguments(windowSizeArgument(config));
    }

    return chromeOptions;
  }

  public FirefoxOptions firefoxOptions(FrameworkConfig config) {
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    if (config.headless()) {
      firefoxOptions.addArguments("-headless");
      firefoxOptions.addArguments("--width=" + config.viewportWidth());
      firefoxOptions.addArguments("--height=" + config.viewportHeight());
    }
    return firefoxOptions;
  }

  public EdgeOptions edgeOptions(FrameworkConfig config) {
    EdgeOptions edgeOptions = new EdgeOptions();
    applyNetworkLogging(edgeOptions, config);
    if (config.headless()) {
      edgeOptions.addArguments("--headless=new");
      edgeOptions.addArguments("--disable-gpu");
      edgeOptions.addArguments("--no-sandbox");
      edgeOptions.addArguments("--disable-dev-shm-usage");
      edgeOptions.addArguments(windowSizeArgument(config));
    }
    return edgeOptions;
  }

  private void applyNetworkLogging(MutableCapabilities capabilities, FrameworkConfig config) {
    if (!config.networkLogsEnabled()) {
      return;
    }
    LoggingPreferences logs = new LoggingPreferences();
    logs.enable(LogType.PERFORMANCE, Level.ALL);
    capabilities.setCapability("goog:loggingPrefs", logs);
  }

  private String windowSizeArgument(FrameworkConfig config) {
    return String.format("--window-size=%d,%d", config.viewportWidth(), config.viewportHeight());
  }
}
