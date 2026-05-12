package com.example.saucedemo.framework.pageobject;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutPage extends BasePage implements PageLoadable<CheckoutPage> {

  private static final By FIRST_NAME = By.cssSelector("[data-test='firstName']");
  private static final By LAST_NAME = By.cssSelector("[data-test='lastName']");
  private static final By POSTAL_CODE = By.cssSelector("[data-test='postalCode']");
  private static final By CONTINUE_BUTTON = By.cssSelector("[data-test='continue']");
  private static final By ERROR_MESSAGE = By.cssSelector("[data-test='error']");

  public CheckoutPage(WebDriver driver) {
    super(driver);
  }

  @Override
  public CheckoutPage waitUntilLoaded() {
    waitUntilUrlContains("checkout-step-one");
    waitUtils.waitUntilVisible(CONTINUE_BUTTON);
    return this;
  }

  @Step("Submit valid checkout information")
  public CheckoutOverviewPage submitValidCheckoutInformation(
      String firstName, String lastName, String zipCode) {
    submitCheckoutInformation(firstName, lastName, zipCode);
    return new CheckoutOverviewPage(driver).waitUntilLoaded();
  }

  @Step("Submit invalid checkout information")
  public CheckoutPage submitInvalidCheckoutInformation(
      String firstName, String lastName, String zipCode) {
    submitCheckoutInformation(firstName, lastName, zipCode);
    waitUtils.waitUntilVisible(ERROR_MESSAGE);
    return this;
  }

  private void submitCheckoutInformation(String firstName, String lastName, String zipCode) {
    log.info("Submitting checkout information");
    if (firstName != null) {
      waitUtils.type(FIRST_NAME, firstName);
    }
    if (lastName != null) {
      waitUtils.type(LAST_NAME, lastName);
    }
    if (zipCode != null) {
      waitUtils.type(POSTAL_CODE, zipCode);
    }
    waitUtils.click(CONTINUE_BUTTON);
  }

  @Step("Get checkout error message")
  public String getErrorMessage() {
    log.info("Retrieving checkout error message");
    return waitUtils.waitUntilVisible(ERROR_MESSAGE).getText();
  }
}
