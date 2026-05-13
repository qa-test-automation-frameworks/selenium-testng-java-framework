package io.github.prayag.saucedemo.tests;

import io.github.prayag.saucedemo.app.ui.component.HeaderComponent;
import io.github.prayag.saucedemo.app.ui.page.CartPage;
import io.github.prayag.saucedemo.app.ui.page.CheckoutPage;
import io.github.prayag.saucedemo.app.ui.page.InventoryPage;
import io.github.prayag.saucedemo.app.ui.page.LoginPage;
import io.github.prayag.saucedemo.framework.config.ConfigFactory;
import io.github.prayag.saucedemo.framework.driver.WebDriverFactory;
import io.github.prayag.saucedemo.framework.listener.FrameworkListener;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

@Slf4j
public abstract class BaseTestCase {

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

  protected void assumePasswordConfigured() {
    var config = ConfigFactory.getConfig();
    String password = config.appPassword();
    if (password == null || password.isBlank()) {
      if (config.allowPasswordlessSkips()) {
        throw new SkipException(
            "Skipping password-dependent test because APP_PASSWORD is not configured.");
      }
      ConfigFactory.requireLoginPassword(config);
    }
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
      FrameworkListener.attachTestFailureDiagnostics(getDriver());
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
