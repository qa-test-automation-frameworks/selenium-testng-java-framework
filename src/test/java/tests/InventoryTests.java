package tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import common.data.AppConstants;
import common.data.ProductCatalog;
import common.data.ProductDetails;
import common.data.TestGroups;
import common.pageobject.InventoryPage;
import common.pageobject.component.HeaderComponent;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import util.AuthService;

@Slf4j
public class InventoryTests extends BaseTestCase {

  private final ThreadLocal<InventoryPage> inventoryPage = new ThreadLocal<>();
  private final ThreadLocal<HeaderComponent> header = new ThreadLocal<>();

  @BeforeMethod(alwaysRun = true)
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
    inventoryPage.set(new InventoryPage(getDriver()));
    header.set(new HeaderComponent(getDriver()));
  }

  @Test(
      testName = "Verify navigation and header visibility",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY})
  public void verifyInventoryDisplaysHeaderAndMenu() {
    log.info("Starting test: verifyInventoryDisplaysHeaderAndMenu");
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(header().isMenuButtonVisible())
        .as("Side menu burger button should be visible")
        .isTrue();
    assertThat(header().isCartButtonVisible())
        .as("Shopping cart button should be visible")
        .isTrue();
    log.info("Finished test successfully: verifyInventoryDisplaysHeaderAndMenu");
  }

  @Test(
      testName = "Verify product list count",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY})
  public void verifyInventoryDisplaysAllProducts() {
    log.info("Starting test: verifyInventoryDisplaysAllProducts");
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(inventoryPage().getInventoryList().getListItemsCount())
        .as(
            "The product inventory list should contain %s items",
            ProductCatalog.EXPECTED_PRODUCT_COUNT)
        .isEqualTo(ProductCatalog.EXPECTED_PRODUCT_COUNT);
    log.info("Finished test successfully: verifyInventoryDisplaysAllProducts");
  }

  @Test(
      testName = "Verify all product details in catalog",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION})
  public void verifyProductsMatchCatalogDetails() {
    log.info("Starting test: verifyProductsMatchCatalogDetails");
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);
    assertThat(inventoryPage().getInventoryList().getListItemsCount())
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
                        .assertThat(
                            inventoryPage()
                                .getInventoryList()
                                .getProductDetailsByName(expectedProduct.name()))
                        .as("%s details should match catalog", expectedProduct.name())
                        .isEqualTo(expectedProduct)));
    log.info("Finished test successfully: verifyProductsMatchCatalogDetails");
  }

  private InventoryPage inventoryPage() {
    return inventoryPage.get();
  }

  private HeaderComponent header() {
    return header.get();
  }
}
