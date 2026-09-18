---
id: TASK-002
title: Seed catalogue and product listing
status: done
depends_on:
  - TASK-001
---
## Goal

Create the initial customer-facing product catalogue backed by Flyway-seeded demo data.

This issue introduces the core catalogue domain without inventory stock tracking.

## Context

Customers need a simple browsable supermarket catalogue before cart and checkout work can be implemented. The catalogue should be server-rendered, backed by database tables, and populated with realistic demo data through Flyway.

This issue builds on the project foundation issue. The application skeleton, Maven build, H2 configuration, Flyway setup, Thymeleaf, and baseline CSS are assumed to already exist.

## Assumptions

- The catalogue is public and does not require login.
- Product data is managed only through Flyway seed data in this issue.
- Product availability is represented only by an active/archived flag.
- Stock quantities, shopping cart behaviour, and inventory management screens are handled by later issues.
- Prices are stored and calculated as decimal monetary values, not floating point values.

## Scope

- Add category and product database tables.
- Seed 4-6 categories and 12-20 products through Flyway.
- Seed data should look like plausible supermarket data, with varied categories, names, descriptions, unit labels, and prices.
- Products must include:
  - name
  - description
  - category
  - unit label
  - unit price
  - active/archived flag
  - optional image path
- Categories must include:
  - name
  - active/archived flag
- Implement catalogue repository/service/controller code.
- Render a server-side catalogue page at `/` and `/products`.
- Show only active products to customers.
- Do not show products whose category is archived.
- Support category filtering.
- Support text search by product name and description.
- Support category filtering and text search together.
- Treat text search as case-insensitive.
- Trim leading and trailing whitespace from search input.
- Preserve the current search and category filter values in the rendered page.
- Show a clear empty state when no products match the current filters.
- Sort categories by name for filter display.
- Sort products by category name, then product name, unless a more explicit product sort is added in the implementation.
- Display name, description, category, unit label, unit price, and optional image/placeholder.
- Use a local placeholder for products without an image path.
- Add basic responsive CSS for the catalogue.

## Out of Scope

- Product detail pages
- Shopping cart behaviour
- Inventory management screens
- Product image upload
- Stock tracking
- Public JSON API
- Pagination
- Product sorting controls
- Admin-only catalogue management
- Authentication or authorization changes

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- End-to-end tests cover browsing the catalogue at `/` and `/products`.
- End-to-end tests cover category filtering, text search, combined filtering, preserved filter values, and the empty state.
- Flyway creates category and product tables.
- Flyway seeds 4-6 categories and 12-20 products.
- Seeded products cover multiple categories and include realistic names, descriptions, unit labels, and euro prices.
- `/` shows the product catalogue.
- `/products` shows the product catalogue.
- The catalogue is accessible without login.
- Customer catalogue pages show only active products in active categories.
- Category filtering returns only matching active products.
- Text search matches active products by name or description, case-insensitively.
- Category filtering and text search work together.
- Search input is trimmed before filtering.
- Selected category and search text remain visible after filtering.
- A clear empty state appears when filters match no products.
- Categories in the filter UI are sorted by name.
- Products are sorted predictably by category name, then product name.
- Archived products are not shown in the customer catalogue.
- Prices are displayed in euros.
- Product prices are represented with `BigDecimal` in Java and decimal columns in the database.
- Products without an image path display a local placeholder instead of a broken image.
