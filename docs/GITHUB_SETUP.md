# GitHub Setup Guide

Use this checklist after forking or publishing the repository.

## Required Secret

Create an Actions secret named `APP_PASSWORD` with the Sauce Demo password used by trusted login, persona, journey, and full-regression runs.

Forked pull requests do not receive this secret and intentionally run only no-secret `inventory` and `cart` smoke coverage.

## GitHub Pages

Enable GitHub Pages with GitHub Actions as the source. The `publish-allure-report` job deploys the merged Allure report from `main` and exposes the Pages URL in the job environment.

## Required Checks

Recommended required checks for `main`:

- `quality-gates`
- `test (CHROME)`
- `test (FIREFOX)`
- `test (EDGE)`
- `Docs / local-links`
- `Secret Scan / scan`

Also require pull request review and restrict direct pushes to maintainers. Treat "Require branches to be up to date before merging" as optional for this portfolio repository: it improves merge freshness, but it can trigger extra CI runs on a free account.

An optional settings-as-code example is available in [GitHub Ruleset Example](GITHUB_RULESET_EXAMPLE.md), and a repository-ready ruleset artifact is committed at `.github/rulesets/main-protection.json`.

## Repository-Specific Values

After forking, update:

- README badge URLs.
- GitHub Pages report link.
- `APP_PASSWORD` secret.
- Branch protection required check names if GitHub displays matrix jobs differently in the fork.
- Allure issue pattern by passing `-Dallure.issue.pattern=https://github.com/<owner>/<repo>/issues/{}` in custom Maven invocations.

The bundled GitHub Actions workflow already passes the repository-specific Allure issue pattern with `github.repository`.

## Dependency Governance

Dependabot opens monthly grouped PRs for Maven, Docker, and GitHub Actions updates. The scheduled `dependency-governance` job runs quarterly and uploads Maven dependency/plugin update reports plus a CycloneDX SBOM.

The scheduled UI regression workflow also opens or updates a `scheduled-failure` GitHub issue automatically if the quarterly scheduled run fails, so recurring CI health problems are visible without external chat integrations.
