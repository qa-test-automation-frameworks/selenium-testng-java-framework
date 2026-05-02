package common.pageobject;

import common.pageobject.component.InventoryListComponent;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import util.WaitUtils;

/**
 * The BasePage serves as the foundation for all Page Objects in the framework. It provides common
 * interaction methods, synchronization strategies, and shared UI elements like the inventory list.
 */
@Slf4j
public abstract class BasePage {

  protected final WebDriver driver;
  protected final WaitUtils waitUtils;
  private final InventoryListComponent inventoryList;

  /**
   * Initializes the Page Object with a WebDriver instance and setup WaitUtils.
   *
   * @param driver The thread-local WebDriver instance.
   */
  protected BasePage(WebDriver driver) {
    this.driver = driver;
    this.waitUtils = new WaitUtils(driver);
    this.inventoryList = new InventoryListComponent(driver, this.waitUtils);
  }

  public InventoryListComponent getInventoryList() {
    return inventoryList;
  }

  protected void navigateTo(String url) {
    log.info("Navigating to URL: {}", url);
    driver.navigate().to(url);
    waitUtils.waitForPageLoad();
    log.debug("Page load completed for URL: {}", url);
  }
}
