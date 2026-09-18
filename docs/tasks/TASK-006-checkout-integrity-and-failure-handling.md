---
id: TASK-006
title: Checkout integrity and failure handling
status: ready
depends_on:
  - TASK-004
  - TASK-005
---
## Goal

Make pickup and delivery guest checkout robust under collisions, failures, concurrent submissions, and corrupted cart-to-order data.

## Depends On

- Story 4: guest pickup checkout and order confirmation.
- Story 5: delivery checkout.

## Scope

- Apply the following integrity and failure-handling guarantees to both pickup and delivery checkout.
- Prove order-code collision retry at the database uniqueness boundary, not merely through a preflight lookup.
- Add a test-only failure-injection seam that throws after the order and items are persisted and the cart is marked `CHECKED_OUT`, but before commit. It must be disabled outside tests.
- Preserve the one-transaction invariant: failure leaves no order or order items and leaves the cart `ACTIVE`.
- Handle concurrent submissions for one cart so exactly one order is created and both callers resolve to that order's confirmation URL.
- Prove the unique cart-to-order relationship and treat a `CHECKED_OUT` cart with no linked order as corrupted data: use the application’s generic internal-error response and never create a replacement order.
- Use H2 with the configured default transaction isolation; do not set a custom isolation level.

## Out of Scope

- New customer-facing checkout fields or delivery rules.
- Rate limiting, proxy-aware client-IP handling, and other production anti-enumeration controls.

## Acceptance Criteria

- `./mvnw test` and `./mvnw verify` succeed.
- An automated test forces an order-code uniqueness collision and proves a different unique code is persisted.
- An automated rollback test verifies no order or order items remain and the cart stays `ACTIVE` after injected failure.
- Concurrent checkout tests prove exactly one order exists and both submissions resolve to its confirmation URL.
- Tests prove a checked-out cart without an order fails without a replacement order.
- No custom transaction-isolation setting is introduced.
