---
name: check
description: Build and test WeatherApp the way CI does — runs unit tests and a debug build, with this project's known gotchas handled. Use to verify a change compiles and passes before pushing.
---

Run the project's verification the way CI does, then report results plainly.

## Steps

1. **Preflight — secrets.** Confirm `secrets.properties` exists at the repo root. If it is missing, stop and tell the user: the build will fail without it. They must copy `local.defaults.properties` to `secrets.properties` and fill in `API_KEY` (OpenWeatherMap) and `PLACES_API_KEY` (Google Places).

2. **Unit tests** (this is exactly what CI runs):
   ```
   ./gradlew testDebugUnitTest
   ```
   For a single test, narrow with `--tests "com.gago.weatherapp.SomeTest"`.

3. **Debug build:**
   ```
   ./gradlew assembleDebug
   ```
   If the stability-analyzer tasks (`debugStabilityCheck`) are the only thing failing and you just need to confirm compilation, rerun with `-x debugStabilityCheck -x releaseStabilityCheck`, and surface the stability warnings to the user rather than silently skipping them.

4. **Report:** state pass/fail for each step with the relevant output. If tests failed, quote the failing test names and assertion messages. Do not claim success unless both steps actually passed.

## Notes

- Do not touch the `resolutionStrategy.force` CVE pins in the root `build.gradle.kts`.
- Instrumented tests (`connectedAndroidTest`) need a device/emulator and are out of scope here unless the user asks.
- This is additive to the bundled `/verify` skill; use `/verify` when you need to actually run the app and observe behavior.
