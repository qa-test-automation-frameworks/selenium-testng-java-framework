# Verification Status and Historical Results

## Latest main-branch UI run observed — 2026-10-17

At the time of this review, the latest successful default-branch UI run was
[`38938759562`](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/38938759562)
on `main` at `0a430bc15350126514d24daf5c74c431f8a7d68a`; it completed
successfully on 2026-10-16. For a newer run, inspect the
[live evidence dashboard](https://qa-test-automation-frameworks.github.io/.github/)
and follow the Selenium source-run link.

The detailed counts below are from the retained July run, not run `38938759562`.
They are kept as historical context and are not a current browser-matrix result.

## Historical run — 2026-07-11

| Field | Value |
|---|---|
| Repository ref | `main` @ [`10f86c3`](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/commit/10f86c348edc63fe7060d1d8523e306635792c8d) |
| Fast/full gate | [`ui-tests.yml` run 29137543488](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/29137543488) — framework quality, Grid Chrome/Firefox/Edge, accessibility, and Allure publication; completed 2026-07-11T03:15:08Z |
| Record status | Historical; superseded by later main-branch UI runs |
| Target/environment | Vendor-operated Sauce Demo application, exercised through the repository-controlled Docker Grid |
| Evidence class | Controlled browser execution environment; the application under test is third-party hosted |
| Result counts | 34 tests, 34 passed, 0 failed, 0 skipped per browser — CHROME 52.042s, FIREFOX 195.488s, EDGE 69.480s |
| Release | [`v1.0.0`](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/releases/tag/v1.0.0) (portfolio manifest realigned to match the published GitHub release) |
| Report | [Allure history](https://qa-test-automation-frameworks.github.io/selenium-testng-java-framework/) |
| Known limitations | [Known issues](../known-issues.md) and [portfolio review guide](../PORTFOLIO_REVIEW_GUIDE.md) |

The machine-readable record with the exact SHA, run ID/URL, conclusion, and
per-browser result counts is retained at
[`historical-verification.json`](historical-verification.json).
