package io.github.prayag.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.prayag.saucedemo.app.auth.AuthService;
import io.github.prayag.saucedemo.app.ui.page.InventoryPage;
import io.github.prayag.saucedemo.framework.util.AccessibilityProbe;
import io.github.prayag.saucedemo.tests.data.TestGroups;
import io.github.prayag.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Epic("Sauce Demo")
@Feature("Accessibility")
@Owner("QA Automation")
public class AccessibilityTests extends BaseTestCase {
  @BeforeMethod(alwaysRun = true, description = "Authenticate via cookie and open inventory page")
  public void setupTest() {
    AuthService.injectLoginCookieAndNavigate(getDriver());
  }

  @Test(
      testName = "Verify inventory page passes baseline accessibility checks",
      description =
          "Runs a deterministic accessibility smoke probe against the inventory page, fails on blocking baseline issues, and records structural advisories separately.",
      groups = {TestGroups.ACCESSIBILITY},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Opt-in accessibility smoke")
  @Severity(SeverityLevel.NORMAL)
  public void verifyInventoryPagePassesBaselineAccessibilityChecks() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    AccessibilityProbe accessibilityProbe = new AccessibilityProbe(getDriver());
    List<String> findings = accessibilityProbe.findBaselineViolations();
    List<String> advisories = accessibilityProbe.findStructuralAdvisories();

    Allure.addAttachment(
        "Accessibility baseline findings",
        findings.isEmpty()
            ? "No accessibility baseline findings detected."
            : String.join(System.lineSeparator(), findings));
    Allure.addAttachment(
        "Accessibility structural advisories",
        advisories.isEmpty()
            ? "No structural accessibility advisories detected."
            : String.join(System.lineSeparator(), advisories));

    assertThat(inventoryPage.getDisplayedProductImageSources())
        .as("Inventory page should expose visible product images before accessibility checks run")
        .isNotEmpty();
    assertThat(findings)
        .as("Inventory page should not contain baseline accessibility violations")
        .isEmpty();
  }
}
