# ADR 006: Keep Accessibility and Visual Checks Opt-In

## Status
Accepted

## Context
Accessibility and visual checks are useful portfolio extensions, but adding them to the default functional smoke path would introduce extra dependencies, baseline maintenance, and unrelated failure modes.

## Decision
Keep accessibility and visual regression as opt-in extension points. The repository now includes a deliberately narrow `testng-accessibility.xml` suite with an `accessibility` group for stable baseline DOM checks and axe-core scanning on the inventory page, plus a `testng-visual.xml` scaffold that compares approved screenshot hashes when reviewers deliberately opt in. Do not add visual checks to the default functional regression path until screenshot stability expectations are well understood for the chosen environment.

## Consequences
The core suite remains focused on functional UI behavior. Accessibility now has a real rules engine alongside the deterministic probe. Visual checks remain opt-in because screenshot stability depends on the selected browser environment.
