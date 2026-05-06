import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { ArrowRight, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { ErrorState } from '../components/common/AsyncState';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function onSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(form);
      navigate(location.state?.from || '/', { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid min-h-screen lg:grid-cols-[1.1fr_0.9fr]">
      <div className="hidden border-r border-line bg-[radial-gradient(circle_at_top,_rgba(94,234,212,0.14),_transparent_42%),linear-gradient(180deg,_#08111f,_#0f1728)] lg:flex lg:flex-col lg:justify-between lg:p-10">
        <div>
          <div className="flex items-center gap-3 text-sm uppercase tracking-[0.28em] text-muted">
            <ShieldCheck className="h-5 w-5 text-accent" />
            Stonks Platform
          </div>
          <h1 className="mt-6 max-w-xl text-4xl font-semibold leading-tight">Trade, manage, and monitor the platform from one console.</h1>
          <p className="mt-4 max-w-lg text-sm text-muted">
            This frontend is wired to the API Gateway and all microservices. Auth, users, wallet, portfolio, orders, trades, and market data are
            loaded dynamically from the backend.
          </p>
        </div>
        <div className="text-sm text-muted">API Gateway first. No mock business logic.</div>
      </div>

      <div className="flex items-center justify-center p-6">
        <div className="w-full max-w-md rounded-lg border border-line bg-panel p-6 shadow-soft">
          <h2 className="text-xl font-semibold">Sign in</h2>
          <p className="mt-1 text-sm text-muted">Use your auth-service account to unlock the platform.</p>

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
              Password
              <input
                value={form.password}
                onChange={(e) => setForm((current) => ({ ...current, password: e.target.value }))}
                type="password"
                className="mt-2 w-full rounded-lg border border-line bg-panel2 px-3 py-2 text-text outline-none focus:border-accent"
                required
              />
            </label>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-gradient-to-r from-accent to-accent2 px-4 py-2.5 font-semibold text-bg disabled:opacity-60"
            >
              {loading ? 'Signing in...' : 'Open session'}
              <ArrowRight className="h-4 w-4" />
            </button>
          </form>

          <div className="mt-4 text-sm text-muted">
            New account? <Link className="text-accent" to="/register">Register</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
