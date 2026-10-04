# Native Selenium evidence verification — 2026-10-04

## Exact run

- Workflow run: [37213038141](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/37213038141)
- PR branch head: `1cb54b88b2baeb6f210af6c147e977496d0a2639`
- Exact PR merge checkout recorded in the manifests: `84a71b252cb76e49313f1a79235e24ef9b7c32b0` (matches the open PR's `merge_commit_sha` at verification time)
- Attempt: `1`
- Job results: `quality-gates` 111467887397, Chrome 111468027115, Edge 111468027086, Firefox 111468027123, and `required-ci` 111468779948 all succeeded. Dependency review 111467887559 succeeded. Accessibility, dependency governance, and Pages publication were skipped by their declared trigger conditions.
- The separate BrowserStack workflow was not dispatched; these results do not claim a cloud-grid execution or no-key remote run.

## Reconciled native counts

The four retained records under [`records`](evidence/2026-10-04-native-emitter/records) were downloaded from that exact Actions run. Every record has the same run ID, attempt, and exact merge checkout SHA, passes the vendored schema/semantic validator, and has `publication.disposition = pending`.

| Scope | Native Surefire selected | Passed | Failed | Skipped | Unique retried | Raw retry attempts |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Framework quality | 3 | 3 | 0 | 0 | 0 | 0 |
| Chrome | 34 | 34 | 0 | 0 | 0 | 0 |
| Firefox | 34 | 34 | 0 | 0 | 0 | 0 |
| Edge | 34 | 34 | 0 | 0 | 0 | 0 |

For each browser, the retained record's native XML input digest was recomputed from the downloaded `TEST-TestSuite.xml` bytes and matched exactly. The record counts also match the raw XML's `tests`, `failures`, `errors`, `skipped`, and testcase outcomes. The record SHA-256 values and native input hashes are in [`manifest.json`](evidence/2026-10-04-native-emitter/manifest.json). Raw XML and detailed Surefire HTML were checked locally but are not committed because they contain case-level diagnostic information; their original Actions artifacts expire on **2027-01-02 15:26:58 UTC**.

The organization schema validator accepted all four downloaded records. The run exercised actual Java 21 / Maven 3.9.15 / TestNG 7.12.0 UI execution on all three browsers. The SauceDemo target does not expose a release or fixture revision, so those values remain null with the limitation stated in each record. This is current E02 execution evidence only; short-lived Actions artifacts are not E03 durable publication, and the optional BrowserStack and accessibility scopes remain separate.
