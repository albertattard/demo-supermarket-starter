# ADR-001: Spring Boot and Maven

## Status

Accepted.

## Context

The project needs a conventional Java application foundation from the start. The foundation must provide build structure, dependency management, and a plugin lifecycle for development and automation. The team is already familiar with the Spring Boot and Maven stack, which reduces delivery risk and makes the project easier to maintain during workshop and demonstration work.

## Decision

We will use Spring Boot with Maven for the application foundation. Spring Boot provides integrated support for the server-rendered web, security, persistence, configuration, testing, and operational concerns expected by this project. Maven provides a stable and widely supported build lifecycle that is sufficient for the project's automation needs.

## Consequences

The project starts with a standard Spring Boot structure and a Maven build. Dependencies and build plugins can be managed through Maven conventions. The team can use familiar tools and patterns instead of spending foundation effort on build or framework selection. Future changes to the build system would need to account for the Spring Boot and Maven lifecycle already established by this decision.
