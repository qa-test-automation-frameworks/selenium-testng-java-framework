package io.github.prayag.saucedemo.framework.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class DiagnosticRedactorTest {

  @Test(groups = "framework")
  public void redactsCommonSensitiveTokens() {
    String rawValue =
        "password=secret authorization:Bearer abc.123 customer=test@example.com phone=+1 555 123 4567";

    String redacted = DiagnosticRedactor.redact(rawValue);

    assertThat(redacted)
        .doesNotContain("secret")
        .doesNotContain("abc.123")
        .doesNotContain("test@example.com")
        .doesNotContain("+1 555 123 4567");
  }
}
