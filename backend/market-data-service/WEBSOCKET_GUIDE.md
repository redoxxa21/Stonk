# Stonk Market Data Service — WebSocket & API Guide

Complete reference for all REST APIs, WebSocket topics, and frontend integration examples.

---

## Table of Contents

- [Service Info](#service-info)
- [REST API Reference](#rest-api-reference)
- [WebSocket Reference](#websocket-reference)
- [Frontend Connection Examples](#frontend-connection-examples)
- [Docker & Gateway Routing](#docker--gateway-routing)

---

## Service Info

| Property        | Value                          |
|-----------------|--------------------------------|
| Service name    | `market-data-service`          |
| Default port    | `2650`                         |
| WebSocket path  | `/ws-market`                   |
| Protocol        | STOMP over WebSocket + SockJS  |
| Auth (REST)     | JWT Bearer token               |
| Auth (WebSocket)| Public (no auth required)      |

---

## REST API Reference

All REST endpoints require a valid JWT token in the `Authorization: Bearer <token>` header.

---

### 1. List All Stocks

```
GET /market/stocks
```

**Response** `200 OK`:
```json
[
  {
    "symbol": "AAPL",
    "name": "Apple Inc.",
    "currentPrice": 189.30,
    "previousClose": 187.00,
    "changePercent": 1.2299,
    "lastUpdated": "2026-05-08T14:30:00",
    "cumulativeVolume": 125000,
    "realizedVolatility": 18.4500,
    "liquidityScore": 6775.0678
  }
]
```

---

### 2. Get Single Stock

```
GET /market/stocks/{symbol}
```

**Path Parameters:**
| Param  | Type   | Description          |
|--------|--------|----------------------|
| symbol | String | Stock ticker (e.g. AAPL) |

**Response** `200 OK`:
```json
{
  "symbol": "AAPL",
  "name": "Apple Inc.",
  "currentPrice": 189.30,
  "previousClose": 187.00,
  "changePercent": 1.2299,
  "lastUpdated": "2026-05-08T14:30:00",
  "cumulativeVolume": 125000,
  "realizedVolatility": 18.4500,
  "liquidityScore": 6775.0678
}
```

**Error** `404 Not Found`:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Stock not found with symbol: XYZ",
  "path": "/market/stocks/XYZ",
  "timestamp": "2026-05-08T14:30:00"
}
```

---

### 3. Get Current Candle

```
GET /market/stocks/{symbol}/candles
```

**Response** `200 OK`:
```json
{
  "symbol": "AAPL",
  "timeframe": "1m",
  "open": 188.50,
  "high": 190.00,
  "low": 188.20,
  "close": 189.30,
  "volume": 3200,
  "minuteEpoch": 28583383
}
```

---

### 4. Get Order Book

```
GET /market/stocks/{symbol}/orderbook
```

**Response** `200 OK`:
```json
{
  "symbol": "AAPL",
  "bids": [
    { "price": 189.25, "quantity": 130 },
    { "price": 189.20, "quantity": 110 },
    { "price": 189.15, "quantity": 90 },
    { "price": 189.10, "quantity": 70 },
    { "price": 189.05, "quantity": 50 }
  ],
  "asks": [
    { "price": 189.35, "quantity": 130 },
    { "price": 189.40, "quantity": 110 },
    { "price": 189.45, "quantity": 90 },
    { "price": 189.50, "quantity": 70 },
    { "price": 189.55, "quantity": 50 }
  ],
  "timestamp": 1715180000
}
```

---

### 5. Get Market Overview

```
GET /market/overview
```

**Response** `200 OK`:
```json
{
  "topGainers": [
    { "symbol": "NVDA", "name": "NVIDIA Corporation", "price": 900.50, "changePercent": 3.2100 },
    { "symbol": "AAPL", "name": "Apple Inc.", "price": 189.30, "changePercent": 1.2299 }
  ],
  "topLosers": [
    { "symbol": "INTC", "name": "Intel Corporation", "price": 30.10, "changePercent": -4.2900 },
    { "symbol": "TSLA", "name": "Tesla Inc.", "price": 170.50, "changePercent": -2.1500 }
  ],
  "marketStatus": "OPEN",
  "timestamp": 1715180000
}
```

---

### 6. Get Market Events

```
GET /market/events?limit=20
```

**Query Parameters:**
| Param | Type | Default | Description              |
|-------|------|---------|--------------------------|
| limit | int  | 20      | Number of events (1-100) |

**Response** `200 OK`:
```json
[
  {
    "eventType": "PANIC_SELLING",
    "symbol": "TSLA",
    "severity": 0.8,
    "message": "High volatility detected",
    "timestamp": 1715180000
  }
]
```

---

## WebSocket Reference

### Connection

| Property          | Value                                              |
|-------------------|----------------------------------------------------|
| Endpoint          | `/ws-market`                                       |
| Direct URL        | `ws://localhost:2650/ws-market`                     |
| SockJS URL        | `http://localhost:2650/ws-market`                   |
| Via Gateway       | `ws://localhost:8080/ws-market` or `ws://localhost:8081/ws-market` (Docker) |
| Protocol          | STOMP 1.2                                          |
| Heartbeat         | 10s server → client, 10s client → server           |
| Authentication    | None required (public market data)                 |

---

### Topic 1: Live Stock Price

**Subscribe:** `/topic/stocks/{symbol}`

**Example:** `/topic/stocks/AAPL`

**Payload:**
```json
{
  "symbol": "AAPL",
  "price": 182.45,
  "changePercent": 1.5000,
  "volume": 50000,
  "realizedVolatility": 18.4500,
  "liquidityScore": 2712.1200,
  "timestamp": 1715000000
}
```

**Triggered by:** Every `trade-executed` Kafka event for this symbol.

---

### Topic 2: Live Candles

**Subscribe:** `/topic/candles/{symbol}`

**Example:** `/topic/candles/AAPL`

**Payload:**
```json
{
  "symbol": "AAPL",
  "timeframe": "1m",
  "open": 180.00,
  "high": 183.00,
  "low": 179.00,
  "close": 182.00,
  "volume": 12000,
  "minuteEpoch": 28583333
}
```

**Triggered by:** Every `trade-executed` Kafka event for this symbol.

---

### Topic 3: Live Order Book

**Subscribe:** `/topic/orderbook/{symbol}`

**Example:** `/topic/orderbook/AAPL`

**Payload:**
```json
{
  "symbol": "AAPL",
  "bids": [
    { "price": 182.40, "quantity": 130 },
    { "price": 182.35, "quantity": 110 },
    { "price": 182.30, "quantity": 90 },
    { "price": 182.25, "quantity": 70 },
    { "price": 182.20, "quantity": 50 }
  ],
  "asks": [
    { "price": 182.50, "quantity": 130 },
    { "price": 182.55, "quantity": 110 },
    { "price": 182.60, "quantity": 90 },
    { "price": 182.65, "quantity": 70 },
    { "price": 182.70, "quantity": 50 }
  ],
  "timestamp": 1715000000
}
```

**Triggered by:** Every `trade-executed` Kafka event for this symbol.

---

### Topic 4: Market Overview

**Subscribe:** `/topic/market/overview`

**Payload:**
```json
{
  "topGainers": [
    { "symbol": "NVDA", "name": "NVIDIA Corporation", "price": 900.50, "changePercent": 3.21 }
  ],
  "topLosers": [
    { "symbol": "INTC", "name": "Intel Corporation", "price": 30.10, "changePercent": -4.29 }
  ],
  "marketStatus": "OPEN",
  "timestamp": 1715000000
}
```

**Triggered by:** Every `trade-executed` Kafka event (recalculates gainers/losers).

---

### Topic 5: Market Events

**Subscribe:** `/topic/market/events`

**Payload:**
```json
{
  "eventType": "PANIC_SELLING",
  "symbol": "AAPL",
  "severity": 0.8,
  "message": "High volatility detected",
  "timestamp": 1715000000
}
```

**Triggered by:** `market-event-created` and `volatility-changed` Kafka events.

**Possible event types:**
- `PANIC_SELLING`
- `BULL_RUN`
- `FLASH_CRASH`
- `VOLATILITY_CHANGE`
- `HIGH_VOLUME`
- `CIRCUIT_BREAKER`

---

## Frontend Connection Examples

### 1. React + SockJS + STOMP

```bash
npm install sockjs-client @stomp/stompjs
```

```jsx
// hooks/useMarketSocket.js
import { useEffect, useRef, useCallback, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'http://localhost:2650/ws-market';

export function useMarketSocket() {
  const clientRef = useRef(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (msg) => console.log('[STOMP]', msg),
      onConnect: () => {
        console.log('Connected to Stonk Market WebSocket');
        setConnected(true);
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const subscribe = useCallback((topic, callback) => {
    const client = clientRef.current;
    if (!client || !client.connected) {
      console.warn('Not connected yet, queuing subscription');
      // Retry after connection
      const interval = setInterval(() => {
        if (client?.connected) {
          clearInterval(interval);
          client.subscribe(topic, (message) => {
            callback(JSON.parse(message.body));
          });
        }
      }, 500);
      return () => clearInterval(interval);
    }

    const subscription = client.subscribe(topic, (message) => {
      callback(JSON.parse(message.body));
    });
    return () => subscription.unsubscribe();
  }, []);

  return { connected, subscribe };
}

// ─── Usage in a component ─────────────────────────────────

// components/StockTicker.jsx
import React, { useState, useEffect } from 'react';
import { useMarketSocket } from '../hooks/useMarketSocket';

export function StockTicker({ symbol }) {
  const { connected, subscribe } = useMarketSocket();
  const [price, setPrice] = useState(null);

  useEffect(() => {
    const unsub = subscribe(`/topic/stocks/${symbol}`, (data) => {
      setPrice(data);
    });
    return unsub;
  }, [symbol, subscribe]);

  if (!connected) return <div>Connecting...</div>;
  if (!price) return <div>Waiting for {symbol} data...</div>;

  return (
    <div>
      <h2>{price.symbol}</h2>
      <p>Price: ${price.price}</p>
      <p>Change: {price.changePercent}%</p>
      <p>Volume: {price.volume.toLocaleString()}</p>
    </div>
  );
}
```

---

### 2. Vanilla JavaScript

```html
<!DOCTYPE html>
<html>
<head>
  <title>Stonk Market Feed</title>
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js"></script>
</head>
<body>
  <h1>Live Market Feed</h1>
  <div id="output"></div>

  <script>
    const client = new StompJs.Client({
      webSocketFactory: () => new SockJS('http://localhost:2650/ws-market'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (msg) => console.log('[STOMP]', msg),
    });

    client.onConnect = () => {
      console.log('✅ Connected to Stonk Market WebSocket');

      // Subscribe to AAPL stock price
      client.subscribe('/topic/stocks/AAPL', (message) => {
        const data = JSON.parse(message.body);
        console.log('📈 AAPL:', data);
        document.getElementById('output').innerHTML += `
          <p><b>${data.symbol}</b>: $${data.price} (${data.changePercent}%)</p>
        `;
      });

      // Subscribe to market events
      client.subscribe('/topic/market/events', (message) => {
        const event = JSON.parse(message.body);
        console.log('🚨 Event:', event);
      });

      // Subscribe to market overview
      client.subscribe('/topic/market/overview', (message) => {
        const overview = JSON.parse(message.body);
        console.log('📊 Overview:', overview);
      });
    };

    client.onStompError = (frame) => {
      console.error('❌ STOMP error:', frame.headers['message']);
    };

    client.activate();
  </script>
</body>
</html>
```

---

### 3. Next.js Reusable Hook

```typescript
// lib/useStompClient.ts
'use client';

import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:2650/ws-market';

interface UseStompClientOptions {
  onConnect?: () => void;
  onDisconnect?: () => void;
}

export function useStompClient(options?: UseStompClientOptions) {
  const clientRef = useRef<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setIsConnected(true);
        options?.onConnect?.();
      },
      onDisconnect: () => {
        setIsConnected(false);
        options?.onDisconnect?.();
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const subscribe = useCallback(
    <T = any>(topic: string, callback: (data: T) => void) => {
      const client = clientRef.current;
      if (!client) return () => {};

      if (client.connected) {
        const sub = client.subscribe(topic, (msg: IMessage) => {
          callback(JSON.parse(msg.body));
        });
        return () => sub.unsubscribe();
      }

      // Wait for connection
      let sub: any = null;
      const onConnect = client.onConnect;
      client.onConnect = (frame) => {
        onConnect?.(frame);
        sub = client.subscribe(topic, (msg: IMessage) => {
          callback(JSON.parse(msg.body));
        });
      };
      return () => sub?.unsubscribe();
    },
    []
  );

  return { isConnected, subscribe, client: clientRef };
}

// ─── Usage in a Next.js page ──────────────────────────────

// app/market/page.tsx
'use client';

import { useEffect, useState } from 'react';
import { useStompClient } from '@/lib/useStompClient';

interface StockPrice {
  symbol: string;
  price: number;
  changePercent: number;
  volume: number;
  timestamp: number;
}

export default function MarketPage() {
  const { isConnected, subscribe } = useStompClient();
  const [prices, setPrices] = useState<Record<string, StockPrice>>({});

  useEffect(() => {
    const symbols = ['AAPL', 'GOOGL', 'MSFT', 'TSLA', 'NVDA'];
    const unsubscribes = symbols.map((sym) =>
      subscribe<StockPrice>(`/topic/stocks/${sym}`, (data) => {
        setPrices((prev) => ({ ...prev, [data.symbol]: data }));
      })
    );

    return () => unsubscribes.forEach((unsub) => unsub());
  }, [subscribe]);

  return (
    <div>
      <h1>Stonk Market {isConnected ? '🟢' : '🔴'}</h1>
      {Object.values(prices).map((p) => (
        <div key={p.symbol}>
          <strong>{p.symbol}</strong>: ${p.price} ({p.changePercent}%)
        </div>
      ))}
    </div>
  );
}
```

---

## Docker & Gateway Routing

### Direct Connection (Development)

Connect directly to the market-data-service:

```
WebSocket: ws://localhost:2650/ws-market
SockJS:    http://localhost:2650/ws-market
REST:      http://localhost:2650/market/stocks
```

### Via API Gateway (Production / Docker)

The API Gateway routes WebSocket traffic transparently:

```yaml
# api-gateway application.yaml
routes:
  - id: market-data-ws
    uri: lb:ws://market-data-service
    predicates:
      - Path=/ws-market/**

  - id: market-data-service
    uri: lb://market-data-service
    predicates:
      - Path=/market/**
```

Connect through the gateway:

```
WebSocket: ws://localhost:8080/ws-market     (local gateway)
WebSocket: ws://localhost:8081/ws-market     (Docker gateway, port 8081→8080)
SockJS:    http://localhost:8081/ws-market
REST:      http://localhost:8081/market/stocks
```

### Secure WebSocket (wss://)

For production with TLS, place a reverse proxy (nginx, Cloudflare, etc.) in front:

```
wss://api.stonk.io/ws-market  →  ws://api-gateway:8080/ws-market
```

Nginx example:

```nginx
location /ws-market {
    proxy_pass http://api-gateway:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_read_timeout 86400;
}
```

---

## Architecture Flow

```
┌─────────────┐    trade-executed     ┌───────────────────────┐
│   Trading   │ ──────────────────▶  │  Market Data Service  │
│   Service   │    (Kafka topic)      │                       │
└─────────────┘                       │  1. Update DB (Stock) │
                                      │  2. Update Candle     │
┌─────────────┐    market-event       │  3. Update Volatility │
│  Simulation │ ──────────────────▶  │  4. Update OrderBook  │
│   Service   │    (Kafka topic)      │  5. Publish Kafka     │
└─────────────┘                       │  6. Broadcast WS ─────┼──▶ /topic/stocks/{sym}
                                      │                       │──▶ /topic/candles/{sym}
                                      │                       │──▶ /topic/orderbook/{sym}
                                      │                       │──▶ /topic/market/overview
                                      │                       │──▶ /topic/market/events
                                      └───────────────────────┘
                                                 ▲
                                                 │ STOMP/SockJS
                                                 ▼
                                      ┌───────────────────────┐
                                      │   Frontend Clients    │
                                      │  (React / Next / JS)  │
                                      └───────────────────────┘
```
