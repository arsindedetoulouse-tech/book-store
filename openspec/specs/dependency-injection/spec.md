# dependency-injection Specification

## Purpose

Defines how dependency-injection declarations are distributed across modules and brought together, so that adding a feature requires editing exactly one place in the application module.

## Requirements

### Requirement: Modules declare their own dependencies

A module that provides or consumes injected dependencies SHALL declare them in its own injection module. A module SHALL NOT reference the injection declarations of an unrelated module.

#### Scenario: Feature provides a screen's dependencies

- **WHEN** a feature module introduces a screen that requires injected collaborators
- **THEN** the feature declares those dependencies in its own injection module
- **AND** the feature's injection module does not contain declarations belonging to another feature

#### Scenario: Shared client configuration

- **WHEN** a module provides a shared resource such as an HTTP client
- **THEN** its injection module declares that resource
- **AND** consuming modules obtain it through injection rather than constructing it themselves

### Requirement: Application module is the single composition root

All module injection declarations SHALL be registered in exactly one place in the application module. That place SHALL be the only location where the composition root is assembled.

#### Scenario: Adding a feature module

- **WHEN** a new feature module with its own injection module is added to the project
- **THEN** exactly one location in the application module must be updated to register it
- **AND** no other module requires modification

#### Scenario: Injecting into a feature's screen

- **WHEN** a feature screen's view model is created
- **THEN** its declared dependencies are resolved from the application-wide injection graph
- **AND** the screen does not construct its own dependencies

#### Scenario: Injecting into a shared composable

- **WHEN** a composable in the design system requires an injected dependency
- **THEN** it resolves it through the injection API available in composition
- **AND** it does not require the composable to receive the dependency as a parameter

### Requirement: Injectable view models are lifecycle-scoped

A screen's view model SHALL be obtainable through the injection integration for view models, so that its lifecycle is tied to the composition rather than to manual instantiation.

#### Scenario: View model obtained in composition

- **WHEN** a screen requests its view model during composition
- **THEN** the view model is resolved from the injection graph
- **AND** the same instance is returned for the same owner within the same lifecycle

#### Scenario: Configuration change

- **WHEN** the screen's configuration changes and the composition is recreated
- **THEN** a new view model instance is obtained rather than a stale instance being retained

### Requirement: Injection behaviour is verifiable in tests

A module's injection declarations SHALL be verifiable in isolation, so that a test can assert which dependencies a module provides and resolve them without starting the full application.

#### Scenario: Verifying a module's declarations

- **WHEN** a test loads a single module's injection declarations in isolation
- **THEN** every declared dependency resolves successfully
- **AND** the test does not require the application module or any other feature to be loaded

#### Scenario: Starting the full graph

- **WHEN** all module declarations are loaded together as they are in the application
- **THEN** the graph starts without unresolved or duplicate declaration errors