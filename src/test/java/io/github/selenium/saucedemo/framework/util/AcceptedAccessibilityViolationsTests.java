package io.github.selenium.saucedemo.framework.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.selenium.saucedemo.framework.util.AcceptedAccessibilityViolations.Entry;
import java.time.LocalDate;
import java.util.List;
import org.testng.annotations.Test;

public class AcceptedAccessibilityViolationsTests {

  private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);

  private static String entry(String ruleId, String acceptedOn, String expiresOn) {
    return """
        [{"ruleId":"%s","page":"inventory","reason":"vendor markup","acceptedOn":"%s",\
        "expiresOn":"%s","issueUrl":"https://github.com/example/repo/issues/1"}]"""
        .formatted(ruleId, acceptedOn, expiresOn);
  }

  @Test
  public void committedRegisterParses() {
    assertThat(
            AcceptedAccessibilityViolations.loadFromClasspath(
                "/accessibility/accepted-violations.json"))
        .isNotNull();
  }

  @Test
  public void acceptedRuleMovesOutOfTheBlockingSet() {
    List<Entry> entries =
        AcceptedAccessibilityViolations.parse(entry("color-contrast", "2026-10-01", "2026-12-01"));

    var result =
        AcceptedAccessibilityViolations.evaluate(
            entries, List.of("color-contrast", "image-alt"), "inventory", TODAY);

    assertThat(result.accepted()).containsExactly("color-contrast");
    assertThat(result.unaccepted()).containsExactly("image-alt");
    assertThat(result.problems()).isEmpty();
  }

  @Test
  public void expiredAcceptanceIsAProblem() {
    List<Entry> entries =
        AcceptedAccessibilityViolations.parse(entry("color-contrast", "2026-07-01", "2026-09-01"));

    var result =
        AcceptedAccessibilityViolations.evaluate(
            entries, List.of("color-contrast"), "inventory", TODAY);

    assertThat(result.problems())
        .containsExactly("Accepted accessibility violation color-contrast expired on 2026-09-01");
    assertThat(result.unaccepted()).containsExactly("color-contrast");
  }

  @Test
  public void staleAcceptanceIsAProblem() {
    List<Entry> entries =
        AcceptedAccessibilityViolations.parse(entry("color-contrast", "2026-10-01", "2026-12-01"));

    var result = AcceptedAccessibilityViolations.evaluate(entries, List.of(), "inventory", TODAY);

    assertThat(result.problems()).containsExactly("Stale acceptance for color-contrast; remove it");
  }

  @Test
  public void acceptancesLongerThanNinetyDaysAreRejected() {
    assertThatThrownBy(
            () ->
                AcceptedAccessibilityViolations.parse(
                    entry("color-contrast", "2026-01-01", "2026-12-01")))
        .hasMessageContaining("within 90 days");
  }

  @Test
  public void entriesMissingAFieldAreRejected() {
    assertThatThrownBy(() -> AcceptedAccessibilityViolations.parse("[{\"ruleId\":\"x\"}]"))
        .hasMessageContaining("is required");
  }
}
