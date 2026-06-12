# Reliability, Retry, and Quarantine Policy

## Default Behavior

Retries are disabled by default. A failing browser assertion remains a failing test unless a reviewer explicitly enables retries and the test carries `@Retryable(reason = "...")`.

## Retry Rules

- Retry only a known transient browser or target condition with a documented reason.
- Do not retry assertion defects, invalid test data, authentication failures, configuration errors, or deterministic selector failures.
- Keep `retry.count` at one for diagnostic runs unless an incident requires a documented exception.
- CI evidence must expose retry counts through Allure and `target/portfolio-metrics-v1.json`.
- A test that passes only after retry is investigated as a flake; it is not treated as equivalent to a first-attempt pass.

## Quarantine Rules

Quarantine entries live in `reliability/quarantine.yml`. Each entry must include a fully qualified test identifier, owner, issue URL, failure signature, added date, and an expiry no more than 14 days later. Expired entries block merge. The register is currently empty.

## Triage

1. Reproduce in the same browser, execution type, viewport, and thread count.
2. Inspect the assertion, screenshot, URL, capabilities, page source, console output, and framework log.
3. Compare matrix entries to distinguish product behavior from browser or Grid infrastructure.
4. Fix deterministic framework or test defects without retries.
5. Quarantine only when an owned issue and removal date exist.
