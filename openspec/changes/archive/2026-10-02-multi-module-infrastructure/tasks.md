# Tasks

## 1. Restore a green build

The project does not currently assemble. Nothing below can be verified until this group passes, so it comes first and touches no structure.

- [x] 1.1 Raise `compileSdk` and `targetSdk` to 37 in `app/build.gradle.kts` using the AGP 9 `release(37)` form, and verify platform `android-37` is provisioned (Build Tools 37.0.0 and the SDK license are already present; `cmdline-tools` is absent) by running `./gradlew :app:assembleDebug`
- [x] 1.2 Raise `kotlin` to 2.2.10 in `gradle/libs.versions.toml` (AGP 9.1.1 requires KGP >= 2.2.10 for its built-in Kotlin) and verify `./gradlew :app:compileDebugKotlin` succeeds
- [x] 1.3 Raise `composeBom` to 2026.09.00 in `gradle/libs.versions.toml` and verify `:app:checkDebugAarMetadata` passes with no remaining platform-level violations
- [x] 1.4 Declare the Java 21 toolchain and replace `JavaVersion.VERSION_11` in `app/build.gradle.kts`, then verify via `./gradlew :app:assembleDebug` that compilation succeeds using the provisioned Temurin 21.0.7 rather than the system JDK 25
- [x] 1.5 Run `./gradlew :app:lintDebug` and fix or baseline any violations introduced by the SDK and dependency bumps, so the pre-existing state is known before refactoring

## 2. Version catalog and build logic

- [x] 2.1 Add version and library entries to `gradle/libs.versions.toml` for OrbitMVI 12.0.1, Navigation3 1.2.0, kotlinx-serialization, Koin 4.2.2 (via its BOM), Ktor 3.5.2 (OkHttp engine, content negotiation, kotlinx JSON, logging, mock), and detekt 2.0.0-alpha.6, plus the `android-library` plugin alias; verify with `./gradlew dependencies` that the catalog resolves
- [x] 2.2 Create `build-logic` as an included build: `build-logic/settings.gradle.kts` declaring `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }`, and reference it from root `settings.gradle.kts` with `includeBuild`; verify `./gradlew help` still succeeds
- [x] 2.3 Add a `detekt` convention to `build-logic` that applies the detekt plugin with the project's shared configuration and exposes per-module `detekt<SourceSet>` tasks; verify the tasks appear via `./gradlew tasks --all | grep detekt`
- [x] 2.4 Add a `kotlin-library` convention for pure Kotlin modules applying `org.jetbrains.kotlin.jvm` with the JDK 21 toolchain and the project code style; verify it applies cleanly to a throwaway module
- [x] 2.5 Add a `android-library` convention applying `com.android.library` with the shared `compileSdk`, `minSdk`, toolchain, namespace derivation, and consumer ProGuard rules; verify it applies cleanly to a throwaway module
- [x] 2.6 Add an `android-compose-library` convention extending the library convention with Compose enabled and the Compose plugin; verify it applies cleanly to a throwaway module
- [x] 2.7 Add an `android-application` convention with the shared configuration plus application-specific `applicationId` and build types; verify `./gradlew :app:assembleDebug` still succeeds after `:app` is converted in task 3.1
- [x] 2.8 Create `config/detekt/detekt.yml` using the 2.0 type-safe ruleset layout (rule sets reorganised relative to 1.x), starting with a small rule set; verify `./gradlew detekt` runs without configuration errors

## 3. Convert the application module

- [x] 3.1 Replace `:app`'s inline `android` block with the application convention, keeping only `applicationId`, `versionCode`, `versionName`, `testInstrumentationRunner`, and release build-type settings; verify `./gradlew :app:assembleDebug` succeeds
- [x] 3.2 Delete `Greeting` and `GreetingPreview` from `MainActivity.kt` and replace the content with a placeholder-free composition root; verify `./gradlew :app:assembleDebug` succeeds and no placeholder text remains in `:app` sources
- [x] 3.3 Move `ui/theme` (`Theme.kt`, `Color.kt`, `Type.kt`) into `:core:designsystem` under its own namespace, updating all references; verify `./gradlew :app:assembleDebug` succeeds

## 4. Core modules

