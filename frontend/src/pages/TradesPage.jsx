import { useEffect, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import DataTable from '../components/common/DataTable';
import Badge from '../components/common/Badge';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { usePortfolioHoldings } from '../hooks/usePortfolioHoldings';
import { apiClient } from '../lib/apiClient';
import { formatMoney, formatDateTime } from '../lib/format';

export default function TradesPage() {
  const { user } = useAuth();
  const [trades, setTrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ symbol: '', quantity: 1 });
  const [busy, setBusy] = useState(false);
  const { canSell, loading: holdingsLoading } = usePortfolioHoldings();

  async function loadTrades() {
    if (!user?.id) return;
    setLoading(true);
    setError('');
    try {
      const data = await apiClient.get(`/trades/${user.id}`);
      setTrades(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadTrades();
  }, [user?.id]);

  async function submitTrade(type) {
    if (type === 'SELL' && !canSell(form.symbol, form.quantity)) return;
    setBusy(true);
    setError('');
    try {
      await apiClient.post(`/trades/${type.toLowerCase()}`, {
        userId: user.id,
        symbol: form.symbol,
        quantity: Number(form.quantity),
      });
      setForm((current) => ({ ...current, symbol: '', quantity: 1 }));
      await loadTrades();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <LoadingState label="Loading trades..." />;
  if (error && !trades.length) return <ErrorState error={error} onRetry={loadTrades} />;

  const sellDisabled = busy || holdingsLoading || !canSell(form.symbol, form.quantity);

  return (
    <div className="space-y-6">
      <PageHeader title="Trades" subtitle="Place orders and review your recent trade activity." />

      <SectionCard title="Submit trade" subtitle="Enter a symbol and quantity to place a buy or sell order.">
        {error ? <div className="mb-4"><ErrorState error={error} /></div> : null}
        <div className="mb-4 rounded-2xl border border-line bg-[#242412] p-4 text-sm text-muted">
          Use this form for quick trade entry. For additional symbol context, review the instrument in Market before submitting.
        </div>
        <div className="grid gap-4 md:grid-cols-3">
          <label className="block text-sm text-muted">
            Symbol
            <input value={form.symbol} onChange={(e) => setForm((current) => ({ ...current, symbol: e.target.value }))} className="rh-input" required />
          </label>
          <label className="block text-sm text-muted">
            Quantity
            <input type="number" min="1" step="1" value={form.quantity} onChange={(e) => setForm((current) => ({ ...current, quantity: e.target.value }))} className="rh-input" required />
          </label>
          <div className="flex items-end gap-2">
            <button disabled={busy} type="button" onClick={() => submitTrade('BUY')} className="rh-button-positive">
              Buy
            </button>
            <button disabled={sellDisabled} type="button" onClick={() => submitTrade('SELL')} className="rh-button-danger">
              Sell
            </button>
          </div>
        </div>
        {form.symbol && !canSell(form.symbol, form.quantity) ? (
          <div className="mt-4 rounded-2xl border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-100">
            You need at least {Number(form.quantity) || 0} shares of {form.symbol.toUpperCase()} to sell.
          </div>
        ) : null}
      </SectionCard>

      <SectionCard title="Trade history" subtitle="Realtime execution records.">
        {error ? <div className="mb-4"><ErrorState error={error} /></div> : null}
        <DataTable
          rowKey={(row) => row.id}
          rows={trades}
          emptyTitle="No trades"
          emptyDescription="This user has not executed any trades."
          columns={[
            { key: 'id', header: 'ID' },
            { key: 'symbol', header: 'Symbol' },
            { key: 'type', header: 'Type' },
            { key: 'quantity', header: 'Qty' },
            { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
            { key: 'status', header: 'Status', render: (row) => <Badge tone={row.status === 'COMPLETED' || row.status === 'EXECUTED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'neutral'}>{row.status}</Badge> },
            { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
          ]}
        />
      </SectionCard>
    </div>
  );
}
