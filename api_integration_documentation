# Stonk Platform API Documentation

This document provides a comprehensive overview of the REST APIs and WebSocket endpoints available in the Stonk microservices backend, designed for frontend integration.

> [!NOTE]
> All REST API endpoints are accessible via the **API Gateway** on the designated server port (default 8080). For example, `http://localhost:8080/auth/login`.

---

## 1. Authentication Service (`/auth` & `/users`)

Handles user registration, login, and user management.

### `POST /auth/register`
Registers a new user account.
*   **Request Body (JSON):**
    ```json
    {
      "username": "string (min 3, max 50)",
      "email": "string (valid email)",
      "password": "string (min 6 characters)",
      "role": "string (optional, defaults to USER)"
    }
    ```
*   **Response (201 Created):** `AuthResponse`
    ```json
    {
      "token": "string (JWT)",
      "userId": "number",
      "username": "string"
    }
    ```

### `POST /auth/login`
Authenticates an existing user.
*   **Request Body (JSON):**
    ```json
    {
      "username": "string",
      "password": "string"
    }
    ```
*   **Response (200 OK):** `AuthResponse` (same as register)

### `GET /users/{id}`
Retrieves user details by ID.
*   **Response:** `UserResponse`
    ```json
    {
      "id": "number",
      "username": "string",
      "email": "string",
      "role": "string",
      "createdAt": "datetime",
      "updatedAt": "datetime"
    }
    ```

### `GET /users/username/{username}`
Retrieves user details by username.
*   **Response:** `UserResponse`

### `GET /users`
Retrieves a list of all users.
*   **Response:** `List<UserResponse>`

### `PUT /users/{id}`
Updates a user's details.
*   **Request Body (JSON):**
    ```json
    {
      "username": "string (optional)",
      "email": "string (optional)"
    }
    ```
*   **Response:** `UserResponse`

### `DELETE /users/{id}`
Deletes a user account.
*   **Response:** `204 No Content`

---

## 2. Wallet Service (`/wallet`)

Manages user balances and transactions.

### `POST /wallet/{userId}/create`
Creates a new wallet for a user.
*   **Response (201 Created):** `WalletResponse`
    ```json
    {
      "id": "number",
      "userId": "number",
      "username": "string",
      "balance": "number",
      "currency": "string"
    }
    ```

### `GET /wallet/{userId}`
Retrieves wallet details for a user.
*   **Response:** `WalletResponse`

### `POST /wallet/{userId}/deposit`
Deposits funds into a wallet.
*   **Request Body (JSON):**
    ```json
    {
      "amount": "number"
    }
    ```
*   **Response:** `WalletResponse`

### `POST /wallet/{userId}/withdraw`
Withdraws funds from a wallet.
*   **Request Body (JSON):** `AmountRequest` (same as deposit)
*   **Response:** `WalletResponse`

### `POST /wallet/{userId}/debit` & `POST /wallet/{userId}/credit`
Internal endpoints (usually invoked by trading service) to debit or credit funds.
*   **Request Body (JSON):** `AmountRequest`
*   **Response:** `WalletResponse`

### `GET /wallet/{userId}/transactions`
Retrieves transaction history for a user's wallet.
*   **Response:** `List<TransactionResponse>`
    ```json
    [
      {
        "id": "number",
        "type": "string (DEPOSIT/WITHDRAWAL/CREDIT/DEBIT)",
        "amount": "number",
        "balanceAfter": "number",
        "description": "string",
        "createdAt": "datetime"
      }
    ]
    ```

---

## 3. Trading Service (`/trades`)

Handles order placements. Requires Authorization header with JWT.

### `POST /trades/buy` & `POST /trades/sell`
Executes a buy or sell trade.
*   **Headers:** `Authorization: Bearer <JWT>`
*   **Request Body (JSON):**
    ```json
    {
      "userId": "number",
      "symbol": "string (e.g., AAPL)",
      "quantity": "number"
    }
    ```
