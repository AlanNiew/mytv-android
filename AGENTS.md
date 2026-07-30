# AGENTS.md

Workspace instructions for ZCode agents working on **mytv-android** (我的电视) — a native Android
IPTV/video player app. Primary docs in repo: `README.md`, `CLAUDE.md`, `PROJECT_GUIDE.md`.
Read those for product/UX detail; this file captures build/tooling facts and conventions that are
easy to get wrong.

## What this repo is

Single-module Gradle Android app (Kotlin + Jetpack Compose) for Android 5.0+ (minSdk 21,
compile/target 34). Supports Android TV (Leanback/D-Pad), mobile, and tablet form factors from one
codebase. Built-in HTTP server on port `10481` serves a web UI for remote settings.

- Package root: `top.yogiczy.mytv` under `app/src/main/java/`
- Namespace / applicationId: `top.yogiczy.mytv`

## Modules — read this before assuming multi-module

`settings.gradle.kts` includes **only `:app`**. The sibling directories `core/`, `mobile/`, `tv/`,
`allinone/`, and `ijkplayer-java/` contain **only `build/` output** — they are NOT Gradle modules
and have no source. Do not add them to `settings.gradle.kts` or try to import from them. All source
lives under `app/`.

## Build & tooling commands (verified)

> CI (`.github/workflows/check.yml`) runs `testDebugUnitTest` → `lintDebug` → `assembleDebug`
> on Zulu JDK 17, with a Gradle cache. There is **no ktlint plugin and no `ktlintCheck` task** —
> use `.editorconfig` as the style source of truth (manual enforcement; lint handles the rest).
> Unit tests run on JVM (no device needed) — pure parsers are safe to test under `src/test/`.

```bash
./gradlew assembleDebug        # debug APK (what CI runs)
./gradlew assembleRelease      # release APK (needs signing config)
./gradlew bundleRelease        # AAB
./gradlew installDebug         # install on connected device/emulator
./gradlew clean
```

- JDK **17** required (CI uses Zulu 17). Gradle wrapper is provided (`./gradlew` / `gradlew.bat`).
- Version catalog: `gradle/libs.versions.toml`. AGP 8.10.1, Kotlin 2.0.0, Compose Compiler plugin.
- NDK ABIs: `armeabi-v7a`, `arm64-v8a`, `x86_64`.
- JVM target: `1.8` (set in `app/build.gradle.kts` `compileOptions`/`kotlinOptions`).

### Release signing

Release builds read signing from env vars first, then `key.properties` at repo root, falling back to
`keystore.jks`. The `release.yml` workflow expects GitHub secrets `KEYSTORE_BASE64`,
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, `GIT_TOKEN`. Do not commit real keystore material.

## Entry points & navigation

- `MyTVApplication.kt` — `Application` subclass.
- `activities/MainActivity.kt` — inspects device type and routes to one of:
  - `LeanbackActivity.kt` — Android TV (landscape, D-Pad/remote)
  - `MobileActivity.kt` — phone (portrait, touch)
  - `PadActivity.kt` — tablet (landscape, touch)
- `BootReceiver.kt` — launch-on-boot.

## Architecture (MVVM)

- **data/entities** — domain models (Iptv, Epg, …).
- **data/repositories** — data access. Subpackages: `iptv/` (+ `iptv/parser`), `epg/` (+ `epg/fetcher`), `git/` (+ `git/parser`).
- **data/utils/Constants.kt** — default source URLs and app constants.
- **ui/screens/leanback/** — TV UI: `main/`, `panel/`, `classicpanel/`, `quickpanel/`, `settings/`, `video/`, `monitor/`, `toast/`, `update/`. Each has a `components/` subpackage.
- **ui/theme/** — `LeanbackTheme`, `MobileTheme`, `PadTheme`.
- **ui/utils/SP.kt** — `SharedPreferences` wrapper for user config.
- **ui/utils/HttpServer.kt** — embedded web server (`SERVER_PORT = 10481`) for remote settings.
- Data flow: Repository → ViewModel (`StateFlow`) → Compose `collectAsState()`.

### Key conventions & rules

- **HTTP client is centralized.** Always obtain `OkHttpClient` from
  `data/OkHttpClientProvider.kt` (`OkHttpClientProvider.client`). Do not construct ad-hoc
  `OkHttpClient` instances in repositories/fetchers. (Established in commit ca0cfcc.)
- **Serialization**: use `kotlinx.serialization` (`@Serializable`) — the `kotlin.serialization`
  plugin is applied. Do not introduce Gson/Moshi without reason.
- **State**: expose via `StateFlow`/`Flow`; collect in Compose with `collectAsState()`/`collectAsStateWithLifecycle()`.
- **Dependencies are NOT injected via Hilt** — DI is manual. Match existing manual construction patterns.
- **Player**: Media3 ExoPlayer in `ui/screens/leanback/video/player/Media3VideoPlayer.kt`; supports HLS and RTSP. Multi-line auto-switch + retry on failure is a core behavior — preserve it.
- **TV focus/D-Pad**: all interactive UI must remain navigable with a remote (focusable, OK-key handling). Do not break D-Pad navigation when touching leanback components.
- **Coding style** (`.editorconfig`): 4-space indent, LF line endings, UTF-8, trim trailing
  whitespace, final newline. Markdown is exempt from trailing-whitespace trimming.

## Sensitive areas — read before changing

- `app/build.gradle.kts` — signing config and build types; release minify + resource shrinking are on.
- `app/proguard-rules.pro` — currently near-empty defaults. Adding keep rules here is required when
  introducing reflective/serialized APIs that R8 would otherwise strip.
- `data/repositories/iptv/parser` and `git/parser` — subscription source parsing (M3U / TVBox). A bad
  parser can crash on unknown formats (see CHANGELOG 1.4.0 fix).
- `ui/utils/HttpServer.kt` + `res/raw/index.html` — the remote settings web UI; `index.html` references
  `jsdelivr` CDN (note in README).

## Things that differ from `CLAUDE.md`

- `ktlintCheck` / `ktlintFormat` are **not** registered tasks here. Don't instruct agents to run them;
  the CLAUDE.md "代码检查和测试" section has been corrected to use `lintDebug` + `testDebugUnitTest`.
- Version is controlled by git tags (`v*.*.*`); `versionCode`/`versionName` in `defaultConfig` are
  placeholders (1 / "1.0"). The release workflow derives the version from the tag.
