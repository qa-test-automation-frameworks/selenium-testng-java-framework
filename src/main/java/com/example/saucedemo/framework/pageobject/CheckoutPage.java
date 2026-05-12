package com.example.saucedemo.framework.pageobject;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutPage extends BasePage {

  private final By firstName = By.cssSelector("[data-test='firstName']");
  private final By lastName = By.cssSelector("[data-test='lastName']");
  private final By postalCode = By.cssSelector("[data-test='postalCode']");
  private final By continueButton = By.cssSelector("[data-test='continue']");
  private final By errorMessage = By.cssSelector("[data-test='error']");

  public CheckoutPage(WebDriver driver) {
    super(driver);
  }

  @Step("Submit checkout information")
  public CheckoutPage submitCheckoutInformation(String firstName, String lastName, String zipCode) {
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
    waitUtils.waitForPageLoad();
    return this;
  }

  @Step("Get checkout error message")
  public String getErrorMessage() {
    log.info("Retrieving checkout error message");
    return waitUtils.waitUntilVisible(errorMessage).getText();
  }
}
