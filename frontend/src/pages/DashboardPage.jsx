import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowUpRight, WalletCards, Layers3, ArrowUpDown, CandlestickChart, RefreshCw, Shield, CircleHelp } from 'lucide-react';
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
  const { stocks, overview, marketEvents, socketStatus, refreshMarket } = usePlatform();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [summary, setSummary] = useState({
    wallet: null,
    portfolio: [],
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
        const [wallet, portfolio, trades] = await Promise.all([
          apiClient.get(`/wallet/${user.id}`).catch(() => null),
          apiClient.get(`/portfolio/${user.id}`).catch(() => []),
          apiClient.get(`/trades/${user.id}`).catch(() => []),
        ]);

        if (!alive) return;
        setSummary({
          wallet,
          portfolio: Array.isArray(portfolio) ? portfolio : [],
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
  const latestTrades = summary.trades.slice(0, 8);

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Dashboard${user?.username ? ` for ${user.username}` : ''}`}
        subtitle="A live overview of your market activity, account value, and recent trading performance."
        actions={[
          <button
            key="help"
            type="button"
            onClick={() => navigate('/help')}
            className="rh-button-secondary"
          >
            <CircleHelp className="h-4 w-4" />
            Help center
          </button>,
          // <button
          //   key="refresh"
          //   type="button"
          //   onClick={() => refreshMarket()}
          //   className="rh-button-primary"
          // >
          //   <RefreshCw className="h-4 w-4" />
          //   Refresh market
          // </button>,
        ]}
      />

      <SectionCard title="Operating picture" subtitle="What matters right now for this trading session.">
        <div className="grid gap-4 xl:grid-cols-[1.15fr_0.85fr]">
          <div className="rounded-[24px] border border-line bg-[linear-gradient(180deg,rgba(18,18,18,0.98),rgba(10,10,10,0.96))] p-5 shadow-soft">
            <div className="flex flex-wrap items-center gap-2">
              <Badge tone={socketStatus === 'connected' ? 'success' : socketStatus === 'connecting' ? 'warning' : 'neutral'}>
                Live {socketStatus}
              </Badge>
              {user?.role === 'ADMIN' ? (
                <Badge tone="warning">
                  <span className="inline-flex items-center gap-1">
                    <Shield className="h-3.5 w-3.5" />
                    Admin session
                  </span>
                </Badge>
              ) : null}
            </div>
            <h2 className="mt-4 text-2xl font-semibold text-text">Trade from the live market, then verify settlement across wallet, portfolio, and trade history.</h2>
            <p className="mt-3 max-w-2xl text-sm leading-6 text-muted">
              Stay on top of market conditions, review open exposure, and move quickly between trading, funding, and portfolio decisions.
            </p>
            <div className="mt-5 flex flex-wrap gap-2">
              <button type="button" onClick={() => navigate('/market')} className="rh-button-primary rounded-full">
                Open market
              </button>
              <button type="button" onClick={() => navigate('/trades')} className="rh-button-secondary rounded-full">
                Review trades
              </button>
            </div>
          </div>

          <div className="grid gap-3">
            <div className="rh-dark-panel p-4">
              <div className="text-xs uppercase tracking-[0.22em] text-muted">Market status</div>
              <div className="mt-2 text-2xl font-semibold">{overview?.marketStatus || 'Live'}</div>
              <div className="mt-1 text-sm text-white/65">Current trading session</div>
            </div>
            <div className="rh-panel-subtle p-4">
              <div className="text-xs uppercase tracking-[0.22em] text-muted">Recent events</div>
              <div className="mt-2 text-2xl font-semibold text-text">{marketEvents.length}</div>
              <div className="mt-1 text-sm text-muted">Latest market updates</div>
            </div>
          </div>
        </div>
      </SectionCard>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Wallet balance" value={summary.wallet?.balance ?? 0} note={summary.wallet?.currency || 'USD'} tone="success" />
        <StatCard label="Portfolio value" value={portfolioValue} note={summary.portfolio.length ? `${summary.portfolio.length} holdings` : 'Empty'} tone="accent" />
        <StatCard label="Invested capital" value={investedValue} note="Held cost basis" tone="warning" />
        <StatCard label="Market listings" value={formatNumber(stocks.length)} note="Live catalog" tone="neutral" format="plain" />
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <SectionCard
          title="Market snapshot"
          subtitle="Current prices and previous close for selected symbols."
          actions={[<button key="market" type="button" className="rh-button-secondary" onClick={() => navigate('/market')}>Open market</button>]}
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
            <EmptyState title="No market data" description="Market information is not available right now." />
          )}
        </SectionCard>

        <SectionCard
          title="Recent trades"
          subtitle="Your latest completed and pending trade activity."
          actions={[<button key="trades" type="button" className="rh-button-secondary" onClick={() => navigate('/trades')}>Open trades</button>]}
        >
          {latestTrades.length ? (
            <DataTable
              rowKey={(row) => row.id}
              rows={latestTrades}
              columns={[
                { key: 'symbol', header: 'Symbol' },
                { key: 'type', header: 'Type' },
                {
                  key: 'status',
                  header: 'Status',
                  render: (row) => <Badge tone={row.status === 'COMPLETED' || row.status === 'EXECUTED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'neutral'}>{row.status}</Badge>,
                },
                { key: 'quantity', header: 'Qty' },
                { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
              ]}
            />
          ) : (
            <EmptyState title="No trades yet" description="Submit a buy or sell trade to see execution here." />
          )}
        </SectionCard>
      </div>

      <div className="grid gap-6 xl:grid-cols-2">
        <SectionCard
          title="Portfolio breakdown"
          subtitle="Current value is computed from live market data."
          actions={[<button key="portfolio" type="button" className="rh-button-secondary" onClick={() => navigate('/portfolio')}>Open portfolio</button>]}
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
            <EmptyState title="No holdings" description="No positions are currently held in this portfolio." />
          )}
        </SectionCard>

        <SectionCard
          title="Wallet summary"
          subtitle="Available balance and recent account funding details."
          actions={[
            <button key="wallet" type="button" className="rh-button-secondary" onClick={() => navigate('/wallet')}>
              Open wallet
            </button>,
          ]}
        >
          <div className="grid gap-4 md:grid-cols-2">
            <div className="rh-panel-subtle p-4">
              <div className="text-xs uppercase tracking-wide text-muted">Balance</div>
              <div className="mt-2 text-2xl font-semibold">{formatMoney(summary.wallet?.balance)}</div>
              <div className="mt-1 text-sm text-muted">{summary.wallet?.currency || 'USD'}</div>
            </div>
            <div className="rh-panel-subtle p-4">
              <div className="text-xs uppercase tracking-wide text-muted">Last updated</div>
              <div className="mt-2 text-sm text-text">{formatDateTime(summary.wallet?.updatedAt)}</div>
              <div className="mt-1 text-sm text-muted">Live wallet state</div>
            </div>
          </div>
          <div className="mt-4 flex flex-wrap gap-2">
            <button type="button" onClick={() => navigate('/wallet')} className="rh-button-ghost">
              <WalletCards className="h-4 w-4" />
              Wallet
            </button>
            <button type="button" onClick={() => navigate('/portfolio')} className="rh-button-ghost">
              <Layers3 className="h-4 w-4" />
              Portfolio
            </button>
            <button type="button" onClick={() => navigate('/trades')} className="rh-button-ghost">
              <ArrowUpDown className="h-4 w-4" />
              Trades
            </button>
            <button type="button" onClick={() => navigate('/market')} className="rh-button-ghost">
              <CandlestickChart className="h-4 w-4" />
              Market
            </button>
          </div>
        </SectionCard>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <StatCard label="Open positions" value={summary.portfolio.length} note="Holding count" format="plain" />
        <StatCard label="Trades tracked" value={summary.trades.length} note="Activity" format="plain" />
      </div>
    </div>
  );
}
