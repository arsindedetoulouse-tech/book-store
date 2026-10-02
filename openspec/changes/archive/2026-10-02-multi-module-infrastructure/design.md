# Design

## Context

See `proposal.md` for motivation. The constraints that shaped this design:

**The toolchain is already past its limits.** `gradle/wrapper/gradle-wrapper.properties` pins Gradle 9.3.1, which is exactly the minimum AGP 9.1.1 requires. `gradle/libs.versions.toml` pins `kotlin = "2.0.21"`, used only for the `kotlin.plugin.compose` alias, because AGP 9 removed `org.jetbrains.kotlin.android` in favour of built-in Kotlin. AGP 9.1.1 requires KGP >= 2.2.10. The Compose compiler plugin is therefore version-mismatched against the compiler that actually runs.

**The `compileSdk` DSL is new.** `app/build.gradle.kts` uses `compileSdk { version = release(36) { minorApiLevel = 1 } }`, the AGP 9 form. Moving to 37 changes `minorApiLevel` semantics, not just the number.

**`AGENTS.md` is the governing document for future sessions.** Whatever is written here has to be reflected back into it, or the next agent will follow rules that describe an API the project does not use.

## Goals / Non-Goals

**Goals:**

- One place where SDK levels, toolchain, and plugin configuration are declared.
- One place where dependency versions are declared.
- A build that fails loudly on a layer violation, not on a code review comment.
- A document that matches the code it governs.

**Non-Goals:**

- A data layer between `:feature:` and `:core:network`. Revisit if multiple data sources per domain appear.
- CI configuration.
- Any feature behaviour beyond a skeleton.

## Decisions

### Convention plugins in an included `build-logic` build

`AGENTS.md` Role D says to keep centralized build logic in convention plugins "if present", leaving the choice open. With five-plus modules, duplicating `compileSdk`, `minSdk`, toolchain, `buildFeatures.compose`, and namespace scheme per module is exactly what the same rule forbids elsewhere.

Chosen: `build-logic` as an included build, referenced from `settings.gradle.kts` via `includeBuild`.

Rejected: flat per-module scripts. Cheaper now, but every module becomes a rewrite once the first convention plugin appears.

Rejected: `buildSrc`. Single-version constraint and weaker isolation than an included build.

The included build must share the root version catalog; `build-logic/settings.gradle.kts` declares `versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }` so versions are never duplicated.

### Navigation3, and `AGENTS.md` rewritten to match

`AGENTS.md` Section 1 mandates "Compose Navigation (Type-Safe via `@Serializable` destination objects)" and Role B point 2 mandates extensions on `NavGraphBuilder`. Neither exists in Navigation3 1.2.0.

The real Navigation3 API, read from the artifact bytecode:

```
androidx.navigation3.runtime:
  interface NavKey                     marker interface, no members
  class NavEntry<K>
  class EntryProviderScope<T>
      entry(key, metadata) { content } equivalent of composable<T>()
      addEntryProvider(kClass) { ... } lazy registration by type
  fun rememberNavBackStack(vararg NavKey): NavBackStack<NavKey>
  deeplink: DeepLinkDecoder, DeepLinkMatcher, DeepLinkSerializer

androidx.navigation3.ui:
  NavDisplay(backStack) { onBack }
  AnimatedSceneKey, Scene, SinglePaneScene, DialogScene, SceneStrategy
```

`NavKey` is an empty marker interface. Keys are typed objects, not strings, so the "never hardcode routes" rule is satisfied more directly than in Navigation2. `@Serializable` is not part of key identity; serialization is handled separately by `rememberNavBackStack(SavedStateConfiguration, ...)` and `DeepLinkDecoder` for deep links.

Chosen: Navigation3 1.2.0, plus an `AGENTS.md` edit recording `NavKey` / `EntryProviderScope` in place of `@Serializable` / `NavGraphBuilder`.

Rejected: Navigation 2.9.8. It matches the current document text and needs only `minCompileSdk=35`, but Nav3 was chosen deliberately, and its `minCompileSdk=37` matches the rest of the stack exactly.

Cost of the choice: `AGENTS.md` must change. That edit is in scope of this change precisely because the divergence is otherwise invisible until the first feature module is written against the wrong API.

### `:core:model` is a pure Kotlin module

