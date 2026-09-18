---
id: TASK-008
title: Security and demo users
status: ready
depends_on: []
---
## Goal

Add simple username/password security for logistics and inventory users.

Customers remain unauthenticated guests, while internal role areas require login.

## Scope

- Configure Spring Security.
- Add user and role persistence as needed.
- Seed demo users through Flyway:
  - username `logistics`, password `logistics`, role `ROLE_LOGISTICS`
  - username `inventory`, password `inventory`, role `ROLE_INVENTORY`
- Store passwords as BCrypt hashes.
- Add a simple login page at `/login`.
- Add logout support.
- Restrict `/logistics/**` to `ROLE_LOGISTICS`.
- Restrict `/inventory/**` to `ROLE_INVENTORY`.
- Keep public customer catalogue/cart/checkout routes accessible without login.
- Ensure `/actuator/health` remains available for CI readiness.
- Document demo credentials in the README.

## Out of Scope

- User registration
- Password reset
- User management screens
- Customer accounts
- Multi-role demo users
- OAuth or external identity providers

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- Public catalogue routes are accessible without login.
- `/logistics/**` redirects unauthenticated users to login.
- `/inventory/**` redirects unauthenticated users to login.
- Logistics user can access logistics routes and not inventory routes.
- Inventory user can access inventory routes and not logistics routes.
- Seeded passwords are BCrypt hashes in the database migration.
