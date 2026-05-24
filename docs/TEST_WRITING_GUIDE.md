# Test Writing Guide

Follow these guidelines when adding new tests or modifying existing ones.

This repository is intentionally UI-automation-first. Keep `src/test/java` focused on TestNG UI scenarios, data providers, and test data. ADR 005 explicitly avoids framework-only tests so the repository stays centered on browser-observable behavior.

## Adding a New Page Object
1. Create a new class in `src/main/java/io/github/selenium/saucedemo/app/ui/page`.
2. Extend `BasePage`.
3. Add a constructor that calls `super(driver)`.
4. Define static locators using `private static final By` when the selector does not depend on runtime data.
5. Implement action methods and use `@Step` for reporting.
6. Keep construction lightweight; callers should invoke `waitUntilLoaded()` explicitly when page readiness must be asserted.
7. Keep business assertions in test classes, not page objects.

## Adding a New Component
If a UI element is shared across multiple pages (e.g., a Footer):
1. Create a class in `io.github.selenium.saucedemo.app.ui.component`.
2. Extend `BaseComponent`.
3. Instantiate it within the Page Objects that use it.

## Writing a New Test
1. Create a test class in `src/test/java/io/github/selenium/saucedemo/tests`.
2. Extend `BaseTestCase`.
3. Annotate test methods with `@Test`.
4. Use the `testName` and `description` attributes in `@Test`; Allure reads the TestNG description automatically.
5. Instantiate Page Objects close to where they are used and call `waitUntilLoaded()` explicitly when the scenario depends on page readiness.
6. Use **AssertJ** for assertions. Always include a descriptive message using `.as()`.
7. Add meaningful TestNG groups such as `smoke`, `login`, `inventory`, `cart`, or `regression`.
8. Keep shared navigation or header assertions in dedicated coverage tests so unrelated scenarios fail for one clear reason.
9. Add tests because they prove meaningful user behavior, not to inflate counts. Prefer focused negative cases and full user journeys that exercise realistic business paths.
10. Do not add framework unit tests or framework-only TestNG suites. If a framework change needs extra confidence, strengthen an existing UI scenario or add a new user-observable browser scenario instead.
11. Use `@Retryable(reason = "...")` only for scenarios with a documented infrastructure timing risk. Do not use retries to hide product defects or weak waits.
12. Prefer deterministic-but-varied test data for user-entered fields such as checkout names and postal codes. Keep fixed catalog expectations static, but avoid proving the workflow with only one identity fixture forever.
13. Keep visual regression checks opt-in under the `visual` group. Approve baselines deliberately and avoid adding screenshot comparisons to the default functional regression path.

The current suite includes one deliberate retry showcase on the performance-glitch persona login. Treat it as the reference example for future retryable scenarios.

### Example Test Annotation
```java
@Test(
    testName = "Verify adding multiple products to cart",
    description = "Adds two products and verifies the cart badge count updates to match.",
    groups = {TestGroups.SMOKE, TestGroups.CART},
    timeOut = TestTimeouts.STANDARD_UI_TIMEOUT_MS)
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
- **End-to-end journeys**: use a dedicated journey class when a test intentionally spans login, browsing, cart, checkout, and logout.
