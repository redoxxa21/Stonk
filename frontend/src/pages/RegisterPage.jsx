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
      <div className="flex items-center justify-center p-6 lg:p-10">
        <div className="w-full max-w-md rounded-[32px] border border-line bg-[linear-gradient(180deg,rgba(16,16,16,0.98),rgba(10,10,10,0.96))] p-7 shadow-soft backdrop-blur">
          <div className="inline-flex rounded-full border border-[#b7df43] bg-[rgba(195,245,60,0.14)] px-3 py-1 text-xs font-medium uppercase tracking-[0.22em] text-accent">
            Quick onboarding
          </div>
          <h2 className="mt-4 text-2xl font-semibold">Create account</h2>
          <p className="mt-1 text-sm text-muted">Create your account to start tracking markets and managing your portfolio.</p>

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
              Email
              <input
                value={form.email}
                onChange={(e) => setForm((current) => ({ ...current, email: e.target.value }))}
                type="email"
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
          <div className="rounded-2xl border border-line bg-[#111111] px-3 py-3 text-sm text-muted">
              New registrations are created as <span className="font-medium text-text">USER</span> accounts.
          </div>
            <button
              type="submit"
              disabled={loading}
              className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-accent px-4 py-3 font-semibold text-black shadow-glow transition hover:translate-y-[-1px] disabled:opacity-60"
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

      <div className="hidden border-l border-line bg-[radial-gradient(circle_at_top_right,_rgba(195,245,60,0.08),_transparent_18%),linear-gradient(180deg,_#080808,_#101010)] lg:flex lg:flex-col lg:justify-between lg:p-12">
        <div>
          <div className="text-sm uppercase tracking-[0.28em] text-muted">Stonks Platform</div>
          <h1 className="mt-6 max-w-xl text-5xl font-semibold leading-tight text-text">Join the market with a setup flow that feels like a real finance product.</h1>
          <p className="mt-4 max-w-lg text-sm text-muted">
            Create your account and step into a streamlined experience for trading, funding, and portfolio tracking.
          </p>
        </div>
      </div>
    </div>
  );
}
