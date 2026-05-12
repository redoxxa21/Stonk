import { useEffect, useMemo, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import StatCard from '../components/common/StatCard';
import DataTable from '../components/common/DataTable';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../lib/apiClient';
import { formatMoney, formatDateTime } from '../lib/format';

export default function WalletPage() {
  const { user } = useAuth();
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [amount, setAmount] = useState('0.00');
  const [busy, setBusy] = useState(false);

  async function loadWallet() {
    if (!user?.id) return;
    setLoading(true);
    setError('');
    try {
      const walletData = await apiClient.get(`/wallet/${user.id}`).catch((err) => {
        if (err.status === 404) return null;
        throw err;
      });
      const txData = walletData
        ? await apiClient.get(`/wallet/${user.id}/transactions`).catch(() => [])
        : [];
      setWallet(walletData);
      setTransactions(Array.isArray(txData) ? txData : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadWallet();
  }, [user?.id]);

  async function mutate(action) {
    if (!user?.id) return;
    setBusy(true);
    setError('');
    try {
      if (action === 'create') {
        await apiClient.post(`/wallet/${user.id}/create`);
      } else {
        await apiClient.post(`/wallet/${user.id}/${action}`, { amount: Number(amount) });
      }
      await loadWallet();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const total = useMemo(() => wallet?.balance ?? 0, [wallet]);

  if (loading) return <LoadingState label="Loading wallet..." />;
  if (error && !wallet) return <ErrorState error={error} onRetry={loadWallet} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Wallet"
        subtitle="Manage your balance, funding activity, and recent transactions."
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Username" value={wallet?.username ?? user?.username ?? '-'} note="Owner" tone="warning" />
        <StatCard label="Balance" value={total} note={wallet?.currency || 'USD'} tone="success" />
        <StatCard label="Transactions" value={transactions.length} note="History" tone="accent" format="plain" />
        {/* <StatCard label="User ID" value={wallet?.id ?? user?.id ?? '-'} note="Owner" tone="neutral" format="plain" /> */}
      </div>

      <div className="grid gap-6 xl:grid-cols-[0.82fr_1.18fr]">
        {
          wallet &&
          <SectionCard title="Balance controls" subtitle="Create a wallet and manage available funds.">
            <div className="space-y-4">
              {error ? <ErrorState error={error} /> : null}
              <div className="rounded-2xl border border-line bg-[#242412] p-4 text-sm text-muted">
                Keep your wallet funded so you can respond quickly to market opportunities and account activity.
              </div>
              <label className="block text-sm text-muted">
                Amount
                <input
                  value={amount}
                  onChange={(event) => setAmount(event.target.value)}
                  type="number"
                  min="0.01"
                  step="0.01"
                  className="rh-input"
                />
              </label>
              <div className="grid grid-cols-2 gap-2">
                {/* <button onClick={() => mutate('create')} disabled={busy} type="button" className="rh-button-ghost">
                Create wallet
              </button> */}
                <button onClick={() => mutate('deposit')} disabled={busy} type="button" className="rh-button-primary">
                  Deposit
                </button>
                <button onClick={() => mutate('withdraw')} disabled={busy} type="button" className="rh-button-secondary">
                  Withdraw
                </button>
                {/* <button onClick={() => mutate('debit')} disabled={busy} type="button" className="rh-button-ghost">
                Debit
              </button>
              <button onClick={() => mutate('credit')} disabled={busy} type="button" className="rh-button-ghost">
                Credit
              </button>
              <button onClick={loadWallet} disabled={busy} type="button" className="rh-button-secondary">
                Refresh
              </button> */}
              </div>
            </div>
          </SectionCard>
        }

        <SectionCard title="Wallet record" subtitle="A summary of the wallet currently linked to your account.">
          {error ? <div className="mb-4"><ErrorState error={error} /></div> : null}
          {wallet ? (
            <DataTable
              rowKey={(row) => row.key}
              rows={[
                // { key: 'id', label: 'User ID', value: wallet.id },
                { key: 'username', label: 'Username', value: wallet.username },
                { key: 'balance', label: 'Balance', value: formatMoney(wallet.balance) },
                { key: 'currency', label: 'Currency', value: wallet.currency },
              ]}
              columns={[
                { key: 'label', header: 'Field' },
                { key: 'value', header: 'Value' },
              ]}
            />
          ) : (
            <div className="space-y-3">
              <div className="rounded-2xl border border-line bg-[#242412] p-4 text-sm text-muted">No wallet exists for this user yet.</div>
              <button onClick={() => mutate('create')} type="button" className="rh-button-primary">
                Create wallet now
              </button>
            </div>
          )}
        </SectionCard>
      </div>

      <SectionCard title="Transactions" subtitle="Latest debit/credit/deposit history.">
        <DataTable
          rowKey={(row) => row.id}
          rows={transactions}
          emptyTitle="No transactions"
          emptyDescription="No wallet activity recorded yet."
          columns={[
            { key: 'type', header: 'Type' },
            { key: 'amount', header: 'Amount', render: (row) => formatMoney(row.amount) },
            { key: 'balanceAfter', header: 'Balance after', render: (row) => formatMoney(row.balanceAfter) },
            { key: 'description', header: 'Description' },
            { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
          ]}
        />
      </SectionCard>
    </div>
  );
}
