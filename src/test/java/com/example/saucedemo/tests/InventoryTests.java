package com.example.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.example.saucedemo.framework.data.AppConstants;
import com.example.saucedemo.framework.data.ProductDetails;
import com.example.saucedemo.framework.pageobject.InventoryPage;
import com.example.saucedemo.framework.pageobject.component.HeaderComponent;
import com.example.saucedemo.framework.pageobject.component.InventoryListComponent;
import com.example.saucedemo.framework.util.AuthService;
import com.example.saucedemo.tests.data.ProductCatalog;
import com.example.saucedemo.tests.data.TestGroups;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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

  @BeforeMethod(alwaysRun = true)
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @Test(
      testName = "Verify navigation and header visibility",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY})
  @Story("Inventory navigation")
  @Severity(SeverityLevel.CRITICAL)
  public void verifyInventoryDisplaysHeaderAndMenu() {
    log.info("Starting test: verifyInventoryDisplaysHeaderAndMenu");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(header.isMenuButtonVisible())
        .as("Side menu burger button should be visible")
        .isTrue();
    assertThat(header.isCartButtonVisible()).as("Shopping cart button should be visible").isTrue();
    log.info("Finished test successfully: verifyInventoryDisplaysHeaderAndMenu");
  }

  @Test(
      testName = "Verify product list count",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY})
  @Story("Product catalog")
  @Severity(SeverityLevel.NORMAL)
  public void verifyInventoryDisplaysAllProducts() {
    log.info("Starting test: verifyInventoryDisplaysAllProducts");
    InventoryPage inventoryPage = new InventoryPage(getDriver());

    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(inventoryPage.getInventoryList().getListItemsCount())
        .as(
            "The product inventory list should contain %s items",
            ProductCatalog.EXPECTED_PRODUCT_COUNT)
        .isEqualTo(ProductCatalog.EXPECTED_PRODUCT_COUNT);
    log.info("Finished test successfully: verifyInventoryDisplaysAllProducts");
  }

  @Test(
      testName = "Verify all product details in catalog",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION})
  @Story("Product catalog")
  @Severity(SeverityLevel.NORMAL)
  public void verifyProductsMatchCatalogDetails() {
    log.info("Starting test: verifyProductsMatchCatalogDetails");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    InventoryListComponent inventoryList = inventoryPage.getInventoryList();

    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(inventoryList.getListItemsCount())
        .as("Total product count should be %s", ProductCatalog.EXPECTED_PRODUCT_COUNT)
        .isEqualTo(ProductCatalog.EXPECTED_PRODUCT_COUNT);

    log.info("Verifying individual product details against the catalog");
    List<ProductDetails> expectedProducts =
        List.of(
            ProductCatalog.BACKPACK,
            ProductCatalog.BIKE_LIGHT,
            ProductCatalog.BOLT_TSHIRT,
            ProductCatalog.FLEECE_JACKET,
            ProductCatalog.ONESIE,
            ProductCatalog.RED_TSHIRT);

    assertSoftly(
        softly ->
            expectedProducts.forEach(
                expectedProduct ->
                    softly
                        .assertThat(inventoryList.getProductDetailsByName(expectedProduct.name()))
                        .as("%s details should match catalog", expectedProduct.name())
                        .isEqualTo(expectedProduct)));
    log.info("Finished test successfully: verifyProductsMatchCatalogDetails");
  }

  @Test(
      testName = "Verify products can be sorted by price low to high",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION})
  @Story("Product sorting")
  @Severity(SeverityLevel.NORMAL)
  public void verifyProductsCanBeSortedByPriceLowToHigh() {
    log.info("Starting test: verifyProductsCanBeSortedByPriceLowToHigh");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    List<Double> actualPrices =
        inventoryPage.sortProductsByPriceLowToHigh().getDisplayedProductPrices();
    List<Double> sortedPrices = new ArrayList<>(actualPrices);
    sortedPrices.sort(Double::compareTo);

    assertThat(actualPrices)
        .as("Product prices should be displayed from lowest to highest after sorting")
        .isEqualTo(sortedPrices);
    log.info("Finished test successfully: verifyProductsCanBeSortedByPriceLowToHigh");
  }

  @Test(
      testName = "Verify removing a product from inventory clears cart badge",
      groups = {TestGroups.INVENTORY, TestGroups.CART, TestGroups.REGRESSION})
  @Story("Inventory cart controls")
  @Severity(SeverityLevel.NORMAL)
  public void verifyRemovingProductFromInventoryClearsCartBadge() {
    log.info("Starting test: verifyRemovingProductFromInventoryClearsCartBadge");
    InventoryPage inventoryPage = new InventoryPage(getDriver());
    HeaderComponent header = new HeaderComponent(getDriver());

    inventoryPage.addProductToCart(ProductCatalog.BACKPACK.name());
    header.waitForProductAddedToCartCount(1);
    inventoryPage.removeProductFromCart(ProductCatalog.BACKPACK.name());
    header.waitForProductAddedToCartCount(0);

    assertThat(header.getProductAddedToCartCount())
        .as("Cart badge should disappear after removing the only product")
        .isZero();
    log.info("Finished test successfully: verifyRemovingProductFromInventoryClearsCartBadge");
  }
}
