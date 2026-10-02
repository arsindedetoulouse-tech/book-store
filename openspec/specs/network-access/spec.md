# network-access Specification

## Purpose

Defines the boundary between feature modules and remote data access, so that wire-format concerns stay in one place and remain substitutable in tests.

## Requirements

### Requirement: Network access is exposed as typed interfaces

The network module SHALL expose remote operations as typed interfaces whose methods return domain types. Feature modules SHALL consume those interfaces and SHALL NOT depend on the underlying HTTP client type.

#### Scenario: Feature performing a remote call

- **WHEN** a feature module needs remote data
- **THEN** it calls a method on a network interface
- **AND** the result it handles is expressed in domain types
- **AND** no HTTP client type, request builder, or response type appears in the feature module

#### Scenario: Exposing a remote operation

- **WHEN** a new remote operation is needed
- **THEN** it is declared on a network interface returning a domain type
- **AND** the interface is usable by feature modules without importing the HTTP library

### Requirement: Wire-format types are isolated to the network module

Types that describe the serialized form of remote data SHALL be declared only inside the network module. Domain model types SHALL NOT be annotated for serialization, and the domain model module SHALL NOT depend on any serialization facility.

#### Scenario: Domain type definition

- **WHEN** a domain type is declared in the domain model module
- **THEN** it carries no serialization annotation
- **AND** the domain model module declares no dependency on a serialization library or plugin

#### Scenario: Mapping a response to a domain type

- **WHEN** a remote response is received
- **THEN** the network module maps its wire-format representation to the corresponding domain type
- **AND** the wire-format type remains internal to the network module

#### Scenario: Serializing view-model state

- **WHEN** a screen's state is captured or restored
- **THEN** its shape is not constrained by any remote data format

### Requirement: HTTP client configuration is centralised

HTTP client configuration such as timeouts, content negotiation, logging, and engine selection SHALL be declared once in the network module and SHALL NOT be repeated by consuming modules.

#### Scenario: Consuming module omits client configuration

- **WHEN** a feature module obtains a client through injection
- **THEN** the module declares no timeouts, serialization format, or engine choice of its own
- **AND** the injected client already carries the project's shared configuration

#### Scenario: Changing shared client behaviour

- **WHEN** shared client behaviour such as the logging level changes
- **THEN** the change is made once in the network module
- **AND** every consumer reflects it without modification

### Requirement: Remote failures surface as typed results

A remote operation SHALL NOT propagate raw transport or serialization exceptions to callers. Failures SHALL be surfaced as an explicit typed result that distinguishes the failure modes a caller can reasonably handle.

#### Scenario: Transport failure

- **WHEN** a remote call fails because of connectivity or timeout
- **THEN** the caller receives a typed failure indicating a connectivity-class problem
- **AND** no raw transport exception reaches the caller

#### Scenario: Malformed response

- **WHEN** a response cannot be parsed into its wire-format representation
- **THEN** the caller receives a typed failure indicating a data-format problem
- **AND** no raw serialization exception reaches the caller

#### Scenario: Successful call

- **WHEN** a remote call succeeds
- **THEN** the caller receives the mapped domain result
- **AND** the success case is distinguishable from every failure case by type

### Requirement: Network access is substitutable in tests

A network interface SHALL be replaceable with a test double without modifying the code under test. A test double SHALL be constructible without a real network connection.

#### Scenario: Testing a feature that depends on network access

- **WHEN** a unit test exercises a screen's view model that depends on a network interface
- **THEN** the test substitutes its own implementation of that interface
- **AND** the screen's view model code is unchanged

#### Scenario: Exercising failure handling

- **WHEN** a test needs to verify a failure path
- **THEN** the test double returns the corresponding typed failure
- **AND** the view model under test is not modified to accommodate the test

#### Scenario: Intercepting requests

- **WHEN** a test needs to verify the actual request sent to the server
- **THEN** the request can be intercepted without a live server
- **AND** the assertion is made against the intercepted request