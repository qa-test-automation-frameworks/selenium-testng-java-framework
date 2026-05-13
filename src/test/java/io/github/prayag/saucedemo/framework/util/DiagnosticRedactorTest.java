package io.github.prayag.saucedemo.framework.util;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.prayag.saucedemo.framework.config.ConfigLoader;
import io.github.prayag.saucedemo.framework.config.ConfigSources;
import java.util.Map;
import java.util.Properties;
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

  @Test(groups = "framework")
  public void redactsHtmlCookiesAndConfiguredCredentials() {
    Properties systemProperties = new Properties();
    systemProperties.setProperty("APP_USERNAME", "standard_user");
    systemProperties.setProperty("APP_PASSWORD", "top-secret-value");
    var config =
        new ConfigLoader()
            .load(new ConfigSources(Map.of(), systemProperties, getClass().getClassLoader()));
    String rawValue =
        "<input type='password' value='top-secret-value'> "
            + "Set-Cookie: session-username=standard_user; "
            + "token=abc.123&email=person@example.test";

    String redacted = DiagnosticRedactor.redact(rawValue, config);

    assertThat(redacted)
        .doesNotContain("top-secret-value")
        .doesNotContain("standard_user")
        .doesNotContain("abc.123")
        .doesNotContain("person@example.test");
  }
}
