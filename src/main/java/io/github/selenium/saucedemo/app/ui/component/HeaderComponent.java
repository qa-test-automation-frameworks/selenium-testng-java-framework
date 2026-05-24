package io.github.selenium.saucedemo.app.ui.component;

import io.github.selenium.saucedemo.app.ui.page.CartPage;
import io.github.selenium.saucedemo.app.ui.page.LoginPage;
import io.github.selenium.saucedemo.framework.ui.BaseComponent;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
    List<WebElement> visibleBadges =
        driver.findElements(CART_ITEM_COUNT).stream().filter(WebElement::isDisplayed).toList();
    if (visibleBadges.isEmpty()) {
      log.info("Cart badge is not visible; treating cart count as 0");
      return 0;
    }

    int count = Integer.parseInt(visibleBadges.get(0).getText());
    log.info("Current product count in cart badge: {}", count);
    return count;
  }

  @Step("Wait for cart badge count to become {0}")
  public void waitForProductAddedToCartCount(int expectedCount) {
    if (expectedCount == 0) {
      waitUntilCartBadgeIsEmpty();
    } else {
      waitUtils.waitUntilTextPresent(CART_ITEM_COUNT, String.valueOf(expectedCount));
    }
  }

  @Step("Wait for cart badge to be empty")
  public void waitUntilCartBadgeIsEmpty() {
    waitUtils.waitUntil(
        currentDriver ->
            currentDriver.findElements(CART_ITEM_COUNT).stream().noneMatch(WebElement::isDisplayed),
        "Cart badge should be hidden");
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
