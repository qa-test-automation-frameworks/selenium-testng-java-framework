# Supported runtime policy

JDK 21 is the required build and test baseline. Maven Enforcer rejects JDK 17
before tests with an explicit supported-version message, and the Java compiler
targets release 21. CI uses Eclipse Temurin 21 for framework, browser and Grid
jobs. The Maven wrapper selects the Maven version. Newer JDKs are not claimed as
supported until the browser drivers, Selenium and Maven plugins have been
verified together.

Node.js 24.21.0 is pinned in `.nvmrc` for dependency-free evidence validation;
it is not required to compile or run the Java test suites. The evidence helper
fails early when Node is below the pin or uses another major. Node 24 is an
active LTS line in the [official release schedule](https://nodejs.org/en/about/previous-releases),
and [24.21.0](https://nodejs.org/en/blog/release/v24.21.0) is the current tested
patch baseline. Stable later Node 24 patches may satisfy the check, but are not
automatically verified.

The no-secret first-success path on a clean checkout is:

```sh
./mvnw -B -Dtestng.suite.file=testng-framework-unit.xml verify
```

This runs the framework-quality suite. For the documented headless browser
smoke, install the matching browser and run:

```sh
./mvnw clean test -Dgroups=inventory,cart -Dheadless=true
```

The CI workflow provisions Temurin 21, includes a fresh Maven-cache bootstrap,
and executes the unsupported-JDK rejection control. The supported baseline was
checked against upstream release and lifecycle information on 2026-10-16.
Runtime updates require rerunning quality, browser and Grid compatibility
checks; prior results remain tied to the runtime that produced them.
