package io.github.prayag.saucedemo.tests.data;

import lombok.Builder;

public final class CheckoutScenario {

  private CheckoutScenario() {}

  public static CheckoutInformation emptyInformation() {
    return CheckoutInformation.empty();
  }

  public static CheckoutInformation missingLastName() {
    return CheckoutInformation.builder()
        .firstName(TestPerson.DEFAULT.firstName())
        .postalCode(TestAddress.DEFAULT.postalCode())
        .build();
  }

  public static CheckoutInformation missingPostalCode() {
    return CheckoutInformation.builder()
        .firstName(TestPerson.DEFAULT.firstName())
        .lastName(TestPerson.DEFAULT.lastName())
        .build();
  }

  public static CheckoutInformation validInformation() {
    return CheckoutInformation.builder()
        .firstName(TestPerson.DEFAULT.firstName())
        .lastName(TestPerson.DEFAULT.lastName())
        .postalCode(TestAddress.DEFAULT.postalCode())
        .build();
  }

  @Builder
  public record CheckoutInformation(String firstName, String lastName, String postalCode) {

    public static CheckoutInformation empty() {
      return builder().build();
    }
  }
}
