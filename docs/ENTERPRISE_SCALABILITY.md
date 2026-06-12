# Enterprise Scalability

## What Scales Today

- Each TestNG worker owns an isolated `ThreadLocal<WebDriver>` and wait helper.
- Local and remote execution share the same page objects and typed configuration contract.
- Selenium Grid, browser capabilities, platform, version, viewport, and thread count are runtime inputs.
- GitHub Actions separates quality gates from a Chrome, Firefox, and Edge browser matrix.
- Failure artifacts are partitioned by browser job and contain capabilities needed for routing.

## Capacity Model

The practical concurrency ceiling is the lowest of:

1. TestNG `thread.count`.
2. Available Grid slots for the requested browser/capability.
3. Runner CPU and memory.
4. Target-environment account, data, and rate limits.
5. Artifact and log throughput.

Scale by sharding independent suites across CI jobs first, then increasing per-job thread count gradually. Record queue time, session startup time, test duration, retry rate, and target error rate at each step. Do not equate a large thread count with useful throughput.

## Enterprise Grid Pattern

For a managed Grid or cloud provider:

- Route sessions by browser, version, platform, region, and team label.
- Use short-lived credentials from the CI secret store.
- Apply per-team concurrency quotas and queue timeouts.
- Prefer immutable browser images and staged upgrades.
- Preserve session IDs, node identity, and optional video URLs in failure evidence.
- Separate smoke, regression, accessibility, and visual workloads so one queue cannot starve all signals.

## Data and Environment Isolation

Parallel UI tests require unique accounts or resettable state. Shared users, carts, and orders create false failures even when WebDriver is thread-safe. Enterprise adoption should provision test identities per worker, use deterministic cleanup, and prevent destructive suites from sharing an environment with release-blocking smoke coverage.

## Observability and SLOs

Track first-attempt pass rate, retry rate, p50/p95 test duration, Grid queue time, session creation failure rate, browser-specific failure rate, and artifact completeness. The repository emits per-run test, failure, skip, retry, and duration totals to `target/portfolio-metrics-v1.json`; a centralized platform should aggregate these with CI and Grid telemetry.

## Limits of This Demonstration

The bundled Docker Compose Grid is a reproducible development target, not a high-availability Grid. Sauce Demo is a controlled public demonstration application with static data and limited enterprise identity, network, and data-volume behavior. The framework demonstrates execution boundaries and diagnostics; production scale still requires capacity tests against the chosen Grid and owned environments.