- [x] 4.1 Create `:core:model` as a pure Kotlin module using the `kotlin-library` convention, with no dependencies, and confirm it resolves as a dependency of an Android module under AGP 9.1.1; if it does not, convert it to `com.android.library` and record why
- [x] 4.2 Create `:core:designsystem` using the Compose library convention, depend it on `:core:model` only if needed, and move the relocated theme into it; verify it builds and that `:app` compiles against it
- [x] 4.3 Create `:core:navigation` using the Compose library convention with Navigation3 and kotlinx-serialization, and declare the first typed `NavKey` destination; verify it builds and that the destination type is importable without any string route
- [x] 4.4 Create `:core:network` using the Compose library convention (or the library convention, whichever fits), depending on `:core:model` and Ktor, with the client configured once: OkHttp engine, timeouts, content negotiation, logging
- [x] 4.5 Define wire-format `@Serializable` DTOs and mappers inside `:core:network`, and expose a typed `BookApi` interface returning domain types from `:core:model`; verify the interface exposes no Ktor type and no DTO type
- [x] 4.6 Wrap remote failures in a typed result distinguishing connectivity and data-format failures, so no raw transport or serialization exception escapes the interface; verify with a unit test covering success, connectivity failure, and malformed response
- [x] 4.7 Declare the network module's Koin module for the client and `BookApi`; verify it starts in isolation via a Koin test

## 5. First feature module

- [x] 5.1 Create `:feature:booklist` using the Compose library convention, depending on `:core:designsystem`, `:core:navigation`, `:core:network`, and `:core:model` only
- [x] 5.2 Implement `BookListViewModel` as an Orbit container with `State`, `Intent`, and `SideEffect`, using `intent { reduce { } }` for state and `intent { postSideEffect(...) }` for one-time events, and taking `BookApi` by constructor injection; verify with a unit test using `viewModel.test(this)` asserting the initial state and a reduced state
- [x] 5.3 Implement the feature's screen composable, collecting state via `container.stateFlow.collectAsStateWithLifecycle()` and side effects via `container.sideEffectFlow`, and obtain the view model through the Koin view-model integration; verify it compiles and the view model resolves in composition
- [x] 5.4 Declare the feature's navigation contribution as an extension on `EntryProviderScope<NavKey>` registering the feature's destinations; verify the extension is usable from `:app`
- [x] 5.5 Declare the feature's Koin module registering its view model; verify it starts in isolation via a Koin test

## 6. Compose the application

- [x] 6.1 Add an `Application` subclass that starts Koin with the core modules plus each feature module, the only such registration site in the project; verify `./gradlew :app:assembleDebug` succeeds and the manifest references the subclass
- [x] 6.2 Replace `MainActivity`'s content with a `NavDisplay` over a back stack seeded with the start destination, invoking each feature's navigation contribution exactly once; verify the app assembles and back stack changes recompose

## 7. Layer verification

- [x] 7.1 Implement `verifyModuleLayers` in `build-logic`, reading project dependencies during evaluation (never as task inputs, never across projects) and enforcing the coarse rule: `:feature:X` may depend on `:core:*` only, `:feature:X` must not depend on `:feature:Y`, `:core:X` depends on `:core:model` or nothing; verify by temporarily adding a `:feature:booklist` -> `:feature:other` dependency and confirming the task fails naming both modules
- [x] 7.2 Wire `verifyModuleLayers` into `check` and verify `./gradlew check` fails on a reintroduced violation and passes once removed
- [x] 7.3 Run `./gradlew check --configuration-cache` and verify it succeeds, confirming the new task did not break configuration caching

## 8. Static analysis

- [x] 8.1 Apply the detekt convention to every module and run `./gradlew detekt`; verify all modules are analysed
- [x] 8.2 Fix or explicitly baseline all detekt findings across the codebase; verify `./gradlew detekt` reports no unbaselined violations

## 9. Correct AGENTS.md

- [x] 9.1 Update the initial protocol to list Role A, Role B, Role C, and Role D, matching the four roles defined in Section 2
- [x] 9.2 Replace the Section 1 navigation statement with Navigation3: typed `NavKey` destinations, deep links and restore via the serialization facilities, no string routes; verify no `@Serializable` destination or `NavGraphBuilder` guidance remains
- [x] 9.3 Rewrite Role B navigation standards to specify `NavKey` in `:core:navigation` and `EntryProviderScope` extensions per feature; verify the documented API matches the API used in `:feature:booklist`
- [x] 9.4 Add Koin and Ktor to the Section 1 stack description, and add `:core:network`, `:core:model`, `:core:designsystem`, and `build-logic` to the module topology; verify every listed module exists in `settings.gradle.kts`
- [x] 9.5 Document `verifyModuleLayers` in the validation workflow of Section 3 alongside detekt and lint, with the command to run it; verify the documented command runs as written

## 10. Final verification

- [x] 10.1 Run `./gradlew clean assembleDebug detekt check --configuration-cache` from a clean state and verify the whole pipeline passes
- [x] 10.2 Run `./gradlew :app:testDebugUnitTest :feature:booklist:testDebugUnitTest` and verify all unit tests pass
- [x] 10.3 Review every requirement in the five delta specs against the implemented code and confirm each scenario is satisfied or explicitly deferred