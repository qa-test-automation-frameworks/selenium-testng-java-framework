package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.saucedemo.framework.pageobject.CartPage;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.framework.util.AuthService;
import com.example.saucedemo.tests.data.ProductCatalog;
import com.example.saucedemo.tests.data.TestGroups;
import com.example.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Description;
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
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Description(
      "Adds two products from the inventory page and verifies the cart badge reflects the combined count.")
  @Story("Add products to cart")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyUserCanAddProductsToCart() {
    InventoryPage inventoryPage = new InventoryPage(getDriver()).waitUntilLoaded();
    HeaderComponent header = new HeaderComponent(getDriver());

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
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Description(
      "Adds products from inventory, opens the cart, and verifies the selected item details and quantities.")
  @Story("Cart contents")
  @Severity(SeverityLevel.NORMAL)
  public void verifyCartDisplaysSelectedProducts() {
    InventoryPage inventoryPage = new InventoryPage(getDriver()).waitUntilLoaded();
    HeaderComponent header = new HeaderComponent(getDriver());

    log.info("Adding products to cart and navigating to cart page");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BOLT_TSHIRT.name());

    header.navigateToCart();
    CartPage cartPage = new CartPage(getDriver()).waitUntilLoaded();

    assertThat(cartPage.getInventoryList().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details in cart should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage.getProductQuantityByIndex(0))
        .as("Quantity for the first item in cart should be 1")
        .isEqualTo(1);

    assertThat(
            cartPage.getInventoryList().getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details in cart should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(cartPage.getProductQuantityByIndex(1))
        .as("Quantity for the second item in cart should be 1")
        .isEqualTo(1);
  }

  @Test(
      testName = "Verify removing products from cart",
      description =
          "Removes products one by one from the cart page and verifies the item count decreases to zero.",
      groups = {TestGroups.CART, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Description(
      "Removes products one by one from the cart page and verifies the item count decreases to zero.")
  @Story("Remove products from cart")
  @Severity(SeverityLevel.NORMAL)
  public void verifyUserCanRemoveProductsFromCart() {
    InventoryPage inventoryPage = new InventoryPage(getDriver()).waitUntilLoaded();
    HeaderComponent header = new HeaderComponent(getDriver());

    log.info("Adding products and navigating to cart for removal");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BIKE_LIGHT.name());

    header.navigateToCart();
    CartPage cartPage = new CartPage(getDriver()).waitUntilLoaded();

    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("There should be 2 items in the cart initially")
        .isEqualTo(2);

    log.info("Removing first item from cart");
    cartPage.removeCartItemAtIndex(0);
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("After removing one item, there should be 1 item left in the cart")
        .isEqualTo(1);

    log.info("Removing second item from cart");
    cartPage.removeCartItemAtIndex(0);
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("After removing all items, the cart should be empty")
        .isEqualTo(0);
  }

  @Test(
      testName = "Verify empty cart shows no items",
      description =
          "Opens the cart before adding any products and verifies that no cart items are displayed.",
      groups = {TestGroups.CART, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Description(
      "Opens the cart before adding any products and verifies that no cart items are displayed.")
  @Story("Cart contents")
  @Severity(SeverityLevel.NORMAL)
  public void verifyEmptyCartDisplaysNoItems() {
    HeaderComponent header = new HeaderComponent(getDriver());

    header.navigateToCart();
    CartPage cartPage = new CartPage(getDriver()).waitUntilLoaded();

    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("A cart opened before adding products should not contain items")
        .isZero();
  }
}
