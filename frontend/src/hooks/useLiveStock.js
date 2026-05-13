import { useEffect, useState } from 'react';
import { usePlatform } from '../context/PlatformContext';
import { marketSocket } from '../lib/marketSocket';

export function useLiveStock(symbol) {
  const { stocks, loadSymbol, history, trackPrice } = usePlatform();
  const cachedStock = stocks.find((item) => String(item.symbol || '').toUpperCase() === String(symbol || '').toUpperCase()) || null;
  const [stock, setStock] = useState(cachedStock);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!symbol) return;

    let alive = true;

    setStock((current) => {
      if (current && String(current.symbol || '').toUpperCase() === String(symbol).toUpperCase()) {
        return current;
      }
      return cachedStock;
    });

    async function sync() {
      setLoading(true);
      setError('');
      try {
        const current = await loadSymbol(symbol);
        if (!alive) return;
        setStock(current);
        trackPrice(current.symbol, current.currentPrice);
      } catch (err) {
        if (!alive) return;
        setError(err.message);
      } finally {
        if (alive) setLoading(false);
      }
    }

    sync();
    const timer = setInterval(sync, 10000);
    const unsubscribe = marketSocket.subscribe(`/topic/stocks/${symbol}`, (message) => {
      if (!alive) return;
      setStock((current) => {
        const next = {
          ...(current || {}),
          symbol,
          currentPrice: message.price,
          changePercent: message.changePercent,
          cumulativeVolume: message.volume,
          realizedVolatility: message.realizedVolatility,
          liquidityScore: message.liquidityScore,
          lastUpdated: message.timestamp ? new Date(message.timestamp).toISOString() : new Date().toISOString(),
        };
        return next;
      });
      trackPrice(symbol, message.price);
    });

    return () => {
      alive = false;
      clearInterval(timer);
      unsubscribe();
    };
  }, [symbol, cachedStock, loadSymbol, trackPrice]);

  return {
    stock,
    loading,
    error,
    history: history[symbol] || [],
  };
}
