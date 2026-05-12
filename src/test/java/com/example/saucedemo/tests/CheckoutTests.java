package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.example.saucedemo.tests.data.TestTimeouts;
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
    log.info("Starting test: verifyCheckoutValidation expecting error: {}", expectedErrorMessage);

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
    log.info("Finished test successfully: verifyCheckoutValidation");
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
    log.info("Starting test: verifyUserCanCompleteCheckout");

    CheckoutPage checkoutPage = addBackpackToCartAndOpenCheckout();
    CheckoutInformation validInformation = CheckoutScenario.validInformation();
    CheckoutOverviewPage overviewPage =
        checkoutPage.submitValidCheckoutInformation(
            validInformation.firstName(),
            validInformation.lastName(),
            validInformation.postalCode());
    CheckoutCompletePage completePage = overviewPage.finishCheckout();

    assertThat(completePage.getConfirmationMessage())
        .as("Checkout success message should be displayed")
        .isEqualTo("Thank you for your order!");
    log.info("Finished test successfully: verifyUserCanCompleteCheckout");
  }

  private CheckoutPage addBackpackToCartAndOpenCheckout() {
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.navigateToCart();

    return new CartPage(getDriver()).continueToCheckout();
  }
}
