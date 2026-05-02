package common.listener;

import common.driver.WebDriverFactory;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class FrameworkListener implements ITestListener {

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
}
