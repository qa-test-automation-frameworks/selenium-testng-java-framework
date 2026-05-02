# Test Writing Guide

Follow these guidelines when adding new tests or modifying existing ones.

## Adding a New Page Object
1. Create a new class in `src/main/java/common/pageobject`.
2. Extend `BasePage`.
3. Add a constructor that calls `super(driver)`.
4. Define locators using private `By` variables.
5. Implement action methods and use `@Step` for reporting.
6. Use `WaitUtils` or existing BasePage helpers for pre-action readiness checks.
7. Keep business assertions in test classes, not page objects.

## Adding a New Component
If a UI element is shared across multiple pages (e.g., a Footer):
1. Create a class in `common.pageobject.component`.
2. Extend `BasePage`.
3. Instantiate it within the Page Objects that use it.

## Writing a New Test
1. Create a test class in `src/test/java/tests`.
2. Extend `BaseTestCase`.
3. Annotate test methods with `@Test`.
4. Use the `testName` attribute in `@Test` for better reporting.
5. Initialize Page Objects in a `@BeforeMethod` setup.
6. Use **AssertJ** for assertions. Always include a descriptive message using `.as()`.
7. Add meaningful TestNG groups such as `smoke`, `login`, `inventory`, `cart`, or `regression`.

### Example Assertion
```java
assertThat(page.getSomeValue())
    .as("The balance should be positive after the transaction")
    .isGreaterThan(0);
```

## Naming Conventions
- **Pages**: `SomethingPage`
- **Components**: `SomethingComponent`
- **Tests**: `business_logic_should_do_something` (lowercase with underscores)
