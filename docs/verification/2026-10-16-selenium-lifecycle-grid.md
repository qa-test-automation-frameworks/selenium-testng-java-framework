# Selenium session lifecycle and Grid sample

The lifecycle controls run without a browser. They inject a fake `WebDriver` through a package-private factory seam and cover session-creation failure, setup failure after a session exists, a disconnected browser during quit, and two simultaneous TestNG workers. The controls verify that a partially initialized session is quit where possible and that driver and wait helpers are removed from the owning thread even when quit throws.

`BaseTestCase.afterMethod` now puts teardown in a `finally` block around failure diagnostics. `FrameworkListener` also clears the thread-local session after a TestNG configuration failure. The factory closes any driver it created if timeout configuration, window setup, diagnostics initialization, or wait-helper setup fails.

The current bounded Chrome Grid sample is documented in [the 22 October 2026 report](2026-10-22-grid-capacity.md), with the exact-head workflow run and sanitized source rows. The request-to-session-created timer includes Grid queueing and browser startup; the client measurement cannot split them. The sample uses three repetition batches per concurrency level, and its medians and ranges are descriptive only. It does not establish product capacity or production throughput.

The sample records SHA-256 digests of session identifiers only, so independent sessions can be checked without publishing raw session IDs. On successful runs, the workflow summary includes the resource limits and redacted sample JSON.

The separate shared-reference-app experiment is dated evidence for one exact framework checkout and pinned target. Its last recorded successful run is [#6](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/39012550947), completed 2026-10-17 07:10:15 UTC against checkout `18b7cf9b5adfe8ed81e4a6beff344cb8784322e7` and application revision `e9f438e06a2b092dccfc6e9ab1820ff2562ddad7`. That run predates the updated single-scenario harness. Since `main` has changed, current compatibility is not established. See [the shared-app profile](../shared-reference-app.md) for its scope and current evidence record.
