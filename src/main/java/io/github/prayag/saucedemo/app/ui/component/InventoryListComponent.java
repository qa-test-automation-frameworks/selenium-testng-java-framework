package io.github.prayag.saucedemo.app.ui.component;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.app.ui.page.ProductDetailPage;
import io.github.prayag.saucedemo.framework.ui.BaseComponent;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class InventoryListComponent extends BaseComponent {

  private static final By LIST_ITEMS =
      By.cssSelector("[data-test='inventory-item'], [data-test='cart-item']");
  private static final By PRODUCT_NAME_ELEMENT =
      By.cssSelector("[data-test='inventory-item-name']");
  private static final By PRODUCT_DESCRIPTION_ELEMENT =
      By.cssSelector("[data-test='inventory-item-desc']");
  private static final By PRODUCT_PRICE_ELEMENT =
      By.cssSelector("[data-test='inventory-item-price']");
  private static final By ADD_TO_CART_BUTTON = By.cssSelector("button[data-test^='add-to-cart']");
  private static final By REMOVE_BUTTON = By.cssSelector("button[data-test^='remove']");

  private final By rootLocator;

  private InventoryListComponent(WebDriver driver, By rootLocator) {
    super(driver);
    this.rootLocator = rootLocator;
  }

  public static InventoryListComponent inventory(WebDriver driver) {
    return new InventoryListComponent(driver, By.cssSelector("[data-test='inventory-list']"));
  }

  public static InventoryListComponent cart(WebDriver driver) {
    return new InventoryListComponent(driver, By.cssSelector("[data-test='cart-list']"));
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
    List<WebElement> items = getItemList();
    return items.stream()
        .filter(item -> item.findElement(PRODUCT_NAME_ELEMENT).getText().equals(name))
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
    WebElement product = getProductByName(name);
    ProductDetails details =
        new ProductDetails(
            product.findElement(PRODUCT_NAME_ELEMENT).getText(),
            product.findElement(PRODUCT_DESCRIPTION_ELEMENT).getText(),
            product.findElement(PRODUCT_PRICE_ELEMENT).getText());
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
    waitUtils.waitUntilNestedClickable(() -> getProductByName(name), ADD_TO_CART_BUTTON).click();
    waitForActionButtonText(name, "Remove");
    log.debug("Successfully added product to cart: {}", name);
    return this;
  }

  @Step("Remove product '{0}' from cart")
  public InventoryListComponent removeProductFromCart(String name) {
    log.info("Removing product from cart: {}", name);
    boolean cartList = rootLocator.toString().contains("cart-list");
    int startingCount = getListItemsCount();
    waitUtils.waitUntilNestedClickable(() -> getProductByName(name), REMOVE_BUTTON).click();
    if (cartList) {
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
    waitUtils.waitUntilNestedClickable(() -> getProductByName(name), PRODUCT_NAME_ELEMENT).click();
    return new ProductDetailPage(driver).waitUntilLoaded();
  }

  @Step("Get action button text for product '{0}'")
  public String getActionButtonText(String name) {
    WebElement product = getProductByName(name);
    if (product.findElements(ADD_TO_CART_BUTTON).stream().anyMatch(WebElement::isDisplayed)) {
      return product.findElement(ADD_TO_CART_BUTTON).getText();
    }
    return product.findElement(REMOVE_BUTTON).getText();
  }

  @Step("Remove product at index {0} from cart")
  public InventoryListComponent removeProductFromCartByIndex(int index) {
    log.info("Removing product from cart at index: {}", index);
    List<WebElement> items = getItemList();
    if (index < 0 || index >= items.size()) {
      throw new NoSuchElementException(
          String.format("Product list has %d items; cannot remove index %d", items.size(), index));
    }

    waitUtils.waitUntilNestedClickable(items.get(index), REMOVE_BUTTON).click();
    waitForItemCount(items.size() - 1);
    log.debug("Successfully removed product at index: {}", index);
    return this;
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
    return root.findElements(LIST_ITEMS).stream().filter(WebElement::isDisplayed).toList();
  }

  private NoSuchElementException missingProduct(String name, List<WebElement> visibleItems) {
    List<String> visibleNames =
        visibleItems.stream()
            .map(item -> item.findElement(PRODUCT_NAME_ELEMENT).getText())
            .toList();
    return new NoSuchElementException(
        String.format("Product '%s' was not found. Visible products: %s", name, visibleNames));
  }
}
