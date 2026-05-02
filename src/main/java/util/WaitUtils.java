package util;

import common.config.ConfigFactory;
import common.config.FrameworkConfig;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
  }

  /**
   * Waits until an element located by the given By selector is visible.
   *
   * @param locator The By selector of the element.
   * @return The visible WebElement.
   */
  public WebElement waitUntilVisible(By locator) {
    log.debug("Waiting for visibility of element located by: {}", locator);
    return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  /**
   * Waits until the given WebElement is visible.
   *
   * @param element The WebElement to wait for.
   * @return The visible WebElement.
   */
  public WebElement waitUntilVisible(WebElement element) {
    log.debug("Waiting for visibility of element: {}", element);
    return wait.until(ExpectedConditions.visibilityOf(element));
  }

  /**
   * Waits until an element located by the given By selector is clickable.
   *
   * @param locator The By selector of the element.
   * @return The clickable WebElement.
   */
  public WebElement waitUntilClickable(By locator) {
    log.debug("Waiting for clickability of element located by: {}", locator);
    return wait.until(ExpectedConditions.elementToBeClickable(locator));
  }

  public WebElement waitUntilNestedClickable(WebElement parent, By childLocator) {
    log.debug("Waiting for nested element to be clickable: {}", childLocator);
    return wait.until(
        driver -> {
          WebElement child = parent.findElement(childLocator);
          return child.isDisplayed() && child.isEnabled() ? child : null;
        });
  }

  public List<WebElement> waitUntilAllVisible(By locator) {
    log.debug("Waiting for all matching elements to be visible: {}", locator);
    return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
  }

  public boolean waitUntilTextPresent(By locator, String text) {
    log.debug("Waiting for text '{}' to be present in element: {}", text, locator);
    return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
  }

  /**
   * Waits until the given element is invisible.
   *
   * @param locator The By selector of the element.
   * @return true if invisible.
   */
  public boolean waitUntilInvisible(By locator) {
    log.debug("Waiting for invisibility of element located by: {}", locator);
    return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
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
    wait.until(
        d ->
            ((JavascriptExecutor) d)
                .executeScript("return document.readyState")
                .equals("complete"));
  }
}
