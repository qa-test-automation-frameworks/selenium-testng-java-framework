package com.example.saucedemo.framework.pageobject;

import com.example.saucedemo.framework.pageobject.component.InventoryListComponent;
import io.qameta.allure.Step;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@Slf4j
public class InventoryPage extends BasePage implements PageLoadable<InventoryPage> {

  private final InventoryListComponent inventoryList;

  public InventoryPage(WebDriver driver) {
    super(driver);
    this.inventoryList = new InventoryListComponent(driver);
  }

  public InventoryListComponent getInventoryList() {
    return inventoryList;
  }

  private final By primaryHeader = By.className("app_logo");
  private final By productSort = By.cssSelector("[data-test='product-sort-container']");
  private final By productPrice = By.cssSelector("[data-test='inventory-item-price']");

  @Override
  public InventoryPage waitUntilLoaded() {
    waitUntilUrlContains("inventory");
    waitUtils.waitUntilVisible(primaryHeader);
    waitUtils.waitUntilVisible(productSort);
    return this;
  }

  @Step("Get header text from landing page")
  public String getHeaderText() {
    log.info("Retrieving the main header text from the inventory page");
    String text = waitUtils.waitUntilVisible(primaryHeader).getText();
    log.debug("Inventory page header text retrieved: {}", text);
    return text;
  }

  @Step("Add product '{0}' to cart from inventory")
  public InventoryPage addProductToCart(String productName) {
    inventoryList.addProductToCart(productName);
    return this;
  }

  @Step("Remove product '{0}' from inventory")
  public InventoryPage removeProductFromCart(String productName) {
    inventoryList.removeProductFromCart(productName);
    return this;
  }

  @Step("Sort products by price low to high")
  public InventoryPage sortProductsByPriceLowToHigh() {
    new Select(waitUtils.waitUntilVisible(productSort)).selectByValue("lohi");
    return this;
  }

  @Step("Get displayed product prices from inventory")
  public List<BigDecimal> getDisplayedProductPrices() {
    return waitUtils.waitUntilAllVisible(productPrice).stream()
        .map(WebElement::getText)
        .map(price -> price.replace("$", ""))
        .map(BigDecimal::new)
        .toList();
  }
}
