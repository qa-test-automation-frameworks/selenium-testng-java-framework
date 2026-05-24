package io.github.selenium.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.selenium.saucedemo.app.auth.AuthService;
import io.github.selenium.saucedemo.app.ui.page.InventoryPage;
import io.github.selenium.saucedemo.framework.config.ConfigFactory;
import io.github.selenium.saucedemo.framework.util.VisualBaselineManager;
import io.github.selenium.saucedemo.tests.data.TestGroups;
import io.github.selenium.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.Locale;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Sauce Demo")
@Feature("Visual Regression")
@Owner("QA Automation")
public class VisualRegressionTests extends BaseTestCase {

  @BeforeMethod(alwaysRun = true, description = "Authenticate via cookie and open inventory page")
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @Test(
      testName = "Verify inventory page matches approved visual baseline",
      description =
          "Runs an opt-in screenshot-hash comparison against an approved stable inventory-page region and supports deliberate baseline approval when requested.",
      groups = {TestGroups.VISUAL},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Opt-in visual baseline")
  @Severity(SeverityLevel.NORMAL)
  public void verifyInventoryPageMatchesApprovedVisualBaseline() {
    By appLogo = By.className("app_logo");
    var config = ConfigFactory.getConfig();
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    byte[] screenshot = getDriver().findElement(appLogo).getScreenshotAs(OutputType.BYTES);
    String baselineName =
        String.format(
            "inventory-app-logo-%s-%s",
            config.browser().toLowerCase(Locale.ROOT), config.headless() ? "headless" : "headed");

    var result = new VisualBaselineManager(config).compare(baselineName, screenshot);

    Allure.addAttachment(
        "Visual baseline comparison",
        String.join(
            System.lineSeparator(),
            "Baseline path: " + result.baselinePath(),
            "Expected hash: " + result.expectedHash(),
            "Actual hash: " + result.actualHash(),
            "Approved this run: " + result.approved(),
            "Message: " + result.message()));

    assertThat(inventoryPage.getDisplayedProductImageSources())
        .as("Inventory page should render product images before the visual baseline is evaluated")
        .isNotEmpty();
    assertThat(result.passed()).as(result.message()).isTrue();
  }
}
