# Spec Delta

## Purpose

Defines how screens are addressed and how feature navigation graphs are contributed, so that no route is ever identified by a hardcoded string.

## ADDED Requirements

### Requirement: Destinations are declared in the navigation module

Every screen destination SHALL be declared as a typed value in the navigation module. Route identity SHALL be carried by the type of the destination value, and SHALL NOT be encoded as a string, URI pattern, or serialized route identifier.

#### Scenario: Declaring a screen destination

- **WHEN** a contributor adds a new screen
- **THEN** they declare a typed destination value in the navigation module that identifies the screen and carries any arguments it requires
- **AND** no string literal is used to identify that screen anywhere in the project

#### Scenario: Destination carries arguments

- **WHEN** a screen requires an argument such as an item identifier
- **THEN** the argument is declared as a typed property of the destination value
- **AND** reading or writing that argument requires no string parsing

#### Scenario: Two screens with the same simple name

- **WHEN** two distinct screens exist in different features
- **THEN** their destinations are distinguishable by type alone
- **AND** no naming collision forces a string-based disambiguator

### Requirement: Features contribute their own navigation entries

A feature module SHALL contribute its own screens to the navigation graph through an extension declared in that feature module. The application module SHALL compose every feature's contribution into a single navigation graph.

#### Scenario: Adding a feature to the app

- **WHEN** a new feature module with screens is added
- **THEN** its contribution is invoked once from the application module's navigation host
- **AND** no other feature module is modified

#### Scenario: Feature graph declaration

- **WHEN** a contributor writes a feature's navigation contribution
- **THEN** it is declared as an extension on the navigation entry provider scope
- **AND** it registers that feature's destinations with their screen content

### Requirement: Navigation survives process death and deep links

Destination values SHALL remain restorable across process death, and SHALL be encodable for deep-link handling, through the navigation library's serialization facilities.

#### Scenario: Process death while on a screen

- **WHEN** the process is terminated while a screen is displayed and later restored
- **THEN** the same screen is restored with the same arguments
- **AND** restoration does not depend on a string route

#### Scenario: Deep link to a screen

- **WHEN** an external deep link targets a declared screen
- **THEN** the destination is matched and resolved to its typed destination value
- **AND** the screen is displayed with the arguments carried by the link

### Requirement: Navigation state is observable

The navigation state SHALL be observable as Compose state, so that a change to the back stack triggers recomposition without manual invalidation.

#### Scenario: Back stack changes

- **WHEN** a destination is pushed onto or removed from the back stack
- **THEN** the navigation host recomposes to display the new state
- **AND** the change is observed through Compose state rather than an explicit refresh call