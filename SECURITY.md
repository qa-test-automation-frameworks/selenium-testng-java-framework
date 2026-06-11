# Security Policy

Report credential exposure, workflow bypasses, dependency vulnerabilities, and diagnostic-redaction failures privately through GitHub security reporting. Do not include live credentials in a public issue.

Secrets belong in environment variables or CI secret stores. Pull-request workflows from forks must remain uncredentialed, and report artifacts must pass through the framework's redaction controls before publication.
