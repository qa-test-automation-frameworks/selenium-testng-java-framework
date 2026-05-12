package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.saucedemo.framework.config.ConfigFactory;
import com.example.saucedemo.framework.data.AppConstants;
import com.example.saucedemo.framework.data.LoginRequest;
import com.example.saucedemo.framework.pageobject.CartPage;
import com.example.saucedemo.framework.pageobject.CheckoutCompletePage;
import com.example.saucedemo.framework.pageobject.CheckoutOverviewPage;
import com.example.saucedemo.framework.pageobject.CheckoutPage;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.LoginPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.tests.data.CheckoutScenario;
import com.example.saucedemo.tests.data.CheckoutScenario.CheckoutInformation;
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
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Description(
      "Logs in through the UI, sorts inventory, adds a product, verifies the cart and checkout overview, completes the order, and logs out.")
  @Story("Full purchase journey")
  @Severity(SeverityLevel.BLOCKER)
  public void verifyStandardUserCanCompleteShoppingJourney() {
    var config = ConfigFactory.getConfig();
    ConfigFactory.requireLoginPassword(config);

    LoginPage loginPage = pages().login();
    loginPage.login(new LoginRequest(config.appUrl(), config.appUsername(), config.appPassword()));

    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    assertThat(inventoryPage.getHeaderText())
        .as("The authenticated journey should start on the inventory page")
        .isEqualTo(AppConstants.HEADER_TITLE);

    List<BigDecimal> actualPrices =
        inventoryPage.sortProductsByPriceLowToHigh().getDisplayedProductPrices();
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
        .isEqualByComparingTo(new BigDecimal("32.39"));

    CheckoutCompletePage completePage = overviewPage.finishCheckout();
    assertThat(completePage.getConfirmationMessage())
        .as("The full shopping journey should end with an order confirmation")
        .isEqualTo("Thank you for your order!");

    assertThat(header.logout().isLoginButtonVisible())
        .as("The journey should leave the application in a logged-out state")
        .isTrue();
  }
}
