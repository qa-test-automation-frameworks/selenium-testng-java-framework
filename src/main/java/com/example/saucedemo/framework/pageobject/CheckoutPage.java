package com.example.saucedemo.framework.pageobject;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutPage extends BasePage implements PageLoadable<CheckoutPage> {

  private final By firstName = By.cssSelector("[data-test='firstName']");
  private final By lastName = By.cssSelector("[data-test='lastName']");
  private final By postalCode = By.cssSelector("[data-test='postalCode']");
  private final By continueButton = By.cssSelector("[data-test='continue']");
  private final By errorMessage = By.cssSelector("[data-test='error']");

  public CheckoutPage(WebDriver driver) {
    super(driver);
    waitUntilLoaded();
  }

  @Override
  public CheckoutPage waitUntilLoaded() {
    waitUntilUrlContains("checkout-step-one");
    waitUtils.waitUntilVisible(continueButton);
    return this;
  }

  @Step("Submit valid checkout information")
  public CheckoutOverviewPage submitValidCheckoutInformation(
      String firstName, String lastName, String zipCode) {
    submitCheckoutInformation(firstName, lastName, zipCode);
    return new CheckoutOverviewPage(driver);
  }

  @Step("Submit invalid checkout information")
  public CheckoutPage submitInvalidCheckoutInformation(
      String firstName, String lastName, String zipCode) {
    submitCheckoutInformation(firstName, lastName, zipCode);
    waitUtils.waitUntilVisible(errorMessage);
    return this;
  }

  private void submitCheckoutInformation(String firstName, String lastName, String zipCode) {
    log.info("Submitting checkout information");
    if (firstName != null) {
      waitUtils.type(this.firstName, firstName);
    }
    if (lastName != null) {
      waitUtils.type(this.lastName, lastName);
    }
    if (zipCode != null) {
      waitUtils.type(this.postalCode, zipCode);
    }
    waitUtils.click(continueButton);
  }

  @Step("Get checkout error message")
  public String getErrorMessage() {
    log.info("Retrieving checkout error message");
    return waitUtils.waitUntilVisible(errorMessage).getText();
  }
}
