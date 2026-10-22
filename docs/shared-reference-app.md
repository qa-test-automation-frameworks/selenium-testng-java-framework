# Shared reference application profile

This optional, dated experiment runs one complete claims review journey twice against the immutable fictional target. Each invocation gives member A the `BaseTestCase` driver and gives member B a separate driver owned and cleaned by one bounded worker. It checks the target's server-enforced member boundary while the reviewer moves member A's claim through review and approval. Playwright remains the primary browser contract suite; this experiment is not a duplicate of the full UI suite or an always-required compatibility gate.

The test target pins application source `e9f438e06a2b092dccfc6e9ab1820ff2562ddad7`, fixture `v0.1.0`, its multi-architecture GHCR image digest, PostgreSQL 18.6, Selenium Grid 4.43.0, and the Chrome node digest in `docker/shared-app.compose.yaml`. Each run creates an isolated namespace and random synthetic database/control credentials, keeps PostgreSQL inside the Compose network, and removes volumes during teardown. The same named TestNG scenario runs twice sequentially. It records only hashes of remote session and claim identifiers, then checks that all four sessions and both claims are distinct and cleaned up.

GitHub Actions keeps this profile optional and runs it for pull requests that change its implementation, its runtime/build inputs, or its evidence publisher. Manual dispatch is also available; no schedule is needed. Each successful run is evidence only for its exact tested framework SHA and target revision. It does not guarantee compatibility for every later framework commit, another target revision, or general production use.

### Last recorded successful experiment

- Run: [Shared reference app UI #6](https://github.com/qa-test-automation-frameworks/selenium-testng-java-framework/actions/runs/39012550947), completed 2026-10-17 07:10:15 UTC.
- Tested checkout SHA: `18b7cf9b5adfe8ed81e4a6beff344cb8784322e7`.
- Target: `quality-reference-application`, source revision `e9f438e06a2b092dccfc6e9ab1820ff2562ddad7`, fixture `v0.1.0`.
- Current status: **current compatibility not established**. At the 2026-10-21 audit checkpoint, `main` was `528796a0e905420908b863d138c2dabb41a80757`, which differs from the tested checkout SHA. The target remains pinned to the listed source revision.

Run #6 predates the single-scenario, two-invocation control now in this profile. It is a historical evidence point and does not verify the updated harness; a fresh exact-head run is required for that.

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
