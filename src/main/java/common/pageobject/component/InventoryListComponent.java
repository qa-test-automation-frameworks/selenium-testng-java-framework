package common.pageobject.component;

import common.data.ProductDetails;
import common.pageobject.BaseComponent;
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
      By.cssSelector(
          "[data-test='inventory-item'], [data-test='cart-item'], .inventory_item, .cart_item");
  protected final By productNameElement = By.cssSelector("[data-test='inventory-item-name']");
  protected final By productDescriptionElement =
      By.cssSelector("[data-test='inventory-item-desc']");
  protected final By productPriceElement = By.cssSelector("[data-test='inventory-item-price']");
  private final By productCartButton =
      By.cssSelector(
          "button[data-test^='add-to-cart'], button[data-test^='remove'], button.btn_inventory, button.btn_secondary.cart_button");

  public InventoryListComponent(WebDriver driver) {
    super(driver);
  }

  /**
   * Returns a list of all product web elements currently on the page.
   *
   * @return List of WebElements.
   */
  public List<WebElement> getItemList() {
    log.debug("Fetching all inventory items from the page");
    return driver.findElements(listItems);
  }

  /**
   * Returns the total count of products displayed.
   *
   * @return Product count.
   */
  public int getListItemsCount() {
    int count = getItemList().size();
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
  public WebElement getProductByName(String name) {
    log.info("Searching for product by name: {}", name);
    waitUtils.waitUntilAllVisible(listItems);
    String xpath =
        String.format(
            "//*[self::div or self::li][contains(concat(' ', normalize-space(@class), ' '), ' inventory_item ') "
                + "or contains(concat(' ', normalize-space(@class), ' '), ' cart_item ') "
                + "or @data-test='inventory-item' or @data-test='cart-item']"
                + "[descendant::*[@data-test='inventory-item-name' and text()='%s']]",
            name);
    try {
      return driver.findElement(By.xpath(xpath));
    } catch (NoSuchElementException e) {
      throw new NoSuchElementException(
          String.format("Product with name '%s' was not found on the page", name));
    }
  }

  /**
   * Extracts details for a product by its name.
   *
   * @param name Name of the product.
   * @return ProductDetails record.
   */
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
  public InventoryListComponent clickProductCartButtonByName(String name) {
    log.info("Clicking cart button for product: {}", name);
    WebElement product = getProductByName(name);
    waitUtils.waitUntilNestedClickable(product, productCartButton).click();
    log.debug("Successfully clicked cart button for: {}", name);
    return this;
  }

  /**
   * Clicks the cart button for a product at a specific index.
   *
   * @param index Zero-based index of the product.
   */
  @Step("Add product at index {0} to cart")
  public InventoryListComponent clickProductCartButtonByIndex(int index) {
    log.info("Clicking cart button for product at index: {}", index);
    List<WebElement> items = getItemList();
    if (index < 0 || index >= items.size()) {
      throw new NoSuchElementException(
          String.format("Product list has %d items; cannot click index %d", items.size(), index));
    }

    waitUtils.waitUntilNestedClickable(items.get(index), productCartButton).click();
    log.debug("Successfully clicked cart button at index: {}", index);
    return this;
  }
}
