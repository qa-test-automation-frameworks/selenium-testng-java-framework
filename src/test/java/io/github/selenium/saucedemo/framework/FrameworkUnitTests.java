package io.github.selenium.saucedemo.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.selenium.saucedemo.framework.config.BrowserType;
import io.github.selenium.saucedemo.framework.config.ExecutionType;
import io.github.selenium.saucedemo.framework.config.FrameworkConfigurationException;
import io.github.selenium.saucedemo.framework.util.DiagnosticRedactor;
import org.testng.annotations.Test;

public class FrameworkUnitTests {

  @Test
  public void redactsCredentialsAndContactDetailsFromDiagnostics() {
    String raw = "Authorization: Bearer abc.def password=secret user@example.com +1 (555) 123-4567";

    String redacted = DiagnosticRedactor.redact(raw);

    assertThat(redacted)
        .doesNotContain("abc.def", "secret", "user@example.com", "555")
        .contains("<redacted>");
  }

  @Test
  public void resolvesSupportedBrowserAndExecutionValuesCaseInsensitively() {
    assertThat(BrowserType.from("firefox")).isEqualTo(BrowserType.FIREFOX);
    assertThat(ExecutionType.from("remote")).isEqualTo(ExecutionType.REMOTE);
  }

  @Test
  public void rejectsUnsupportedBrowserAndExecutionValues() {
    assertThatThrownBy(() -> BrowserType.from("opera"))
        .isInstanceOf(FrameworkConfigurationException.class)
        .hasMessageContaining("Unsupported browser");
    assertThatThrownBy(() -> ExecutionType.from("distributed"))
        .isInstanceOf(FrameworkConfigurationException.class)
        .hasMessageContaining("Unsupported execution.type");
  }
}
