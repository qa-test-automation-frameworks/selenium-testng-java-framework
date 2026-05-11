package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.data.AppConstants;
import common.data.ProductCatalog;
import common.data.TestGroups;
import common.pageobject.CartPage;
import common.pageobject.InventoryPage;
import common.pageobject.component.HeaderComponent;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import util.AuthService;

@Slf4j
public class CartTests extends BaseTestCase {

  private final ThreadLocal<InventoryPage> inventoryPage = new ThreadLocal<>();
  private final ThreadLocal<CartPage> cartPage = new ThreadLocal<>();
  private final ThreadLocal<HeaderComponent> header = new ThreadLocal<>();

  @BeforeMethod(alwaysRun = true)
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
    inventoryPage.set(new InventoryPage(getDriver()));
    cartPage.set(new CartPage(getDriver()));
    header.set(new HeaderComponent(getDriver()));
  }

  @Test(
      testName = "Verify adding multiple products to cart",
      groups = {TestGroups.SMOKE, TestGroups.CART})
  public void verifyUserCanAddProductsToCart() {
    log.info("Starting test: verifyUserCanAddProductsToCart");
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo(AppConstants.HEADER_TITLE);

    log.info("Adding two products to the cart");
    inventoryPage()
        .getInventoryList()
        .clickProductCartButtonByName(ProductCatalog.BACKPACK.name())
        .clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    assertThat(header().getProductAddedToCartCount())
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
    inventoryPage()
        .getInventoryList()
        .clickProductCartButtonByName(ProductCatalog.BACKPACK.name())
        .clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    header().navigateToCart();

    assertThat(
            cartPage().getInventoryList().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details in cart should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage().getProductQuantityByIndex(0))
        .as("Quantity for the first item in cart should be 1")
        .isEqualTo(1);

    assertThat(
            cartPage()
                .getInventoryList()
                .getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details in cart should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(cartPage().getProductQuantityByIndex(1))
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
    inventoryPage()
        .getInventoryList()
        .clickProductCartButtonByName(ProductCatalog.BACKPACK.name())
        .clickProductCartButtonByName(ProductCatalog.BIKE_LIGHT.name());

    header().navigateToCart();
    assertThat(cartPage().getInventoryList().getListItemsCount())
        .as("There should be 2 items in the cart initially")
        .isEqualTo(2);

    log.info("Removing first item from cart");
    cartPage().getInventoryList().clickProductCartButtonByIndex(0);
    assertThat(cartPage().getInventoryList().getListItemsCount())
        .as("After removing one item, there should be 1 item left in the cart")
        .isEqualTo(1);

    log.info("Removing second item from cart");
    cartPage().getInventoryList().clickProductCartButtonByIndex(0);
    assertThat(cartPage().getInventoryList().getListItemsCount())
        .as("After removing all items, the cart should be empty")
        .isEqualTo(0);
    log.info("Finished test successfully: verifyUserCanRemoveProductsFromCart");
  }

  private InventoryPage inventoryPage() {
    return inventoryPage.get();
  }

  private CartPage cartPage() {
    return cartPage.get();
  }

  private HeaderComponent header() {
    return header.get();
  }
}
