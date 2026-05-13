package io.github.prayag.saucedemo.app.ui.component;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.framework.ui.BaseComponent;
import io.qameta.allure.Step;
import java.util.function.Supplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Root-scoped behavior for one inventory, cart, or checkout overview product row. */
public class ProductItemComponent extends BaseComponent {

  private static final By PRODUCT_NAME_ELEMENT =
      By.cssSelector("[data-test='inventory-item-name']");
  private static final By PRODUCT_DESCRIPTION_ELEMENT =
      By.cssSelector("[data-test='inventory-item-desc']");
  private static final By PRODUCT_PRICE_ELEMENT =
      By.cssSelector("[data-test='inventory-item-price']");
  private static final By PRODUCT_QUANTITY_ELEMENT = By.cssSelector("[data-test='item-quantity']");
  private static final By ADD_TO_CART_BUTTON = By.cssSelector("button[data-test^='add-to-cart']");
  private static final By REMOVE_BUTTON = By.cssSelector("button[data-test^='remove']");

  private final Supplier<WebElement> rootSupplier;

  public ProductItemComponent(WebDriver driver, Supplier<WebElement> rootSupplier) {
    super(driver);
    this.rootSupplier = rootSupplier;
  }

  @Step("Get product details from item")
  public ProductDetails details() {
    WebElement root = root();
    return new ProductDetails(
        root.findElement(PRODUCT_NAME_ELEMENT).getText(),
        root.findElement(PRODUCT_DESCRIPTION_ELEMENT).getText(),
        root.findElement(PRODUCT_PRICE_ELEMENT).getText());
  }

  @Step("Add product item to cart")
  public void addToCart() {
    waitUtils.waitUntilNestedClickable(this::root, ADD_TO_CART_BUTTON).click();
  }

  @Step("Remove product item from cart")
  public void removeFromCart() {
    waitUtils.waitUntilNestedClickable(this::root, REMOVE_BUTTON).click();
  }

  @Step("Open product item detail")
  public void openDetail() {
    waitUtils.waitUntilNestedClickable(this::root, PRODUCT_NAME_ELEMENT).click();
  }

  @Step("Get product item action button text")
  public String actionButtonText() {
    WebElement root = root();
    if (root.findElements(ADD_TO_CART_BUTTON).stream().anyMatch(WebElement::isDisplayed)) {
      return root.findElement(ADD_TO_CART_BUTTON).getText();
    }
    return root.findElement(REMOVE_BUTTON).getText();
  }

  @Step("Get product item quantity")
  public int quantity() {
    return Integer.parseInt(root().findElement(PRODUCT_QUANTITY_ELEMENT).getText());
  }

  private WebElement root() {
    return rootSupplier.get();
  }
}
