# ADR 006: Keep Accessibility and Visual Checks Opt-In

## Status
Accepted

## Context
Accessibility and visual checks are useful portfolio extensions, but adding them to the default functional smoke path would introduce extra dependencies, baseline maintenance, and unrelated failure modes.

## Decision
Keep accessibility and visual regression as opt-in extension points. Add a future `accessibility` group only when an axe-core dependency and a small inventory-page smoke check are maintained deliberately. Do not add visual baselines until screenshots can be stabilized across CI browsers.

## Consequences
The core suite remains focused on functional UI behavior. The repository still documents where accessibility and visual checks fit without pretending that the current Docker Grid setup records videos, traces, or visual baselines.
