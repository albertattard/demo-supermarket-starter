---
id: TASK-009
title: Logistics order dashboard
status: ready
depends_on: []
---
## Goal

Implement the logistics order dashboard and fulfilment-specific status transitions.

Logistics users should be able to find orders, inspect details, and move them through valid workflow states.

## Scope

- Add logistics order list at `/logistics/orders`.
- Add order detail page at `/logistics/orders/{orderCode}`.
- Show orders newest first.
- Support filtering by fulfilment type:
  - `PICKUP`
  - `DELIVERY`
- Support filtering by status.
- Support searching by human-friendly order code.
- Display customer contact details, fulfilment details, order items, and totals.
- Implement backend status transition validation.
- Valid common transition:
  - `PLACED -> PREPARING`
- Valid pickup transitions:
  - `PREPARING -> READY_FOR_PICKUP -> COMPLETED`
- Valid delivery transitions:
  - `PREPARING -> OUT_FOR_DELIVERY -> COMPLETED`
- Allow cancellation from `PLACED` or `PREPARING`.
- Reject invalid transitions in the service layer.
- Show only valid next actions in the UI.
- Use HTMX where practical for status updates.

## Out of Scope

- Editing customer details
- Editing quantities or prices
- Delivery assignment
- Driver workflow
- Picking lists or labels
- Audit trail

## Acceptance Criteria

- `./mvnw test` succeeds.
- `./mvnw verify` succeeds.
- Logistics user can list and inspect orders.
- Logistics user can search by order code.
- Pickup orders cannot be moved to `OUT_FOR_DELIVERY`.
- Delivery orders cannot be moved to `READY_FOR_PICKUP`.
- Invalid status transitions are rejected by backend tests.
- Status update UI only offers valid next transitions.
- Orders can be cancelled from `PLACED` or `PREPARING`.
