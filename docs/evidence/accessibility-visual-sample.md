# Accessibility and Visual Sample Evidence

This is a reviewer-facing sample of the optional extension boundary, not a claim of
WCAG conformance or pixel-perfect visual testing.

| Signal | Sample scope | Interpretation |
|---|---|---|
| Accessibility smoke | Structural checks against the Sauce Demo page | Useful regression signal; requires a product-specific WCAG scope and manual review process. |
| Visual baseline | Hash/baseline scaffold for selected page output | Detects a changed baseline input; it is not a pixel-diff service. |
| Ownership | Baseline changes require review and an intentional update | Prevents silent screenshot drift. |

For an enterprise product, define the target WCAG criteria, browser/device matrix,
baseline owner, approved-change workflow, and false-positive triage before treating
either signal as a release gate.
