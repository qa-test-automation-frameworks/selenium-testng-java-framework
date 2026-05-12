package com.example.saucedemo.framework.pageobject.component;

import com.example.saucedemo.framework.data.ProductDetails;
import com.example.saucedemo.framework.pageobject.BaseComponent;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class InventoryListComponent extends BaseComponent {

  protected final By listItems =
      By.cssSelector("[data-test='inventory-item'], [data-test='cart-item']");
  protected final By productNameElement = By.cssSelector("[data-test='inventory-item-name']");
  protected final By productDescriptionElement =
      By.cssSelector("[data-test='inventory-item-desc']");
  protected final By productPriceElement = By.cssSelector("[data-test='inventory-item-price']");
  private final By addToCartButton = By.cssSelector("button[data-test^='add-to-cart']");
  private final By removeButton = By.cssSelector("button[data-test^='remove']");

  public InventoryListComponent(WebDriver driver) {
    super(driver);
  }

  /**
   * Returns a list of all product web elements currently on the page.
   *
   * @return List of WebElements.
   */
  @Step("Get visible inventory list items")
  public List<WebElement> getItemList() {
    log.debug("Fetching all inventory items from the page");
    return waitUtils.waitUntilElementCountAtLeast(listItems, 1);
  }

  /**
   * Returns the total count of products displayed.
   *
   * @return Product count.
   */
  @Step("Get inventory list item count")
  public int getListItemsCount() {
    // Intentionally short-circuit empty states so cart-empty assertions do not wait for elements
    // that should never appear.
    int count =
        driver.findElements(listItems).isEmpty()
            ? 0
            : waitUtils.waitUntilAllVisible(listItems).size();
    log.debug("Inventory item count: {}", count);
    return count;
  }

  /**
   * Finds a product web element by its exact name.
   *
   * @param name Name of the product.
   * @return The product WebElement.
   * @throws AssertionError if the product is not found.
   */
  @Step("Find product '{0}' in inventory list")
  public WebElement getProductByName(String name) {
    log.info("Searching for product by name: {}", name);
    return waitUtils.waitUntilAllVisible(listItems).stream()
        .filter(item -> item.findElement(productNameElement).getText().equals(name))
        .findFirst()
        .orElseThrow(
            () ->
                new NoSuchElementException(
                    String.format("Product with name '%s' was not found on the page", name)));
  }

  /**
   * Extracts details for a product by its name.
   *
   * @param name Name of the product.
   * @return ProductDetails record.
   */
  @Step("Get product details for '{0}'")
  public ProductDetails getProductDetailsByName(String name) {
    log.info("Retrieving details for product: {}", name);
    WebElement product = getProductByName(name);
    ProductDetails details =
        new ProductDetails(
            product.findElement(productNameElement).getText(),
            product.findElement(productDescriptionElement).getText(),
            product.findElement(productPriceElement).getText());
    log.debug("Product details retrieved: {}", details);
    return details;
  }

  /**
   * Clicks the 'Add to Cart' or 'Remove' button for a specific product by name.
   *
   * @param name Name of the product.
   */
  @Step("Add product '{0}' to cart")
  public InventoryListComponent addProductToCart(String name) {
    log.info("Adding product to cart: {}", name);
    WebElement product = getProductByName(name);
    waitUtils.waitUntilNestedClickable(product, addToCartButton).click();
    log.debug("Successfully added product to cart: {}", name);
    return this;
  }

  @Step("Remove product '{0}' from cart")
  public InventoryListComponent removeProductFromCart(String name) {
    log.info("Removing product from cart: {}", name);
    WebElement product = getProductByName(name);
    waitUtils.waitUntilNestedClickable(product, removeButton).click();
    log.debug("Successfully removed product from cart: {}", name);
    return this;
  }

  @Step("Remove product at index {0} from cart")
  public InventoryListComponent removeProductFromCartByIndex(int index) {
    log.info("Removing product from cart at index: {}", index);
    List<WebElement> items = getItemList();
    if (index < 0 || index >= items.size()) {
      throw new NoSuchElementException(
          String.format("Product list has %d items; cannot remove index %d", items.size(), index));
    }

    waitUtils.waitUntilNestedClickable(items.get(index), removeButton).click();
    waitForItemCount(items.size() - 1);
    log.debug("Successfully removed product at index: {}", index);
    return this;
  }

  @Step("Wait for inventory list item count to become {0}")
  public void waitForItemCount(int expectedCount) {
    waitUtils.waitUntilElementCountIs(listItems, expectedCount);
  }
}
