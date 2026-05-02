package common.pageobject;

import common.driver.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import util.WaitUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class BasePO {

    protected final By listItems = By.cssSelector("[data-test='inventory-item']");
    protected final By productNameElement = By.cssSelector("[data-test='inventory-item-name']");
    protected final By productDescriptionElement = By.cssSelector("[data-test='inventory-item-desc']");
    protected final By productPriceElement = By.cssSelector("[data-test='inventory-item-price']");
    private final By productCartButton = By.cssSelector("[data-test='inventory-item-price']+button");
    private final By cartButton = By.id("shopping_cart_container");
    private final By cartItemCount = By.cssSelector("[data-test='shopping-cart-badge']");
    private final By menuButton = By.cssSelector("#menu_button_container div div button");
    private final By logoutButton = By.cssSelector("[data-test='logout-sidebar-link']");
    protected final Map<String, String> productDetails = new HashMap<>();

    protected final WaitUtils waitUtils = new WaitUtils();

    protected WebDriver getDriver() {
        return WebDriverFactory.getThreadLocalWebDriver();
    }

    protected void navigateTo(String url) {
        log.info("Navigating to {}", url);
        getDriver().navigate().to(url);
        waitUtils.waitForPageLoad(500);
    }

    protected List<WebElement> getItemList() {
        return getDriver().findElements(listItems);
    }

    protected int getListItemsCount() {
        return getItemList().size();
    }

    protected Map<String, String> getProductDetailsByIndex(int index) {
        WebElement product = getItemList().get(index);
        String productName = product.findElement(productNameElement).getText();
        String productDescription = product.findElement(productDescriptionElement).getText();
        String productPrice = product.findElement(productPriceElement).getText();
        productDetails.put("productName", productName);
        productDetails.put("productDescription", productDescription);
        productDetails.put("productPrice", productPrice);
        return productDetails;
    }

    public void assertProductDetails(int index, Map<String, String> expectedProductDetails) {
        Map<String, String> actualProductDetails = getProductDetailsByIndex(index);
        Assertions.assertThat(actualProductDetails).isEqualTo(expectedProductDetails);
    }

    public void clickProductCartButtonByIndex(int index) {
        waitUtils.waitForPageLoad(500);
        WebElement product = getItemList().get(index);
        product.findElement(productCartButton).click();
    }

    public boolean isCartButtonVisible() {
        return getDriver().findElement(cartButton).isDisplayed();
    }

    public void navigateToCart() {
        getDriver().findElement(cartButton).click();
        waitUtils.waitForPageLoad(500);
    }

    private int getProductAddedToCartCount() {
        return Integer.parseInt(getDriver().findElement(cartButton).findElement(cartItemCount).getText());
    }

    public void assertProductAddedToCart(int expectedProductAddedToCartCount) {
        int addedToCartCount = getProductAddedToCartCount();
        Assertions.assertThat(addedToCartCount).isEqualTo(expectedProductAddedToCartCount);
    }

    private boolean isMenuButtonVisible() {
        return getDriver().findElement(menuButton).isDisplayed();
    }

    public void assertMenuButtonVisible() {
        Assertions.assertThat(isMenuButtonVisible()).isTrue();
        log.info("Asserted Menu button visible");
    }

    private WebElement getMenu() {
        return getDriver().findElement(menuButton);
    }

    public void openMenu() {
        getMenu().click();
        log.info("Opened menu button");
    }

    public void clickLogoutButton() {
        getDriver().findElement(logoutButton).click();
        log.info("Clicked on Log out button");
    }

    public void assertListItemEmpty() {
        Assertions.assertThat(getDriver().findElements(listItems).isEmpty()).isTrue();
        log.info("Asserted list item empty");
    }

}