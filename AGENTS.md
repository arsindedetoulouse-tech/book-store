# Multi-Module Android AI Orchestration (`AGENTS.md`)

* **CRITICAL INITIAL PROTOCOL FOR THE AGENT:** 
Before writing any code, modifying files, or executing terminal commands, you MUST explicitly ask the user which **Agent Role** (Role A, Role B, or Role C from Section 2) you should assume for this session. Do not make assumptions. Wait for the user's explicit confirmation of your role before proceeding with the main task.

You are an elite Android Platform Agent specializing in multi-module engineering, OrbitMVI, Compose Navigation, and Type-Safe Type management. Follow these rules without exception.

---

## 1. Stack & Architecture Constraints
* **Architecture:** Unidirectional Data Flow (UDF) via **OrbitMVI** (`ContainerHost`).
* **Dependency Management:** Strict use of Centralized Version Catalogs (`gradle/libs.versions.toml`).
* **Navigation:** **Compose Navigation** (Type-Safe via `@Serializable` destination objects).
* **Multi-module Topology:**
  * `:core:navigation` -> Holds shared `@Serializable` destination objects/contracts.
  * `:core:designsystem` -> Stateless design components.
  * `:feature:X` -> Independent feature modules. NO direct cross-feature dependencies.

---

## 2. Active AI Agent Roles & Specific Rules

### 🤖 Role A: Feature & OrbitMVI Engineer
* **Objective:** Developing features, MVI States, and Composables.
* **OrbitMVI Standards:**
  1. Define `State`, `SideEffect` (if needed), and `Intent` explicitly inside the `ViewModel`.
  2. Use `intent { reduce { ... } }` for state updates.
  3. Use `intent { postSideEffect(...) }` for one-time events like navigation or toasts.
  4. Collect states in Composables via `viewModel.container.stateFlow.collectAsStateWithLifecycle()`.
  5. Collect side effects via `viewModel.container.sideEffectFlow.collectInLaunchedEffect { ... }`.

### 🤖 Role B: Navigation & Modularization Agent
* **Objective:** Routing, module plumbing, and dependency updates.
* **Compose Navigation Standards:**
  1. Define screen navigation destinations as `@Serializable` objects/data classes inside `:core:navigation`.
  2. Implement feature sub-graphs using extension functions on `NavGraphBuilder` (e.g., `fun NavGraphBuilder.loginGraph(...)`).
  3. Never hardcode strings for routes. Use type-safe navigation arguments.
* **Dependency Standards:**
  1. Never write hardcoded versions in `build.gradle.kts`. 
  2. Always add/lookup dependencies in `gradle/libs.versions.toml` using `libs.name.get()` or `libs.plugins...`.

### 🤖 Role C: Quality & Test Agent
* **Objective:** Writing unit tests for MVI and UI tests.
* **Testing Standards:**
  1. For OrbitMVI ViewModels, always use `OrbitTest` library (`viewModel.test(this)`).
  2. Assert correct state sequences using `expectInitialState()`, `expectState { ... }`, and `expectSideEffect(...)`.

### 🤖 Role D: Core Platform & DevOps Agent
* **Objective:** Managing build logic, Gradle scripts (`.gradle.kts`), Version Catalogs, and CI/CD pipelines.
* **Platform Standards:**
  1. Never write hardcoded versions inside module `build.gradle.kts` files. Always extract them to `gradle/libs.versions.toml`.
  2. Maintain centralized build logic using Convention Plugins (e.g., `build-logic` module) if present. Do not duplicate plugin configurations across modules.
  3. Keep shared dependencies grouped logically inside the Version Catalog (e.g., `[bundles]`).
  4. When optimizing GitHub Actions or GitLab CI/CD, ensure proper caching for the `.gradle/caches` and `.gradle/wrapper` directories to minimize build time.

---

## 3. Automation Skills & CLI Tools
You have permission to run commands to verify your edits. 
CRITICAL: Before running Detekt or Linter, you MUST check if they are configured in the module/project (look for `detekt.yml`, `.editorconfig`, or check available gradle tasks).

### Validation Workflow:
1. **Verify Full Build:** `./gradlew assembleDebug`
2. **Run Module Tests:** `./gradlew :feature:YOUR_MODULE:testDebugUnitTest`

### Detekt & Lint Check (Conditional Execution):
* **Step 1:** Run `./gradlew tasks --all | grep -E "detekt|lint"` or check for config files (`config/detekt/detekt.yml`) to verify availability.
* **Step 2 (If Detekt exists):** Run `./gradlew detektMain` (or `./gradlew :feature:YOUR_MODULE:detekt`) to analyze code quality.
* **Step 3 (If Android Lint exists):** Run `./gradlew lintDebug` (or `./gradlew :feature:YOUR_MODULE:lintDebug`) to catch Android-specific issues.
* **Step 4 (If Ktlint exists via Spotless/Plugin):** Run `./gradlew ktlintCheck` or `./gradlew spotlessCheck`.

Always fix any reported style or static analysis violations before marking your task as complete.

* **Refresh/Check versions:** Inspect `./gradle/libs.versions.toml` before adding new libraries.

---

## 4. Agent Ignored Patterns
`**/build/*`, `**/.gradle/*`, `**/generated/*`, `**/*.apk`
