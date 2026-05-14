package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.app.data.AppRoute;
import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.app.ui.ProductSelectors;
import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class ProductDetailPage extends BasePage implements PageLoadable<ProductDetailPage> {

  private static final By BACK_BUTTON = By.cssSelector("[data-test='back-to-products']");

  public ProductDetailPage(WebDriver driver) {
    super(driver);
  }

  @Override
  public ProductDetailPage waitUntilLoaded() {
    waitUntilPathContains(AppRoute.PRODUCT_DETAIL.path());
    waitUtils.waitUntilVisible(ProductSelectors.DETAIL_ROOT);
    productDetailRoot().findElement(ProductSelectors.NAME);
    waitUtils.waitUntilVisible(BACK_BUTTON);
    return this;
  }

  @Step("Get product details from detail page")
  public ProductDetails getProductDetails() {
    WebElement productDetail = productDetailRoot();
    ProductDetails details =
        new ProductDetails(
            productDetail.findElement(ProductSelectors.NAME).getText(),
            productDetail.findElement(ProductSelectors.DESCRIPTION).getText(),
            productDetail.findElement(ProductSelectors.PRICE).getText());
    log.debug("Product detail page details retrieved: {}", details);
    return details;
  }

  @Step("Add product to cart from detail page")
  public ProductDetailPage addToCart() {
    waitUtils
        .waitUntilNestedClickable(this::productDetailRoot, ProductSelectors.ADD_TO_CART_BUTTON)
        .click();
    waitUtils.waitUntil(
        currentDriver -> {
          WebElement removeButton = productDetailRoot().findElement(ProductSelectors.REMOVE_BUTTON);
          return removeButton.isDisplayed() ? removeButton : null;
        },
        "Remove button did not become visible on product detail page");
    return this;
  }

  @Step("Get product action button text from detail page")
  public String getActionButtonText() {
    WebElement productDetail = productDetailRoot();
    if (productDetail.findElements(ProductSelectors.ADD_TO_CART_BUTTON).stream()
        .anyMatch(WebElement::isDisplayed)) {
      return productDetail.findElement(ProductSelectors.ADD_TO_CART_BUTTON).getText();
    }
    return productDetail.findElement(ProductSelectors.REMOVE_BUTTON).getText();
  }

  @Step("Go back to products list")
  public InventoryPage goBack() {
    waitUtils.click(BACK_BUTTON);
    return new InventoryPage(driver).waitUntilLoaded();
  }

  private WebElement productDetailRoot() {
    return waitUtils.waitUntilVisible(ProductSelectors.DETAIL_ROOT);
  }
}
