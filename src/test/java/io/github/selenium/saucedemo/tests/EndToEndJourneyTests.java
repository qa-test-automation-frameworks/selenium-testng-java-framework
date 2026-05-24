package io.github.selenium.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.selenium.saucedemo.app.data.AppConstants;
import io.github.selenium.saucedemo.app.data.LoginRequest;
import io.github.selenium.saucedemo.app.ui.component.HeaderComponent;
import io.github.selenium.saucedemo.app.ui.page.CartPage;
import io.github.selenium.saucedemo.app.ui.page.CheckoutCompletePage;
import io.github.selenium.saucedemo.app.ui.page.CheckoutOverviewPage;
import io.github.selenium.saucedemo.app.ui.page.CheckoutPage;
import io.github.selenium.saucedemo.app.ui.page.InventoryPage;
import io.github.selenium.saucedemo.app.ui.page.ProductSortOption;
import io.github.selenium.saucedemo.framework.config.ConfigFactory;
import io.github.selenium.saucedemo.tests.data.CheckoutPricing;
import io.github.selenium.saucedemo.tests.data.CheckoutScenario;
import io.github.selenium.saucedemo.tests.data.CheckoutScenario.CheckoutInformation;
import io.github.selenium.saucedemo.tests.data.ProductCatalog;
import io.github.selenium.saucedemo.tests.data.TestGroups;
import io.github.selenium.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

@Slf4j
@Epic("Sauce Demo")
@Feature("End-to-End Journey")
@Owner("QA Automation")
public class EndToEndJourneyTests extends BaseTestCase {

  @Test(
      testName = "Verify standard user can complete a realistic shopping journey",
      description =
          "Logs in through the UI, sorts inventory, adds a product, verifies the cart and checkout overview, completes the order, and logs out.",
      groups = {TestGroups.JOURNEY, TestGroups.REGRESSION},
      timeOut = TestTimeouts.JOURNEY_TIMEOUT_MS)
  @Story("Full purchase journey")
  @Severity(SeverityLevel.BLOCKER)
  public void verifyStandardUserCanCompleteShoppingJourney() {
    var config = ConfigFactory.getConfig();
    assumePasswordConfigured();

    InventoryPage inventoryPage =
        pages()
            .login()
            .login(new LoginRequest(config.appUrl(), config.appUsername(), config.appPassword()));
    HeaderComponent header = pages().header();

    assertThat(inventoryPage.getAppLogoText())
        .as("The authenticated journey should start on the inventory page")
        .isEqualTo(AppConstants.HEADER_TITLE);

    List<BigDecimal> actualPrices =
        inventoryPage
            .sortProductsBy(ProductSortOption.PRICE_LOW_TO_HIGH)
            .getDisplayedProductPrices();
    List<BigDecimal> sortedPrices = new ArrayList<>(actualPrices);
    sortedPrices.sort(BigDecimal::compareTo);
    assertThat(actualPrices)
        .as("The journey should preserve visible sort behavior before product selection")
        .isEqualTo(sortedPrices);

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.waitForProductAddedToCartCount(1);
    CartPage cartPage = header.navigateToCart();

    assertThat(cartPage.getInventoryList().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Cart should contain the selected backpack before checkout")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage.getQuantityForProduct(ProductCatalog.BACKPACK.name()))
        .as("Cart quantity for the selected backpack should be one")
        .isEqualTo(1);

    CheckoutPage checkoutPage = cartPage.continueToCheckout();
    CheckoutInformation checkoutInformation = CheckoutScenario.validInformation();
    CheckoutOverviewPage overviewPage =
        checkoutPage.submitValidCheckoutInformation(
            checkoutInformation.firstName(),
            checkoutInformation.lastName(),
            checkoutInformation.postalCode());

    assertThat(overviewPage.getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Checkout overview should keep the selected product details")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(overviewPage.getTotal())
        .as("Checkout total should include backpack item total and tax")
        .isEqualByComparingTo(CheckoutPricing.expectedTotal(ProductCatalog.BACKPACK.price()));

    CheckoutCompletePage completePage = overviewPage.finishCheckout();
    assertThat(completePage.getConfirmationMessage())
        .as("The full shopping journey should end with an order confirmation")
        .isEqualTo("Thank you for your order!");

    assertThat(header.logout().isLoginButtonVisible())
        .as("The journey should leave the application in a logged-out state")
        .isTrue();
  }
}
