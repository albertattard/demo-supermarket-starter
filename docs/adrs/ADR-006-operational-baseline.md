# ADR-006: Health-Only Actuator Exposure

## Status

Accepted.

## Context

The foundation needs a minimal operational endpoint so a running application can be checked locally and by simple readiness tooling. Spring Boot Actuator can expose many operational and diagnostic endpoints, but the foundation does not yet have a defined consumer, access model, or operational requirement for those broader endpoints. Keeping the operational surface small avoids exposing diagnostic information before there is a clear reason and security model for it.

## Decision

We will expose only the Spring Boot Actuator health endpoint over HTTP. Health probes will be enabled so the application has a simple readiness signal. Continuous integration will verify the application through the Maven `verify` lifecycle rather than requiring additional runtime operational endpoints. Metrics, environment, info, and other diagnostic endpoints will remain disabled until there is a concrete operational need and an access-control decision for them.

## Consequences

Local readiness checks have a health endpoint available, and automated verification runs through the build lifecycle. The operational surface area remains narrow at the foundation stage, which reduces accidental diagnostic exposure. Additional Actuator endpoints can be enabled later, but doing so should be intentional and should define who or what is allowed to consume them.
