package com.example.saucedemo.framework.pageobject;

import com.example.saucedemo.framework.util.WaitUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

/**
 * The BasePage serves as the foundation for all Page Objects in the framework. It provides common
 * interaction methods, synchronization strategies, and shared UI elements like the inventory list.
 */
@Slf4j
public abstract class BasePage {

  protected final WebDriver driver;
  protected final WaitUtils waitUtils;

  /**
   * Initializes the Page Object with a WebDriver instance and setup WaitUtils.
   *
   * @param driver The thread-local WebDriver instance.
   */
  protected BasePage(WebDriver driver) {
    this.driver = driver;
    this.waitUtils = new WaitUtils(driver);
  }

  protected void navigateTo(String url) {
    log.info("Navigating to URL: {}", url);
    driver.navigate().to(url);
    waitUtils.waitForPageLoad();
    log.debug("Page load completed for URL: {}", url);
  }
}
