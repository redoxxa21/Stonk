import { useEffect, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import DataTable from '../components/common/DataTable';
import Badge from '../components/common/Badge';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../lib/apiClient';
import { formatMoney, formatDateTime } from '../lib/format';

export default function TradesPage() {
  const { user } = useAuth();
  const [trades, setTrades] = useState([]);
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ symbol: '', quantity: 1 });
  const [busy, setBusy] = useState(false);

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

  async function loadDetail(id) {
    try {
      const response = await apiClient.get(`/trades/detail/${id}`);
      setDetail(response);
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <LoadingState label="Loading trades..." />;
  if (error && !trades.length) return <ErrorState error={error} onRetry={loadTrades} />;

  return (
    <div className="space-y-6">
      <PageHeader title="Trades" subtitle="Direct execution flow from the trading service." />

      <SectionCard title="Submit trade" subtitle="Buy or sell through trading-service.">
        <div className="grid gap-4 md:grid-cols-3">
          <label className="block text-sm text-muted">
            Symbol
            <input value={form.symbol} onChange={(e) => setForm((current) => ({ ...current, symbol: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
          </label>
          <label className="block text-sm text-muted">
            Quantity
            <input type="number" min="1" step="1" value={form.quantity} onChange={(e) => setForm((current) => ({ ...current, quantity: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
          </label>
          <div className="flex items-end gap-2">
            <button disabled={busy} type="button" onClick={() => submitTrade('BUY')} className="rounded-lg border border-line bg-emerald-500/15 px-4 py-2.5 text-emerald-200">
              Buy
            </button>
            <button disabled={busy} type="button" onClick={() => submitTrade('SELL')} className="rounded-lg border border-line bg-rose-500/15 px-4 py-2.5 text-rose-200">
              Sell
            </button>
          </div>
        </div>
      </SectionCard>

      <div className="grid gap-6 xl:grid-cols-[1fr_0.7fr]">
        <SectionCard title="Trade history" subtitle="Realtime execution records.">
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
              { key: 'status', header: 'Status', render: (row) => <Badge tone={row.status === 'COMPLETED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'neutral'}>{row.status}</Badge> },
              { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
              {
                key: 'detail',
                header: 'Detail',
                render: (row) => (
                  <button type="button" onClick={() => loadDetail(row.id)} className="rounded-md border border-line px-3 py-1.5 text-xs">
                    Open
                  </button>
                ),
              },
            ]}
          />
        </SectionCard>

        <SectionCard title="Trade detail" subtitle="Selected trade payload.">
          {detail ? (
            <DataTable
              rowKey={(row) => row.key}
              rows={[
                { key: 'id', label: 'ID', value: detail.id },
                { key: 'symbol', label: 'Symbol', value: detail.symbol },
                { key: 'type', label: 'Type', value: detail.type },
                { key: 'quantity', label: 'Quantity', value: detail.quantity },
                { key: 'price', label: 'Price', value: formatMoney(detail.price) },
                { key: 'totalAmount', label: 'Total', value: formatMoney(detail.totalAmount) },
                { key: 'status', label: 'Status', value: detail.status },
                { key: 'orderId', label: 'Order ID', value: detail.orderId ?? '-' },
              ]}
              columns={[
                { key: 'label', header: 'Field' },
                { key: 'value', header: 'Value' },
              ]}
            />
          ) : (
            <div className="rounded-lg border border-dashed border-line p-4 text-sm text-muted">Select a trade row to inspect it here.</div>
          )}
        </SectionCard>
      </div>
    </div>
  );
}
