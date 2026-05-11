package util;

import common.config.ConfigFactory;
import common.config.FrameworkConfig;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility class for synchronization and explicit waits. Encapsulates WebDriverWait to provide
 * reusable wait strategies.
 */
@Slf4j
public class WaitUtils {

  private final WebDriver driver;
  private final WebDriverWait wait;

  /**
   * Initializes WaitUtils with a specific WebDriver instance.
   *
   * @param driver The WebDriver instance to use for waits.
   */
  public WaitUtils(WebDriver driver) {
    this.driver = driver;
    FrameworkConfig config = ConfigFactory.getConfig();
    this.wait =
        new WebDriverWait(
            driver,
            Duration.ofSeconds(config.explicitWaitSeconds()),
            Duration.ofMillis(config.pollingIntervalMs()));
    this.wait.ignoring(StaleElementReferenceException.class);
  }

  /**
   * Waits until an element located by the given By selector is visible.
   *
   * @param locator The By selector of the element.
   * @return The visible WebElement.
   */
  public WebElement waitUntilVisible(By locator) {
    log.debug("Waiting for visibility of element located by: {}", locator);
    try {
      return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for visibility of element located by: {}", locator);
      throw new TimeoutException(
          String.format("Element not visible after timeout: %s", locator), e);
    }
  }

  /**
   * Waits until the given WebElement is visible.
   *
   * @param element The WebElement to wait for.
   * @return The visible WebElement.
   */
  public WebElement waitUntilVisible(WebElement element) {
    log.debug("Waiting for visibility of element: {}", element);
    try {
      return wait.until(ExpectedConditions.visibilityOf(element));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for visibility of element: {}", element);
      throw new TimeoutException(
          String.format("Element not visible after timeout: %s", element), e);
    }
  }

  /**
   * Waits until an element located by the given By selector is clickable.
   *
   * @param locator The By selector of the element.
   * @return The clickable WebElement.
   */
  public WebElement waitUntilClickable(By locator) {
    log.debug("Waiting for clickability of element located by: {}", locator);
    try {
      return wait.until(ExpectedConditions.elementToBeClickable(locator));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for clickability of element located by: {}", locator);
      throw new TimeoutException(
          String.format("Element not clickable after timeout: %s", locator), e);
    }
  }

  public WebElement waitUntilNestedClickable(WebElement parent, By childLocator) {
    log.debug("Waiting for nested element to be clickable: {}", childLocator);
    try {
      return wait.until(
          driver -> {
            WebElement child = parent.findElement(childLocator);
            return child.isDisplayed() && child.isEnabled() ? child : null;
          });
    } catch (TimeoutException e) {
      log.error("Timeout waiting for nested element to be clickable: {}", childLocator);
      throw new TimeoutException(
          String.format("Nested element not clickable after timeout: %s", childLocator), e);
    }
  }

  public List<WebElement> waitUntilAllVisible(By locator) {
    log.debug("Waiting for all matching elements to be visible: {}", locator);
    try {
      return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for all matching elements to be visible: {}", locator);
      throw new TimeoutException(
          String.format("Not all elements visible after timeout: %s", locator), e);
    }
  }

  public boolean waitUntilTextPresent(By locator, String text) {
    log.debug("Waiting for text '{}' to be present in element: {}", text, locator);
    try {
      return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for text '{}' to be present in element: {}", text, locator);
      throw new TimeoutException(
          String.format("Text '%s' not present in element %s after timeout", text, locator), e);
    }
  }

  /**
   * Waits until the given element is invisible.
   *
   * @param locator The By selector of the element.
   * @return true if invisible.
   */
  public boolean waitUntilInvisible(By locator) {
    log.debug("Waiting for invisibility of element located by: {}", locator);
    try {
      return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for invisibility of element located by: {}", locator);
      throw new TimeoutException(
          String.format("Element not invisible after timeout: %s", locator), e);
    }
  }

  public void type(By locator, String text) {
    WebElement element = waitUntilVisible(locator);
    element.clear();
    element.sendKeys(text);
  }

  public void click(By locator) {
    waitUntilClickable(locator).click();
  }

  /** Waits for the browser's document ready state to be 'complete'. */
  public void waitForPageLoad() {
    log.debug("Waiting for page document ready state to be complete");
    try {
      wait.until(
          d ->
              ((JavascriptExecutor) d)
                  .executeScript("return document.readyState")
                  .equals("complete"));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for page document ready state to be complete");
      throw new TimeoutException("Page did not load completely within timeout", e);
    }
  }
}
