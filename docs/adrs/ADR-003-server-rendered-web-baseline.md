# ADR-003: Server-Rendered Web Baseline

## Status

Accepted.

## Context

The foundation establishes the web application baseline before domain-specific customer and staff workflows are implemented. The application needs a server-rendered structure, static asset handling, and an initial public page that can verify the web stack is wired correctly. The expected early workflows are form- and page-oriented, so server rendering is sufficient without introducing a separate client-side application build. The team wants to avoid introducing a new front-end framework for this baseline because a separate JavaScript application would add another application state model in the browser alongside the backend state already managed by the Java application.

## Decision

We will include Spring Web MVC, Thymeleaf, and the HTMX WebJar for the server-rendered web baseline. We will provide a public placeholder home page at `/` that renders through Thymeleaf and loads the shared CSS and HTMX asset. We will not implement domain-specific business screens as part of the foundation. This keeps the UI stack aligned with Spring Boot conventions while still leaving room for incremental interactivity through HTMX. It also keeps most application state and rendering decisions on the server, which simplifies development and maintenance for the workflows currently expected.

## Consequences

The project is prepared for server-rendered web flows using Spring MVC, Thymeleaf templates, static assets, and HTMX. The placeholder home page provides a simple end-to-end check of the web stack without committing to catalog, shopping bag, order, inventory, or logistics behavior. Business screens can be added later on top of this baseline. The project does not yet commit to a client-side application structure, package manager, or frontend build pipeline beyond HTMX-enhanced server-rendered pages. If future workflows require richer client-side state, that should be treated as a new architecture decision rather than assumed into the foundation.
