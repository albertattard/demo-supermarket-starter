---
id: TASK-012
title: Documentation and workshop backlog
status: ready
depends_on: []
---
## Goal

Document the project decisions, local workflow, local verification, and future workshop backlog.

The documentation should make the repository usable as a baseline for AI-assisted development.

## Scope

- Update `README.md` with:
  - project purpose
  - tech stack
  - personas
  - fulfilment rules
  - demo credentials
  - how to run locally
  - how to run tests
  - how E2E tests work
  - local verification summary
  - workshop automation flow
  - intentionally missing features
- Add or update `docs/adrs/README.md` with key architectural decisions:
  - Java 25
  - Spring Boot + Thymeleaf + HTMX
  - no React or other JS framework
  - H2 in-memory
  - Flyway schema/seed data
  - persisted guest carts with opaque tokens
  - no payment integration
  - no stock tracking yet
  - order item snapshots
  - logistics/inventory roles
  - E2E tests with Java Playwright and `@Tag("e2e")`
  - no containers
  - no Lombok
  - no `AGENTS.md` in the baseline
- Add sample future backlog entries with acceptance criteria:
  - stock tracking
  - pickup/delivery slots
  - product images

## Out of Scope

- Creating GitHub Issues automatically
- Issue templates
- Pull request templates
- `AGENTS.md`
- Formal architecture diagrams

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- README accurately reflects implemented behaviour.
- `docs/adrs/README.md` records the important decisions and rationale.
- Documentation clearly states that H2 data resets on restart.
- Documentation clearly states that payment and production deployment are out of scope.
- Documentation includes sample backlog items with acceptance criteria.
- No `AGENTS.md` file is added.
