# Capability Status

| Capability | Status | Evidence |
| --- | --- | --- |
| Multi-browser Grid smoke | Enforced | `.github/workflows/ui-tests.yml` |
| Framework unit gate | Enforced | `testng-framework-unit.xml` |
| Accessibility scan | Opt-in | `testng-accessibility.xml` |
| Visual hash comparison | Opt-in | `testng-visual.xml` |
| Cloud-grid smoke | Secret-gated | `.github/workflows/cloud-grid.yml` |
| Allure diagnostics | Enforced for UI runs | `target/allure-results/` |

## Feature currency review — 2026-10-16

| Capability | Current implementation | Official support checked | Benefit and compatibility risk | Decision and proof |
| --- | --- | --- | --- | --- |
| Selenium runtime | Selenium Java 4.51.0, Java 21, TestNG 7.12.0 | [Selenium 4.51 release](https://www.selenium.dev/blog/2026/selenium-4-51-released/) documents the Java, Grid and BiDi changes in this release. | Keeping the client and Grid image aligned avoids protocol drift; changing the runtime invalidates browser compatibility evidence. | **Pin 4.51.0.** Exact UI CI `38926217582` passed Chrome, Firefox and Edge suites, quality gates, cold bootstrap and Grid lifecycle sampling. |
| Local driver/browser setup | `WebDriverFactory` creates browser drivers without a third-party driver-manager dependency; Selenium Manager supplies its built-in fallback when a driver is unavailable. | [Selenium Manager](https://www.selenium.dev/documentation/selenium_manager/) documents driver and browser discovery, download, caching and offline controls. | Removes a separate manager dependency; first use can still require network access and cached browser assets. | **Retain Selenium Manager.** Document the cache/network prerequisite; do not call this path offline unless a cold-cache offline test proves it. |
| Remote Grid lifecycle | `RemoteWebDriver` targets a configured Grid URL; lifecycle cleanup is enforced and a bounded capacity experiment records queue/session behavior. | [Grid architecture](https://www.selenium.dev/documentation/grid/architecture/) documents the router, queue, distributor, nodes and slots. | A bounded remote path gives useful lifecycle and queue evidence. Capacity varies with host, browser, image, slots and workload. | **Retain.** D03 run `38926217582` measured 21 sessions at 1/2/4 workers on one hosted runner; this is not a general capacity promise. |
| WebDriver BiDi events | No BiDi event subscriptions are used; tests rely on WebDriver commands, screenshots and existing diagnostics. | [WebDriver BiDi](https://www.selenium.dev/documentation/webdriver/bidi/) documents the bidirectional event protocol; 4.51 adds Java BiDi code generation. | Console/network event capture could improve diagnosis, but it adds browser-specific behavior and does not currently close an observed gap. | **Defer.** Add only for a concrete event-driven test or diagnostic requirement, then verify the supported browser matrix and teardown. |
| Accessibility engine | axe-selenium 4.10.2 remains pinned in the current framework. | The open [4.13.0 candidate](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/pull/34) reports its scan and quality checks separately from browser execution. | A newer engine can add rules, but it may also change findings and require reviewed baseline or test updates. | **Retain 4.10.2 for now.** Candidate PR #34 fails Chrome, Firefox and Edge tests; do not treat its passing scan/quality gates as browser compatibility. |

The browser matrix and Grid sample support the current pinned Selenium line.
BiDi remains a deliberate omission, and the failed axe update stays visible as
unresolved compatibility work.
