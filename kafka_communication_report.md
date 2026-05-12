# Backend Communication & Kafka Report

This report outlines how the microservices in the `Stonks` backend communicate, including the Kafka topics utilized, the producers and consumers for each topic, and the mix of synchronous (REST) and asynchronous (Kafka) communication patterns.

---

## 1. Synchronous vs Asynchronous Communication

The backend predominantly relies on **asynchronous, event-driven communication** via Kafka to handle business processes (such as trading sagas) and market data streaming. However, it also uses **synchronous REST communication** for specific direct invocations, mostly originating from the `trading-service`.

### Synchronous Communication (REST via `RestTemplate`)
- **`trading-service` $\rightarrow$ `wallet-service`:** 
  - `POST /wallet/{userId}/debit`
  - `POST /wallet/{userId}/credit`
- **`trading-service` $\rightarrow$ `portfolio-service`:**
  - `POST /portfolio/{userId}/buy`
  - `POST /portfolio/{userId}/sell`
- **`trading-service` $\rightarrow$ `market-data-service`:**
  - `GET /market/prices/{symbol}` (or similar, to fetch current stock prices during trade validation).

### Asynchronous Communication (Kafka)
All other inter-service data flows, including the complex Saga choreographies (trade execution, wallet compensation, portfolio adjustments) and market simulations, are handled asynchronously via Kafka.

---

## 2. Kafka Topics: Producers & Consumers

Below is a comprehensive list of the Kafka topics used in the system, along with the services that produce and consume them.

> [!NOTE] 
> The **`audit-log-service`** consumes almost every topic in the system to maintain a centralized audit trail.

### Trading Saga Topics
These topics facilitate the choreographed Saga pattern for processing trades across the trading, wallet, and portfolio services.

| Topic | Producer(s) | Consumer(s) |
| :--- | :--- | :--- |
| `trade-initiated` | `trading-service` | `wallet-service`, `portfolio-service`, `audit-log-service` |
| `trade-completed` | `trading-service` | `audit-log-service` |
| `wallet-debited` | `wallet-service` | `trading-service`, `audit-log-service` |
| `wallet-failed` | `wallet-service` | `trading-service`, `audit-log-service` |
| `wallet-credited` | `wallet-service` | `trading-service`, `audit-log-service` |
| `wallet-refund-requested` | `trading-service` | `wallet-service`, `audit-log-service` |
| `wallet-credit-requested` | `trading-service` | `wallet-service`, `audit-log-service` |
| `portfolio-add-requested` | `trading-service` | `portfolio-service`, `audit-log-service` |
| `portfolio-added` | `portfolio-service` | `trading-service`, `audit-log-service` |
| `portfolio-deducted` | `portfolio-service` | `trading-service`, `audit-log-service` |
| `portfolio-failed` | `portfolio-service` | `trading-service`, `audit-log-service` |

### Market & Exchange Topics
These topics handle the simulation of market activities, order matching, and price updates.

| Topic | Producer(s) | Consumer(s) |
| :--- | :--- | :--- |
| `order-request` | `market-simulation-service` | `order-service`, `audit-log-service` |
| `trade-executed` | `order-service` | `market-data-service`, `audit-log-service` |
| `market-price-updated` | `market-data-service` | `market-simulation-service`, `audit-log-service` |
| `market-event-created` | `market-simulation-service` (or internal) | `market-data-service` |
| `volatility-changed` | `market-simulation-service` (or internal) | `market-data-service` |

### General Domain Topics
| Topic | Producer(s) | Consumer(s) |
| :--- | :--- | :--- |
| `user-registration` | `auth-service` | `audit-log-service` |

---

## 3. Service-by-Service Communication Overview

### `audit-log-service`
*   **Role:** Centralized logging and auditing.
*   **Consumes:** `user-registration`, `trade-initiated`, `trade-completed`, all `wallet-*` events, all `portfolio-*` events, `order-request`, `trade-executed`, `market-price-updated`.

### `auth-service`
*   **Produces:** `user-registration` upon successful user signup.

### `trading-service`
*   **Role:** Orchestrates the Trade Saga and exposes sync endpoints to the frontend.
*   **Sync Clients:** Calls `wallet-service`, `portfolio-service`, and `market-data-service` via REST.
*   **Produces:** `trade-initiated`, `trade-completed`, `wallet-refund-requested`, `wallet-credit-requested`, `portfolio-add-requested`.
*   **Consumes:** `wallet-debited`, `wallet-failed`, `wallet-credited`, `portfolio-added`, `portfolio-deducted`, `portfolio-failed`.

### `wallet-service`
*   **Role:** Manages user balances.
*   **Sync Provider:** Exposes debit/credit REST endpoints.
*   **Produces:** `wallet-debited`, `wallet-failed`, `wallet-credited`.
*   **Consumes:** `trade-initiated`, `wallet-refund-requested`, `wallet-credit-requested`.

### `portfolio-service`
*   **Role:** Manages user stock holdings.
*   **Sync Provider:** Exposes buy/sell REST endpoints.
*   **Produces:** `portfolio-added`, `portfolio-deducted`, `portfolio-failed`.
*   **Consumes:** `trade-initiated`, `portfolio-add-requested`.

### `order-service`
*   **Role:** Acts as the exchange matching engine.
*   **Produces:** `trade-executed` when orders are matched.
*   **Consumes:** `order-request` (typically from bots).

### `market-data-service`
*   **Role:** Processes raw trades into price updates and broadcasts to the frontend via WebSockets.
*   **Produces:** `market-price-updated`.
*   **Consumes:** `trade-executed`, `market-event-created`, `volatility-changed`.

### `market-simulation-service`
*   **Role:** Runs bot engines to generate market liquidity.
*   **Produces:** `order-request` to simulate buy/sell pressure.
*   **Consumes:** `market-price-updated` to adjust bot strategies.
