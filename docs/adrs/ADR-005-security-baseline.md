# ADR-005: Protected-by-Default Web Security

## Status

Accepted.

## Context

The foundation needs security enabled early enough that later features are built with authentication in mind instead of treating security as a later retrofit. The safest default for new application routes is to require authentication unless there is an explicit reason for public access. At the same time, the public placeholder page, static assets, HTMX WebJar, and health endpoint need to remain accessible without credentials so local development, automated tests, and readiness checks can verify the application. The foundation does not yet need a custom login experience or user-management model.

## Decision

We will enable Spring Security from the foundation and use a protected-by-default route policy. We will explicitly permit unauthenticated access to `/`, `/actuator/health`, `/css/**`, `/js/**`, `/images/**`, and `/webjars/**`. All other routes will require authentication until a more specific security model is introduced. Protected routes will use HTTP Basic for now because it exercises the authentication boundary without committing the application to a custom login, session, role, or user-management flow.

## Consequences

New application routes are protected unless they are deliberately added to the public route list. HTTP Basic provides a minimal authentication mechanism before custom login or user-management flows are implemented. The home page, required public assets, and health checks remain usable without credential handling. Future security changes must preserve or intentionally replace the protected-by-default policy and the public route decisions.
