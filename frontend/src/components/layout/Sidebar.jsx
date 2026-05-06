import { NavLink } from 'react-router-dom';
import {
  ArrowUpDown,
  CandlestickChart,
  LayoutDashboard,
  Layers3,
  ListOrdered,
  Shield,
  UserCircle2,
  WalletCards,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/market', label: 'Market', icon: CandlestickChart },
  { to: '/wallet', label: 'Wallet', icon: WalletCards },
  { to: '/portfolio', label: 'Portfolio', icon: Layers3 },
  { to: '/orders', label: 'Orders', icon: ListOrdered },
  { to: '/trades', label: 'Trades', icon: ArrowUpDown },
  { to: '/profile', label: 'Profile', icon: UserCircle2 },
];

export default function Sidebar({ mobile = false, onNavigate }) {
  const { user } = useAuth();
  const baseClass = mobile
    ? 'flex h-full w-full flex-col bg-panel'
    : 'hidden w-72 shrink-0 border-r border-line bg-panel/95 lg:flex lg:flex-col';

  return (
    <aside className={baseClass}>
      <div className="border-b border-line px-5 py-5">
        <div className="text-xs uppercase tracking-[0.2em] text-muted">Stonks Platform</div>
        <div className="mt-2 text-lg font-semibold">Trading Console</div>
        <div className="mt-1 text-sm text-muted">Gateway-driven microservices UI</div>
      </div>

      <nav className="flex-1 px-3 py-4">
        <div className="space-y-1">
          {navItems.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              onClick={onNavigate}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${
                  isActive ? 'bg-white/10 text-text' : 'text-muted hover:bg-white/5 hover:text-text'
                }`
              }
            >
              <Icon className="h-4 w-4" />
              <span>{label}</span>
            </NavLink>
          ))}
        </div>

        <div className="mt-6 rounded-lg border border-line bg-panel2 p-4">
          <div className="text-xs uppercase tracking-wide text-muted">Session</div>
          <div className="mt-2 text-sm font-medium">{user?.username || 'Unknown user'}</div>
          <div className="mt-1 text-xs text-muted">{user?.role || 'USER'}</div>
          {user?.role === 'ADMIN' ? (
            <div className="mt-3 inline-flex items-center gap-2 rounded-full border border-amber-400/30 bg-amber-400/10 px-2.5 py-1 text-xs text-amber-200">
              <Shield className="h-3.5 w-3.5" />
              Admin access
            </div>
          ) : null}
        </div>
      </nav>
    </aside>
  );
}
