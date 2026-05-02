package common.driver;

import common.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WebDriverFactory {

    private static final ThreadLocal<WebDriver> webDriver = new InheritableThreadLocal<>();

    private WebDriverFactory() {  /*Singleton pattern*/ }

    public enum DriverType { CHROME, FIREFOX, EDGE, SAFARI }

    private static WebDriver getDriver() {
        FrameworkConfig config = ConfigFactory.getConfig();
        DriverType driverType = DriverType.valueOf(config.driverType().toUpperCase());
        String executionType = config.executionType().toLowerCase();
        
        WebDriver driver;
        if (executionType.equalsIgnoreCase("remote")) {
            String remoteUrl = config.remoteUrl();
            try {
                driver = getRemoteDriver(driverType, remoteUrl);
            } catch (Exception e) {
                log.error("Failed to create remote driver at {}, falling back to local", remoteUrl, e);
                driver = createLocalDriver(driverType);
            }
        } else {
            driver = createLocalDriver(driverType);
        }

        setTimeOut(driver);
        driver.manage().window().maximize();
        return driver;
    }

    private static WebDriver createLocalDriver(DriverType driverType) {
        log.info("Creating local WebDriver for type {}", driverType);
        return switch (driverType) {
            case CHROME -> new ChromeDriver(getChromeOptions());
            case FIREFOX -> new FirefoxDriver();
            case EDGE -> new EdgeDriver();
            case SAFARI -> new SafariDriver();
        };
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        // Create a map for profile preferences
        Map<String, Object> prefs = new HashMap<>();
        // 1. Disable the service that offers to save passwords
        prefs.put("credentials_enable_service", false);
        // 2. Disable the overall password manager
        prefs.put("profile.password_manager_enabled", false);
        // 3. Disable the specific "Data Breach" / Leak Detection popup (CRITICAL)
        prefs.put("profile.password_manager_leak_detection", false);
        chromeOptions.setExperimentalOption("prefs", prefs);
        
        // Add headless mode for CI/Docker environments
        if (ConfigFactory.getConfig().headless()) {
            chromeOptions.addArguments("--headless=new");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--window-size=1920,1080");
        }
        
        return chromeOptions;
    }

    public static void cleanUpDriver() {
            try {
                webDriver.get().quit();
                webDriver.remove();
            } catch (Exception e) {
                log.error("Error quitting driver", e);
            }
    }

    private static WebDriver getRemoteDriver(DriverType driverType, String remoteUrl) throws MalformedURLException, URISyntaxException {
        log.info("Creating remote WebDriver for type {} at {}", driverType, remoteUrl);
        final URI hubUrl = new URI(remoteUrl);
        MutableCapabilities capabilities = switch (driverType) {
            case CHROME -> getChromeOptions();
            case FIREFOX -> new org.openqa.selenium.firefox.FirefoxOptions();
            case EDGE -> new org.openqa.selenium.edge.EdgeOptions();
            case SAFARI -> new org.openqa.selenium.safari.SafariOptions();
        };
        return new RemoteWebDriver(hubUrl.toURL(), capabilities);
    }

    private static void setTimeOut(WebDriver driver) {
        Duration timeout = Duration.ofSeconds(30);
        driver.manage().timeouts().implicitlyWait(timeout);
        driver.manage().timeouts().pageLoadTimeout(timeout);
        driver.manage().timeouts().scriptTimeout(timeout);
    }

   public static void initThreadLocalDriver() {
        WebDriver driver = null;
        try {
            driver = getDriver();
        }  catch (Exception e) {
           log.error("Error initializing driver", e);
        }
       webDriver.set(driver);
   }

   public static WebDriver getThreadLocalWebDriver() {
        return  webDriver.get();
   }
}
