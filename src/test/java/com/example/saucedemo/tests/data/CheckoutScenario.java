package com.example.saucedemo.tests.data;

import java.util.concurrent.ThreadLocalRandom;

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
        .firstName(randomName("Pat"))
        .lastName(randomName("Tester"))
        .postalCode(randomPostalCode())
        .build();
  }

  private static String randomName(String prefix) {
    return prefix + ThreadLocalRandom.current().nextInt(100, 1_000);
  }

  private static String randomPostalCode() {
    return String.format("%05d", ThreadLocalRandom.current().nextInt(100_000));
  }

  public record CheckoutInformation(String firstName, String lastName, String postalCode) {

    public static CheckoutInformation empty() {
      return builder().build();
    }

    public static Builder builder() {
      return new Builder();
    }

    public static final class Builder {
      private String firstName;
      private String lastName;
      private String postalCode;

      private Builder() {}

      public Builder firstName(String firstName) {
        this.firstName = firstName;
        return this;
      }

      public Builder lastName(String lastName) {
        this.lastName = lastName;
        return this;
      }

      public Builder postalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
      }

      public CheckoutInformation build() {
        return new CheckoutInformation(firstName, lastName, postalCode);
      }
    }
  }
}
