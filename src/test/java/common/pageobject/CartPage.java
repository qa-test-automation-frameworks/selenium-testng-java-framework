package common.pageobject;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

@Slf4j
public class CartPage extends BasePage {

  public CartPage(WebDriver driver) {
    super(driver);
  }

  private final By productQuantityElement = By.cssSelector("[data-test='item-quantity']");

  @Step("Get product quantity at index {0} in cart")
  public int getProductQuantityByIndex(int index) {
    log.info("Retrieving product quantity for item at index {} in the cart", index);
    List<WebElement> items = getItemList();
    
    assertThat(items)
        .as("Cart should have at least %d items to check index %d", index + 1, index)
        .hasSizeGreaterThan(index);

    String quantityText = items.get(index).findElement(productQuantityElement).getText();
    int quantity = Integer.parseInt(quantityText);
    log.debug("Quantity for item at index {}: {}", index, quantity);
    return quantity;
  }
}
