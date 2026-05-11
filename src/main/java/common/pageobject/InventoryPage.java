package common.pageobject;

import common.pageobject.component.InventoryListComponent;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class InventoryPage extends BasePage {

  private final InventoryListComponent inventoryList;

  public InventoryPage(WebDriver driver) {
    super(driver);
    this.inventoryList = new InventoryListComponent(driver);
  }

  public InventoryListComponent getInventoryList() {
    return inventoryList;
  }

  private final By primaryHeader = By.className("app_logo");

  @Step("Get header text from landing page")
  public String getHeaderText() {
    log.info("Retrieving the main header text from the inventory page");
    String text = waitUtils.waitUntilVisible(primaryHeader).getText();
    log.debug("Inventory page header text retrieved: {}", text);
    return text;
  }
}
