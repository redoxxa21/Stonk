import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, RefreshCw, ShoppingCart, ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import StatCard from '../components/common/StatCard';
import Badge from '../components/common/Badge';
import Sparkline from '../components/common/Sparkline';
import DataTable from '../components/common/DataTable';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { usePlatform } from '../context/PlatformContext';
import { useLiveStock } from '../hooks/useLiveStock';
import { apiClient } from '../lib/apiClient';
import { formatMoney, deltaClass } from '../lib/format';

export default function MarketSymbolPage() {
  const { symbol = '' } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { history, socketStatus } = usePlatform();
  const { stock, loading, error } = useLiveStock(symbol);
  const [candle, setCandle] = useState(null);
  const [orderbook, setOrderbook] = useState(null);
  const [orderType, setOrderType] = useState('BUY');
  const [quantity, setQuantity] = useState(1);
  const [orderPrice, setOrderPrice] = useState('');
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (stock?.currentPrice && !orderPrice) {
      setOrderPrice(String(stock.currentPrice));
    }
  }, [stock?.currentPrice]);

  useEffect(() => {
    let alive = true;
    async function loadSnapshots() {
      try {
        const [candleData, orderbookData] = await Promise.all([
          apiClient.get(`/market/stocks/${symbol}/candles`).catch(() => null),
          apiClient.get(`/market/stocks/${symbol}/orderbook`).catch(() => null),
        ]);
        if (!alive) return;
        setCandle(candleData);
        setOrderbook(orderbookData);
      } catch {
        if (!alive) return;
      }
    }

    if (symbol) {
      loadSnapshots();
    }

    return () => {
      alive = false;
    };
  }, [symbol]);

  const series = history[symbol]?.map((point) => point.price) || [];
  const changeClass = deltaClass(stock?.changePercent);

  async function submitOrder() {
    if (!user?.id || !stock) return;
    setSubmitting(true);
    setMessage('');
    try {
      await apiClient.post('/exchange/orders', {
        userId: user.id,
        symbol: stock.symbol,
        type: orderType,
        quantity: Number(quantity),
        price: Number(orderPrice),
      });
      setMessage('Order submitted successfully.');
    } catch (err) {
      setMessage(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function submitTrade(type) {
    if (!user?.id || !stock) return;
    setSubmitting(true);
    setMessage('');
    try {
      await apiClient.post(`/trades/${type.toLowerCase()}`, {
        userId: user.id,
        symbol: stock.symbol,
        quantity: Number(quantity),
      });
      setMessage(`${type} trade submitted successfully.`);
    } catch (err) {
      setMessage(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading && !stock) return <LoadingState label="Loading live stock data..." />;
  if (error && !stock) return <ErrorState error={error} onRetry={() => window.location.reload()} />;
  if (!stock) return null;

  const recent = (history[symbol] || []).slice(-8).reverse();

  return (
    <div className="space-y-6">
      <PageHeader
        title={stock.symbol}
        subtitle={stock.name}
        actions={[
          <Badge key="feed" tone={socketStatus === 'connected' ? 'success' : socketStatus === 'connecting' ? 'warning' : 'neutral'}>
            Live {socketStatus}
          </Badge>,
          <button key="back" type="button" onClick={() => navigate('/market')} className="rh-button-secondary">
            <ArrowLeft className="h-4 w-4" />
            Back
          </button>,
          <button key="reload" type="button" onClick={() => navigate(0)} className="rh-button-primary">
            <RefreshCw className="h-4 w-4" />
            Reload
          </button>,
        ]}
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Current price" value={stock.currentPrice} note="Live" tone="success" />
        <StatCard label="Previous close" value={stock.previousClose} note="Reference" tone="neutral" />
        <StatCard label="Change" value={`${Number(stock.changePercent).toFixed(2)}%`} note={Number(stock.changePercent) >= 0 ? 'Up' : 'Down'} tone={Number(stock.changePercent) >= 0 ? 'success' : 'danger'} />
        <StatCard label="Last updated" value={stock.lastUpdated ? new Date(stock.lastUpdated).toLocaleTimeString() : '-'} note="Latest tick" tone="accent" />
      </div>

      <div className="grid gap-6 xl:grid-cols-[1.35fr_0.65fr]">
        <SectionCard title="Live chart" subtitle="Track recent price movement for this symbol.">
          <div className="rounded-[24px] border border-line bg-panel2 p-4">
            <div className="flex items-center justify-between gap-3">
              <div>
                <div className="text-xs uppercase tracking-wide text-muted">Trend</div>
                <div className="mt-1 text-2xl font-semibold">{formatMoney(stock.currentPrice)}</div>
              </div>
              <Badge tone={Number(stock.changePercent) >= 0 ? 'success' : 'danger'}>{Number(stock.changePercent).toFixed(2)}%</Badge>
            </div>
            <div className="mt-4 text-accent">
              <Sparkline values={series} />
            </div>
            <div className="mt-4 grid gap-2 md:grid-cols-4">
              {recent.map((point) => (
                <div key={point.time} className="rounded-xl border border-line bg-[#1a1f0f] px-3 py-2 text-xs text-muted">
                  <div>{new Date(point.time).toLocaleTimeString()}</div>
                  <div className="mt-1 text-sm text-text">{formatMoney(point.price)}</div>
                </div>
              ))}
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Trade ticket" subtitle="Create an order or place a trade for this symbol.">
          <div className="space-y-4">
            <div className="rounded-2xl border border-line bg-[#242412] p-4 text-sm text-muted">
              Review the latest price, choose your order details, and submit when you are ready to trade.
            </div>
            <label className="block text-sm text-muted">
              Order type
              <select
                value={orderType}
                onChange={(event) => setOrderType(event.target.value)}
                className="rh-select"
              >
                <option value="BUY">BUY</option>
                <option value="SELL">SELL</option>
              </select>
            </label>
            <label className="block text-sm text-muted">
              Quantity
              <input
                type="number"
                min="1"
                step="1"
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
                className="rh-input"
              />
            </label>
            <label className="block text-sm text-muted">
              Price
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={orderPrice}
                onChange={(event) => setOrderPrice(event.target.value)}
                className="rh-input"
              />
            </label>
            <div className="grid grid-cols-2 gap-2">
              <button onClick={submitOrder} disabled={submitting} type="button" className="rh-button-ghost">
                <ShoppingCart className="h-4 w-4" />
                Create order
              </button>
              <button onClick={() => submitTrade('BUY')} disabled={submitting} type="button" className="rh-button-positive">
                <ArrowUpRight className="h-4 w-4" />
                Trade buy
              </button>
              <button onClick={() => submitTrade('SELL')} disabled={submitting} type="button" className="rh-button-danger">
                <ArrowDownLeft className="h-4 w-4" />
                Trade sell
              </button>
              <button type="button" onClick={() => setOrderPrice(String(stock.currentPrice))} className="rh-button-secondary">
                Use live price
              </button>
            </div>
            {message ? <div className="rounded-2xl border border-line bg-[#1a1f0f] px-3 py-2 text-sm text-text">{message}</div> : null}
          </div>
        </SectionCard>
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <SectionCard title="Candle snapshot" subtitle="Recent open, high, low, close, and volume data.">
          <DataTable
            rowKey={(row) => row.label}
            rows={[
              { label: 'Open', value: formatMoney(candle?.open) },
              { label: 'High', value: formatMoney(candle?.high) },
              { label: 'Low', value: formatMoney(candle?.low) },
              { label: 'Close', value: formatMoney(candle?.close) },
              { label: 'Volume', value: candle?.volume ?? '-' },
            ]}
            columns={[
              { key: 'label', header: 'Field' },
              { key: 'value', header: 'Value' },
            ]}
          />
        </SectionCard>

        <SectionCard title="Order book" subtitle="Recent bid and ask levels for this symbol.">
          <DataTable
            rowKey={(row) => row.key}
            rows={[
              ...(orderbook?.bids || []).slice(0, 3).map((entry, index) => ({
                key: `bid-${index}`,
                side: 'Bid',
                price: formatMoney(entry.price),
                quantity: entry.quantity,
              })),
              ...(orderbook?.asks || []).slice(0, 3).map((entry, index) => ({
                key: `ask-${index}`,
                side: 'Ask',
                price: formatMoney(entry.price),
                quantity: entry.quantity,
              })),
            ]}
            emptyTitle="No order book snapshot"
            emptyDescription="Order book data is not available right now."
            columns={[
              { key: 'side', header: 'Side' },
              { key: 'price', header: 'Price' },
              { key: 'quantity', header: 'Quantity' },
            ]}
          />
        </SectionCard>
      </div>

      <SectionCard title="Symbol details" subtitle="Key reference details for the selected symbol.">
        <DataTable
          rowKey={(row) => row.label}
          rows={[
            { label: 'Symbol', value: stock.symbol },
            { label: 'Name', value: stock.name },
            { label: 'Current price', value: formatMoney(stock.currentPrice) },
            { label: 'Previous close', value: formatMoney(stock.previousClose) },
            { label: 'Change percent', value: `${Number(stock.changePercent).toFixed(2)}%`, tone: changeClass },
            { label: 'Volume', value: stock.cumulativeVolume ?? '-' },
            { label: 'Liquidity score', value: stock.liquidityScore ?? '-' },
          ]}
          columns={[
            { key: 'label', header: 'Field' },
            {
              key: 'value',
              header: 'Value',
              render: (row) => <span className={row.tone || ''}>{row.value}</span>,
            },
          ]}
        />
      </SectionCard>
    </div>
  );
}