*   **Response (201 Created):** `TradeResponse`
    ```json
    {
      "id": "number",
      "userId": "number",
      "symbol": "string",
      "type": "string (BUY/SELL)",
      "quantity": "number",
      "price": "number",
      "totalAmount": "number",
      "status": "string (PENDING/EXECUTED/FAILED)",
      "orderId": "number",
      "createdAt": "datetime"
    }
    ```

### `GET /trades/{userId}`
Retrieves trade history for a user.
*   **Headers:** `Authorization: Bearer <JWT>`
*   **Response:** `List<TradeResponse>`

### `GET /trades/detail/{id}`
Retrieves a specific trade's details by Trade ID.
*   **Response:** `TradeResponse`

---

## 4. Portfolio Service (`/portfolio`)

Manages user stock holdings.

### `GET /portfolio/{userId}`
Retrieves a user's entire portfolio.
*   **Response:** `List<HoldingResponse>`
    ```json
    [
      {
        "id": "number",
        "username": "string",
        "symbol": "string",
        "quantity": "number",
        "averagePrice": "number",
        "totalInvested": "number"
      }
    ]
    ```

### `GET /portfolio/{userId}/holding/{symbol}`
Retrieves a specific holding (e.g., AAPL) for a user.
*   **Response:** `HoldingResponse`

### `POST /portfolio/{userId}/buy` & `POST /portfolio/{userId}/sell`
Internal endpoints to adjust portfolio holdings after a trade.
*   **Request Body (JSON):**
    ```json
    {
      "symbol": "string",
      "quantity": "number",
      "price": "number"
    }
    ```
*   **Response:** `HoldingResponse`

---

## 5. Market Data Service (`/market`)

Provides on-demand market snapshots and historical context.

### `GET /market/stocks`
Lists all available stocks.
*   **Response:** `List<StockResponse>`
    ```json
    [
      {
        "symbol": "string",
        "name": "string",
        "price": "number",
        "changePercent": "number"
      }
    ]
    ```

### `GET /market/stocks/{symbol}`
Gets the current price and details for a single stock.
*   **Response:** `StockResponse`

### `GET /market/overview`
Retrieves top gainers/losers and market status.
*   **Response:** `MarketOverviewMessage`

### `GET /market/stocks/{symbol}/candles`
Retrieves the current 1-minute candle snapshot for a stock.
*   **Response:** `LiveCandleMessage`

### `GET /market/stocks/{symbol}/orderbook`
Retrieves the current simulated order book snapshot.
*   **Response:** `LiveOrderBookMessage`

### `GET /market/events`
Retrieves a list of recent market events.
*   **Response:** `List<MarketEventMessage>`

---

## 6. Order/Exchange Service (`/exchange`)

### `GET /exchange/books/{symbol}`
Retrieves the order book snapshot directly from the exchange engine.
*   **Response:** `BookSnapshot`
    ```json
    {
      "symbol": "string",
      "bestBid": "number",
      "bestAsk": "number",
      "bidSize": "number",
      "askSize": "number"
    }
    ```

### `POST /exchange/orders`
Submits an HTTP order request (used for tests/bots).
*   **Request Body (JSON):** `HttpOrderRequest`
*   **Response:** `List<TradeExecutedEvent>`

---

## 7. Audit Log Service (`/audit`)

### `GET /audit/events`
Retrieves recent audit logs. Supports pagination (`?page=0&size=50`) and filtering by topic (`?topic=xyz`).
*   **Response:** `Page<AuditEventRecord>`

---

## WebSockets: Live Market Data

The Market Data service provides live STOMP-over-WebSocket streams for real-time frontend updates.

### Connection Details
*   **Endpoint:** `ws://<api-gateway-host>:<port>/ws-market` (or `/ws-market` with SockJS fallback)
*   **Protocol:** STOMP

> [!IMPORTANT]
> The WebSocket endpoint does not require authentication to connect to the public market data topics.

### Subscribable Topics

