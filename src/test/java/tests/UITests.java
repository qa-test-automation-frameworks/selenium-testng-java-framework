package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.data.ProductCatalog;
import common.pageobject.CartPage;
import common.pageobject.InventoryPage;
import common.pageobject.LoginPage;
import common.pageobject.component.HeaderComponent;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
public class UITests extends BaseTestCase {

  private final ThreadLocal<CartPage> cartPage = new ThreadLocal<>();
  private final ThreadLocal<InventoryPage> inventoryPage = new ThreadLocal<>();
  private final ThreadLocal<LoginPage> loginPage = new ThreadLocal<>();
  private final ThreadLocal<HeaderComponent> header = new ThreadLocal<>();

  @BeforeMethod(alwaysRun = true)
  public void setupPages() {
    log.info("Initializing page objects for test: {}", getClass().getSimpleName());
    cartPage.set(new CartPage(getDriver()));
    inventoryPage.set(new InventoryPage(getDriver()));
    loginPage.set(new LoginPage(getDriver()));
    header.set(new HeaderComponent(getDriver()));
  }

  @Test(
      testName = "Verify navigation and header visibility",
      groups = {"smoke", "inventory"})
  public void inventory_should_display_header_and_menu() {
    log.info("Starting test: inventory_should_display_header_and_menu");
    login();
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(header().isMenuButtonVisible())
        .as("Side menu burger button should be visible")
        .isTrue();
    assertThat(header().isCartButtonVisible())
        .as("Shopping cart button should be visible")
        .isTrue();
    log.info("Finished test successfully: inventory_should_display_header_and_menu");
  }

  @Test(
      testName = "Verify product list count",
      groups = {"smoke", "inventory"})
  public void inventory_should_display_all_products() {
    log.info("Starting test: inventory_should_display_all_products");
    login();
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(inventoryPage().getListItemsCount())
        .as("The product inventory list should contain 6 items")
        .isEqualTo(6);
    log.info("Finished test successfully: inventory_should_display_all_products");
  }

  @Test(
      testName = "Verify all product details in catalog",
      groups = {"inventory", "regression"})
  public void products_should_match_catalog_details() {
    log.info("Starting test: products_should_match_catalog_details");
    login();
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(inventoryPage().getListItemsCount())
        .as("Total product count should be 6")
        .isEqualTo(6);

    log.info("Verifying individual product details against the catalog");
    assertThat(inventoryPage().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(inventoryPage().getProductDetailsByName(ProductCatalog.BIKE_LIGHT.name()))
        .as("Bike light details should match catalog")
        .isEqualTo(ProductCatalog.BIKE_LIGHT);
    assertThat(inventoryPage().getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(inventoryPage().getProductDetailsByName(ProductCatalog.FLEECE_JACKET.name()))
        .as("Fleece jacket details should match catalog")
        .isEqualTo(ProductCatalog.FLEECE_JACKET);
    assertThat(inventoryPage().getProductDetailsByName(ProductCatalog.ONESIE.name()))
        .as("Onesie details should match catalog")
        .isEqualTo(ProductCatalog.ONESIE);
    assertThat(inventoryPage().getProductDetailsByName(ProductCatalog.RED_TSHIRT.name()))
        .as("Red T-shirt details should match catalog")
        .isEqualTo(ProductCatalog.RED_TSHIRT);
    log.info("Finished test successfully: products_should_match_catalog_details");
  }

  @Test(
      testName = "Verify adding multiple products to cart",
      groups = {"smoke", "cart"})
  public void user_should_be_able_to_add_products_to_cart() {
    log.info("Starting test: user_should_be_able_to_add_products_to_cart");
    login();
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");

    log.info("Adding two products to the cart");
    inventoryPage().clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    inventoryPage().clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    assertThat(header().getProductAddedToCartCount())
        .as("The cart badge should show 2 items added")
        .isEqualTo(2);
    log.info("Finished test successfully: user_should_be_able_to_add_products_to_cart");
  }

  @Test(
      testName = "Verify added products are visible in cart",
      groups = {"cart", "regression"})
  public void cart_should_display_selected_products() {
    log.info("Starting test: cart_should_display_selected_products");
    login();
    log.info("Adding products to cart and navigating to cart page");
    inventoryPage().clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    inventoryPage().clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    header().navigateToCart();

    assertThat(cartPage().getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details in cart should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage().getProductQuantityByIndex(0))
        .as("Quantity for the first item in cart should be 1")
        .isEqualTo(1);

    assertThat(cartPage().getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details in cart should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(cartPage().getProductQuantityByIndex(1))
        .as("Quantity for the second item in cart should be 1")
        .isEqualTo(1);
    log.info("Finished test successfully: cart_should_display_selected_products");
  }

  @Test(
      testName = "Verify removing products from cart",
      groups = {"cart", "regression"})
  public void user_should_be_able_to_remove_products_from_cart() {
    log.info("Starting test: user_should_be_able_to_remove_products_from_cart");
    login();
    log.info("Adding products and navigating to cart for removal");
    inventoryPage().clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    inventoryPage().clickProductCartButtonByName(ProductCatalog.BIKE_LIGHT.name());

    header().navigateToCart();
    assertThat(cartPage().getListItemsCount())
        .as("There should be 2 items in the cart initially")
        .isEqualTo(2);

    log.info("Removing first item from cart");
    cartPage().clickProductCartButtonByIndex(0);
    assertThat(cartPage().getListItemsCount())
        .as("After removing one item, there should be 1 item left in the cart")
        .isEqualTo(1);

    log.info("Removing second item from cart");
    cartPage().clickProductCartButtonByIndex(0);
    assertThat(cartPage().getListItemsCount())
        .as("After removing all items, the cart should be empty")
        .isEqualTo(0);
    log.info("Finished test successfully: user_should_be_able_to_remove_products_from_cart");
  }

  @Test(
      testName = "Verify user can logout",
      groups = {"smoke", "login"})
  public void user_should_be_able_to_logout() {
    log.info("Starting test: user_should_be_able_to_logout");
    login();
    assertThat(inventoryPage().getHeaderText())
        .as("Inventory page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    log.info("Performing logout operation");
    loginPage().logout();
    log.info("Finished test successfully: user_should_be_able_to_logout");
  }

  private CartPage cartPage() {
    return cartPage.get();
  }

  private InventoryPage inventoryPage() {
    return inventoryPage.get();
  }

  private LoginPage loginPage() {
    return loginPage.get();
  }

  private HeaderComponent header() {
    return header.get();
  }
}
