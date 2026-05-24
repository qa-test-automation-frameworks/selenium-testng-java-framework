package io.github.selenium.saucedemo.framework.ui;

import io.github.selenium.saucedemo.framework.driver.WebDriverFactory;
import io.github.selenium.saucedemo.framework.util.WaitUtils;
import org.openqa.selenium.WebDriver;

/** Base class for reusable page fragments that need WebDriver access and explicit waits. */
public abstract class BaseComponent {

  protected final WebDriver driver;
  protected final WaitUtils waitUtils;

  protected BaseComponent(WebDriver driver) {
    this.driver = driver;
    this.waitUtils = WebDriverFactory.getThreadLocalWaitUtils(driver);
  }
}
