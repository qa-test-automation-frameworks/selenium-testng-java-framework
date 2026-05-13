package io.github.prayag.saucedemo.app.ui.component;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.app.ui.ProductSelectors;
import io.github.prayag.saucedemo.framework.ui.BaseComponent;
import io.qameta.allure.Step;
import java.util.function.Supplier;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Root-scoped behavior for one inventory, cart, or checkout overview product row. */
public class ProductItemComponent extends BaseComponent {

  private final Supplier<WebElement> rootSupplier;

  public ProductItemComponent(WebDriver driver, Supplier<WebElement> rootSupplier) {
    super(driver);
    this.rootSupplier = rootSupplier;
  }

  @Step("Get product details from item")
  public ProductDetails details() {
    WebElement root = root();
    return new ProductDetails(
        root.findElement(ProductSelectors.NAME).getText(),
        root.findElement(ProductSelectors.DESCRIPTION).getText(),
        root.findElement(ProductSelectors.PRICE).getText());
  }

  @Step("Add product item to cart")
  public void addToCart() {
    waitUtils.waitUntilNestedClickable(this::root, ProductSelectors.ADD_TO_CART_BUTTON).click();
  }

  @Step("Remove product item from cart")
  public void removeFromCart() {
    waitUtils.waitUntilNestedClickable(this::root, ProductSelectors.REMOVE_BUTTON).click();
  }

  @Step("Open product item detail")
  public void openDetail() {
    waitUtils.waitUntilNestedClickable(this::root, ProductSelectors.NAME).click();
  }

  @Step("Get product item action button text")
  public String actionButtonText() {
    WebElement root = root();
    if (root.findElements(ProductSelectors.ADD_TO_CART_BUTTON).stream()
        .anyMatch(WebElement::isDisplayed)) {
      return root.findElement(ProductSelectors.ADD_TO_CART_BUTTON).getText();
    }
    return root.findElement(ProductSelectors.REMOVE_BUTTON).getText();
  }

  @Step("Get product item quantity")
  public int quantity() {
    return Integer.parseInt(root().findElement(ProductSelectors.QUANTITY).getText());
  }

  private WebElement root() {
    return rootSupplier.get();
  }
}
