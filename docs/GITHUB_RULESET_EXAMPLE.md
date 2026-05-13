# GitHub Ruleset Example

This repository cannot enforce branch protection settings for forks, but the example below shows the intended `main` branch policy for maintainers who want settings-as-code.

1. Replace `<owner>` and `<repo>` with your repository values.
2. Review `docs/examples/main-ruleset.json`.
3. Apply it from a GitHub CLI session with repository admin permissions:

```bash
gh api repos/<owner>/<repo>/rulesets --method POST --input docs/examples/main-ruleset.json
```

The ruleset requires pull requests and the main CI checks documented in `docs/GITHUB_SETUP.md`.
