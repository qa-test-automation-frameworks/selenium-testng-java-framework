package com.example.saucedemo.framework.listener;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.driver.WebDriverFactory;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class FrameworkListener implements ITestListener {

  @Override
  public void onStart(ITestContext context) {
    RetryRegistry.clear();
  }

  @Override
  public void onTestFailure(ITestResult result) {
    log.error("Test failed: {}", result.getName());
    WebDriver driver = WebDriverFactory.getThreadLocalWebDriver();
    if (driver != null) {
      log.info("Attaching screenshot and diagnostics to Allure report");
      try {
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(
            "Screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
        Allure.addAttachment("Current URL", driver.getCurrentUrl());
        attachCapabilities(driver);
        attachBrowserLogs(driver);
        attachNetworkLogs(driver);
        attachGridVideoLink(driver);
        Allure.addAttachment("Page Source", "text/html", driver.getPageSource(), ".html");
      } catch (Exception e) {
        log.error("Failed to attach diagnostics to Allure report", e);
      }
    }
  }

  @Override
  public void onTestStart(ITestResult result) {
    log.info("Starting test: {}", result.getName());
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    log.info("Test passed: {}", result.getName());
  }

  @Override
  public void onFinish(ITestContext context) {
    var retries = RetryRegistry.snapshot();
    if (retries.isEmpty()) {
      log.info("No tests required retry in this run");
      return;
    }
    String summary = String.join(System.lineSeparator(), retries);
    log.warn("Tests required retry: {}", summary);
    Allure.addAttachment("Retry Summary", summary);
  }

  private void attachCapabilities(WebDriver driver) {
    if (driver instanceof HasCapabilities hasCapabilities) {
      Allure.addAttachment("Browser Capabilities", hasCapabilities.getCapabilities().toString());
    }
  }

  private void attachBrowserLogs(WebDriver driver) {
    try {
      LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
      StringBuilder builder = new StringBuilder();
      logs.forEach(entry -> builder.append(entry).append(System.lineSeparator()));
      Allure.addAttachment("Browser Console Logs", builder.toString());
    } catch (Exception e) {
      log.debug("Browser console logs are not available for this driver", e);
    }
  }

  private void attachNetworkLogs(WebDriver driver) {
    if (!ConfigFactory.getConfig().networkLogsEnabled()) {
      return;
    }
    try {
      LogEntries logs = driver.manage().logs().get(LogType.PERFORMANCE);
      String payload =
          logs.getAll().stream()
              .map(Object::toString)
              .collect(Collectors.joining(System.lineSeparator()));
      Allure.addAttachment("Browser Network Logs", "application/json", payload, ".json");
    } catch (Exception e) {
      log.debug("Browser network logs are not available for this driver", e);
    }
  }

  private void attachGridVideoLink(WebDriver driver) {
    String baseUrl = ConfigFactory.getConfig().gridVideoBaseUrl();
    if (baseUrl == null || baseUrl.isBlank() || !(driver instanceof RemoteWebDriver remoteDriver)) {
      return;
    }
    String videoUrl = baseUrl.replaceAll("/$", "") + "/" + remoteDriver.getSessionId() + ".mp4";
    Allure.addAttachment("Selenium Grid Video", videoUrl);
  }
}
