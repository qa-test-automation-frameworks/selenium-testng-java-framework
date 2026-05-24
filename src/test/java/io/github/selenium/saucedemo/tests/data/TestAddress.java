package io.github.selenium.saucedemo.tests.data;

/** Deterministic address data for checkout scenarios. */
public record TestAddress(String postalCode) {

  public static final TestAddress DEFAULT = new TestAddress("411001");
}
