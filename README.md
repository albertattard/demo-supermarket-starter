# Demo Supermarket

Demo Supermarket is a sample online grocery store used for workshop and
demonstration scenarios.

## Workshop

Workshop attendees should start with
[RUNBOOK.md](https://github.com/albertattard/demo-supermarket-runbook/blob/main/runbooks/README.md).
It uses the canonical task backlog as read-only context and keeps all workshop
changes local to the clone.

The application models a small supermarket that lets customers browse a product
catalogue, build a shopping bag, and place guest orders for pickup or delivery.
It also includes simple internal workflows for the teams that keep the store
running.

## What The Application Supports

Customers can:

- browse available grocery products
- search and filter the catalogue
- add products to a shopping bag
- place a guest order
- choose pickup or delivery
- receive an order code for confirmation

Logistics staff can:

- review incoming orders
- see customer and fulfillment details
- move orders through pickup or delivery preparation
- cancel orders when the order is still early in the process

Inventory staff can:

- maintain product and category information
- archive items that should no longer appear in the customer catalogue
- keep historical orders unchanged when catalogue details change later

## Fulfillment Model

Orders can be fulfilled by pickup or delivery.

Pickup has no delivery fee. Delivery is available only for configured service
areas and may include a fee depending on the order value.

Payment is intentionally outside the application. Customers pay when they pick
up the order or receive the delivery.

## Project Purpose

This project is intentionally small and incomplete. It is meant to provide a
realistic business domain for practicing AI-assisted software development,
reviewing feature work, and extending an application through a backlog.

Possible future enhancements include stock tracking, pickup and delivery time
slots, customer accounts, payment integration, richer product images, and order
history.

## License

[MIT](LICENSE)
