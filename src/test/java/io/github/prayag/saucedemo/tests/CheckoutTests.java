package io.github.prayag.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.prayag.saucedemo.app.auth.AuthService;
import io.github.prayag.saucedemo.app.ui.component.HeaderComponent;
import io.github.prayag.saucedemo.app.ui.page.CheckoutCompletePage;
import io.github.prayag.saucedemo.app.ui.page.CheckoutOverviewPage;
import io.github.prayag.saucedemo.app.ui.page.CheckoutPage;
import io.github.prayag.saucedemo.app.ui.page.InventoryPage;
import io.github.prayag.saucedemo.tests.data.CheckoutPricing;
import io.github.prayag.saucedemo.tests.data.CheckoutScenario;
import io.github.prayag.saucedemo.tests.data.CheckoutScenario.CheckoutInformation;
import io.github.prayag.saucedemo.tests.data.ProductCatalog;
import io.github.prayag.saucedemo.tests.data.TestGroups;
import io.github.prayag.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Slf4j
@Epic("Sauce Demo")
@Feature("Checkout")
@Owner("QA Automation")
public class CheckoutTests extends BaseTestCase {

  @BeforeMethod(alwaysRun = true, description = "Authenticate via cookie and open inventory page")
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @DataProvider(name = "invalidCheckoutScenarios")
  public Object[][] invalidCheckoutScenarios() {
    return new Object[][] {
      {CheckoutScenario.emptyInformation(), "Error: First Name is required"},
      {CheckoutScenario.missingLastName(), "Error: Last Name is required"},
      {CheckoutScenario.missingPostalCode(), "Error: Postal Code is required"}
    };
  }

  @Test(
      testName = "Verify checkout validates required information",
      description =
          "Attempts checkout with invalid required-field combinations and verifies the matching validation message for each scenario.",
      groups = {TestGroups.CHECKOUT, TestGroups.REGRESSION},
      dataProvider = "invalidCheckoutScenarios",
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Checkout validation")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyCheckoutValidation(
      CheckoutInformation invalidInformation, String expectedErrorMessage) {
    CheckoutPage checkoutPage = addBackpackToCartAndOpenCheckout();

    assertThat(
            checkoutPage
                .submitInvalidCheckoutInformation(
                    invalidInformation.firstName(),
                    invalidInformation.lastName(),
                    invalidInformation.postalCode())
                .getErrorMessage())
        .as("Checkout should show the expected required-field validation message")
        .isEqualTo(expectedErrorMessage);
  }

  @Test(
      testName = "Verify user can complete checkout",
      description =
          "Completes a happy-path checkout flow and verifies the order confirmation message is displayed.",
      groups = {TestGroups.SMOKE, TestGroups.CHECKOUT},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Checkout completion")
  @Severity(SeverityLevel.BLOCKER)
  public void verifyUserCanCompleteCheckout() {
    CheckoutPage checkoutPage = addBackpackToCartAndOpenCheckout();
    CheckoutInformation validInformation = CheckoutScenario.validInformation();
    CheckoutOverviewPage overviewPage =
        checkoutPage.submitValidCheckoutInformation(
            validInformation.firstName(),
            validInformation.lastName(),
            validInformation.postalCode());

    assertThat(overviewPage.getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Checkout overview should show the selected backpack details")
        .isEqualTo(ProductCatalog.BACKPACK);
    var expectedItemTotal = CheckoutPricing.itemTotal(ProductCatalog.BACKPACK.price());
    var expectedTax = CheckoutPricing.taxFor(ProductCatalog.BACKPACK.price());
    var expectedTotal = CheckoutPricing.expectedTotal(ProductCatalog.BACKPACK.price());

    assertThat(overviewPage.getItemTotal())
        .as("Checkout item total should equal the selected backpack catalog price")
        .isEqualByComparingTo(expectedItemTotal);
    assertThat(overviewPage.getTax())
        .as("Checkout tax should be Sauce Demo's 8%% tax for the backpack")
        .isEqualByComparingTo(expectedTax);
    assertThat(overviewPage.getTotal())
        .as("Checkout total should include item total and tax")
        .isEqualByComparingTo(expectedTotal);

    CheckoutCompletePage completePage = overviewPage.finishCheckout();

    assertThat(completePage.getConfirmationMessage())
        .as("Checkout success message should be displayed")
        .isEqualTo("Thank you for your order!");
  }

  private CheckoutPage addBackpackToCartAndOpenCheckout() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    return header.navigateToCart().continueToCheckout();
  }
}
