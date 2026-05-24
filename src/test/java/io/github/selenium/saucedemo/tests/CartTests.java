package io.github.selenium.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.selenium.saucedemo.app.auth.AuthService;
import io.github.selenium.saucedemo.app.ui.component.HeaderComponent;
import io.github.selenium.saucedemo.app.ui.page.CartPage;
import io.github.selenium.saucedemo.app.ui.page.InventoryPage;
import io.github.selenium.saucedemo.tests.data.ProductCatalog;
import io.github.selenium.saucedemo.tests.data.TestGroups;
import io.github.selenium.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
@Epic("Sauce Demo")
@Feature("Cart")
@Owner("QA Automation")
public class CartTests extends BaseTestCase {

  @BeforeMethod(alwaysRun = true, description = "Authenticate via cookie and open inventory page")
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @Test(
      testName = "Verify adding multiple products to cart",
      description =
          "Adds two products from the inventory page and verifies the cart badge reflects the combined count.",
      groups = {TestGroups.SMOKE, TestGroups.CART},
      timeOut = TestTimeouts.FAST_UI_TIMEOUT_MS)
  @Story("Add products to cart")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyUserCanAddProductsToCart() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    log.info("Adding two products to the cart");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BOLT_TSHIRT.name());
    header.waitForProductAddedToCartCount(2);

    assertThat(header.getProductAddedToCartCount())
        .as("The cart badge should show 2 items added")
        .isEqualTo(2);
  }

  @Test(
      testName = "Verify added products are visible in cart",
      description =
          "Adds products from inventory, opens the cart, and verifies the selected item details and quantities.",
      groups = {TestGroups.CART, TestGroups.REGRESSION},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Cart contents")
  @Severity(SeverityLevel.NORMAL)
  public void verifyCartDisplaysSelectedProducts() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    log.info("Adding products to cart and navigating to cart page");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BOLT_TSHIRT.name());

    CartPage cartPage = header.navigateToCart();

    assertThat(cartPage.getInventoryList().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details in cart should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage.getQuantityForProduct(ProductCatalog.BACKPACK.name()))
        .as("Quantity for Backpack in cart should be 1")
        .isEqualTo(1);

    assertThat(
            cartPage.getInventoryList().getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details in cart should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(cartPage.getQuantityForProduct(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Quantity for Bolt T-shirt in cart should be 1")
        .isEqualTo(1);
  }

  @Test(
      testName = "Verify removing products from cart",
      description =
          "Removes products one by one from the cart page and verifies the item count decreases to zero.",
      groups = {TestGroups.CART, TestGroups.REGRESSION},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Remove products from cart")
  @Severity(SeverityLevel.NORMAL)
  public void verifyUserCanRemoveProductsFromCart() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    log.info("Adding products and navigating to cart for removal");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BIKE_LIGHT.name());

    CartPage cartPage = header.navigateToCart();

    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("There should be 2 items in the cart initially")
        .isEqualTo(2);

    log.info("Removing Backpack from cart");
    cartPage.removeProduct(ProductCatalog.BACKPACK.name());
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("After removing Backpack, there should be 1 item left in the cart")
        .isEqualTo(1);

    log.info("Removing Bike Light from cart");
    cartPage.removeProduct(ProductCatalog.BIKE_LIGHT.name());
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("After removing all items, the cart should be empty")
        .isZero();
  }

  @Test(
      testName = "Verify empty cart shows no items",
      description =
          "Opens the cart before adding any products and verifies that no cart items are displayed.",
      groups = {TestGroups.CART, TestGroups.REGRESSION},
      timeOut = TestTimeouts.FAST_UI_TIMEOUT_MS)
  @Story("Cart contents")
  @Severity(SeverityLevel.NORMAL)
  public void verifyEmptyCartDisplaysNoItems() {
    HeaderComponent header = pages().header();

    CartPage cartPage = header.navigateToCart();

    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("A cart opened before adding products should not contain items")
        .isZero();
  }
}
