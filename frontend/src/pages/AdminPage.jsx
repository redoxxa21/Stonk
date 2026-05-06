import { useEffect, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import DataTable from '../components/common/DataTable';
import Badge from '../components/common/Badge';
import { LoadingState, ErrorState } from '../components/common/AsyncState';
import { apiClient } from '../lib/apiClient';
import { useAuth } from '../context/AuthContext';

export default function AdminPage() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [query, setQuery] = useState('');
  const [form, setForm] = useState({ id: '', username: '', email: '' });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  async function loadUsers() {
    setLoading(true);
    setError('');
    try {
      const data = await apiClient.get('/users');
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  const filtered = users.filter((item) => `${item.id} ${item.username} ${item.email} ${item.role}`.toLowerCase().includes(query.toLowerCase()));

  async function searchByUsername(event) {
    event.preventDefault();
    try {
      const userByName = await apiClient.get(`/users/username/${encodeURIComponent(form.username)}`);
      setUsers([userByName]);
    } catch (err) {
      setError(err.message);
    }
  }

  async function updateUser(id) {
    try {
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
      await apiClient.delete(`/users/${id}`);
      await loadUsers();
    } catch (err) {
      setError(err.message);
    }
  }

  if (loading) return <LoadingState label="Loading users..." />;
  if (error && !users.length) return <ErrorState error={error} onRetry={loadUsers} />;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Admin Console"
        subtitle={`Administrative user management surface for ${user?.username || 'the current session'}.`}
      />

      <SectionCard title="User registry" subtitle="Real user-service records.">
        <form onSubmit={searchByUsername} className="mb-4 grid gap-3 md:grid-cols-[1fr_1fr_auto]">
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Filter all users"
            className="rounded-lg border border-line bg-panel2 px-3 py-2 text-text"
          />
          <input
            value={form.username}
            onChange={(e) => setForm((current) => ({ ...current, username: e.target.value }))}
            placeholder="Search by username"
            className="rounded-lg border border-line bg-panel2 px-3 py-2 text-text"
          />
          <button type="submit" className="rounded-lg border border-line bg-white/5 px-4 py-2.5 text-sm">
            Search
          </button>
        </form>

        <DataTable
          rowKey={(row) => row.id}
          rows={filtered}
          emptyTitle="No users"
          emptyDescription="The user service returned an empty collection."
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
                  <button type="button" onClick={() => setForm({ id: row.id, username: row.username, email: row.email })} className="rounded-md border border-line px-3 py-1.5 text-xs">
                    Load
                  </button>
                  <button type="button" onClick={() => updateUser(row.id)} className="rounded-md border border-line px-3 py-1.5 text-xs">
                    Save
                  </button>
                  <button type="button" onClick={() => deleteUser(row.id)} className="rounded-md border border-line px-3 py-1.5 text-xs">
                    Delete
                  </button>
                </div>
              ),
            },
          ]}
        />
      </SectionCard>

      <SectionCard title="Quick edit" subtitle="Update the selected record by ID.">
        <div className="grid gap-4 md:grid-cols-3">
          <input
            value={form.id}
            onChange={(e) => setForm((current) => ({ ...current, id: e.target.value }))}
            placeholder="User ID"
            className="rounded-lg border border-line bg-panel2 px-3 py-2 text-text"
          />
          <input
            value={form.username}
            onChange={(e) => setForm((current) => ({ ...current, username: e.target.value }))}
            placeholder="Username"
            className="rounded-lg border border-line bg-panel2 px-3 py-2 text-text"
          />
          <input
            value={form.email}
            onChange={(e) => setForm((current) => ({ ...current, email: e.target.value }))}
            placeholder="Email"
            className="rounded-lg border border-line bg-panel2 px-3 py-2 text-text"
          />
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          <button type="button" onClick={loadUsers} className="rounded-lg border border-line bg-white/5 px-4 py-2.5 text-sm">
            Reload
          </button>
          <button type="button" onClick={() => updateUser(form.id)} className="rounded-lg bg-gradient-to-r from-accent to-accent2 px-4 py-2.5 font-semibold text-bg">
            Save changes
          </button>
        </div>
      </SectionCard>
    </div>
  );
}
