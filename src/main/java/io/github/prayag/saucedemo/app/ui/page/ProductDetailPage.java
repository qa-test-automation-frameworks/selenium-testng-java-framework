package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class ProductDetailPage extends BasePage implements PageLoadable<ProductDetailPage> {

  private static final By PRODUCT_DETAIL = By.cssSelector("[data-test='inventory-item']");
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
    waitUtils.waitUntilVisible(PRODUCT_DETAIL);
    productDetailRoot().findElement(PRODUCT_NAME);
    waitUtils.waitUntilVisible(BACK_BUTTON);
    return this;
  }

  @Step("Get product details from detail page")
  public ProductDetails getProductDetails() {
    WebElement productDetail = productDetailRoot();
    ProductDetails details =
        new ProductDetails(
            productDetail.findElement(PRODUCT_NAME).getText(),
            productDetail.findElement(PRODUCT_DESCRIPTION).getText(),
            productDetail.findElement(PRODUCT_PRICE).getText());
    log.debug("Product detail page details retrieved: {}", details);
    return details;
  }

  @Step("Add product to cart from detail page")
  public ProductDetailPage addToCart() {
    waitUtils.waitUntilNestedClickable(this::productDetailRoot, ADD_TO_CART_BUTTON).click();
    waitUtils.waitUntil(
        currentDriver -> {
          WebElement removeButton = productDetailRoot().findElement(REMOVE_BUTTON);
          return removeButton.isDisplayed() ? removeButton : null;
        },
        "Remove button did not become visible on product detail page");
    return this;
  }

  @Step("Get product action button text from detail page")
  public String getActionButtonText() {
    WebElement productDetail = productDetailRoot();
    if (productDetail.findElements(ADD_TO_CART_BUTTON).stream().anyMatch(WebElement::isDisplayed)) {
      return productDetail.findElement(ADD_TO_CART_BUTTON).getText();
    }
    return productDetail.findElement(REMOVE_BUTTON).getText();
  }

  @Step("Go back to products list")
  public InventoryPage goBack() {
    waitUtils.click(BACK_BUTTON);
    return new InventoryPage(driver).waitUntilLoaded();
  }

  private WebElement productDetailRoot() {
    return waitUtils.waitUntilVisible(PRODUCT_DETAIL);
  }
}
