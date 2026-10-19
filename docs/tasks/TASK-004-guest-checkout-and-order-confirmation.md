---
id: TASK-004
title: Guest pickup checkout and order confirmation
status: needs-grooming
depends_on:
  - TASK-003
---
## Goal

Implement the first usable guest checkout: customers with an active cart can place a **pickup** order and view its confirmation.

This deliberately excludes delivery and the more demanding checkout-integrity work. Those are separate follow-up stories so that this story can be reviewed and implemented as one customer journey.

## Scope

- Add order and order-item tables.
- An order references its source cart through a non-null, unique `cart_id`; a cart can have at most one order.
- Add the `PICKUP` fulfilment type and the `PLACED` order status.
- Add `GET` and `POST /cart/{cartToken}/checkout` for active carts.
- Collect full name, email, and phone number. Require trimmed, non-blank values and validate email using Jakarta Bean Validation's `@Email` constraint.
- Create immutable snapshots of product name, unit label, unit price, quantity, line total, submitted customer data, fulfilment type, goods subtotal, delivery fee, and grand total.
- Pickup delivery fee is `0.00`.
- Mark the cart `CHECKED_OUT` only when order creation succeeds; reject empty carts and carts containing archived products.
- A repeated submission for a checked-out cart redirects to its existing order instead of creating another order.
- Checked-out carts retain the existing cart-not-found behaviour on cart routes.
- Add a confirmation route at `/orders/{orderCode}/confirmation`.

## Questions to resolve during readiness review

- What exact public order-code format, alphabet, entropy target, and collision behaviour should this first story guarantee?
- Is an order confirmation public to anyone holding the code, or should this story introduce another access mechanism?
- What customer-facing response should an unknown order code receive?

Do not choose answers to these questions implicitly in implementation. The groomed fixture resolves them for the workshop demo.

## Out of Scope

- Delivery addresses, delivery postal-code eligibility, and delivery fees (story 5).
- Concurrent-submission, forced-collision, rollback-injection, and corrupted-data scenarios (story 6).
- Payments, accounts, cancellation, time slots, notifications, editing orders, and lifecycle transitions beyond `PLACED`.

## Acceptance Criteria

- `./mvnw test` and `./mvnw verify` succeed.
- A full-stack or end-to-end test completes pickup checkout from an active cart.
- A valid checkout returns `303 See Other` to the canonical confirmation URL.
- Validation failures return `422 Unprocessable Content`, preserve submitted values, and show programmatically associated field errors.
- Empty-cart and archived-product checkout failures return `422 Unprocessable Content` with a clear form-level error.
- Pickup checkout creates one `PLACED` order with immutable product and customer snapshots, a `0.00` delivery fee, and the correct totals.
- A repeated checkout creates no additional order and resolves to the original confirmation page.
- Checked-out carts cannot be modified.
