package io.github.prayag.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.github.prayag.saucedemo.app.auth.AuthService;
import io.github.prayag.saucedemo.app.ui.component.HeaderComponent;
import io.github.prayag.saucedemo.app.ui.component.InventoryListComponent;
import io.github.prayag.saucedemo.app.ui.page.InventoryPage;
import io.github.prayag.saucedemo.app.ui.page.ProductDetailPage;
import io.github.prayag.saucedemo.framework.data.AppConstants;
import io.github.prayag.saucedemo.framework.listener.Retryable;
import io.github.prayag.saucedemo.tests.data.ProductCatalog;
import io.github.prayag.saucedemo.tests.data.TestGroups;
import io.github.prayag.saucedemo.tests.data.TestTimeouts;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
@Epic("Sauce Demo")
@Feature("Inventory")
@Owner("QA Automation")
public class InventoryTests extends BaseTestCase {

  @BeforeMethod(alwaysRun = true, description = "Authenticate via cookie and open inventory page")
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @Test(
      testName = "Verify navigation and header visibility",
      description =
          "Opens the inventory page through cookie authentication and verifies the main header and menu controls are visible.",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Inventory navigation")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyInventoryDisplaysHeaderAndMenu() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    assertThat(inventoryPage.getAppLogoText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(header.isMenuButtonVisible())
        .as("Side menu burger button should be visible")
        .isTrue();
    assertThat(header.isCartButtonVisible()).as("Shopping cart button should be visible").isTrue();
  }

  @Test(
      testName = "Verify product list count",
      description =
          "Validates that the inventory page renders the complete fixed Sauce Demo catalog.",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Product catalog")
  @Severity(SeverityLevel.NORMAL)
  public void verifyInventoryDisplaysAllProducts() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();

    assertThat(inventoryPage.getInventoryList().getListItemsCount())
        .as(
            "The product inventory list should contain %s items",
            ProductCatalog.EXPECTED_PRODUCT_COUNT)
        .isEqualTo(ProductCatalog.EXPECTED_PRODUCT_COUNT);
  }

  @Test(
      testName = "Verify all product details in catalog",
      description =
          "Checks that each displayed inventory item matches the centralized demo catalog data.",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Product catalog")
  @Severity(SeverityLevel.NORMAL)
  public void verifyProductsMatchCatalogDetails() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    InventoryListComponent inventoryList = inventoryPage.getInventoryList();

    assertThat(inventoryList.getListItemsCount())
        .as("Total product count should be %s", ProductCatalog.EXPECTED_PRODUCT_COUNT)
        .isEqualTo(ProductCatalog.EXPECTED_PRODUCT_COUNT);

    assertSoftly(
        softly ->
            ProductCatalog.allProducts()
                .forEach(
                    expectedProduct -> {
                      softly
                          .assertThat(inventoryList.getProductDetailsByName(expectedProduct.name()))
                          .as(
                              "%s details should match the expected catalog",
                              expectedProduct.name())
                          .isEqualTo(expectedProduct);
                    }));
  }

  @Test(
      testName = "Verify products can be sorted by price low to high",
      description =
          "Sorts the inventory by ascending price and verifies the rendered product prices follow that order.",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Product sorting")
  @Severity(SeverityLevel.NORMAL)
  public void verifyProductsCanBeSortedByPriceLowToHigh() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    List<BigDecimal> actualPrices =
        inventoryPage.sortProductsByPriceLowToHigh().getDisplayedProductPrices();
    List<BigDecimal> sortedPrices = new ArrayList<>(actualPrices);
    sortedPrices.sort(BigDecimal::compareTo);

    assertThat(actualPrices)
        .as("Product prices should be displayed from lowest to highest after sorting")
        .isEqualTo(sortedPrices);
  }

  @Test(
      testName = "Verify removing a product from inventory clears cart badge",
      description =
          "Adds a single product, removes it from the inventory page, and confirms the cart badge disappears.",
      groups = {TestGroups.INVENTORY, TestGroups.CART, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Retryable
  @Story("Inventory cart controls")
  @Severity(SeverityLevel.NORMAL)
  public void verifyRemovingProductFromInventoryClearsCartBadge() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.waitForProductAddedToCartCount(1);
    inventoryPage.removeProductFromCart(ProductCatalog.BACKPACK.name());
    header.waitForProductAddedToCartCount(0);

    assertThat(header.getProductAddedToCartCount())
        .as("Cart badge should disappear after removing the only product")
        .isZero();
  }

  @Test(
      testName = "Verify product detail page displays catalog details",
      description =
          "Opens a product from inventory, verifies its detail page content, adds it to cart, and returns to inventory.",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Product detail")
  @Severity(SeverityLevel.NORMAL)
  public void verifyProductDetailPageMatchesCatalog() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    ProductDetailPage detailPage = inventoryPage.openProductDetail(ProductCatalog.BACKPACK.name());

    assertThat(detailPage.getProductDetails())
        .as("Product detail page should match the selected catalog item")
        .isEqualTo(ProductCatalog.BACKPACK);

    detailPage.addToCart();
    header.waitForProductAddedToCartCount(1);

    assertThat(detailPage.getActionButtonText())
        .as("Detail page action should switch to Remove after adding the product")
        .isEqualTo("Remove");
    assertThat(detailPage.goBack().getAppLogoText())
        .as("Back to products should return to the inventory page")
        .isEqualTo(AppConstants.HEADER_TITLE);
  }

  @Test(
      testName = "Verify add-to-cart button changes to remove after adding product",
      description = "Adds a product and verifies the inventory button switches to Remove state.",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION},
      timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
  @Story("Inventory cart controls")
  @Severity(SeverityLevel.NORMAL)
  public void verifyAddToCartButtonTogglesAfterAddingProduct() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());

    assertThat(inventoryPage.getInventoryList().getActionButtonText(ProductCatalog.BACKPACK.name()))
        .as("Added product action button should switch to Remove")
        .isEqualTo("Remove");
  }
}
