package com.example.saucedemo.framework.pageobject;

import com.example.saucedemo.framework.driver.WebDriverFactory;
import com.example.saucedemo.framework.util.WaitUtils;
import io.qameta.allure.Step;
import java.util.Objects;
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
    this.waitUtils = WebDriverFactory.getThreadLocalWaitUtils(driver);
  }

  @Step("Navigate browser to {0}")
  protected void navigateTo(String url) {
    log.info("Navigating to URL: {}", url);
    driver.navigate().to(url);
    log.debug("Page load completed for URL: {}", url);
  }

  @Step("Wait until current URL contains '{0}'")
  protected void waitUntilUrlContains(String expectedUrlFragment) {
    waitUtils.waitUntil(
        currentDriver ->
            Objects.toString(currentDriver.getCurrentUrl(), "").contains(expectedUrlFragment),
        String.format(
            "Current URL did not contain '%s' within the configured timeout", expectedUrlFragment));
  }
}
