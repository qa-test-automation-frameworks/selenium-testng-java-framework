package com.example.saucedemo.framework.driver;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.config.ExecutionType;
import com.example.saucedemo.framework.config.FrameworkConfig;
import com.example.saucedemo.framework.config.FrameworkConfigurationException;
import com.example.saucedemo.framework.util.DiagnosticsCollector;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 * Factory class for managing WebDriver instances. Utilizes ThreadLocal to ensure thread-safety
 * during parallel execution.
 */
@Slf4j
public class WebDriverFactory {

  private static final ThreadLocal<WebDriver> WEB_DRIVER = new ThreadLocal<>();
  private static final BrowserOptionsFactory BROWSER_OPTIONS_FACTORY = new BrowserOptionsFactory();

  private WebDriverFactory() {
    /* Utility class */
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
      case CHROME -> new ChromeDriver(BROWSER_OPTIONS_FACTORY.chromeOptions(config));
      case FIREFOX -> new FirefoxDriver(BROWSER_OPTIONS_FACTORY.firefoxOptions(config));
      case EDGE -> new EdgeDriver(BROWSER_OPTIONS_FACTORY.edgeOptions(config));
      case SAFARI -> {
        if (config.headless() || !isMacOs()) {
          throw new FrameworkConfigurationException(
              "Safari requires local headed execution on macOS with remote automation enabled");
        }
        yield new SafariDriver();
      }
    };
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
      DiagnosticsCollector.stop();
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
          case CHROME -> BROWSER_OPTIONS_FACTORY.chromeOptions(config);
          case FIREFOX -> BROWSER_OPTIONS_FACTORY.firefoxOptions(config);
          case EDGE -> BROWSER_OPTIONS_FACTORY.edgeOptions(config);
          case SAFARI ->
              throw new FrameworkConfigurationException(
                  "Safari requires local headed execution on macOS with remote automation enabled");
        };
    return new RemoteWebDriver(hubUrl.toURL(), capabilities);
  }

  private static void applyWindowSize(WebDriver driver, FrameworkConfig config) {
    if (config.headless()) {
      return;
    }
    if (config.maximizeWindow()) {
      driver.manage().window().maximize();
      return;
    }
    driver
        .manage()
        .window()
        .setSize(new Dimension(config.viewportWidth(), config.viewportHeight()));
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
      if (WEB_DRIVER.get() != null) {
        log.warn(
            "WebDriver was already initialized for thread {}; cleaning it up first",
            Thread.currentThread().getName());
        cleanUpDriver();
      }
      WebDriver driver = getDriver(config);
      DiagnosticsCollector.start(driver, config);
      WEB_DRIVER.set(driver);
    } catch (Exception e) {
      log.error("Error initializing driver", e);
      throw new IllegalStateException("Failed to initialize WebDriver", e);
    }
  }

  private static boolean isMacOs() {
    return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac");
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
