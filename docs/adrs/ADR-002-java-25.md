# ADR-002: Java 25

## Status

Accepted.

## Context

The foundation issue specifies Java 25, and the project needs a runtime baseline that matches that requirement. Java is the organisation's default programming language for enterprise application development, so choosing Java keeps the project aligned with the organisation's go-to technology stack, existing skills, and support expectations. Using the specified Java version keeps local development, CI, and deployment aligned around one supported runtime instead of requiring compatibility with multiple Java baselines.

## Decision

We will target Java 25 as the project runtime baseline because Java is the organisation's standard enterprise application language, Java 25 is an explicit project constraint, and a single current runtime baseline simplifies tooling, dependency selection, and support expectations.

## Consequences

The project can use Java 25 as its assumed language and runtime level. Local development, CI, and deployment environments must provide a compatible Java 25 runtime. Moving to a different Java baseline later would require revisiting this decision and the project configuration that depends on it.
