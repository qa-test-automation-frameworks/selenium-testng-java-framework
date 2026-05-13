package io.github.prayag.saucedemo.app.data;

import java.net.URI;

/** Application routes used by page readiness checks and navigation-focused tests. */
public enum AppRoute {
  LOGIN(""),
  INVENTORY("inventory.html"),
  CART("cart.html"),
  CHECKOUT_STEP_ONE("checkout-step-one.html"),
  CHECKOUT_STEP_TWO("checkout-step-two.html"),
  CHECKOUT_COMPLETE("checkout-complete.html"),
  PRODUCT_DETAIL("inventory-item.html");

  private final String path;

  AppRoute(String path) {
    this.path = path;
  }

  public String path() {
    return path;
  }

  public String absoluteUrl(String baseUrl) {
    return URI.create(baseUrl).resolve(path).toString();
  }
}
