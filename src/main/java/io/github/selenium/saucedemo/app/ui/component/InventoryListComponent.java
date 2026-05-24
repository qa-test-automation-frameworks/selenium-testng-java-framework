package io.github.selenium.saucedemo.app.ui.component;

import io.github.selenium.saucedemo.app.data.ProductDetails;
import io.github.selenium.saucedemo.app.ui.ProductSelectors;
import io.github.selenium.saucedemo.app.ui.page.ProductDetailPage;
import io.github.selenium.saucedemo.framework.ui.BaseComponent;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class InventoryListComponent extends BaseComponent {

  private enum ListContext {
    INVENTORY,
    CART
  }

  private final By rootLocator;
  private final ListContext context;

  private InventoryListComponent(WebDriver driver, By rootLocator, ListContext context) {
    super(driver);
    this.rootLocator = rootLocator;
    this.context = context;
  }

  public static InventoryListComponent inventory(WebDriver driver) {
    return new InventoryListComponent(
        driver, By.cssSelector("[data-test='inventory-list']"), ListContext.INVENTORY);
  }

  public static InventoryListComponent cart(WebDriver driver) {
    return new InventoryListComponent(
        driver, By.cssSelector("[data-test='cart-list']"), ListContext.CART);
  }

  /**
   * Returns all visible list items after at least one item appears.
   *
   * <p>Do not call this method when the list may legitimately be empty, such as an empty cart. Use
   * {@link #getListItemsCount()} for empty-state assertions.
   *
   * @return List of WebElements.
   */
  @Step("Get visible inventory list items")
  public List<WebElement> getItemList() {
    log.debug("Fetching all inventory items from the page");
    return waitUtils.waitUntil(
        currentDriver -> {
          List<WebElement> items = visibleItems();
          return items.isEmpty() ? null : items;
        },
        String.format("No visible list items were found inside %s", rootLocator));
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
    int count = visibleItems().size();
    log.debug("Inventory item count: {}", count);
    return count;
  }

  @Step("Get product item component for '{0}'")
  public ProductItemComponent getProductItemByName(String name) {
    return new ProductItemComponent(driver, () -> findProductElementByName(name));
  }

  private WebElement findProductElementByName(String name) {
    log.info("Searching for product by name: {}", name);
    List<WebElement> items = getItemList();
    return items.stream()
        .filter(item -> item.findElement(ProductSelectors.NAME).getText().equals(name))
        .findFirst()
        .orElseThrow(() -> missingProduct(name, items));
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
    ProductDetails details = getProductItemByName(name).details();
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
    getProductItemByName(name).addToCart();
    waitForActionButtonText(name, "Remove");
    log.debug("Successfully added product to cart: {}", name);
    return this;
  }

  @Step("Remove product '{0}' from cart")
  public InventoryListComponent removeProductFromCart(String name) {
    log.info("Removing product from cart: {}", name);
    int startingCount = getListItemsCount();
    getProductItemByName(name).removeFromCart();
    if (context == ListContext.CART) {
      waitForItemCount(startingCount - 1);
    } else {
      waitForActionButtonText(name, "Add to cart");
    }
    log.debug("Successfully removed product from cart: {}", name);
    return this;
  }

  @Step("Open product detail page for '{0}'")
  public ProductDetailPage openProductDetail(String name) {
    log.info("Opening product detail page for: {}", name);
    getProductItemByName(name).openDetail();
    return new ProductDetailPage(driver).waitUntilLoaded();
  }

  @Step("Get action button text for product '{0}'")
  public String getActionButtonText(String name) {
    return getProductItemByName(name).actionButtonText();
  }

  @Step("Wait for inventory list item count to become {0}")
  public void waitForItemCount(int expectedCount) {
    waitUtils.waitUntil(
        currentDriver -> visibleItems().size() == expectedCount,
        String.format(
            "List item count inside %s did not become %d within the configured timeout",
            rootLocator, expectedCount));
  }

  @Step("Wait for product '{0}' action button text to become '{1}'")
  public void waitForActionButtonText(String name, String expectedText) {
    waitUtils.waitUntil(
        currentDriver -> expectedText.equals(getActionButtonText(name)),
        String.format(
            "Action button text for product '%s' did not become '%s'", name, expectedText));
  }

  private List<WebElement> visibleItems() {
    WebElement root = waitUtils.waitUntilVisible(rootLocator);
    return root.findElements(ProductSelectors.LIST_ITEM).stream()
        .filter(WebElement::isDisplayed)
        .toList();
  }

  private NoSuchElementException missingProduct(String name, List<WebElement> visibleItems) {
    List<String> visibleNames =
        visibleItems.stream()
            .map(item -> item.findElement(ProductSelectors.NAME).getText())
            .toList();
    return new NoSuchElementException(
        String.format("Product '%s' was not found. Visible products: %s", name, visibleNames));
  }
}
