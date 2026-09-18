---
id: TASK-005
title: Delivery checkout
status: ready
depends_on:
  - TASK-004
---
## Goal

Extend guest checkout so customers can choose delivery, provide a valid delivery address, and receive the correct delivery price and confirmation details.

## Depends On

- Story 4: guest pickup checkout and order confirmation.

## Scope

- Add the `DELIVERY` fulfilment type to the existing checkout and order model.
- Seed the approved postal codes `10115` and `10116` through Flyway.
- For delivery, collect street, house number, postal code, and city. Do not collect a country.
- Require trimmed, non-blank delivery fields. Trim postal codes before validation; require exactly five digits before checking the approved list.
- Distinguish malformed postal codes from syntactically valid but unsupported postal codes in field-level messages.
- Snapshot delivery address fields on delivery orders only; pickup orders must not retain them.
- Calculate a `4.99` delivery fee when the goods subtotal is below `50.00`, otherwise `0.00`.
- Extend the public confirmation page with delivery address and `Pay when your order is delivered.` for delivery orders.

## Out of Scope

- Changing pickup checkout semantics.
- Checkout collision, concurrency, rollback, and corrupted-data hardening (story 6).
- Delivery time slots, countries, payment, customer accounts, and notifications.

## Acceptance Criteria

- `./mvnw test` and `./mvnw verify` succeed.
- A full-stack or end-to-end test completes delivery checkout using ` 10115 ` and creates a `PLACED` order.
- Incomplete delivery address, malformed postal code, and unsupported postal code `80331` return `422 Unprocessable Content`, preserve submitted values, and display associated corrective field errors.
- Delivery fee is `4.99` below `50.00` goods subtotal and `0.00` at or above it.
- Delivery confirmations show the delivery address, snapshots, totals, and the delivery payment message.
- Pickup orders continue to have no retained delivery address and display the pickup payment message.
