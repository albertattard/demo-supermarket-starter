---
id: TASK-013
title: UI accessibility and responsive polish
status: ready
depends_on: []
---
## Goal

Apply basic UI, accessibility, and responsive polish across the server-rendered pages.

The application should be usable for workshop demos without introducing a frontend framework.

## Scope

- Keep styling in `src/main/resources/static/css/app.css`.
- Use local HTMX static resource.
- Ensure pages are usable on desktop and phone-sized viewports.
- Use semantic HTML where practical.
- Ensure form labels are associated with inputs.
- Ensure buttons and links have clear text.
- Ensure validation messages are visible and understandable.
- Ensure critical flows do not depend on custom JavaScript beyond HTMX enhancement.
- Keep the UI simple and supermarket-appropriate.
- Avoid Bootstrap, Tailwind, React, Angular, Vue, or frontend build tooling.

## Out of Scope

- Full design system
- Formal WCAG audit
- Visual regression testing
- Dark mode
- Advanced animations
- Product image generation

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- Catalogue, cart, checkout, logistics, and inventory pages are usable at mobile and desktop widths.
- Forms have associated labels.
- Validation messages are visible near the relevant fields or action.
- HTMX-enhanced actions still have server-side validation.
- No frontend JavaScript framework is introduced.
