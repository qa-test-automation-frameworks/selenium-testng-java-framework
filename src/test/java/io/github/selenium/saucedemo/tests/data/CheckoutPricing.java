package io.github.selenium.saucedemo.tests.data;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CheckoutPricing {

  private static final BigDecimal SAUCE_DEMO_TAX_RATE = new BigDecimal("0.08");

  private CheckoutPricing() {}

  public static BigDecimal itemTotal(String catalogPrice) {
    return new BigDecimal(catalogPrice.replace("$", ""));
  }

  public static BigDecimal taxFor(String catalogPrice) {
    return itemTotal(catalogPrice).multiply(SAUCE_DEMO_TAX_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal expectedTotal(String catalogPrice) {
    return itemTotal(catalogPrice).add(taxFor(catalogPrice));
  }
}
