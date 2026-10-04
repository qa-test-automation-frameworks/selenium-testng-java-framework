# Native Selenium evidence verification — 2026-10-04

## Exact executions

- UI workflow: [37214733913](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/37214733913), PR head `7a2d420da252a294b017986034f96844005efa14`, exact PR merge checkout `0fc841270da21f08863cf9ec296d057393d453d3`, attempt 1.
- Cloud Grid: [37214755074](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/37214755074), branch head `7a2d420da252a294b017986034f96844005efa14`, attempt 1, manual dispatch with `run_smoke=false`.
- UI `quality-gates`, `evidence-failure-control`, Chrome, Firefox, Edge, dependency review, and `required-ci` all succeeded. Accessibility, dependency governance, and Pages publication were skipped by their declared trigger conditions.
- Cloud `check-secrets` succeeded, emitted a skipped record stating credentials are not configured, and uploaded it. `browserstack-smoke` was skipped; no session started.

## Reconciled native results

The six retained records listed in the [evidence manifest](evidence/2026-10-04-native-emitter/manifest.json) were downloaded from those exact runs. All six pass the vendored schema and semantic validator. UI records bind to the tested PR merge SHA; the Cloud Grid skip binds to the workflow-dispatch branch SHA.

| Scope | Disposition | Selected | Passed | Failed | Skipped | Retried | Raw retry attempts |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Framework quality | passed | 3 | 3 | 0 | 0 | 0 | 0 |
| Failure control | failed | 1 | 0 | 1 | 0 | 0 | 0 |
| Chrome | passed | 34 | 34 | 0 | 0 | 0 | 0 |
| Firefox | passed | 34 | 34 | 0 | 0 | 0 | 0 |
| Edge | passed | 34 | 34 | 0 | 0 | 0 | 0 |
| BrowserStack | skipped | null | null | null | null | null | no run |

For each of the five executed scopes, the manifest's Surefire XML input hash was recomputed from the downloaded `TEST-TestSuite.xml` bytes and matched; aggregate counters match the XML and case-level outcomes. This includes the intentional one-case TestNG failure. The quality and negative-control raw XML summaries are now retained as workflow artifacts as well as hashed in their records. Browser report artifacts and all manifest/native archive digests are recorded with their expiry in the evidence manifest; artifacts expire **2027-01-02** and are not durable publication.

The run exercised Java 21, Maven 3.9.15, TestNG 7.12.0, and the actual Selenium suites on Chrome, Firefox, and Edge. SauceDemo exposes no release or fixture revision, so those values remain null with the limitation stated in each record. The dedicated failure suite is included only by `testng-evidence-failure-control.xml`; it is excluded from ordinary framework-quality and UI suites. The cloud dispatch's false opt-in is deliberate: an actual BrowserStack smoke may consume paid cloud minutes.

GitHub's active `main-protection` ruleset requires `quality-gates` and the three browser checks. The `required-ci` aggregation and negative-control job pass on this run, but the rule itself was not modified. E02 Selenium UI, failure, and no-key skip paths are now verified; accessibility remains an optional unexecuted scope. E03 durable retention and E04 ingestion remain separate work.
