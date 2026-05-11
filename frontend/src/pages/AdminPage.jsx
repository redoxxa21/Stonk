import { useEffect, useMemo, useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { Search, Shield } from 'lucide-react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import StatCard from '../components/common/StatCard';
import DataTable from '../components/common/DataTable';
import Badge from '../components/common/Badge';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { apiClient } from '../lib/apiClient';
import { useAuth } from '../context/AuthContext';
import { formatDateTime, formatMoney } from '../lib/format';

export default function AdminPage() {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [query, setQuery] = useState('');
  const [usernameQuery, setUsernameQuery] = useState('');
  const [form, setForm] = useState({ id: '', username: '', email: '' });
  const [selectedUserId, setSelectedUserId] = useState('');
  const [selectedSnapshot, setSelectedSnapshot] = useState({
    wallet: null,
    transactions: [],
    portfolio: [],
    trades: [],
  });
  const [systemTrades, setSystemTrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedLoading, setSelectedLoading] = useState(false);
  const [systemLoading, setSystemLoading] = useState(false);
  const [error, setError] = useState('');

  async function loadSystemTrades(sourceUsers) {
    setSystemLoading(true);
    try {
      const settled = await Promise.all(
        sourceUsers.map(async (account) => {
          const trades = await apiClient.get(`/trades/${account.id}`).catch(() => []);
          return Array.isArray(trades)
            ? trades.map((trade) => ({
                ...trade,
                userId: account.id,
                username: account.username,
              }))
            : [];
        }),
      );

      const nextTrades = settled
        .flat()
        .sort((left, right) => new Date(right.createdAt || 0) - new Date(left.createdAt || 0));

      setSystemTrades(nextTrades);
    } finally {
      setSystemLoading(false);
    }
  }

  async function loadUsers() {
    setLoading(true);
    setError('');
    try {
      const data = await apiClient.get('/users');
      const nextUsers = Array.isArray(data) ? data : [];
      setUsers(nextUsers);

      if (!selectedUserId && nextUsers.length) {
        setSelectedUserId(String(nextUsers[0].id));
        setForm({
          id: nextUsers[0].id,
          username: nextUsers[0].username,
          email: nextUsers[0].email,
        });
      }

      await loadSystemTrades(nextUsers);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  useEffect(() => {
    async function loadSelectedUserSnapshot() {
      if (!selectedUserId) {
        setSelectedSnapshot({
          wallet: null,
          transactions: [],
          portfolio: [],
          trades: [],
        });
        return;
      }

      setSelectedLoading(true);
      try {
        const wallet = await apiClient.get(`/wallet/${selectedUserId}`).catch((err) => {
          if (err.status === 404) return null;
          throw err;
        });

        const [transactions, portfolio, trades] = await Promise.all([
          wallet ? apiClient.get(`/wallet/${selectedUserId}/transactions`).catch(() => []) : Promise.resolve([]),
          apiClient.get(`/portfolio/${selectedUserId}`).catch(() => []),
          apiClient.get(`/trades/${selectedUserId}`).catch(() => []),
        ]);

        setSelectedSnapshot({
          wallet,
          transactions: Array.isArray(transactions) ? transactions : [],
          portfolio: Array.isArray(portfolio) ? portfolio : [],
          trades: Array.isArray(trades) ? trades : [],
        });
      } catch (err) {
        setError(err.message);
      } finally {
        setSelectedLoading(false);
      }
    }

    loadSelectedUserSnapshot();
  }, [selectedUserId]);

  const filtered = users.filter((item) =>
    `${item.id} ${item.username} ${item.email} ${item.role}`.toLowerCase().includes(query.toLowerCase()),
  );

  const selectedUser = useMemo(
    () => users.find((item) => String(item.id) === String(selectedUserId)) || null,
    [users, selectedUserId],
  );

  const systemSummary = useMemo(() => {
    const completed = systemTrades.filter((trade) => trade.status === 'COMPLETED' || trade.status === 'EXECUTED').length;
    const failed = systemTrades.filter((trade) => trade.status === 'FAILED').length;
    const activeUsers = new Set(systemTrades.map((trade) => trade.userId)).size;

    return {
      completed,
      failed,
      activeUsers,
    };
  }, [systemTrades]);

  const adminSections = [
    { to: '/admin', label: 'Overview' },
    { to: '/admin/accounts', label: 'Accounts' },
    { to: '/admin/trades', label: 'Trades' },
    { to: '/admin/review', label: 'Review' },
  ];

  const currentSection =
    adminSections.find((section) =>
      section.to === '/admin' ? location.pathname === '/admin' : location.pathname.startsWith(section.to),
    )?.label || 'Overview';

  async function searchByUsername(event) {
    event.preventDefault();
    const needle = usernameQuery.trim();
    if (!needle) {
      await loadUsers();
      return;
    }

    try {
      setError('');
      const userByName = await apiClient.get(`/users/username/${encodeURIComponent(needle)}`);
      setUsers([userByName]);
      setSelectedUserId(String(userByName.id));
      setForm({
        id: userByName.id,
        username: userByName.username,
        email: userByName.email,
      });
      await loadSystemTrades([userByName]);
    } catch (err) {
      setError(err.message);
    }
  }

  async function updateUser(id) {
    try {
      setError('');
      await apiClient.put(`/users/${id}`, {
        username: form.username || undefined,
        email: form.email || undefined,
      });
      await loadUsers();
    } catch (err) {
      setError(err.message);
    }
  }

  async function deleteUser(id) {
    try {
      setError('');
      await apiClient.delete(`/users/${id}`);
      const remainingUsers = users.filter((account) => String(account.id) !== String(id));
      setUsers(remainingUsers);
      if (String(selectedUserId) === String(id)) {
        setSelectedUserId(remainingUsers[0] ? String(remainingUsers[0].id) : '');
      }
      await loadSystemTrades(remainingUsers);
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <LoadingState label="Loading users..." />;
  if (error && !users.length) return <ErrorState error={error} onRetry={loadUsers} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Admin Control Center"
        subtitle={`Privileged workspace for ${user?.username || 'this session'}.`}
        actions={[
          <Badge key="role" tone="warning">
            <span className="inline-flex items-center gap-1">
              <Shield className="h-3.5 w-3.5" />
              Admin role
            </span>
          </Badge>,
        ]}
      />

      <div className="flex flex-wrap gap-2">
        {adminSections.map((section) => (
          <NavLink
            key={section.to}
            to={section.to}
            end={section.to === '/admin'}
            className={({ isActive }) =>
              `rounded-full px-4 py-2 text-sm transition ${
                isActive
                  ? 'border border-amber-800 bg-[rgba(183,121,31,0.18)] text-amber-200'
                  : 'border border-line bg-[#131313] text-muted hover:border-amber-900 hover:bg-[rgba(183,121,31,0.12)] hover:text-amber-100'
              }`
            }
          >
            {section.label}
          </NavLink>
        ))}
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <StatCard label="Users loaded" value={users.length} note="Directory" tone="accent" format="plain" />
        <StatCard label="Admins" value={users.filter((item) => item.role === 'ADMIN').length} note="Privileged" tone="warning" format="plain" />
        <StatCard label="System trades" value={systemTrades.length} note="All users" tone="neutral" format="plain" />
        <StatCard label="Failed trades" value={systemSummary.failed} note="Requires review" tone="danger" format="plain" />
      </div>

      {currentSection === 'Overview' ? (
        <SectionCard title="Admin Overview" subtitle="High-level account and trade signals across the platform.">
          <div className="grid gap-4 md:grid-cols-3">
            <StatCard label="Completed trades" value={systemSummary.completed} note="Settled" tone="success" format="plain" />
            <StatCard label="Failed trades" value={systemSummary.failed} note="Exceptions" tone="danger" format="plain" />
            <StatCard label="Active traders" value={systemSummary.activeUsers} note="Unique users" tone="warning" format="plain" />
          </div>
        </SectionCard>
      ) : null}

      {currentSection === 'Trades' ? (
        <SectionCard title="System Trade Monitor" subtitle="A consolidated view of recent trading activity across all available accounts.">
          {error ? <div className="mb-4"><ErrorState error={error} /></div> : null}
          {systemLoading ? (
            <LoadingState label="Loading system trade activity..." />
          ) : (
            <DataTable
              rowKey={(row) => `${row.userId}-${row.id}`}
              rows={systemTrades.slice(0, 20)}
              emptyTitle="No trade activity"
              emptyDescription="System-wide trade history is not available yet."
              columns={[
                { key: 'userId', header: 'User ID' },
                { key: 'username', header: 'Username' },
                { key: 'symbol', header: 'Symbol' },
                { key: 'type', header: 'Type' },
                { key: 'quantity', header: 'Qty' },
                { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
                {
                  key: 'status',
                  header: 'Status',
                  render: (row) => (
                    <Badge tone={row.status === 'COMPLETED' || row.status === 'EXECUTED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'neutral'}>
                      {row.status}
                    </Badge>
                  ),
                },
                { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
              ]}
            />
          )}
        </SectionCard>
      ) : null}

      {currentSection === 'Accounts' ? (
        <SectionCard title="Account Registry" subtitle="Search the user base and select an account to inspect wallet, portfolio, and trade activity.">
          {error ? <div className="mb-4"><ErrorState error={error} /></div> : null}
          <form onSubmit={searchByUsername} className="mb-4 grid gap-3 md:grid-cols-[1fr_1fr_auto]">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Filter all users"
              className="rh-input mt-0"
            />
            <input
              value={usernameQuery}
              onChange={(e) => setUsernameQuery(e.target.value)}
              placeholder="Search by username"
              className="rh-input mt-0"
            />
            <button type="submit" className="rh-button-secondary">
              Search
            </button>
          </form>

          <DataTable
            rowKey={(row) => row.id}
            rows={filtered}
            emptyTitle="No users"
            emptyDescription="No user records are available."
            columns={[
              { key: 'id', header: 'ID' },
              { key: 'username', header: 'Username' },
              { key: 'email', header: 'Email' },
              { key: 'role', header: 'Role', render: (row) => <Badge tone={row.role === 'ADMIN' ? 'warning' : 'neutral'}>{row.role}</Badge> },
              {
                key: 'actions',
                header: 'Actions',
                render: (row) => (
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        setSelectedUserId(String(row.id));
                        setForm({ id: row.id, username: row.username, email: row.email });
                        navigate('/admin/review');
                      }}
                      className="rh-button-secondary !rounded-xl !px-3 !py-1.5 !text-xs"
                    >
                      Review
                    </button>
                    <button type="button" onClick={() => updateUser(row.id)} className="rh-button-ghost !rounded-xl !px-3 !py-1.5 !text-xs">
                      Save
                    </button>
                    <button type="button" onClick={() => deleteUser(row.id)} className="rh-button-danger !rounded-xl !px-3 !py-1.5 !text-xs">
                      Delete
                    </button>
                  </div>
                ),
              },
            ]}
          />
        </SectionCard>
      ) : null}

      {currentSection === 'Review' ? (
        <div className="space-y-6">
          <SectionCard
            title="Selected Account Oversight"
            subtitle={selectedUser ? `Inspecting ${selectedUser.username}'s wallet, holdings, and trade activity.` : 'Select an account from the registry to begin review.'}
          >
            {selectedLoading ? (
              <LoadingState label="Loading selected account..." />
            ) : selectedUser ? (
              <div className="space-y-6">
                <div className="grid gap-4 md:grid-cols-4">
                  <StatCard label="Account ID" value={selectedUser.id} note="Selected" format="plain" />
                  <StatCard label="Wallet balance" value={selectedSnapshot.wallet?.balance ?? 0} note={selectedSnapshot.wallet?.currency || 'USD'} tone="success" />
                  <StatCard label="Holdings" value={selectedSnapshot.portfolio.length} note="Positions" tone="warning" format="plain" />
                  <StatCard label="Trade count" value={selectedSnapshot.trades.length} note="History" tone="accent" format="plain" />
                </div>

                <div className="grid gap-6 xl:grid-cols-2">
                  <SectionCard title="Wallet Snapshot" subtitle="Current balance and recent funding movements for the selected account.">
                    <div className="space-y-4">
                      <DataTable
                        rowKey={(row) => row.key}
                        rows={[
                          { key: 'username', label: 'Username', value: selectedUser.username },
                          { key: 'email', label: 'Email', value: selectedUser.email },
                          { key: 'role', label: 'Role', value: selectedUser.role },
                          { key: 'balance', label: 'Wallet balance', value: formatMoney(selectedSnapshot.wallet?.balance) },
                          { key: 'currency', label: 'Currency', value: selectedSnapshot.wallet?.currency || 'USD' },
                        ]}
                        columns={[
                          { key: 'label', header: 'Field' },
                          { key: 'value', header: 'Value' },
                        ]}
                      />
                      <DataTable
                        rowKey={(row) => row.id}
                        rows={selectedSnapshot.transactions.slice(0, 6)}
                        emptyTitle="No wallet activity"
                        emptyDescription="No transaction history is available for this account."
                        columns={[
                          { key: 'type', header: 'Type' },
                          { key: 'amount', header: 'Amount', render: (row) => formatMoney(row.amount) },
                          { key: 'balanceAfter', header: 'Balance after', render: (row) => formatMoney(row.balanceAfter) },
                          { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
                        ]}
                      />
                    </div>
                  </SectionCard>

                  <SectionCard title="Portfolio Snapshot" subtitle="Current holdings and position values for the selected account.">
                    <DataTable
                      rowKey={(row) => `${row.id}-${row.symbol}`}
                      rows={selectedSnapshot.portfolio}
                      emptyTitle="No holdings"
                      emptyDescription="This user does not currently hold any portfolio positions."
                      columns={[
                        { key: 'symbol', header: 'Symbol' },
                        { key: 'quantity', header: 'Qty' },
                        { key: 'averagePrice', header: 'Avg price', render: (row) => formatMoney(row.averagePrice) },
                        { key: 'totalInvested', header: 'Invested', render: (row) => formatMoney(row.totalInvested) },
                      ]}
                    />
                  </SectionCard>
                </div>

                <SectionCard title="User Trade History" subtitle="Recent trade records for the selected account.">
                  <DataTable
                    rowKey={(row) => row.id}
                    rows={selectedSnapshot.trades}
                    emptyTitle="No trades"
                    emptyDescription="This account has no recorded trades."
                    columns={[
                      { key: 'id', header: 'ID' },
                      { key: 'symbol', header: 'Symbol' },
                      { key: 'type', header: 'Type' },
                      { key: 'quantity', header: 'Qty' },
                      { key: 'price', header: 'Price', render: (row) => formatMoney(row.price) },
                      {
                        key: 'status',
                        header: 'Status',
                        render: (row) => (
                          <Badge tone={row.status === 'COMPLETED' || row.status === 'EXECUTED' ? 'success' : row.status === 'FAILED' ? 'danger' : 'neutral'}>
                            {row.status}
                          </Badge>
                        ),
                      },
                      { key: 'createdAt', header: 'Created', render: (row) => formatDateTime(row.createdAt) },
                    ]}
                  />
                </SectionCard>
              </div>
            ) : (
              <div className="rounded-2xl border border-dashed border-line bg-[#1a1f0f] p-4 text-sm text-muted">
                Select an account from the registry to review wallet balance, portfolio positions, and trade activity.
              </div>
            )}
          </SectionCard>

          <SectionCard title="Privileged Edit Panel" subtitle="Apply controlled updates to the selected account record.">
            {error ? <div className="mb-4"><ErrorState error={error} /></div> : null}
            <div className="grid gap-4 md:grid-cols-3">
              <input
                value={form.id}
                onChange={(e) => setForm((current) => ({ ...current, id: e.target.value }))}
                placeholder="User ID"
                className="rh-input mt-0"
              />
              <input
                value={form.username}
                onChange={(e) => setForm((current) => ({ ...current, username: e.target.value }))}
                placeholder="Username"
                className="rh-input mt-0"
              />
              <input
                value={form.email}
                onChange={(e) => setForm((current) => ({ ...current, email: e.target.value }))}
                placeholder="Email"
                className="rh-input mt-0"
              />
            </div>
            <div className="mt-4 flex flex-wrap gap-2">
              <button type="button" onClick={loadUsers} className="rh-button-secondary">
                Reload directory
              </button>
              <button type="button" onClick={() => updateUser(form.id)} className="rh-button-primary">
                Save changes
              </button>
            </div>
          </SectionCard>
        </div>
      ) : null}
    </div>
  );
}
