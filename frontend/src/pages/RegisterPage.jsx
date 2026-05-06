import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ErrorState } from '../components/common/AsyncState';
import { ArrowRight } from 'lucide-react';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', role: 'USER' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function onSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      await register(form);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-[0.9fr_1.1fr]">
      <div className="flex items-center justify-center p-6">
        <div className="w-full max-w-md rounded-lg border border-line bg-panel p-6 shadow-soft">
          <h2 className="text-xl font-semibold">Create account</h2>
          <p className="mt-1 text-sm text-muted">Registers through auth-service and hydrates the session immediately.</p>

          {error ? <div className="mt-4"><ErrorState error={error} /></div> : null}

          <form onSubmit={onSubmit} className="mt-5 space-y-4">
            <label className="block text-sm text-muted">
              Username
              <input
                value={form.username}
                onChange={(e) => setForm((current) => ({ ...current, username: e.target.value }))}
                className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text outline-none focus:border-accent"
                required
              />
            </label>
            <label className="block text-sm text-muted">
              Email
              <input
                value={form.email}
                onChange={(e) => setForm((current) => ({ ...current, email: e.target.value }))}
                type="email"
                className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text outline-none focus:border-accent"
                required
              />
            </label>
            <label className="block text-sm text-muted">
              Password
              <input
                value={form.password}
                onChange={(e) => setForm((current) => ({ ...current, password: e.target.value }))}
                type="password"
                className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text outline-none focus:border-accent"
                required
              />
            </label>
            <label className="block text-sm text-muted">
              Role
              <select
                value={form.role}
                onChange={(e) => setForm((current) => ({ ...current, role: e.target.value }))}
                className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text outline-none focus:border-accent"
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </label>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-accent to-accent2 px-4 py-2.5 font-semibold text-bg disabled:opacity-60"
            >
              {loading ? 'Creating...' : 'Register'}
              <ArrowRight className="h-4 w-4" />
            </button>
          </form>

          <div className="mt-4 text-sm text-muted">
            Existing account? <Link className="text-accent" to="/login">Sign in</Link>
          </div>
        </div>
      </div>

      <div className="hidden border-l border-line bg-[radial-gradient(circle_at_top,_rgba(96,165,250,0.14),_transparent_42%),linear-gradient(180deg,_#08111f,_#0f1728)] lg:flex lg:flex-col lg:justify-between lg:p-10">
        <div>
          <div className="text-sm uppercase tracking-[0.28em] text-muted">Stonks Platform</div>
          <h1 className="mt-6 max-w-xl text-4xl font-semibold leading-tight">A single trading surface for the entire backend.</h1>
          <p className="mt-4 max-w-lg text-sm text-muted">
            The user profile, wallet, portfolio, orders, trades, and market view all bind to backend microservices through the gateway.
          </p>
        </div>
      </div>
    </div>
  );
}
