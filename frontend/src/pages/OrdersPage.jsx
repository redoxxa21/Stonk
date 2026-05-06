import { useEffect, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import DataTable from '../components/common/DataTable';
import Badge from '../components/common/Badge';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../lib/apiClient';
import { formatMoney, formatDateTime } from '../lib/format';

export default function OrdersPage() {
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ symbol: '', type: 'BUY', quantity: 1, price: '' });
  const [busy, setBusy] = useState(false);

  async function loadOrders() {
    if (!user?.id) return;
    setLoading(true);
    setError('');
    try {
      const data = await apiClient.get(`/orders/${user.id}`);
      setOrders(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOrders();
  }, [user?.id]);

  async function submitOrder(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await apiClient.post('/orders', {
        userId: user.id,
        symbol: form.symbol,
        type: form.type,
        quantity: Number(form.quantity),
        price: Number(form.price),
      });
      setForm((current) => ({ ...current, symbol: '', quantity: 1, price: '' }));
      await loadOrders();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function updateOrder(id, action) {
    setBusy(true);
    try {
      await apiClient.put(`/orders/${id}/${action}`);
      await loadOrders();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (loading) return <LoadingState label="Loading orders..." />;
  if (error && !orders.length) return <ErrorState error={error} onRetry={loadOrders} />;

  return (
    <div className="space-y-6">
      <PageHeader title="Orders" subtitle="Create buy and sell orders, then inspect and manage their lifecycle." />

      <SectionCard title="Create order" subtitle="Order service DTOs mapped directly.">
        <form onSubmit={submitOrder} className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <label className="block text-sm text-muted">
            Symbol
            <input value={form.symbol} onChange={(e) => setForm((current) => ({ ...current, symbol: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
          </label>
          <label className="block text-sm text-muted">
            Type
            <select value={form.type} onChange={(e) => setForm((current) => ({ ...current, type: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text">
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </label>
          <label className="block text-sm text-muted">
            Quantity
            <input type="number" min="1" step="1" value={form.quantity} onChange={(e) => setForm((current) => ({ ...current, quantity: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
          </label>
          <label className="block text-sm text-muted">
            Price
            <input type="number" min="0.01" step="0.01" value={form.price} onChange={(e) => setForm((current) => ({ ...current, price: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
          </label>
          <div className="md:col-span-2 xl:col-span-4">
            <button disabled={busy} className="rounded-lg bg-gradient-to-r from-accent to-accent2 px-4 py-2.5 font-semibold text-bg" type="submit">
              Submit order
            </button>
          </div>
        </form>
      </SectionCard>

      <SectionCard title="Order history" subtitle="Current order state, completion and cancellation actions.">
        <DataTable
          rowKey={(row) => row.id}
          rows={orders}
          emptyTitle="No orders"
          emptyDescription="This user has not placed any orders yet."
          columns={[
            { key: 'id', header: 'ID' },
            { key: 'symbol', header: 'Symbol' },
            { key: 'type', header: 'Type' },
            { key: 'quantity', header: 'Qty' },
            { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
            { key: 'status', header: 'Status', render: (row) => <Badge tone={row.status === 'COMPLETED' ? 'success' : row.status === 'CANCELLED' ? 'danger' : 'neutral'}>{row.status}</Badge> },
            { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
            {
              key: 'actions',
              header: 'Actions',
              render: (row) => (
                <div className="flex flex-wrap gap-2">
                  <button type="button" onClick={() => updateOrder(row.id, 'complete')} className="rounded-md border border-line px-3 py-1.5 text-xs">
                    Complete
                  </button>
                  <button type="button" onClick={() => updateOrder(row.id, 'cancel')} className="rounded-md border border-line px-3 py-1.5 text-xs">
                    Cancel
                  </button>
                </div>
              ),
            },
          ]}
        />
      </SectionCard>
    </div>
  );
}
