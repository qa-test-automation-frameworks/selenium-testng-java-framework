package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.data.ProductCatalog;
import common.data.TestGroups;
import common.pageobject.CartPage;
import common.pageobject.InventoryPage;
import common.pageobject.LoginPage;
import common.pageobject.component.HeaderComponent;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
public class UITests extends BaseTestCase {

  private InventoryPage inventoryPage;
  private CartPage cartPage;
  private LoginPage loginPage;
  private HeaderComponent header;

  @BeforeMethod(alwaysRun = true)
  public void setupTest() {
    login();
    inventoryPage = new InventoryPage(getDriver());
    cartPage = new CartPage(getDriver());
    loginPage = new LoginPage(getDriver());
    header = new HeaderComponent(getDriver());
  }

  @Test(
      testName = "Verify navigation and header visibility",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY})
  public void verifyInventoryDisplaysHeaderAndMenu() {
    log.info("Starting test: verifyInventoryDisplaysHeaderAndMenu");
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(header.isMenuButtonVisible())
        .as("Side menu burger button should be visible")
        .isTrue();
    assertThat(header.isCartButtonVisible())
        .as("Shopping cart button should be visible")
        .isTrue();
    log.info("Finished test successfully: verifyInventoryDisplaysHeaderAndMenu");
  }

  @Test(
      testName = "Verify product list count",
      groups = {TestGroups.SMOKE, TestGroups.INVENTORY})
  public void verifyInventoryDisplaysAllProducts() {
    log.info("Starting test: verifyInventoryDisplaysAllProducts");
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(inventoryPage.getInventoryList().getListItemsCount())
        .as("The product inventory list should contain 6 items")
        .isEqualTo(6);
    log.info("Finished test successfully: verifyInventoryDisplaysAllProducts");
  }

  @Test(
      testName = "Verify all product details in catalog",
      groups = {TestGroups.INVENTORY, TestGroups.REGRESSION})
  public void verifyProductsMatchCatalogDetails() {
    log.info("Starting test: verifyProductsMatchCatalogDetails");
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(inventoryPage.getInventoryList().getListItemsCount())
        .as("Total product count should be 6")
        .isEqualTo(6);

    log.info("Verifying individual product details against the catalog");
    assertThat(
            inventoryPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(
            inventoryPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.BIKE_LIGHT.name()))
        .as("Bike light details should match catalog")
        .isEqualTo(ProductCatalog.BIKE_LIGHT);
    assertThat(
            inventoryPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(
            inventoryPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.FLEECE_JACKET.name()))
        .as("Fleece jacket details should match catalog")
        .isEqualTo(ProductCatalog.FLEECE_JACKET);
    assertThat(
            inventoryPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.ONESIE.name()))
        .as("Onesie details should match catalog")
        .isEqualTo(ProductCatalog.ONESIE);
    assertThat(
            inventoryPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.RED_TSHIRT.name()))
        .as("Red T-shirt details should match catalog")
        .isEqualTo(ProductCatalog.RED_TSHIRT);
    log.info("Finished test successfully: verifyProductsMatchCatalogDetails");
  }

  @Test(
      testName = "Verify adding multiple products to cart",
      groups = {TestGroups.SMOKE, TestGroups.CART})
  public void verifyUserCanAddProductsToCart() {
    log.info("Starting test: verifyUserCanAddProductsToCart");
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");

    log.info("Adding two products to the cart");
    inventoryPage.getInventoryList().clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    inventoryPage
        .getInventoryList()
        .clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    assertThat(header.getProductAddedToCartCount())
        .as("The cart badge should show 2 items added")
        .isEqualTo(2);
    log.info("Finished test successfully: verifyUserCanAddProductsToCart");
  }

  @Test(
      testName = "Verify added products are visible in cart",
      groups = {TestGroups.CART, TestGroups.REGRESSION})
  public void verifyCartDisplaysSelectedProducts() {
    log.info("Starting test: verifyCartDisplaysSelectedProducts");
    log.info("Adding products to cart and navigating to cart page");
    inventoryPage.getInventoryList().clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    inventoryPage
        .getInventoryList()
        .clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    header.navigateToCart();

    assertThat(
            cartPage.getInventoryList().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details in cart should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage.getProductQuantityByIndex(0))
        .as("Quantity for the first item in cart should be 1")
        .isEqualTo(1);

    assertThat(
            cartPage
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
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
  public void verifyUserCanRemoveProductsFromCart() {
    log.info("Starting test: verifyUserCanRemoveProductsFromCart");
    log.info("Adding products and navigating to cart for removal");
    inventoryPage.getInventoryList().clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    inventoryPage
        .getInventoryList()
        .clickProductCartButtonByName(ProductCatalog.BIKE_LIGHT.name());

    header.navigateToCart();
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("There should be 2 items in the cart initially")
        .isEqualTo(2);

    log.info("Removing first item from cart");
    cartPage.getInventoryList().clickProductCartButtonByIndex(0);
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("After removing one item, there should be 1 item left in the cart")
        .isEqualTo(1);

    log.info("Removing second item from cart");
    cartPage.getInventoryList().clickProductCartButtonByIndex(0);
    assertThat(cartPage.getInventoryList().getListItemsCount())
        .as("After removing all items, the cart should be empty")
        .isEqualTo(0);
    log.info("Finished test successfully: verifyUserCanRemoveProductsFromCart");
  }

  @Test(
      testName = "Verify user can logout",
      groups = {TestGroups.SMOKE, TestGroups.LOGIN})
  public void verifyUserCanLogout() {
    log.info("Starting test: verifyUserCanLogout");
    assertThat(inventoryPage.getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    log.info("Performing logout operation");
    loginPage.logout();
    log.info("Finished test successfully: verifyUserCanLogout");
  }
}
