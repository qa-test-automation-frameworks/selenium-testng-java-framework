package io.github.prayag.saucedemo.app.ui.page;

import io.github.prayag.saucedemo.framework.ui.BasePage;
import io.github.prayag.saucedemo.framework.ui.PageLoadable;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutCompletePage extends BasePage implements PageLoadable<CheckoutCompletePage> {

  private static final By CONFIRMATION_MESSAGE = By.cssSelector("[data-test='complete-header']");
  private static final By BACK_HOME_BUTTON = By.cssSelector("[data-test='back-to-products']");

  public CheckoutCompletePage(WebDriver driver) {
    super(driver);
  }

  @Override
  public CheckoutCompletePage waitUntilLoaded() {
    waitUntilUrlContains("checkout-complete");
    waitUtils.waitUntilVisible(CONFIRMATION_MESSAGE);
    waitUtils.waitUntilVisible(BACK_HOME_BUTTON);
    return this;
  }

  @Step("Get checkout confirmation message")
  public String getConfirmationMessage() {
    log.info("Retrieving checkout confirmation message");
    return waitUtils.waitUntilVisible(CONFIRMATION_MESSAGE).getText();
  }
}
