---
id: TASK-003
title: Persisted guest cart
status: done
depends_on:
  - TASK-002
---
## Goal

Implement persisted guest carts using opaque URL tokens.

Customers should be able to add products to a shopping bag and return to it through an unguessable token-based cart URL that does not expose database identifiers.

## Scope

- Add cart and cart item database tables.
- Use numeric internal database IDs only internally.
- Generate an opaque public cart token for URLs.
- Use stable public product slugs, not product database IDs, for catalogue add-to-cart and cart item mutation requests.
- Store stable unique public slugs for products in persistence and backfill or seed slugs for the existing demo products.
- Store cart state. This story only creates and uses `ACTIVE` carts. `CHECKED_OUT` and `ABANDONED` may be included as reserved enum values, but this story must not implement transitions into those states.
- Add routes:
  - `POST /cart/start`
  - `GET /cart/{cartToken}`
  - `POST /cart/{cartToken}/items`
  - `POST /cart/{cartToken}/items/{productSlug}`
  - `POST /cart/{cartToken}/items/{productSlug}/remove`
- Add "add to cart" actions from the product catalogue.
- If a product is already in the cart, increment its quantity.
- Support integer quantities only.
- Enforce minimum quantity `1`.
- Enforce maximum quantity `99` per cart line.
- Support updating cart line quantities.
- Support removing cart lines.
- Handle stale cart pages from another browser or device by applying mutations against the latest persisted server state.
- Use HTMX as progressive enhancement for cart quantity updates and cart summary refreshes while keeping normal form submissions working without JavaScript.
- Show current line totals and cart subtotal.

## Out of Scope

- Checkout
- Order creation
- Session-backed carts
- Customer accounts
- Cart cleanup jobs
- Stock validation
- Transitions to `CHECKED_OUT` or `ABANDONED`

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- Update or add an end-to-end or full-stack web test that covers starting a guest cart from the product catalogue, adding a product, reopening the cart by token URL, updating quantity, removing the line, and handling an unknown token.
- `POST /cart/start` creates a new `ACTIVE` cart on every request and redirects to `/cart/{cartToken}`.
- `POST /cart/start` is the only start-cart route; starting a cart must not be implemented as a state-changing `GET`.
- If `POST /cart/start` receives a `productSlug` form parameter, it creates the cart, adds the selected product, and redirects to `/cart/{cartToken}`.
- Cart URLs use `/cart/{cartToken}` and cart item mutation routes use product slugs within that cart; no route or form field in this story exposes cart, cart item, or product database IDs.
- Products have stable unique public slugs stored in persistence. Existing demo products are backfilled or seeded with slugs, and catalogue/cart forms use those slugs instead of product database IDs.
- Cart tokens are unique, case-sensitive, URL-safe opaque strings generated from at least 128 bits of cryptographic randomness.
- Cart tokens do not encode database IDs, product slugs, customer data, or other meaningful application data.
- Cart tokens are at least 32 characters long and use only URL-safe characters such as letters, digits, `_`, and `-`.
- `GET /cart/{cartToken}` renders the active cart for a known active token.
- `POST /cart/{cartToken}/items` with a `productSlug` form parameter adds the product to the active cart.
- `POST /cart/{cartToken}/items/{productSlug}` with a `quantity` form parameter updates the cart line for that product.
- `POST /cart/{cartToken}/items/{productSlug}/remove` removes the cart line for that product.
- Adding the same product twice increments the existing cart line.
- Add-product requests increment the current server-side quantity, even if the submitting page was rendered before another browser or device changed the cart.
- Quantity updates reject values below `1` and above `99`.
- Quantity updates use last-write-wins behaviour: the submitted valid quantity replaces the current server-side quantity for that cart line.
- Cart mutation responses render or redirect to the latest persisted cart state.
- Cart mutation forms work as normal form submissions without JavaScript.
- HTMX cart mutation requests return enough updated markup to refresh the changed line and cart subtotal without a full-page reload.
- Removing a cart line works.
- Removing a cart line that was already removed from the same cart by another browser or device does not produce a server error; the customer is returned to the latest cart state.
- Cart subtotal is calculated from current product prices.
- Opening or mutating an unknown cart token returns `404 Not Found` using a dedicated customer-facing cart-not-found page that includes a link back to the product catalogue.
- Non-active cart tokens, if present because reserved states are stored, are not usable in this story and return the same `404 Not Found` response.
- Adding, updating, or removing an unknown product slug for a known active cart returns `404 Not Found` using a customer-facing response.
- Removing a known product slug that is not currently in the cart does not mutate the cart and returns the latest cart state.
