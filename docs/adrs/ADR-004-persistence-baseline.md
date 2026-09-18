# ADR-004: Persistence Baseline

## Status

Accepted.

## Context

The foundation needs persistence infrastructure before domain-specific data modelling is implemented. The expected supermarket domain is primarily CRUD-oriented, and the team is familiar with Java/Spring persistence patterns. Local development and tests need a database that can run without external infrastructure, and schema changes need an explicit ownership mechanism. The persistence approach should fit the Spring Boot stack without requiring a production database service for basic development and verification.

## Decision

We will configure Spring Data JPA, H2, and Flyway immediately. H2 will run in memory for local development and tests, using PostgreSQL compatibility mode. Flyway will own schema evolution through `src/main/resources/db/migration`. Hibernate schema generation will be disabled for mutation and configured to validate the schema instead. This combines Spring Data JPA for conventional data access, H2 for low-friction local execution, and Flyway for reviewable schema history. H2 is a local and test database choice only; this decision does not select the production database. PostgreSQL compatibility mode is used to keep local SQL behaviour closer to a production-style relational database where H2 can support it.

## Consequences

The project has a persistence stack available at the foundation stage. Local development and tests can run against an in-memory H2 database that approximates PostgreSQL behaviour where H2 supports it. Schema changes are expected to be represented as Flyway migrations under `src/main/resources/db/migration`, and application startup validates the resulting schema through JPA. Future persistence changes must account for the JPA, Flyway, and schema-validation baseline. A future production database choice should be recorded as a separate architecture decision.
