# build-infrastructure Specification

## Purpose

Defines how build configuration is declared so that every module compiles against the same platform, toolchain, and dependency versions without repeating itself.

## Requirements

### Requirement: Single source of dependency versions

The project SHALL declare every third-party dependency and plugin version in the centralized version catalog. Module build scripts SHALL NOT contain literal version strings for external dependencies.

#### Scenario: Adding a dependency version

- **WHEN** a contributor needs a library that is not yet declared
- **THEN** they add a version entry and a library entry to the version catalog
- **AND** reference it from the module build script through the generated catalog accessor
- **AND** no literal version string appears in any module build script

#### Scenario: Changing a shared version

- **WHEN** a shared library version is changed in the catalog
- **THEN** every module referencing that library resolves the new version
- **AND** no per-module edit is required to keep versions consistent

### Requirement: Single source of module build configuration

Shared module build configuration SHALL be declared once and applied to modules by convention, rather than repeated in each module's build script. This configuration includes the compilation SDK level, minimum SDK level, Java/Kotlin language and bytecode level, and whether Compose is enabled.

#### Scenario: Creating a new module

- **WHEN** a contributor creates a new library module
- **THEN** applying the library convention to it yields the same SDK levels, toolchain, and Compose configuration as every existing library module
- **AND** the module's own build script contains only module-specific settings such as namespace and dependencies

#### Scenario: Changing a shared build setting

- **WHEN** a shared setting such as the compilation SDK level changes
- **THEN** the change is made in exactly one place
- **AND** every module picks it up without individual edits

### Requirement: Current Android platform targeting

The application SHALL compile and target against an Android platform level equal to or greater than the minimum required by its dependencies. The build SHALL fail with a clear message when a dependency requires a higher platform level than the project targets.

#### Scenario: Dependency requires a newer platform

- **WHEN** a dependency declares that it requires a higher minimum compilation platform than the project targets
- **THEN** the build fails during dependency metadata validation
- **AND** the failure names the dependency and states the required platform level

#### Scenario: Project targets a sufficient platform

- **WHEN** all declared dependencies are compatible with the project's target platform level
- **THEN** dependency metadata validation succeeds
- **AND** the debug variant assembles without platform-related errors

### Requirement: Pinned Java and Kotlin toolchain

The project SHALL compile against a single, explicitly declared Java language and bytecode level, and SHALL NOT depend on whatever JDK happens to be the machine default. The declared level SHALL be applied uniformly to all modules.

#### Scenario: Machine default JDK differs from the declared toolchain

- **WHEN** a developer builds with a different JDK set as the system default
- **THEN** every module still compiles to the declared bytecode level
- **AND** compilation does not fail because of the default JDK version

#### Scenario: Toolchain JDK is not the system JDK

- **WHEN** the required toolchain is provisioned separately from the system JDK
- **THEN** the build uses the declared toolchain for compilation
- **AND** no manual environment configuration is required from the developer