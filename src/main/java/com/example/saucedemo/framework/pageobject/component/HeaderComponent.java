package com.example.saucedemo.framework.pageobject.component;

import com.example.saucedemo.framework.pageobject.BaseComponent;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class HeaderComponent extends BaseComponent {

  private final By cartButton = By.id("shopping_cart_container");
  private final By cartItemCount = By.cssSelector("[data-test='shopping-cart-badge']");
  private final By menuButton = By.id("react-burger-menu-btn");
  private final By logoutButton = By.id("logout_sidebar_link");

  public HeaderComponent(WebDriver driver) {
    super(driver);
  }

  @Step("Check cart button visibility")
  public boolean isCartButtonVisible() {
    boolean visible = waitUtils.isVisible(cartButton);
    log.debug("Cart button visibility: {}", visible);
    return visible;
  }

  @Step("Navigate to cart via header")
  public void navigateToCart() {
    log.info("Navigating to cart page via header icon");
    waitUtils.click(cartButton);
    waitUtils.waitForPageLoad();
    log.debug("Cart page load completed");
  }

  @Step("Get product count from cart badge")
  public int getProductAddedToCartCount() {
    List<WebElement> badges = driver.findElements(cartItemCount);
    int count = badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    log.info("Current product count in cart badge: {}", count);
    return count;
  }

  public void waitForProductAddedToCartCount(int expectedCount) {
    if (expectedCount == 0) {
      waitUtils.waitUntilElementCountIs(cartItemCount, 0);
    } else {
      waitUtils.waitUntilTextPresent(cartItemCount, String.valueOf(expectedCount));
    }
  }

  @Step("Check menu button visibility")
  public boolean isMenuButtonVisible() {
    boolean visible = waitUtils.isVisible(menuButton);
    log.debug("Menu button visibility: {}", visible);
    return visible;
  }

  @Step("Open side menu via header")
  public void openMenu() {
    log.info("Opening the side menu");
    waitUtils.click(menuButton);
    log.debug("Side menu opened");
  }

  @Step("Click logout button in side menu")
  public void clickLogoutButton() {
    log.info("Clicking the logout button in the side menu");
    waitUtils.click(logoutButton);
    log.debug("Successfully clicked logout button");
  }

  @Step("Logout from application")
  public void logout() {
    log.info("Starting logout process");
    waitUtils.waitForPageLoad();
    openMenu();
    clickLogoutButton();
    log.info("Logout process completed");
  }
}