1.  **Stock Price Updates**
    *   **Topic:** `/topic/stocks/{symbol}` (e.g., `/topic/stocks/AAPL`)
    *   **Payload:** `LiveStockPriceMessage` (contains `price`, `changePercent`, `volume`, etc.)

2.  **Candlestick Updates**
    *   **Topic:** `/topic/candles/{symbol}`
    *   **Payload:** `LiveCandleMessage` (contains `open`, `high`, `low`, `close`, `volume`, etc.)

3.  **Order Book Updates**
    *   **Topic:** `/topic/orderbook/{symbol}`
    *   **Payload:** `LiveOrderBookMessage` (contains lists of `bids` and `asks`)

4.  **Market Overview (Gainers/Losers)**
    *   **Topic:** `/topic/market/overview`
    *   **Payload:** `MarketOverviewMessage` (contains `topGainers`, `topLosers`, `marketStatus`)

5.  **Market Events**
    *   **Topic:** `/topic/market/events`
    *   **Payload:** `MarketEventMessage` (contains events like trade execution notifications)

---

## 8. Frontend Integration Guide

To integrate with the WebSocket backend, the frontend team should use the **STOMP protocol**, as the backend is built using Spring Boot's STOMP-over-WebSocket implementation. 

### Libraries to Install
The frontend team should install the following NPM packages:

```bash
npm install @stomp/stompjs
npm install sockjs-client
```
*   **`@stomp/stompjs`**: The standard library for connecting to a STOMP broker.
*   **`sockjs-client`**: The backend explicitly has a SockJS fallback configured (`.withSockJS()`). This is highly recommended to ensure the connection still works on older browsers or behind strict corporate firewalls that block raw WebSockets.

### Integration Example (React / Vanilla JS)

Here is a clean example of how to connect, subscribe to topics, and gracefully disconnect:

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 1. Initialize the STOMP client
const stompClient = new Client({
  // Using SockJS as the connection factory (recommended fallback method)
  webSocketFactory: () => new SockJS('http://localhost:8080/ws-market'),
  
  // Alternatively, if they want to use raw WebSockets without SockJS:
  // brokerURL: 'ws://localhost:8080/ws-market',

  debug: function (str) {
    console.log('STOMP: ' + str);
  },
  
  // Try to reconnect every 5 seconds if connection is lost
  reconnectDelay: 5000,
  
  // Heartbeat to keep connection alive (matches backend configuration)
  heartbeatIncoming: 10000,
  heartbeatOutgoing: 10000,
});

// 2. Define what happens when successfully connected
stompClient.onConnect = (frame) => {
  console.log('Connected to Market Data WebSocket!');

  // --- Example: Subscribe to a specific stock's live price ---
  const stockSymbol = 'AAPL';
  stompClient.subscribe(`/topic/stocks/${stockSymbol}`, (message) => {
    // Parse the JSON payload
    const livePriceData = JSON.parse(message.body);
    console.log(`Live Price for ${stockSymbol}:`, livePriceData);
    // TODO: Update frontend UI state here
  });

  // --- Example: Subscribe to the Market Overview ---
  stompClient.subscribe('/topic/market/overview', (message) => {
    const overviewData = JSON.parse(message.body);
    console.log('Market Overview:', overviewData);
  });
};

// 3. Define what happens on connection error
stompClient.onStompError = (frame) => {
  console.error('Broker reported error: ' + frame.headers['message']);
  console.error('Additional details: ' + frame.body);
};

// 4. Activate the connection
stompClient.activate();

// 5. Important: Gracefully disconnect when the component unmounts
// stompClient.deactivate();
```

### Key Takeaways:
*   **No Authentication Needed**: The market data websocket is publicly open. You do not need to pass a JWT token in the connection headers.
*   **JSON Parsing**: The payloads are delivered as text. The frontend must call `JSON.parse(message.body)` inside the subscription callbacks to access the data objects.
*   **Topic Dynamic Routing**: For stock-specific data (candles, order books, prices), you need to dynamically insert the symbol into the topic string (e.g., `/topic/candles/TSLA`).
