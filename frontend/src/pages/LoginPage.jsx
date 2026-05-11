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
      <div className="hidden border-r border-line bg-[linear-gradient(180deg,#050505,#0b0b0b)] text-white lg:flex lg:flex-col lg:justify-between lg:p-12">
        <div>
          <div className="flex items-center gap-3 text-sm uppercase tracking-[0.28em] text-white/60">
            <ShieldCheck className="h-5 w-5 text-accent" />
            Stonks Platform
          </div>
          <h1 className="mt-6 max-w-xl text-5xl font-semibold leading-tight text-white">Invest, track, and trade in one focused workspace.</h1>
          <p className="mt-4 max-w-lg text-sm text-white/72">
            Access live market tools, portfolio insights, funding controls, and account management from a single secure platform.
          </p>
        </div>
        <div className="rounded-[28px] border border-white/10 bg-white/5 p-5 text-sm text-white/72 backdrop-blur">
          Designed for fast decisions, clear visibility, and secure access.
        </div>
      </div>

      <div className="flex items-center justify-center p-6 lg:p-10">
        <div className="w-full max-w-md rounded-[32px] border border-line bg-[linear-gradient(180deg,rgba(16,16,16,0.98),rgba(10,10,10,0.96))] p-7 shadow-soft backdrop-blur">
          <div className="inline-flex rounded-full border border-[#b7df43] bg-[rgba(195,245,60,0.14)] px-3 py-1 text-xs font-medium uppercase tracking-[0.22em] text-accent">
            Secure access
          </div>
          <h2 className="mt-4 text-2xl font-semibold">Sign in</h2>
          <p className="mt-1 text-sm text-muted">Sign in to access your trading workspace.</p>

          {error ? <div className="mt-4"><ErrorState error={error} /></div> : null}

          <form onSubmit={onSubmit} className="mt-5 space-y-4">
            <label className="block text-sm text-muted">
              Username
              <input
                value={form.username}
                onChange={(e) => setForm((current) => ({ ...current, username: e.target.value }))}
                className="rh-input"
                required
              />
            </label>
            <label className="block text-sm text-muted">
              Password
              <input
                value={form.password}
                onChange={(e) => setForm((current) => ({ ...current, password: e.target.value }))}
                type="password"
                className="rh-input"
                required
              />
            </label>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-accent px-4 py-3 font-semibold text-black shadow-glow transition hover:translate-y-[-1px] disabled:opacity-60"
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
