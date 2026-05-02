package common.pageobject.component;

import static org.assertj.core.api.Assertions.assertThat;

import common.pageobject.BasePage;
import io.qameta.allure.Step;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@Slf4j
public class HeaderComponent extends BasePage {

  private final By cartButton = By.id("shopping_cart_container");
  private final By cartItemCount = By.cssSelector("[data-test='shopping-cart-badge']");
  private final By menuButton = By.id("react-burger-menu-btn");
  private final By logoutButton = By.id("logout_sidebar_link");

  public HeaderComponent(WebDriver driver) {
    super(driver);
  }

  public boolean isCartButtonVisible() {
    boolean visible = driver.findElement(cartButton).isDisplayed();
    log.debug("Cart button visibility: {}", visible);
    return visible;
  }

  @Step("Navigate to cart via header")
  public void navigateToCart() {
    log.info("Navigating to cart page via header icon");
    assertThat(isCartButtonVisible())
        .as("Shopping cart button should be visible before clicking")
        .isTrue();
    driver.findElement(cartButton).click();
    waitUtils.waitForPageLoad();
    log.debug("Cart page load completed");
  }

  public int getProductAddedToCartCount() {
    List<WebElement> badges = driver.findElements(cartItemCount);
    int count = badges.isEmpty() ? 0 : Integer.parseInt(badges.get(0).getText());
    log.info("Current product count in cart badge: {}", count);
    return count;
  }

  public boolean isMenuButtonVisible() {
    boolean visible = driver.findElement(menuButton).isDisplayed();
    log.debug("Menu button visibility: {}", visible);
    return visible;
  }

  @Step("Open side menu via header")
  public void openMenu() {
    log.info("Opening the side menu");
    assertThat(isMenuButtonVisible())
        .as("Side menu burger button should be visible before clicking")
        .isTrue();
    driver.findElement(menuButton).click();
    log.debug("Side menu opened");
  }

  @Step("Click logout button in side menu")
  public void clickLogoutButton() {
    log.info("Clicking the logout button in the side menu");
    waitUtils.waitUntilVisible(logoutButton);
    driver.findElement(logoutButton).click();
    log.debug("Successfully clicked logout button");
  }
}
