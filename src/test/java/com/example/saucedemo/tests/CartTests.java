package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.saucedemo.framework.data.AppConstants;
import com.example.saucedemo.framework.pageobject.CartPage;
import com.example.saucedemo.framework.pageobject.CheckoutCompletePage;
import com.example.saucedemo.framework.pageobject.CheckoutOverviewPage;
import com.example.saucedemo.framework.pageobject.CheckoutPage;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.framework.util.AuthService;
import com.example.saucedemo.tests.data.CheckoutScenario;
import com.example.saucedemo.tests.data.CheckoutScenario.CheckoutInformation;
import com.example.saucedemo.tests.data.ProductCatalog;
import com.example.saucedemo.tests.data.TestGroups;
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
@Feature("Cart and Checkout")
@Owner("QA Automation")
public class CartTests extends BaseTestCase {

  @BeforeMethod(alwaysRun = true)
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @Test(
      testName = "Verify adding multiple products to cart",
      groups = {TestGroups.SMOKE, TestGroups.CART})
  @Story("Add products to cart")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyUserCanAddProductsToCart() {
    log.info("Starting test: verifyUserCanAddProductsToCart");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);

    log.info("Adding two products to the cart");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BOLT_TSHIRT.name());
    header.waitForProductAddedToCartCount(2);

    assertThat(header.getProductAddedToCartCount())
        .as("The cart badge should show 2 items added")
        .isEqualTo(2);
    log.info("Finished test successfully: verifyUserCanAddProductsToCart");
  }

  @Test(
      testName = "Verify added products are visible in cart",
      groups = {TestGroups.CART, TestGroups.REGRESSION})
  @Story("Cart contents")
  @Severity(SeverityLevel.NORMAL)
  public void verifyCartDisplaysSelectedProducts() {
    log.info("Starting test: verifyCartDisplaysSelectedProducts");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    log.info("Adding products to cart and navigating to cart page");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BOLT_TSHIRT.name());

    header.navigateToCart();
    CartPage cartPage = new CartPage(getDriver());

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
    log.info("Finished test successfully: verifyCartDisplaysSelectedProducts");
  }

  @Test(
      testName = "Verify removing products from cart",
      groups = {TestGroups.CART, TestGroups.REGRESSION})
  @Story("Remove products from cart")
  @Severity(SeverityLevel.NORMAL)
  public void verifyUserCanRemoveProductsFromCart() {
    log.info("Starting test: verifyUserCanRemoveProductsFromCart");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    log.info("Adding products and navigating to cart for removal");
    inventoryPage
        .addProductToCart(ProductCatalog.BACKPACK.name())
        .addProductToCart(ProductCatalog.BIKE_LIGHT.name());

    header.navigateToCart();
    CartPage cartPage = new CartPage(getDriver());

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
    log.info("Finished test successfully: verifyUserCanRemoveProductsFromCart");
  }

  @Test(
      testName = "Verify checkout requires first name, last name, and postal code",
      groups = {TestGroups.CHECKOUT, TestGroups.REGRESSION})
  @Story("Checkout validation")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyCheckoutShowsValidationForMissingRequiredFields() {
    log.info("Starting test: verifyCheckoutShowsValidationForMissingRequiredFields");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.navigateToCart();

    CartPage cartPage = new CartPage(getDriver());
    CheckoutPage checkoutPage = cartPage.continueToCheckout();
    CheckoutInformation missingInformation = CheckoutScenario.emptyInformation();
    assertThat(
            checkoutPage
                .submitCheckoutInformation(
                    missingInformation.firstName(),
                    missingInformation.lastName(),
                    missingInformation.postalCode())
                .getErrorMessage())
        .as("Checkout should require first name first")
        .isEqualTo("Error: First Name is required");
    log.info("Finished test successfully: verifyCheckoutShowsValidationForMissingRequiredFields");
  }

  @Test(
      testName = "Verify user can complete checkout",
      groups = {TestGroups.SMOKE, TestGroups.CHECKOUT})
  @Story("Checkout completion")
  @Severity(SeverityLevel.BLOCKER)
  public void verifyUserCanCompleteCheckout() {
    log.info("Starting test: verifyUserCanCompleteCheckout");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.navigateToCart();

    CheckoutPage checkoutPage = new CartPage(getDriver()).continueToCheckout();
    CheckoutInformation validInformation = CheckoutScenario.validInformation();
    checkoutPage.submitCheckoutInformation(
        validInformation.firstName(), validInformation.lastName(), validInformation.postalCode());
    CheckoutOverviewPage overviewPage = new CheckoutOverviewPage(getDriver());
    CheckoutCompletePage completePage = overviewPage.finishCheckout();

    assertThat(completePage.getConfirmationMessage())
        .as("Checkout success message should be displayed")
        .isEqualTo("Thank you for your order!");
    log.info("Finished test successfully: verifyUserCanCompleteCheckout");
  }

  @Test(
      testName = "Verify checkout requires last name",
      groups = {TestGroups.CHECKOUT, TestGroups.REGRESSION})
  @Story("Checkout validation")
  @Severity(SeverityLevel.NORMAL)
  public void verifyCheckoutRequiresLastName() {
    log.info("Starting test: verifyCheckoutRequiresLastName");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.navigateToCart();

    CheckoutPage checkoutPage = new CartPage(getDriver()).continueToCheckout();
    CheckoutInformation missingLastName = CheckoutScenario.missingLastName();

    assertThat(
            checkoutPage
                .submitCheckoutInformation(
                    missingLastName.firstName(),
                    missingLastName.lastName(),
                    missingLastName.postalCode())
                .getErrorMessage())
        .as("Checkout should require last name when first name is present")
        .isEqualTo("Error: Last Name is required");
    log.info("Finished test successfully: verifyCheckoutRequiresLastName");
  }

  @Test(
      testName = "Verify empty cart shows no items",
      groups = {TestGroups.CART, TestGroups.REGRESSION})
  @Story("Cart contents")
  @Severity(SeverityLevel.NORMAL)
  public void verifyEmptyCartDisplaysNoItems() {
    log.info("Starting test: verifyEmptyCartDisplaysNoItems");
    HeaderComponent header = new HeaderComponent(getDriver());

    header.navigateToCart();
    CartPage cartPage = new CartPage(getDriver());

    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("A cart opened before adding products should not contain items")
        .isZero();
    log.info("Finished test successfully: verifyEmptyCartDisplaysNoItems");
  }
}
