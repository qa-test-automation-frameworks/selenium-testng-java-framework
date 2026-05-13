package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import io.github.prayag.saucedemo.app.ui.component.InventoryListComponent;
import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutOverviewPage extends BasePage implements PageLoadable<CheckoutOverviewPage> {

  private static final By FINISH_BUTTON = By.cssSelector("[data-test='finish']");
  private static final By ITEM_TOTAL = By.cssSelector("[data-test='subtotal-label']");
  private static final By TAX = By.cssSelector("[data-test='tax-label']");
  private static final By TOTAL = By.cssSelector("[data-test='total-label']");

  private final InventoryListComponent inventoryList;

  public CheckoutOverviewPage(WebDriver driver) {
    super(driver);
    this.inventoryList = InventoryListComponent.cart(driver);
  }

  @Override
  public CheckoutOverviewPage waitUntilLoaded() {
    waitUntilUrlContains("checkout-step-two");
    waitUtils.waitUntilVisible(ITEM_TOTAL);
    waitUtils.waitUntilVisible(TAX);
    waitUtils.waitUntilVisible(TOTAL);
    waitUtils.waitUntilVisible(FINISH_BUTTON);
    return this;
  }

  @Step("Get checkout overview product details for '{0}'")
  public ProductDetails getProductDetailsByName(String productName) {
    return inventoryList.getProductDetailsByName(productName);
  }

  @Step("Get checkout item total")
  public BigDecimal getItemTotal() {
    return moneyValue(ITEM_TOTAL);
  }

  @Step("Get checkout tax")
  public BigDecimal getTax() {
    return moneyValue(TAX);
  }

  @Step("Get checkout total")
  public BigDecimal getTotal() {
    return moneyValue(TOTAL);
  }

  @Step("Finish checkout")
  public CheckoutCompletePage finishCheckout() {
    log.info("Finishing checkout");
    waitUtils.click(FINISH_BUTTON);
    return new CheckoutCompletePage(driver).waitUntilLoaded();
  }

  private BigDecimal moneyValue(By locator) {
    String amount = waitUtils.waitUntilVisible(locator).getText().replaceAll(".*\\$", "");
    return new BigDecimal(amount);
  }
}
