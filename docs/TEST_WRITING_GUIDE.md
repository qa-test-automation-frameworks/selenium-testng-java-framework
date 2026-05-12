# Test Writing Guide

Follow these guidelines when adding new tests or modifying existing ones.

This repository is intentionally UI-automation-only. Keep `src/test/java` focused on TestNG scenarios, data providers, and test data rather than adding a separate framework unit-test layer.

## Adding a New Page Object
1. Create a new class in `src/main/java/com/example/saucedemo/framework/pageobject`.
2. Extend `BasePage`.
3. Add a constructor that calls `super(driver)`.
4. Define locators using private `By` variables.
5. Implement action methods and use `@Step` for reporting.
6. Keep construction lightweight; callers should invoke `waitUntilLoaded()` explicitly when page readiness must be asserted.
7. Keep business assertions in test classes, not page objects.

## Adding a New Component
If a UI element is shared across multiple pages (e.g., a Footer):
1. Create a class in `com.example.saucedemo.framework.pageobject.component`.
2. Extend `BaseComponent`.
3. Instantiate it within the Page Objects that use it.

## Writing a New Test
1. Create a test class in `src/test/java/com/example/saucedemo/tests`.
2. Extend `BaseTestCase`.
3. Annotate test methods with `@Test`.
4. Use the `testName` and `description` attributes in `@Test`, and add Allure `@Description` when you want the description rendered in the dedicated Allure panel.
5. Instantiate Page Objects close to where they are used and call `waitUntilLoaded()` explicitly when the scenario depends on page readiness.
6. Use **AssertJ** for assertions. Always include a descriptive message using `.as()`.
7. Add meaningful TestNG groups such as `smoke`, `login`, `inventory`, `cart`, or `regression`.
8. Keep shared navigation or header assertions in dedicated coverage tests so unrelated scenarios fail for one clear reason.

### Example Test Annotation
```java
@Description("Adds two products and verifies the cart badge count updates to match.")
@Test(
    testName = "Verify adding multiple products to cart",
    description = "Adds two products and verifies the cart badge count updates to match.",
    groups = {TestGroups.SMOKE, TestGroups.CART},
    timeOut = TestTimeouts.UI_TEST_TIMEOUT_MS)
public void verifyUserCanAddProductsToCart() {
  InventoryPage inventoryPage = new InventoryPage(getDriver()).waitUntilLoaded();
  // Arrange, act, assert
}
```

### Example Assertion
```java
assertThat(page.getSomeValue())
    .as("The balance should be positive after the transaction")
    .isGreaterThan(0);
```

## Naming Conventions
- **Pages**: `SomethingPage`
- **Components**: `SomethingComponent`
- **Tests**: use descriptive camelCase names, for example `verifyUserCanAddProductsToCart`.



