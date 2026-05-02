package common.pageobject.component;

import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import util.WaitUtils;

@Slf4j
public class InventoryListComponent {

  private final WebDriver driver;
  private final WaitUtils waitUtils;

  protected final By listItems = By.cssSelector("[data-test='inventory-item']");
  protected final By productNameElement = By.cssSelector("[data-test='inventory-item-name']");
  protected final By productDescriptionElement =
      By.cssSelector("[data-test='inventory-item-desc']");
  protected final By productPriceElement = By.cssSelector("[data-test='inventory-item-price']");
  private final By productCartButton =
      By.cssSelector("button.btn_inventory, button.btn_secondary.cart_button");

  /** Record representing product details (Name, Description, Price). */
  public record ProductDetails(String name, String description, String price) {}

  public InventoryListComponent(WebDriver driver, WaitUtils waitUtils) {
    this.driver = driver;
    this.waitUtils = waitUtils;
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
            "//div[@data-test='inventory-item' and descendant::div[@data-test='inventory-item-name' and text()='%s']]",
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
  public void clickProductCartButtonByName(String name) {
    log.info("Clicking cart button for product: {}", name);
    waitUtils.waitForPageLoad();
    WebElement product = getProductByName(name);
    waitUtils.waitUntilNestedClickable(product, productCartButton).click();
    log.debug("Successfully clicked cart button for: {}", name);
  }

  /**
   * Clicks the cart button for a product at a specific index.
   *
   * @param index Zero-based index of the product.
   */
  @Step("Add product at index {0} to cart")
  public void clickProductCartButtonByIndex(int index) {
    log.info("Clicking cart button for product at index: {}", index);
    waitUtils.waitForPageLoad();
    List<WebElement> items = getItemList();
    if (index < 0 || index >= items.size()) {
      throw new NoSuchElementException(
          String.format("Product list has %d items; cannot click index %d", items.size(), index));
    }

    waitUtils.waitUntilNestedClickable(items.get(index), productCartButton).click();
    log.debug("Successfully clicked cart button at index: {}", index);
  }
}
