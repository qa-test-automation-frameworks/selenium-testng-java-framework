package util;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utility class for synchronization and explicit waits.
 * Encapsulates WebDriverWait to provide reusable wait strategies.
 */
@Slf4j
public class WaitUtils {

  private final WebDriver driver;
  private final WebDriverWait wait;
  private static final Duration TIMEOUT = Duration.ofSeconds(10);

  /**
   * Initializes WaitUtils with a specific WebDriver instance.
   *
   * @param driver The WebDriver instance to use for waits.
   */
  public WaitUtils(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, TIMEOUT);
  }

  /**
   * Waits until an element located by the given By selector is visible.
   *
   * @param locator The By selector of the element.
   */
  public void waitUntilVisible(By locator) {
    wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
  }

  /**
   * Waits until the given WebElement is visible.
   *
   * @param element The WebElement to wait for.
   */
  public void waitUntilVisible(WebElement element) {
    wait.until(ExpectedConditions.visibilityOf(element));
  }

  /**
   * Waits for the browser's document ready state to be 'complete'.
   */
  public void waitForPageLoad() {
    wait.until(
        d ->
            ((JavascriptExecutor) d)
                .executeScript("return document.readyState")
                .equals("complete"));
  }
}
