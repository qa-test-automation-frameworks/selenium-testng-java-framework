# Shared reference application profile

This profile runs two deliberately small journeys against the immutable fictional claims target used by the portfolio. It demonstrates the Selenium framework's JVM and remote Grid lifecycle, two parallel browser sessions, and the APP's server-enforced member boundary. Playwright remains the primary browser contract suite; this is not a duplicate of the full UI suite.

The test target pins application source `e9f438e06a2b092dccfc6e9ab1820ff2562ddad7`, fixture `v0.1.0`, its multi-architecture GHCR image digest, PostgreSQL 18.6, Selenium Grid 4.43.0, and the Chrome node digest in `docker/shared-app.compose.yaml`. The run creates an isolated namespace and random synthetic database/control credentials, keeps PostgreSQL inside the Compose network, and removes volumes during teardown. The two TestNG methods execute concurrently with separate thread-local drivers: Member A submits a claim and a reviewer moves it through review to approval; Member B verifies that the claim is absent from its own session.

GitHub Actions runs this profile on pull requests that change its implementation and supports manual dispatch. Required proof includes the readiness endpoint's source/fixture identity, Grid status, Surefire native results, schema-v3 evidence validation, and unconditional Compose cleanup. The evidence scope is `selenium.shared-app`; it is separate from the existing Sauce Demo UI scopes. Anonymous image access is required. If the image is private, the run should fail before browser tests and must not be represented as a product-test failure.

For local execution, export unique `APP_NAMESPACE`, random `POSTGRES_PASSWORD`, and random `TEST_CONTROL_TOKEN`, then run:

```sh
docker compose -f docker/shared-app.compose.yaml up -d db reference-app selenium-hub chrome
./mvnw -B -ntp -C -Dtestng.suite.file=testng-shared-app.xml \
  -Dexecution.type=remote -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=CHROME -Dheadless=true -Dthread.count=2 \
  -Dapp.url=http://reference-app:4300/ test
docker compose -f docker/shared-app.compose.yaml down --volumes --remove-orphans
```
