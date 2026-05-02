package common.pageobject;

import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class CartPage extends BasePage {

  public CartPage(WebDriver driver) {
    super(driver);
  }

  private final By productQuantityElement = By.cssSelector("[data-test='item-quantity']");

  @Step("Get product quantity at index {0} in cart")
  public int getProductQuantityByIndex(int index) {
    log.info("Retrieving product quantity for item at index {} in the cart", index);
    List<WebElement> items = getInventoryList().getItemList();
    if (index < 0 || index >= items.size()) {
      throw new NoSuchElementException(
          String.format(
              "Cart has %d items; cannot read quantity at index %d", items.size(), index));
    }

    String quantityText = items.get(index).findElement(productQuantityElement).getText();
    int quantity = Integer.parseInt(quantityText);
    log.debug("Quantity for item at index {}: {}", index, quantity);
    return quantity;
  }
}
