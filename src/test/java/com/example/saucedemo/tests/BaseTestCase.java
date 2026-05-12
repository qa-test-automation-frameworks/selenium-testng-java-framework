package com.example.saucedemo.tests;

import com.example.saucedemo.framework.driver.WebDriverFactory;
import com.example.saucedemo.framework.listener.FrameworkListener;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Slf4j
@Listeners(FrameworkListener.class)
public abstract class BaseTestCase {

  protected WebDriver getDriver() {
    return WebDriverFactory.getThreadLocalWebDriver();
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
}
