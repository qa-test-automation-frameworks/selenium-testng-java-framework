package com.example.saucedemo.tests.data;

public final class CheckoutScenario {

  private CheckoutScenario() {}

  public static CheckoutInformation emptyInformation() {
    return new CheckoutInformation(null, null, null);
  }

  public static CheckoutInformation missingLastName() {
    return new CheckoutInformation("Pat", null, "411001");
  }

  public static CheckoutInformation validInformation() {
    return new CheckoutInformation("Pat", "Tester", "411001");
  }

  public record CheckoutInformation(String firstName, String lastName, String postalCode) {}
}
