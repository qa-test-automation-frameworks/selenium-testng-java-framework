package io.github.prayag.saucedemo.tests.data;

import lombok.Builder;

public final class CheckoutScenario {

  private CheckoutScenario() {}

  public static CheckoutInformation emptyInformation() {
    return CheckoutInformation.empty();
  }

  public static CheckoutInformation missingLastName() {
    return CheckoutInformation.builder().firstName("Pat").postalCode("411001").build();
  }

  public static CheckoutInformation missingPostalCode() {
    return CheckoutInformation.builder().firstName("Pat").lastName("Tester").build();
  }

  public static CheckoutInformation validInformation() {
    return CheckoutInformation.builder()
        .firstName("Pat")
        .lastName("Tester")
        .postalCode("411001")
        .build();
  }

  @Builder
  public record CheckoutInformation(String firstName, String lastName, String postalCode) {

    public static CheckoutInformation empty() {
      return builder().build();
    }
  }
}
