# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Single-module native Android weather app. Kotlin 2.3.21, AGP 9.2, Java 17 target, Jetpack Compose (Material 3), MVVM + Clean Architecture (`data` / `domain` / `ui` / `di` under `com.gago.weatherapp`). DI via Hilt, networking via Retrofit + Moshi, Firebase Analytics/Crashlytics. minSdk 27, target/compileSdk 37. Dependencies are managed in `gradle/libs.versions.toml`.

## Build & test

- Unit tests (what CI runs): `./gradlew testDebugUnitTest`
- Single test: `./gradlew testDebugUnitTest --tests "com.gago.weatherapp.SomeTest"`
- Debug build: `./gradlew assembleDebug`
- Release bundle: `./gradlew bundleRelease` (needs signing config; only set up in CI)
- Instrumented tests are **not** run in CI: `./gradlew connectedAndroidTest` (needs a device/emulator)

## Required setup — the build fails without this

API keys are injected via the Google Secrets Gradle plugin from `secrets.properties` at the repo root (gitignored). Without it the build fails. Copy `local.defaults.properties` (the committed template) to `secrets.properties` and fill in:
- `API_KEY` — OpenWeatherMap
- `PLACES_API_KEY` — Google Places

Both are exposed via `BuildConfig` and the manifest. Never commit `secrets.properties`.

## Gotchas

- **Stability analyzer:** the `compose.stability.analyzer` plugin adds `debugStabilityCheck` / `releaseStabilityCheck` tasks that gate the build. To run a plain build skipping them (as the CodeQL workflow does): `./gradlew build -x debugStabilityCheck -x releaseStabilityCheck`.
- **Dependabot alerts on jose4j/jdom2/bcprov/bcpkix/commons-lang3/httpclient are AGP's own transitive deps, not the app's.** These libraries never appear in any `:app` compile/runtime/test configuration — they're pulled in by the Android Gradle Plugin's own build-time tooling (lint, manifest merger, etc.) via the plugin classpath that Gradle resolves from `pluginManagement` in `settings.gradle.kts`. That classpath is a separate resolution graph from `allprojects.configurations.all`/`buildscript {}`, so a `resolutionStrategy.force` in `build.gradle.kts` **cannot reach it** — confirmed empirically with `./gradlew buildEnvironment`, which still showed the unpatched versions even with the force block in place. (A prior commit, `de45c79`, added exactly such a force block believing it patched these CVEs; it was a no-op and has since been removed.) They don't ship in the APK, so there's no runtime risk to app users — but the alerts will keep showing in GitHub's Dependabot until AGP itself bumps its transitive deps upstream. If new alerts show up in this family again, don't reach for `resolutionStrategy.force` in `build.gradle.kts` — verify first with `./gradlew :app:dependencies --configuration <config>` (should show nothing) and `./gradlew buildEnvironment` (will show the real, unpinnable version).
- **Lint:** no ktlint/detekt/spotless. Android Lint runs as part of the build against `app/lint-baseline.xml`. Regenerate the baseline (don't hand-edit) if lint output is the only blocker. Kotlin style is "official" (`kotlin.code.style=official`), applied by the IDE — there is no format CLI.

## Conventions

- Branches: `dev` is the default working branch; `main` is production (runs CodeQL + deploy). **Both `dev` and `main` are protected — you cannot `git push` to them directly; every change lands via a PR.** Open PRs against `dev` unless releasing. See the worktree workflow below.
- Issue templates exist for feature / bug / chore under `.github/ISSUE_TEMPLATE/`.
- Additional context lives in `.cursor/rules/*.mdc` (architecture, testing, UI, performance guidelines).

## Workflow: GitHub Issue → Worktree → PR

Gabriel opens issues and asks for them to be fixed. Each task lives in its **own git worktree** and ends in a **draft PR**; Gabriel tests from the PR before merging to `dev`. Shortcut: `/fix-issue <number>`.

`dev` and `main` are protected — **never push to them directly.** Always work on a `<type>/<n>-<slug>` branch and open a PR against `dev`.

1. **Read the full issue** before implementing:

   ```bash
   gh issue view <n> --repo gago852/WeatherApp
   ```

   If it's ambiguous, don't implement blindly — ask via `gh issue comment <n> --body "..."`.

2. **Create worktree + branch** from an updated `dev`:

   ```bash
   git fetch origin
   git worktree add ../WeatherApp-<n>-<slug> -b <type>/<n>-<slug> origin/dev
   ```

   Types: `feat` · `fix` · `refactor` · `test` · `chore` · `docs` · `perf`.

3. **Implement** following Clean Architecture: `domain` (use cases / interfaces) → `data` (repositories, remote, datastore) → `ui` (Compose screens/viewmodels) → wire dependencies in `di` (Hilt).

4. **Verify before committing** — run `/check`, or manually:

   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew assembleDebug
   ```

   Requires `secrets.properties`; mind the stability-analyzer gotcha (see above).

5. **Commit** — Conventional Commits, description in English (matching this repo's history):

   ```
   <type>(<scope>): <imperative description>

   Closes #<n>
   ```

   Scope is optional; suggested scopes (layer / feature): `data`, `domain`, `ui`, `di`, `weather`, `location`, `settings`.

   **Breaking changes:** mark them with `!` right after the type/scope (e.g. `feat!:`, `fix(data)!:`) or a `BREAKING CHANGE:` footer. This is not just style — the release version-bump workflow (see below) parses commit messages to decide major/minor/patch, and the breaking-change marker always wins over the type, on any type (`fix!:` is major, not patch).

6. **Draft PR** against `dev`:

   ```bash
   gh pr create --base dev --draft \
     --title "<type>(<scope>): <description>" \
     --body "## Changes ... ## Tests ... Closes #<n>"
   ```

   When done, clean up the worktree: `git worktree remove ../WeatherApp-<n>-<slug>`.

## Release process: dev → release/x.y.z → main

Releases are cut from the tip of `dev`, never directly from feature branches, and never pushed straight to `main` (protected).

1. **Cut the release branch** from an updated `dev`:

   ```bash
   git fetch origin
   git checkout -b release/x.y.z origin/dev
   git push -u origin release/x.y.z
   ```

2. **Version bump is automatic.** Pushing a `release/**` branch triggers `.github/workflows/release-version-bump.yml`, which walks the Conventional Commits between `origin/main` and the branch tip, picks the bump level (major if any commit has a breaking-change marker, else minor if any `feat:`, else patch), updates `versionCode`/`versionName` in `app/build.gradle.kts`, and pushes a `chore(release): bump version to x.y.z` commit onto the release branch. No manual editing of `versionCode`/`versionName` needed.

3. **Open the PR against `main`** once the bump commit lands:

   ```bash
   gh pr create --base main --title "chore(release): x.y.z" --body "..."
   ```

4. **Merging to `main` triggers the real deploy.** `gradle-test.yml` and `security.yml` run on the merge push, and `playstore-deploy.yml` fires automatically via `workflow_run` once `gradle-test` succeeds on `main` — building/signing the AAB & APK, uploading to Play Store (`internal` track by default), and creating a GitHub Release. This is why routine changes (including Dependabot bumps) must never land on `main` directly — see the Dependabot `target-branch: dev` setting in `.github/dependabot.yml`.
