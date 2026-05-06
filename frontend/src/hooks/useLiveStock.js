import { useEffect, useState } from 'react';
import { usePlatform } from '../context/PlatformContext';

export function useLiveStock(symbol) {
  const { loadSymbol, history, trackPrice } = usePlatform();
  const [stock, setStock] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!symbol) return;

    let alive = true;

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

    return () => {
      alive = false;
      clearInterval(timer);
    };
  }, [symbol]);

  return {
    stock,
    loading,
    error,
    history: history[symbol] || [],
  };
}
