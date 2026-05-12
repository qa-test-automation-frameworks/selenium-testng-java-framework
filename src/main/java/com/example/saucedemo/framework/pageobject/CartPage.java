package com.example.saucedemo.framework.pageobject;

import com.example.saucedemo.framework.pageobject.component.InventoryListComponent;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class CartPage extends BasePage implements PageLoadable<CartPage> {

  private final InventoryListComponent inventoryList;

  public CartPage(WebDriver driver) {
    super(driver);
    this.inventoryList = new InventoryListComponent(driver);
  }

  public InventoryListComponent getInventoryList() {
    return inventoryList;
  }

  private final By productQuantityElement = By.cssSelector("[data-test='item-quantity']");
  private final By checkoutButton = By.cssSelector("[data-test='checkout']");

  @Override
  public CartPage waitUntilLoaded() {
    waitUntilUrlContains("cart");
    waitUtils.waitUntilVisible(checkoutButton);
    return this;
  }

  @Step("Get product quantity at index {0} in cart")
  public int getProductQuantityByIndex(int index) {
    log.info("Retrieving product quantity for item at index {} in the cart", index);
    List<WebElement> items = getInventoryList().getItemList();
    if (index < 0 || index >= items.size()) {
      throw new NoSuchElementException(
          String.format(
              "Cart has %d items; cannot read quantity at index %d", items.size(), index));
    }

    String quantityText = items.get(index).findElement(productQuantityElement).getText();
    int quantity = Integer.parseInt(quantityText);
    log.debug("Quantity for item at index {}: {}", index, quantity);
    return quantity;
  }

  @Step("Remove product '{0}' from cart")
  public CartPage removeProduct(String productName) {
    inventoryList.removeProductFromCart(productName);
    return this;
  }

  @Step("Remove cart item at index {0}")
  public CartPage removeCartItemAtIndex(int index) {
    inventoryList.removeProductFromCartByIndex(index);
    return this;
  }

  @Step("Continue to checkout")
  public CheckoutPage continueToCheckout() {
    waitUtils.click(checkoutButton);
    return new CheckoutPage(driver).waitUntilLoaded();
  }
}
