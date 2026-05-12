package com.example.saucedemo.tests;

import com.example.saucedemo.framework.driver.WebDriverFactory;
import com.example.saucedemo.framework.listener.FrameworkListener;
import com.example.saucedemo.framework.pageobject.CartPage;
import com.example.saucedemo.framework.pageobject.CheckoutPage;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.LoginPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Slf4j
@Listeners(FrameworkListener.class)
public abstract class BaseTestCase {

  private static final FrameworkListener DIAGNOSTICS = new FrameworkListener();

  protected WebDriver getDriver() {
    return WebDriverFactory.getThreadLocalWebDriver();
  }

  protected Pages pages() {
    return new Pages(getDriver());
  }

  protected void quitWebDriver() {
    log.info("Cleaning up WebDriver for thread: {}", Thread.currentThread().getName());
    WebDriverFactory.cleanUpDriver();
  }

  @BeforeMethod(alwaysRun = true, description = "Initialize WebDriver")
  public void beforeMethod(ITestResult result) {
    log.info("BeforeMethod: Initializing driver for test: {}", result.getMethod().getMethodName());
    WebDriverFactory.initThreadLocalDriver();
  }

  @AfterMethod(alwaysRun = true, description = "Quit WebDriver")
  public void afterMethod(ITestResult result) {
    log.info("AfterMethod: Tearing down driver for test: {}", result.getMethod().getMethodName());
    if (result.getStatus() == ITestResult.FAILURE) {
      DIAGNOSTICS.attachTestFailureDiagnostics(getDriver());
    }
    quitWebDriver();
  }

  protected record Pages(WebDriver driver) {

    public InventoryPage inventory() {
      return new InventoryPage(driver);
    }

    public LoginPage login() {
      return new LoginPage(driver);
    }

    public CartPage cart() {
      return new CartPage(driver);
    }

    public CheckoutPage checkout() {
      return new CheckoutPage(driver);
    }

    public HeaderComponent header() {
      return new HeaderComponent(driver);
    }
  }
}
