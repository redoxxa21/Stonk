const DEFAULT_WS_URL = import.meta.env.VITE_MARKET_WS_URL || `${window.location.origin.replace(/^http/, 'ws')}/ws-market/websocket`;

function buildFrame(command, headers = {}, body = '') {
  const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`);
  return `${command}\n${headerLines.join('\n')}\n\n${body}\0`;
}

class MarketSocket {
  constructor() {
    this.socket = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.listeners = new Set();
    this.reconnectTimer = null;
    this.connectPromise = null;
    this.subscriptionSeed = 0;
  }

  emitStatus(status) {
    this.listeners.forEach((listener) => listener(status));
  }

  onStatusChange(listener) {
    this.listeners.add(listener);
    listener(this.connected ? 'connected' : 'disconnected');
    return () => this.listeners.delete(listener);
  }

  async connect() {
    if (this.connected) return;
    if (this.connectPromise) return this.connectPromise;

    this.connectPromise = new Promise((resolve, reject) => {
      try {
        const socket = new WebSocket(DEFAULT_WS_URL);
        this.socket = socket;
        this.emitStatus('connecting');

        socket.onopen = () => {
          socket.send(
            buildFrame('CONNECT', {
              'accept-version': '1.2,1.1,1.0',
              'heart-beat': '10000,10000',
            }),
          );
        };

        socket.onmessage = (event) => {
          const frame = String(event.data || '');
          if (frame.startsWith('CONNECTED')) {
            this.connected = true;
            this.emitStatus('connected');
            this.resubscribeAll();
            resolve();
            return;
          }

          if (frame.startsWith('MESSAGE')) {
            const subscriptionId = this.readHeader(frame, 'subscription');
            const payload = this.readBody(frame);
            const subscription = this.subscriptions.get(subscriptionId);
            if (!subscription) return;
            try {
              subscription.callback(JSON.parse(payload));
            } catch {
              subscription.callback(payload);
            }
            return;
          }

          if (frame.startsWith('ERROR')) {
            this.emitStatus('error');
            reject(new Error('Market websocket error'));
          }
        };

        socket.onclose = () => {
          const hadConnection = this.connected;
          this.connected = false;
          this.connectPromise = null;
          this.emitStatus('disconnected');
          if (hadConnection || this.subscriptions.size) {
            this.queueReconnect();
          }
        };

        socket.onerror = () => {
          this.emitStatus('error');
        };
      } catch (error) {
        this.connectPromise = null;
        reject(error);
      }
    });

    return this.connectPromise;
  }

  queueReconnect() {
    if (this.reconnectTimer) return;
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null;
      this.connect().catch(() => {});
    }, 5000);
  }

  readHeader(frame, key) {
    const match = frame.match(new RegExp(`^${key}:(.+)$`, 'm'));
    return match ? match[1].trim() : '';
  }

  readBody(frame) {
    const split = frame.indexOf('\n\n');
    if (split === -1) return '';
    return frame.slice(split + 2).replace(/\0$/, '');
  }

  resubscribeAll() {
    this.subscriptions.forEach((subscription, id) => {
      this.socket?.send(
        buildFrame('SUBSCRIBE', {
          id,
          destination: subscription.topic,
        }),
      );
    });
  }

  subscribe(topic, callback) {
    const id = `sub-${++this.subscriptionSeed}`;
    this.subscriptions.set(id, { topic, callback });
    this.connect()
      .then(() => {
        this.socket?.send(
          buildFrame('SUBSCRIBE', {
            id,
            destination: topic,
          }),
        );
      })
      .catch(() => {});

    return () => {
      this.subscriptions.delete(id);
      if (this.connected) {
        this.socket?.send(buildFrame('UNSUBSCRIBE', { id }));
      }
    };
  }
}

export const marketSocket = new MarketSocket();
