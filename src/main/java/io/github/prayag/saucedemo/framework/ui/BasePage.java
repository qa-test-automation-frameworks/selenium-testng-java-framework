package io.github.prayag.saucedemo.framework.ui;

import io.github.prayag.saucedemo.framework.driver.WebDriverFactory;
import io.github.prayag.saucedemo.framework.util.WaitUtils;
import io.qameta.allure.Step;
import java.net.URI;
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

  @Step("Wait until current path ends with '{0}'")
  protected void waitUntilPathEndsWith(String expectedPath) {
    waitUtils.waitUntil(
        currentDriver -> currentPath(currentDriver.getCurrentUrl()).endsWith(expectedPath),
        String.format(
            "Current path did not end with '%s' within the configured timeout", expectedPath));
  }

  @Step("Wait until current path contains '{0}'")
  protected void waitUntilPathContains(String expectedPathFragment) {
    waitUtils.waitUntil(
        currentDriver -> currentPath(currentDriver.getCurrentUrl()).contains(expectedPathFragment),
        String.format(
            "Current path did not contain '%s' within the configured timeout",
            expectedPathFragment));
  }

  private String currentPath(String currentUrl) {
    String url = Objects.toString(currentUrl, "");
    if (url.isBlank()) {
      return "";
    }
    return URI.create(url).getPath();
  }
}
