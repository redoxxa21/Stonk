import { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../lib/apiClient';

export function usePortfolioHoldings() {
  const { user } = useAuth();
  const [holdings, setHoldings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user?.id) {
      setHoldings([]);
      setLoading(false);
      setError('');
      return;
    }

    let alive = true;

    async function loadHoldings() {
      setLoading(true);
      setError('');
      try {
        const data = await apiClient.get(`/portfolio/${user.id}`);
        if (!alive) return;
        setHoldings(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!alive) return;
        setError(err.message);
        setHoldings([]);
      } finally {
        if (alive) setLoading(false);
      }
    }

    loadHoldings();

    return () => {
      alive = false;
    };
  }, [user?.id]);

  const holdingsBySymbol = useMemo(() => {
    return holdings.reduce((map, holding) => {
      const symbol = String(holding.symbol || '').toUpperCase();
      if (!symbol) return map;
      const quantity = Number(holding.quantity || 0);
      map.set(symbol, (map.get(symbol) || 0) + quantity);
      return map;
    }, new Map());
  }, [holdings]);

  const getAvailableQuantity = (symbol) => {
    const normalizedSymbol = String(symbol || '').toUpperCase();
    return holdingsBySymbol.get(normalizedSymbol) || 0;
  };

  const canSell = (symbol, quantity) => {
    const requestedQuantity = Number(quantity);
    if (!normalizedSymbol(symbol) || !Number.isFinite(requestedQuantity) || requestedQuantity <= 0) {
      return false;
    }
    return getAvailableQuantity(symbol) >= requestedQuantity;
  };

  return {
    holdings,
    loading,
    error,
    refresh: () => {
      if (!user?.id) return Promise.resolve();
      return apiClient.get(`/portfolio/${user.id}`).then((data) => {
        setHoldings(Array.isArray(data) ? data : []);
      });
    },
    getAvailableQuantity,
    canSell,
  };
}

function normalizedSymbol(symbol) {
  return String(symbol || '').trim();
}