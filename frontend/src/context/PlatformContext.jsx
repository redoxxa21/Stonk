import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiClient } from '../lib/apiClient';
import { marketSocket } from '../lib/marketSocket';
import { useAuth } from './AuthContext';

const PlatformContext = createContext(null);

export function PlatformProvider({ children }) {
  const [stocks, setStocks] = useState([]);
  const [selectedSymbol, setSelectedSymbol] = useState('');
  const [history, setHistory] = useState({});
  const [overview, setOverview] = useState(null);
  const [marketEvents, setMarketEvents] = useState([]);
  const [socketStatus, setSocketStatus] = useState('disconnected');
  const [marketLoading, setMarketLoading] = useState(false);
  const [marketError, setMarketError] = useState('');

  const refreshMarket = useCallback(async () => {
    setMarketLoading(true);
    setMarketError('');
    try {
      const [data, overviewData, eventsData] = await Promise.all([
        apiClient.get('/market/stocks'),
        apiClient.get('/market/overview').catch(() => null),
        apiClient.get('/market/events').catch(() => []),
      ]);
      setStocks(Array.isArray(data) ? data : []);
      setOverview(overviewData);
      setMarketEvents(Array.isArray(eventsData) ? eventsData.slice(0, 8) : []);
      if (!selectedSymbol && data?.length) {
        setSelectedSymbol(data[0].symbol);
      }
      return data;
    } catch (error) {
      setMarketError(error.message);
      throw error;
    } finally {
      setMarketLoading(false);
    }
  }, [selectedSymbol]);

  const loadSymbol = useCallback(async (symbol) => {
    const stock = await apiClient.get(`/market/stocks/${encodeURIComponent(symbol)}`);
    setSelectedSymbol(stock.symbol);
    setHistory((current) => {
      const next = current[stock.symbol] ? [...current[stock.symbol]] : [];
      const price = Number(stock.currentPrice);
      next.push({ time: Date.now(), price });
      return { ...current, [stock.symbol]: next.slice(-24) };
    });
    return stock;
  }, []);

  const trackPrice = useCallback((symbol, price) => {
    setHistory((current) => {
      const next = current[symbol] ? [...current[symbol]] : [];
      next.push({ time: Date.now(), price: Number(price) });
      return { ...current, [symbol]: next.slice(-24) };
    });
  }, []);

  useEffect(() => {
    const unsubscribeStatus = marketSocket.onStatusChange(setSocketStatus);
    const unsubscribeOverview = marketSocket.subscribe('/topic/market/overview', (message) => {
      setOverview(message);
    });
    const unsubscribeEvents = marketSocket.subscribe('/topic/market/events', (message) => {
      setMarketEvents((current) => [message, ...current].slice(0, 8));
    });

    return () => {
      unsubscribeStatus();
      unsubscribeOverview();
      unsubscribeEvents();
    };
  }, []);

  const { session } = useAuth();

  useEffect(() => {
    if (!session?.token) return;

    let alive = true;
    // refreshMarket().catch(() => {});
    const timer = setInterval(() => {
      if (alive) refreshMarket().catch(() => {});
    }, 30000);
    return () => {
      alive = false;
      clearInterval(timer);
    };
  }, [refreshMarket, session?.token]);

  const value = useMemo(
    () => ({
      stocks,
      selectedSymbol,
      history,
      overview,
      marketEvents,
      socketStatus,
      marketLoading,
      marketError,
      refreshMarket,
      loadSymbol,
      trackPrice,
      setSelectedSymbol,
    }),
    [stocks, selectedSymbol, history, overview, marketEvents, socketStatus, marketLoading, marketError],
  );

  return <PlatformContext.Provider value={value}>{children}</PlatformContext.Provider>;
}

export function usePlatform() {
  const value = useContext(PlatformContext);
  if (!value) {
    throw new Error('usePlatform must be used inside PlatformProvider');
  }
  return value;
}
