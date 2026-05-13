package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class ProductDetailPage extends BasePage implements PageLoadable<ProductDetailPage> {

  private static final By PRODUCT_NAME = By.cssSelector("[data-test='inventory-item-name']");
  private static final By PRODUCT_DESCRIPTION = By.cssSelector("[data-test='inventory-item-desc']");
  private static final By PRODUCT_PRICE = By.cssSelector("[data-test='inventory-item-price']");
  private static final By ADD_TO_CART_BUTTON = By.cssSelector("button[data-test^='add-to-cart']");
  private static final By REMOVE_BUTTON = By.cssSelector("button[data-test^='remove']");
  private static final By BACK_BUTTON = By.cssSelector("[data-test='back-to-products']");

  public ProductDetailPage(WebDriver driver) {
    super(driver);
  }

  @Override
  public ProductDetailPage waitUntilLoaded() {
    waitUntilUrlContains("inventory-item");
    waitUtils.waitUntilVisible(PRODUCT_NAME);
    waitUtils.waitUntilVisible(BACK_BUTTON);
    return this;
  }

  @Step("Get product details from detail page")
  public ProductDetails getProductDetails() {
    ProductDetails details =
        new ProductDetails(
            waitUtils.waitUntilVisible(PRODUCT_NAME).getText(),
            waitUtils.waitUntilVisible(PRODUCT_DESCRIPTION).getText(),
            waitUtils.waitUntilVisible(PRODUCT_PRICE).getText());
    log.debug("Product detail page details retrieved: {}", details);
    return details;
  }

  @Step("Add product to cart from detail page")
  public ProductDetailPage addToCart() {
    waitUtils.click(ADD_TO_CART_BUTTON);
    waitUtils.waitUntilVisible(REMOVE_BUTTON);
    return this;
  }

  @Step("Get product action button text from detail page")
  public String getActionButtonText() {
    if (waitUtils.isVisible(ADD_TO_CART_BUTTON)) {
      return waitUtils.waitUntilVisible(ADD_TO_CART_BUTTON).getText();
    }
    return waitUtils.waitUntilVisible(REMOVE_BUTTON).getText();
  }

  @Step("Go back to products list")
  public InventoryPage goBack() {
    waitUtils.click(BACK_BUTTON);
    return new InventoryPage(driver).waitUntilLoaded();
  }
}
