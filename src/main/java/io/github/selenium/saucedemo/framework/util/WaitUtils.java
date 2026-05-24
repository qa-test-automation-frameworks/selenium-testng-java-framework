package io.github.selenium.saucedemo.framework.util;

import io.github.selenium.saucedemo.framework.config.FrameworkConfig;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
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
 *
 * <p>The small {@link #type(By, String)} and {@link #click(By)} helpers intentionally combine the
 * wait and interaction steps for the most common page-object actions. If broader interaction logic
 * is introduced later, those methods can move to a dedicated interaction helper without changing
 * page-object behavior.
 */
@Slf4j
public class WaitUtils {

  private final WebDriver driver;
  private final WebDriverWait wait;
  private final Duration pollingInterval;

  /**
   * Initializes WaitUtils with a specific WebDriver instance.
   *
   * @param driver The WebDriver instance to use for waits.
   */
  public WaitUtils(WebDriver driver, FrameworkConfig config) {
    this.driver = driver;
    this.pollingInterval = Duration.ofMillis(config.pollingIntervalMs());
    this.wait =
        new WebDriverWait(
            driver, Duration.ofSeconds(config.explicitWaitSeconds()), pollingInterval);
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
    return waitUntilNestedClickable(() -> parent, childLocator);
  }

  public WebElement waitUntilNestedClickable(Supplier<WebElement> parentSupplier, By childLocator) {
    log.debug("Waiting for nested element to be clickable: {}", childLocator);
    try {
      return wait.until(
          driver -> {
            WebElement child = parentSupplier.get().findElement(childLocator);
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

  public List<WebElement> waitUntilElementCountIs(By locator, int expectedCount) {
    log.debug("Waiting for element count {} to be: {}", locator, expectedCount);
    try {
      return wait.until(ExpectedConditions.numberOfElementsToBe(locator, expectedCount));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for element count {} to be {}", locator, expectedCount);
      throw new TimeoutException(
          String.format("Element count for %s was not %d after timeout", locator, expectedCount),
          e);
    }
  }

  public List<WebElement> waitUntilElementCountAtLeast(By locator, int minimumCount) {
    log.debug("Waiting for element count {} to be at least: {}", locator, minimumCount);
    try {
      return wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, minimumCount - 1));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for element count {} to be at least {}", locator, minimumCount);
      throw new TimeoutException(
          String.format(
              "Element count for %s was not at least %d after timeout", locator, minimumCount),
          e);
    }
  }

  /**
   * Checks whether an element is currently visible using a fast-fail timeout equal to the
   * configured polling interval.
   *
   * <p>This is not a synchronization point. It is intended for optional-element presence checks
   * after a preceding wait has already ensured state stability. Call {@link #waitUntilVisible(By)}
   * or a domain-specific wait method when the element may still be loading or transitioning.
   */
  public boolean isVisible(By locator) {
    return isVisible(locator, pollingInterval);
  }

  public boolean isVisible(By locator, Duration timeout) {
    try {
      return new WebDriverWait(driver, timeout, pollingInterval)
          .until(ExpectedConditions.visibilityOfElementLocated(locator))
          .isDisplayed();
    } catch (TimeoutException e) {
      return false;
    }
  }

  public boolean isPresent(By locator) {
    return !driver.findElements(locator).isEmpty();
  }

  public <T> T waitUntil(Function<WebDriver, T> condition, String failureMessage) {
    log.debug("Waiting for custom condition: {}", failureMessage);
    try {
      return wait.until(condition);
    } catch (TimeoutException e) {
      log.error("Timeout waiting for custom condition: {}", failureMessage);
      throw new TimeoutException(failureMessage, e);
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

  /** Waits until an element is either absent from the DOM or present but not visible. */
  public boolean waitUntilInvisibleOrAbsent(By locator, String failureMessage) {
    log.debug("Waiting for element to be invisible or absent: {}", locator);
    try {
      return wait.until(
          currentDriver -> {
            List<WebElement> elements = currentDriver.findElements(locator);
            return elements.isEmpty() || elements.stream().noneMatch(WebElement::isDisplayed);
          });
    } catch (TimeoutException e) {
      log.error("Timeout waiting for element to be invisible or absent: {}", locator);
      throw new TimeoutException(failureMessage, e);
    }
  }

  /** Waits for an element to be visible before clearing and typing text into it. */
  public void type(By locator, String text) {
    WebElement element = waitUntilVisible(locator);
    element.clear();
    element.sendKeys(text);
  }

  /** Waits for an element to be clickable before clicking it. */
  public void click(By locator) {
    waitUntilClickable(locator).click();
  }

  /** Waits for the browser's document ready state to be 'complete'. */
  public void waitForPageLoad() {
    log.debug("Waiting for page document ready state to be complete");
    try {
      wait.until(
          d ->
              "complete"
                  .equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    } catch (TimeoutException e) {
      log.error("Timeout waiting for page document ready state to be complete");
      throw new TimeoutException("Page did not load completely within timeout", e);
    }
  }
}
