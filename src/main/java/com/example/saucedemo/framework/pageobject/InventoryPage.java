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

  // Sauce Demo does not expose a data-test attribute on the app logo.
  private static final By APP_LOGO = By.className("app_logo");
  private static final By PRODUCT_SORT = By.cssSelector("[data-test='product-sort-container']");
  private static final By PRODUCT_PRICE = By.cssSelector("[data-test='inventory-item-price']");

  private final InventoryListComponent inventoryList;

  public InventoryPage(WebDriver driver) {
    super(driver);
    this.inventoryList = InventoryListComponent.inventory(driver);
  }

  public InventoryListComponent getInventoryList() {
    return inventoryList;
  }

  @Override
  public InventoryPage waitUntilLoaded() {
    waitUntilUrlContains("inventory");
    waitUtils.waitUntilVisible(APP_LOGO);
    waitUtils.waitUntilVisible(PRODUCT_SORT);
    return this;
  }

  @Step("Get app logo text from inventory page")
  public String getAppLogoText() {
    log.info("Retrieving the app logo text from the inventory page");
    String text = waitUtils.waitUntilVisible(APP_LOGO).getText();
    log.debug("Inventory page app logo text retrieved: {}", text);
    return text;
  }

  @Step("Add product '{0}' to cart from inventory")
  public InventoryPage addProductToCart(String productName) {
    inventoryList.addProductToCart(productName);
    return this;
  }

  @Step("Open product detail page for '{0}'")
  public ProductDetailPage openProductDetail(String productName) {
    return inventoryList.openProductDetail(productName);
  }

  @Step("Remove product '{0}' from inventory")
  public InventoryPage removeProductFromCart(String productName) {
    inventoryList.removeProductFromCart(productName);
    return this;
  }

  @Step("Sort products by price low to high")
  public InventoryPage sortProductsByPriceLowToHigh() {
    new Select(waitUtils.waitUntilVisible(PRODUCT_SORT)).selectByValue("lohi");
    return this;
  }

  @Step("Get displayed product prices from inventory")
  public List<BigDecimal> getDisplayedProductPrices() {
    return waitUtils.waitUntilAllVisible(PRODUCT_PRICE).stream()
        .map(WebElement::getText)
        .map(price -> price.replace("$", ""))
        .map(BigDecimal::new)
        .toList();
  }
}
