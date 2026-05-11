import { NavLink, useLocation } from 'react-router-dom';
import { Menu, RefreshCw, Search, LogOut, MoonStar, SunMedium } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { usePlatform } from '../../context/PlatformContext';
import { useTheme } from '../../context/ThemeContext';
import Badge from '../common/Badge';
import { findNavigationItem, getNavigationForRole } from '../../lib/navigation';

export default function Topbar({ onToggleSidebar, onSearch }) {
  const { user, logout } = useAuth();
  const { refreshMarket, marketLoading, socketStatus } = usePlatform();
  const { isLight, toggleTheme } = useTheme();
  const location = useLocation();
  const navItems = getNavigationForRole(user?.role);
  const currentPage = findNavigationItem(location.pathname, user?.role);

  return (
    <header className="app-topbar sticky top-0 z-20 border-b border-line backdrop-blur-xl">
      <div className="flex flex-wrap items-center gap-3 px-4 py-4 lg:px-6">
        <button
          type="button"
          onClick={onToggleSidebar}
          className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-line bg-panel text-text lg:hidden"
        >
          <Menu className="h-4 w-4" />
        </button>

        <div className="min-w-0 flex-1">
          <div className="text-xs uppercase tracking-[0.24em] text-muted">Workspace</div>
          <div className="mt-1 text-lg font-semibold text-text">{currentPage?.label || 'Trading Console'}</div>
          <div className="text-sm text-muted">{currentPage?.description || 'Gateway-driven trading workspace'}</div>
        </div>

        <div className="app-searchbar hidden min-w-[280px] flex-1 items-center gap-3 rounded-2xl border border-line px-3 py-2 shadow-sm md:flex">
          <Search className="h-4 w-4 text-muted" />
          <input
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                onSearch?.(event.currentTarget.value);
              }
            }}
            placeholder="Search page or symbol like AAPL"
            className="w-full bg-transparent text-sm outline-none placeholder:text-muted"
          />
        </div>

        <div className="ml-auto flex items-center gap-2">
          <button
            type="button"
            onClick={toggleTheme}
            className="rh-button-secondary !rounded-xl !px-3 !py-2"
            aria-label={isLight ? 'Switch to dark mode' : 'Switch to light mode'}
            title={isLight ? 'Switch to dark mode' : 'Switch to light mode'}
          >
            {isLight ? <MoonStar className="h-4 w-4" /> : <SunMedium className="h-4 w-4" />}
          </button>
          <Badge tone={socketStatus === 'connected' ? 'success' : socketStatus === 'connecting' ? 'warning' : 'neutral'}>
            Live
          </Badge>
          <button
            type="button"
            onClick={refreshMarket}
            className="rh-button-secondary"
          >
            <RefreshCw className={`h-4 w-4 ${marketLoading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
          <Badge tone={user?.role === 'ADMIN' ? 'warning' : 'accent'}>{user?.role || 'USER'}</Badge>
          <button
            type="button"
            onClick={logout}
            className="rh-button-ghost"
          >
            <LogOut className="h-4 w-4" />
            Sign out
          </button>
        </div>
      </div>

      <div className="hidden gap-2 overflow-x-auto px-4 pb-4 lg:flex lg:px-6">
        {navItems.map(({ to, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) =>
              `rounded-full px-3 py-1.5 text-sm transition ${
                isActive ? 'bg-accent text-black shadow-sm' : 'bg-[#131313] text-muted hover:bg-[#1a1a1a] hover:text-text'
              }`
            }
          >
            {label}
          </NavLink>
        ))}
      </div>
    </header>
  );
}
