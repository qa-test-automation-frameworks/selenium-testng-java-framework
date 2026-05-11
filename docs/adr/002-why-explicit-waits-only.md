# ADR 002: Use Explicit Waits Only

## Status
Accepted

## Context
UI tests need deterministic synchronization without hiding timing issues behind global implicit waits.

## Decision
Set implicit wait to zero and centralize synchronization in `WaitUtils`.

## Consequences
Page objects must use explicit waits for interactions and assertions. Failures are easier to diagnose because each timeout points to a specific awaited condition.
