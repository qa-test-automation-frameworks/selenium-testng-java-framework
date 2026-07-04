# Capability Status

| Capability | Status | Evidence |
| --- | --- | --- |
| Multi-browser Grid smoke | Enforced | `.github/workflows/ui-tests.yml` |
| Framework unit gate | Enforced | `testng-framework-unit.xml` |
| Accessibility scan | Opt-in | `testng-accessibility.xml` |
| Visual hash comparison | Opt-in | `testng-visual.xml` |
| Cloud-grid smoke | Secret-gated | `.github/workflows/cloud-grid.yml` |
| Allure diagnostics | Enforced for UI runs | `target/allure-results/` |
