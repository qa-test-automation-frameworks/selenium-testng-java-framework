package io.github.selenium.saucedemo.tests.data;

/** Deterministic person data for checkout scenarios. */
public record TestPerson(String firstName, String lastName) {

  public static final TestPerson DEFAULT = new TestPerson("Pat", "Tester");
}
