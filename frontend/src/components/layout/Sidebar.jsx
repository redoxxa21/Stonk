import { NavLink } from 'react-router-dom';
import { BarChart3, Zap } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getNavigationForRole } from '../../lib/navigation';

export default function Sidebar({ mobile = false, onNavigate }) {
  const { user } = useAuth();
  const navItems = getNavigationForRole(user?.role);
  const baseClass = mobile
    ? 'app-sidebar flex h-full w-full flex-col backdrop-blur'
    : 'app-sidebar hidden w-80 shrink-0 border-r border-line backdrop-blur lg:flex lg:flex-col';

  return (
    <aside className={baseClass}>
      <div className="border-b border-line px-5 py-6">
        <div className="flex items-center gap-3">
          <div className="rounded-2xl bg-accent p-3 text-black shadow-glow">
            <BarChart3 className="h-5 w-5" />
          </div>
          <div>
            <div className="text-xs uppercase tracking-[0.24em] text-muted">Stonk Platform</div>
            <div className="mt-1 text-xl font-semibold text-text">Trading Console</div>
          </div>
        </div>
        <div className="app-session-panel mt-4 rounded-3xl border p-4 shadow-soft">
          <div className="flex items-center gap-2 text-xs uppercase tracking-[0.24em] text-white/55">
            <Zap className="h-3.5 w-3.5 text-accent" />
            Session lane
          </div>
          <div className="mt-3 text-base font-semibold text-white">{user?.username || 'Unknown user'}</div>
          {/*<div className="mt-1 text-sm text-white/68">Role: {user?.role || 'USER'}</div>*/}
        </div>
      </div>

      <nav className="flex-1 px-3 py-5">
        <div className="mb-3 px-3 text-xs uppercase tracking-[0.22em] text-muted">Navigation</div>
        <div className="space-y-1.5">
          {navItems.map(({ to, label, description, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              onClick={onNavigate}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-2xl px-3 py-3 text-sm transition ${
                  isActive
                    ? 'border border-[#b7df43] bg-[rgba(195,245,60,0.14)] text-text shadow-sm'
                    : 'border border-transparent text-muted hover:border-line hover:bg-[#141414] hover:text-text'
                }`
              }
            >
              <div className="rounded-xl border border-line bg-[#141414] p-2">
                <Icon className="h-4 w-4" />
              </div>
              <div>
                <div className="font-medium">{label}</div>
                <div className="text-xs text-muted">{description}</div>
              </div>
            </NavLink>
          ))}
        </div>

      </nav>
    </aside>
  );
}
