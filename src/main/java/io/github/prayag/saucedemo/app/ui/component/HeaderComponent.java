package io.github.prayag.saucedemo.app.ui.component;

import io.github.prayag.saucedemo.app.ui.page.CartPage;
import io.github.prayag.saucedemo.app.ui.page.LoginPage;
import io.github.prayag.saucedemo.framework.ui.BaseComponent;
import io.qameta.allure.Step;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class HeaderComponent extends BaseComponent {

  private static final By CART_BUTTON = By.id("shopping_cart_container");
  private static final By CART_ITEM_COUNT = By.cssSelector("[data-test='shopping-cart-badge']");
  private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
  private static final By LOGOUT_BUTTON = By.id("logout_sidebar_link");

  public HeaderComponent(WebDriver driver) {
    super(driver);
  }

  public boolean isCartButtonVisible() {
    boolean visible = waitUtils.isVisible(CART_BUTTON);
    log.debug("Cart button visibility: {}", visible);
    return visible;
  }

  @Step("Navigate to cart via header")
  public CartPage navigateToCart() {
    log.info("Navigating to cart page via header icon");
    CartPage cartPage;
    waitUtils.click(CART_BUTTON);
    cartPage = new CartPage(driver).waitUntilLoaded();
    log.debug("Cart page load completed");
    return cartPage;
  }

  @Step("Get product count from cart badge")
  public int getProductAddedToCartCount() {
    if (!waitUtils.isVisible(CART_ITEM_COUNT, Duration.ofSeconds(1))) {
      log.info("Cart badge is not visible; treating cart count as 0");
      return 0;
    }

    int count = Integer.parseInt(waitUtils.waitUntilVisible(CART_ITEM_COUNT).getText());
    log.info("Current product count in cart badge: {}", count);
    return count;
  }

  @Step("Wait for cart badge count to become {0}")
  public void waitForProductAddedToCartCount(int expectedCount) {
    if (expectedCount == 0) {
      waitUtils.waitUntilInvisibleOrAbsent(CART_ITEM_COUNT, "Cart badge should be hidden");
    } else {
      waitUtils.waitUntilTextPresent(CART_ITEM_COUNT, String.valueOf(expectedCount));
    }
  }

  public boolean isMenuButtonVisible() {
    boolean visible = waitUtils.isVisible(MENU_BUTTON);
    log.debug("Menu button visibility: {}", visible);
    return visible;
  }

  @Step("Open side menu via header")
  public void openMenu() {
    log.info("Opening the side menu");
    waitUtils.click(MENU_BUTTON);
    log.debug("Side menu opened");
  }

  @Step("Click logout button in side menu")
  public void clickLogoutButton() {
    log.info("Clicking the logout button in the side menu");
    waitUtils.click(LOGOUT_BUTTON);
    log.debug("Successfully clicked logout button");
  }

  @Step("Logout from application")
  public LoginPage logout() {
    log.info("Starting logout process");
    openMenu();
    clickLogoutButton();
    LoginPage loginPage = new LoginPage(driver).waitUntilLoaded();
    log.info("Logout process completed");
    return loginPage;
  }
}
