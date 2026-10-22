# Shared reference application profile

This optional, dated experiment runs one complete claims review journey twice against the immutable fictional target. Each invocation gives member A the `BaseTestCase` driver and gives member B a separate driver owned and cleaned by one bounded worker. It checks the target's server-enforced member boundary while the reviewer moves member A's claim through review and approval. Playwright remains the primary browser contract suite; this experiment is not a duplicate of the full UI suite or an always-required compatibility gate.

The test target pins application source `e9f438e06a2b092dccfc6e9ab1820ff2562ddad7`, fixture `v0.1.0`, its multi-architecture GHCR image digest, PostgreSQL 18.6, Selenium Grid 4.43.0, and the Chrome node digest in `docker/shared-app.compose.yaml`. Each run creates an isolated namespace and random synthetic database/control credentials, keeps PostgreSQL inside the Compose network, and removes volumes during teardown. The same named TestNG scenario runs twice sequentially. It records only hashes of remote session and claim identifiers, then checks that all four sessions and both claims are distinct and cleaned up.

GitHub Actions keeps this profile optional and runs it for pull requests that change its implementation, its runtime/build inputs, or its evidence publisher. Manual dispatch is also available; no schedule is needed. Each successful run is evidence only for its exact tested framework SHA and target revision. It does not guarantee compatibility for every later framework commit, another target revision, or general production use.

### Latest recorded successful experiment

- Run: [Shared reference app UI #23](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/39708395542), completed 2026-10-22 01:56:03 UTC.
- Exact tested checkout SHA: `01eea29e6f7d4c3ea924b2dbf50fc42345d37ab1`; tested tree SHA: `aaeea8d555842a843670aff4d953f1506d0d2357`.
- Target: `quality-reference-application`, source revision `e9f438e06a2b092dccfc6e9ab1820ff2562ddad7`, fixture `v0.1.0`.
- Native TestNG/Surefire result: 2 invocation rows passed with no failures or skips. Surefire reported one unique method and two invocation rows; their durations were 2.572 s and 2.299 s, with 9.677 s total suite time. Schema-v3 evidence completed with `disposition=passed` and `integrity=complete`.
- Sanitized coordination result: 4 distinct session hashes, 4 cleanups, and 2 distinct claim hashes.
- Runtime: Java 21.0.12.1, Maven 3.9.15, Selenium 4.51.0, TestNG 7.12.0.
- Current `main` commit `863e736c2a1dd1b16ca5f3cb609bb2d35b23a3a6` has the same tree SHA as the tested checkout. Compatibility is verified for this exact framework tree and target revision; later framework tree or target changes remain unverified.

Run #6 ([2026-10-17](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/39012550947)) is a historical point from before this single-scenario, two-invocation harness. The updated harness is verified by run #23. The workflow path check also confirmed selection for driver source, test source/resources, Compose, TestNG suites, Maven inputs, evidence scripts, documentation, and the workflow itself; the check output is kept under `work/audit-2026-10-21/B4/logs/workflow-path-match.log`.

The workflow's run summary and schema-v3 artifact carry the exact checkout SHA, run ID, completion time, and target identity for each later execution. Evidence timestamps come from the execution itself; editing this page does not refresh experiment evidence. The evidence scope is `selenium.shared-app`, separate from the required Sauce Demo UI scopes. Anonymous image access is required. If the image is private, the run should fail before browser tests and must not be represented as a product-test failure.

For local execution, export unique `APP_NAMESPACE`, random `POSTGRES_PASSWORD`, and random `TEST_CONTROL_TOKEN`, then run:

```sh
docker compose -f docker/shared-app.compose.yaml up -d db reference-app selenium-hub chrome
./mvnw -B -ntp -C -Dtestng.suite.file=testng-shared-app.xml \
  -Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=CHROME -Dheadless=true -Dthread.count=2 \
  -Dapp.url=http://reference-app:4300/ test
docker compose -f docker/shared-app.compose.yaml down --volumes --remove-orphans
```
