package com.example.saucedemo.framework.driver;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.config.ExecutionType;
import com.example.saucedemo.framework.config.FrameworkConfig;
import com.example.saucedemo.framework.config.FrameworkConfigurationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * Factory class for managing WebDriver instances. Utilizes ThreadLocal to ensure thread-safety
 * during parallel execution.
 */
@Slf4j
public class WebDriverFactory {

  private static final ThreadLocal<WebDriver> WEB_DRIVER = new ThreadLocal<>();

  private WebDriverFactory() {
    /*Singleton pattern*/
  }

  /** Supported browser types. */
  public enum DriverType {
    CHROME,
    FIREFOX,
    EDGE,
    SAFARI
  }

  /**
   * Internal method to create a new WebDriver instance based on configuration.
   *
   * @return A configured WebDriver instance.
   */
  private static WebDriver getDriver(FrameworkConfig config) {
    DriverType driverType = parseDriverType(config.browser());

    WebDriver driver =
        switch (ExecutionType.from(config.executionType())) {
          case REMOTE -> {
            String remoteUrl = config.remoteUrl();
            try {
              yield getRemoteDriver(driverType, remoteUrl, config);
            } catch (MalformedURLException | URISyntaxException e) {
              throw new FrameworkConfigurationException("Invalid remote URL: " + remoteUrl, e);
            }
          }
          case LOCAL -> createLocalDriver(driverType, config);
        };

    setTimeOut(driver, config);
    applyWindowSize(driver, config);
    return driver;
  }

  /**
   * Creates a local WebDriver instance.
   *
   * @param driverType The type of browser to create.
   * @return A local WebDriver instance.
   */
  private static WebDriver createLocalDriver(DriverType driverType, FrameworkConfig config) {
    log.info("Creating local WebDriver for type {}", driverType);
    return switch (driverType) {
      case CHROME -> new ChromeDriver(getChromeOptions(config));
      case FIREFOX -> new FirefoxDriver(getFirefoxOptions(config));
      case EDGE -> new EdgeDriver(getEdgeOptions(config));
      case SAFARI -> {
        if (config.headless()) {
          throw new FrameworkConfigurationException(
              "Safari is supported only for local headed macOS runs");
        }
        yield new SafariDriver();
      }
    };
  }

  /**
   * Configures Chrome-specific options, including headless mode and security preferences.
   *
   * @return Configured ChromeOptions.
   */
  private static ChromeOptions getChromeOptions(FrameworkConfig config) {
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

  private static FirefoxOptions getFirefoxOptions(FrameworkConfig config) {
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    if (config.headless()) {
      firefoxOptions.addArguments("-headless");
      firefoxOptions.addArguments("--width=" + config.viewportWidth());
      firefoxOptions.addArguments("--height=" + config.viewportHeight());
    }
    return firefoxOptions;
  }

  private static EdgeOptions getEdgeOptions(FrameworkConfig config) {
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

  /** Quits the current thread's WebDriver and removes it from ThreadLocal storage. */
  public static void cleanUpDriver() {
    try {
      if (WEB_DRIVER.get() != null) {
        WEB_DRIVER.get().quit();
      }
    } catch (Exception e) {
      log.error("Error quitting driver", e);
    } finally {
      WEB_DRIVER.remove();
      log.debug("ThreadLocal WebDriver reference cleared.");
    }
  }

  /**
   * Creates a remote WebDriver instance for execution on Selenium Grid.
   *
   * @param driverType The type of browser.
   * @param remoteUrl The Selenium Hub URL.
   * @return A RemoteWebDriver instance.
   */
  private static WebDriver getRemoteDriver(
      DriverType driverType, String remoteUrl, FrameworkConfig config)
      throws MalformedURLException, URISyntaxException {
    log.info("Creating remote WebDriver for type {} at {}", driverType, remoteUrl);
    final URI hubUrl = new URI(remoteUrl);
    MutableCapabilities capabilities =
        switch (driverType) {
          case CHROME -> getChromeOptions(config);
          case FIREFOX -> getFirefoxOptions(config);
          case EDGE -> getEdgeOptions(config);
          case SAFARI ->
              throw new FrameworkConfigurationException(
                  "Safari is supported only for local headed macOS runs");
        };
    return new RemoteWebDriver(hubUrl.toURL(), capabilities);
  }

  private static String windowSizeArgument(FrameworkConfig config) {
    return String.format("--window-size=%d,%d", config.viewportWidth(), config.viewportHeight());
  }

  private static void applyWindowSize(WebDriver driver, FrameworkConfig config) {
    if (config.headless()) {
      return;
    }
    if (!config.headless() && config.maximizeWindow()) {
      driver.manage().window().maximize();
      return;
    }
    driver
        .manage()
        .window()
        .setSize(new Dimension(config.viewportWidth(), config.viewportHeight()));
  }

  private static void applyNetworkLogging(
      MutableCapabilities capabilities, FrameworkConfig config) {
    if (!config.networkLogsEnabled()) {
      return;
    }
    LoggingPreferences logs = new LoggingPreferences();
    logs.enable(LogType.PERFORMANCE, Level.ALL);
    capabilities.setCapability("goog:loggingPrefs", logs);
  }

  /**
   * Sets default timeouts for the driver.
   *
   * @param driver The WebDriver instance to configure.
   */
  private static void setTimeOut(WebDriver driver, FrameworkConfig config) {
    driver.manage().timeouts().implicitlyWait(Duration.ZERO);
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.pageLoadTimeoutSeconds()));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(config.scriptTimeoutSeconds()));
  }

  private static DriverType parseDriverType(String browser) {
    try {
      return DriverType.valueOf(browser.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format(
              "Unsupported browser '%s'. Supported browsers: CHROME, FIREFOX, EDGE, SAFARI",
              browser),
          e);
    }
  }

  /** Initializes a new driver for the current thread. */
  public static void initThreadLocalDriver() {
    initThreadLocalDriver(ConfigFactory.getConfig());
  }

  public static void initThreadLocalDriver(FrameworkConfig config) {
    try {
      WEB_DRIVER.set(getDriver(config));
    } catch (Exception e) {
      log.error("Error initializing driver", e);
      throw new IllegalStateException("Failed to initialize WebDriver", e);
    }
  }

  /**
   * Retrieves the current thread's WebDriver instance.
   *
   * @return The active WebDriver for this thread.
   */
  public static WebDriver getThreadLocalWebDriver() {
    return WEB_DRIVER.get();
  }
}
