import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowUpRight, WalletCards, Layers3, ListOrdered, ArrowUpDown, CandlestickChart, RefreshCw } from 'lucide-react';
import PageHeader from '../components/common/PageHeader';
import StatCard from '../components/common/StatCard';
import SectionCard from '../components/common/SectionCard';
import Badge from '../components/common/Badge';
import DataTable from '../components/common/DataTable';
import { LoadingState, ErrorState, EmptyState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { usePlatform } from '../context/PlatformContext';
import { apiClient } from '../lib/apiClient';
import { formatMoney, formatNumber, formatDateTime, deltaClass } from '../lib/format';

export default function DashboardPage() {
  const navigate = useNavigate();
  const { user, refreshUser } = useAuth();
  const { stocks, refreshMarket } = usePlatform();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [summary, setSummary] = useState({
    wallet: null,
    portfolio: [],
    orders: [],
    trades: [],
  });

  useEffect(() => {
    let alive = true;

    async function load() {
      if (!user?.id) return;
      setLoading(true);
      setError('');
      try {
        await refreshUser();
        const [wallet, portfolio, orders, trades] = await Promise.all([
          apiClient.get(`/wallet/${user.id}`).catch(() => null),
          apiClient.get(`/portfolio/${user.id}`).catch(() => []),
          apiClient.get(`/orders/${user.id}`).catch(() => []),
          apiClient.get(`/trades/${user.id}`).catch(() => []),
        ]);

        if (!alive) return;
        setSummary({
          wallet,
          portfolio: Array.isArray(portfolio) ? portfolio : [],
          orders: Array.isArray(orders) ? orders : [],
          trades: Array.isArray(trades) ? trades : [],
        });
      } catch (err) {
        if (alive) setError(err.message);
      } finally {
        if (alive) setLoading(false);
      }
    }

    load();
    return () => {
      alive = false;
    };
  }, [user?.id]);

  const portfolioValue = useMemo(() => {
    const priceBySymbol = new Map(stocks.map((stock) => [stock.symbol, Number(stock.currentPrice)]));
    return summary.portfolio.reduce((total, holding) => {
      const currentPrice = priceBySymbol.get(holding.symbol) ?? Number(holding.averagePrice || 0);
      return total + currentPrice * Number(holding.quantity || 0);
    }, 0);
  }, [summary.portfolio, stocks]);

  const investedValue = useMemo(
    () => summary.portfolio.reduce((total, holding) => total + Number(holding.totalInvested || 0), 0),
    [summary.portfolio],
  );

  if (loading) return <LoadingState label="Loading dashboard..." />;
  if (error) return <ErrorState error={error} onRetry={refreshMarket} />;

  const topStocks = stocks.slice(0, 6);
  const latestOrders = summary.orders.slice(0, 5);
  const latestTrades = summary.trades.slice(0, 5);

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Dashboard${user?.username ? ` for ${user.username}` : ''}`}
        subtitle="Live snapshots from auth, user, wallet, portfolio, order, trade, and market services."
        actions={[
          <button
            key="refresh"
            type="button"
            onClick={() => refreshMarket()}
            className="inline-flex items-center gap-2 rounded-lg border border-line bg-panel px-3 py-2 text-sm"
          >
            <RefreshCw className="h-4 w-4" />
            Refresh market
          </button>,
        ]}
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Wallet balance" value={summary.wallet?.balance ?? 0} note={summary.wallet?.currency || 'USD'} tone="success" />
        <StatCard label="Portfolio value" value={portfolioValue} note={summary.portfolio.length ? `${summary.portfolio.length} holdings` : 'Empty'} tone="accent" />
        <StatCard label="Invested capital" value={investedValue} note="Held cost basis" tone="warning" />
        <StatCard label="Market listings" value={formatNumber(stocks.length)} note="Live catalog" tone="neutral" />
      </div>

      <div className="grid gap-6 xl:grid-cols-3">
        <SectionCard
          title="Market snapshot"
          subtitle="Live service data with current prices and previous close."
          actions={[<button key="market" type="button" className="text-sm text-accent" onClick={() => navigate('/market')}>Open market</button>]}
        >
          {topStocks.length ? (
            <DataTable
              rowKey={(row) => row.symbol}
              rows={topStocks}
              columns={[
                { key: 'symbol', header: 'Symbol' },
                { key: 'name', header: 'Name' },
                { key: 'currentPrice', header: 'Price', render: (row) => formatMoney(row.currentPrice) },
                {
                  key: 'changePercent',
                  header: 'Change',
                  render: (row) => <span className={deltaClass(row.changePercent)}>{Number(row.changePercent).toFixed(2)}%</span>,
                },
              ]}
            />
          ) : (
            <EmptyState title="No market data" description="The market service returned an empty catalog." />
          )}
        </SectionCard>

        <SectionCard
          title="Latest orders"
          subtitle="Order service history for the active user."
          actions={[<button key="orders" type="button" className="text-sm text-accent" onClick={() => navigate('/orders')}>Open orders</button>]}
        >
          {latestOrders.length ? (
            <DataTable
              rowKey={(row) => row.id}
              rows={latestOrders}
              columns={[
                { key: 'symbol', header: 'Symbol' },
                { key: 'type', header: 'Type' },
                { key: 'status', header: 'Status', render: (row) => <Badge tone={row.status === 'COMPLETED' ? 'success' : row.status === 'CANCELLED' ? 'danger' : 'neutral'}>{row.status}</Badge> },
                { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
              ]}
            />
          ) : (
            <EmptyState title="No orders yet" description="Create an order to see it here." />
          )}
        </SectionCard>

        <SectionCard
          title="Latest trades"
          subtitle="Trade service events for the active user."
          actions={[<button key="trades" type="button" className="text-sm text-accent" onClick={() => navigate('/trades')}>Open trades</button>]}
        >
          {latestTrades.length ? (
            <DataTable
              rowKey={(row) => row.id}
              rows={latestTrades}
              columns={[
                { key: 'symbol', header: 'Symbol' },
                { key: 'type', header: 'Type' },
                { key: 'quantity', header: 'Qty' },
                { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
              ]}
            />
          ) : (
            <EmptyState title="No trades yet" description="Submit a buy or sell trade to see execution history." />
          )}
        </SectionCard>
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <SectionCard
          title="Portfolio breakdown"
          subtitle="Current value is computed from live market data."
          actions={[<button key="portfolio" type="button" className="text-sm text-accent" onClick={() => navigate('/portfolio')}>Open portfolio</button>]}
        >
          {summary.portfolio.length ? (
            <DataTable
              rowKey={(row) => `${row.id}-${row.symbol}`}
              rows={summary.portfolio}
              columns={[
                { key: 'symbol', header: 'Symbol' },
                { key: 'quantity', header: 'Qty' },
                { key: 'averagePrice', header: 'Avg Price', render: (row) => formatMoney(row.averagePrice) },
                { key: 'totalInvested', header: 'Invested', render: (row) => formatMoney(row.totalInvested) },
              ]}
            />
          ) : (
            <EmptyState title="No holdings" description="The portfolio service has no positions for this user." />
          )}
        </SectionCard>

        <SectionCard
          title="Wallet summary"
          subtitle="Balance and transaction flow from the wallet service."
          actions={[
            <button key="wallet" type="button" className="text-sm text-accent" onClick={() => navigate('/wallet')}>
              Open wallet
            </button>,
          ]}
        >
          <div className="grid gap-4 md:grid-cols-2">
            <div className="rounded-lg border border-line bg-panel2 p-4">
              <div className="text-xs uppercase tracking-wide text-muted">Balance</div>
              <div className="mt-2 text-2xl font-semibold">{formatMoney(summary.wallet?.balance)}</div>
              <div className="mt-1 text-sm text-muted">{summary.wallet?.currency || 'USD'}</div>
            </div>
            <div className="rounded-lg border border-line bg-panel2 p-4">
              <div className="text-xs uppercase tracking-wide text-muted">Last updated</div>
              <div className="mt-2 text-sm text-text">{formatDateTime(summary.wallet?.updatedAt)}</div>
              <div className="mt-1 text-sm text-muted">Live wallet state</div>
            </div>
          </div>
          <div className="mt-4 flex flex-wrap gap-2">
            <button type="button" onClick={() => navigate('/wallet')} className="inline-flex items-center gap-2 rounded-lg bg-white/5 px-3 py-2 text-sm">
              <WalletCards className="h-4 w-4" />
              Wallet
            </button>
            <button type="button" onClick={() => navigate('/portfolio')} className="inline-flex items-center gap-2 rounded-lg bg-white/5 px-3 py-2 text-sm">
              <Layers3 className="h-4 w-4" />
              Portfolio
            </button>
            <button type="button" onClick={() => navigate('/orders')} className="inline-flex items-center gap-2 rounded-lg bg-white/5 px-3 py-2 text-sm">
              <ListOrdered className="h-4 w-4" />
              Orders
            </button>
            <button type="button" onClick={() => navigate('/trades')} className="inline-flex items-center gap-2 rounded-lg bg-white/5 px-3 py-2 text-sm">
              <ArrowUpDown className="h-4 w-4" />
              Trades
            </button>
            <button type="button" onClick={() => navigate('/market')} className="inline-flex items-center gap-2 rounded-lg bg-white/5 px-3 py-2 text-sm">
              <CandlestickChart className="h-4 w-4" />
              Market
            </button>
          </div>
        </SectionCard>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        <StatCard label="Open positions" value={summary.portfolio.length} note="Holding count" />
        <StatCard label="Orders tracked" value={summary.orders.length} note="Order service" />
        <StatCard label="Trades tracked" value={summary.trades.length} note="Trading service" />
      </div>
    </div>
  );
}
