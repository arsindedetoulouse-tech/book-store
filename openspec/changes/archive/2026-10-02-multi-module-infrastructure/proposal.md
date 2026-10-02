# Proposal

## Why

The project cannot build. `./gradlew assembleDebug` fails in `:app:checkDebugAarMetadata` because `androidx.core:core-ktx:1.19.1` and `androidx.lifecycle:lifecycle-runtime-compose:2.11.0` require `compileSdk` 37 while the module compiles against `android-36.1`.

Underneath that, the build is a single `:app` module, while `AGENTS.md` mandates a multi-module topology with OrbitMVI, type-safe Compose Navigation, and a centralized version catalog. None of that exists yet, and no mechanism enforces the architectural rules the document states. This change establishes the infrastructure that the rest of `AGENTS.md` can then be written against.

## What Changes

**Fix the broken build**

- Raise `compileSdk` and `targetSdk` to 37 (Nav3 and Compose BOM 2026.09 both require `minCompileSdk=37`).
- Raise Kotlin from 2.0.21 to 2.2.10, the minimum AGP 9.1.1 supports for its built-in Kotlin.
- Pin the JDK toolchain to 21 (Temurin 21.0.7 is already provisioned under `~/.gradle/jdks`); the system default is JDK 25.
- Upgrade the Compose BOM from 2024.09.00 to 2026.09.00, closing a two-year gap with the already-current `lifecycle` 2.11.0 and `activity-compose` 1.13.0.

**Establish the multi-module topology**

- Add `:core:model`, `:core:designsystem`, `:core:navigation`, `:core:network`, and `:feature:*` modules.
- Move shared state out of `:app`; delete the `Greeting` composable and its `@Preview`.

**Add build infrastructure**

- Introduce a `build-logic` included build with convention plugins, so `compileSdk`, `minSdk`, toolchain, and Compose configuration are declared once instead of duplicated per module.
- Extend `gradle/libs.versions.toml` with OrbitMVI, Navigation3, kotlinx-serialization, Koin, Ktor, and detekt. No module build script will hardcode a version.

**Enforce the documented architecture**

- Add a `verifyModuleLayers` task that fails the build when a `:feature:` module depends on another `:feature:` module. Gradle already rejects circular project dependencies on its own; this task covers the layer rule, which is not a cycle and is currently unenforced.

**Correct `AGENTS.md` to match the chosen stack**

- `AGENTS.md` describes `NavGraphBuilder` extensions and `@Serializable` destinations. Navigation3 has neither: its API is `NavKey` plus `EntryProviderScope`. Leaving the document unchanged would create a rule no agent could follow.
- Add the Role D (Core Platform & DevOps) entry to the initial protocol, which currently lists only Roles A, B, and C while Section 2 defines four roles.
- Document the Koin and Ktor layers, which the document does not mention.

## Capabilities

### New Capabilities

- `build-infrastructure`: toolchain, SDK levels, version catalog, and convention plugins as the single source of build configuration.
- `module-architecture`: module topology and the layering rules between `:app`, `:feature:`, and `:core:`, including automated enforcement of the no-cross-feature rule.
- `navigation`: type-safe Navigation3 destinations held in `:core:navigation` and feature entry graphs.
- `dependency-injection`: per-module Koin modules aggregated at a single composition root in `:app`.
- `network-access`: a typed network facade in `:core:network` over a Ktor client, keeping wire-format concerns out of feature modules.

### Modified Capabilities

None. `openspec/specs/` is empty, so no existing capability's requirements change.

## Impact

**Files changed**

- `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `gradle.properties`
- `settings.gradle.kts`, root `build.gradle.kts`, `app/build.gradle.kts`
- `AGENTS.md` (in scope of this change, as a stated requirement rather than a side effect)

**Files added**

- `build-logic/` included build with convention plugins
- `core/model`, `core/designsystem`, `core/navigation`, `core/network`
- `feature/booklist` as the first feature module

**Files removed**

- `app/src/main/java/com/arsinde/oldboobstore/MainActivity.kt` `Greeting` and `GreetingPreview`

**Dependencies added**

OrbitMVI 12.0.1, Navigation3 1.2.0, Koin 4.2.2, Ktor 3.5.2 (OkHttp engine), kotlinx-serialization, detekt 2.0.0-alpha.6

**Environment**

`compileSdk = 37` requires platform `android-37`, which is not currently installed. Build Tools 37.0.0 is present and the SDK license is accepted, so AGP is expected to provision the platform automatically; `cmdline-tools` is absent, so `sdkmanager` is unavailable as a fallback. JDK 21 is already provisioned by Gradle.

**Not in scope**

CI workflow, feature implementations beyond a skeleton `:feature:booklist`, and any data layer between `:feature:` and `:core:network`.