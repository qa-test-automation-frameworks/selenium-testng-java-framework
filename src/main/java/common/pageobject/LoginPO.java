package common.pageobject;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import util.WaitUtils;

@Slf4j
public class LoginPO extends BasePO {

    private final WaitUtils waitUtils = new WaitUtils();
    private final By usernameField = By.id("user-name");
    private final By passwordField = By.id("password");
    private final By loginButton = By.id("login-button");

    public void enterUsername(String username) {
        getDriver().findElement(usernameField).sendKeys(username);
        waitUtils.waitForPageLoad(500);
    }

    public void enterPassword(String password) {
        getDriver().findElement(passwordField).sendKeys(password);
        waitUtils.waitForPageLoad(500);
    }

    public void clickLoginButton() {
        getDriver().findElement(loginButton).click();
    }

    public void login(String url, String username, String password) {
        navigateTo(url);
        log.info("Entering username and password");
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        waitUtils.waitForPageLoad(500);
        log.info("Login Successful");
    }

    public void logout() {
        waitUtils.waitForPageLoad(800);
        openMenu();
        waitUtils.waitForPageLoad(1000);
        clickLogoutButton();
        log.info("Logout Successful");
    }

}
