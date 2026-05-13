package io.github.prayag.saucedemo.app.ui.page;

/** Sauce Demo inventory sort options. */
public enum ProductSortOption {
  NAME_A_TO_Z("az"),
  NAME_Z_TO_A("za"),
  PRICE_LOW_TO_HIGH("lohi"),
  PRICE_HIGH_TO_LOW("hilo");

  private final String value;

  ProductSortOption(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
