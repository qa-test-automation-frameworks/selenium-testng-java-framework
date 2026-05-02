package common.driver;

import common.config.ConfigFactory;
import common.config.FrameworkConfig;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

/**
 * Factory class for managing WebDriver instances. Utilizes ThreadLocal to ensure thread-safety
 * during parallel execution.
 */
@Slf4j
public class WebDriverFactory {

  private static final ThreadLocal<WebDriver> webDriver = new ThreadLocal<>();

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
  private static WebDriver getDriver() {
    FrameworkConfig config = ConfigFactory.getConfig();
    DriverType driverType = parseDriverType(config.browser());
    String executionType = config.executionType().toLowerCase();

    WebDriver driver;
    if (executionType.equalsIgnoreCase("remote")) {
      String remoteUrl = config.remoteUrl();
      try {
        driver = getRemoteDriver(driverType, remoteUrl);
      } catch (MalformedURLException | URISyntaxException e) {
        throw new RuntimeException("Invalid remote URL: " + remoteUrl, e);
      }
    } else {
      driver = createLocalDriver(driverType);
    }

    setTimeOut(driver);
    driver.manage().window().maximize();
    return driver;
  }

  /**
   * Creates a local WebDriver instance.
   *
   * @param driverType The type of browser to create.
   * @return A local WebDriver instance.
   */
  private static WebDriver createLocalDriver(DriverType driverType) {
    log.info("Creating local WebDriver for type {}", driverType);
    return switch (driverType) {
      case CHROME -> new ChromeDriver(getChromeOptions());
      case FIREFOX -> new FirefoxDriver(getFirefoxOptions());
      case EDGE -> new EdgeDriver(getEdgeOptions());
      case SAFARI -> new SafariDriver();
    };
  }

  /**
   * Configures Chrome-specific options, including headless mode and security preferences.
   *
   * @return Configured ChromeOptions.
   */
  private static ChromeOptions getChromeOptions() {
    ChromeOptions chromeOptions = new ChromeOptions();
    Map<String, Object> prefs = new HashMap<>();
    prefs.put("credentials_enable_service", false);
    prefs.put("profile.password_manager_enabled", false);
    prefs.put("profile.password_manager_leak_detection", false);
    chromeOptions.setExperimentalOption("prefs", prefs);

    if (ConfigFactory.getConfig().headless()) {
      chromeOptions.addArguments("--headless=new");
      chromeOptions.addArguments("--disable-gpu");
      chromeOptions.addArguments("--no-sandbox");
      chromeOptions.addArguments("--disable-dev-shm-usage");
      chromeOptions.addArguments("--window-size=1920,1080");
    }

    return chromeOptions;
  }

  private static FirefoxOptions getFirefoxOptions() {
    FirefoxOptions firefoxOptions = new FirefoxOptions();
    if (ConfigFactory.getConfig().headless()) {
      firefoxOptions.addArguments("-headless");
      firefoxOptions.addArguments("--width=1920");
      firefoxOptions.addArguments("--height=1080");
    }
    return firefoxOptions;
  }

  private static EdgeOptions getEdgeOptions() {
    EdgeOptions edgeOptions = new EdgeOptions();
    if (ConfigFactory.getConfig().headless()) {
      edgeOptions.addArguments("--headless=new");
      edgeOptions.addArguments("--disable-gpu");
      edgeOptions.addArguments("--no-sandbox");
      edgeOptions.addArguments("--disable-dev-shm-usage");
      edgeOptions.addArguments("--window-size=1920,1080");
    }
    return edgeOptions;
  }

  /** Quits the current thread's WebDriver and removes it from ThreadLocal storage. */
  public static void cleanUpDriver() {
    try {
      if (webDriver.get() != null) {
        webDriver.get().quit();
      }
      webDriver.remove();
    } catch (Exception e) {
      log.error("Error quitting driver", e);
    }
  }

  /**
   * Creates a remote WebDriver instance for execution on Selenium Grid.
   *
   * @param driverType The type of browser.
   * @param remoteUrl The Selenium Hub URL.
   * @return A RemoteWebDriver instance.
   */
  private static WebDriver getRemoteDriver(DriverType driverType, String remoteUrl)
      throws MalformedURLException, URISyntaxException {
    log.info("Creating remote WebDriver for type {} at {}", driverType, remoteUrl);
    final URI hubUrl = new URI(remoteUrl);
    MutableCapabilities capabilities =
        switch (driverType) {
          case CHROME -> getChromeOptions();
          case FIREFOX -> getFirefoxOptions();
          case EDGE -> getEdgeOptions();
          case SAFARI -> new SafariOptions();
        };
    return new RemoteWebDriver(hubUrl.toURL(), capabilities);
  }

  /**
   * Sets default timeouts for the driver.
   *
   * @param driver The WebDriver instance to configure.
   */
  private static void setTimeOut(WebDriver driver) {
    FrameworkConfig config = ConfigFactory.getConfig();
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
    try {
      webDriver.set(getDriver());
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
    return webDriver.get();
  }
}
