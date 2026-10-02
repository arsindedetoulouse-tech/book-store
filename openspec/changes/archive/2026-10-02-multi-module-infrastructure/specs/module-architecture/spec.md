# Spec Delta

## Purpose

Defines how code is partitioned into modules and which dependencies between those modules are permitted, so that architectural boundaries hold as the project grows.

## ADDED Requirements

### Requirement: Layered module topology

The project SHALL organize code into an application module, feature modules, and core modules. Core modules SHALL be further distinguished by responsibility: domain model, design system, navigation contracts, and network access.

#### Scenario: A feature needs to render UI

- **WHEN** a feature module requires composables, colours, or typography
- **THEN** it depends on the design system module
- **AND** it does not re-declare shared visual components locally

#### Scenario: A feature needs domain data

- **WHEN** a feature module requires a domain type
- **THEN** the type is obtained from the domain model module
- **AND** the domain model module depends on no other project module

#### Scenario: A feature needs to perform I/O

- **WHEN** a feature module requires network data
- **THEN** it depends on the network module and consumes typed interfaces
- **AND** it does not construct an HTTP client or define its own wire format types

### Requirement: No direct feature-to-feature dependencies

A feature module SHALL NOT declare a dependency on another feature module. Navigation between features SHALL be expressed through the navigation module so that neither feature references the other directly.

#### Scenario: Declaring a forbidden dependency

- **WHEN** a contributor declares a dependency from one feature module to another feature module
- **THEN** verification fails and the build is rejected
- **AND** the failure names both modules and the violated rule

#### Scenario: Navigating between two features

- **WHEN** a user action in one feature must lead to a screen in another feature
- **THEN** the destination is declared in the navigation module
- **AND** each feature contributes its own entries without referencing the other's module

#### Scenario: Allowed dependency direction

- **WHEN** a feature module declares a dependency on a core module
- **THEN** verification succeeds

#### Scenario: Core module declaring a project dependency

- **WHEN** a core module other than the network module declares a dependency on another project module
- **THEN** verification fails, since core modules are intended to be independent
- **AND** the network module's dependency on the domain model module is the sole permitted exception

### Requirement: Circular module dependencies are rejected

The build SHALL reject any configuration that introduces a circular dependency between modules, at the earliest point such a configuration can be detected.

#### Scenario: Introducing a dependency cycle

- **WHEN** a set of module dependencies forms a cycle
- **THEN** the build fails during configuration
- **AND** the cycle is reported rather than resolved silently

### Requirement: Boundary verification runs as part of normal validation

Boundary verification SHALL be wired into the project's standard verification lifecycle so that boundary violations are detected by an ordinary build, not only by deliberate inspection.

#### Scenario: Running standard verification

- **WHEN** a developer runs the project's verification task
- **THEN** module boundary rules are checked
- **AND** a violation fails the build

#### Scenario: Violation is introduced after a passing build

- **WHEN** a forbidden dependency is added and verification runs again
- **THEN** the previously passing build now fails
- **AND** the failure identifies the offending dependency

### Requirement: Build configuration supports caching

The verification tasks introduced by this capability SHALL NOT degrade the build's ability to cache configuration, so that the additional checks do not slow down repeated builds.

#### Scenario: Verification under configuration caching

- **WHEN** verification runs with configuration caching enabled
- **THEN** the build succeeds
- **AND** no error is raised indicating that configuration caching is incompatible with the project

### Requirement: Shared code leaves the application module

Code used by more than one module SHALL be moved out of the application module into the appropriate core or feature module. The application module SHALL retain only composition responsibilities.

#### Scenario: Locating shared composables and theme

- **WHEN** the application module is inspected after the split
- **THEN** shared visual components and theming are defined in the design system module
- **AND** the application module contains the application entry point, the navigation host, and dependency-injection startup only

#### Scenario: Placeholder content

- **WHEN** the sample greeting screen has been removed
- **THEN** no placeholder or template content remains in the application module's sources