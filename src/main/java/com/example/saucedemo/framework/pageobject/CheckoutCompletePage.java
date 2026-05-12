package com.example.saucedemo.framework.pageobject;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutCompletePage extends BasePage implements PageLoadable<CheckoutCompletePage> {

  private static final By CONFIRMATION_MESSAGE = By.cssSelector("[data-test='complete-header']");

  public CheckoutCompletePage(WebDriver driver) {
    super(driver);
  }

  @Override
  public CheckoutCompletePage waitUntilLoaded() {
    waitUntilUrlContains("checkout-complete");
    waitUtils.waitUntilVisible(CONFIRMATION_MESSAGE);
    return this;
  }

  @Step("Get checkout confirmation message")
  public String getConfirmationMessage() {
    log.info("Retrieving checkout confirmation message");
    return waitUtils.waitUntilVisible(CONFIRMATION_MESSAGE).getText();
  }
}
