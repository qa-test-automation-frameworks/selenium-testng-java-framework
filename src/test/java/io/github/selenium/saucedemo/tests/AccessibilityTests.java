package io.github.selenium.saucedemo.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.deque.html.axecore.results.Rule;
import io.github.selenium.saucedemo.app.auth.AuthService;
import io.github.selenium.saucedemo.app.ui.page.InventoryPage;
import io.github.selenium.saucedemo.framework.util.AcceptedAccessibilityViolations;
import io.github.selenium.saucedemo.framework.util.AccessibilityProbe;
import io.github.selenium.saucedemo.tests.data.TestGroups;
import io.github.selenium.saucedemo.tests.data.TestTimeouts;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
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

  private static String joinOrNone(List<String> lines, String none) {
    return lines.isEmpty() ? none : String.join(System.lineSeparator(), lines);
  }

  @Test(
      testName = "Verify inventory page passes baseline accessibility checks",
      description =
          "Fails on WCAG 2.0/2.1 A and AA axe-core violations, except dated, reasoned acceptances; records best-practice and other axe results as advisories, and records structural advisories separately.",
      groups = {TestGroups.ACCESSIBILITY},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Accessibility")
  @Severity(SeverityLevel.NORMAL)
  public void verifyInventoryPagePassesBaselineAccessibilityChecks() {
    InventoryPage inventoryPage = pages().inventory().waitUntilLoaded();
    AccessibilityProbe accessibilityProbe = new AccessibilityProbe(getDriver());
    List<String> findings = accessibilityProbe.findBaselineViolations();
    List<String> advisories = accessibilityProbe.findStructuralAdvisories();
    AccessibilityProbe.AxeScan scan = accessibilityProbe.scanAxe();
    List<String> blockingRuleIds = scan.blocking().stream().map(Rule::getId).toList();
    AcceptedAccessibilityViolations.Evaluation evaluation =
        AcceptedAccessibilityViolations.evaluate(
            AcceptedAccessibilityViolations.loadFromClasspath(
                "/accessibility/accepted-violations.json"),
            blockingRuleIds,
            "inventory",
            LocalDate.now());
    List<String> blockingSummaries =
        scan.blocking().stream()
            .filter(rule -> evaluation.unaccepted().contains(rule.getId()))
            .map(AccessibilityProbe::formatAxeViolation)
            .toList();

    Allure.addAttachment(
        "Accessibility baseline findings",
        joinOrNone(findings, "No accessibility baseline findings detected."));
    Allure.addAttachment(
        "Accessibility structural advisories",
        joinOrNone(advisories, "No structural accessibility advisories detected."));
    Allure.addAttachment(
        "axe-core blocking violations (WCAG A/AA)",
        joinOrNone(blockingSummaries, "No blocking axe-core violations detected."));
    Allure.addAttachment(
        "axe-core accepted violations",
        joinOrNone(List.copyOf(evaluation.accepted()), "No accepted axe-core violations."));
    Allure.addAttachment(
        "axe-core advisories (best practice and other)",
        joinOrNone(
            scan.advisory().stream().map(AccessibilityProbe::formatAxeViolation).toList(),
            "No axe-core advisories detected."));

    assertThat(inventoryPage.getDisplayedProductImageSources())
        .as("Inventory page should expose visible product images before accessibility checks run")
        .isNotEmpty();
    assertThat(findings)
        .as("Inventory page should not contain baseline accessibility violations")
        .isEmpty();
    assertThat(evaluation.problems())
        .as("Accepted accessibility violations should be current")
        .isEmpty();
    assertThat(blockingSummaries)
        .as("Inventory page should have no unaccepted WCAG 2.0/2.1 A/AA axe-core violations")
        .isEmpty();
  }

  @Test(
      testName = "Verify the axe oracle blocks a known WCAG violation",
      description =
          "Planted-defect control: an image without alt text must produce a blocking image-alt violation, not an advisory.",
      groups = {TestGroups.ACCESSIBILITY},
      timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
  @Story("Accessibility oracle control")
  @Severity(SeverityLevel.NORMAL)
  public void verifyAxeOracleBlocksKnownWcagViolation() {
    String html =
        "<!DOCTYPE html><html lang=\"en\"><head><title>Control</title></head><body><main>"
            + "<h1>Control</h1>"
            + "<img src=\"data:image/gif;base64,R0lGODlhAQABAAAAACw=\""
            + " width=\"10\" height=\"10\">"
            + "</main></body></html>";
    AccessibilityProbe probe = new AccessibilityProbe(getDriver());
    AccessibilityProbe.AxeScan scan;
    getDriver()
        .get(
            "data:text/html;charset=utf-8,"
                + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20"));
    try {
      scan = probe.scanAxe();
    } catch (RuntimeException dataUrlInjectionRefused) {
      // Some Chrome/Grid combinations refuse script injection on data: URLs; write the same
      // document into about:blank instead.
      getDriver().get("about:blank");
      ((JavascriptExecutor) getDriver())
          .executeScript("document.open(); document.write(arguments[0]); document.close();", html);
      scan = probe.scanAxe();
    }

    assertThat(scan.blocking().stream().map(Rule::getId))
        .as("image-alt is a WCAG A rule and must block")
        .contains("image-alt");
    assertThat(scan.advisory().stream().map(Rule::getId))
        .as("image-alt must not be classified as an advisory")
        .doesNotContain("image-alt");
  }
}
