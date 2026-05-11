# Stonks Frontend

React + Tailwind frontend for the full Stonks microservices platform.

## Run

```bash
npm install
npm run dev
```

Default gateway target is `http://localhost:8080` via Vite proxy.
By default, the proxy targets `http://localhost:8081` to match this repo's Docker Compose gateway mapping, and it rewrites `/api/*` requests to `/*` before forwarding.

## Archived prototype

The earlier wallet-only static prototype is preserved at:

`backend/wallet-service/frontend`

It is not deleted and can be compared against this implementation.