Chosen: `org.jetbrains.kotlin.jvm`, no Android plugin, no dependencies. The module holds domain types that OrbitMVI uses as state and that `:core:network` maps onto, and nothing in that set should need the Android platform.

This costs one thing: AGP 9 constrains how non-Android modules compose with Android ones. The compatibility has to be confirmed during implementation rather than assumed, and if `kotlin.jvm` cannot be consumed from an Android module under AGP 9.1.1, the fallback is `com.android.library`.

Rejected: `com.android.library`. Guaranteed to compose with the other modules, but it attaches the Android plugin to the one module whose contents are supposed to be platform-independent, and would force a namespace that the module has no use for.

### Ktor behind a typed facade in `:core:network`

A `:core:network` module that exposes a raw `HttpClient` leaks Ktor into every feature. Each feature would then own its own serialization, error mapping, and timeouts, and OrbitMVI tests would have no seam to substitute.

Chosen: `:core:network` owns `@Serializable` DTOs, Ktor configuration, and DTO-to-domain mapping, and exposes typed interfaces (e.g. `BookApi`) returning domain types from `:core:model`.

Rejected: a separate `:core:data` repository layer. Worth it when a domain has several sources (network + Room + cache); for a single source it doubles module count for no gain.

Engine: `ktor-client-okhttp` 3.5.2. `ktor-client-cio` and `ktor-client-apache5` 3.4.1 are also present in the Gradle cache from earlier experimentation.

### DTOs in `:core:network`, domain in `:core:model`

Chosen: `:core:model` holds plain domain types with no annotations; `:core:network` holds `@Serializable` DTOs and mappers.

Rejected: DTOs alongside domain types in `:core:model`. That would put `@Serializable` on classes that OrbitMVI uses as `State`, fixing their shape to the wire format for no benefit. It would also force the serialization plugin onto `:core:model`, the one module intended to have no dependencies.

### JDK toolchain 21, declared explicitly

`JAVA_HOME` is unset, the system default is JDK 25, and AGP 9.1.1 requires JDK 17 or newer. Temurin 21.0.7 is already provisioned at `~/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2`. Declaring `jvmToolchain(21)` in a convention plugin makes the choice explicit and reproducible, and removes the mismatch against `.idea/compiler.xml`, which already sets `bytecodeTargetLevel` 21.

`app/build.gradle.kts` currently sets `JavaVersion.VERSION_11`; the convention plugin replaces this with the toolchain.

### `verifyModuleLayers` for the no-cross-feature rule

Two distinct problems, often conflated:

```
Circular project dependency        Layer violation
:feature:a <-> :feature:b          :feature:booklist -> :feature:cart
                                   (not a cycle, so no failure)
Gradle rejects this already       AGENTS.md forbids it, nothing checks
free, at configuration time
```

So a cycle detector would add nothing. What is missing is the layer rule.

Chosen: a `verifyModuleLayers` task registered once in the root project and wired into `check`, implemented in `build-logic`. It reads project dependencies after evaluation and fails with a message naming both modules and the violated rule.

Rejected: per-module allowlists. Each module would declare its own permitted dependency list and the check would compare reality against that declaration. More flexible, since it could later forbid `:feature:X` from reaching `:core:network` directly, but it introduces a second declaration that must be maintained beside every dependency change. Deferred until a concrete need appears.

Rejected: an external plugin or bytecode-level rule analysis. None of the candidates are in the Gradle cache, each would add a foreign dependency to infrastructure that is still being established, and bytecode analysis only runs after compilation, so a layer error surfaces late and expensively.

Rejected: no enforcement. With seven or more modules, a boundary violation would present as a slow build rather than a clear error.

Implementation constraint: the check must not treat the dependency list as a task input, and must not read `project.dependencies` during configuration of another project, or it will break configuration cache and isolated projects. Reading during evaluation and failing directly is safe.

Granularity: the coarse rule only. `:feature:X` may depend on any `:core:` module; `:feature:X` may not depend on another `:feature:`; `:core:X` depends on `:core:model` or nothing. Per-module allowlists were considered and rejected as premature: with seven modules the coarse rule already covers what `AGENTS.md` forbids, and an allowlist per module adds a declaration that must be maintained alongside every new dependency. Tighten when the need is concrete.

### detekt 2.0.0-alpha.6

