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

Also require pull request review, require branches to be up to date, and restrict direct pushes to maintainers.

## Repository-Specific Values

After forking, update:

- README badge URLs.
- GitHub Pages report link.
- `APP_PASSWORD` secret.
- Branch protection required check names if GitHub displays matrix jobs differently in the fork.
- Allure issue pattern by passing `-Dallure.issue.pattern=https://github.com/<owner>/<repo>/issues/{}` in custom Maven invocations.

The bundled GitHub Actions workflow already passes the repository-specific Allure issue pattern with `github.repository`.

## Dependency Governance

Dependabot opens weekly PRs for Maven, Docker, and GitHub Actions updates. The scheduled `dependency-governance` job uploads Maven dependency/plugin update reports and a CycloneDX SBOM.
