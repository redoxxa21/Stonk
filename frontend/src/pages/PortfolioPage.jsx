import { useEffect, useMemo, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import StatCard from '../components/common/StatCard';
import DataTable from '../components/common/DataTable';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { usePlatform } from '../context/PlatformContext';
import { apiClient } from '../lib/apiClient';
import { formatMoney, deltaClass } from '../lib/format';

export default function PortfolioPage() {
  const { user } = useAuth();
  const { stocks, refreshMarket } = usePlatform();
  const [holdings, setHoldings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const priceMap = useMemo(() => new Map(stocks.map((stock) => [stock.symbol, Number(stock.currentPrice)])), [stocks]);

  async function loadPortfolio() {
    if (!user?.id) return;
    setLoading(true);
    setError('');
    try {
      const data = await apiClient.get(`/portfolio/${user.id}`);
      setHoldings(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadPortfolio();
    refreshMarket().catch(() => {});
  }, [user?.id]);

  const summary = useMemo(() => {
    const invested = holdings.reduce((total, holding) => total + Number(holding.totalInvested || 0), 0);
    const current = holdings.reduce((total, holding) => {
      const marketPrice = priceMap.get(holding.symbol) ?? Number(holding.averagePrice || 0);
      return total + marketPrice * Number(holding.quantity || 0);
    }, 0);
    return { invested, current, pnl: current - invested };
  }, [holdings, priceMap]);

  if (loading) return <LoadingState label="Loading portfolio..." />;
  if (error) return <ErrorState error={error} onRetry={loadPortfolio} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Portfolio"
        subtitle="Review your holdings, current value, and unrealized performance."
      />

      <SectionCard title="Portfolio summary" subtitle="A quick view of your current position performance.">
        <div className="grid gap-3 md:grid-cols-3 text-sm text-muted">
          <div className="rh-panel-subtle p-4">Invested capital reflects the total amount committed to your current positions.</div>
          <div className="rh-panel-subtle p-4">Current value updates with the latest available market prices.</div>
          <div className="rh-panel-subtle p-4">Unrealized P/L highlights how your holdings are performing right now.</div>
        </div>
      </SectionCard>

      <div className="grid gap-4 md:grid-cols-3">
        <StatCard label="Invested capital" value={summary.invested} note="Cost basis" tone="warning" />
        <StatCard label="Current value" value={summary.current} note="Marked to market" tone="success" />
        <StatCard label="Unrealized P/L" value={`${summary.pnl >= 0 ? '+' : ''}${formatMoney(summary.pnl)}`} note={summary.pnl >= 0 ? 'Gain' : 'Loss'} tone={summary.pnl >= 0 ? 'success' : 'danger'} />
      </div>

      <SectionCard title="Holdings" subtitle="A detailed view of your active portfolio positions.">
        <DataTable
          rowKey={(row) => `${row.id}-${row.symbol}`}
          rows={holdings.map((holding) => {
            const currentPrice = priceMap.get(holding.symbol) ?? Number(holding.averagePrice || 0);
            const currentValue = currentPrice * Number(holding.quantity || 0);
            const pnl = currentValue - Number(holding.totalInvested || 0);
            return { ...holding, currentPrice, currentValue, pnl };
          })}
          emptyTitle="No holdings"
          emptyDescription="This portfolio is empty."
          columns={[
            { key: 'id', header: 'User ID' },
            { key: 'username', header: 'Username' },
            { key: 'symbol', header: 'Symbol' },
            { key: 'quantity', header: 'Qty' },
            { key: 'averagePrice', header: 'Avg Price', render: (row) => formatMoney(row.averagePrice) },
            { key: 'currentPrice', header: 'Current Price', render: (row) => formatMoney(row.currentPrice) },
            { key: 'totalInvested', header: 'Invested', render: (row) => formatMoney(row.totalInvested) },
            { key: 'currentValue', header: 'Value', render: (row) => formatMoney(row.currentValue) },
            { key: 'pnl', header: 'P/L', render: (row) => <span className={deltaClass(row.pnl)}>{row.pnl >= 0 ? '+' : ''}{formatMoney(row.pnl)}</span> },
          ]}
        />
      </SectionCard>
    </div>
  );
}
