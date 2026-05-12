package com.example.saucedemo.framework.pageobject;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class CheckoutOverviewPage extends BasePage implements PageLoadable<CheckoutOverviewPage> {

  private final By finishButton = By.cssSelector("[data-test='finish']");

  public CheckoutOverviewPage(WebDriver driver) {
    super(driver);
  }

  @Override
  public CheckoutOverviewPage waitUntilLoaded() {
    waitUntilUrlContains("checkout-step-two");
    waitUtils.waitUntilVisible(finishButton);
    return this;
  }

  @Step("Finish checkout")
  public CheckoutCompletePage finishCheckout() {
    log.info("Finishing checkout");
    waitUtils.click(finishButton);
    return new CheckoutCompletePage(driver).waitUntilLoaded();
  }
}
