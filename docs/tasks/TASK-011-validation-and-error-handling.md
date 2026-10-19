---
id: TASK-011
title: Validation and error handling
status: ready
depends_on: []
---
## Goal

Improve validation and user-facing error handling across catalogue, cart, logistics, and inventory workflows.

The baseline should remain simple, but invalid input and invalid business actions should be handled clearly.

## Scope

- Use Bean Validation annotations for form input where practical.
- Show inline field errors in Thymeleaf forms.
- Show page-level business errors for invalid cart/order states.
- Return updated Thymeleaf fragments with errors for HTMX requests.
- Validate:
  - missing delivery address fields
  - unsupported delivery postal code
  - invalid cart quantity
  - invalid order status transition
  - invalid product/category forms
- Keep error styling simple and accessible.

## Out of Scope

- Global toast notification system
- Client-side validation framework
- Internationalization
- Formal WCAG audit
- Custom exception hierarchy beyond what is useful
- Pickup checkout validation and checkout-state failures (story 7)

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- Invalid forms redisplay with field errors.
- Business rule failures show clear page-level messages.
- HTMX validation failures update the relevant fragment without requiring a full page reload where HTMX is used.
- Backend tests cover representative validation and business-rule failures.
