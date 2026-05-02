package tests;

import common.config.ConfigFactory;
import common.config.FrameworkConfig;
import common.driver.WebDriverFactory;
import common.listener.FrameworkListener;
import common.pageobject.LoginPage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

@Slf4j
@Listeners(FrameworkListener.class)
public abstract class BaseTestCase {

  @BeforeSuite(alwaysRun = true)
  public void beforeSuite() {
    log.info("BeforeSuite: Generating Allure environment information");
    generateAllureEnvironmentFile();
  }

  protected WebDriver getDriver() {
    return WebDriverFactory.getThreadLocalWebDriver();
  }

  protected void login() {
    log.info("Performing global login for test setup");
    new LoginPage(getDriver())
        .login(
            ConfigFactory.getConfig().appUrl(),
            ConfigFactory.getConfig().appUsername(),
            ConfigFactory.getConfig().appPassword());
  }

  protected void quitWebDriver() {
    log.info("Cleaning up WebDriver for thread: {}", Thread.currentThread().getName());
    WebDriverFactory.cleanUpDriver();
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod(ITestResult result) {
    log.info("BeforeMethod: Initializing driver for test: {}", result.getMethod().getMethodName());
    WebDriverFactory.initThreadLocalDriver();
  }

  @AfterMethod(alwaysRun = true)
  public void afterMethod(ITestResult result) {
    log.info("AfterMethod: Tearing down driver for test: {}", result.getMethod().getMethodName());
    quitWebDriver();
  }

  private void generateAllureEnvironmentFile() {
    FrameworkConfig config = ConfigFactory.getConfig();
    Properties props = new Properties();
    props.setProperty("Environment", System.getProperty("env", "qa").toUpperCase());
    props.setProperty("App URL", config.appUrl());
    props.setProperty("Browser", config.browser());
    props.setProperty("Execution Type", config.executionType());
    props.setProperty("OS", System.getProperty("os.name"));
    props.setProperty("Java Version", System.getProperty("java.version"));

    String resultsDir = System.getProperty("allure.results.directory", "target/allure-results");
    try {
      Files.createDirectories(Paths.get(resultsDir));
      try (FileOutputStream fos = new FileOutputStream(resultsDir + "/environment.properties")) {
        props.store(fos, "Allure Environment Properties");
        log.info("Allure environment.properties generated successfully in {}", resultsDir);
      }
    } catch (IOException e) {
      log.error("Failed to generate Allure environment.properties", e);
    }
  }
}
