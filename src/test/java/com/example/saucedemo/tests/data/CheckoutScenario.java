package com.example.saucedemo.tests.data;

public final class CheckoutScenario {

  private CheckoutScenario() {}

  public static CheckoutInformation emptyInformation() {
    return new CheckoutInformation(null, null, null);
  }

  public record CheckoutInformation(String firstName, String lastName, String postalCode) {}
}
