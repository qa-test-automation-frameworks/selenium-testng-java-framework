package common.listener;

import common.driver.WebDriverFactory;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
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
}
