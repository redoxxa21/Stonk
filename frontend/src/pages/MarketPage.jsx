import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Search, RefreshCw } from 'lucide-react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import Badge from '../components/common/Badge';
import Sparkline from '../components/common/Sparkline';
import { LoadingState, ErrorState, EmptyState } from '../components/common/AsyncState';
import { usePlatform } from '../context/PlatformContext';
import { formatMoney, deltaClass } from '../lib/format';

export default function MarketPage() {
  const navigate = useNavigate();
  const { stocks, history, overview, marketEvents, socketStatus, refreshMarket, marketLoading, marketError, setSelectedSymbol } = usePlatform();
  const [query, setQuery] = useState('');

  const filtered = useMemo(() => {
    const needle = query.trim().toLowerCase();
    if (!needle) return stocks;
    return stocks.filter((stock) => `${stock.symbol} ${stock.name}`.toLowerCase().includes(needle));
  }, [stocks, query]);

  if (marketLoading && !stocks.length) return <LoadingState label="Loading market catalog..." />;
  if (marketError && !stocks.length) return <ErrorState error={marketError} onRetry={refreshMarket} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Market"
        subtitle="Follow live symbols, review movement, and open detailed market views."
        actions={[
          <Badge key="feed" tone={socketStatus === 'connected' ? 'success' : socketStatus === 'connecting' ? 'warning' : 'neutral'}>
            Live {socketStatus}
          </Badge>,
          <button key="refresh" type="button" onClick={refreshMarket} className="rh-button-primary">
            <RefreshCw className="h-4 w-4" />
            Refresh
          </button>,
        ]}
      />

      <div className="grid gap-4 xl:grid-cols-3">
        <SectionCard title="Market state" subtitle="A snapshot of the current trading session.">
          <div className="text-3xl font-semibold text-text">{overview?.marketStatus || 'LIVE'}</div>
          <div className="mt-2 text-sm text-muted">Top level status for the trading session.</div>
        </SectionCard>
        <SectionCard title="Top gainers" subtitle="Fast movers in the current overview.">
          <div className="space-y-3">
            {(overview?.topGainers || []).slice(0, 3).map((item) => (
              <div key={item.symbol} className="flex items-center justify-between rounded-2xl border border-line bg-panel2 px-3 py-3">
                <div>
                  <div className="font-medium text-text">{item.symbol}</div>
                  <div className="text-xs text-muted">{item.name}</div>
                </div>
                <div className="text-right">
                  <div className="text-sm text-text">{formatMoney(item.price || item.currentPrice)}</div>
                  <div className="text-xs text-[#5f880f]">+{Number(item.changePercent || 0).toFixed(2)}%</div>
                </div>
              </div>
            ))}
          </div>
        </SectionCard>
        <SectionCard title="Market events" subtitle="Recent updates across the market.">
          <div className="space-y-3">
            {marketEvents.slice(0, 3).map((event, index) => (
              <div key={`${event.symbol || 'market'}-${event.timestamp || index}`} className="rounded-2xl border border-line bg-panel2 px-3 py-3">
                <div className="text-sm font-medium text-text">{event.eventType || 'Market update'}</div>
                <div className="mt-1 text-xs text-muted">{event.symbol ? `${event.symbol} • ` : ''}{event.message || 'Live market event received.'}</div>
              </div>
            ))}
          </div>
        </SectionCard>
      </div>

      <SectionCard
        title="Watchlist"
        subtitle="Search the live catalog and inspect each instrument."
        actions={[
          <div key="search" className="flex items-center gap-2 rounded-2xl border border-line bg-panel2 px-3 py-2">
            <Search className="h-4 w-4 text-muted" />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Filter by symbol or company"
              className="bg-transparent text-sm outline-none placeholder:text-muted"
            />
          </div>,
        ]}
      >
        {filtered.length ? (
          <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
            {filtered.map((stock) => {
              const priceHistory = history[stock.symbol]?.map((point) => point.price) || [Number(stock.currentPrice)];
              return (
                <button
                  key={stock.symbol}
                  type="button"
                  onClick={() => {
                    setSelectedSymbol(stock.symbol);
                    navigate(`/market/${stock.symbol}`);
                  }}
                  className="rounded-[24px] border border-line bg-panel2 p-4 text-left transition hover:border-[#b7df43] hover:bg-[#1a1f0f]"
                >
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <div className="text-lg font-semibold">{stock.symbol}</div>
                      <div className="text-sm text-muted">{stock.name}</div>
                    </div>
                    <Badge tone={Number(stock.changePercent) >= 0 ? 'success' : 'danger'}>
                      {Number(stock.changePercent).toFixed(2)}%
                    </Badge>
                  </div>
                  <div className="mt-4 flex items-end justify-between gap-4">
                    <div>
                      <div className="text-xs uppercase tracking-wide text-muted">Current price</div>
                      <div className="mt-1 text-xl font-semibold">{formatMoney(stock.currentPrice)}</div>
                      <div className={`mt-1 text-xs ${deltaClass(stock.changePercent)}`}>Prev close {formatMoney(stock.previousClose)}</div>
                    </div>
                    <div className="w-28 text-accent">
                      <Sparkline values={priceHistory} />
                    </div>
                  </div>
                  <div className="mt-4 flex items-center justify-between text-xs text-muted">
                    <span>{stock.lastUpdated ? new Date(stock.lastUpdated).toLocaleTimeString() : 'Live'}</span>
                    <span className="inline-flex items-center gap-1">
                      Open detail <ArrowRight className="h-3.5 w-3.5" />
                    </span>
                  </div>
                </button>
              );
            })}
          </div>
        ) : (
          <EmptyState title="No matching symbols" description="Try a different search term or refresh the market catalog." />
        )}
      </SectionCard>
    </div>
  );
}
