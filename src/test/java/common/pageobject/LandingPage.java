package common.pageobject;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@Slf4j
public class LandingPage extends BasePage {

  public LandingPage(WebDriver driver) {
    super(driver);
  }

  private final By primaryHeader = By.className("app_logo");

  @Step("Get header text from landing page")
  public String getHeaderText() {
    log.info("Retrieving the main header text from the landing page");
    assertThat(driver.findElement(primaryHeader).isDisplayed())
        .as("Primary header (logo) should be displayed on the landing page")
        .isTrue();
    String text = driver.findElement(primaryHeader).getText();
    log.debug("Landing page header text retrieved: {}", text);
    return text;
  }
}
