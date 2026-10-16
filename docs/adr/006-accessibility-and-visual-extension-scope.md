# ADR 006: Keep Accessibility and Visual Checks Opt-In

## Status

Accepted

## Context

Accessibility and visual checks are useful portfolio extensions, but adding them to the default functional smoke path would introduce extra dependencies, baseline maintenance, and unrelated failure modes. At portfolio level, Playwright owns the routine Axe coverage for the shared target. Selenium's distinguishing acceptance scope is browser lifecycle, cross-browser execution, and Grid capacity; repeating the same page/rule checks in every framework would increase maintenance without adding independent coverage.

## Decision

Keep accessibility and visual regression as opt-in extension points for ordinary pull requests. The repository includes a deliberately narrow `testng-accessibility.xml` suite with an `accessibility` group for stable baseline DOM checks and axe-core scanning on the inventory page, plus a `testng-visual.xml` scaffold that compares approved screenshot hashes when reviewers deliberately opt in. The accessibility suite is run on the quarterly scheduled workflow and can be requested through `workflow_dispatch`; when requested or scheduled, the `required-ci` aggregate requires it to pass. It remains optional on routine pull requests because Playwright carries routine shared-target Axe coverage while Selenium focuses on its distinct browser/Grid lifecycle risks. Reassess this boundary for A05 only if a Selenium-specific accessibility risk or behavior is identified on the shared application. Do not add visual checks to the default functional regression path until screenshot stability expectations are well understood for the chosen environment.

## Consequences

The core pull-request suite remains focused on functional UI behavior, while scheduled/manual accessibility execution still produces real Axe results and fails closed when requested. Portfolio-level accessibility coverage should be claimed from the Playwright routine suite; Selenium provides an optional cross-check, not a second independent accessibility program. Visual checks remain opt-in because screenshot stability depends on the selected browser environment.
