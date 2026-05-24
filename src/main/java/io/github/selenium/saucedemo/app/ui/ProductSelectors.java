package io.github.selenium.saucedemo.app.ui;

import org.openqa.selenium.By;

/** Shared selectors for Sauce Demo product row/detail contracts. */
public final class ProductSelectors {

  public static final By DETAIL_ROOT = By.cssSelector("[data-test='inventory-item']");
  public static final By LIST_ITEM =
      By.cssSelector("[data-test='inventory-item'], [data-test='cart-item']");
  public static final By NAME = By.cssSelector("[data-test='inventory-item-name']");
  public static final By DESCRIPTION = By.cssSelector("[data-test='inventory-item-desc']");
  public static final By PRICE = By.cssSelector("[data-test='inventory-item-price']");
  public static final By QUANTITY = By.cssSelector("[data-test='item-quantity']");
  public static final By ADD_TO_CART_BUTTON = By.cssSelector("button[data-test^='add-to-cart']");
  public static final By REMOVE_BUTTON = By.cssSelector("button[data-test^='remove']");

  private ProductSelectors() {}
}
