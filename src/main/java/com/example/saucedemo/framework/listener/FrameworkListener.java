package com.example.saucedemo.framework.listener;

import com.example.saucedemo.framework.driver.WebDriverFactory;
import com.example.saucedemo.framework.util.DiagnosticsAttacher;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.testng.IConfigurationListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

@Slf4j
public class FrameworkListener implements ITestListener, IConfigurationListener {

  private static final DiagnosticsAttacher DIAGNOSTICS_ATTACHER = new DiagnosticsAttacher();

  @Override
  public void onStart(ITestContext context) {
    RetryRegistry.clear();
  }

  @Override
  public void onTestFailure(ITestResult result) {
    log.error("Test failed: {}", result.getName());
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
  public void onTestSkipped(ITestResult result) {
    log.warn("Test skipped: {}", result.getName());
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

  @Override
  public void onConfigurationFailure(ITestResult result) {
    log.error("Configuration failed: {}", result.getName());
    DIAGNOSTICS_ATTACHER.attach(
        "Configuration failure", WebDriverFactory.getThreadLocalWebDriver());
  }

  public static void attachTestFailureDiagnostics(WebDriver driver) {
    DIAGNOSTICS_ATTACHER.attach("Test failure", driver);
  }
}
