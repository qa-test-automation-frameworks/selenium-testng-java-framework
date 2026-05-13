package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.app.ui.component.InventoryListComponent;
import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class CartPage extends BasePage implements PageLoadable<CartPage> {

  private static final By PRODUCT_QUANTITY_ELEMENT = By.cssSelector("[data-test='item-quantity']");
  private static final By CART_TITLE = By.cssSelector("[data-test='title']");
  private static final By CHECKOUT_BUTTON = By.cssSelector("[data-test='checkout']");

  private final InventoryListComponent inventoryList;

  public CartPage(WebDriver driver) {
    super(driver);
    this.inventoryList = InventoryListComponent.cart(driver);
  }

  public InventoryListComponent getInventoryList() {
    return inventoryList;
  }

  @Override
  public CartPage waitUntilLoaded() {
    waitUntilUrlContains("cart");
    waitUtils.waitUntilVisible(CART_TITLE);
    waitUtils.waitUntilVisible(CHECKOUT_BUTTON);
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

    String quantityText = items.get(index).findElement(PRODUCT_QUANTITY_ELEMENT).getText();
    int quantity = Integer.parseInt(quantityText);
    log.debug("Quantity for item at index {}: {}", index, quantity);
    return quantity;
  }

  @Step("Get quantity for product '{0}' in cart")
  public int getQuantityForProduct(String productName) {
    log.info("Retrieving product quantity for item '{}' in the cart", productName);
    int quantity = getInventoryList().getProductItemByName(productName).quantity();
    log.debug("Quantity for product '{}': {}", productName, quantity);
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
    waitUtils.click(CHECKOUT_BUTTON);
    return new CheckoutPage(driver).waitUntilLoaded();
  }
}
