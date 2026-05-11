package common.data;

/** Immutable product details displayed in inventory and cart views. */
public record ProductDetails(String name, String description, String price) {}
