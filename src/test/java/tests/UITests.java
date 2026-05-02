package tests;

import static org.assertj.core.api.Assertions.assertThat;

import common.data.ProductCatalog;
import common.pageobject.CartPage;
import common.pageobject.LandingPage;
import common.pageobject.LoginPage;
import common.pageobject.component.HeaderComponent;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Slf4j
public class UITests extends BaseTestCase {

  private CartPage cartPage;
  private LandingPage landingPage;
  private LoginPage loginPage;
  private HeaderComponent header;

  @BeforeMethod(alwaysRun = true)
  public void setupPages() {
    log.info("Initializing page objects for test: {}", getClass().getSimpleName());
    cartPage = new CartPage(getDriver());
    landingPage = new LandingPage(getDriver());
    loginPage = new LoginPage(getDriver());
    header = new HeaderComponent(getDriver());
  }

  @Test(testName = "Verify navigation and header visibility")
  public void inventory_should_display_header_and_menu() {
    log.info("Starting test: inventory_should_display_header_and_menu");
    login();
    assertThat(landingPage.getHeaderText())
        .as("Landing page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(header.isMenuButtonVisible())
        .as("Side menu burger button should be visible")
        .isTrue();
    assertThat(header.isCartButtonVisible())
        .as("Shopping cart button should be visible")
        .isTrue();
    log.info("Finished test successfully: inventory_should_display_header_and_menu");
  }

  @Test(testName = "Verify product list count")
  public void inventory_should_display_all_products() {
    log.info("Starting test: inventory_should_display_all_products");
    login();
    assertThat(landingPage.getHeaderText())
        .as("Landing page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(landingPage.getListItemsCount())
        .as("The product inventory list should contain 6 items")
        .isEqualTo(6);
    log.info("Finished test successfully: inventory_should_display_all_products");
  }

  @Test(testName = "Verify all product details in catalog")
  public void products_should_match_catalog_details() {
    log.info("Starting test: products_should_match_catalog_details");
    login();
    assertThat(landingPage.getHeaderText())
        .as("Landing page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    assertThat(landingPage.getListItemsCount())
        .as("Total product count should be 6")
        .isEqualTo(6);

    log.info("Verifying individual product details against the catalog");
    assertThat(landingPage.getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(landingPage.getProductDetailsByName(ProductCatalog.BIKE_LIGHT.name()))
        .as("Bike light details should match catalog")
        .isEqualTo(ProductCatalog.BIKE_LIGHT);
    assertThat(landingPage.getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(landingPage.getProductDetailsByName(ProductCatalog.FLEECE_JACKET.name()))
        .as("Fleece jacket details should match catalog")
        .isEqualTo(ProductCatalog.FLEECE_JACKET);
    assertThat(landingPage.getProductDetailsByName(ProductCatalog.ONESIE.name()))
        .as("Onesie details should match catalog")
        .isEqualTo(ProductCatalog.ONESIE);
    assertThat(landingPage.getProductDetailsByName(ProductCatalog.RED_TSHIRT.name()))
        .as("Red T-shirt details should match catalog")
        .isEqualTo(ProductCatalog.RED_TSHIRT);
    log.info("Finished test successfully: products_should_match_catalog_details");
  }

  @Test(testName = "Verify adding multiple products to cart")
  public void user_should_be_able_to_add_products_to_cart() {
    log.info("Starting test: user_should_be_able_to_add_products_to_cart");
    login();
    assertThat(landingPage.getHeaderText())
        .as("Landing page header title should be Swag Labs")
        .isEqualTo("Swag Labs");

    log.info("Adding two products to the cart");
    landingPage.clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    landingPage.clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    assertThat(header.getProductAddedToCartCount())
        .as("The cart badge should show 2 items added")
        .isEqualTo(2);
    log.info("Finished test successfully: user_should_be_able_to_add_products_to_cart");
  }

  @Test(testName = "Verify added products are visible in cart")
  public void cart_should_display_selected_products() {
    log.info("Starting test: cart_should_display_selected_products");
    login();
    log.info("Adding products to cart and navigating to cart page");
    landingPage.clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    landingPage.clickProductCartButtonByName(ProductCatalog.BOLT_TSHIRT.name());

    header.navigateToCart();

    assertThat(cartPage.getProductDetailsByName(ProductCatalog.BACKPACK.name()))
        .as("Backpack details in cart should match catalog")
        .isEqualTo(ProductCatalog.BACKPACK);
    assertThat(cartPage.getProductQuantityByIndex(0))
        .as("Quantity for the first item in cart should be 1")
        .isEqualTo(1);

    assertThat(cartPage.getProductDetailsByName(ProductCatalog.BOLT_TSHIRT.name()))
        .as("Bolt T-shirt details in cart should match catalog")
        .isEqualTo(ProductCatalog.BOLT_TSHIRT);
    assertThat(cartPage.getProductQuantityByIndex(1))
        .as("Quantity for the second item in cart should be 1")
        .isEqualTo(1);
    log.info("Finished test successfully: cart_should_display_selected_products");
  }

  @Test(testName = "Verify removing products from cart")
  public void user_should_be_able_to_remove_products_from_cart() {
    log.info("Starting test: user_should_be_able_to_remove_products_from_cart");
    login();
    log.info("Adding products and navigating to cart for removal");
    landingPage.clickProductCartButtonByName(ProductCatalog.BACKPACK.name());
    landingPage.clickProductCartButtonByName(ProductCatalog.BIKE_LIGHT.name());

    header.navigateToCart();
    assertThat(landingPage.getListItemsCount())
        .as("There should be 2 items in the cart initially")
        .isEqualTo(2);

    log.info("Removing first item from cart");
    cartPage.clickProductCartButtonByIndex(0);
    assertThat(landingPage.getListItemsCount())
        .as("After removing one item, there should be 1 item left in the cart")
        .isEqualTo(1);

    log.info("Removing second item from cart");
    cartPage.clickProductCartButtonByIndex(0);
    assertThat(landingPage.getListItemsCount())
        .as("After removing all items, the cart should be empty")
        .isEqualTo(0);
    log.info("Finished test successfully: user_should_be_able_to_remove_products_from_cart");
  }

  @Test(testName = "Verify user can logout")
  public void user_should_be_able_to_logout() {
    log.info("Starting test: user_should_be_able_to_logout");
    login();
    assertThat(landingPage.getHeaderText())
        .as("Landing page header title should be Swag Labs")
        .isEqualTo("Swag Labs");
    log.info("Performing logout operation");
    loginPage.logout();
    log.info("Finished test successfully: user_should_be_able_to_logout");
  }
}