The JetBrains AGP 9 plugin compatibility registry states that `io.gitlab.arturbosch.detekt` below 2.0.0 requires `android.newDsl=false` **and** `android.builtInKotlin=false`. The second flag disables AGP 9's built-in Kotlin entirely, which means re-introducing the `org.jetbrains.kotlin.android` plugin that AGP 9 removed, and abandoning the new `compileSdk` DSL.

Chosen: `detekt 2.0.0-alpha.6`, the only detekt line compatible with AGP 9.

Cost: an alpha release with a reorganized configuration model. Rule sets moved from `detekt-rules-<name>` to a new layout and are enabled through type-safe rulesets. Android Lint remains available (`app:lintDebug`) and is not affected, so it can cover the gap in the short term.

Rejected: detekt 1.23.8, stable but incompatible with AGP 9 as configured.

### `targetSdk` 37

`compileSdk` 37 is forced by Navigation3 and Compose BOM 2026.09. `targetSdk` governs runtime behaviour, not compilation. Keeping it at 36 would leave an unexplained gap, since compile-time platform behaviour is already sourced from 37.

## Risks / Trade-offs

[Platform `android-37` is not installed] → Build Tools 37.0.0 is present, the SDK license is accepted, and the network is reachable, so AGP should provision the platform. `cmdline-tools` is absent, so `sdkmanager` is not available if that fails. Verify early in the migration; if provisioning fails, the fallback is installing `cmdline-tools`.

[detekt 2.0.0-alpha.6 is an alpha] → Rule configuration differs from 1.x. Keep the initial ruleset small, and lean on Android Lint meanwhile.

[Replacing `JavaVersion.VERSION_11` with a toolchain changes bytecode level] → Source sets compile to Java 21 instead of 11. Nothing in the project depends on 11, but `lint` and any bytecode-inspecting tooling should be re-run after the change.

[`verifyModuleLayers` risks configuration cache regression] → Read dependencies during evaluation, never as task inputs, and never across projects. Confirm `./gradlew check --configuration-cache` still succeeds.

[Navigation3 `NavKey` keys have no serialization identity] → Deep links and process-death restore go through `DeepLinkDecoder` / `rememberNavBackStack(SavedStateConfiguration, ...)`, not `@Serializable`. This must be documented in `AGENTS.md`, or keys will be written assuming a contract that does not exist.

[Layer rule permits `:feature:X -> :core:Y` for any `:core:` module] → A feature could reach `:core:network` when it should go through a nearer abstraction. The rule encodes what `AGENTS.md` actually forbids; tightening it per-module is a later refinement.

[`:app` becomes the only composition root] → Every new feature module must register its Koin module in `:app`, or it fails at runtime, not at build time. The compile-time check covers layers, not DI registration.

[`kotlin.jvm` consumption from an Android module under AGP 9] → AGP 9 removed `org.jetbrains.kotlin.android` and changed how Kotlin is applied, so a plain Kotlin module's consumability from an Android module is not guaranteed. Confirm early; the fallback is `com.android.library` for `:core:model`.

## Migration Plan

Order matters: the build is currently broken, so nothing else can be verified until it compiles.

1. Fix `compileSdk`/`targetSdk` to 37, Kotlin to 2.2.10, BOM to 2026.09.00, toolchain 21. Confirm `./gradlew assembleDebug` succeeds on the existing single `:app` module before touching structure.
2. Add `build-logic` with the convention plugins, sharing the root catalog.
3. Convert `:app` to the application convention plugin.
4. Create `:core:model` (pure Kotlin), `:core:designsystem`, `:core:navigation`, `:core:network` as empty modules; confirm `:core:model` resolves as a dependency of an Android module, falling back to `com.android.library` if not; move shared code.
5. Create `:feature:booklist` as a skeleton proving OrbitMVI plus Koin plus Navigation3 wiring end to end.
6. Add `verifyModuleLayers`, wire into `check`.
7. Configure detekt.
8. Delete `Greeting` / `GreetingPreview`.
9. Rewrite `AGENTS.md` against the resulting code.

Rollback: steps 2 through 8 revert by removing the modules and restoring `app/build.gradle.kts`. Step 1 is the only one with no clean rollback, since raising the SDK level and Kotlin version is one-way in practice.

## Open Questions

None.