# Known Issues

| Area | Status | Workaround |
| --- | --- | --- |
| Public demo credentials | Password-backed scenarios need `APP_PASSWORD` | Public PRs run smoke coverage that does not require repository secrets |
| Safari execution | Requires local macOS headed execution | Use Chrome, Firefox, or Edge in Docker Grid for CI |
| Visual hash stability | Sensitive to browser and display changes | Keep visual checks opt-in until the target environment is pinned |
