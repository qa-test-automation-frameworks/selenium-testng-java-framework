package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.example.saucedemo.app.auth.AuthService;
import com.example.saucedemo.framework.data.AppConstants;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.framework.pageobject.component.InventoryListComponent;
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
  @Description(
      "Opens the inventory page through cookie authentication and verifies the main header and menu controls are visible.")
  @Story("Inventory navigation")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyInventoryDisplaysHeaderAndMenu() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    HeaderComponent header = pages().header();

    assertThat(inventoryPage.getHeaderText())
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
  @Description("Validates that the inventory page renders the complete fixed Sauce Demo catalog.")
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
  @Description(
      "Checks that each displayed inventory item matches the centralized demo catalog data.")
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
  @Description(
      "Sorts the inventory by ascending price and verifies the rendered product prices follow that order.")
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
  @Description(
      "Adds a single product, removes it from the inventory page, and confirms the cart badge disappears.")
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
}
