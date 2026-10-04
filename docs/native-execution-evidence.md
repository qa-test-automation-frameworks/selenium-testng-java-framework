# Native execution evidence

The UI workflow emits one schema-v3 record per framework quality run, browser
matrix leg, optional accessibility run, and BrowserStack run/skip. Counts come
from current Maven Surefire `TEST-*.xml` files, not logs or Allure JSON. The
adapter checks XML safety, file size, root shape, nonempty cases, and agreement
between native aggregate counters and testcase entries. It retains aggregate
counts and a SHA-256 of the native input in limitations; it omits case names,
parameters, exception text, credentials, screenshots, and logs from the record.

`selected` is the number of final Surefire testcase invocations, `executed` is
selected minus skipped, and passed/failed are final outcomes. `retried` counts
unique testcase entries containing a Surefire `flakyFailure` or `flakyError`;
retries never inflate selected or final outcomes. `native_retry_attempts` records
the total number of those native attempt elements separately. If Surefire's
output does not expose retries in those standard elements, both measures are
zero; the native XML hash and attempt elements remain in the existing raw report
artifact. A missing report emits unavailable evidence and fails collection; inconsistent output
emits partial failed evidence and fails collection. The earlier native test
step retains its exit status, so valid failure evidence cannot turn a failed
test green.

BrowserStack with missing secrets emits `skipped` with a reason, null counts,
null execution timestamps, and no received shard. It does not start a Maven
runner or claim a test result. The stable `required-ci` job fails unless the
quality gate and all three browser matrix children pass; accessibility is
required on its scheduled trigger and when requested manually.

The Cloud Grid manual input `run_smoke` defaults to false because a real
BrowserStack run may consume paid cloud minutes. A scheduled run proceeds when
credentials exist; a manual run proceeds only after explicit opt-in. Every
non-execution path produces a reasoned skip record without contacting the
BrowserStack hub.

The checked-in schema and dependency-free Node validator are portable snapshots
of the organization E01 contract. Schema SHA-256:
`40502a47e825755d216e35ea19716ad7de69e47accd6aed1cfb3607160e4d1b9`.
Freshness policy SHA-256:
`1313329c1c06bd3a056dd1ed7d25c631036cae58480fdc4e767f22a0a01a568e`.
Update these snapshots deliberately when E01 changes. Node is pinned by
`.nvmrc`; the workflow uses setup-node before invoking the validator.

GitHub API inspection on 2026-10-04 found an active `main-protection` ruleset.
Its current required contexts are `quality-gates`, `test (CHROME)`,
`test (EDGE)`, and `test (FIREFOX)`; legacy branch protection returned
“Branch not protected”. The new standalone `required-ci` check is not yet named
in that ruleset. The four existing required contexts already enforce each
mandatory child separately; add `required-ci` to the rule only after a new
workflow run confirms its exact check name.

Local verification used the real framework-quality Maven suite (3 passed, 0
failed, 0 skipped), then generated and validated a current-format record. Five
focused Python tests cover redaction, failed native outcome, missing/inconsistent
reports, credential skip semantics, and a runner that never started. A local
dedicated TestNG failure-control suite (`testng-evidence-failure-control.xml`)
produced exactly one reconciled failed record locally. Its explicit suite keeps
the intentional failure out of ordinary framework-quality runs. The workflow's
`evidence-failure-control` job now runs that suite with continue-on-error only
for the native test step, then requires a schema-valid, source-bound failed
record matching its raw Surefire report. Remote verification of the committed
failure control and all three browser legs passed in [run 37213951076](verification/2026-10-04-native-emitter.md).
The optional BrowserStack credential-skip record has only local schema coverage;
the modified cloud-grid path awaits a scheduled run, and accessibility was not
part of that PR workflow. These optional scopes are not represented as executed.
