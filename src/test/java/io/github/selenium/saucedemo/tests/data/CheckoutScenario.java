package io.github.selenium.saucedemo.tests.data;

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
    return validInformationBuilder().build();
  }

  public static CheckoutInformation variedInformation(String seed) {
    String normalizedSeed = normalizeSeed(seed);
    String suffix = normalizedSeed.length() > 12 ? normalizedSeed.substring(0, 12) : normalizedSeed;
    int postalCodeSeed = Math.floorMod(normalizedSeed.hashCode(), 100_000);

    return CheckoutInformation.builder()
        .firstName("Pat" + suffix)
        .lastName("Tester" + suffix)
        .postalCode(String.format("%05d", postalCodeSeed))
        .build();
  }

  public static CheckoutInformation.Builder validInformationBuilder() {
    return CheckoutInformation.builder()
        .firstName(TestPerson.DEFAULT.firstName())
        .lastName(TestPerson.DEFAULT.lastName())
        .postalCode(TestAddress.DEFAULT.postalCode());
  }

  private static String normalizeSeed(String seed) {
    String normalized = seed == null ? "seed" : seed.replaceAll("[^A-Za-z0-9]", "");
    return normalized.isBlank() ? "seed" : normalized;
  }

  public record CheckoutInformation(String firstName, String lastName, String postalCode) {

    public static Builder builder() {
      return new Builder();
    }

    public static CheckoutInformation empty() {
      return builder().build();
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
