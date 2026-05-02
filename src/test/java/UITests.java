import common.pageobject.CartPO;
import common.pageobject.LandingPO;
import common.pageobject.LoginPO;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;

import java.util.Map;

@Slf4j
public class UITests extends BaseTestCase {

    private final CartPO cartPO = new CartPO();
    private final LandingPO landingPO = new LandingPO();
    private final LoginPO loginPO = new LoginPO();

    @Test
    public void testNavigation() {
        log.info("Navigation test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.assertMenuButtonVisible();
        landingPO.assertCartButtonVisible();
    }

    @Test
    public void testListProducts() {
        log.info("List test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.assertListItemsSize(6);
    }

    @Test
    public void testProductDetails() {
        log.info("Product test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.assertListItemsSize(6);
        landingPO.assertProductDetails(0, Map.of("productName", "Sauce Labs Backpack",
                "productDescription", "carry.allTheThings() with the sleek, streamlined Sly Pack that melds uncompromising style with unequaled laptop and tablet protection.",
                "productPrice", "$29.99"));
        landingPO.assertProductDetails(1, Map.of("productName", "Sauce Labs Bike Light",
                "productDescription", "A red light isn't the desired state in testing but it sure helps when riding your bike at night. Water-resistant with 3 lighting modes, 1 AAA battery included.",
                "productPrice", "$9.99"));
        landingPO.assertProductDetails(2, Map.of("productName", "Sauce Labs Bolt T-Shirt",
                "productDescription", "Get your testing superhero on with the Sauce Labs bolt T-shirt. From American Apparel, 100% ringspun combed cotton, heather gray with red bolt.",
                "productPrice", "$15.99"));
        landingPO.assertProductDetails(3, Map.of("productName", "Sauce Labs Fleece Jacket",
                "productDescription", "It's not every day that you come across a midweight quarter-zip fleece jacket capable of handling everything from a relaxing day outdoors to a busy day at the office.",
                "productPrice", "$49.99"));
        landingPO.assertProductDetails(4, Map.of("productName", "Sauce Labs Onesie",
                "productDescription", "Rib snap infant onesie for the junior automation engineer in development. Reinforced 3-snap bottom closure, two-needle hemmed sleeved and bottom won't unravel.",
                "productPrice", "$7.99"));
        landingPO.assertProductDetails(5, Map.of("productName", "Test.allTheThings() T-Shirt (Red)",
                "productDescription", "This classic Sauce Labs t-shirt is perfect to wear when cozying up to your keyboard to automate a few tests. Super-soft and comfy ringspun combed cotton.",
                "productPrice", "$15.99"));
    }

    @Test
    public void testAddingProductToCart() {
        log.info("Adding product to cart test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.assertListItemsSize(6);
        landingPO.assertProductDetails(0, Map.of("productName", "Sauce Labs Backpack",
                "productDescription", "carry.allTheThings() with the sleek, streamlined Sly Pack that melds uncompromising style with unequaled laptop and tablet protection.",
                "productPrice", "$29.99"));
        landingPO.clickProductCartButtonByIndex(0);
        landingPO.assertProductDetails(2, Map.of("productName", "Sauce Labs Bolt T-Shirt",
                "productDescription", "Get your testing superhero on with the Sauce Labs bolt T-shirt. From American Apparel, 100% ringspun combed cotton, heather gray with red bolt.",
                "productPrice", "$15.99"));
        landingPO.clickProductCartButtonByIndex(2);
        landingPO.assertProductAddedToCart(2);
    }

    @Test
    public void testAddedProductInCart() {
        log.info("Added product to cart test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.clickProductCartButtonByIndex(0);
        landingPO.clickProductCartButtonByIndex(2);
        landingPO.assertCartButtonVisible();
        landingPO.navigateToCart();
        cartPO.assertProductDetails(0, Map.of("productName", "Sauce Labs Backpack",
                "productDescription", "carry.allTheThings() with the sleek, streamlined Sly Pack that melds uncompromising style with unequaled laptop and tablet protection.",
                "productPrice", "$29.99"));
        cartPO.assertProductQuantityByIndex(0, 1);
        cartPO.assertProductDetails(1, Map.of("productName", "Sauce Labs Bolt T-Shirt",
                "productDescription", "Get your testing superhero on with the Sauce Labs bolt T-shirt. From American Apparel, 100% ringspun combed cotton, heather gray with red bolt.",
                "productPrice", "$15.99"));
        cartPO.assertProductQuantityByIndex(1, 1);
    }

    @Test
    public void testRemoveProductFromCart() {
        log.info("Removing product from cart test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.clickProductCartButtonByIndex(0);
        landingPO.clickProductCartButtonByIndex(1);
        landingPO.assertCartButtonVisible();
        landingPO.navigateToCart();
        landingPO.assertListItemsSize(2);
        cartPO.clickProductCartButtonByIndex(0);
        landingPO.assertListItemsSize(1);
        cartPO.clickProductCartButtonByIndex(0);
        landingPO.assertListItemEmpty();
    }

    @Test
    public void testLogout() {
        log.info("Logout test");
        landingPO.assertHeaderLabel("Swag Labs");
        landingPO.assertListItemsSize(6);
        loginPO.logout();
    }
}
