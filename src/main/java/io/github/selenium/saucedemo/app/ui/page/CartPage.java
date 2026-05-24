package io.github.selenium.saucedemo.app.ui.page;

import io.github.selenium.saucedemo.app.data.AppRoute;
import io.github.selenium.saucedemo.app.ui.component.InventoryListComponent;
import io.github.selenium.saucedemo.framework.ui.BasePage;
import io.github.selenium.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CartPage extends BasePage implements PageLoadable<CartPage> {

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
    waitUntilPathEndsWith(AppRoute.CART.path());
    waitUtils.waitUntilVisible(CART_TITLE);
    waitUtils.waitUntilVisible(CHECKOUT_BUTTON);
    return this;
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

  @Step("Continue to checkout")
  public CheckoutPage continueToCheckout() {
    waitUtils.click(CHECKOUT_BUTTON);
    return new CheckoutPage(driver).waitUntilLoaded();
  }
}
