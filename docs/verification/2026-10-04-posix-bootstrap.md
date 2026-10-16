# POSIX bootstrap verification — 2026-10-04

The Maven wrapper is tracked with executable mode `100755`. A POSIX checkout no
longer needs `chmod +x mvnw` before following the quick start. Wrapper content and
the Windows wrapper are unchanged.

Validation used Linux amd64, Eclipse Temurin JDK 21.0.12.1 and Maven 3.9.15.
A disposable clone of implementation commit `44d5cf8` retained the executable
mode and successfully ran these documented commands:

```sh
./mvnw --version
./mvnw clean test -Dgroups=inventory,cart -Dheadless=true
```

The fresh-checkout browser suite completed with **15 tests, 0 failures,
0 errors and 0 skipped**. The smoke used headless Chrome and the existing public
Sauce Demo target. It required network access for the target and declared tool
downloads; no private provider credentials were supplied. Maven's existing
user-level download cache was available, so this is fresh-checkout verification,
not proof of a fully empty-cache or offline installation.

The original checkout also passed the framework quality command:

```sh
./mvnw -Dtestng.suite.file=testng-framework-unit.xml verify
```

It executed **3 framework tests, 0 failures/errors/skips**, and completed the
configured verification lifecycle, including static quality checks. Browser
smoke ran separately because those three tests do not establish browser behavior.

This evidence covers the documented POSIX first-success path. It does not claim
fresh Firefox, Edge, Grid, BrowserStack or every optional suite execution.
