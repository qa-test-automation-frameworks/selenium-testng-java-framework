package io.github.selenium.saucedemo.framework;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Intentionally failing test, included only by testng-evidence-failure-control.xml. */
public class EvidenceFailureControlTest {

  @Test
  public void alwaysFailsForPortfolioEvidenceControl() {
    Assert.fail("EXPECTED_FAILURE_CONTROL: verify native failed-run evidence collection");
  }
}
