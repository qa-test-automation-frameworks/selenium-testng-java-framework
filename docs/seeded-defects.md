# Seeded Defect Examples

| Seeded defect | Expected detector |
| --- | --- |
| Remove password masking before screenshots | Diagnostic redaction unit test and report review |
| Accept an unsupported browser string | Framework unit test for `BrowserType` |
| Replace explicit waits with an implicit wait | Static review plus affected UI scenario timing |
| Share one WebDriver between parallel methods | Browser matrix failures and session/capability evidence |
| Change a product price assertion | Focused inventory or checkout scenario |

Apply mutations only on a temporary branch or dedicated mutation job. The examples are intended to prove that the checks detect meaningful regressions, not to remain in the default branch.
