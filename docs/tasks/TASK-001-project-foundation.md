---
id: TASK-001
title: Create Spring Boot project foundation
status: done
depends_on: []
---
## Goal

Create the baseline Spring Boot project for the Demo Supermarket application so future feature work can be built, tested, and reviewed consistently.

This issue establishes the build, application skeleton, configuration, database migration setup, local verification lifecycle, and basic documentation. It should not implement supermarket business features yet.

## Context

Demo Supermarket is a server-rendered Spring Boot application for a workshop scenario. This first issue should leave the repository in a clean, runnable state with the framework, test lifecycle, and documentation conventions in place.

Engineers working on later issues should be able to clone the repository, run the application, run the test suite, and understand the intended stack without needing any additional setup notes.

## Assumptions

- This is the first application implementation issue.
- No product, cart, checkout, logistics, or inventory behaviour exists yet.
- The application uses server-rendered pages with Thymeleaf and HTMX, not a JavaScript frontend framework.
- H2 is used as an in-memory development and test database.
- Flyway owns schema creation from the beginning, even if the initial migration only establishes the migration baseline.
- Browser-based E2E testing is introduced in this issue with a minimal smoke test. Later UI stories should extend the E2E suite for the workflows they add.
- The public customer-facing pages introduced in early stories do not require login.
- Routes not explicitly made public should require authentication by default.

## Scope

- Create a Maven-based Spring Boot application.
- Use Java 25.
- Use Spring Boot 4.1.x.
- Use package `demo.supermarket`.
- Use application class `SupermarketApplication`.
- Add dependencies for:
  - Spring Web MVC
  - Thymeleaf
  - HTMX WebJar static resource support
  - Spring Security
  - Spring Data JPA
  - H2
  - Flyway
  - Spring Boot Actuator
  - Bean Validation
  - Java Playwright for E2E tests
- Add HTMX as a dependency-managed WebJar so future Thymeleaf pages can use it without relying on an external CDN or Node-based tooling.
- Configure H2 as an in-memory database for local development and tests.
- Configure Flyway to load migrations from the standard `src/main/resources/db/migration` location.
- Add an initial Flyway migration file so Flyway runs successfully on startup. The migration may be intentionally minimal, but it must contain executable SQL, be valid for H2, and must not create supermarket business tables.
- Add default application configuration for local development and tests.
- Expose `/actuator/health` through Actuator for local readiness checks.
- Add a minimal server-rendered home page at `/` with the application name and a short placeholder message.
- Keep the home page deliberately small; it exists as an initial smoke-test target until the catalogue issue replaces `/` with the product catalogue.
- Add a minimal local CSS file used by the home page.
- Configure Spring Security so `/`, `/actuator/health`, and static assets required by the home page are accessible without authentication.
- Configure Spring Security so every other route requires authentication by default.
- Configure Java Playwright, not Node Playwright.
- Install the Playwright Chromium browser binary automatically as part of `./mvnw verify`.
- Run Playwright browsers headless.
- Tag all E2E tests with `@Tag("e2e")`.
- Configure Surefire to exclude `@Tag("e2e")` tests from the `test` phase.
- Configure Failsafe or equivalent Maven lifecycle wiring to run only `@Tag("e2e")` tests during the `integration-test` and `verify` phases.
- Start the Spring Boot application before E2E tests on port `18080`.
- Pass the E2E server port to the application through command-line arguments or system properties.
- Use `/actuator/health` as the E2E readiness check.
- Add a minimal Playwright smoke test that opens `/` and verifies the initial home page renders.
- Add Maven Wrapper pinned to Apache Maven 3.9.16.
- Add `.gitignore` and `.editorconfig`.
- Add initial `README.md` with setup, run, and test instructions.
- Add `docs/adrs/README.md` with the architectural decisions made in this issue.

## Out of Scope

- Product catalogue
- Shopping bag/cart
- Checkout
- Orders
- Logistics dashboard
- Inventory management screens
- Seeded business data
- Authentication screens beyond default/security placeholder behaviour
- E2E browser flows beyond the initial home page smoke test
- Marketing landing page content
- Catalogue-specific E2E assertions
- Cart, checkout, logistics, or inventory E2E coverage
- Visual regression testing
- Cross-browser test matrix
- Mobile viewport test matrix
- Screenshots or videos as required test artefacts
- Node-based Playwright tooling
- Playwright browsers other than Chromium
- Production database configuration
- Deployment configuration
- Docker or container setup
- Frontend build tooling
- Repository-local Codex skills; these are delivered by [TASK-014](TASK-014-repository-local-agent-skills.md) in a separate, dedicated baseline commit.

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw test` does not run E2E tests.
- `./mvnw verify` succeeds.
- `./mvnw verify` runs the normal test suite and then the E2E suite.
- E2E tests are selected by `@Tag("e2e")`, not only by class name.
- Application starts locally with `./mvnw spring-boot:run`.
- `/actuator/health` returns HTTP 200 when the app is running.
- `/` returns HTTP 200 and renders a minimal server-side home page.
- `/`, `/actuator/health`, and static assets required by the home page, including `/css/**`, `/js/**`, `/images/**`, and `/webjars/**`, are accessible without authentication.
- Routes other than the explicitly permitted public routes require authentication by default.
- The home page makes clear that catalogue functionality is not implemented yet.
- The E2E Maven lifecycle starts the Spring Boot application before browser tests and stops it after the tests complete.
- The E2E Maven lifecycle starts the application on port `18080` without requiring a dedicated `application-e2e.yml` file.
- E2E tests wait for `/actuator/health` before opening browser pages.
- Playwright installs and runs Chromium headless locally.
- The initial E2E smoke test opens `/` and verifies the home page renders.
- Automated tests verify that the local CSS asset and HTMX WebJar asset are available without authentication.
- Flyway runs during application startup using `src/main/resources/db/migration`.
- The initial Flyway migration is present, valid, and does not create business tables.
- H2 in-memory configuration is present for local development and tests.
- Maven Wrapper files are committed, documented, and pinned to Apache Maven 3.9.16.
- `.gitignore` excludes build outputs, IDE metadata, and local runtime files that should not be committed.
- `.editorconfig` defines consistent formatting basics for the repository.
- README explains the project purpose, stack, how to run/test it, and that the first `./mvnw verify` run may download the Playwright Chromium browser binary.
- `docs/adrs/README.md` records the key architectural decisions made so far.
- The application does not require Node, npm, Docker, or an external database to run locally.
