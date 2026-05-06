import { useEffect, useState } from 'react';
import PageHeader from '../components/common/PageHeader';
import SectionCard from '../components/common/SectionCard';
import StatCard from '../components/common/StatCard';
import DataTable from '../components/common/DataTable';
import { ErrorState, LoadingState } from '../components/common/AsyncState';
import { useAuth } from '../context/AuthContext';

export default function ProfilePage() {
  const { user, session, updateProfile, refreshUser } = useAuth();
  const [form, setForm] = useState({ username: '', email: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setForm({
      username: user?.username || '',
      email: user?.email || '',
    });
  }, [user?.id]);

  async function save(event) {
    event.preventDefault();
    if (!user?.id) return;
    setLoading(true);
    setError('');
    try {
      await updateProfile(user.id, form);
      await refreshUser();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  if (!user) return <LoadingState label="Loading profile..." />;

  return (
    <div className="space-y-6">
      <PageHeader title="Profile" subtitle="User settings and identity from the user-service profile model." />

      <div className="grid gap-4 md:grid-cols-4">
        <StatCard label="User ID" value={user.id} note="Profile" />
        <StatCard label="Username" value={user.username} note="Login name" />
        <StatCard label="Email" value={user.email} note="Contact" />
        <StatCard label="Role" value={user.role} note="Access level" />
      </div>

      <div className="grid gap-6 xl:grid-cols-[0.8fr_1.2fr]">
        <SectionCard title="Edit profile" subtitle="Username and email only. Password stays in auth-service.">
          {error ? <ErrorState error={error} /> : null}
          <form onSubmit={save} className="space-y-4">
            <label className="block text-sm text-muted">
              Username
              <input value={form.username} onChange={(e) => setForm((current) => ({ ...current, username: e.target.value }))} className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
            </label>
            <label className="block text-sm text-muted">
              Email
              <input value={form.email} onChange={(e) => setForm((current) => ({ ...current, email: e.target.value }))} type="email" className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text" required />
            </label>
            <button disabled={loading} type="submit" className="rounded-lg bg-gradient-to-r from-accent to-accent2 px-4 py-2.5 font-semibold text-bg">
              Save profile
            </button>
          </form>
        </SectionCard>

        <SectionCard title="Session record" subtitle="Token metadata and current auth state.">
          <DataTable
            rowKey={(row) => row.key}
            rows={[
              { key: 'username', label: 'Session username', value: session?.username || '-' },
              { key: 'role', label: 'Session role', value: session?.role || '-' },
              { key: 'token', label: 'Token present', value: session?.token ? 'Yes' : 'No' },
              { key: 'backend', label: 'Profile endpoint', value: '/users/username/{username}' },
            ]}
            columns={[
              { key: 'label', header: 'Field' },
              { key: 'value', header: 'Value' },
            ]}
          />
        </SectionCard>
      </div>
    </div>
  );
}
