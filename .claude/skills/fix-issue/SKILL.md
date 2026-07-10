---
name: fix-issue
description: Fix a GitHub issue end-to-end in its own git worktree and open a draft PR against dev. Triggered by the user as /fix-issue <number> (e.g. "fix issue 42").
disable-model-invocation: true
---

# Fix Issue → Worktree → PR

Resolve issue `$ARGUMENTS` (number) following the repo workflow. Always work in a dedicated worktree and leave a draft PR; Gabriel tests from the PR before merging to `dev`.

Repo: `gago852/WeatherApp`. Read `CLAUDE.md` for the absolute rules, per-layer conventions, the `secrets.properties` requirement, and the verify commands.

## Steps

1. **Read the full issue** — title alone is not enough:

   ```bash
   gh issue view $ARGUMENTS --repo gago852/WeatherApp --comments
   ```

   Extract: the exact problem, files mentioned, any implementation hint, and the acceptance criteria. If it's ambiguous, **don't implement blindly** — ask and stop until answered:

   ```bash
   gh issue comment $ARGUMENTS --repo gago852/WeatherApp --body "Before implementing: ..."
   ```

2. **Create worktree + branch** from an updated `dev`:

   ```bash
   git fetch origin
   git worktree add ../WeatherApp-$ARGUMENTS-<slug> -b <type>/$ARGUMENTS-<slug> origin/dev
   ```

   Pick `<type>` (`feat`/`fix`/`refactor`/`test`/`chore`/`docs`/`perf`) per the issue and a short `<slug>`. Do all work inside that worktree.

3. **Implement** following Clean Architecture: `domain` (use cases / interfaces) → `data` (repositories, remote, datastore) → `ui` (Compose screens/viewmodels) → wire dependencies in `di` (Hilt). Add or update unit tests for the change.

4. **Verify** (the `/check` skill covers this) — do not commit if anything fails:

   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew assembleDebug
   ```

   The build needs `secrets.properties` (copy from `local.defaults.properties` if missing). If only `debugStabilityCheck` fails, rerun with `-x debugStabilityCheck -x releaseStabilityCheck` and surface the warnings rather than hiding them.

5. **Commit** — Conventional Commits, description in English (matching the repo history):

   ```
   <type>(<scope>): <imperative description>

   Closes #$ARGUMENTS
   ```

   Optional scopes: `data`, `domain`, `ui`, `di`, `weather`, `location`, `settings`.

6. **Open a draft PR** against `dev`:

   ```bash
   gh pr create --base dev --draft \
     --title "<type>(<scope>): <description>" \
     --body "$(cat <<'EOF'
   ## Changes
   - ...
   ## Tests
   - ...
   Closes #<n>
   EOF
   )"
   ```

7. **Report** the PR number/URL to Gabriel and how to verify it. Do **not** merge and do **not** remove the draft status. Leave the worktree in place for testing; it can be cleaned up later with `git worktree remove ../WeatherApp-$ARGUMENTS-<slug>`.

## Rules

- Never push directly to `dev` or `main` — both are protected; everything goes through the draft PR.
- Never commit secrets: `secrets.properties`, `local.properties`, `release.properties`, `google-services.json`, or any keystore.
- Do not remove or weaken the `resolutionStrategy.force` CVE pins in the root `build.gradle.kts`.
- Don't add dependencies or change build config (Gradle/AGP/Kotlin/SDK versions, `gradle/libs.versions.toml`, ProGuard rules, signing) without flagging it first — open the draft PR with a comment explaining what's needed and why.
