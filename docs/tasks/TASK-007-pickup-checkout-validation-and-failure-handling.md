---
id: TASK-007
title: Pickup checkout validation and failure handling
status: ready
depends_on:
  - TASK-004
---
## Goal

Make the pickup checkout introduced in story 4 handle invalid customer input and invalid cart states clearly, without changing its successful checkout or confirmation behaviour.

## Depends On

- Story 4: guest pickup checkout and order confirmation.

## Scope

- Keep pickup checkout as a normal full-document form submission, not an HTMX interaction.
- Validate the customer values after applying the trimming defined by story 4. Empty-after-trimming values are invalid, and validation errors redisplay the trimmed submitted values.
- Require each customer field to be non-blank after trimming. Validate email with Jakarta Bean Validation's `@Email`; `albert@example.com` is valid and `albert.example.com` is invalid.
- Validation failures render the checkout page directly with `422 Unprocessable Content`, do not redirect, preserve trimmed submitted values, and show programmatically associated inline errors.
- A direct empty-cart checkout submission renders the checkout page with `422 Unprocessable Content`, preserves trimmed submitted values, and shows a clear form-level error.
- Re-check that every cart product is active while creating an order. If a product was archived after the cart was populated, render the checkout page with `422 Unprocessable Content`, preserve trimmed submitted values, show a clear form-level error, and create no order.

## Out of Scope

- Changing successful pickup checkout, confirmation, order snapshots, cart locking, or repeated-submission behaviour from story 4.
- Delivery address validation and delivery postal-code eligibility (story 5).
- Collision, rollback, concurrency, and corrupted-data checkout hardening (story 6).
- HTMX checkout interactions, client-side validation, payments, accounts, cancellation, notifications, and order lifecycle changes.

## Acceptance Criteria

- `./mvnw test` and `./mvnw verify` succeed.
- Missing contact fields and an invalid email return `422 Unprocessable Content` without redirecting, preserve the trimmed submitted values, and show associated inline errors.
- An empty-cart submission and an archived-product submission return `422 Unprocessable Content` without redirecting, preserve the trimmed submitted values, and show a clear form-level error. The archived-product test archives a cart product after the cart is populated and proves that no order is created.
