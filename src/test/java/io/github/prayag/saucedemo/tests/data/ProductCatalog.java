package io.github.prayag.saucedemo.tests.data;

import io.github.prayag.saucedemo.app.data.ProductDetails;
import java.util.List;

public final class ProductCatalog {

  public static final int EXPECTED_PRODUCT_COUNT = 6;

  public static final ProductDetails BACKPACK =
      new ProductDetails(
          "Sauce Labs Backpack",
          "carry.allTheThings() with the sleek, streamlined Sly Pack that melds uncompromising style with unequaled laptop and tablet protection.",
          "$29.99");

  public static final ProductDetails BIKE_LIGHT =
      new ProductDetails(
          "Sauce Labs Bike Light",
          "A red light isn't the desired state in testing but it sure helps when riding your bike at night. Water-resistant with 3 lighting modes, 1 AAA battery included.",
          "$9.99");

  public static final ProductDetails BOLT_TSHIRT =
      new ProductDetails(
          "Sauce Labs Bolt T-Shirt",
          "Get your testing superhero on with the Sauce Labs bolt T-shirt. From American Apparel, 100% ringspun combed cotton, heather gray with red bolt.",
          "$15.99");

  public static final ProductDetails FLEECE_JACKET =
      new ProductDetails(
          "Sauce Labs Fleece Jacket",
          "It's not every day that you come across a midweight quarter-zip fleece jacket capable of handling everything from a relaxing day outdoors to a busy day at the office.",
          "$49.99");

  public static final ProductDetails ONESIE =
      new ProductDetails(
          "Sauce Labs Onesie",
          "Rib snap infant onesie for the junior automation engineer in development. Reinforced 3-snap bottom closure, two-needle hemmed sleeved and bottom won't unravel.",
          "$7.99");

  public static final ProductDetails RED_TSHIRT =
      new ProductDetails(
          "Test.allTheThings() T-Shirt (Red)",
          "This classic Sauce Labs t-shirt is perfect to wear when cozying up to your keyboard to automate a few tests. Super-soft and comfy ringspun combed cotton.",
          "$15.99");

  public static List<ProductDetails> allProducts() {
    return List.of(BACKPACK, BIKE_LIGHT, BOLT_TSHIRT, FLEECE_JACKET, ONESIE, RED_TSHIRT);
  }

  private ProductCatalog() {}
}
