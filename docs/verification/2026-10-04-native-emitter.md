# Native Selenium evidence verification — 2026-10-04

## Exact run

- Workflow run: [37213951076](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/37213951076)
- Implementation branch head: `958fb81cbc83a88802ea3fd958c0240642092f89`
- Exact PR merge checkout recorded in all five manifests: `907f8e7b5f52dd6376897a745a564db114bad46d` (matches the open PR's `merge_commit_sha`)
- Attempt: `1`
- `quality-gates`, `evidence-failure-control`, Chrome, Firefox, Edge, dependency review, and `required-ci` succeeded. Accessibility, dependency governance, and Pages publication were skipped by their declared trigger conditions.
- The cloud-grid workflow was not dispatched. This run makes no BrowserStack execution or no-key remote-record claim.

## Reconciled native counts

The five retained records listed in the [evidence manifest](evidence/2026-10-04-native-emitter/manifest.json) were downloaded from that exact Actions run. Every record is bound to its run ID, attempt, and tested merge SHA; all five passed the vendored schema and semantic validator. The failed record is intentional and valid evidence, not a failed workflow.

| Scope | Disposition | Selected | Passed | Failed | Skipped | Unique retried | Raw retry attempts |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Framework quality | passed | 3 | 3 | 0 | 0 | 0 | 0 |
| Failure control | failed | 1 | 0 | 1 | 0 | 0 | 0 |
| Chrome | passed | 34 | 34 | 0 | 0 | 0 | 0 |
| Firefox | passed | 34 | 34 | 0 | 0 | 0 | 0 |
| Edge | passed | 34 | 34 | 0 | 0 | 0 | 0 |

For each browser, the retained record's native XML input digest was recomputed from the downloaded `TEST-TestSuite.xml` and matched exactly; its counts also match the raw XML aggregate counters and testcase outcomes. The failure-control job checked its generated record against exactly one failed Surefire testcase, the raw XML digest, current Actions run ID, and current source SHA before passing. The quality gate also emitted a three-case record; its native input hash is retained in the record and was verified inside the job, but the quality job does not upload raw XML separately. Raw browser XML contains case-level details, so it is not committed; its original Actions artifacts expire on **2027-01-02 15:41:29 UTC**. Artifact archive hashes, record hashes, report hashes, and job IDs are listed in the [`evidence manifest`](evidence/2026-10-04-native-emitter/manifest.json).

The successful browser run exercised the actual Selenium/TestNG suite on Chrome, Firefox, and Edge. SauceDemo exposes no application release or fixture revision, so those values remain null with the limitation stated in each record. The dedicated control suite is included only by `testng-evidence-failure-control.xml` and is excluded from normal framework-quality and UI suites.

This verifies E02's Selenium quality, browser, failure, and aggregate-gate paths. Credential-skip behavior is covered by local schema tests, and an earlier run [36415043699](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/36415043699) had a successful secret preflight with BrowserStack skipped before this emitter existed; the new cloud-grid skip record still awaits its next scheduled run. Accessibility was not run in this PR workflow. Artifacts remain short-lived, so this is not E03 durable publication.
