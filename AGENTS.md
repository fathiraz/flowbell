# AGENTS.md

## Cursor Cloud specific instructions

### Project overview

FlowBell is a single-module Android app (Kotlin 2.1.0, Jetpack Compose, Material 3) that bridges device notifications to user-configured webhooks. No backend services or external databases are required — all storage is on-device via Room/SQLite.

### Environment

- **JDK 21** is required (`jvmTarget = "21"` in build config).
- **Android SDK** must be installed at `$ANDROID_HOME` with platform `android-35`, `build-tools;35.0.0`, and `platform-tools`.
- The environment variables `ANDROID_HOME` and `PATH` (including cmdline-tools and platform-tools) are persisted in `~/.bashrc`.

### Build & test commands

| Task | Command |
|---|---|
| Debug build | `./gradlew assembleDebug` |
| Release build | `./gradlew assembleRelease` (needs keystore) |
| Unit tests | `./gradlew testDebugUnitTest` |
| Lint | `./gradlew lintDebug` |
| Dependency check | `./gradlew dependencyUpdates` |

### Gotchas

- Both `settings.gradle` (Groovy) and `settings.gradle.kts` (Kotlin DSL) exist at the root. Gradle uses the Groovy file which only includes `:app`. The `.kts` file references non-existent `:core:*` modules — ignore it.
- Similarly, both `build.gradle` and `build.gradle.kts` exist at the root; the Groovy version takes precedence.
- Lint has **pre-existing errors** (3 errors, 155 warnings) relating to `NewApi` checks and other issues. `lintDebug` will fail; this is not caused by environment setup.
- No test source directories exist (`app/src/test/` and `app/src/androidTest/` are absent). `testDebugUnitTest` completes with `NO-SOURCE`.
- This is a pure Android client app — there is no backend server to start, no Docker containers, no database migrations to run.
- The first Gradle invocation downloads the wrapper distribution (~200 MB) and all dependencies. Use `--no-daemon` in CI/cloud to avoid zombie daemons.
- An Android emulator or physical device is required for instrumented tests (`connectedAndroidTest`) and running the APK — neither is available in a headless cloud VM.
