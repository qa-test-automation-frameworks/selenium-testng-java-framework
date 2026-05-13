package io.github.prayag.saucedemo.framework.listener;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class RetryRegistryTest {

  @AfterMethod(alwaysRun = true)
  public void clearRegistry() {
    RetryRegistry.clear();
  }

  @Test(groups = "framework")
  public void retryRegistryKeepsSuiteWideEntriesUntilExplicitlyCleared() {
    RetryRegistry.clear();

    RetryRegistry.record("first test retried 1/2");
    RetryRegistry.record("second test retried 1/2");

    assertThat(RetryRegistry.snapshot())
        .containsExactly("first test retried 1/2", "second test retried 1/2");
  }
}
